/*
* PercentEntity.java 
* Created on  202017/6/7 15:39 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.ifeng.data.ILoader;
import com.ifeng.mongo.MongoCodec;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class PercentEntity extends MongoCodec{
    private String key;
    private int begin;
    private int end;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public void decode(ILoader loader) {
        this.key = loader.getString("key");
        this.begin = loader.getInt("begin");
        this.end = loader.getInt("end");
    }
}
