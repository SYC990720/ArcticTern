package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class BaseStation extends RealmObject {

    private int mcc;            // Mobile Country Code

    private int mnc;            // Mobile Network Code

    private int lac;            // Location Area Code or TAC(Tracking Area Code) for LTE

    private int cid;            // Cell Identity

    private int arfcn;          // Absolute RF Channel Number (or UMTS Absolute RF Channel Number for WCDMA)

    private int bsic_psc_pci;   /* bsic for GSM, psc for WCDMA, pci for LTE,
                                   GSM has #getPsc() but always get Integer.MAX_VALUE,
                                   psc is undefined for GSM */

    private double lon;         // Base station longitude

    private double lat;         // Base station latitude

    private int asuLevel;       /* Signal level as an asu value, asu is calculated based on 3GPP RSRP
                                   for GSM, between 0..31, 99 is unknown
                                   for WCDMA, between 0..31, 99 is unknown
                                   for LTE, between 0..97, 99 is unknown
                                   for CDMA, between 0..97, 99 is unknown */

    private int signalLevel;    // Signal level as an int from 0..4

    private int dbm;            // Signal strength as dBm

    private String type;        // Signal type, GSM or WCDMA or LTE or CDMA

    // Getter and Setter for all fields
    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getArfcn() {
        return arfcn;
    }

    public void setArfcn(int arfcn) {
        this.arfcn = arfcn;
    }

    public int getBsic_psc_pci() {
        return bsic_psc_pci;
    }

    public void setBsic_psc_pci(int bsic_psc_pci) {
        this.bsic_psc_pci = bsic_psc_pci;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getAsuLevel() {
        return asuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        this.asuLevel = asuLevel;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "BaseStation{" +
                "mcc=" + mcc +
                ", mnc=" + mnc +
                ", lac=" + lac +
                ", cid=" + cid +
                ", arfcn=" + arfcn +
                ", bsic_psc_pci=" + bsic_psc_pci +
                ", lon=" + lon +
                ", lat=" + lat +
                ", asuLevel=" + asuLevel +
                ", signalLevel=" + signalLevel +
                ", dbm=" + dbm +
                ", type='" + type + '\'' +
                '}';
    }
}
