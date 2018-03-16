package com.ifeng.hippo.filters;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.*;
import com.ifeng.hippo.core.IFilter;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.LabelInfo;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.entity.UserAgentInfo;
import com.ifeng.hippo.proxy.Proxy;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.redis.RedisClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_ANDROID_PREFIX;
import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_IOS_PREFIX;

/**
 * DeviceTagFilter.java
 *
 * @author zhusy
 * @date 2018-3-2 11:39.
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 */
public class DspFilter implements IFilter {
    private RedisClient redisClient = RedisFactory.newInstance();
    private final static Logger logger = Logger.getLogger(DspFilter.class);
    private UserAgentInfo userAgent;
    private ConcurrentHashMap<String, Integer> provincesMap;
    private ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> tagAppMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> tagPcMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> tagWapMap = new ConcurrentHashMap<>();

    @Override
    public Object filter(Context context) {
        try {
            TaskFragment tf = (TaskFragment) context.getObject("taskFragment");
            if (tf.getTaskSource() == TaskSource.ACCURATEOPERATIONS) {
                Proxy proxy = (Proxy) context.getObject("proxy");
                userAgent = (UserAgentInfo) context.getObject("userAgent");
                provincesMap = (ConcurrentHashMap<String, Integer>) context.getObject("provincesMap");
                tagAppMap = (ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>>) context.getObject("tagAppMap");
                tagPcMap = (ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>>) context.getObject("tagPcMap");
                tagWapMap = (ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>>) context.getObject("tagWapMap");
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
                    logger.info("task id: " + tf.getTaskId() + " api_tag request success:" + data);
                } else {
                    logger.info("task id: " + tf.getTaskId() + " api_tag request success:null");
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

            StringBuilder interestStr = new StringBuilder();
            StringBuilder uinfoStr = new StringBuilder();
            List<Integer> defaultInterestTagList = new ArrayList<>();
            List<Integer> defaultUserInfoTagList = new ArrayList<>();

            boolean hasInterest = false;
            boolean hasUserInfo = false;

            if (tf.getInterest() != null && !tf.getInterest().isEmpty()) {
                hasInterest = true;
            }
            if (tf.getUserInfo() != null && !tf.getUserInfo().isEmpty()) {
                hasUserInfo = true;
            }

            /** 获取默认标签 */
            int random = (int) (Math.random() * 10000);
            ConcurrentHashMap<Integer, List<KeyValuePair<Integer, LabelInfo>>> map;
            switch (tf.getPlatform()) {
                case APP:
                    map = tagAppMap;
                    break;
                case PC:
                    map = tagPcMap;
                    break;
                case WAP:
                    map = tagWapMap;
                    break;
                default:
                    map = new ConcurrentHashMap<>();
                    break;
            }
            for (Map.Entry<Integer, List<KeyValuePair<Integer, LabelInfo>>> entry : map.entrySet()) {
                for (KeyValuePair<Integer, LabelInfo> keyValuePair : entry.getValue()) {
                    if (random <= keyValuePair.getK()) {
                        if ("s".equals(keyValuePair.getV().getType())) {
                            defaultUserInfoTagList.add(keyValuePair.getV().getId());
                        } else if ("i".equals(keyValuePair.getV().getType())) {
                            defaultInterestTagList.add(keyValuePair.getV().getId());
                        }
                        break;
                    }
                }
            }

            int rand = (int) (Math.random() * 10);
            String province = "广州";
            if (proxy.getAddr() != null) {
                province = proxy.getAddr().split("省")[0].replace("市", "");
            }

            int provinceId = provincesMap.get(province);

            String idfa = "", os = "", imei = "", brand = "", model = "", value = "", tagListStr = "", ua = "", uid = "", dm = "";

            /** 根据任务平台分别执行App：取设备ID，PC、Wap：取ua、uid、referer */
            if (tf.getPlatform() == Platform.APP) {
                String key = String.format(RedisPrefix.TAGS_DEVICE_LIST, tf.getTaskId());
                if (tf.getDeviceInfo() == DeviceInfo.ANDROID
                        || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand >= 4)) {
                    os = "android";
                    key = key + "_" + os + "_" + provinceId;
                    value = redisClient.lpopString(key);
                    if (value == null || "".equals(value)) {
                        key = String.format(DEVICE_ID_ANDROID_PREFIX, provinceId);
                        value = redisClient.lpopString(key);
                    }
                    value = value.replace("\"", "");
                    String[] vs = value.split(",");
                    imei = vs[0];
                    brand = vs[1];
                    model = vs[2];
                    if (vs.length > 3) {
                        tagListStr = vs[3];
                    }
                } else if (tf.getDeviceInfo() == DeviceInfo.IPHONE
                        || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand < 4)) {
                    os = "ios";
                    key = key + "_" + os + "_" + provinceId;
                    value = redisClient.lpopString(key);
                    if (value == null || "".equals(value)) {
                        key = String.format(DEVICE_ID_IOS_PREFIX, provinceId);
                        value = redisClient.lpopString(key);
                    }
                    value = value.replace("\"", "");
                    String[] vs = value.split(",");
                    idfa = vs[0];
                    brand = vs[1];
                    model = vs[2];
                    if (vs.length > 3) {
                        tagListStr = vs[3];
                    }
                }
                redisClient.rpushString(key, value.replace("\"", ""));
            } else {
                String key = RedisPrefix.PC_TAGS_PREFIX;
                if (tf.getPlatform() == Platform.WAP) {
                    key = RedisPrefix.WAP_TAGS_PREFIX;
                }
                value = redisClient.lpopString(key);
                if (value != null && !"".equals(value)) {
                    value = value.replace("\"", "");
                    tagListStr = value;
                    redisClient.rpushString(key, value);
                }
                ua = userAgent.getUserAgent();
                uid = getUid();
                dm = tf.getReferer();
                dm = (dm != null && !"".equals(dm)) ? dm.replace("http://", "").replace("https://", "") : "";
                dm = dm.indexOf("/") > 0 ? dm.substring(0, dm.indexOf("/")) : dm;
            }

            /** 处理从Redis中取出的标签信息 */
            if (!"".equals(tagListStr)) {
                String[] tagArray = tagListStr.split(" ");
                List<Integer> interestTagList = new ArrayList<>();
                List<Integer> userInfoagList = new ArrayList<>();
                for (int i = 0; i < tagArray.length; i++) {
                    int tagValue = Integer.valueOf(tagArray[i]);
                    if (tagValue < 600) {
                        userInfoagList.add(tagValue);
                    } else {
                        interestTagList.add(tagValue);
                    }
                }
                if (hasInterest) {
                    /** 取交集，若交集为空，取任务标签列表 */
                    interestTagList.retainAll(tf.getInterest());
                    if (interestTagList.size() == 0) {
                        interestTagList = tf.getInterest();
                    }
                }
                if (hasUserInfo) {
                    /** 取交集，若交集为空，取任务标签列表 */
                    userInfoagList.retainAll(tf.getUserInfo());
                    if (userInfoagList.size() == 0) {
                        userInfoagList = tf.getUserInfo();
                    }
                }
                defaultInterestTagList = interestTagList;
                defaultUserInfoTagList = userInfoagList;
            } else if (hasInterest) {
                /** 取交集，若交集为空，取任务标签列表 */
                defaultInterestTagList.retainAll(tf.getInterest());
                if (defaultInterestTagList.size() == 0) {
                    defaultInterestTagList = tf.getInterest();
                }
            } else if (hasUserInfo) {
                /** 取交集，若交集为空，取任务标签列表 */
                defaultUserInfoTagList.retainAll(tf.getUserInfo());
                if (defaultUserInfoTagList.size() == 0) {
                    defaultUserInfoTagList = tf.getUserInfo();
                }
            }
            interestStr.append(StringUtils.join(defaultInterestTagList, ","));
            uinfoStr.append(StringUtils.join(defaultUserInfoTagList, ","));

            try {
                if (tf.getPlatform() == Platform.APP) {
                    url = String.format(url, os, idfa.replace("\"", ""), imei.replace("\"", ""), brand, model, proxy.getProxyRealIp(), interestStr.toString(), uinfoStr.toString());
                } else {
                    url = String.format(url, os, proxy.getProxyRealIp(), uid, URLEncoder.encode(ua, "UTF-8"), dm, interestStr.toString(), uinfoStr.toString());
                }
            } catch (Exception e) {
                logger.error("request api_tag url format error, taskId:" + tf.getTaskId() + " platform: " + tf.getPlatform().name() + " url:" + url);
            }

            logger.info("request api_tag url, taskId:" + tf.getTaskId() + " url:" + url);
            //这里发送get请求
            HttpGet request = new HttpGet(url);
            // 获取当前客户端对象
            RequestConfig config = RequestConfig.custom().setConnectTimeout(2 * 1000).setSocketTimeout(2000)
//                    .setProxy(new HttpHost(proxy.getProxyIp(), proxy.getPort()))
                    .build();
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            request.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Accept-Language", "zh-cn,zh;q=0.5");

            HttpResponse response = httpClient.execute(request);
            response.getEntity().getContent();
            String res = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject;

            try {
                /* 把json字符串转换成json对象**/
                jsonObject = JSON.parseObject(res);
                if(tf.getTaskType() == TaskType.CLICK) {
                    Object hrefURLObj = jsonObject.get("hrefURL");
                    if (hrefURLObj == null) {
                        logger.info("dmp resource error, no hrefURL found, taskId:" + tf.getTaskId() + " url:" + url);
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
                } else if(tf.getTaskType() == TaskType.EV){
                    Object obj = jsonObject.get("impURL");
                    if (obj == null) {
                        logger.info("dsp resource error, no impUrl found, taskId:"+tf.getTaskId()+" url:"+url);
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
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
        return "";
    }

    private String getUid() {
        long date = System.currentTimeMillis();
        String fn = Long.toString(Math.round(((int) (Math.random() * 2147483648L))), 36);
        long sn = Math.round(Math.random() * 10000);
        String uid = date + "_" + fn + sn;
        return uid;
    }
}
