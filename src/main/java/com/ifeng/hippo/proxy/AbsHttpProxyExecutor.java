/*
* HttpProxyExecutor.java 
* Created on  202017/8/14 10:55 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.ifeng.configurable.Context;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.TaskInfo;
import com.ifeng.hippo.entity.TimePair;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.utils.HttpResult;
import com.ifeng.hippo.utils.HttpUtils;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.mongo.query.OrderBy;
import com.ifeng.mongo.query.OrderByDirection;
import com.ifeng.mongo.query.WhereType;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public abstract class AbsHttpProxyExecutor extends AbsProxyPersistence implements IProxyExecutor {
    private String url = "";
    private static final Logger logger = Logger.getLogger(AbsHttpProxyExecutor.class);
    protected String name = "";
    private String charset = "gbk";
    private long lastUpdateTimestamp = 0;
    private List<Integer> times = new ArrayList<>();

    @Override
    public void execute() {
        try {
            if (url == null || "".equals(url) || !getSpecialControl()) {
                return;
            }

            if (System.currentTimeMillis() - lastUpdateTimestamp > 60 * 1000) {
                lastUpdateTimestamp = System.currentTimeMillis();
                refreshProvince();

                refreshData();
            }
            int now = Integer.valueOf(DateUtil.format(new Date(), "HHmm"));

            if ((now > 858 && now < 900) || (now > 500 && now < 502)) {
                clearProxyPool();
                return;
            }

            if (now >= times.get(0) && now <= times.get(1)) {
                HttpResult result = HttpUtils.httpGet(url, Charset.forName(charset));
                String res = String.valueOf(result.getBody());

                logger.debug(res);

                doProcess(res);
            }

        } catch (Exception er) {
            logger.error(er);
        }
    }

    private void clearProxyPool() {
        try {
            redisClient.del("proxy_ip_list_cnc");
            redisClient.del("proxy_ip_list_ev_cnc");

            redisClient.del("proxy_ip_list_cmcc");
            redisClient.del("proxy_ip_list_ev_cmcc");

            redisClient.del("proxy_ip_list_cucc");
            redisClient.del("proxy_ip_list_ev_cucc");

            for (int r : provinceIds) {
                for (Map.Entry e : exclusiveProxyIds.entrySet()) {
                    redisClient.del("proxy_ip_list_exclusive_cnc_" + e.getKey() + "_" + r);
                }
                for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> e : appointProxyIds.entrySet()) {
                    redisClient.del("proxy_ip_list_appoint_cnc_" + e.getKey() + "_" + r);
                }
                redisClient.del("proxy_ip_list_cnc_" + r);
                redisClient.del("proxy_ip_list_ev_cnc_" + r);
            }
            for (Map.Entry e : exclusiveProxyIds.entrySet()) {
                redisClient.del("proxy_ip_list_exclusive_cnc_" + e.getKey());
            }
            for (Map.Entry<Integer, KeyValuePair<List<String>, TaskType>> e : appointProxyIds.entrySet()) {
                redisClient.del("proxy_ip_list_appoint_cnc_" + e.getKey());
            }
        } catch (Exception er) {
            logger.error(er);
        }
    }

    @Override
    public void run() {
        execute();
    }

    public abstract void doProcess(Object result);

    public boolean getSpecialControl() {
        return true;
    }

    private void refreshData() {
        times.clear();

        times.add(900);
        times.add(2359);
        try {
            mongoClient.changeDb("hippo");
            mongoClient.getCollection("tasks");

            MongoSelect select = new MongoSelect();
            select.orderBy(new OrderBy("taskType", OrderByDirection.ASC));
            Date date = DateUtil.parse(DateUtil.today());

            select.where("beginDate", WhereType.LessAndEqual, date);
            select.where("endDate", WhereType.GreaterAndEqual, date);
            select.where("status", 0);

            int bt = 900, et = 2359;
            List<TaskInfo> tasks = mongoClient.selectList(select, TaskInfo.class);

            provinceIds.clear();
            exclusiveProxyIds.clear();
            appointProxyIds.clear();

            for (TaskInfo task : tasks) {
                List<TimePair> t = task.getTimePairs();
                if (task.getFiltration().contains(DateUtil.today())) {
                    for (TimePair timePair : t) {
                        if (timePair.getBeginTime() < bt) {
                            bt = timePair.getBeginTime();
                        }
                        if (timePair.getEndTime() > et) {
                            et = timePair.getEndTime();
                        }
                    }

                    if (task.getAppointProxyName() != null && task.getAppointProxyName().size() > 0) {
                        KeyValuePair<List<String>, TaskType> keyValuePair = new KeyValuePair<>();
                        keyValuePair.setK(task.getAppointProxyName());
                        keyValuePair.setV(task.getTaskType());
                        appointProxyIds.put(task.getTaskId(), keyValuePair);
                    } else if (task.getExclusiveProxy() == 1) {
                        exclusiveProxyIds.put(task.getTaskId(), task.getTaskType());
                    }
                    provinceIds.addAll(task.getProvinces());
                }
            }
            times.set(0, bt);
            times.set(1, et);
        } catch (Exception e) {
            logger.error(e);
        } finally {
        }

    }


    @Override
    public void config(Context context) {
        this.url = context.getString("url");
        this.name = context.getString("name");
        this.charset = context.getString("charset", "gbk");
        this.usedFor = context.getString("usedFor", "all");
        this.reusetimes = context.getInt("reusetimes", 3);
    }
}
