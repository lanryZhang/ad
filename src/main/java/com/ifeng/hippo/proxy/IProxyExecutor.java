/*
* IProxyExecutor.java 
* Created on  202017/8/14 10:55 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

import com.ifeng.configurable.Configurable;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public interface IProxyExecutor extends Runnable,Configurable{
    void execute();
}
