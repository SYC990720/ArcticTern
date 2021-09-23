package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class Device extends RealmObject {
    private String IMEI;        // International Mobile Equipment Identity
    private String IMSI;        // International Mobile Subscriber Identity
    private String OSVersion;
    private String apiLevel;
    private String model;
    private String device;
    private String product;

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "Device{" +
                "IMEI='" + IMEI + '\'' +
                ", IMSI='" + IMSI + '\'' +
                ", OSVersion='" + OSVersion + '\'' +
                ", apiLevel='" + apiLevel + '\'' +
                ", model='" + model + '\'' +
                ", device='" + device + '\'' +
                ", product='" + product + '\'' +
                '}';
    }
}
