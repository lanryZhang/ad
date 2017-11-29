/*
* ActionType.java 
* Created on  202017/5/23 15:44 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.contances;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public enum  ActionType {

    PAUSE(0);

    private int value;

    ActionType(int v){
        this.value = v;
    }

    public int getValue() {
        return value;
    }
}
