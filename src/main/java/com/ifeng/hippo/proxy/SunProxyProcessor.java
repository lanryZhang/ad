/*
* SunProxyProcessor.java 
* Created on  202017/11/6 16:08 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.hippo.utils.HttpUtils;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class SunProxyProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(SunProxyProcessor.class);

    private void parseObjectToRedis(JSONArray arr, String evOrClick, int taskId) throws Exception {
        for (Object o : arr) {
            try {
                JSONObject b = (JSONObject) o;
                String proxyIp = b.getString("ip");
                int port = b.getInteger("port");
                String hostIp = b.getString("ip");
                String vpsHost = b.getString("ip");
                String addr = b.getString("city");
                String netName = "cnc";
                long expire = DateUtil.parse(b.getString("expire_time")).getTime() - System.currentTimeMillis();
                if (expire > 120 * 1000) {
                    expire = 120 * 1000;
                }

                StringBuilder sb = new StringBuilder(proxyIp).append("#").append(port).append("#").append("http");
                sb.append("#").append(netName).append("##").append(name).append("#").append(addr).append("##");

                sb.append("#").append(hostIp);
                sb.append("#").append(expire);
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
                er.printStackTrace();
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
            JSONArray arr = (JSONArray) obj.get("data");
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

    @Override
    public boolean getSpecialControl() {
        int now = Integer.valueOf(DateUtil.format(new Date(), "HHmm"));
        if (now >= 0 && now <= 900) {
            return false;
        }
        return true;
    }
}
