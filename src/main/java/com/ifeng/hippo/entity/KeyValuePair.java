/*
* KeyValuePair.java 
* Created on  202017/9/6 16:00 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import java.io.Serializable;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class KeyValuePair<K,V> implements Serializable{
    private K k;
    private V v;

    public KeyValuePair(){}
    public KeyValuePair(K k ,V v){
        this.k = k;
        this.v = v;
    }

    public K getK() {
        return k;
    }

    public void setK(K k) {
        this.k = k;
    }

    public V getV() {
        return v;
    }

    public void setV(V v) {
        this.v = v;
    }
}
