/*
* IExecutor.java 
* Created on  202017/5/26 13:57 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task;

import com.ifeng.hippo.core.WebDriverPool;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.worker.WorkerDescriptor;

import java.util.List;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public interface IExecutor {
    void execute(TaskFragment tf);
    void execute(List<TaskFragment> tfs);
}
