package com.ifeng.hippo.entity;

import com.ifeng.data.ILoader;
import com.ifeng.mongo.MongoCodec;

/**
 * Created by zhanglr on 2016/8/25.
 */
public class UserAgentInfo extends MongoCodec {
    private long id;
    private String userAgent;
    /**
     * PC/移动
     */
    private String platform;
    private String browser;
    private String deviceInfo;

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


    @Override
    public void decode(ILoader loader) {
        this.id = loader.getInt("id");
        this.userAgent = loader.getString("userAgent");
        this.browser = loader.getString("browser");
        this.platform = loader.getString("platform");
        this.deviceInfo = loader.getString("deviceInfo");
    }
}
