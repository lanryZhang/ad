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
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.hippo.utils.HttpUtils;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class SunProxyProcessor extends AbsHttpProxyExecutor{
    private static final Logger logger = Logger.getLogger(SunProxyProcessor.class);
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
                        String proxyIp = b.getString("ip");
                        int port = b.getInteger("port");
                        String hostIp = b.getString("ip");
                        String vpsHost =b.getString("ip");
                        String addr =  b.getString("city");
                        String netName = "cnc";
                        long expire = DateUtil.parse(b.getString("expire_time")).getTime() - System.currentTimeMillis();
                        if (expire > 120 * 1000){
                            expire = 120 * 1000;
                        }

                        StringBuilder sb = new StringBuilder(proxyIp).append("#").append(port).append("#").append("http");
                        sb.append("#").append(netName).append("##").append(name).append("#").append(addr).append("##");

                        sb.append("#").append(hostIp);
                        sb.append("#").append(expire);
                        sb.append("#").append(System.currentTimeMillis());
                        String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
                        String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);

                        pushRedis(clickKey,evKey,sb.toString(),addr);
                    }catch (Exception er){
                        logger.error(er);
                        er.printStackTrace();
                    }
                }
            }
        }catch (Exception er){
            logger.error(er);
        }
    }

    @Override
    public boolean getSpecialControl() {
        int now = Integer.valueOf(DateUtil.format(new Date(), "HHmm"));
        if (now >= 0 && now <=900) {
            return false;
        }
        return true;
    }
}
