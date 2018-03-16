/*
* ProxyIpReportController.java 
* Created on  202017/7/19 19:46 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.web.controller;

import com.ifeng.configurable.Context;
import com.ifeng.core.MessageProcessor;
import com.ifeng.core.distribute.annotions.RequestMapping;
import com.ifeng.core.distribute.annotions.RequestMethod;
import com.ifeng.core.distribute.annotions.WebController;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.Province;
import com.ifeng.hippo.entity.TaskInfo;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.mongo.query.Where;
import com.ifeng.mongo.query.WhereType;
import com.ifeng.redis.RedisClient;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ifeng.hippo.contances.RedisPrefix.VPS_HOST_PREFIX;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
@WebController
public class ProxyIpReportController implements MessageProcessor {
    private RedisClient redisClient = RedisFactory.newInstance();
    private Logger logger = Logger.getLogger(ProxyIpReportController.class);
    private int ipReuseMaxTimes = 7;
    private MongoCli mongoClient = MongoFactory.createMongoClient();
    private static long lastUpdateTimes = System.currentTimeMillis();
    private static List<TaskInfo> tasks;
    private List<Integer> provinceIds = new ArrayList<>();
    protected ConcurrentHashMap<Integer,TaskType> exclusiveProxyIds = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Integer,KeyValuePair<List<String>,TaskType>> appointProxyIds = new ConcurrentHashMap<>();
    protected static ConcurrentHashMap<String,Integer> provincesMap = new ConcurrentHashMap<>();

    /**
     * receive param format: {proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxyRealIp}#{proxy.expire}#timstamp
     * @param context
     * @return
     */
    @RequestMapping(value = "report",method= {RequestMethod.GET, RequestMethod.POST })
    @Override
    public Object process(Context context) {
        try{
//            logger.debug("get http request. param: name="+context.getString("name"));
            if (tasks == null || (System.currentTimeMillis() - lastUpdateTimes) > 5 * 60 * 1000){
                synchronized (ProxyIpReportController.class){
                    if (tasks == null || (System.currentTimeMillis() - lastUpdateTimes) > 5 * 60 * 1000){
                        mongoClient.changeDb("hippo");
                        mongoClient.getCollection("tasks");
                        MongoSelect select = new MongoSelect();
                        Date date = DateUtil.parse(DateUtil.today());

                        select.where("beginDate", WhereType.LessAndEqual, date);
                        select.where("endDate", WhereType.GreaterAndEqual, date);
                        select.where("status",0);

                        tasks = mongoClient.selectList(select, TaskInfo.class);
                        //Collections.sort(tasks, (o1, o2) -> o1.getIpReusedTimes() > o2.getIpReusedTimes() ? -1 : 1);

                        mongoClient.getCollection("province");

                        select = new MongoSelect();
                        List<Province> provinces = mongoClient.selectList(select, Province.class);

                        provincesMap.clear();

                        if (provinces != null){
                            for (Province province : provinces) {
                                provincesMap.put(province.getName().replace("省","").replace("市",""),province.getId());
                            }
                        }
                        provinceIds.clear();
                        appointProxyIds.clear();
                        exclusiveProxyIds.clear();

                        for (TaskInfo task : tasks){
                            if (task.getFiltration() != null && task.getFiltration().contains(DateUtil.today())) {
                                if (task.getProvinces() != null && task.getFiltration() != null &&
                                        task.getFiltration().contains(DateUtil.today())) {
                                    provinceIds.addAll(task.getProvinces());
                                }
                                if (task.getAppointProxyName() != null && task.getAppointProxyName().size() > 0) {
                                    KeyValuePair<List<String>, TaskType> keyValuePair = new KeyValuePair<>();
                                    keyValuePair.setK(task.getAppointProxyName());
                                    keyValuePair.setV(task.getTaskType());
                                    appointProxyIds.put(task.getTaskId(), keyValuePair);
                                } else if (task.getExclusiveProxy() == 1) {
                                    exclusiveProxyIds.put(task.getTaskId(), task.getTaskType());
                                }
                            }
                        }
                        lastUpdateTimes = System.currentTimeMillis();
                    }
                }
            }

            if (tasks == null || tasks.size() == 0){
                return true;
            }

            String param = context.getString("ip");
            logger.info("get proxy from:  "+param);
            if (param == null ){
                return false;
            }
            String[] ps = param.split("#");
            String ip = ps[0];
            String port = ps[1];
            String v = "";
            if (ps.length > 8) {
                v = param.substring(0, param.lastIndexOf("#")) + "##" + ip + "#" + ps[ps.length - 1] + "#" + System.currentTimeMillis();
            }else{
                v = param + "##" + ip + "#60000#" + System.currentTimeMillis();
            }
            String netName = ps[3];
            String agentName = ps[5];
            String clickKey = "";
            String evKey = "";
            String exclusiveKey = "";
            String appointKey = "";
            if (netName.toUpperCase().equals("CUCC")){
                clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC,RedisPrefix.CUCC);
                evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC,RedisPrefix.CUCC);
                exclusiveKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_EXCLUSIVE, RedisPrefix.CUCC);
                appointKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_APPOINT, RedisPrefix.CUCC);
            }else if (netName.toUpperCase().equals("CNC")){
                clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC,RedisPrefix.CNC);
                evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC,RedisPrefix.CNC);
                exclusiveKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_EXCLUSIVE, RedisPrefix.CNC);
                appointKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_APPOINT, RedisPrefix.CNC);
            }else{
                clickKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC,RedisPrefix.CMCC);
                evKey = String.format(RedisPrefix.PROXY_IP_LIST_EV_IDC,RedisPrefix.CMCC);
                exclusiveKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_EXCLUSIVE, RedisPrefix.CMCC);
                appointKey = String.format(RedisPrefix.PROXY_IP_LIST_IDC_APPOINT, RedisPrefix.CMCC);
            }

            String addr = ps[6];
            int cityId = -1;
            int provinceId = -1;
            if (addr != null && !addr.equals("")){
                String[] str = addr.split("省");
                String province = str[0].replace("市","");
                provinceId = provincesMap.get(province);

                if (str.length > 1){
                    String city = str[1];
                    if (provincesMap.containsKey(city))
                        cityId = provincesMap.get(city);
                }
            }

            if (provinceIds.contains(provinceId) && provinceId > 0){
                for (int i = 0;i < 10;i++) {
                    redisClient.lpushString(clickKey + "_" + provinceId, v);
                    redisClient.lpushString(evKey + "_" + provinceId, v);
                }
                for (Map.Entry<Integer, TaskType> entry : exclusiveProxyIds.entrySet()) {
                    redisClient.lpushString(exclusiveKey + "_" + entry.getKey() + "_" + provinceId, v);
                }
                for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> entry : appointProxyIds.entrySet()) {
                    if (entry.getValue().getK().contains(agentName)) {
                        redisClient.lpushString(appointKey + "_" + entry.getKey() + "_" + provinceId, v);
                    }
                }
            } else if (provinceIds.contains(cityId) && cityId > 0){
                for (int i = 0;i < 10;i++) {
                    redisClient.lpushString(clickKey + "_" + cityId, v);
                    redisClient.lpushString(evKey + "_" + cityId, v);
                }
                for (Map.Entry<Integer, TaskType> entry : exclusiveProxyIds.entrySet()) {
                    redisClient.lpushString(exclusiveKey + "_" + entry.getKey() + "_" + cityId, v);
                }
                for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> entry : appointProxyIds.entrySet()) {
                    if (entry.getValue().getK().contains(agentName)) {
                        redisClient.lpushString(appointKey + "_" + entry.getKey() + "_" + cityId, v);
                    }
                }
            } else{
                redisClient.lpushString(clickKey, v);
                redisClient.lpushString(evKey, v);
                for (Map.Entry<Integer, TaskType> entry : exclusiveProxyIds.entrySet()) {
                    redisClient.lpushString(exclusiveKey + "_" + entry.getKey(), v);
                }
                for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> entry : appointProxyIds.entrySet()) {
                    if (entry.getValue().getK().contains(agentName)) {
                        redisClient.lpushString(appointKey + "_" + entry.getKey(), v);
                    }
                }
            }

            Map<String,Object> map = new HashMap<>();
            map.put("lastUpdateTime",System.currentTimeMillis());
            Where where = new Where();
            where.and("host",ps[4]);
            mongoClient.getCollection("vps");
            mongoClient.update(map,where);
        }catch (Exception er){
            er.printStackTrace();
            logger.error(er);
            return false;
        }
        return true;
    }
}