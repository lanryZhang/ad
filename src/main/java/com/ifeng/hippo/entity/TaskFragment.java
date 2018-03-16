/*
* TaskFragment.java 
* Created on  202017/5/24 10:30 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.alibaba.fastjson.JSON;
import com.ifeng.hippo.contances.*;
import com.ifeng.hippo.task.TaskAction;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务分片
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskFragment {

    /**
     * 任务Id
     */
    private int taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 父节点Id
     */
    private int parentId;

    /**
     * 碎片ID
     */
    private int fragmentId;
    /**
     * 任务类型
     */
    private List<TaskAction> actions;
    /**
     * 目标PV量
     */
    private int targetPv;
    /**
     * 分片时间长度/秒
     */
    private int duration;
    /**
     * 执行该分片所用时间/秒
     */
    private int execDuration;
    /**
     * 当前剩余待刷的量
     */
    private AtomicInteger remainPv;
    /**
     * 已刷的量
     */
    private AtomicInteger finishedPv;
    /**
     * PV到UV对应比率
     */
    private int pvToUvRatio;
    /**
     * 子任务的分片
     */
    private List<TaskFragment> subFragments;
    /**
     * 目标地址
     */
    private String url;
    /**
     * IP重复利用次数
     */
    private int ipReusedTimes;

    /**
     * 等待超时时间
     */
    private int waitTimeout;

    /**
     * 任务类型
     * 0 手机 1 pc
     */
    private Platform platform;
    private TaskType taskType;
    private String scriptPath;
    private List<TimePair> timePairs;
    private String referer;
    private List<String> filtration;
    /**
     * 广告投放位置
     * 首页 TOP，频道首页 CHANNEL_TOP，列表页LIST，底页BOTTOM 默认DEFAULT
     */
    private TaskPosition taskPosition;

    /**
     * 页面加载成功标记
     */
    private String finishedFlag;

    private DeviceInfo deviceInfo;

    /**
     * 请求类型 0 webkit 1 httpclient
     */
    private RequestType requestType;

    private long beginTime;
    private String groupId;
    /**
     * 是否独享代理池
     */
    private int exclusiveProxy;
    /**
     * 指定代理，默认为“”
     */
    private List<String> appointProxyName;

    private int forceWait;

    private String mainScriptPath;

    private String behaviourData;

    /**
     * 行为数据互动比例
     */
    private int activeProportion;
    /**
     * 脚本互动比例
     */
    private int shellProportion;


    public int getActiveProportion() {
        return activeProportion;
    }

    public void setActiveProportion(int activeProportion) {
        this.activeProportion = activeProportion;
    }

    public int getShellProportion() {
        return shellProportion;
    }

    public void setShellProportion(int shellProportion) {
        this.shellProportion = shellProportion;
    }
    /**
     * 是否禁用图片
     */
    private int disableImg;

    public int getDisableImg() {
        return disableImg;
    }

    public void setDisableImg(int disableImg) {
        this.disableImg = disableImg;
    }

    /**
     * 可执行脚本
     */
    private String executeScript;

    /**
     * 代理信息
     */
    private String proxyStr;

    /**
     * 用户兴趣标签
     */
    private List<Integer> interest;

    /**
     * 用户信息标签
     */
    private List<Integer> userInfo;

    public List<Integer> getInterest() {
        return interest;
    }

    public void setInterest(List<Integer> interest) {
        this.interest = interest;
    }

    public List<Integer> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(List<Integer> userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 监测代码，API任务特有
     */
    private String detectionCode;

    public String getDetectionCode() {
        return detectionCode;
    }

    public void setDetectionCode(String detectionCode) {
        this.detectionCode = detectionCode;
    }

    public String getProxyStr() {
        return proxyStr;
    }

    public void setProxyStr(String proxyStr) {
        this.proxyStr = proxyStr;
    }

    public String getExecuteScript() {
        return executeScript;
    }

    public void setExecuteScript(String executeScript) {
        this.executeScript = executeScript;
    }
    public String getBehaviourData() {
        return behaviourData;
    }

    public void setBehaviourData(String behaviourData) {
        this.behaviourData = behaviourData;
    }


    public String getMainScriptPath() {
        return mainScriptPath;
    }

    public void setMainScriptPath(String mainScriptPath) {
        this.mainScriptPath = mainScriptPath;
    }

    private List<Integer> provinces;

    private int forceArrive;

    public int getForceArrive() {
        return forceArrive;
    }

    private TaskSource taskSource;

    public TaskSource getTaskSource() {
        return taskSource;
    }

    public void setTaskSource(TaskSource taskSource) {
        this.taskSource = taskSource;
    }

    public void setForceArrive(int forceArrive) {
        this.forceArrive = forceArrive;
    }

    private String api;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }


    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Integer> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<Integer> provinces) {
        this.provinces = provinces;
    }

    public int getForceWait() {
        return forceWait;
    }

    public void setForceWait(int forceWait) {
        this.forceWait = forceWait;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getFinishedFlag() {
        return finishedFlag;
    }

    public void setFinishedFlag(String finishedFlag) {
        this.finishedFlag = finishedFlag;
    }

    public TaskPosition getTaskPosition() {
        return taskPosition;
    }

    public void setTaskPosition(TaskPosition taskPosition) {
        this.taskPosition = taskPosition;
    }

    public List<String> getFiltration() {
        return filtration;
    }

    public void setFiltration(List<String> filtration) {
        this.filtration = filtration;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public List<TimePair> getTimePairs() {
        return timePairs;
    }

    public void setTimePairs(List<TimePair> timePairs) {
        this.timePairs = timePairs;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public int getIpReusedTimes() {
        return ipReusedTimes;
    }

    public void setIpReusedTimes(int ipReusedTimes) {
        this.ipReusedTimes = ipReusedTimes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFragmentId() {
        return fragmentId;
    }

    public void setFragmentId(int fragmentId) {
        this.fragmentId = fragmentId;
    }

    public List<TaskAction> getActions() {
        return actions;
    }

    public void setActions(List<TaskAction> actions) {
        this.actions = actions;
    }

    public AtomicInteger getRemainPv() {
        return remainPv;
    }

    public void setRemainPv(AtomicInteger remainPv) {
        this.remainPv = remainPv;
    }

    public AtomicInteger getFinishedPv() {
        return finishedPv;
    }

    public void setFinishedPv(AtomicInteger finishedPv) {
        this.finishedPv = finishedPv;
    }

    public int getPvToUvRatio() {
        return pvToUvRatio;
    }

    public void setPvToUvRatio(int pvToUvRatio) {
        this.pvToUvRatio = pvToUvRatio;
    }

    public List<TaskFragment> getSubFragments() {
        return subFragments;
    }

    public void setSubFragments(List<TaskFragment> subFragments) {
        this.subFragments = subFragments;
    }

    public int getTargetPv() {
        return targetPv;
    }

    public void setTargetPv(int targetPv) {
        this.targetPv = targetPv;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getExecDuration() {
        return execDuration;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setExecDuration(int execDuration) {
        this.execDuration = execDuration;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getExclusiveProxy() {
        return exclusiveProxy;
    }

    public void setExclusiveProxy(int exclusiveProxy) {
        this.exclusiveProxy = exclusiveProxy;
    }

    public List<String> getAppointProxyName() {
        return appointProxyName;
    }

    public void setAppointProxyName(List<String> appointProxyName) {
        this.appointProxyName = appointProxyName;
    }

    @Override
    public String toString(){
        return new StringBuilder().append("taskId=").append(this.taskId)
                .append(", url=").append(this.url)
                .append(",  finishedPv=").append(this.finishedPv)
                .append(",  targetPv=").append(this.targetPv).toString();
    }

    public String toJsonString(){
        return JSON.toJSONString(this);
    }
}
