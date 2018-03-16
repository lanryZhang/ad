package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.ILoader;

/**
 * LabelInfo.java
 *
 * @author zhusy
 * @date 2018-3-2 17:40.
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 */
public class LabelInfo  implements IDecode {
    private int id;
    private String labelName;
    private int cate;
    private String cateName;
    private String type;
    private int appProp;
    private int pcProp;
    private int wapProp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public int getCate() {
        return cate;
    }

    public void setCate(int cate) {
        this.cate = cate;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAppProp() {
        return appProp;
    }

    public void setAppProp(int appProp) {
        this.appProp = appProp;
    }

    public int getPcProp() {
        return pcProp;
    }

    public void setPcProp(int pcProp) {
        this.pcProp = pcProp;
    }

    public int getWapProp() {
        return wapProp;
    }

    public void setWapProp(int wapProp) {
        this.wapProp = wapProp;
    }

    @Override
    public void decode(ILoader loader) {
        this.id = loader.getInt("id");
        this.labelName = loader.getString("labelName");
        this.cate = loader.getInt("cate");
        this.cateName = loader.getString("cateName");
        this.type = loader.getString("type");
        this.appProp = loader.getInt("appProp");
        this.pcProp = loader.getInt("pcProp");
        this.wapProp = loader.getInt("wapProp");
    }
}
