package com.ifeng.hippo.redis;

import com.ifeng.redis.RedisClient;
import com.ifeng.redis.RedisClusterClient;
import org.apache.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public class RedisFactory {
    private static ThreadLocal<RedisClusterClient> masterClusterThreadLocal  = new ThreadLocal<>();
    private static RedisClusterClient clusterClient;
    private static RedisClusterClient profileClusterClient;
    private static ThreadLocal<RedisClient> redisClientThreadLocal  = new ThreadLocal<>();
    private static final Logger logger = Logger.getLogger(RedisFactory.class);
    private static RedisClient instance;

    public static RedisClient newInstance() {
        if (instance == null){
            synchronized (RedisFactory.class){
                if (instance == null){
                    try {
                        instance = new RedisClient("redis_1");
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
                        RedisClient client = new RedisClient(name);
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
                        RedisClient client = new RedisClient(name, sharded);
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

    public static RedisClusterClient newCluster() {
        try {
            if (null == clusterClient) {
                synchronized(RedisFactory.class) {
                    if (null == clusterClient) {
                        JedisPoolConfig config = new JedisPoolConfig();
                        config.setMaxTotal(60000);
                        config.setMaxIdle(1000);
                        config.setMaxWaitMillis(3000);
                        config.setTestOnBorrow(true);
                        config.setTestOnReturn(true);
                        config.setTimeBetweenEvictionRunsMillis(3000);
                        config.setNumTestsPerEvictionRun(1000);
                        config.setMinEvictableIdleTimeMillis(3000);
                        config.setTestWhileIdle(true);

                        Set set = new HashSet();
                        set.add(new HostAndPort("10.90.13.198",8000));
                        set.add(new HostAndPort("10.90.13.199",8000));
                        set.add(new HostAndPort("10.90.13.200",8000));
                        set.add(new HostAndPort("10.90.13.198",8001));
                        set.add(new HostAndPort("10.90.13.199",8001));
                        set.add(new HostAndPort("10.90.13.200",8001));
                        clusterClient = new RedisClusterClient(new JedisCluster(set,config));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return clusterClient;
    }
}
