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
import com.ifeng.hippo.entity.AdsFlowLogEntity;
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

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
@WebController
public class TaskRebalanceController implements MessageProcessor {
    private RedisClient redisClient = RedisFactory.newInstance();
    private Logger logger = Logger.getLogger(TaskRebalanceController.class);
    private MongoCli adClient = MongoFactory.createAdMongoClient();

    @RequestMapping(value = "rebalance",method= {RequestMethod.GET, RequestMethod.POST })
    @Override
    public Object process(Context context) {
        try {
            String adId = context.getString("adid");
            String ps = context.getString("ps");
            String date = context.getString("date");
            int syncCount = context.getInt("c");

            logger.info("get task sync request, adid:"+adId+" ps:"+ps+" date:"+date+" count:"+syncCount);
            adClient.changeDb("AdsFlowLog");
            adClient.getCollection("adid_ps_taskid");
            MongoSelect select = new MongoSelect();

            select.where("adid", adId);
            select.where("ps", ps);
            select.where("tm", date);

            AdsFlowLogEntity en = adClient.selectOne(select, AdsFlowLogEntity.class);

            if (en == null){
                return true;
            }

            String taskKey = String.format(RedisPrefix.TASK_TARGET_PV_FINISHED_PREFIX, en.getTaskId(), date);

            if ("".equals(redisClient.getString(taskKey)) || redisClient.getString(taskKey) == null){
                return true;
            }else{
                int num = Integer.valueOf(redisClient.getString(taskKey));

                if (syncCount > num){
                    redisClient.set(taskKey,syncCount);
                    logger.info("task error rate was too high, get statistic system sync info--"+en.toString()+" current task finished:"+num);
                }
            }
        } catch (Exception er) {
            logger.error(er);
            return false;
        }
        return true;
    }
}