/*
* QueueManager.java 
* Created on  202017/6/12 10:48 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.core.data;

import com.ifeng.hippo.entity.TaskFragment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class DataManager {
    private static BlockingQueue<TaskFragment> workerQueue = new LinkedBlockingQueue();
    private static BlockingDeque<TaskFragment> masterQueue = new LinkedBlockingDeque<>();

    private static Map<String,AtomicInteger> processCount =  new ConcurrentHashMap();

    public static Map<String, AtomicInteger> getProcessCount() {
        return processCount;
    }

    public static BlockingQueue<TaskFragment> getWorkerQueue(){
        return workerQueue;
    }

    public static BlockingQueue<TaskFragment> getMasterQueue(){
        return masterQueue;
    }
}

