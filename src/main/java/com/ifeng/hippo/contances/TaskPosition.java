/*
* TaskPosition.java 
* Created on  202017/8/25 17:01 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.contances;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public enum TaskPosition {
    TOP(1),
    LIST(2),
    BOTTOM(3),
    DEFAULT(0);

    private int value;

    TaskPosition(int v){
        this.value = v;
    }

    public int getValue() {
        return value;
    }

}
