/*
* IActionExecutor.java 
* Created on  202017/6/5 11:34 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task.actions;

import com.ifeng.hippo.task.TaskAction;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public interface IActionExecutor {
    void execute(TaskAction action);
}
