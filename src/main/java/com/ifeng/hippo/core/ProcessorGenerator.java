/*
* ProcesserPool.java 
* Created on  202017/7/25 10:50 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.*;
import com.ifeng.hippo.entity.*;
import com.ifeng.hippo.filters.ProxyFilter;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.proxy.Proxy;
import com.ifeng.hippo.proxy.ProxyType;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.task.PvToUvRatioCaculator;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.mongo.query.OrderByDirection;
import com.ifeng.redis.RedisClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_ANDROID_PREFIX;
import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_IOS_PREFIX;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ProcessorGenerator {
    private List<IFilter> filters = new ArrayList<>();
    private Logger logger = Logger.getLogger(getClass());
    private String binPath = "";
    private RedisClient redisClient = RedisFactory.newInstance();
    private String scriptPath;
    private String netName;
    private Context ctx;
    protected static ConcurrentHashMap<String, Integer> provincesMap = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> tagAppMap = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> tagPcMap = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> tagWapMap = new ConcurrentHashMap<>();
    private MongoCli mongoClient = MongoFactory.createMongoClient();
    private ReentrantLock lock = new ReentrantLock();

    public ProcessorGenerator(Context context) {
        this.binPath = context.getString("driver.path");
        this.scriptPath = context.getString("scriptPath");
        this.netName = context.getString("netName");
        this.ctx = context;

        try {
            mongoClient.changeDb("hippo");
            mongoClient.getCollection("province");

            provincesMap.clear();

            MongoSelect select = new MongoSelect();
            List<Province> provinces = mongoClient.selectList(select, Province.class);

            if (provinces != null) {
                for (Province province : provinces) {
                    provincesMap.put(province.getName().replace("省", "").replace("市", ""), province.getId());
                }
            }

            mongoClient.getCollection("label_info");
            List<LabelInfo> labelInfos = mongoClient.selectList(new MongoSelect().orderBy("cate", OrderByDirection.ASC), LabelInfo.class);

            if(labelInfos != null){
                Map<String, Integer> valueMap = new HashMap<>();
                for(LabelInfo labelInfo : labelInfos){
                    KeyValuePair<Integer, LabelInfo> appKeyValuePair = new KeyValuePair<>();
                    KeyValuePair<Integer, LabelInfo> pcKeyValuePair = new KeyValuePair<>();
                    KeyValuePair<Integer, LabelInfo> wapKeyValuePair = new KeyValuePair<>();
                    /** app */
                    if(valueMap.containsKey(labelInfo.getCate() + "app")){
                        int value = valueMap.get(labelInfo.getCate() + "app") + labelInfo.getAppProp();
                        appKeyValuePair.setK(value);
                        appKeyValuePair.setV(labelInfo);
                        valueMap.put(labelInfo.getCate() + "app", value);
                    } else {
                        int value = labelInfo.getAppProp();
                        appKeyValuePair.setK(value);
                        appKeyValuePair.setV(labelInfo);
                        valueMap.put(labelInfo.getCate() + "app", value);
                    }
                    List appList = tagAppMap.get(labelInfo.getCate());
                    if(appList == null){
                        appList = new ArrayList<KeyValuePair<Integer, LabelInfo>>();
                    }
                    appList.add(appKeyValuePair);
                    tagAppMap.put(labelInfo.getCate(), appList);

                    /** pc */
                    if(valueMap.containsKey(labelInfo.getCate() + "pc")){
                        int value = valueMap.get(labelInfo.getCate() + "pc") + labelInfo.getPcProp();
                        pcKeyValuePair.setK(value);
                        pcKeyValuePair.setV(labelInfo);
                        valueMap.put(labelInfo.getCate() + "pc", value);
                    } else {
                        int value = labelInfo.getPcProp();
                        pcKeyValuePair.setK(value);
                        pcKeyValuePair.setV(labelInfo);
                        valueMap.put(labelInfo.getCate() + "pc", value);
                    }
                    List pcList = tagPcMap.get(labelInfo.getCate());
                    if(pcList == null){
                        pcList = new ArrayList<KeyValuePair<Integer, LabelInfo>>();
                    }
                    pcList.add(pcKeyValuePair);
                    tagPcMap.put(labelInfo.getCate(), pcList);

                    /** wap */
                    if(valueMap.containsKey(labelInfo.getCate() + "wap")){
                        int value = valueMap.get(labelInfo.getCate() + "wap") + labelInfo.getWapProp();
                        wapKeyValuePair.setK(value);
                        wapKeyValuePair.setV(labelInfo);
                        valueMap.put(labelInfo.getCate() + "wap", value);
                    } else {
                        int value = labelInfo.getWapProp();
                        wapKeyValuePair.setK(value);
                        wapKeyValuePair.setV(labelInfo);
                        valueMap.put(labelInfo.getCate() + "wap", value);
                    }
                    List wapList = tagWapMap.get(labelInfo.getCate());
                    if(wapList == null){
                        wapList = new ArrayList<KeyValuePair<Integer, LabelInfo>>();
                    }
                    wapList.add(wapKeyValuePair);
                    tagWapMap.put(labelInfo.getCate(), wapList);
                }
            }
        } catch (Exception er) {

        } finally {

        }
    }

    public void registFilter(IFilter filter) {
        filters.add(filter);
    }

    public ProcessEx generate(TaskFragment tf) {
        ProcessEx process = null;
        try {
            lock.lock();
            process = new ProcessEx();
            Context context = new Context();
            context.putAll(ctx);

            context.put("taskFragment", tf);
            context.put("netName", netName);
            context.put("provincesMap", provincesMap);
            context.put("tagAppMap", tagAppMap);
            context.put("tagPcMap", tagPcMap);
            context.put("tagWapMap", tagWapMap);
            /** 获取代理信息和UA信息、进行API和DMP方式的处理*/
            filters.forEach(r -> r.filter(context));

            /** 获取代理数据 */
            Proxy proxy = (Proxy) context.getObject("proxy");

            if (proxy == null) {
                logger.info("no proxy available for " + tf.getTaskId());
                return null;
            }

            UserAgentInfo ua = (UserAgentInfo) context.getObject("userAgent");

            if (ua == null || ua.getUserAgent() == null) {
                logger.info("no ua available for " + tf.getTaskId());
                return null;
            }

            if ((tf.getTaskSource() == TaskSource.API || tf.getTaskSource() == TaskSource.ACCURATEOPERATIONS)
                    && ("".equals(tf.getData()) || null == tf.getData())) {
                return null;
            }

            String cookieKey = String.format(RedisPrefix.TASK_PROXY_COOKIE_PREFIX, tf.getTaskId(), ua.getId());

            String cookie = "null";

            /** 按照曝光点击比例分配类型 */
            int random = (int) (Math.random() * 100);
            boolean isUv = false;
            if (random <= tf.getPvToUvRatio()) {
                isUv = true;
            }

            if (isUv && tf.getTaskType() != TaskType.CLICK) {
                if (tf.getTaskSource() != TaskSource.API) {
                    /** 从Redis中获取对应的任务Cookie， 有Cookie则任务类型为点击 */
                    cookie = redisClient.getString(cookieKey);
                    cookie = (cookie == null || "".equals(cookie)) ? "null" : cookie;
                    if (cookie != null && !"null".equals(cookie)) {
                        process.setPvOrUv(" UV ");
                    }
                } else {
                }
            }

            /** 如果类型为点击强制使用点击类型 */
            if (tf.getTaskType() == TaskType.CLICK) {
                process.setPvOrUv(" UV ");
            }

            /** 设置UserAgent */
            process.setUserAgentInfo(ua);
            /** 设置代理池 */
            process.setProxyPoolKey(context.getString("proxyPoolKey"));


            String proxyArgsFormat = "--proxy-type=%s --proxy=%s";
            String proxyArgs;

            if (proxy.getProxyType() == ProxyType.HTTP) {
                proxyArgs = String.format(proxyArgsFormat, "http", proxy.getProxyIp() + ":" + proxy.getPort());
            } else {
                proxyArgs = String.format(proxyArgsFormat, "socks5", proxy.getProxyIp() + ":" + proxy.getPort());
            }

            /** 设置代理 */
            process.setProxy(proxy);
            tf.setProxyStr(proxy.toString());
            /** HTTPClient处理方式*/
            if (tf.getRequestType() == RequestType.HTTP_CLIENT) {
                KeyValuePair<HttpClient, HttpGet> kv = buildHttpClient(tf, cookie, proxy, ua);
                process.wrap(kv.getK());
                process.setRequest(kv.getV());
            } else {
                /** Webkit处理方式*/
                scriptPath = ctx.getString("scriptPath_" + tf.getTaskPosition().toString() + "_" + tf.getTaskType().toString());
                if (null != tf.getMainScriptPath() && !"".equals(tf.getMainScriptPath())) {
                    scriptPath = tf.getMainScriptPath();
                }
                process.wrap(Runtime.getRuntime().exec(binPath + " --web-security=no --ignore-ssl-errors=true --ssl-protocol=any " + proxyArgs + " " + scriptPath + " " + "\"" + JSON.toJSONString(tf).replace("\"", "\\\"").replace("&", "#amp#") + "\"" + " "
                        + "\"" + cookie.replace("\"", "\\\"").replace(" ", "@") + "\"" + " " + "\"" + ua.getUserAgent().replace(" ", "@") + "\""));
            }
            //重置进程启动开始时间
            tf.setBeginTime(System.currentTimeMillis());
            logger.info("start ad request,taskId:" + tf.getTaskId()+" uuid:"+tf.getUuid());
        } catch (Exception er) {
            er.printStackTrace();
            logger.error(er);
        } finally {
            lock.unlock();
        }

        return process;
    }

    /**
     * 组建HTTPClient请求
     * @param tf
     * @param cookieStr
     * @param proxy
     * @param ua
     * @return
     */
    private KeyValuePair<HttpClient,HttpGet> buildHttpClient(TaskFragment tf, String cookieStr, Proxy proxy, UserAgentInfo ua){
        HttpClient httpClient = null;
        KeyValuePair<HttpClient, HttpGet> kv = new KeyValuePair<>();
        try {

            CookieStore cs = new BasicCookieStore();

            JSONArray arr = JSON.parseArray(cookieStr);
            if (arr != null) {
                for (Object o : arr) {
                    try {
                        JSONObject b = (JSONObject) o;
                        String name = b.getString("name");
                        String value = b.getString("value");

                        BasicClientCookie bc = new BasicClientCookie(name, value);
                        bc.setPath(b.getString("path"));
                        bc.setDomain(b.getString("domain"));
                        if (b.containsKey("expiryDate"))
                            bc.setExpiryDate(new Date(b.getLong("expiryDate")));
                        cs.addCookie(bc);
                    } catch (Exception er) {
                    } finally {
                    }
                }
            }

            // 根据地址获取请求
            HttpGet request = new HttpGet(tf.getUrl());//这里发送get请求
            // 获取当前客户端对象
            RequestConfig config = RequestConfig.custom().setConnectTimeout(3 * 1000).setSocketTimeout(3000)
                    .setProxy(new HttpHost(proxy.getProxyIp(), proxy.getPort()))
                    .build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config)
                    .setDefaultCookieStore(cs).build();
            request.setHeader("User-Agent", ua.getUserAgent());
            request.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
            request.setHeader("Referer", tf.getReferer());

            kv.setV(request);
//            // 通过请求对象获取响应对象
//            HttpResponse response = httpClient.execute(request);
//            response.getEntity().getContent();
//            // 判断网络连接状态码是否正常(0--200都数正常)
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                result= EntityUtils.toString(response.getEntity(),"utf-8");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        kv.setK(httpClient);
        return kv;
    }

    public void closeAll() {
        try {
            logger.info("closeAll webdriver! killall phantomjs");
            Runtime.getRuntime().exec("killall phantomjs");
        } catch (IOException e) {
            logger.error(e);
        }
    }
}
