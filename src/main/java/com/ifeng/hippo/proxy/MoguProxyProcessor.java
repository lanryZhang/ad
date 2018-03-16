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
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.utils.IpUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * data format:{proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.expire}#timstamp
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class MoguProxyProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(MoguProxyProcessor.class);

    public MoguProxyProcessor() {
        super();
    }

    private void parseObjectToRedis(JSONArray arr, String evOrClick, int taskId) throws Exception {
        for (Object o : arr) {
            try {
                JSONObject b = (JSONObject) o;
                String proxyIp = b.getString("ip");
                int port = b.getInteger("port");
                String hostIp = proxyIp;
                String vpsHost = proxyIp;
                String addr = IpUtils.getIpArea(proxyIp);
                String netName = "cnc";

                StringBuilder sb = new StringBuilder(proxyIp).append("#").append(port).append("#").append("http");
                sb.append("#").append(netName).append("#").append(vpsHost).append("#").append(name).append("#").append(addr).append("##");

                sb.append("#").append(hostIp);
                sb.append("#").append(120 * 1000);
                sb.append("#").append(System.currentTimeMillis());
                String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
                String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);
                String exclusiveKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_EXCLUSIVE, RedisPrefix.CNC);
                String appointKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_APPOINT, RedisPrefix.CNC);

                switch (evOrClick){
                    case "ev":
                        pushEvRedis(evKey, sb.toString(), addr);
                        break;
                    case "click":
                        pushClickRedis(clickKey, sb.toString(), addr);
                        break;
                    case "exclusive_ev":
                        pushExclusiveRedis(exclusiveKey + "_" + taskId, sb.toString(), addr, "ev");
                        break;
                    case "exclusive_click":
                        pushExclusiveRedis(exclusiveKey + "_" + taskId, sb.toString(), addr, "click");
                        break;
                    case "appoint_ev":
                        pushAppointRedis(appointKey + "_" + taskId, sb.toString(), addr, "ev");
                        break;
                    case "appoint_click":
                        pushAppointRedis(appointKey + "_" + taskId, sb.toString(), addr, "click");
                        break;
                    default:
                        break;
                }

            } catch (Exception er) {
                logger.error(er);
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
            JSONObject obj = JSON.parseObject(res);
            JSONArray arr = (JSONArray) obj.get("msg");
            if (arr != null) {
                for (Map.Entry<Integer, TaskType> entry : exclusiveProxyIds.entrySet()) {
                    if (entry.getValue() == TaskType.EV) {
                        for (int i = 0; i < reusetimes; i++) {
                            parseObjectToRedis(arr, "exclusive_ev", entry.getKey());
                        }
                    } else {
                        parseObjectToRedis(arr, "exclusive_click", entry.getKey());
                    }
                }
                for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> entry : appointProxyIds.entrySet()) {
                    if(entry.getValue().getK().contains(name)) {
                        if (entry.getValue().getV() == TaskType.EV) {
                            for (int i = 0; i < reusetimes; i++) {
                                parseObjectToRedis(arr, "appoint_ev", entry.getKey());
                            }
                        } else {
                            parseObjectToRedis(arr, "appoint_click", entry.getKey());
                        }
                    }
                }
                for (int i = 0; i < reusetimes; i++) {
                    parseObjectToRedis(arr, "ev", 0);
                }
                parseObjectToRedis(arr, "click", 0);
            }
        } catch (Exception er) {
            logger.error(er);
        }
    }
}