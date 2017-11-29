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

import java.util.Iterator;
import java.util.Map;

import static com.ifeng.hippo.proxy.ShangHaiProcessor.PROXYID_MAP;
import static com.ifeng.hippo.proxy.ShangHaiProcessor.findIp;

/**
 * data format:{proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.expire}#timstamp
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class VPNProxyProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(VPNProxyProcessor.class);

    public VPNProxyProcessor(){
        super();
    }
    @Override
    public void doProcess(Object result) {

        if (result == null)
            return;
        try {
            String res = result.toString();
            JSONObject obj = JSON.parseObject(res);
            JSONArray arr = (JSONArray) obj.get("data");
            if (arr != null){
                for (Object o : arr){
                    try {
                        JSONObject b = (JSONObject) o;
                        String proxyIp = b.getString("proxy_out_ip");
                        int port = b.getInteger("proxy_port");
                        String hostIp = b.getString("remote_ip");
                        String vpsHost =b.getString("proxy_in_ip");
                        String status = b.getString("vpn_status");
                        String[] addrArr = b.getString("remote_addr").split("\\s+");
                        String addr = addrArr[2];
                        String netName = "cnc";
                        int expire = b.getInteger("expired_in");
                        if (!"connected".equals(status) ||
                                (35 - expire) > 3){
                            continue;
                        }
//
//                        if (addrArr[1].contains("联通")){
//                            netName = "cucc";
//                        }else if (addrArr[1].contains("移动")){
//                            netName = "cmcc";
//                        }
                        StringBuilder sb = new StringBuilder(proxyIp).append("#").append(port).append("#").append("http");
                        sb.append("#").append(netName).append("#").append(vpsHost).append("#").append(name).append("#").append(addr).append("##");

                        sb.append("#").append(hostIp);
                        sb.append("#").append(expire * 1000);
                        sb.append("#").append(System.currentTimeMillis());
                        String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
                        String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);

                        pushRedis(clickKey,evKey,sb.toString(),addr);
                    }catch (Exception er){
                        logger.error(er);
                    }
                }
            }
        }catch (Exception er){
            logger.error(er);
        }
    }
}
