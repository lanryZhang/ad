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
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.entity.UserAgentInfo;
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
    protected static ConcurrentHashMap<String,Integer> provincesMap = new ConcurrentHashMap<>();
    private MongoCli mongoClient = MongoFactory.createMongoClient();

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
        }catch (Exception er){

        }finally {

        }
    }

    public void registFilter(IFilter filter) {
        filters.add(filter);
    }

    public synchronized ProcessEx generate(TaskFragment tf) {
        ProcessEx process = new ProcessEx();
        try {
            Context context = new Context();
            context.putAll(ctx);

            context.put("taskFragment", tf);
            context.put("netName", netName);
            filters.forEach(r -> r.filter(context));
            UserAgentInfo ua = (UserAgentInfo) context.getObject("userAgent");

            if (ua == null || ua.getUserAgent() == null) {
                logger.debug("no ua available!");
                return null;
            }

            String cookieKey = String.format(RedisPrefix.TASK_PROXY_COOKIE_PREFIX, tf.getTaskId(), ua.getId());

            String cookie = "null";

            int random = (int)(Math.random() * 100);
            boolean isUv = false;
            if (random <= tf.getPvToUvRatio()) {
                isUv = true;
            }

            if (isUv && tf.getTaskType() != TaskType.CLICK ) {
                if ( tf.getTaskSource() != TaskSource.API) {
                    cookie = redisClient.getString(cookieKey);
                    cookie = (cookie == null || "".equals(cookie)) ? "null" : cookie;
                    if (cookie != null && !"null".equals(cookie)) {
                        process.setPvOrUv(" UV ");
                    }
                }else{

                }
            }


            if ( tf.getTaskType() == TaskType.CLICK){
                process.setPvOrUv(" UV ");
            }

            process.setUserAgentInfo(ua);
            process.setProxyPoolKey(context.getString("proxyPoolKey"));
            
            Proxy proxy = (Proxy) context.getObject("proxy");

            if (tf.getTaskSource() == TaskSource.API){
                String data = "";
                int i = 5;
                while(i-- > 0 && "".equals(data)){
                    data = getAPIResult(tf,proxy);
                }

                if (!"".equals(data)){
                    tf.setData(URLEncoder.encode(data,"utf-8"));
                    logger.info("api request success:"+data);
                }else{
                    return null;
                }
            }

            String proxyArgsFormat = "--proxy-type=%s --proxy=%s";
            String proxyArgs;
            if (proxy == null){
                logger.debug("no proxy available!");
                return null;
            }
            if (proxy.getProxyType() == ProxyType.HTTP) {
                proxyArgs = String.format(proxyArgsFormat, "http", proxy.getProxyIp()+":"+proxy.getPort());
            } else {
                proxyArgs = String.format(proxyArgsFormat, "socks5", proxy.getProxyIp()+":"+proxy.getPort());
            }
            process.setProxy(proxy);

            if (tf.getRequestType() == RequestType.HTTP_CLIENT) {
                KeyValuePair<HttpClient,HttpGet> kv = buildHttpClient(tf,cookie,proxy,ua);
                process.wrap(kv.getK());
                process.setRequest(kv.getV());
            }else {
                scriptPath = ctx.getString("scriptPath_" + tf.getTaskPosition().toString() + "_" + tf.getTaskType().toString());
                if (null != tf.getMainScriptPath() && !"".equals(tf.getMainScriptPath())){
                    scriptPath = tf.getMainScriptPath();
                }
                process.wrap(Runtime.getRuntime().exec(binPath + " --ignore-ssl-errors=true --ssl-protocol=any " + proxyArgs + " " + scriptPath + " " + "\"" + JSON.toJSONString(tf).replace("\"", "\\\"").replace("&", "#amp#") + "\"" + " "
                        + "\"" + cookie.replace("\"", "\\\"").replace(" ", "@") + "\"" + " " + "\"" + ua.getUserAgent().replace(" ", "@") + "\""));
            }
        } catch (Exception er) {
            er.printStackTrace();
            logger.error(er);
        }
        return process;
    }

    private String getAPIResult(TaskFragment tf,Proxy proxy){
        try {
            String url = tf.getApi();
            int rand = (int)(Math.random() * 10);
            String province = "广州";
            if (proxy.getAddr() != null)
                province = proxy.getAddr().split("省")[0].replace("市","");
            int provinceId = provincesMap.get(province);

            String key = "";
            String idfa_imei = "";
            if (tf.getDeviceInfo() == DeviceInfo.ANDROID || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand >=4)){
                key = String.format(DEVICE_ID_ANDROID_PREFIX,provinceId);
                idfa_imei = redisClient.lpopString(key);
                url = String.format(url,"android","",idfa_imei.replace("\"",""));
            }else if (tf.getDeviceInfo() == DeviceInfo.IPHONE || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand < 4)){
                key = String.format(DEVICE_ID_IOS_PREFIX,provinceId);
                idfa_imei = redisClient.lpopString(key);
                url = String.format(url,"ios",idfa_imei.replace("\"",""),"");
            }

            redisClient.rpushString(key,idfa_imei.replace("\"",""));
            url += "&rnd="+(int)(1000000 * Math.random());

            HttpClient httpClient = null;
            HttpGet request = new HttpGet(url);//这里发送get请求
            // 获取当前客户端对象
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2 * 1000).setSocketTimeout(2000)
//                    .setProxy(new HttpHost(proxy.getProxyIp(), proxy.getPort()))
                    .build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            request.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Accept-Language", "zh-cn,zh;q=0.5");

            HttpResponse response = httpClient.execute(request);

            response.getEntity().getContent();

            String res = EntityUtils.toString(response.getEntity());

            JSONObject jsonObject = null;

            try {
                /**把json字符串转换成json对象**/
                jsonObject = JSON.parseObject(res);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return jsonObject.toString();
        }catch (Exception ex){

        }
        return "";
    }

    private KeyValuePair<HttpClient,HttpGet> buildHttpClient(TaskFragment tf, String cookieStr, Proxy proxy, UserAgentInfo ua){
        HttpClient httpClient = null;
        KeyValuePair<HttpClient,HttpGet> kv = new KeyValuePair<>();
        try {

            CookieStore cs = new BasicCookieStore();

            JSONArray arr = JSON.parseArray(cookieStr);
            if (arr != null){
                for (Object o: arr){
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
                    }catch (Exception er){}
                    finally {}
                }
            }

            // 根据地址获取请求
            HttpGet request = new HttpGet(tf.getUrl());//这里发送get请求
            // 获取当前客户端对象
            RequestConfig config = RequestConfig.custom().setConnectTimeout(10 * 1000).setSocketTimeout(10000)
                    .setProxy(new HttpHost(proxy.getProxyIp(),proxy.getPort()))
                    .build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config)
                    .setDefaultCookieStore(cs).build();
            request.setHeader("User-Agent",ua.getUserAgent());
            request.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
            request.setHeader("Referer",tf.getReferer());

            kv.setV(request);
//            // 通过请求对象获取响应对象
//            HttpResponse response = httpClient.execute(request);
//            response.getEntity().getContent();
//            // 判断网络连接状态码是否正常(0--200都数正常)
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                result= EntityUtils.toString(response.getEntity(),"utf-8");
//            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
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
