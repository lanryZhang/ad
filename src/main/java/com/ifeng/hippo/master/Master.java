package com.ifeng.hippo.master;

import com.alibaba.fastjson.JSON;
import com.ifeng.configurable.Configurable;
import com.ifeng.configurable.Context;
import com.ifeng.core.clean.CleanupAware;
import com.ifeng.core.clean.ShutdownManager;
import com.ifeng.core.distribute.codec.MessageDecode;
import com.ifeng.core.distribute.codec.MessageEncode;
import com.ifeng.core.distribute.handlers.HeartBeatRespHandler;
import com.ifeng.hippo.contances.Platform;
import com.ifeng.hippo.contances.RedisPrefix;
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

import java.net.InetSocketAddress;
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
    private ScheduledExecutorService ses;
    private ExecutorService es = Executors.newFixedThreadPool(2);
    private final static Logger logger = Logger.getLogger(TaskAssignmentReqHandler.class);

    private static AtomicInteger taskParallel = new AtomicInteger(0);
    private static AtomicInteger workerNumber = new AtomicInteger(0);

    private static int initTaskParallel = 100;
    private Context context;
    private ConcurrentHashMap<Integer, Double> realValueMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, WorkerDescriptor> workerMap = new ConcurrentHashMap<>();
    private RedisClient redisClient = RedisFactory.newInstance();
    private long lastUpdateTime = 0L;
    private static int taskAssignAvg = 0;

    @Override
    public void cleanup() {
        if (!es.isShutdown())
            es.shutdown();

        if (!ses.isShutdown())
            ses.shutdown();
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
        private HashMap<Integer, List<Double>> errorRatioMap = new HashMap<>();

        public TaskAssginServer() {
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
            try {
                mongoClient.changeDb("hippo");
                mongoClient.getCollection("tasks");

                MongoSelect select = new MongoSelect();
                select.orderBy(new OrderBy("taskType", OrderByDirection.ASC));
                Date date = DateUtil.parse(DateUtil.today());

                select.where("beginDate", WhereType.LessAndEqual, date);
                select.where("endDate", WhereType.GreaterAndEqual, date);
                select.where("status", 0);
                select.orderBy(new OrderBy("targetPv", OrderByDirection.ASC));

                List<TaskInfo> tasks = mongoClient.selectList(select, TaskInfo.class);

                initRemainValueMap(tasks);
//                for (int i = 0; i < wn; i++) {
                List<TaskFragment> list = splitTask(tasks);



                if (list.size() > 0) {
                    for (TaskFragment tf : list) {
                        DataManager.getMasterQueue().offer(tf);
                    }
                    int divided = workerNumber.get() > 0 ? workerNumber.get() : 1;
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

        private List<TaskFragment> splitTask(List<TaskInfo> tasks) {
            List<TaskFragment> list = new ArrayList<>();
            TreeMap<Integer, Integer> standardLine = null;

            for (TaskInfo taskInfo : tasks) {
                try {

                    if (taskInfo.getFiltration() == null || !taskInfo.getFiltration().contains(DateUtil.today())) {
                        DataManager.getMasterQueue().clear();
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
                    for (TimePair tp : taskInfo.getTimePairs()) {
                        if (now >= tp.getBeginTime() &&
                                now <= tp.getEndTime()) {
                            end = tp.getEndTime() - 9;
                            inTimePair = true;
                        }

//                        SortedMap<Integer, Integer> map = standardLine.subMap(tp.getBeginTime(),true,tp.getEndTime(),true);
//
//                        for (Map.Entry<Integer, Integer> en : map.entrySet()) {
//                           standardLineTotalNum += en.getValue();
//                        }
                    }

                    String successKey = String.valueOf(taskInfo.getTaskId()) + "_success";
                    String errorKey = String.valueOf(taskInfo.getTaskId()) + "_error";

                    if (!inTimePair) {
                        if (DataManager.getProcessCount().containsKey(successKey))
                            DataManager.getProcessCount().remove(successKey);
                        if (DataManager.getProcessCount().containsKey(errorKey))
                            DataManager.getProcessCount().remove(errorKey);

                        errorRatioMap.remove(taskInfo.getTaskId());

                        DataManager.getMasterQueue().clear();
                        continue;
                    }

                    SortedMap<Integer, Integer> map = standardLine.subMap(now,true,end,true);

                    for (Map.Entry<Integer, Integer> en : map.entrySet()) {
                        standardLineTotalNum += en.getValue();
                    }

                    int tpv = 0;
                    double lastRemainValue = 0;
                    int finishedPv = 0, remainPv = 0;
                    int targetPv = taskInfo.getTargetPv() + taskInfo.getOverflow();
                    lastRemainValue = realValueMap.get(taskInfo.getTaskId());
                    String rk = String.format(RedisPrefix.TASK_TARGET_PV_FINISHED_PREFIX,
                            taskInfo.getTaskId(), DateUtil.today());
                    String finishedPvStr = redisClient.getString(rk);

                    finishedPv = Integer.valueOf(finishedPvStr == null || "".equals(finishedPvStr) ? "0" : finishedPvStr);
                    remainPv = targetPv - finishedPv;
                    double ratio = ((standardLine.get(now) * 1.0) / standardLineTotalNum);

                    lastRemainValue = lastRemainValue + remainPv * ratio;

                    tpv = (int) lastRemainValue;

                    if (lastRemainValue < 1) {
                        realValueMap.put(taskInfo.getTaskId(), lastRemainValue);
                    } else {
                        realValueMap.put(taskInfo.getTaskId(), lastRemainValue - tpv);
                    }
                    int reportErrorCount = 0, successCount = 0;

                    if (DataManager.getProcessCount().containsKey(errorKey)) {
                        reportErrorCount = DataManager.getProcessCount().get(errorKey).get();
                        DataManager.getProcessCount().get(errorKey).set(0);
                    }

                    double errorRatio = 0;
                    if (DataManager.getProcessCount().containsKey(successKey)) {
                        successCount = DataManager.getProcessCount().get(successKey).get();
                        DataManager.getProcessCount().get(successKey).set(0);
                        if (reportErrorCount + successCount > 0)
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
                    double avgRatio = 0;

                    for (Double r : listRatio) {
                        avgRatio += r;
                    }
                    if (listRatio.size() > 0) {
                        avgRatio = (avgRatio * 1.0) / listRatio.size();
                    }
                    int t = tpv;

                    if (avgRatio > 0 && avgRatio < 1) {
                        tpv = (int) (tpv / (1.0 - avgRatio));
                    } else if (avgRatio == 1) {
                        tpv += tpv;
                    }

                    int addErrorCount = 3;
                    if (reportErrorCount < addErrorCount &&
                            addErrorCount > tpv){
                        tpv += reportErrorCount;
                    }

                    logger.info("task assign num : " + t + " task id :" + taskInfo.getTaskId() + " reassign error count:"
                            + reportErrorCount + " success count:" + successCount + " error ratio:" + avgRatio + " assign actually:" + tpv);

                    if (System.currentTimeMillis() - lastUpdateTime > 10 * 60 * 1000){
                        DataManager.getProcessCount().remove(successKey);
                        DataManager.getProcessCount().remove(errorKey);
                        errorRatioMap.clear();
                    }

                    if (tpv == 0) {
                        continue;
                    }

                    for (int i = 0; i < tpv; i++) {
                        TaskFragment tf = new TaskFragment();
                        tf.setTaskName(taskInfo.getTaskName());
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
                        tf.setProvinces(taskInfo.getProvinces());
                        tf.setForceArrive(taskInfo.getForceArrive());
                        tf.setTaskSource(taskInfo.getTaskSource());
                        tf.setApi(taskInfo.getApi());
                        tf.setMainScriptPath(taskInfo.getMainScriptPath());
                        list.add(tf);
//                        logger.debug("split task, taskFragment info : " + tf.toString() + " finishedPv :" + finishedPv + " remainPv : " + remainPv + " standardLineTotalNum :" + standardLineTotalNum);
                    }
                    lastRemainValue = lastRemainValue - t;

                    realValueMap.put(taskInfo.getTaskId(), lastRemainValue);

                } catch (Exception er) {
                    logger.error(er);
                }
            }
            return list;
        }

        private List<TaskFragment> changeTaskToFragment(List<TaskInfo> tasks) {

            List<TaskFragment> tfs = new ArrayList<>();
            for (TaskInfo taskInfo : tasks) {
                TaskFragment tf = new TaskFragment();
                tf.setTaskName(taskInfo.getTaskName());
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
                tfs.add(tf);
            }
            return tfs;
        }
    }

    private static HashSet<String> sets = new HashSet();

    class WorkWatchCallBack extends WatchCallBack {


        @Override
        public Object callback(TreeCacheEvent event) {
            try {
                WorkerDescriptor wd = null;
                if (event.getData() != null) {
                    switch (event.getType()) {
                        case NODE_ADDED:
                            if (event.getData().getData() != null && event.getData().getData().length > 0) {
                                wd = JSON.parseObject(new String(event.getData().getData(), "utf-8"), WorkerDescriptor.class);
                                workerMap.put(event.getData().getPath(), wd);
                                taskParallel.addAndGet(wd.getTaskParallel());
                                logger.info("new worker online," + wd.toString() + " current task parallel is " + (taskParallel.get() == 0 ? initTaskParallel : taskParallel.get()));
                                workerNumber.incrementAndGet();
                            }
                            break;
                        case NODE_REMOVED:
                            workerNumber.decrementAndGet();
                            wd = workerMap.get(event.getData().getPath());
                            taskParallel.addAndGet(-1 * wd.getTaskParallel());
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
            serverMonitorDescriptor.setTaskParallel(taskParallel.get() == 0 ? initTaskParallel : taskParallel.get());
            serverMonitorDescriptor.setTaskParallelDesc(taskParallel.get() == 0 ? "default value" : " stat of all workers");
            serverMonitorDescriptor.setPort(context.getInt("port"));
            serverMonitorDescriptor.setHostIp(context.getString("hostIp"));
            serverMonitorDescriptor.setBlockingTaskNumber(DataManager.getMasterQueue().size());
            serverMonitorDescriptor.setUpdateTime(System.currentTimeMillis());
            serverMonitorDescriptor.setWorkerNumber(workerNumber.get());
            serverMonitorDescriptor.setCreateTime(createTime);
        }
    }

    @Override
    public void config(Context context) {
        this.hostIp = context.getString("hostIp");
        this.port = context.getInt("port");
        this.partners = context.getString("partners");
        if (this.partners != null && !"".equals(this.partners)) {
            try {
                String[] arr = this.partners.split(",");
                ses = Executors.newScheduledThreadPool(arr.length + 3);
                for (String p : arr) {
                    String url = context.getString("partners." + p + ".url");
                    String className = context.getString("partners." + p + ".processor.class");
                    int interval = context.getInt("partners." + p + ".interval");
                    String name = context.getString("partners." + p + ".name");
                    String charset = context.getString("partners." + p + ".charset", "gbk");
                    String usedFor = context.getString("partners." + p + ".usedFor", "all");
                    Context c = new Context();
                    c.put("url", url);
                    c.put("name", name);
                    c.put("charset", charset);
                    c.put("usedFor", usedFor);
                    IProxyExecutor iProxyExecutor = (IProxyExecutor) Class.forName(className).newInstance();
                    iProxyExecutor.config(c);
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
        MonitorServer ms = new MonitorServer();
        ShutdownManager.regist(ms);
        es.submit(ms);

        WorkerMonitor wm = new WorkerMonitor();
        ShutdownManager.regist(wm);

        es.submit(wm);

        TaskAssginServer tas = new TaskAssginServer();
        ses.scheduleAtFixedRate(tas, 0, 1000 * 60, TimeUnit.MILLISECONDS);

        ServerMonitor sm = new ServerMonitor();
        ShutdownManager.regist(sm);
        ses.scheduleAtFixedRate(sm, 0, 1000 * 4, TimeUnit.MILLISECONDS);
    }
}
