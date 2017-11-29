/*
* TaskInfo.java 
* Created on  202017/5/22 13:32 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.IEncode;
import com.ifeng.data.ILoader;
import com.ifeng.hippo.contances.*;
import com.ifeng.hippo.task.TaskAction;
import com.ifeng.mongo.MongoDataLoader;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskInfo implements IEncode, IDecode{

    public TaskInfo(){
        actions = new ArrayList<>();
        remainPv = new AtomicInteger(0);
        finishedPv = new AtomicInteger(0);
        timePairs = new ArrayList<>();
        subTasks = new ArrayList<>();
        createTime = new Date();
        taskName = "";
        taskDescription = "";
        scriptPath = "";
    }

    private String referer;
    private int taskId;
    private int parentId;
    private DeviceInfo deviceInfo;

    /**
     * 任务执行平台
     * 0 手机 1 pc
     */
    private Platform platform;
    private List<TaskAction> actions;
    /**
     * 任务类型 曝光/点击
     */
    private TaskType taskType;
    /**
     * IP重复利用次数
     */
    private int ipReusedTimes;
    private String taskName;
    private String taskDescription;
    private String url;
    private int targetPv;
    private int pvToUvRatio;
    private Date beginDate;
    private Date endDate;
    private List<TimePair> timePairs;
    /**
     * 在线状态 0上线 1下线
     */
    private int status;
    private Date createTime;
    private long latestModifyTime;

    private List<TaskInfo> subTasks;
    /**
     * 当前剩余待刷的量
     */
    private AtomicInteger remainPv;
    /**
     * 已刷的量
     */
    private AtomicInteger finishedPv;
    /**
     * 等待超时时间
     */
    private int waitTimeout;
    private String scriptPath;
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

    /**
     * 请求类型 0 webkit 1 httpclient
     */
    private RequestType requestType;

    /**
     * 溢出量
     */
    private int overflow;

    /**
     * 溢出比率
     */
    private int overflowRate;
    private String groupId;

    private int forceWait;
    private List<Integer> provinces;
    private int forceArrive;

    private TaskSource taskSource;

    private String data;

    private String api;

    private String mainScriptPath;

    public String getMainScriptPath() {
        return mainScriptPath;
    }

    public void setMainScriptPath(String mainScriptPath) {
        this.mainScriptPath = mainScriptPath;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public TaskSource getTaskSource() {
        return taskSource;
    }

    public void setTaskSource(TaskSource taskSource) {
        this.taskSource = taskSource;
    }

    public int getForceArrive() {
        return forceArrive;
    }

    public void setForceArrive(int forceArrive) {
        this.forceArrive = forceArrive;
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

    public int getOverflowRate() {
        return overflowRate;
    }

    public void setOverflowRate(int overflowRate) {
        this.overflowRate = overflowRate;
    }

    public int getOverflow() {
        return overflow;
    }

    public void setOverflow(int overflow) {
        this.overflow = overflow;
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

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
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

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<TaskAction> getActions() {
        return actions;
    }

    public void setActions(List<TaskAction> actions) {
        this.actions = actions;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTargetPv() {
        return targetPv ;
    }

    public void setTargetPv(int targetPv) {
        this.targetPv = targetPv;
    }

    public int getPvToUvRatio() {
        return pvToUvRatio;
    }

    public void setPvToUvRatio(int pvToUvRatio) {
        this.pvToUvRatio = pvToUvRatio;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<TimePair> getTimePairs() {
        return timePairs;
    }

    public void setTimePairs(List<TimePair> timePairs) {
        this.timePairs = timePairs;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getLatestModifyTime() {
        return latestModifyTime;
    }

    public void setLatestModifyTime(long latestModifyTime) {
        this.latestModifyTime = latestModifyTime;
    }

    public List<TaskInfo> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<TaskInfo> subTasks) {
        this.subTasks = subTasks;
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

    public int getIpReusedTimes() {
        return ipReusedTimes;
    }

    public void setIpReusedTimes(int ipReusedTimes) {
        this.ipReusedTimes = ipReusedTimes;
    }

    public int getWaitTimeout() {
        return waitTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public void decode(ILoader loader) {
        this.setUrl(loader.getString("url"));
        this.setTargetPv(loader.getInt("targetPv"));
        this.setBeginDate(loader.getDate("beginDate"));
        this.setCreateTime(loader.getDate("createTime"));
        this.setEndDate(loader.getDate("endDate"));
        this.setFinishedPv(new AtomicInteger(loader.getInt("finishedPv")));
        this.setParentId(loader.getInt("parentId"));
        this.setPvToUvRatio(loader.getInt("pvToUvRatio"));
        this.setLatestModifyTime(loader.getLong("latestModifyTime"));
        this.setRemainPv(new AtomicInteger(loader.getInt("remainPv")));
        this.setStatus(loader.getInt("status"));
        this.setTaskName(loader.getString("taskName"));
        this.setTaskDescription(loader.getString("taskDescription"));
        this.setTaskId(loader.getInt("taskId"));

        ArrayList<String> ft = (ArrayList) loader.getObject("filtration");
        this.setFiltration(ft);

        ArrayList<Integer> provinces = (ArrayList) loader.getObject("provinces");
        this.setProvinces(provinces);

        List<TimePair> tps = new ArrayList<>();
        ArrayList<Document> list = (ArrayList) loader.getObject("timePairs");
        for (Document doc: list) {
            TimePair tp = new TimePair((int) Double.parseDouble(doc.get("beginTime").toString()),(int) Double.parseDouble(doc.get("endTime").toString()));
            tps.add(tp);
        }
        this.setTimePairs(tps);
        List<TaskInfo> subTasks = new ArrayList<>();
        ArrayList<Document> subTaskList = (ArrayList) loader.getObject("subTasks");
        for (Document doc: subTaskList) {
            TaskInfo ti = new TaskInfo();
            ti.decode(new MongoDataLoader(doc,false));
            subTasks.add(ti);
        }
        this.setSubTasks(subTasks);

        List<TaskAction> actions = new ArrayList<>();
        ArrayList<Document> docs = (ArrayList) loader.getObject("actions");
        for (Document doc: docs) {
            TaskAction ta = new TaskAction(ActionType.valueOf(doc.get("actionType").toString()),doc.get("actionData"));
            actions.add(ta);
        }

        this.setActions(actions);

        this.setIpReusedTimes(loader.getInt("ipReusedTimes"));
        this.setWaitTimeout(loader.getInt("waitTimeout"));
        this.setPlatform(Platform.valueOf(loader.getString("platform")));
        this.setTaskPosition(TaskPosition.valueOf(loader.getString("taskPosition")));
        this.setStatus(loader.getInt("status"));
        this.setTaskType(TaskType.valueOf(loader.getString("taskType")));
        this.setScriptPath(loader.getString("scriptPath"));
        this.setReferer(loader.getString("referer"));
        this.setFinishedFlag(loader.getString("finishedFlag"));
        this.setDeviceInfo(DeviceInfo.valueOf("".equals(loader.getString("deviceInfo"))?"DEFAULT":loader.getString("deviceInfo")));
        this.setRequestType(RequestType.valueOf("".equals(loader.getString("requestType"))?"WEBKIT":loader.getString("requestType")));
        this.setOverflow(loader.getInt("overflow"));
        this.setOverflowRate(loader.getInt("overflowRate"));
        this.setGroupId(loader.getString("creatorGroupId"));
        this.setForceWait(loader.getInt("forceWait"));
        this.setForceArrive(loader.getInt("forceArrive"));
        this.setTaskSource(TaskSource.valueOf("".equals(loader.getString("taskSource")) ? "IFENGAD" : loader.getString("taskSource").toUpperCase()));
        this.setApi(loader.getString("api"));
        this.setMainScriptPath(loader.getString("mainScriptPath"));
    }

    @Override
    public Document encode() {
        Document doc = new Document();
        doc.put("taskId",this.taskId);
        doc.put("parentId",this.parentId);
        List<Document> actionDocuments = new ArrayList<>();
        for (TaskAction action: this.actions) {
            Document d = new Document();
            d.put("actionType", action.getActionType().toString());
            d.put("actionData", action.getActionData());
            actionDocuments.add(d);
        }
        doc.put("actions",actionDocuments);
        doc.put("platform",this.platform.toString());
        doc.put("taskPosition",this.taskPosition.toString());
        doc.put("taskName",this.taskName);
        doc.put("taskDescription",this.taskDescription);
        doc.put("url",this.url);
        doc.put("targetPv",this.targetPv);
        doc.put("pvToUvRatio",this.pvToUvRatio);
        doc.put("beginDate",this.beginDate);
        doc.put("endDate",this.endDate);
        doc.put("filtration",this.filtration);

        List<Document> timePairs = new ArrayList();
        for (TimePair tp: this.timePairs) {
            Document d = new Document();
            d.put("beginTime",tp.getBeginTime());
            d.put("endTime",tp.getEndTime());
            timePairs.add(d);
        }

        doc.put("timePairs",timePairs);
        doc.put("createTime",this.createTime);
        doc.put("latestModityTime",this.latestModifyTime);
        List<Document> subTasks = new ArrayList<>();
        for (TaskInfo tf: this.subTasks) {
            subTasks.add(tf.encode());
        }
        doc.put("subTasks",subTasks);
        doc.put("remainPv",this.remainPv);
        doc.put("finishedPv",this.finishedPv);
        doc.put("taskId",this.taskId);
        doc.put("taskId",this.taskId);
        doc.put("taskId",this.taskId);
        doc.put("ipReusedTimes",this.ipReusedTimes);
        doc.put("waitTimeout",this.waitTimeout);
        doc.put("status",this.status);
        doc.put("taskType",this.taskType.toString());
        doc.put("referer",this.referer);
        doc.put("finishedFlag",this.finishedFlag);
        doc.put("deviceInfo",this.deviceInfo.toString());
        doc.put("requestType",this.requestType.toString());
        doc.put("overflow",this.overflow);
        doc.put("overflowRate",this.overflowRate);
        doc.put("creatorGroupId",this.groupId);
        doc.put("forceWait",this.forceWait);
        return doc;
    }
}
