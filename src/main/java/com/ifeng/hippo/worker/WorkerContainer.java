/*
* WorkerContainer.java 
* Created on  202017/5/25 13:03 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class WorkerContainer {
    private static Map<String, WorkerDescriptor> workerMap = new ConcurrentHashMap<>();

    public Map<String, WorkerDescriptor> getMap() {
        return workerMap;
    }
}
