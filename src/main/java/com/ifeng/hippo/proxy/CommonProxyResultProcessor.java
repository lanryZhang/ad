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
public class CommonProxyResultProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(CommonProxyResultProcessor.class);
    @Override
    public void doProcess(Object result) {
        if (result == null)
            return;
        try {
            String res = result.toString();
            String[] ips = res.split("\n");
            if (ips != null) {
                for (String ip : ips) {
                    String[] temp = ip.split("#");
                    if (temp.length > 3) {
                        String netname = "cnc";
                        String[] ipFormat = temp[0].split(":");
                        StringBuilder sb = new StringBuilder(ipFormat[0]).append("#").append(ipFormat[1]).append("#").append("http");
                        sb.append("#").append(netname).append("##").append(name).append("#").append(temp[1].split("\\s+")[0]).append("##");

                        int expire = Integer.valueOf(temp[2]);
                        int cometime = Integer.valueOf(temp[3]);
                        if ((expire - cometime) < 5) {
                            continue;
                        }
                        sb.append("#").append(temp[0].split(":")[0]);
                        sb.append("#").append((expire - cometime) * 1000);
                        sb.append("#").append(System.currentTimeMillis());
                        String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
                        String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);

                        String province = temp[1].split("\\s+")[0];

                        pushRedis(clickKey, evKey, sb.toString(), province);
                    }
                }
            }
        }catch (Exception er){
            logger.error(er);
        }
    }
}
