package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.IEncode;
import com.ifeng.data.ILoader;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DeviceTagsInfo.java
 *
 * @author zhusy
 * @date 2018-3-2 11:16.
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 */
public class DeviceTagsInfo  implements IEncode, IDecode {
    /**
     * 设备号
     */
    private String deviceId;
    /**
     * 系统类型
     */
    private String os;
    /**
     * 品牌
     */
    private String brand;
    /**
     * 型号
     */
    private String model;
    /**
     * 城市
     */
    private String city;
    /**
     * 省份
     */
    private String province;
    /**
     * 省份ID
     */
    private int provinceId;
    /**
     * 标签列表
     */
    private List<Integer> tagList;
    /**
     * 时间戳
     */
    private long timestamp;

    private Date delkey;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public List<Integer> getTagList() {
        return tagList;
    }

    public void setTagList(List<Integer> tagList) {
        this.tagList = tagList;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDelkey() {
        return delkey;
    }

    public void setDelkey(Date delkey) {
        this.delkey = delkey;
    }

    @Override
    public void decode(ILoader loader) {
        this.deviceId = loader.getString("deviceId");
        this.os = loader.getString("os");
        this.brand = loader.getString("brand");
        this.model = loader.getString("model");
        this.city = loader.getString("city");
        this.province = loader.getString("province");
        this.provinceId = loader.getInt("provinceId");
        this.tagList = (ArrayList<Integer>) loader.getObject("tagList");
        this.timestamp = loader.getLong("timestamp");
        this.delkey = (Date) loader.getObject("delkey");
    }

    @Override
    public Document encode() {
        Document doc = new Document();
        doc.put("deviceId",this.deviceId);
        doc.put("os",this.os);
        doc.put("brand",this.brand);
        doc.put("model",this.model);
        doc.put("city",this.city);
        doc.put("province",this.province);
        doc.put("provinceId",this.provinceId);
        doc.put("tagList",this.tagList);
        doc.put("timestamp",this.timestamp);
        doc.put("delkey",this.delkey);
        return doc;
    }

    @Override
    public String toString() {
        return deviceId + "," + brand + "," + model + "," + StringUtils.join(tagList," ");
    }
}
