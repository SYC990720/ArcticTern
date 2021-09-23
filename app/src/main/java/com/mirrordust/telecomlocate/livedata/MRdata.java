package com.mirrordust.telecomlocate.livedata;

import android.arch.lifecycle.LiveData;
import com.mirrordust.telecomlocate.entity.*;
import io.realm.RealmList;

import java.util.UUID;

public class MRdata extends LiveData<MRdata> {
    private String mID;

    private long index;                     // used to distinguish which data set sample belongs to

    /* Basic attributes */

    // timestamp in milliseconds
    private long time;

    // location
    private LatLng latLng;

    // motion modes
    private String mode;

    /* Measurements */

    private RealmList<BaseStation> BSList;  // base stations list, get from #getAllCellInfo()

    private BaseStation MBS;                // connected base station

    private Signal signal;                  // mobile signal strength,
    // get from #PhoneStateListener.LISTEN_SIGNAL_STRENGTHS

    private Battery btry;                   // battery

    private Geomagnetism gm;                // geomagnetic measurements

    private Barometric baro;                // barometric pressure

    public MRdata() {
        mID = UUID.randomUUID().toString();
        time = System.currentTimeMillis();
    }

    // Getter and Setter for all fields
    public String getmID() {
        return mID;
    }

    public void setmID(String mID) {
        this.mID = mID;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public RealmList<BaseStation> getBSList() {
        return BSList;
    }

    public void setBSList(RealmList<BaseStation> BSList) {
        this.BSList = BSList;
    }

    public BaseStation getMBS() {
        return MBS;
    }

    public void setMBS(BaseStation MBS) {
        this.MBS = MBS;
    }

    public Signal getSignal() {
        return signal;
    }

    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    public Battery getBtry() {
        return btry;
    }

    public void setBtry(Battery btry) {
        this.btry = btry;
    }

    public Geomagnetism getGm() {
        return gm;
    }

    public void setGm(Geomagnetism gm) {
        this.gm = gm;
    }

    public Barometric getBaro() {
        return baro;
    }

    public void setBaro(Barometric baro) {
        this.baro = baro;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "mID='" + mID + '\'' +
                ", index=" + index +
                ", time=" + time +
                ", latLng=" + latLng +
                ", mode='" + mode + '\'' +
                ", BSList=" + BSList +
                ", MBS=" + MBS +
                ", signal=" + signal +
                ", btry=" + btry +
                ", gm=" + gm +
                ", baro=" + baro +
                '}';
    }
}
