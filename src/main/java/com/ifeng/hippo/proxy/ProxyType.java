/*
* ProxyType.java 
* Created on  202017/6/7 14:35 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.proxy;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public enum ProxyType {
    HTTP(0),
    SOCKS5(1);

    private int value;
    ProxyType(int v){
        this.value = v;
    }
}
