package com.ifeng.hippo.redis;

import com.ifeng.configurable.ComponentConfiguration;
import com.ifeng.redis.RedisClient;
import com.ifeng.redis.RedisClusterClient;
import org.apache.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public class RedisFactory {
    private static ThreadLocal<RedisClient> redisClientThreadLocal  = new ThreadLocal<>();
    private static final Logger logger = Logger.getLogger(RedisFactory.class);
    private static RedisClient instance;
    private static String redisPath = null;

    public static void initAllInstance(String path){
        redisPath = path;
    }

    public static RedisClient newInstance() {
        if (instance == null){
            synchronized (RedisFactory.class){
                if (instance == null){
                    try {
                        instance = new RedisClient("redis_1",redisPath);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }
        return instance;
    }

    public static RedisClient newInstance(String name) {
        try {
            if (null == redisClientThreadLocal.get()) {
                synchronized(RedisFactory.class) {
                    if (null == redisClientThreadLocal.get()) {
                        RedisClient client = new RedisClient(name,redisPath);
                        redisClientThreadLocal.set(client);
                    }
                }
            }
            return redisClientThreadLocal.get();
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    public static RedisClient newInstance(String name, boolean sharded) {
        try {
            if (null == redisClientThreadLocal.get()) {
                synchronized(RedisFactory.class) {
                    if (null == redisClientThreadLocal.get()) {
                        RedisClient client = new RedisClient(name, sharded,redisPath);
                        redisClientThreadLocal.set(client);
                    }
                }
            }
            return redisClientThreadLocal.get();
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }
}
