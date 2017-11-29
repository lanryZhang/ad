/*
* UAProcessor.java 
* Created on  202017/5/24 9:39 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.filters;

import com.alibaba.fastjson.JSON;
import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.DeviceInfo;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.Platform;
import com.ifeng.hippo.core.IFilter;
import com.ifeng.hippo.entity.PercentEntity;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.entity.UserAgentInfo;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.proxy.Proxy;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class UAFilter implements IFilter {
    private RedisClient redisClient = RedisFactory.newInstance();
    private MongoCli mongoClient = MongoFactory.createMongoClient();

    private final static Logger logger = Logger.getLogger(ProxyFilter.class);
    private static ConcurrentHashMap<String, List<UserAgentInfo>> uaAPPMap = new ConcurrentHashMap();
    private static ConcurrentHashMap<String, List<UserAgentInfo>> uaPcMap = new ConcurrentHashMap();
    private static long uaVersion = 0;
    private static ConcurrentHashSet<PercentEntity> appBrowserPercent = new ConcurrentHashSet<>();
    private static ConcurrentHashSet<PercentEntity> pcBrowserPercent = new ConcurrentHashSet<>();
    private static ConcurrentHashSet<PercentEntity> appDeviceInfoPercent = new ConcurrentHashSet<>();
    private static List<String> appleDeviceList = new ArrayList<>();
    private static List<String> androidDeviceList = new ArrayList<>();
    private String browser = "", device = "", platform = "";
    private int random;

    public synchronized void initUserAgent(TaskFragment tf) {
        ConcurrentHashMap<String, List<UserAgentInfo>> map = uaAPPMap;
        if (tf.getPlatform() == Platform.PC) {
            map = uaPcMap;
        }
        long v = 0;
        try {
            v = Long.valueOf(redisClient.getString(RedisPrefix.UA_VERSION));
        } catch (Exception er) {
            logger.error("获取ua版本失败。");
        }

        if (map.size() == 0 || uaVersion == 0 || uaVersion < v) {
            try {
                uaVersion = v;
                mongoClient.changeDb("hippo");
                mongoClient.getCollection("user_agents");

                MongoSelect select = new MongoSelect();
                select.where("platform", tf.getPlatform() == Platform.APP ? "app" : "pc");

                List<UserAgentInfo> tasks = mongoClient.selectList(select, UserAgentInfo.class);
                ConcurrentHashMap<String, List<UserAgentInfo>> finalMap = map;

                finalMap.clear();

                tasks.forEach(r -> {

                    String uaKey = "";

                    if (tf.getPlatform() == Platform.APP) {
                        uaKey = String.format(RedisPrefix.APP_UA_KEY_PREFIX, r.getPlatform(), r.getDeviceInfo(), r.getBrowser());
                    } else {
                        uaKey = String.format(RedisPrefix.PC_UA_KEY_PREFIX, r.getPlatform(), r.getBrowser());
                    }
                    List<UserAgentInfo> list = finalMap.get(uaKey);
                    if (list == null) {
                        list = new ArrayList<>();
                        finalMap.put(uaKey, list);
                    }
                    list.add(r);
                });
                uaVersion = System.currentTimeMillis();
                redisClient.set(RedisPrefix.UA_VERSION, uaVersion);
            } catch (Exception er) {
                logger.error(er);
            }
        }
    }

    private List<PercentEntity> selectBrowser(String type) {
        try {
            mongoClient.changeDb("hippo");
            mongoClient.getCollection("browser_percents");
            MongoSelect select = new MongoSelect();
            select.where("type", type);
            List<PercentEntity> list = mongoClient.selectAll(select, PercentEntity.class);
            list.forEach(r -> {
                if ("pc".equals(type)) {
                    pcBrowserPercent.add(r);
                } else {
                    appBrowserPercent.add(r);
                }
            });
            return list;
        } catch (Exception er) {
            logger.error(er);
        }
        return null;
    }

    public void initUaPercent(TaskFragment tf) {
        long v = 0;
        try {
            v = Long.valueOf(redisClient.getString(RedisPrefix.UA_VERSION));
        } catch (Exception er) {
            logger.error("获取UA分布比例版本失败。");
        }

        if (appBrowserPercent.size() == 0 || uaVersion == 0 || uaVersion < v) {
            appBrowserPercent.clear();
            selectBrowser("app");
        }

        if (pcBrowserPercent.size() == 0 || uaVersion == 0 || uaVersion < v) {
            pcBrowserPercent.clear();
            selectBrowser("pc");
        }

        if (appDeviceInfoPercent.size() == 0 || uaVersion == 0 || uaVersion < v) {
            try {
                mongoClient.changeDb("hippo");
                mongoClient.getCollection("device_percents");
                MongoSelect select = new MongoSelect();
                List<PercentEntity> list = mongoClient.selectAll(select, PercentEntity.class);

                androidDeviceList.clear();
                appleDeviceList.clear();
                appDeviceInfoPercent.clear();

                list.forEach(r -> {
                    if (r.getKey().toLowerCase().startsWith("android")) {
                        androidDeviceList.add(r.getKey());
                    } else {
                        appleDeviceList.add(r.getKey());
                    }
                    appDeviceInfoPercent.add(r);
                });
            } catch (Exception er) {
                logger.error(er);
            }
        }
    }

    @Override
    public Object filter(Context context) {
        TaskFragment tf = (TaskFragment) context.getObject("taskFragment");
        initUserAgent(tf);
        initUaPercent(tf);

        String uaKey = "";
        List<UserAgentInfo> list;
        UserAgentInfo ua = null;

        Proxy proxy = (Proxy) context.getObject("proxy");

        if (proxy == null) {
            logger.info("no proxy available");
            return null;
        }


        random = (int) (Math.random() * 1000);
        if (tf.getPlatform() == Platform.APP || tf.getPlatform() == Platform.WAP) {
            appBrowserPercent.forEach(r -> {
                if (r.getBegin() <= random && random < r.getEnd()) {
                    browser = r.getKey();
                }
            });

            if (tf.getDeviceInfo() != null && tf.getDeviceInfo() == DeviceInfo.ANDROID) {
                random = (int) (Math.random() * androidDeviceList.size());
                device = androidDeviceList.get(random);
            } else if (tf.getDeviceInfo() != null && tf.getDeviceInfo() == DeviceInfo.IPHONE) {
                random = (int) (Math.random() * appleDeviceList.size());
                device = appleDeviceList.get(random);
            } else {
                random = (int) (Math.random() * 100);
                appDeviceInfoPercent.forEach(r -> {
                    if (r.getBegin() <= random && random < r.getEnd()) {
                        device = r.getKey();
                    }
                });
            }

            platform = "app";
            uaKey = String.format(RedisPrefix.APP_UA_KEY_PREFIX, platform, device, browser);
            list = uaAPPMap.get(uaKey);

        } else {
            pcBrowserPercent.forEach(r -> {
                if (r.getBegin() <= random && random < r.getEnd()) {
                    browser = r.getKey();
                }
            });

            platform = "pc";
            uaKey = String.format(RedisPrefix.PC_UA_KEY_PREFIX, platform, browser);
            list = uaPcMap.get(uaKey);
        }

        if (list != null) {
            int index = (int) (Math.random() * list.size());
            ua = list.get(index);
        } else {
            try {
                ua = new UserAgentInfo();
                if (tf.getPlatform() == Platform.APP || tf.getPlatform() == Platform.WAP) {
                    if (tf.getDeviceInfo() != null && tf.getDeviceInfo() == DeviceInfo.ANDROID) {
                        ua.setUserAgent(redisClient.getString(RedisPrefix.ANDROID_DEFAULT_UA));
                        ua.setId(100002);
                    } else if (tf.getDeviceInfo() != null && tf.getDeviceInfo() == DeviceInfo.IPHONE) {
                        ua.setUserAgent(redisClient.getString(RedisPrefix.APPLE_DEFAULT_UA));
                        ua.setId(100003);
                    } else {
                        ua.setUserAgent(redisClient.getString(RedisPrefix.APP_DEFAULT_UA));
                        ua.setId(100000);
                    }
                } else {
                    ua.setUserAgent(redisClient.getString(RedisPrefix.PC_DEFAULT_UA));
                    ua.setId(100001);
                }
            } catch (Exception er) {
                logger.error(er);
            }
        }

        context.put("userAgent", ua);
        return true;
    }
}