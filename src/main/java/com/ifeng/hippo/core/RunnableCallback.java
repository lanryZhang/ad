/*
* RunnableCallback.java 
* Created on  202017/6/5 13:56 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.core;

import com.ifeng.core.clean.CleanupAware;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class RunnableCallback implements Runnable, Callback, CleanupAware {

    @Override
    public <T> Object execute(T... args) {
        return null;
    }

    public void preRun() {

    }

    @Override
    public void run() {

    }

    public void postRun() {

    }

    public Exception error() {
        return null;
    }

    public Object getResult() {
        return null;
    }

    public void shutdown() {

    }

    public String getThreadName() {
        return null;
    }

    @Override
    public void cleanup() {

    }
}
