/*
* ApiFilter.java 
* Created on  202018/1/10 19:27 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.filters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.DeviceInfo;
import com.ifeng.hippo.contances.TaskSource;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.core.IFilter;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.proxy.Proxy;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.mongo.MongoCli;
import com.ifeng.redis.RedisClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;

import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_ANDROID_PREFIX;
import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_IOS_PREFIX;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ApiFilter implements IFilter {
    private RedisClient redisClient = RedisFactory.newInstance();
    private final static Logger logger = Logger.getLogger(ApiFilter.class);
    private ConcurrentHashMap<String, Integer> provincesMap;

    @Override
    public Object filter(Context context) {
        try {
            TaskFragment tf = (TaskFragment) context.getObject("taskFragment");
            if (tf.getTaskSource() == TaskSource.API) {
                Proxy proxy = (Proxy) context.getObject("proxy");
                provincesMap = (ConcurrentHashMap<String, Integer>) context.getObject("provincesMap");
                if (proxy == null) {
                    return null;
                }
                String data = "";
                int i = 2;
                while (i-- > 0 && ("".equals(data) || "{}".equals(data) || data.length() < 200)) {
                    data = getAPIResult(tf, proxy);
                }

                if (!"".equals(data) && !"{}".equals(data) && data.length() > 200) {
                    tf.setData(URLEncoder.encode(data, "utf-8"));
                    logger.info("task id: " + tf.getTaskId() + " api request success:" + data);
                } else {
                    logger.info("task id: " + tf.getTaskId() + " api request success:null");
                    return null;
                }
            }
        } catch (Exception er) {
            logger.error(er);
        }
        return true;
    }

    private String getAPIResult(TaskFragment tf, Proxy proxy) {
        try {
            String url = tf.getApi();
            int rand = (int) (Math.random() * 10);
            String province = "广州";
            if (proxy.getAddr() != null)
                province = proxy.getAddr().split("省")[0].replace("市", "");
            int provinceId = provincesMap.get(province);

            String key = "";
            String idfa = "", imei = "", brand = "", model = "", value = "";
            if (tf.getDeviceInfo() == DeviceInfo.ANDROID
                    || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand >= 4)) {
                key = String.format(DEVICE_ID_ANDROID_PREFIX, provinceId);
                value = redisClient.lpopString(key);
                value = value.replace("\"", "");
                String[] vs = value.split(",");
                imei = vs[0];
                brand = vs[1];
                model = vs[2];
                url = String.format(url, "android", "", imei.replace("\"", ""), brand, model, proxy.getProxyRealIp());
            } else if (tf.getDeviceInfo() == DeviceInfo.IPHONE
                    || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand < 4)) {
                key = String.format(DEVICE_ID_IOS_PREFIX, provinceId);
                value = redisClient.lpopString(key);
                value = value.replace("\"", "");
                String[] vs = value.split(",");
                idfa = vs[0];
                brand = vs[1];
                model = vs[2];
                url = String.format(url, "ios", idfa.replace("\"", ""), "", brand, model, proxy.getProxyRealIp());
            }

            redisClient.rpushString(key, value.replace("\"", ""));
            url += "&rnd=" + (int) (1000000 * Math.random());

            logger.info("request api url, taskid:" + tf.getTaskId() + " url:" + url);

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
                if (tf.getTaskType() == TaskType.CLICK) {
                    Object hrefURLObj = jsonObject.get("hrefURL");
                    if (hrefURLObj == null) {
                        logger.info("api resource error, no hrefURL found, taskId:" + tf.getTaskId() + " url:" + url);
                        return "";
                    } else {
                        Object obj = jsonObject.get("clkURL");
                        if (obj == null || obj instanceof String) {
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.add(obj);
                            jsonArray.add(tf.getDetectionCode());
                            jsonObject.put("clkURL", jsonArray);
                        } else if (obj instanceof JSONArray) {
                            ((JSONArray) obj).add(tf.getDetectionCode());
                            jsonObject.put("clkURL", obj);
                        }
                    }
                } else if (tf.getTaskType() == TaskType.EV) {
                    Object obj = jsonObject.get("impURL");
                    if (obj == null) {
                        logger.info("api resource error, no impUrl found, taskId:" + tf.getTaskId() + " url:" + url);
                        return "";
                    } else {
                        if (obj instanceof String) {
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.add(obj);
                            jsonArray.add(tf.getDetectionCode());
                            jsonObject.put("impURL", jsonArray);
                        } else if (obj instanceof JSONArray) {
                            ((JSONArray) obj).add(tf.getDetectionCode());
                            jsonObject.put("impURL", obj);
                        }
                    }
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
                return "";
            }
            return jsonObject.toString();
        } catch (Exception ex) {

        }
        return "";
    }
}