/*
* TaskType.java 
* Created on  202017/6/7 11:32 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.contances;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public enum DeviceInfo {
    IPHONE(0),
    ANDROID(1),
    DEFAULT(2);
    private int value;

    DeviceInfo(int v){
        this.value = v;
    }
    public int getValue() {
        return value;
    }
}
