/*
* TaskExecutor.java 
* Created on  202017/5/25 16:46 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.core.WebDriverPool;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.task.actions.IActionExecutor;
import com.ifeng.hippo.task.actions.PurseActionExecutor;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.worker.WorkerDescriptor;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务执行器
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskExecutor extends AbsExecutor{
    private final static Logger logger = Logger.getLogger(TaskExecutor.class);
    private RedisClient redisClient = RedisFactory.newInstance();
    private WebDriverPool pool;
    private WorkerDescriptor workerDescriptor;

    private static List<IActionExecutor> actionExecutors = new ArrayList<>();

    static {
        actionExecutors.add(new PurseActionExecutor());
    }

    public TaskExecutor(WebDriverPool wp ,WorkerDescriptor workerDescriptor ){
        this.pool = wp;
        this.workerDescriptor = workerDescriptor;
    }
    @Override
    public void execute(List<TaskFragment> tfs) {

        if (tfs == null || tfs.size() == 0 || pool == null) {
            return;
        }
        Collections.sort(tfs, (o1, o2) -> o1.getTargetPv() > o2.getTargetPv() ? 0 : 1);

        int loopTimes = 0;
        int lastLoopTimes = 0;
        Iterator<TaskFragment> ite = tfs.iterator();

        while (ite.hasNext()){
            TaskFragment tf = ite.next();
            loopTimes = tf.getTargetPv();
            for (int i = lastLoopTimes; i < loopTimes; i++) {
                PhantomJSDriver driver = null;
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        logger.error("thread InterruptedException! ");
                        return;
                    }
                    WebDriver wd = pool.getForTask(tf);
                    if (wd != null){
                        driver = (PhantomJSDriver)wd;
                    } else{
                        logger.error("can not get a webdriver for task and try it again.");
                        continue;
                    }

                    Thread.currentThread().sleep(0);

                    for (int k = 0; k < tfs.size();k++){
                        String ip = "";
                        try {
                            Proxy p0= (Proxy) driver.getCapabilities().asMap().get("proxy");

                            if (p0 != null){
                                if (p0.getHttpProxy() != null){
                                    ip = p0.getHttpProxy();
                                }else if(p0.getSocksProxy() != null){
                                    ip = p0.getSocksProxy();
                                }else{
                                    ip = workerDescriptor.getHostIp();
                                }
                            }
                            execute(tfs.get(k), driver, ip, true);
                        } catch (Exception er) {
                            logger.error(er);
                        }
                    }

                    Long end = System.currentTimeMillis();

                }catch (InterruptedException er){
                    logger.error("Task Executor Service Shuting Down!");
                    return;
                }  catch (Exception e) {
                    logger.error(e);
                }finally {
                    if (driver != null) {
                        pool.returnToBrokenPool(driver);
                    }
                }
            }
            lastLoopTimes = loopTimes;
            ite.remove();
        }
    }

    /**
     * 执行任务中的Action
     * @param tf
     */
    private void executeTaskActions(TaskFragment tf){
        if (tf.getActions() != null){
            tf.getActions().forEach(r-> actionExecutors.forEach(k-> k.execute(r)));
        }
    }

    private void execute(TaskFragment tf , PhantomJSDriver driver,
                         String proxy,boolean isFirst) throws Exception {
        String cookieKey = String.format(RedisPrefix.TASK_PROXY_COOKIE_PREFIX, tf.getTaskId(), proxy);
        boolean isUv = PvToUvRatioCaculator.toUv(tf.getPvToUvRatio());
        String pvStr = " PV";
        if (isUv && tf.getTaskType() != TaskType.CLICK) {
            long len = redisClient.llen(cookieKey);
            int index = (int) (Math.random() * len);
            String values = redisClient.lindex(cookieKey, index);
            if (values != null && !"".equals(values)) {
                Set<JSONObject> jsonObjects = (Set<JSONObject>) JSON.parseObject(values,Set.class);
                for (JSONObject obj : jsonObjects){
                    Cookie cookie = new Cookie(obj.getString("name"),obj.getString("value"),
                            obj.getString("domain"),obj.getString("path"),
                            obj.getDate("expiry"));
                    driver.manage().addCookie(cookie);
                }
            }
            pvStr = " UV";
        }
        try {
            if (isFirst) {
                driver.manage().timeouts().pageLoadTimeout(tf.getWaitTimeout(), TimeUnit.SECONDS)
                        .implicitlyWait(tf.getWaitTimeout(), TimeUnit.SECONDS);
            }
            long b = System.currentTimeMillis();
            int random = (int) (Math.random() * 10000);
            driver.get(tf.getUrl());
            driver.findElement(null).click();
            logger.info("page source:"+driver.getPageSource());
            driver.close();
            long e = System.currentTimeMillis();
            if ((e - b) < tf.getWaitTimeout() * 1000){
                String key = DateUtil.today() + "_" + tf.getTaskId();

                logger.info(tf.getTaskId() + " " + tf.getUrl() + pvStr + " " + proxy + " 1 sessionId " + driver.getSessionId());

                //执行TaskAction
                executeTaskActions(tf);

//                try {
//                    String rk = String.format(RedisPrefix.TASK_TARGET_PV_FINISHED_PREFIX, tf.getTaskId(), DateUtil.today());
//                    redisClient.incr(rk);
//                    redisClient.expireKey(rk, 86400);
//                } catch (Exception er) {
//                    logger.error("Url request finished，but update redis faild. TaskInfo: " + tf.toString());
//                }
            }

        }catch (TimeoutException er){
            logger.error("request url timeout task "+tf.toString());
        } catch (Exception er) {
            logger.error(er);
        }
        if (!isUv || tf.getTaskType() == TaskType.CLICK){
            Set<Cookie> cookies = driver.manage().getCookies();
            if (cookies != null && cookies.size() > 0) {
                redisClient.lpushString(cookieKey, JSON.toJSONString(cookies));
            }
        }
        if (tf.getSubFragments() != null && tf.getSubFragments().size() > 0) {
            for (TaskFragment tfi : tf.getSubFragments()) {
                execute(tfi, driver, proxy,false);
            }
        }
    }
}
