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
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.redis.RedisClient;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

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
public class UnideskNewController implements MessageProcessor {
    private Logger logger = Logger.getLogger(UnideskNewController.class);
    protected static ConcurrentHashMap<String, Integer> provincesMap = new ConcurrentHashMap<>();
    private MongoCli mongoClient = MongoFactory.createMongoClient();
    private RedisClient redisClient = RedisFactory.newInstance();

    public UnideskNewController(){
        initProvinceMap();
    }
    @RequestMapping(value = "get_mate1",method= {RequestMethod.GET, RequestMethod.POST })
    @Override
    public Object process(Context context) {

        String iisId = context.getString("iisid");
        String bid = context.getString("bid");
        String type = context.getString("type");
        String ip = context.getString("client_ip");
        String callBack = context.getString("callback");
        String addr = context.getString("addr","广东省");

        FullHttpRequest request = (FullHttpRequest) context.getObject("request");

        String ua = request.headers().get("User-Agent");

        String os = "android";
        if (null != ua && ua.toLowerCase().contains("iphone")){
            os = "ios";
        }
        String res = "{}";
        try{
            res = getApiResult(iisId,bid,os,ip,addr);
        }catch (Exception er){
            er.printStackTrace();
        }
        return callBack+"("+res +")";
    }

    private void initProvinceMap(){
        if (provincesMap == null || provincesMap.size() == 0) {
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
            } catch (Exception er) {

            } finally {

            }
        }
    }

    private String getApiResult(String iisId,String bid,String os,String ip,String addr) throws Exception{
        logger.info("get regist getApiResult ! addr="+addr);
        String province = "广州";
        if (addr != null)
            province = addr.split("省")[0].replace("市","");
        int provinceId = provincesMap.get(province);

        String key = "";
        String idfa = "",imei = "",brand = "",model = "",value = "";
        if (os.equals("android")){
            key = String.format(DEVICE_ID_ANDROID_PREFIX,provinceId);
            value = redisClient.lpopString(key);
            value = value.replace("\"","");
            String[] vs = value.split(",");
            imei = vs[0];
            brand = vs[1];
            model = vs[2];
        } else {
            key = String.format(DEVICE_ID_IOS_PREFIX,provinceId);
            value = redisClient.lpopString(key);
            value = value.replace("\"","");
            String[] vs = value.split(",");
            idfa = vs[0];
            brand = vs[1];
            model = vs[2];
        }
        logger.info("get regist idfa !"+idfa+",imei="+imei+" brand="+brand+" model="+model);
        redisClient.rpushString(key,value.replace("\"",""));

        String url = String.format("http://iis1.deliver.ifeng.com/hz?iisid=%s&bid=%s&os=%s&network=wifi&mac=&android_id=&idfa=%s&imei=%s&brand=%s&model=%s&device=&screen_density=489&carries=1&screen_width=1920&screen_height=1080&ip=%s",
                iisId,bid,os,idfa,imei,brand,model,ip);

        logger.info("get regist request :"+url);

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
    }
}
