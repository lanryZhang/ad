/*
* CityProcessor.java 
* Created on  202017/5/24 9:41 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.filters;

import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.core.IFilter;
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.proxy.Proxy;
import com.ifeng.hippo.proxy.ProxyType;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.utils.HttpUtils;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ifeng.hippo.proxy.ShangHaiProcessor.CONNECT_FIRST_PROXYID_MAP;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ProxyFilter implements IFilter {
    private RedisClient redisClient = RedisFactory.newInstance();
    private final static Logger logger = Logger.getLogger(ProxyFilter.class);
    /**
     * proxy format: {proxy.hostIp}:{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxyRealIp}#{proxy.expire}#timstamp
     * @param context
     * @return
     */
    @Override
    public Object filter(Context context){
        String proxyStr = null;
        TaskFragment tf = (TaskFragment) context.getObject("taskFragment");
        String proxyIdListKey = tf.getTaskType() == TaskType.EV ? RedisPrefix.PROXY_IP_LIST_EV_IDC : RedisPrefix.PROXY_IP_LIST_IDC;
        proxyIdListKey = String.format(proxyIdListKey,context.getString("netName"));
        List<String> proxyKeyList = new ArrayList<>();
        if (tf.getProvinces() != null){
            for (Integer pId : tf.getProvinces()){
                if (pId != 0){
                    proxyKeyList.add(proxyIdListKey + "_"+pId);
                }
            }
            if (proxyKeyList.size() > 0) {
                int random = (int) (Math.random() * proxyKeyList.size());
                proxyIdListKey = proxyKeyList.get(random);
            }
        }

        String str = context.getString("proxyFilters");
        String[] proxyFilters = str.split(",");

        try {
            int repeat = 50;

            while (--repeat >= 0){
                try {
                    proxyStr = redisClient.lpopString(proxyIdListKey);
                    if (proxyStr == null || "".equals(proxyStr)) {
                        Thread.currentThread().sleep(5);
                        continue;
                    } else {
                        String[] arr = proxyStr.split("#");
                        long timestamp = Long.valueOf(arr[arr.length - 1]);
                        long expire = Long.valueOf(arr[arr.length - 2]);
                        long timeDiff = System.currentTimeMillis() - timestamp;

                        if (timeDiff > expire){
                            proxyStr = "";
                            continue;
                        }

                        boolean doFilter = false;
                        for (String p : proxyFilters){
                            if (p.equals(arr[5])){
                                doFilter = true;
                                break;
                            }
                        }

                        if (doFilter) continue;

                        if (tf.getForceWait() == 1 && expire - timeDiff > (tf.getWaitTimeout() * 1000)){
                            continue;
                        }
                        String key = String.format(RedisPrefix.PROXY_PREFIX, arr[9], tf.getTaskId(), DateUtil.today());
                        String t = redisClient.getString(key);
                        t = t == null || "".equals(t) ? "0" : t;
                        int times = Integer.valueOf(t);
                        if (tf.getIpReusedTimes() > times) {
                            try {
                                redisClient.incr(key);
                                redisClient.expireKey(key, 24 * 60 * 60);

                                Proxy proxy = new Proxy();
                                proxy.setTimestamp(timestamp);
                                if (arr.length > 1) {
                                    proxy.setProxyIp(arr[0]);
                                    proxy.setPort(Integer.valueOf(arr[1]));

                                    if (arr[2].equals("socks5")) {
                                        proxy.setProxyType(ProxyType.SOCKS5);
                                    } else {
                                        proxy.setProxyType(ProxyType.HTTP);
                                    }

                                    proxy.setNetName(arr[3]);
                                    proxy.setVpsHost(arr[4]);
                                    proxy.setPartner(arr[5]);
                                    proxy.setAddr(arr[6]);
                                    proxy.setUserName(arr[7]);
                                    proxy.setPassword(arr[8]);
                                    proxy.setProxyRealIp(arr[9]);
                                    proxy.setExpire(expire);
                                }

                                context.put("proxy", proxy);
                                context.put("proxyPoolKey",proxyIdListKey);
                            }catch (Exception er1){
                                logger.error(er1);
                                return false;
                            }
                            return true;
                        } else {
                            logger.info("proxy " + proxyStr + " was used too many times for task " + tf);
                            continue;
                        }
                    }
                }catch (Exception er){
                    logger.error(er);
                }finally {
                    if (proxyStr != null && !"".equals(proxyStr)){
                        try {
                            redisClient.rpushString(proxyIdListKey, proxyStr);
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return true;
    }
}
