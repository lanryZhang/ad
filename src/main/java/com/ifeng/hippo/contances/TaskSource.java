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
public enum TaskSource {
    IFENGAD(0),
    API(1),
    DMP(2),
    DOL(3),
    ACCURATEOPERATIONS(4);

    private int value;

    TaskSource(int v){
        this.value = v;
    }

    public int getValue() {
        return value;
    }

}
