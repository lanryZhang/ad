/*
* WorkerExecutor.java 
* Created on  202017/5/25 10:45 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.worker;

import com.alibaba.fastjson.JSON;
import com.ifeng.configurable.Context;
import com.ifeng.core.clean.CleanupAware;
import com.ifeng.core.distribute.message.MessageFactory;
import com.ifeng.hippo.core.ProcessorGenerator;
import com.ifeng.hippo.core.data.DataManager;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.filters.*;
import com.ifeng.hippo.task.IExecutor;
import com.ifeng.hippo.task.TaskExecutorExec;
import com.ifeng.hippo.zookeeper.ZkState;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class WorkerExecutor implements Callable, CleanupAware {
    private Context context;
    private WorkerDescriptor workerDescriptor;
    private ThreadPoolExecutor executorService;
    private ProcessorGenerator processorGenerator;
    private int taskParallel;
    private AtomicInteger runningTasks;
    private ZkState zkState;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private ChannelHandlerContext ctx;
    private final static Logger logger = Logger.getLogger(WorkerExecutor.class);
    private long lastSubmitTime = System.currentTimeMillis();
    private AtomicInteger processTaskNum = new AtomicInteger(0);
    private PriorityBlockingQueue queue ;
    public WorkerExecutor(Context context){
        queue = new PriorityBlockingQueue<>();
        this.context = context;
        /** 任务节点并行量 */
        this.taskParallel = context.getInt("task.parallel");
        int poolSize = this.taskParallel * 2;
        executorService = new ThreadPoolExecutor(poolSize,poolSize, 0, TimeUnit.MILLISECONDS,queue);
        /** 任务生成器 */
        processorGenerator = new ProcessorGenerator(context);
//        webDriverPool = new WebDriverPool(context);

        /** 任务执行器配置代理处理流程 */
        processorGenerator.registFilter(new ProxyFilter());
        /** 任务执行器配置UserAgent处理流程 */
        processorGenerator.registFilter(new UAFilter());
        processorGenerator.registFilter(new ApiFilter());
        /** 配置精准投放处理流程 */
        processorGenerator.registFilter(new DspFilter());
        processorGenerator.registFilter(new DmpUrlFilter());

        /** 正在执行的任务数量 */
        runningTasks = new AtomicInteger(0);
        ctx = (ChannelHandlerContext) context.getObject("ctx");
        /** 客户端描述 */
        workerDescriptor = new WorkerDescriptor();
        workerDescriptor.setCreateTime(System.currentTimeMillis());
        workerDescriptor.setHostIp(context.getString("hostIp"));
        workerDescriptor.setLocalIp(context.getString("localIp"));
        workerDescriptor.setPort(context.getInt("port"));
        workerDescriptor.setPid(context.getInt("pid"));
        workerDescriptor.setWorkerId(UUID.randomUUID().toString());
        workerDescriptor.setTaskParallel(context.getInt("task.parallel"));

        zkState = new ZkState(context);
    }

    /**
     * 在Zookeeper创建节点
     */
    private void createWorkNode() {
        try {
            String zkPath = context.getString("zkPath");
            String nodeName = context.getString("localIp") + ":" + workerDescriptor.getWorkerId();
            zkPath = StringUtils.stripEnd(zkPath, "/");
            zkPath = zkPath + "/" + nodeName;

            workerDescriptor.setLastUpdateTime(System.currentTimeMillis());
            zkState.writeBytes(zkPath, JSON.toJSONString(workerDescriptor).getBytes());
        }catch (Exception er){
            logger.error(er);
        }
    }

    /**
     * 向Zookeeper提交节点状态信息
     */
    private void submitWorkerDescriptor( ){
        try {
            if (System.currentTimeMillis() - lastSubmitTime > 2000) {
                lastSubmitTime = System.currentTimeMillis();
                String zkPath = context.getString("zkPath");
                String nodeName = context.getString("localIp") + ":" + workerDescriptor.getWorkerId();
                zkPath = StringUtils.stripEnd(zkPath, "/");
                zkPath = zkPath + "/" + nodeName;

                workerDescriptor.setLastUpdateTime(System.currentTimeMillis());
                workerDescriptor.setProcessTaskNum(processTaskNum.get());
                workerDescriptor.setThreadPoolSize(queue.size());

                zkState.writeBytes(zkPath, JSON.toJSONString(workerDescriptor).getBytes());
                logger.info("update status to zookeeper..");
            }
        }catch (RuntimeException er){
            logger.error("zookeeper error, init zkStat again!" + er);
            try {
                zkState = new ZkState(context);
            }finally {

            }
        }
    }

    @Override
    public Object call() throws Exception {
        /** 在Zookeeper注册节点 */
        createWorkNode();
        TaskFragment preProcessTask ;
        KeyValuePair<String,Integer> taskParallelKv = new KeyValuePair<>("taskParallel",taskParallel);
        KeyValuePair<String,Integer> kv;
        long lastSubmitTime = System.currentTimeMillis();

        while (countDownLatch.getCount() > 0) {
            try {
                /** 如果当前线程阻塞队列的阻塞数量少于并发量，则执行任务 */
                if (queue.size() <= taskParallel) {
                    /** 从任务队列中取出任务 */
                    preProcessTask = DataManager.getWorkerQueue().poll(4000, TimeUnit.MILLISECONDS);

                    /** 提交客户端状态信息 */
                    submitWorkerDescriptor();
                    /** 如果任务队列空了，向服务端发请求，请求新任务 */
                    if (preProcessTask == null) {
                        List<KeyValuePair<String, Integer>> list = new ArrayList<>();
                        list.add(taskParallelKv);
                        if (System.currentTimeMillis() - lastSubmitTime > 1000) {
                            /** 将之前跑的任务报告给服务端 */
                            for (Map.Entry<String, AtomicInteger> item : DataManager.getProcessCount().entrySet()) {
                                if (item.getValue().get() > 0) {
                                    kv = new KeyValuePair<>(item.getKey(), item.getValue().get());
                                    list.add(kv);
                                    item.getValue().set(0);
                                }
                            }
                            lastSubmitTime = System.currentTimeMillis();
                        }
                        /** 向服务端汇报状态或拿取任务 */
                        ctx.writeAndFlush(MessageFactory.createTaskAssignmentReqMessage(list));
                        continue;
                    }
                    /** 增加已执行数量 */
                    processTaskNum.incrementAndGet();
                    preProcessTask.setBeginTime(System.currentTimeMillis());

                    executorService.execute(new Task(preProcessTask, workerDescriptor));
                    /** 增加正在运行的任务数 */
                    runningTasks.incrementAndGet();
                }
                if (runningTasks.get() >= taskParallel) {
                    runningTasks.set(0);
                    logger.info("current threadpool blocking size:"+queue.size());
                    logger.debug("get task from server:" + runningTasks.get());
                }
            } catch (Exception er) {
                er.printStackTrace();
                logger.error(er);
            }
        }

        return 0;
    }

    @Override
    public void cleanup() {
        logger.info("worker beginning shutdown...");
        processorGenerator.closeAll();
        countDownLatch.countDown();
        executorService.shutdown();
        if (zkState != null){
            zkState.remove();
            zkState.close();
        }
        DataManager.getWorkerQueue().clear();
    }

    public  class Task implements Comparable,Runnable{
        private TaskFragment tf;
        private IExecutor taskExecutor;
        public Task(TaskFragment tf,WorkerDescriptor workerDescriptor){
            /** 任务执行器 */
            taskExecutor = new TaskExecutorExec(processorGenerator,workerDescriptor);
            this.tf = tf;
        }

        public TaskFragment getTf() {
            return tf;
        }

        @Override
        public void run() {
            try {
                /** 任务开始执行 */
                taskExecutor.execute(tf);
            } catch (Exception er){
                logger.error(er);
            } finally {
            }
//            return tf;
        }

        @Override
        public int compareTo(Object o) {
            /** 判断任务目标量做比较 */
            Task t = (Task) o;
            if (this.getTf().getTargetPv() < t.getTf().getTargetPv()){
                return -1;
            }
            if (this.getTf().getTargetPv() > t.getTf().getTargetPv()){
                return 1;
            }
            return 0;
        }
    }
}
