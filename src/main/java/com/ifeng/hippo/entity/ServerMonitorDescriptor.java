/*
* ServerMonitorDescriptor.java 
* Created on  202017/6/5 17:01 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.ifeng.hippo.worker.WorkerDescriptor;

import java.util.List;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class ServerMonitorDescriptor {
    private String hostIp;
    private int port;
    private int workerNumber;
    private int taskParallel;
    private int blockingTaskNumber;
    private int discardTaskNumber;
    private long updateTime;
    private String taskParallelDesc;
    private long createTime;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getTaskParallelDesc() {
        return taskParallelDesc;
    }

    public void setTaskParallelDesc(String taskParallelDesc) {
        this.taskParallelDesc = taskParallelDesc;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkerNumber() {
        return workerNumber;
    }

    public void setWorkerNumber(int workerNumber) {
        this.workerNumber = workerNumber;
    }

    public int getTaskParallel() {
        return taskParallel;
    }

    public void setTaskParallel(int taskParallel) {
        this.taskParallel = taskParallel;
    }

    public int getBlockingTaskNumber() {
        return blockingTaskNumber;
    }

    public void setBlockingTaskNumber(int blockingTaskNumber) {
        this.blockingTaskNumber = blockingTaskNumber;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getDiscardTaskNumber() {
        return discardTaskNumber;
    }

    public void setDiscardTaskNumber(int discardTaskNumber) {
        this.discardTaskNumber = discardTaskNumber;
    }
}
