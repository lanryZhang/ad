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
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.hippo.utils.HttpUtils;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;

/**
 * data format:{proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.realIp}#{proxy.expire}#timstamp
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class XunProxyProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(XunProxyProcessor.class);

    public XunProxyProcessor() {
        super();
    }

    @Override
    public void doProcess(Object result) {
        if (result == null)
            return;
        try {
            String res = result.toString();
            JSONObject obj = JSON.parseObject(res);
            JSONObject resultObj = (JSONObject) obj.get("RESULT");
            if (resultObj != null) {
                try {
                    String proxyIp = resultObj.getString("wanIp");
                    int port = Integer.valueOf(resultObj.getString("proxyport"));
                    String hostIp = proxyIp;
                    String vpsHost = "";
                    String addr = "上海市";
                    String netName = "cnc";
//                        int expire = b.getInteger("expired_in");

                    StringBuilder sb = new StringBuilder(proxyIp).append("#").append(port).append("#").append("http");
                    sb.append("#").append(netName).append("#").append(vpsHost).append("#").append(name).append("#").append(addr).append("##");

                    sb.append("#").append(hostIp);
                    sb.append("#").append(35 * 1000);
                    sb.append("#").append(System.currentTimeMillis());
                    String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
                    String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);

//                    pushRedis(clickKey, evKey, sb.toString(), addr);

                } catch (Exception er) {
                    logger.error(er);
                }
            }
        } catch (Exception er) {
            logger.error(er);
        }
    }
}
