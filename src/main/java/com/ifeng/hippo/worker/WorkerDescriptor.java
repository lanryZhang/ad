/*
* WorkerDescriptor.java 
* Created on  202017/5/25 13:11 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.worker;

import com.ifeng.hippo.utils.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class WorkerDescriptor {
    private String hostIp;
    private int port;
    private String hostName;
    private int taskParallel;
    private int threadPoolSize;
    private String workerId;
    private long createTime;
    private int pid;
    private long lastUpdateTime;
    private int processTaskNum;
    private int totalPv;
    private String localIp;

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getProcessTaskNum() {
        return processTaskNum;
    }

    public void setProcessTaskNum(int processTaskNum) {
        this.processTaskNum = processTaskNum;
    }

    public int getTotalPv() {
        return totalPv;
    }

    public void setTotalPv(int totalPv) {
        this.totalPv = totalPv;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getTaskParallel() {
        return taskParallel;
    }

    public void setTaskParallel(int taskParallel) {
        this.taskParallel = taskParallel;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    @Override
    public String toString(){
        return new StringBuilder().append("workerId : ").append(workerId)
                .append(",hostIp :").append(hostIp).append(", taskParallel : ").append(taskParallel).append(", threadPoolSize : ").append(threadPoolSize)
                .append(", online time : ").append(DateUtil.parseDate(createTime)).toString();
    }
}
