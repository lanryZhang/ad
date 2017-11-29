/*
* IProcessor.java 
* Created on  202017/5/24 9:37 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.core;


import com.ifeng.configurable.Context;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public interface IFilter {
    Object filter(Context context);
}
