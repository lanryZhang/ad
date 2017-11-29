/*
* AbsProxyPersistence.java 
* Created on  202017/11/4 19:11 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
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
    protected RedisClient redisClient = RedisFactory.newInstance();
    protected List<Integer> provinceIds = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(AbsProxyPersistence.class);
    protected static ConcurrentHashMap<String,Integer> provincesMap = new ConcurrentHashMap<>();

    protected void pushRedis(String clickKey,String evKey,String value){

        try {
            if ("ev".equals(usedFor.toLowerCase())) {
                redisClient.lpushString(evKey, value);
            }else if ("click".equals(usedFor.toLowerCase())) {
                redisClient.lpushString(clickKey, value);
            }else{
                redisClient.lpushString(evKey, value);
                redisClient.lpushString(clickKey, value);
            }
            logger.info("get proxy: " + value);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected void pushRedis(String clickKey,String evKey,String value,String addr){
        try {
            int provinceId = -1;
            int cityId = -1;
            if (addr != null && !addr.equals("")){
                String[] str = addr.split("省");
                addr = str[0].replace("市","");
                provinceId = provincesMap.get(addr);
                if (str.length > 1){
                    String city = str[1];
                    if (provincesMap.contains(city))
                        cityId = provincesMap.get(city);
                }
            }

            if ("ev".equals(usedFor.toLowerCase())) {
                if (provinceIds.contains(provinceId)){
                    redisClient.lpushString(evKey+"_"+provinceId, value);
                }else if (provinceIds.contains(cityId)){
                    redisClient.lpushString(evKey + "_" + cityId, value);
                }else{
                    redisClient.lpushString(evKey, value);
                }
            }else if ("click".equals(usedFor.toLowerCase())) {
                if (provinceIds.contains(provinceId)) {
                    redisClient.lpushString(clickKey + "_" + provinceId, value);
                }else if (provinceIds.contains(cityId)){
                    redisClient.lpushString(clickKey + "_" + cityId, value);
                }else{
                    redisClient.lpushString(clickKey , value);
                }
            }else{
                if (provinceIds.contains(provinceId)) {
                    redisClient.lpushString(evKey + "_" + provinceId, value);
                    redisClient.lpushString(clickKey + "_" + provinceId, value);
                }else if (provinceIds.contains(cityId)){
                    redisClient.lpushString(evKey + "_" + cityId, value);
                    redisClient.lpushString(clickKey + "_" + cityId, value);
                }else{
                    redisClient.lpushString(evKey , value);
                    redisClient.lpushString(clickKey, value);
                }
            }
            logger.info("get proxy: " + value);
        } catch (Exception e) {
            logger.error(e);
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
