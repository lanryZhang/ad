package com.ifeng.hippo.mongo;


import com.ifeng.configurable.ComponentConfiguration;
import com.ifeng.configurable.Context;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.query.Where;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MongoFactory {
	private static MongoCli instance;
	private static String path;
	private static int port;
	private static HashMap<String,Context> map;

	public static void initAllInstance(String path){
		ComponentConfiguration componentConfiguration = new ComponentConfiguration();
		map = componentConfiguration.load(path+"/conf/mongo.properties");
	}

	/**
	 * 默认数据库
	 * @return
	 */
	public static MongoCli getInstance()  {
		if (instance == null){
			synchronized (MongoFactory.class) {
				if (instance == null){
					ServerAddress addr21 = new ServerAddress("10.50.16.21", 27021);
					ServerAddress addr35 = new ServerAddress("10.50.16.35", 27021);
					ServerAddress addr36 = new ServerAddress("10.50.16.36", 27021);
					ServerAddress addr18 = new ServerAddress("10.50.16.18", 27021);
					List<ServerAddress> list = new ArrayList<ServerAddress>();
					list.add(addr18);
					list.add(addr21);
					list.add(addr35);
					list.add(addr36);
					instance = new MongoCli(list,new ArrayList<>());
				}
			}
		}
		return instance;
	}

	public static MongoCli createMongoClient(String localHost,int port){
		ServerAddress addr18 = new ServerAddress(localHost, port);
		List<ServerAddress> list = new ArrayList<>();
		list.add(addr18);
		return new MongoCli(list,new ArrayList<>());
	}

	public static MongoCli createMongoClient(){
		Context context= map.get("hippo");

		ServerAddress addr20 = new ServerAddress(context.getString("host"), context.getInt("port"));
		List<ServerAddress> list = new ArrayList<>();
		list.add(addr20);
		MongoCredential mc = MongoCredential.createScramSha1Credential(context.getString("username"),
				context.getString("database"), context.getString("pwd").toCharArray());
		return new MongoCli(list, Arrays.asList(mc));
	}

	public static MongoCli createAdMongoClient(){
		Context context= map.get("AdsFlowLog");

		ServerAddress addr20 = new ServerAddress(context.getString("host"), context.getInt("port"));
		List<ServerAddress> list = new ArrayList<>();
		list.add(addr20);
		MongoCredential mc = MongoCredential.createScramSha1Credential(context.getString("username"),
				context.getString("database"), context.getString("pwd").toCharArray());
		return new MongoCli(list, Arrays.asList(mc));
	}
}
