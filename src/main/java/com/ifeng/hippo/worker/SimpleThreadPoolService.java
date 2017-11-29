/*
* SimpleThreadPoolService.java 
* Created on  202017/9/15 9:36 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.worker;

import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class SimpleThreadPoolService extends ThreadPoolExecutor {

    private static final Logger logger = Logger.getLogger(SimpleThreadPoolService.class);
    public SimpleThreadPoolService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        FutureTask tee = (FutureTask) r;

    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        WorkerExecutor.Task tee = (WorkerExecutor.Task) r;
        logger.info("thread end:"+System.currentTimeMillis()+" task id: "+tee.getTf().getUuid());
    }

}
