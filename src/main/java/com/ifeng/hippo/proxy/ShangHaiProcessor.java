/*
* AbsShangHaiProcessor.java 
* Created on  202017/9/2 13:43 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.hippo.utils.HttpUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public  class ShangHaiProcessor {

    public final static ConcurrentHashMap<String,Long> PROXYID_MAP = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String,Long> CONNECT_FIRST_PROXYID_MAP = new ConcurrentHashMap<>();
    public static void updateIp(String key) throws Exception {
        HttpUtils.httpGet("http://42.62.66.16:40000/exportud.ashx?action=UPDATE&PID="+key);
    }

    public static String findIp(String key) throws Exception {
        HttpResult result = HttpUtils.httpGet("http://42.62.66.16:40000/exportud.ashx?action=FIND&PID="+key);
        if (result != null && result.getBody()!=null) {
            String res = result.getBody().toString();
            JSONObject obj = JSON.parseObject(res);
            int level = obj.getInteger("ProxyLevel");
            if (level == 10){
                return obj.getString("ProxyIP")+"#"+ obj.getString("ProxyPort");
            }else{
            }
        }

        return null;
    }
}
