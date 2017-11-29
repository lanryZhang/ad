/*
* AdsFlowLogEntity.java 
* Created on  202017/11/14 16:14 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.ILoader;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class AdsFlowLogEntity implements IDecode {
    private int adid;
    private int ps;
    private String date;
    private int taskId;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getAdid() {
        return adid;
    }

    public void setAdid(int adid) {
        this.adid = adid;
    }

    public int getPs() {
        return ps;
    }

    public void setPs(int ps) {
        this.ps = ps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public void decode(ILoader loader) {
        this.setAdid(loader.getInt("adid"));
        this.setPs(loader.getInt("ps"));
        this.setDate(loader.getString("tm"));
        this.setTaskId(loader.getInt("taskid"));
    }

    @Override
    public String toString(){
        return new StringBuilder().append("taskId:").append(this.getTaskId()).append(" adId:").append(this.getAdid())
                .append(" ps:").append(this.getPs()).append(" date:").append(this.getDate()).toString();
    }
}
