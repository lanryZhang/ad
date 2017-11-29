/*
* CommonProxyResultProcessor.java 
* Created on  202017/8/14 14:07 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.ifeng.hippo.contances.RedisPrefix.SHANGHAI_RECONNECT_PROXY_LIST;

/**
 * data format:{proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.realIp}#{proxy.expire}#timstamp
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ShangHaiProxyReconnectProcessor extends AbsHttpProxyExecutor {
    private RedisClient redisClient = RedisFactory.newInstance();
    private static final Logger logger = Logger.getLogger(ShangHaiProxyReconnectProcessor.class);
    @Override
    public void doProcess(Object result) {

        if (result == null)
            return;
        try {
            String res = result.toString();
            JSONObject obj = JSON.parseObject(res);
            JSONArray arr = (JSONArray) obj.get("Proxies");
            if (arr != null){
                for (Object o : arr){
                    JSONObject b = (JSONObject) o;
                    Map<String,String> map = redisClient.hgetAll(SHANGHAI_RECONNECT_PROXY_LIST);
                    redisClient.del(SHANGHAI_RECONNECT_PROXY_LIST);
                    String proxyId = b.getString("ProxyIP")+":";
                    if (map.containsKey(proxyId)){
                        ShangHaiProcessor.updateIp(proxyId);
                    }
                }
            }
        }catch (Exception er){
            logger.error(er);
        }
    }
}
