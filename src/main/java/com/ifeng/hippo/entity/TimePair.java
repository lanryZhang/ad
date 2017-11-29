/*
* TimePair.java 
* Created on  202017/5/22 13:53 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TimePair {
    public TimePair(){}
    public TimePair(int bt,int et){
        this.beginTime = bt;
        this.endTime = et;
    }
    private int beginTime;
    private int endTime;

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}
