/*
* AppDeviceInfo.java 
* Created on  202017/11/6 18:55 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.IEncode;
import com.ifeng.data.ILoader;
import org.bson.Document;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class AppDeviceInfo  implements IEncode, IDecode {
    private int id;
    /**
     * 系统: android,ios
     */
    private String os;

    /**
     * 提供客户端取到的客户网络信息，包括wifi,2g,3g,4g,unkown
     */
    private String network;

    /**
     * 0-Unkown,1-移动,2-联通,3-电信
     */
    private String carries;

    /**
     * MAC address
     */
    private String mac;

    /**
     * 安卓id，Android设备填写，ios为空
     */
    private String androidId;

    /**
     * ios设备填写，Android为空
     */
    private String idfa;

    /**
     * 设备IMEI
     */
    private String imei;

    /**
     * 设备品牌
     */
    private String brand;

    /**
     * 用户可见的设备名,例如Nexus One
     */
    private String model;

    /**
     * 内部设备名,例如Nexus One是passion
     */
    private String device;

    /**
     * 设备的像素密度dpi
     */
    private String screenDensity;


    @Override
    public void decode(ILoader loader) {
        this.setId(loader.getInt("id"));
        this.setOs(loader.getString("os"));
        this.setNetwork(loader.getString("network"));
        this.setCarries(loader.getString("carries"));
        this.setMac(loader.getString("mac"));
        this.setAndroidId(loader.getString("androidId"));
        this.setIdfa(loader.getString("idfa"));
        this.setImei(loader.getString("imei"));
        this.setBrand(loader.getString("brand"));
        this.setModel(loader.getString("model"));
        this.setDevice(loader.getString("device"));
        this.setScreenDensity(loader.getString("screenDensity"));
    }

    @Override
    public Document encode() {
        Document doc = new Document();
        doc.put("id",this.id);
        doc.put("os",this.os);
        doc.put("network",this.network);
        doc.put("carries",this.carries);
        doc.put("mac",this.mac);
        doc.put("androidId",this.androidId);
        doc.put("idfa",this.idfa);
        doc.put("imei",this.imei);
        doc.put("brand",this.brand);
        doc.put("model",this.model);
        doc.put("device",this.device);
        doc.put("screenDensity",this.screenDensity);
        return doc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCarries() {
        return carries;
    }

    public void setCarries(String carries) {
        this.carries = carries;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getScreenDensity() {
        return screenDensity;
    }

    public void setScreenDensity(String screenDensity) {
        this.screenDensity = screenDensity;
    }
}
