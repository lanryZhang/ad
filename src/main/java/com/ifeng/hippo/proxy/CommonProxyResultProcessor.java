/*
* CommonProxyResultProcessor.java 
* Created on  202017/8/14 14:07 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * /**
 * {proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.realIp}#{proxy.expire}#timstamp
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class CommonProxyResultProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(CommonProxyResultProcessor.class);

    private void parseObjectToRedis(String[] ips, String evOrClick, int taskId) throws Exception {
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
                String exclusiveKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_EXCLUSIVE, RedisPrefix.CNC);
                String appointKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_APPOINT, RedisPrefix.CNC);

                String province = temp[1].split("\\s+")[0];

                switch (evOrClick){
                    case "ev":
                        pushEvRedis(evKey, sb.toString(), province);
                        break;
                    case "click":
                        pushClickRedis(clickKey, sb.toString(), province);
                        break;
                    case "exclusive_ev":
                        pushExclusiveRedis(exclusiveKey + "_" + taskId, sb.toString(), province, "ev");
                        break;
                    case "exclusive_click":
                        pushExclusiveRedis(exclusiveKey + "_" + taskId, sb.toString(), province, "click");
                        break;
                    case "appoint_ev":
                        pushAppointRedis(appointKey + "_" + taskId, sb.toString(), province, "ev");
                        break;
                    case "appoint_click":
                        pushAppointRedis(appointKey + "_" + taskId, sb.toString(), province, "click");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void doProcess(Object result) {
        if (result == null) {
            return;
        }
        try {
            String res = result.toString();
            String[] ips = res.split("\n");
            for (Map.Entry<Integer, TaskType> entry : exclusiveProxyIds.entrySet()) {
                if (entry.getValue() == TaskType.EV) {
                    for (int i = 0; i < reusetimes; i++) {
                        parseObjectToRedis(ips, "exclusive_ev", entry.getKey());
                    }
                } else {
                    parseObjectToRedis(ips, "exclusive_click", entry.getKey());
                }
            }
            for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> entry : appointProxyIds.entrySet()) {
                if(entry.getValue().getK().contains(name)) {
                    if (entry.getValue().getV() == TaskType.EV) {
                        for (int i = 0; i < reusetimes; i++) {
                            parseObjectToRedis(ips, "appoint_ev", entry.getKey());
                        }
                    } else {
                        parseObjectToRedis(ips, "appoint_click", entry.getKey());
                    }
                }
            }
            for (int i = 0; i < reusetimes; i++) {
                parseObjectToRedis(ips, "ev", 0);
            }
            parseObjectToRedis(ips, "click", 0);
        } catch (Exception er) {
            logger.error(er);
        }
    }
}
