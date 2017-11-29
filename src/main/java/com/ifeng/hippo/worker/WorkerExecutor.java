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
import com.ifeng.hippo.filters.ProxyFilter;
import com.ifeng.hippo.filters.UAFilter;
import com.ifeng.hippo.task.IExecutor;
import com.ifeng.hippo.task.TaskExecutorExec;
import com.ifeng.hippo.zookeeper.ZkState;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.BlockingArrayQueue;

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
    public WorkerExecutor(Context context){
        this.context = context;
        this.taskParallel = context.getInt("task.parallel");
        int poolSize = this.taskParallel * 2;
        executorService = new ThreadPoolExecutor(poolSize,poolSize, 0, TimeUnit.MILLISECONDS,new PriorityBlockingQueue<>());
        processorGenerator = new ProcessorGenerator(context);
//        webDriverPool = new WebDriverPool(context);
        processorGenerator.registFilter(new ProxyFilter());
        processorGenerator.registFilter(new UAFilter());

        runningTasks = new AtomicInteger(0);
        ctx = (ChannelHandlerContext) context.getObject("ctx");
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

                zkState.writeBytes(zkPath, JSON.toJSONString(workerDescriptor).getBytes());
                logger.info("update status to zookeeper..");
            }
        }catch (RuntimeException er){
            logger.error("zookeeper error, init zkStat again!" + er);
            zkState = new ZkState(context);
        }
    }

    @Override
    public Object call() throws Exception {
        createWorkNode();
        TaskFragment preProcessTask ;
        KeyValuePair<String,Integer> taskParallelKv = new KeyValuePair<>("taskParallel",taskParallel);
        KeyValuePair<String,Integer> kv;
        long lastSubmitTime = System.currentTimeMillis();

        while (countDownLatch.getCount() > 0) {
            try {
                preProcessTask = DataManager.getWorkerQueue().poll(4000, TimeUnit.MILLISECONDS);

                submitWorkerDescriptor();
                if (preProcessTask == null) {

                    List<KeyValuePair<String, Integer>> list = new ArrayList<>();
                    list.add(taskParallelKv);
                    if (System.currentTimeMillis() - lastSubmitTime > 1000) {
                        for (Map.Entry<String, AtomicInteger> item : DataManager.getProcessCount().entrySet()) {
                            if (item.getValue().get() > 0) {
                                kv = new KeyValuePair<>(item.getKey(), item.getValue().get());
                                list.add(kv);
                                item.getValue().set(0);
                            }
                        }
                        lastSubmitTime = System.currentTimeMillis();
                    }
                    ctx.writeAndFlush(MessageFactory.createTaskAssignmentReqMessage(list));
                    continue;
                }
                processTaskNum.incrementAndGet();
                preProcessTask.setBeginTime(System.currentTimeMillis());

                executorService.execute(new Task(preProcessTask, workerDescriptor));
                runningTasks.incrementAndGet();

                if (runningTasks.get() >= taskParallel) {
                    runningTasks.set(0);

                    logger.debug("get task from server:" + runningTasks.get());
                    Thread.currentThread().sleep(10 * 1000);
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
            taskExecutor = new TaskExecutorExec(processorGenerator,workerDescriptor);
            this.tf = tf;
        }

        public TaskFragment getTf() {
            return tf;
        }

        @Override
        public void run() {
            try {
                taskExecutor.execute(tf);
            } catch (Exception er){
                logger.error(er);
            } finally {
            }
//            return tf;
        }

        @Override
        public int compareTo(Object o) {
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
