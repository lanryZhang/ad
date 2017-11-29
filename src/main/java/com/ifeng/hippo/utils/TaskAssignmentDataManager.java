/*
* ChannelHandlerManager.java 
* Created on  202017/5/26 15:26 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.utils;

import com.ifeng.hippo.entity.TaskFragment;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskAssignmentDataManager {
    private static ConcurrentHashMap<String,ChannelHandlerContext> workerMap = new ConcurrentHashMap();
    private static ConcurrentHashMap<String,BlockingQueue<List<TaskFragment>>> taskAssignDataMap = new ConcurrentHashMap();

    public static ConcurrentHashMap<String,ChannelHandlerContext> getChannelMap(){
        return workerMap;
    }

    public static ConcurrentHashMap<String,BlockingQueue<List<TaskFragment>>> getTaskAssignDataMap(){
        return taskAssignDataMap;
    }
}
