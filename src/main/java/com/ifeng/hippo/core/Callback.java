/*
* Callback.java 
* Created on  202017/6/5 13:56 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.core;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public interface Callback {

    public <T> Object execute(T... args);

}
