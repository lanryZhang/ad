package com.ifeng.hippo;

import com.ifeng.configurable.ComponentConfiguration;
import com.ifeng.configurable.Context;
import com.ifeng.core.ApplicationContext;
import com.ifeng.core.ApplicationContextLoader;
import com.ifeng.core.clean.ShutdownManager;
import com.ifeng.core.distribute.HttpServer;
import com.ifeng.core.distribute.handlers.http.HttpRequestHandler;
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.master.Master;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.worker.Worker;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.redis.RedisClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static com.ifeng.hippo.contances.RedisPrefix.*;

/**
 * Created by zhanglr on 2017/5/24.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        /** args 两个字段，1、启动选项，server或者client； 2、项目路径*/
        if (args.length < 1) {
            System.err.println("args can not be null.");
            System.exit(0);
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownManager());

        String pName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = pName.split("@")[0];
        String path = args[1];

        System.getProperties().setProperty("protostuff.runtime.always_use_sun_reflection_factory", "true");
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        Map<String, Context> map = componentConfiguration.load(path + "/conf/server.properties");

        /** 初始化mongo数据库 */
        MongoFactory.initAllInstance(path);
        /** 初始化redies */
        RedisFactory.initAllInstance(path);

        if (args[0].equals("server")) {
            /** 启动server端 */
            Context context = map.get("server1");
            context.putAll(map.get("zookeeper"));
            context.put("pid", pid);

            Master master = new Master();
            /** 配置Master */
            master.config(context);
            ShutdownManager.regist(master);
            master.start();

            ApplicationContextLoader loader = new ApplicationContextLoader();
            ApplicationContext applicationContext = loader.load(path + "/lib/hippo.jar");
            HttpServer server = new HttpServer(new HttpRequestHandler());
            map.get("httpserver").put("handlerMapper", applicationContext.getMapper());
            server.config(map.get("httpserver"));
            server.start();
        } else if (args[0].equals("client")) {
            /** 启动client端 */
            Context context = map.get("worker1");
            context.putAll(map.get("zookeeper"));
            context.putAll(map.get("phantomjs"));
            context.put("pid", pid);

            Worker worker = new Worker();
            ShutdownManager.regist(worker);
            /** 配置Worker */
            worker.config(context);
            worker.start();
        } else {
            System.err.println("there are no command " + args[0] + " can be exec.");
            System.exit(0);
        }
    }
}