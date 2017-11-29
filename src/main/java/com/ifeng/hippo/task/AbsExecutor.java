/*
* AbsExecutor.java 
* Created on  202017/8/25 12:03 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task;

import com.ifeng.hippo.entity.TaskFragment;

import java.util.List;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public abstract class AbsExecutor implements IExecutor{
    @Override
    public void execute(List<TaskFragment> tfs) {

    }

    @Override
    public void execute(TaskFragment tf) {

    }
}
