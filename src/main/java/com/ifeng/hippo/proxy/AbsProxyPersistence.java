/*
* AbsProxyPersistence.java 
* Created on  202017/11/4 19:11 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public abstract class AbsProxyPersistence {
    protected MongoCli mongoClient = MongoFactory.createMongoClient();

    protected String usedFor = "";
    protected int reusetimes = 3;
    protected RedisClient redisClient = RedisFactory.newInstance();
    protected List<Integer> provinceIds = new ArrayList<>();
    protected ConcurrentHashMap<Integer,TaskType> exclusiveProxyIds = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Integer,KeyValuePair<List<String>,TaskType>> appointProxyIds = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(AbsProxyPersistence.class);
    protected static ConcurrentHashMap<String,Integer> provincesMap = new ConcurrentHashMap<>();

//    protected void pushRedis(String clickKey,String evKey,String value){
//
//        try {
//            if ("ev".equals(usedFor.toLowerCase())) {
//                redisClient.lpushString(evKey, value);
//            }else if ("click".equals(usedFor.toLowerCase())) {
//                redisClient.lpushString(clickKey, value);
//            }else{
//                redisClient.lpushString(evKey, value);
//                redisClient.lpushString(clickKey, value);
//            }
//            logger.info("get proxy: " + value);
//        } catch (Exception e) {
//            logger.error(e);
//        }
//    }

    protected void pushAppointRedis(String clickKey, String value, String addr, String evOrClick) {
        try {
            if("ev".equals(evOrClick) && "click".equals(usedFor.toLowerCase())){
                return;
            } else if("click".equals(evOrClick) && "ev".equals(usedFor.toLowerCase())){
                return;
            }
            pushRedis(clickKey, value, addr);
            logger.info("get appoint proxy: " + clickKey + "####" + value);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected void pushExclusiveRedis(String clickKey, String value, String addr, String evOrClick) {
        try {
            if("ev".equals(evOrClick) && "click".equals(usedFor.toLowerCase())){
                return;
            } else if("click".equals(evOrClick) && "ev".equals(usedFor.toLowerCase())){
                return;
            }
            pushRedis(clickKey, value, addr);
            logger.info("get exclusive proxy: " + clickKey + "####" + value);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected void pushClickRedis(String clickKey,String value,String addr) {
        try {
            if ("ev".equals(usedFor.toLowerCase())) {
                return;
            }
            pushRedis(clickKey,value,addr);
            logger.info("get click proxy: " + value);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected void pushEvRedis(String evKey,String value,String addr) {
        try {
            if ("click".equals(usedFor.toLowerCase())) {
                return;
            }
            pushRedis(evKey,value,addr);
            logger.info("get ev proxy: " + value);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void pushRedis(String key,String value,String addr) throws Exception{
        int provinceId = -1,cityId = -1;
        if (addr != null && !addr.equals("")) {
            String[] str = addr.split("省");
            if (str.length < 2){
                str = addr.split("市");
            }
            addr = str[0].replace("市", "");

            provinceId = provincesMap.get(addr);
            if (str.length > 1) {
                String city = str[1].replace("市","");
                if (provincesMap.containsKey(city))
                    cityId = provincesMap.get(city);
            }
        }

        if (provinceIds.contains(provinceId)) {
            redisClient.lpushString(key + "_" + provinceId, value);
            redisClient.expireKey(key + "_" + provinceId, 24 * 60 * 60);
        } else if (provinceIds.contains(cityId)) {
            redisClient.lpushString(key + "_" + cityId, value);
            redisClient.expireKey(key + "_" + cityId, 24 * 60 * 60);
        } else {
            redisClient.lpushString(key, value);
            redisClient.expireKey(key, 24 * 60 * 60);
        }
    }
    protected void refreshProvince() throws Exception {
        mongoClient.changeDb("hippo");
        mongoClient.getCollection("province");

        provincesMap.clear();

        MongoSelect select = new MongoSelect();
        List<Province> provinces = mongoClient.selectList(select, Province.class);

        if (provinces != null){
            for (Province province : provinces) {
                provincesMap.put(province.getName().replace("省","").replace("市",""),province.getId());
            }
        }
    }
}
