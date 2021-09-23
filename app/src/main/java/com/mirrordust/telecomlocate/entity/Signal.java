package com.mirrordust.telecomlocate.entity;

import io.realm.RealmObject;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class Signal extends RealmObject {
    private int dbm;
    private boolean isGsm;
    private int signalToNoiseRatio;
    private int evdoEcio;
    private int level;

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public boolean isGsm() {
        return isGsm;
    }

    public void setGsm(boolean gsm) {
        isGsm = gsm;
    }

    public int getSignalToNoiseRatio() {
        return signalToNoiseRatio;
    }

    public void setSignalToNoiseRatio(int signalToNoiseRatio) {
        this.signalToNoiseRatio = signalToNoiseRatio;
    }

    public int getEvdoEcio() {
        return evdoEcio;
    }

    public void setEvdoEcio(int evdoEcio) {
        this.evdoEcio = evdoEcio;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "dbm=" + dbm +
                ", isGsm=" + isGsm +
                ", signalToNoiseRatio=" + signalToNoiseRatio +
                ", evdoEcio=" + evdoEcio +
                ", level=" + level +
                '}';
    }
}
