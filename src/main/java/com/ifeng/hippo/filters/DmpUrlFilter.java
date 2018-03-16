/*
* DmpUrlFilter.java 
* Created on  202018/1/10 19:37 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.filters;

import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.DeviceInfo;
import com.ifeng.hippo.contances.TaskSource;
import com.ifeng.hippo.core.IFilter;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.proxy.Proxy;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.utils.MD5Util;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.security.Credential;

import java.security.MessageDigest;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_ANDROID_PREFIX;
import static com.ifeng.hippo.contances.RedisPrefix.DEVICE_ID_IOS_PREFIX;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class DmpUrlFilter  implements IFilter {
    private RedisClient redisClient = RedisFactory.newInstance();
    private final static Logger logger = Logger.getLogger(DmpUrlFilter.class);
    private ConcurrentHashMap<String,Integer> provincesMap;
    @Override
    public Object filter(Context context) {
        try {
            TaskFragment tf = (TaskFragment) context.getObject("taskFragment");
            if (tf.getTaskSource() == TaskSource.DMP) {
                Proxy proxy = (Proxy) context.getObject("proxy");
                provincesMap = (ConcurrentHashMap<String, Integer>) context.getObject("provincesMap");
                initTaskFragmentUrl(tf,proxy);
            }
        }catch (Exception er){
            logger.error(er);
        }
        return true;
    }

    private void initTaskFragmentUrl(TaskFragment tf,Proxy proxy){
        try {
            int rand = (int) (Math.random() * 10);
            String province = "广州";
            if (proxy.getAddr() != null)
                province = proxy.getAddr().split("省")[0].replace("市", "");
            int provinceId = provincesMap.get(province);

            String key = "";
            String value = "",idfa = "",imei = "",dev = "ios";
            if (tf.getDeviceInfo() == DeviceInfo.ANDROID
                    || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand >= 4)) {
                key = String.format(DEVICE_ID_ANDROID_PREFIX, provinceId);
                value = redisClient.lpopString(key);
                String[] vs = value.split(",");
                imei = vs[0];
                imei = imei.replace("\"", "");
                dev = "android";
            } else if (tf.getDeviceInfo() == DeviceInfo.IPHONE
                    || (tf.getDeviceInfo() == DeviceInfo.DEFAULT && rand < 4)) {
                key = String.format(DEVICE_ID_IOS_PREFIX, provinceId);
                value = redisClient.lpopString(key);
                String[] vs = value.split(",");
                idfa = vs[0];
                idfa = idfa.replace("\"", "");
            }

            redisClient.rpushString(key, value.replace("\"", ""));
            String uuid = UUID.randomUUID().toString();
            fixedTaskUrl(tf,imei,idfa,proxy.getProxyRealIp(), dev,uuid);
        }catch (Exception er){
            er.printStackTrace();
            logger.error(er);
        }
    }

    private void fixedTaskUrl(TaskFragment tf,String imei,String idfa,String ip,String dev,String uuid){
        try {
            if ("ios".equals(dev)) {
                tf.setUrl(tf.getUrl().replace("__IDFA__", idfa.toLowerCase()));
                tf.setUrl(tf.getUrl().replace("__IFENGIDFA__", idfa.toLowerCase()));
            } else {
                tf.setUrl(tf.getUrl().replace("__IMEI__", MD5Util.encryption32(imei).toLowerCase()));
                tf.setUrl(tf.getUrl().replace("__IFENGIMEI__", MD5Util.encryption32(imei).toLowerCase()));
            }

            tf.setUrl(tf.getUrl().replace("__IP__", ip));
            tf.setUrl(tf.getUrl().replace("__IFENGIP__", ip));

            tf.setUrl(tf.getUrl().replaceAll("__UUID__", uuid));

            logger.info("get request dmp url:" + tf.getUrl());

            if (null != tf.getSubFragments() && tf.getSubFragments().size() > 0) {
                for (TaskFragment sub : tf.getSubFragments()) {
                    fixedTaskUrl(sub, imei, idfa, ip, dev,uuid);
                }
            }
        }catch (Exception er) {
            logger.error(er);
        }
    }

}
