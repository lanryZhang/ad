/*
* TemplateData.java 
* Created on  202017/5/24 10:22 
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
public class TemplateData extends MongoCodec{
    private int time;
    private int num;

    private String platform;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public void decode(ILoader loader) {
        this.time = loader.getInt("time");
        this.num = loader.getInt("num");
        this.platform = loader.getString("platform");
    }
}
