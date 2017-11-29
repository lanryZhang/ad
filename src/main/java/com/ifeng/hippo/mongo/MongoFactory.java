package com.ifeng.hippo.mongo;


import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.query.Where;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MongoFactory {

	private static ThreadLocal<MongoCli> mongoThreadLocal  = new ThreadLocal<>();

	private static MongoCli instance;

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
//
	public static MongoCli createMongoClient(){
		ServerAddress addr20 = new ServerAddress("172.31.48.36", 27000);
		List<ServerAddress> list = new ArrayList<>();
		list.add(addr20);
		MongoCredential mc = MongoCredential.createScramSha1Credential("hippo", "hippo", "1qazXSW@".toCharArray());
		return new MongoCli(list, Arrays.asList(mc));
	}

	public static MongoCli createAdMongoClient(){
		ServerAddress addr20 = new ServerAddress("172.31.48.36", 27000);
		List<ServerAddress> list = new ArrayList<>();
		list.add(addr20);
		MongoCredential mc = MongoCredential.createScramSha1Credential("hippo", "AdsFlowLog", "1qazXSW@".toCharArray());
		return new MongoCli(list, Arrays.asList(mc));
	}

//	public static MongoCli createMongoClient(){
//		ServerAddress addr20 = new ServerAddress("10.50.16.20", 27017);
//		List<ServerAddress> list = new ArrayList<>();
//		list.add(addr20);
//		MongoCredential mc = MongoCredential.createScramSha1Credential("ifeng", "admin", "1qazXSW@3edc".toCharArray());
//		return new MongoCli(list, Arrays.asList(mc));
//	}
//
//	public static MongoCli createAdMongoClient(){
//		ServerAddress addr20 = new ServerAddress("10.50.16.20", 27017);
//		List<ServerAddress> list = new ArrayList<>();
//		list.add(addr20);
//		MongoCredential mc = MongoCredential.createScramSha1Credential("ifeng", "admin", "1qazXSW@3edc".toCharArray());
//		return new MongoCli(list, Arrays.asList(mc));
//	}
//	public static MongoCli createMongoClient(){
//		ServerAddress addr20 = new ServerAddress("10.50.16.20", 27017);
//		List<ServerAddress> list = new ArrayList<>();
//		list.add(addr20);
//		return new MongoCli(list, new ArrayList<>());
//	}
}
