/*
* TaskExecutor.java 
* Created on  202017/5/25 16:46 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task;

import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.RequestType;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.core.ProcessEx;
import com.ifeng.hippo.core.ProcessorGenerator;
import com.ifeng.hippo.core.data.DataManager;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.entity.TimePair;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.task.actions.IActionExecutor;
import com.ifeng.hippo.task.actions.PurseActionExecutor;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.worker.WorkerDescriptor;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;
import org.openqa.selenium.TimeoutException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务执行器
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskExecutorExec extends AbsExecutor{
    private final static Logger logger = Logger.getLogger(TaskExecutorExec.class);
    private RedisClient redisClient = RedisFactory.newInstance();
    private WorkerDescriptor workerDescriptor;
    private static List<IActionExecutor> actionExecutors = new ArrayList<>();
    private ProcessorGenerator processorGenerator;

    static {
        actionExecutors.add(new PurseActionExecutor());
    }

    public TaskExecutorExec(ProcessorGenerator processerGenerator , WorkerDescriptor workerDescriptor ){
        this.processorGenerator = processerGenerator;
        this.workerDescriptor = workerDescriptor;
    }

    @Override
    public void execute(TaskFragment tf) {
        if (tf == null ||processorGenerator == null) {
            return;
        }
        if (!checkTimePair(tf)){
            return;
        }
        ProcessEx process = null;
        try {
            process = processorGenerator.generate(tf);
            if (process == null){
                logger.debug("no process available");
                return ;
            }
            execute(tf, process);

        } catch (Exception er) {
            logger.error(er);
        }
    }

    private boolean checkTimePair(TaskFragment tf){
        if (tf.getTimePairs() == null || tf.getTimePairs().size() == 0){
            return true;
        }

        boolean inTimePair = false;
        int now = Integer.valueOf(DateUtil.format(new Date(),"HHmm"));
        for(TimePair tp:tf.getTimePairs()){
            if(now >= tp.getBeginTime() &&
                    now <= (tp.getEndTime() - 9)){
                inTimePair = true;
                break;
            }
        }
        return inTimePair;
    }

    private void processWebkitInputStream(TaskFragment tf , ProcessEx process) throws Exception {

        try {
            BufferedReader reader = null;
            try {
                InputStream is = process.getInputStream();
                if (is == null){
                    logger.error("can not get inputstream。");
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    logger.debug(line);
                    processConsoleMessage(line,tf,process);
                }
            }catch (Exception er){
                er.printStackTrace();
                logger.error(er);
            }finally {
                if (reader != null){
                    reader.close();
                }
            }
        }catch (TimeoutException er){
            logger.error("request url timeout task "+tf.toString());
        } catch (Exception er) {
            logger.error(er);
        }
    }

    private void processConsoleMessage(String line,TaskFragment tf , ProcessEx process){
        long uaId = process.getUserAgentInfo().getId();
        String cookieKey = String.format(RedisPrefix.TASK_PROXY_COOKIE_PREFIX, tf.getTaskId(),uaId);
        String cookie = "[]";
        try {
            String successKey = String.valueOf(tf.getTaskId()) + "_success";
            String errorKey = String.valueOf(tf.getTaskId())+"_error";

            String proxyIdListKey = process.getProxyPoolKey();
            if ("".equals(proxyIdListKey) || null == proxyIdListKey) {
                proxyIdListKey = tf.getTaskType() == TaskType.EV ? RedisPrefix.PROXY_IP_LIST_EV_IDC : RedisPrefix.PROXY_IP_LIST_IDC;
                proxyIdListKey = String.format(proxyIdListKey,process.getProxy().getNetName());
            }

            if (line.startsWith("cookie:")) {
                cookie = line.replaceFirst("cookie:", "");
            } else if (line.startsWith("open error")) {
                logger.info(line +":"+ tf.getTaskId() +  process.getPvOrUv() +  tf.getUrl() + "  proxy "
                        + process.getProxy().toString() + " ua: " + uaId + " task uuid:"+tf.getUuid()
                        +" execute time:"+"  "+(System.currentTimeMillis() - tf.getBeginTime()) + " groupId:"+tf.getGroupId()
                        + " userAgent:"+process.getUserAgentInfo().getUserAgent() + " refer:"+tf.getReferer() + " timestamp:"+System.currentTimeMillis());
                redisClient.lrem(proxyIdListKey, process.getProxy().toString());

                if (DataManager.getProcessCount().containsKey(errorKey)) {
                    DataManager.getProcessCount().get(errorKey).incrementAndGet();
                } else {
                    DataManager.getProcessCount().put(errorKey, new AtomicInteger(1));
                }
            } else if (line.startsWith("open success")) {
                String rk = String.format(RedisPrefix.TASK_TARGET_PV_FINISHED_PREFIX, tf.getTaskId(), DateUtil.today());
                try {
                    redisClient.incr(rk);
                    redisClient.expireKey(rk, 7776000);
                } catch (Exception er) {
                    logger.error("Url request finished，but update redis faild. TaskInfo: " + tf.toString());
                }

                logger.info(line +":"+ tf.getTaskId() +  process.getPvOrUv() +  tf.getUrl()+ "  proxy " +
                        process.getProxy().toString() + " ua: " + uaId + " task uuid:"+tf.getUuid()+" execute time:"
                        +"  "+(System.currentTimeMillis() - tf.getBeginTime())+ " groupId:"+tf.getGroupId()
                        + " userAgent:"+process.getUserAgentInfo().getUserAgent() + " refer:"+tf.getReferer() + " timestamp:"+System.currentTimeMillis());

                if (DataManager.getProcessCount().containsKey(successKey)) {
                    DataManager.getProcessCount().get(successKey).incrementAndGet();
                } else {
                    DataManager.getProcessCount().put(successKey, new AtomicInteger(1));
                }
            } else if (line.startsWith("navigate:")) {
                logger.info(line + " " + tf.toString() + " ua:"+process.getUserAgentInfo().getUserAgent() + " refer:"+tf.getReferer() + " groupId:"+tf.getGroupId() + " task uuid:"+tf.getUuid()+ " timestamp:"+System.currentTimeMillis());
            } else if (line.startsWith("for monitor:")){
                logger.info(line);
            }
        } catch (Exception er){
            logger.error(er);
        } finally {
            if (cookie != null && !"[]".equals(cookie) && tf.getTaskType() == TaskType.EV) {
                try {
                    redisClient.setString(cookieKey, cookie);
                    redisClient.expireKey(cookieKey, 7 * 24 * 60 * 60);
                } catch (Exception e) {
                    logger.error(e);
                } finally {

                }
            }
        }
    }

    private void processHttpClientInputStream(TaskFragment tf , ProcessEx process) throws Exception {
        String line = "open error";
        try {

            KeyValuePair<String, String> res = process.execute(tf);
            line = res.getV();
            processConsoleMessage(line, tf, process);
            line = res.getK();

            processConsoleMessage(line, tf, process);
        } catch (Exception er) {
            processConsoleMessage(line, tf, process);
        } finally {
            process.releaseConnection();
        }
    }

    private void execute(TaskFragment tf , ProcessEx process) throws Exception {
        if (tf.getRequestType() == RequestType.HTTP_CLIENT){
            processHttpClientInputStream(tf,process);
        }else{
            processWebkitInputStream(tf,process);
        }
    }
}