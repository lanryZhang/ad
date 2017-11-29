/*
* CommonProxyResultProcessor.java 
* Created on  202017/8/14 14:07 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

/**
 * /**
 * {proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.realIp}#{proxy.expire}#timstamp
 * @author zhanglr
 * @version 1.0.1
 */
public class YiFaProxyResultProcessor extends AbsHttpProxyExecutor {
    private RedisClient redisClient = RedisFactory.newInstance();
    private static final Logger logger = Logger.getLogger(YiFaProxyResultProcessor.class);
    @Override
    public void doProcess(Object result) {
        if (result == null)
            return;
        try {
            String res = result.toString();
            String[] ips = res.split("\n");
            if (ips != null) {
                for (String ip : ips) {
                    String[] temp = ip.split(":");
                    StringBuilder sb = new StringBuilder(temp[0]).append("#").append(temp[1]).append("#").append("http");
                    sb.append("#cnc").append("##").append(name).append("####")
                    .append(temp[0]).append("#");

                    sb.append(30 * 1000);
                    sb.append("#").append(System.currentTimeMillis());
                    String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
                    String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);

//                    pushRedis(clickKey,evKey,sb.toString());
                    logger.info("get proxy: " + sb.toString());
                }
            }
        }catch (Exception er){
            logger.error(er);
        }
    }
}
