/*
* TaskExecuteEntity.java 
* Created on  202017/12/11 18:22 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskExecuteEntity {
    private TaskFragment taskFragment;
    private String userAgent;
    private String cookie;

    public TaskFragment getTaskFragment() {
        return taskFragment;
    }

    public void setTaskFragment(TaskFragment taskFragment) {
        this.taskFragment = taskFragment;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
