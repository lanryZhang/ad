/*
* WatchCallBack.java 
* Created on  202017/6/3 14:04 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.zookeeper;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public abstract class WatchCallBack {
    public  abstract Object callback(TreeCacheEvent event);
}
