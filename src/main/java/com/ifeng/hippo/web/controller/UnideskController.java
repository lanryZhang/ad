/*
* UnideskController.java 
* Created on  202018/1/2 11:04 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.configurable.Context;
import com.ifeng.core.MessageProcessor;
import com.ifeng.core.distribute.annotions.RequestMapping;
import com.ifeng.core.distribute.annotions.RequestMethod;
import com.ifeng.core.distribute.annotions.WebController;
import com.ifeng.hippo.contances.DeviceInfo;
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.entity.ResourceBundle;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.mongo.query.OrderByDirection;
import com.ifeng.mongo.query.WhereType;
import com.ifeng.redis.RedisClient;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_ANDROID_PREFIX;
import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_IOS_PREFIX;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
@WebController
public class UnideskController implements MessageProcessor {
    private Logger logger = Logger.getLogger(UnideskController.class);
    //    protected static ConcurrentHashMap<String, Integer> provincesMap = new ConcurrentHashMap<>();
    private MongoCli mongoClient = MongoFactory.createMongoClient();
//    private RedisClient redisClient = RedisFactory.newInstance();

    @RequestMapping(value = "get_mate", method = {RequestMethod.GET, RequestMethod.POST})
    @Override
    public Object process(Context context) {
        String iisId = context.getString("iisid");
        String bid = context.getString("bid");
        String orderid = context.getString("orderid");
        String resourceid = context.getString("resourceid");
        String type = context.getString("type");
        String ip = context.getString("client_ip");
        String callBack = context.getString("callback");

        FullHttpRequest request = (FullHttpRequest) context.getObject("request");

        String ua = request.headers().get("User-Agent");

        String os = "android";
        if (null != ua && ua.toLowerCase().contains("iphone")) {
            os = "ios";
        } else if (null != ua && ua.toLowerCase().contains("windows")) {
            os = "windows";
        }
        String res = "{}";
        try {
            if (orderid == null || "".equals(orderid)) {
                res = getApiResult(iisId, bid, os, ip);
            } else {
                res = getCpdResource(iisId, bid, os, ip, orderid);
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
        return callBack + "(" + res + ")";
    }

    private String getCpmResource(String resourceId, String bid, String os, String ip, String orderid) throws Exception {
        mongoClient.changeDb("hippo");
        mongoClient.getCollection("cpm");
        int random = (int) (Math.random() * 10000);
        MongoSelect select = new MongoSelect();
        select.where("cpmId", Integer.valueOf(resourceId));
//        select.where("rate", WhereType.LessAndEqual, random);
//        select.orderBy("rate", OrderByDirection.DESC);
        List<ResourceBundle> resourceBundles = mongoClient.selectList(select, ResourceBundle.class);
        int max = 0;
        int index = 0;
        for (int i = 0; i < resourceBundles.size(); i++) {
            int value = resourceBundles.get(i).getRate();
            if (value <= random && value > max) {
                max = resourceBundles.get((i)).getRate();
                index = i;
            }
        }
        ResourceBundle resourceBundle = resourceBundles.get(index);
//        ResourceBundle resourceBundle = mongoClient.selectOne(select, ResourceBundle.class);
        if (resourceBundle == null) {
            return "{}";
        }
        int iisId = resourceBundle.getAdId();
        String url;
        if ("windows".equals(os)) {
            url = String.format("http://iis1.deliver.ifeng.com/hz?iisid=%s&bid=%s&os=%s&orderid=%s&ip=%s&uid=&ua=&dm=&interest=&uinfo=", iisId, bid, os, orderid, ip);
        } else {
            url = String.format("http://iis1.deliver.ifeng.com/hz?iisid=%s&bid=%s&orderid=%s&os=%s&network=wifi&mac=&android_id=&idfa=&imei=&brand=&model=&device=&screen_density=489&carries=1&screen_width=1920&screen_height=1080&ip=%s&interest=&uinfo="
                    , iisId, bid, orderid, os, ip);
        }
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
            if (jsonObject == null) {
                return "{}";
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return jsonObject.toString();
    }

    private String getCpdResource(String iisId, String bid, String os, String ip, String orderid) throws IOException {
        String url;
        if ("windows".equals(os)) {
            url = String.format("http://iis1.deliver.ifeng.com/hz?iisid=%s&bid=%s&os=%s&orderid=%s&ip=%s&uid=&ua=&dm=&interest=&uinfo=", iisId, bid, os, orderid, ip);
        } else {
            url = String.format("http://iis1.deliver.ifeng.com/hz?iisid=%s&bid=%s&orderid=%s&os=%s&network=wifi&mac=&android_id=&idfa=&imei=&brand=&model=&device=&screen_density=489&carries=1&screen_width=1920&screen_height=1080&ip=%s&interest=&uinfo="
                    , iisId, bid, orderid, os, ip);
        }
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
            if (jsonObject == null) {
                return "{}";
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return jsonObject.toString();
    }


    private String getApiResult(String iisId, String bid, String os, String ip) throws Exception {
        String url = String.format("http://iis1.deliver.ifeng.com/hz?iisid=%s&bid=%s&os=%s&network=wifi&mac=&android_id=&idfa=&imei=&brand=&model=&device=&screen_density=489&carries=1&screen_width=1920&screen_height=1080&ip=%s",
                iisId, bid, os, ip);
        logger.info("get regist request :" + url);
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
            if (jsonObject == null) {
                return "{}";
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return jsonObject.toString();
    }
}
