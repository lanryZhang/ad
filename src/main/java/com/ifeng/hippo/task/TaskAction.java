/*
* TASK_TYPE.java 
* Created on  202017/5/23 14:30 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task;

import com.ifeng.hippo.contances.ActionType;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskAction {
    public TaskAction(){}
    public TaskAction(ActionType actionType,Object actionData){
        this.actionType = actionType;
        this.actionData = actionData;
    }
    private ActionType actionType;
    private Object actionData;

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Object getActionData() {
        return actionData;
    }

    public void setActionData(Object actionData) {
        this.actionData = actionData;
    }
}
