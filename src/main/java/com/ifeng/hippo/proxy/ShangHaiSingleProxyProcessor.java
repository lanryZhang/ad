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
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.hippo.utils.HttpUtils;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;
import org.aspectj.weaver.ast.Call;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.ifeng.hippo.proxy.ShangHaiProcessor.PROXYID_MAP;
import static com.ifeng.hippo.proxy.ShangHaiProcessor.findIp;

/**
 * data format:{proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.realIp}#{proxy.expire}#timstamp
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ShangHaiSingleProxyProcessor extends AbsHttpProxyExecutor {
    private static final Logger logger = Logger.getLogger(ShangHaiSingleProxyProcessor.class);

    public ShangHaiSingleProxyProcessor(){
        super();
        FixedProxy fixedProxy = new FixedProxy();
        Thread t = new Thread(fixedProxy);
        t.start();

        ReconnectProxyFirst reconnectProxyFirst = new ReconnectProxyFirst();
        Thread t1 = new Thread(reconnectProxyFirst);
        t1.start();
    }
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
                    try {
                        JSONObject b = (JSONObject) o;
                        String proxyId = b.getString("ProxyInfoID");
                        long timestamp = PROXYID_MAP.containsKey(proxyId)? PROXYID_MAP.get(proxyId):0L;
                        if (System.currentTimeMillis() - timestamp >= 10*1000) {
                            ShangHaiProcessor.updateIp(proxyId);
                        }

                    }catch (Exception er){
                        logger.error(er);
                    }
                }
                Thread.currentThread().sleep(7 * 1000);
                for (Object o : arr){
                    try {
                        JSONObject b = (JSONObject) o;
                        String proxyId = b.getString("ProxyInfoID");
                        String proxyIp = ShangHaiProcessor.findIp(proxyId);

                        if (proxyIp != null) {
                            pushProxyToCache(proxyIp,proxyId);
                            PROXYID_MAP.remove(proxyId);
                        }else{
                            ShangHaiProcessor.updateIp(proxyId);
                            PROXYID_MAP.put(proxyId,System.currentTimeMillis());
                        }
                    }catch (Exception er){
                        logger.error(er);
                    }
                }
            }
        }catch (Exception er){
            logger.error(er);
        }
    }

    private void pushProxyToCache(String ip,String id) throws Exception {
        StringBuilder sb = new StringBuilder(ip).append("#").append("http");
        sb.append("#cnc").append("##").append(name).append("#other#").append(id);

        sb.append("##").append(ip.split("#")[0]).append("#").append(5 * 1000);
        sb.append("#").append(System.currentTimeMillis());
        String clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC, RedisPrefix.CNC);
        String evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC, RedisPrefix.CNC);

        pushRedis(clickKey,evKey,sb.toString());
    }
//
    class ReconnectProxyFirst implements Runnable{

        @Override
        public void run() {
            while (true){
                if (ShangHaiProcessor.PROXYID_MAP.size() != 0){
                    Iterator<Map.Entry<String,Long>> it = ShangHaiProcessor.PROXYID_MAP.entrySet().iterator();
                    while (it.hasNext()) {
                        try {
                            Map.Entry<String, Long> iti = it.next();
                            String proxyId = iti.getKey();
                            String proxyIp = findIp(proxyId);
                            long timestamp = PROXYID_MAP.containsKey(proxyId)? PROXYID_MAP.get(proxyId):0L;
                            if (proxyIp == null) {
                                if (System.currentTimeMillis() - timestamp >= 10*1000){
                                    ShangHaiProcessor.updateIp(proxyId);
                                    PROXYID_MAP.put(proxyId, System.currentTimeMillis());
                                }
                            }else {
                                it.remove();
                                pushProxyToCache(proxyIp,proxyId);
                            }
                        } catch (Exception er) {
                            er.printStackTrace();
                        }
                    }
                }else{
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    class FixedProxy implements Runnable{

        @Override
        public void run(){
            while (true){
                if (ShangHaiProcessor.PROXYID_MAP.size() != 0){
                    Iterator<Map.Entry<String,Long>> it = ShangHaiProcessor.PROXYID_MAP.entrySet().iterator();
                    while (it.hasNext()) {
                        try {
                            Map.Entry<String, Long> iti = it.next();
                            String proxyId = iti.getKey();
                            long timestamp = iti.getValue();

                            String proxyIp = ShangHaiProcessor.findIp(proxyId);

                            if (proxyIp == null ) {
                                if (System.currentTimeMillis() - timestamp >= 6*1000) {
                                    ShangHaiProcessor.updateIp(proxyId);
                                    PROXYID_MAP.put(proxyId, System.currentTimeMillis());
                                }
                            }else{
                                pushProxyToCache(proxyIp,proxyId);
                                it.remove();
                            }

                        } catch (Exception er) {
                            er.printStackTrace();
                        }
                    }
                }else{
                    try {
                        Thread.currentThread().sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
