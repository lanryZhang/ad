package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.ILoader;

/**
 * ResourceBundle.java
 *
 * @author zhusy
 * @date 2018-3-7 16:24.
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 */
public class ResourceBundle implements IDecode {
    private int cpmId;
    private String cpmName;
    private String adName;
    private String adPositionName;
    private int adId;
    private int rate;

    public int getCpmId() {
        return cpmId;
    }

    public void setCpmId(int cpmId) {
        this.cpmId = cpmId;
    }

    public String getCpmName() {
        return cpmName;
    }

    public void setCpmName(String cpmName) {
        this.cpmName = cpmName;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public String getAdPositionName() {
        return adPositionName;
    }

    public void setAdPositionName(String adPositionName) {
        this.adPositionName = adPositionName;
    }

    public int getAdId() {
        return adId;
    }

    public void setAdId(int adId) {
        this.adId = adId;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    @Override
    public void decode(ILoader loader) {
        this.cpmId = loader.getInt("cpmId");
        this.cpmName = loader.getString("cpmName");
        this.adName = loader.getString("adName");
        this.adPositionName = loader.getString("adPositionName");
        this.adId = loader.getInt("adId");
        this.rate = loader.getInt("rate");
    }
}
