package com.ifeng.hippo.master;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.ifeng.configurable.Configurable;
import com.ifeng.configurable.Context;
import com.ifeng.core.clean.CleanupAware;
import com.ifeng.core.clean.ShutdownManager;
import com.ifeng.core.distribute.codec.MessageDecode;
import com.ifeng.core.distribute.codec.MessageEncode;
import com.ifeng.core.distribute.handlers.HeartBeatRespHandler;
import com.ifeng.hippo.contances.DeviceInfo;
import com.ifeng.hippo.contances.Platform;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.contances.TaskSource;
import com.ifeng.hippo.entity.*;
import com.ifeng.hippo.handlers.TaskAssignmentReqHandler;
import com.ifeng.hippo.handlers.TaskAssignmentRespHandler;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.proxy.IProxyExecutor;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.hippo.core.data.DataManager;
import com.ifeng.hippo.worker.WorkerDescriptor;
import com.ifeng.hippo.zookeeper.WatchCallBack;
import com.ifeng.hippo.zookeeper.ZkState;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.mongo.query.OrderBy;
import com.ifeng.mongo.query.OrderByDirection;
import com.ifeng.mongo.query.WhereType;
import com.ifeng.redis.RedisClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by zhanglr on 2016/8/28.
 */
public class Master implements Configurable, CleanupAware {
    private String hostIp = "0.0.0.0";
    private int port = 8888;
    private String partners = "";
    private String cluster = "jinshan";
    private ScheduledExecutorService ses;
    private ScheduledExecutorService deviceTagExecService;
    private ExecutorService es = Executors.newFixedThreadPool(2);
    private final static Logger logger = Logger.getLogger(Master.class);
    protected static ConcurrentHashMap<String, Integer> provincesMap = new ConcurrentHashMap<>();

    /** 总任务并行数量 */
    private static AtomicInteger taskParallel = new AtomicInteger(0);
    /** 客户端数量 */
    private static AtomicInteger workerNumber = new AtomicInteger(0);
    /** 任务队列阻塞后丢弃的任务数量 */
    private static ConcurrentHashMap<String, AtomicInteger> discardTaskNumber = new ConcurrentHashMap<>();

    /** 初始/默认总任务并行数量 */
    private static int initTaskParallel = 100;
    private Context context;
    /** 分配任务数 用于解决目标量少的任务的按时间平均分配 */
    private ConcurrentHashMap<Integer, Double> realValueMap = new ConcurrentHashMap<>();

    /** 客户端列表 */
    private static ConcurrentHashMap<String, WorkerDescriptor> workerMap = new ConcurrentHashMap<>();
    private RedisClient redisClient = RedisFactory.newInstance();
    private MongoCli mongoClient = MongoFactory.createMongoClient();
    private long lastUpdateTime = 0L;
    /** 为每个客户端分配的平均任务数 */
    private static int taskAssignAvg = 0;
    /** 客户端zookeeper地址集合 */
    private static HashSet<String> sets = new HashSet();
    @Override
    public void cleanup() {
        if (!es.isShutdown())
            es.shutdown();

        if (!ses.isShutdown())
            ses.shutdown();
        if (!deviceTagExecService.isShutdown()) {
            deviceTagExecService.shutdown();
        }
        DataManager.getMasterQueue().clear();
        workerMap.clear();
    }

    class MonitorServer implements Runnable, CleanupAware {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        EventLoopGroup masterGroup = new NioEventLoopGroup(1);

        @Override
        public void run() {
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(masterGroup, workGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 10000)
                        //.option(ChannelOption.SO_LINGER, -1)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                        .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                        .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                        .childHandler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel channel) {
                                channel.pipeline().addLast(new MessageDecode());
                                channel.pipeline().addLast(new MessageEncode());
                                channel.pipeline().addLast(new HeartBeatRespHandler());
                                /** 注册一个请求处理器 向客户端分发任务 */
                                channel.pipeline().addLast(new TaskAssignmentRespHandler());
                            }
                        });
                Channel channel = serverBootstrap.bind(new InetSocketAddress(hostIp, port)).sync().channel();
                channel.closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                masterGroup.shutdownGracefully();
                workGroup.shutdownGracefully();
            }
        }

        @Override
        public void cleanup() {
            masterGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    class TaskAssginServer implements Runnable {
        private MongoCli mongoClient = MongoFactory.createMongoClient();
        private TreeMap<Integer, Integer> pcStandardLine = new TreeMap<>();
        private TreeMap<Integer, Integer> wapStandardLine = new TreeMap<>();
        private TreeMap<Integer, Integer> appStandardLine = new TreeMap<>();
        /** 任务失败率列表 */
        private HashMap<Integer, List<Double>> errorRatioMap = new HashMap<>();

        /***
         * 获取阻塞队列阈值以做优化
         */
        public double getThresholdRatio(){
            int size = DataManager.getMasterQueue().size();
            double ratio;
            if(size > 200000){
                ratio = 0.0;
            } else if(size > 180000){
                ratio = 0.2;
            } else if(size > 150000){
                ratio = 0.4;
            } else if(size > 120000){
                ratio = 0.6;
            } else if(size > 100000){
                ratio = 0.8;
            } else {
                ratio = 1.0;
            }
            return ratio;
        }

        public TaskAssginServer() {
            /** 从数据库中取出基准线 */
            try {
                mongoClient.changeDb("hippo");
                List<TemplateData> list;
                if (pcStandardLine.size() == 0) {
                    mongoClient.getCollection("standard_line");
                    MongoSelect select = new MongoSelect();
                    list = mongoClient.selectAll(select, TemplateData.class);
                    list.forEach(r -> {
                        if (r.getPlatform().equals("pc")) {
                            pcStandardLine.put(r.getTime(), r.getNum());
                        } else if (r.getPlatform().equals("app")) {
                            appStandardLine.put(r.getTime(), r.getNum());
                        } else {
                            wapStandardLine.put(r.getTime(), r.getNum());
                        }
                    });
                }

            } catch (Exception er) {
                logger.error(er);
            }
        }

        @Override
        public void run() {
            /** 从数据库中取出任务列表 */
            try {
                mongoClient.changeDb("hippo");
                mongoClient.getCollection("tasks");

                MongoSelect select = new MongoSelect();
                select.orderBy(new OrderBy("taskType", OrderByDirection.ASC));
                Date date = DateUtil.parse(DateUtil.today());

                select.where("beginDate", WhereType.LessAndEqual, date);
                select.where("endDate", WhereType.GreaterAndEqual, date);
                select.where("status", 0);
                if (!"".equals(cluster)) {
                    select.where("belongCluster", cluster);
                }
                select.orderBy(new OrderBy("targetPv", OrderByDirection.ASC));

                List<TaskInfo> tasks = mongoClient.selectList(select, TaskInfo.class);

                /** 初始化realValueMap */
                initRemainValueMap(tasks);
//                for (int i = 0; i < wn; i++) {
                /** 将子任务切割出来 */
                List<TaskFragment> list = splitTask(tasks);


                if (list.size() > 0) {
                    /** 将任务放到任务队列中，并计算每个节点的平均任务分配数量 */
                    for (TaskFragment tf : list) {
                        DataManager.getMasterQueue().offer(tf);
                    }
                    /** 客户端节点数量 */
                    int divided = workerNumber.get() > 0 ? workerNumber.get() : 1;
                    /** 12是经验值 */
                    taskAssignAvg =  DataManager.getMasterQueue().size() / divided / 12;
                }
//                }
            } catch (Exception e) {
                logger.error(e);
            }
        }

        private void initRemainValueMap(List<TaskInfo> tasks) {
            if (tasks != null) {
                tasks.forEach(r -> {
                    if (!realValueMap.containsKey(r.getTaskId())) {
                        realValueMap.put(r.getTaskId(), 0.0);
                    }
                    initRemainValueMap(r.getSubTasks());
                });
            }
        }

        private List<TaskFragment> splitTask(List<TaskInfo> tasks) throws Exception {
            List<TaskFragment> list = new ArrayList<>();
            TreeMap<Integer, Integer> standardLine = null;
            List<String> assignList = new ArrayList<>();

            for (TaskInfo taskInfo : tasks) {
                try {

                    /** 当任务不是当日任务时，从队列中删除 */
                    if (taskInfo.getFiltration() == null || !taskInfo.getFiltration().contains(DateUtil.today())) {
                        DataManager.getMasterQueue().removeIf(r-> r.getTaskId()== taskInfo.getTaskId());
                        continue;
                    }

                    if (taskInfo.getTimePairs() == null || taskInfo.getTimePairs().size() == 0) {
                        continue;
                    }

                    long standardLineTotalNum = 0L;

                    if (taskInfo.getPlatform() == Platform.APP) {
                        standardLine = appStandardLine;
                    } else if (taskInfo.getPlatform() == Platform.WAP) {
                        standardLine = wapStandardLine;
                    } else {
                        standardLine = pcStandardLine;
                    }

                    boolean inTimePair = false;
                    int now = Integer.valueOf(DateUtil.format(new Date(), "HHmm"));
                    int end = 0;
                    /** 判断当前时间是否在任务执行时间段内 */
                    for (TimePair tp : taskInfo.getTimePairs()) {
                        if (now <= tp.getEndTime()){
                            end = tp.getEndTime() - 9;
                            if (now >= tp.getBeginTime()) {
                                inTimePair = true;
                            }
                        }

//                        SortedMap<Integer, Integer> map = standardLine.subMap(tp.getBeginTime(),true,tp.getEndTime(),true);
//
//                        for (Map.Entry<Integer, Integer> en : map.entrySet()) {
//                           standardLineTotalNum += en.getValue();
//                        }
                    }

                    String successKey = String.valueOf(taskInfo.getTaskId()) + "_success";
                    String errorKey = String.valueOf(taskInfo.getTaskId()) + "_error";

                    /** 当时间不在执行时间段内 将任务从队列中删除 */
                    if (!inTimePair) {
                        if (DataManager.getProcessCount().containsKey(successKey))
                            DataManager.getProcessCount().remove(successKey);
                        if (DataManager.getProcessCount().containsKey(errorKey))
                            DataManager.getProcessCount().remove(errorKey);

                        errorRatioMap.remove(taskInfo.getTaskId());

                        DataManager.getMasterQueue().removeIf(r-> r.getTaskId() == taskInfo.getTaskId());
                        continue;
                    }

                    boolean hasInterest = false;
                    boolean hasUserInfo = false;

                    if (taskInfo.getInterest() != null && !taskInfo.getInterest().isEmpty()) {
                        hasInterest = true;
                    }
                    if (taskInfo.getUserInfo() != null && !taskInfo.getUserInfo().isEmpty()) {
                        hasUserInfo = true;
                    }

                    if (taskInfo.getTaskSource() == TaskSource.ACCURATEOPERATIONS && taskInfo.getPlatform() == Platform.APP && (hasInterest || hasUserInfo)){
                        String redisTagFlagKey = String.format(RedisPrefix.TAGS_DEVICE_FLAG, taskInfo.getTaskId());
                        String flag = redisClient.getString(redisTagFlagKey);
                        logger.info("device tag flag of task:" + taskInfo.getTaskId() + " is " + flag);
                        if (flag == null || !"1".equals(flag)) {
                            redisClient.set(redisTagFlagKey, 1);
                            if("0".equals(flag)){
                                logger.info("redis key :" + redisTagFlagKey + "is 0 , delete");
                                for (Map.Entry<String,Integer> entry : provincesMap.entrySet()){
                                    String deleteKey = String.format(RedisPrefix.TAGS_DEVICE_LIST, taskInfo.getTaskId());
                                    redisClient.del(deleteKey + "_android_" + entry.getValue());
                                    redisClient.del(deleteKey + "_ios_" + entry.getValue());
                                }
                            }
                            logger.info("get device tag for task:" + taskInfo.getTaskId());
                            TaskDeviceTagServer task = new TaskDeviceTagServer(taskInfo, provincesMap);
                            deviceTagExecService.submit(task);
                            redisClient.expireKey(redisTagFlagKey, 24 * 60 * 60);
                        }
                    }

                    /** 截取当前时间到当任务结束时间的基准线 */
                    SortedMap<Integer, Integer> map = standardLine.subMap(now,true,end,true);

                    for (Map.Entry<Integer, Integer> en : map.entrySet()) {
                        standardLineTotalNum += en.getValue();
                    }

                    int tpv = 0;
                    double lastRemainValue = 0;
                    int finishedPv = 0, remainPv = 0;
                    /** 任务目标量 */
                    int targetPv = taskInfo.getTargetPv() + taskInfo.getOverflow();
                    lastRemainValue = realValueMap.get(taskInfo.getTaskId());
                    String rk = String.format(RedisPrefix.TASK_TARGET_PV_FINISHED_PREFIX,
                            taskInfo.getTaskId(), DateUtil.today());
                    /** 任务已完成量 */
                    String finishedPvStr = redisClient.getString(rk);

                    /** 任务已完成量 */
                    finishedPv = Integer.valueOf(finishedPvStr == null || "".equals(finishedPvStr) ? "0" : finishedPvStr);
                    /** 任务剩余目标量 */
                    remainPv = targetPv - finishedPv;
                    /** 计算当前时间点的执行任务比例 */
                    double ratio = ((standardLine.get(now) * 1.0) / standardLineTotalNum);

                    lastRemainValue = lastRemainValue + remainPv * ratio;

                    /** 舍掉小数部分 此时间点需要执行的次数 */
                    tpv = (int) lastRemainValue;

                    if (lastRemainValue < 1) {
                        realValueMap.put(taskInfo.getTaskId(), lastRemainValue);
                    } else {
                        realValueMap.put(taskInfo.getTaskId(), lastRemainValue - tpv);
                    }
                    int reportErrorCount = 0, successCount = 0;

                    if (DataManager.getProcessCount().containsKey(errorKey)) {
                        /** 获取当前任务失败数量 */
                        reportErrorCount = DataManager.getProcessCount().get(errorKey).get();
                        DataManager.getProcessCount().get(errorKey).set(0);
                    }

                    double errorRatio = 0;
                    if (DataManager.getProcessCount().containsKey(successKey)) {
                        /** 获取当前任务成功数量 */
                        successCount = DataManager.getProcessCount().get(successKey).get();
                        DataManager.getProcessCount().get(successKey).set(0);
                        if (reportErrorCount + successCount > 0)
                            /** 计算当前任务失败率 */
                            errorRatio = (reportErrorCount * 1.0) / (reportErrorCount + successCount);
                    } else {
                        if (reportErrorCount + successCount > 0)
                            errorRatio = 1;
                    }

                    if (!errorRatioMap.containsKey(taskInfo.getTaskId())) {
                        errorRatioMap.put(taskInfo.getTaskId(), new ArrayList<>());
                    }
                    errorRatioMap.get(taskInfo.getTaskId()).add(errorRatio);
                    List<Double> listRatio = errorRatioMap.get(taskInfo.getTaskId());
                    /** 计算任务平均失败率 */
                    double avgRatio = 0;

                    for (Double r : listRatio) {
                        avgRatio += r;
                    }
                    if (listRatio.size() > 0) {
                        avgRatio = (avgRatio * 1.0) / listRatio.size();
                    }
                    int t = tpv;

                    if (avgRatio > 0 && avgRatio < 1) {
                        //控制错误率
                        if (avgRatio > 0.2 && avgRatio < 0.3){
                            avgRatio = 0.1;
                        }else if (avgRatio >= 0.3 && avgRatio < 0.4){
                            avgRatio = 0.08;
                        }else if (avgRatio >= 0.4 && avgRatio < 0.6){
                            avgRatio = 0.05;
                        }else if (avgRatio >= 0.6){
                            avgRatio = 0;
                        }
                        tpv = (int) (tpv / (1.0 - avgRatio));
                    }else if (avgRatio == 1) {
                        tpv += tpv;
                    }

                    int addErrorCount = 3;
                    if (reportErrorCount < addErrorCount &&
                            addErrorCount > tpv){
                        tpv += reportErrorCount;
                    }

                    String assignInfo = "task assign num : " + t + " task id :" + taskInfo.getTaskId() + " reassign error count:"
                            + reportErrorCount + " success count:" + successCount + " error ratio:" + avgRatio + " assign actually:" + tpv;
                    logger.info(assignInfo);
                    assignList.add(assignInfo);

                    if (System.currentTimeMillis() - lastUpdateTime > 10 * 60 * 1000){
                        DataManager.getProcessCount().remove(successKey);
                        DataManager.getProcessCount().remove(errorKey);
                        errorRatioMap.clear();
                    }

                    if (tpv == 0) {
                        continue;
                    }
                    /**
                     * 为解决任务队列阻塞过多
                     * TODO 核验
                     */
                    if (tpv > 1000){
                        double thresholdRatio = getThresholdRatio();
                        int tpv_tmp = (int) (tpv * thresholdRatio);
                        if(discardTaskNumber.containsKey(DateUtil.today())){
                            discardTaskNumber.get(DateUtil.today()).addAndGet(tpv - tpv_tmp);
                        } else {
                            discardTaskNumber.put(DateUtil.today(), new AtomicInteger(tpv - tpv_tmp));
                        }
                        logger.info("drop tasks，taskId"+taskInfo.getTaskId()+" num:"+ discardTaskNumber.get(DateUtil.today()));
                        tpv = tpv_tmp;
                    }

                    for (int i = 0; i < tpv; i++) {
                        try {
                            TaskFragment tf = new TaskFragment();
                            tf.setTaskName(taskInfo.getTaskName().replaceAll("\"", ""));
                            tf.setTargetPv(taskInfo.getTargetPv());
                            tf.setActions(taskInfo.getActions());
                            tf.setPvToUvRatio(taskInfo.getPvToUvRatio());
                            tf.setTaskId(taskInfo.getTaskId());
                            tf.setSubFragments(changeTaskToFragment(taskInfo.getSubTasks()));
                            tf.setUrl(taskInfo.getUrl());
                            tf.setIpReusedTimes(taskInfo.getIpReusedTimes());
                            int random = (int) (5 * Math.random());
                            tf.setWaitTimeout(taskInfo.getWaitTimeout() + random);
                            tf.setRemainPv(taskInfo.getRemainPv());
                            tf.setFinishedPv(taskInfo.getFinishedPv());
                            tf.setPlatform(taskInfo.getPlatform());
                            tf.setTaskType(taskInfo.getTaskType());
                            tf.setScriptPath(taskInfo.getScriptPath());
                            tf.setTimePairs(taskInfo.getTimePairs());
                            tf.setReferer(taskInfo.getReferer());
                            tf.setTaskPosition(taskInfo.getTaskPosition());
                            tf.setFinishedFlag(taskInfo.getFinishedFlag());
                            tf.setDeviceInfo(taskInfo.getDeviceInfo());
                            tf.setRequestType(taskInfo.getRequestType());
                            tf.setUuid(UUID.randomUUID().toString());
                            tf.setGroupId(taskInfo.getGroupId());
                            tf.setForceWait(taskInfo.getForceWait());
                            tf.setExclusiveProxy(taskInfo.getExclusiveProxy());
                            tf.setAppointProxyName(taskInfo.getAppointProxyName());
                            tf.setProvinces(taskInfo.getProvinces());
                            tf.setForceArrive(taskInfo.getForceArrive());
                            tf.setTaskSource(taskInfo.getTaskSource());
                            tf.setApi(taskInfo.getApi());
                            tf.setMainScriptPath(taskInfo.getMainScriptPath());
                            tf.setBehaviourData(convertBehaviourData(taskInfo));
                            tf.setExecuteScript(URLEncoder.encode(taskInfo.getExecuteScript(), "utf-8").replaceAll("\\+", "%20"));
                            tf.setDisableImg(taskInfo.getDisableImg());
                            tf.setShellProportion(taskInfo.getShellProportion());
                            tf.setActiveProportion(taskInfo.getActiveProportion());
                            tf.setInterest(taskInfo.getInterest());
                            tf.setUserInfo(taskInfo.getUserInfo());
                            tf.setDetectionCode(taskInfo.getDetectionCode());
                            list.add(tf);
                        } catch (Exception er) {
                            logger.error(er);
                        }
//                        logger.debug("split task, taskFragment info : " + tf.toString() + " finishedPv :" + finishedPv + " remainPv : " + remainPv + " standardLineTotalNum :" + standardLineTotalNum);
                    }
                    lastRemainValue = lastRemainValue - t;

                    realValueMap.put(taskInfo.getTaskId(), lastRemainValue);

                } catch (Exception er) {
                    logger.error(er);
                }
            }

            /** 将任务分配情况存到Redis */
            redisClient.setString(RedisPrefix.ASSIGN_LIST, JSON.toJSONString(assignList));
            return list;
        }

        /**
         * 随机取出一个行为数据
         * @param taskInfo
         * @return
         */
        private String convertBehaviourData(TaskInfo taskInfo){
            try{
                if (taskInfo.getBehaviourData() != null && !"".equals(taskInfo.getBehaviourData())
                        && taskInfo.getBehaviourData().startsWith("[")){
                    JSONArray arr = JSON.parseArray(taskInfo.getBehaviourData());
                    int index = (int) (Math.random() * arr.size());
                    return URLEncoder.encode(JSON.toJSONString(arr.get(index)).replaceAll("\\s+",""),"utf-8");
                }else{
                    return taskInfo.getBehaviourData().replaceAll("\\s+","");
                }
            }catch (Exception er){}
            return null;
        }

        private List<TaskFragment> changeTaskToFragment(List<TaskInfo> tasks) {

            List<TaskFragment> tfs = new ArrayList<>();
            for (TaskInfo taskInfo : tasks) {
                try {
                    try {
                        TaskFragment tf = new TaskFragment();
                        tf.setTaskName(taskInfo.getTaskName().replaceAll("\"", ""));
                        tf.setTargetPv(taskInfo.getTargetPv());
                        tf.setActions(taskInfo.getActions());
                        tf.setPvToUvRatio(taskInfo.getPvToUvRatio());
                        tf.setTaskId(taskInfo.getTaskId());
                        tf.setSubFragments(changeTaskToFragment(taskInfo.getSubTasks()));
                        tf.setUrl(taskInfo.getUrl());
                        tf.setIpReusedTimes(taskInfo.getIpReusedTimes());
                        tf.setWaitTimeout(taskInfo.getWaitTimeout());
                        tf.setRemainPv(taskInfo.getRemainPv());
                        tf.setFinishedPv(taskInfo.getFinishedPv());
                        tf.setPlatform(taskInfo.getPlatform());
                        tf.setTaskType(taskInfo.getTaskType());
                        tf.setScriptPath(taskInfo.getScriptPath());
                        tf.setTimePairs(taskInfo.getTimePairs());
                        tf.setFinishedFlag(taskInfo.getFinishedFlag());
                        tf.setDeviceInfo(taskInfo.getDeviceInfo());
                        tf.setRequestType(taskInfo.getRequestType());
                        tf.setReferer(taskInfo.getReferer());
                        tf.setUuid(UUID.randomUUID().toString());
                        tf.setGroupId(taskInfo.getGroupId());
                        tf.setForceWait(taskInfo.getForceWait());
                        tf.setProvinces(taskInfo.getProvinces());
                        tf.setForceArrive(taskInfo.getForceArrive());
                        tf.setTaskSource(taskInfo.getTaskSource());
                        tf.setApi(taskInfo.getApi());
                        tf.setMainScriptPath(taskInfo.getMainScriptPath());
                        tf.setBehaviourData(convertBehaviourData(taskInfo));
                        tf.setExecuteScript(URLEncoder.encode(taskInfo.getExecuteScript(), "utf-8").replaceAll("\\+", "%20"));
                        tf.setDisableImg(taskInfo.getDisableImg());
                        tf.setShellProportion(taskInfo.getShellProportion());
                        tf.setActiveProportion(taskInfo.getActiveProportion());
                        tfs.add(tf);
                    }catch (Exception er){
                        logger.error(er);
                    }
                }catch (Exception er){
                    logger.error(er);
                }
            }
            return tfs;
        }
    }

    class TaskDeviceTagServer implements Runnable {
        private MongoCli mongoClient = MongoFactory.createMongoClient();
        private TaskInfo taskInfo;
        private ConcurrentHashMap<String, Integer> provincesMap;
        public TaskDeviceTagServer(TaskInfo taskInfo, ConcurrentHashMap<String, Integer> provincesMap) {
            this.taskInfo = taskInfo;
            this.provincesMap = provincesMap;
        }

        @Override
        public void run() {
            try {
                /** 计算精准投放任务deviceId */
                long totalStartTime = System.currentTimeMillis();
                String redisListFlagKey = String.format(RedisPrefix.TAGS_DEVICE_LIST, taskInfo.getTaskId());
                mongoClient.changeDb("hippo");
                mongoClient.getCollection("device_tags_empty");
                ArrayList<Integer> tags = new ArrayList<>();
                ArrayList<Integer> allTags = new ArrayList<>();
                ArrayList<Integer> inTags = new ArrayList<>();
                tags.addAll(taskInfo.getInterest());
                tags.addAll(taskInfo.getUserInfo());
                Map<Integer, List<Integer>> tagMap = new HashMap<>();
                for (int tag : tags) {
                    List<Integer> list = tagMap.get(tag / 100);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(tag);
                    tagMap.put(tag / 100, list);
                }
                for (Map.Entry<Integer, List<Integer>> entry : tagMap.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().size() > 1) {
                        inTags.addAll(entry.getValue());
                    } else if (entry.getValue() != null && entry.getValue().size() == 1) {
                        allTags.addAll(entry.getValue());
                    }
                }
                if (tags.size() > 0) {
                    logger.info("calculate device tag for task:" + taskInfo.getTaskId());
                    for (Map.Entry<String,Integer> entry : provincesMap.entrySet()){
                        MongoSelect selectTag = new MongoSelect();
                        MongoSelect androidSelect = new MongoSelect();
                        boolean both = false;
                        if (allTags.size() > 0) {
                            selectTag.where("tagList", WhereType.All, allTags);
                            androidSelect.where("tagList", WhereType.All, allTags);
                        }
                        if (inTags.size() > 0) {
                            selectTag.where("tagList", WhereType.In, inTags);
                            androidSelect.where("tagList", WhereType.In, inTags);
                        }
                        if (taskInfo.getDeviceInfo() == DeviceInfo.ANDROID) {
                            selectTag.where("os", "android");
                        } else if (taskInfo.getDeviceInfo() == DeviceInfo.IPHONE) {
                            selectTag.where("os", "ios");
                        } else {
                            both = true;
                            androidSelect.where("os", "android");
                            selectTag.where("os", "ios");
                        }
                        boolean appointed = false;
                        if (taskInfo.getProvinces() != null && taskInfo.getProvinces().size() > 0 && !taskInfo.getProvinces().contains(0)) {
                            selectTag.where("provinceId", WhereType.In, taskInfo.getProvinces());
                            androidSelect.where("provinceId", WhereType.In, taskInfo.getProvinces());
                            selectTag.page(1, 100000);
                            androidSelect.page(1, 100000);
                            appointed = true;
                        } else {
                            selectTag.where("provinceId", entry.getValue());
                            androidSelect.where("provinceId", entry.getValue());
                            selectTag.page(1, 20000);
                            androidSelect.page(1, 20000);
                        }
                        List<DeviceTagsInfo> androidDeviceList = null;
                        if (both) {
                            long startTime = System.currentTimeMillis();
                            androidDeviceList = mongoClient.selectList(androidSelect, DeviceTagsInfo.class);
                            logger.info("device tag android mongo for province:" + entry.getKey() + " time:" + (System.currentTimeMillis() - startTime));
                        }
                        long startTime = System.currentTimeMillis();
                        List<DeviceTagsInfo> deviceList = mongoClient.selectList(selectTag, DeviceTagsInfo.class);
                        logger.info("device tag ios mongo for province:" + entry.getKey() + " time:" + (System.currentTimeMillis() - startTime));
                        if (androidDeviceList != null) {
                            deviceList.addAll(androidDeviceList);
                        }
                        for (DeviceTagsInfo deviceTagsInfo : deviceList) {
                            if ("ios".equals(deviceTagsInfo.getOs())) {
                                String idfa_imei = deviceTagsInfo.getDeviceId();
                                if (!"#".equals(idfa_imei.toLowerCase()) && idfa_imei.length() == 32 && !idfa_imei.startsWith("00000000")) {
                                    idfa_imei = idfa_imei.substring(0, 8) + "-" + idfa_imei.substring(8, 12) + "-" + idfa_imei.substring(12, 16)
                                            + "-" + idfa_imei.substring(16, 20) + "-" + idfa_imei.substring(20);
                                    deviceTagsInfo.setDeviceId(idfa_imei);
                                }
                            }
                            redisClient.lpush(redisListFlagKey + "_" + deviceTagsInfo.getOs() + "_" + deviceTagsInfo.getProvinceId(), deviceTagsInfo.toString());
                            redisClient.expireKey(redisListFlagKey + "_" + deviceTagsInfo.getOs() + "_" + deviceTagsInfo.getProvinceId(), 24 * 60 * 60);
                        }
                        if(appointed){
                            break;
                        }
                    }
                }
                logger.info("device tag for task:" + taskInfo.getTaskId() + " finished time:" + (System.currentTimeMillis() - totalStartTime));
            } catch (Exception er) {
                String redisTagFlagKey = String.format(RedisPrefix.TAGS_DEVICE_FLAG, taskInfo.getTaskId());
                try {
                    redisClient.del(redisTagFlagKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logger.error("device tag error:" + er);
            } finally {
            }
        }
    }

    class WorkWatchCallBack extends WatchCallBack {


        @Override
        public Object callback(TreeCacheEvent event) {
            try {
                WorkerDescriptor wd = null;
                if (event.getData() != null) {
                    switch (event.getType()) {
                        /** 增加节点 */
                        case NODE_ADDED:
                            if (event.getData().getData() != null && event.getData().getData().length > 0) {
                                /** 解析任务描述 */
                                wd = JSON.parseObject(new String(event.getData().getData(), "utf-8"), WorkerDescriptor.class);
                                /** 将客户端添加到客户端列表 */
                                workerMap.put(event.getData().getPath(), wd);
                                /** 增加对应的总任务并行度 */
                                taskParallel.addAndGet(wd.getTaskParallel());
                                logger.info("new worker online," + wd.toString() + " current task parallel is " + (taskParallel.get() == 0 ? initTaskParallel : taskParallel.get()));
                                /** 增加客户端数量 */
                                workerNumber.incrementAndGet();
                            }
                            break;
                        /** 删除节点 */
                        case NODE_REMOVED:
                            /** 减少客户端数量 */
                            workerNumber.decrementAndGet();
                            /** 解析任务描述 */
                            wd = workerMap.get(event.getData().getPath());
                            /** 减少对应的总任务并行数 */
                            taskParallel.addAndGet(-1 * wd.getTaskParallel());
                            /** 在客户端列表减去对应客户端 */
                            workerMap.remove(event.getData().getPath());
                            logger.info("new worker offline," + wd.toString() + " current task parallel is " + (taskParallel.get() == 0 ? initTaskParallel : taskParallel.get()));
                            break;
                    }
                }
            } catch (Exception er) {
                logger.error(er);
            }
            return null;
        }
    }

    /**
     * Worker监控，实时汇报状态至zookeeper
     */
    class WorkerMonitor implements Runnable, CleanupAware {
        private ZkState zkState;

        @Override
        public void run() {
            try {
                zkState = new ZkState(context);

                String zkPath = context.getString("workersPath");

                List<String> childrens = zkState.getCurator().getChildren().forPath(zkPath);
                if (childrens != null && childrens.size() > 0) {
                    for (String path : childrens) {
                        try {
                            String childPath = zkPath + "/" + path;
                            byte[] bs = zkState.getCurator().getData().forPath(childPath);
                            WorkerDescriptor wd = JSON.parseObject(new String(bs, "utf-8"), WorkerDescriptor.class);
                            workerMap.put(childPath, wd);
                            sets.add(childPath);
                        } catch (Exception er) {
                            logger.error(er);
                        }
                    }
                }
                /** 在zookeeper中注册一个监听器，用来监听客户端上下线 */
                zkState.watch(zkPath, new WorkWatchCallBack());
            } catch (Exception er) {
                logger.error(er);
            }
        }

        @Override
        public void cleanup() {
            if (zkState != null) {
                zkState.close();
            }
        }
    }

    /**
     * Server监控，实时汇报状态至zookeeper
     */
    class ServerMonitor implements Runnable, CleanupAware {
        private ServerMonitorDescriptor serverMonitorDescriptor = new ServerMonitorDescriptor();
        private String serverMonitorPath;
        private ZkState zkState;
        private long createTime;

        public ServerMonitor() {
            serverMonitorPath = context.getString("serverMonitorPath");
            zkState = new ZkState(context);
            createTime = System.currentTimeMillis();
        }

        @Override
        public void cleanup() {
            if (zkState != null) {
                zkState.close();
            }
        }

        @Override
        public void run() {
            if (serverMonitorPath == null || "".equals(serverMonitorPath)) {
                logger.error("serverMonitorPath is null!");
                return;
            }
            try {
                buildServerMonitorDescriptor();
                zkState.writeBytes(serverMonitorPath, JSON.toJSONString(serverMonitorDescriptor).getBytes());
            } catch (Exception er) {
                logger.error("report server status error." + er);
            }
        }

        private void buildServerMonitorDescriptor() {
            /** 总任务并行数量 */
            serverMonitorDescriptor.setTaskParallel(taskParallel.get() == 0 ? initTaskParallel : taskParallel.get());
            /** 总任务并行数量来源 */
            serverMonitorDescriptor.setTaskParallelDesc(taskParallel.get() == 0 ? "default value" : " stat of all workers");
            /** 服务端端口 */
            serverMonitorDescriptor.setPort(context.getInt("port"));
            /** 服务端地址 */
            serverMonitorDescriptor.setHostIp(context.getString("hostIp"));
            /** 丢弃的任务数量 */
            serverMonitorDescriptor.setDiscardTaskNumber(discardTaskNumber.get(DateUtil.today()) == null ? 0 : discardTaskNumber.get(DateUtil.today()).get());
            /** 当前任务阻塞数量 */
            serverMonitorDescriptor.setBlockingTaskNumber(DataManager.getMasterQueue().size());
            /** 当前信息更新时间 */
            serverMonitorDescriptor.setUpdateTime(System.currentTimeMillis());
            /** 当前客户端数量 */
            serverMonitorDescriptor.setWorkerNumber(workerNumber.get());
            /** 服务端启动时间 */
            serverMonitorDescriptor.setCreateTime(createTime);
        }
    }

    @Override
    public void config(Context context) {
        /** 服务端 监听IP */
        this.hostIp = context.getString("hostIp");
        /** 服务端 监听端口 */
        this.port = context.getInt("port");
        /** 代理提供商 */
        this.partners = context.getString("partners");
        /** 集群选择 */
        this.cluster = context.getString("cluster");

        mongoClient.changeDb("hippo");
        mongoClient.getCollection("province");

        provincesMap.clear();

        MongoSelect select = new MongoSelect();
        List<Province> provinces = null;
        try {
            provinces = mongoClient.selectList(select, Province.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (provinces != null) {
            for (Province province : provinces) {
                provincesMap.put(province.getName().replace("省", "").replace("市", ""), province.getId());
            }
        }

        this.deviceTagExecService = Executors.newScheduledThreadPool(100);

        if (this.partners != null && !"".equals(this.partners)) {
            try {
                String[] arr = this.partners.split(",");
                /** TODO 为什么启动 arr.length + 3 个线程*/
                ses = Executors.newScheduledThreadPool(arr.length + 3);
                for (String p : arr) {
                    /** 代理商地址 */
                    String url = context.getString("partners." + p + ".url");
                    /** 代理处理类名 */
                    String className = context.getString("partners." + p + ".processor.class");
                    /** 代理刷新间隔 */
                    int interval = context.getInt("partners." + p + ".interval");
                    /** 代理商名称 */
                    String name = context.getString("partners." + p + ".name");
                    /** 处理字符集 */
                    String charset = context.getString("partners." + p + ".charset", "gbk");
                    /** 代理用途 */
                    String usedFor = context.getString("partners." + p + ".usedFor", "all");
                    /** ip重复插入次数*/
                    int reusetimes = context.getInt("partners." + p + ".reusetimes",3);
                    Context c = new Context();
                    c.put("url", url);
                    c.put("name", name);
                    c.put("charset", charset);
                    c.put("usedFor", usedFor);
                    c.put("reusetimes", reusetimes);
                    IProxyExecutor iProxyExecutor = (IProxyExecutor) Class.forName(className).newInstance();
                    iProxyExecutor.config(c);
                    /** 按照代理时间间隔获取代理 */
                    ses.scheduleAtFixedRate(iProxyExecutor, 0, interval, TimeUnit.MILLISECONDS);
                }
            } catch (Exception er) {
                logger.error(er);
            }
        }
        this.context = context;
    }

    public static int getTaskAssignAvg() {
        return taskAssignAvg;
    }

    public void start() {
        /** 启动一个netty 服务端用于向客户端分发任务*/
        MonitorServer ms = new MonitorServer();
        ShutdownManager.regist(ms);
        es.submit(ms);

        /** 从zookeeper中获取客户端的运行状态 */
        WorkerMonitor wm = new WorkerMonitor();
        ShutdownManager.regist(wm);
        es.submit(wm);

        /** 任务分配器 每分钟运行一次 */
        TaskAssginServer tas = new TaskAssginServer();
        ses.scheduleAtFixedRate(tas, 0, 1000 * 60, TimeUnit.MILLISECONDS);

        /** 将服务端信息更新到Zookeeper， 每4秒执行一次 */
        ServerMonitor sm = new ServerMonitor();
        ShutdownManager.regist(sm);
        ses.scheduleAtFixedRate(sm, 0, 1000 * 4, TimeUnit.MILLISECONDS);
    }
}
