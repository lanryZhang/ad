/*
* PvToUvRatioCaculator.java 
* Created on  202017/5/31 13:01 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.task;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class PvToUvRatioCaculator {
    /**
     * PV UV转换计算
     * 需要转换成UV 返回true，否则返回false
     * @param percent
     * @return
     */
    public static boolean toUv(int percent){
        double r = Math.random() * 100;
        if (r <= percent){
            return true;
        }else{
            return false;
        }
    }
}
