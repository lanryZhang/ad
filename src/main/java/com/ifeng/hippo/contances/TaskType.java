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
public enum TaskType {
    CLICK(0),
    EV(1);

    private int value;

    TaskType(int v){
        this.value = v;
    }
    public int getValue() {
        return value;
    }
}
