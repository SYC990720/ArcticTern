package com.mirrordust.telecomlocate.model;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.mirrordust.telecomlocate.entity.Barometric;
import com.mirrordust.telecomlocate.entity.BaseStation;
import com.mirrordust.telecomlocate.entity.Battery;
import com.mirrordust.telecomlocate.entity.Geomagnetism;
import com.mirrordust.telecomlocate.entity.LatLng;
import com.mirrordust.telecomlocate.entity.Sample;
import com.mirrordust.telecomlocate.entity.Signal;

import java.util.List;

import io.realm.RealmList;
import rx.Observable;
import rx.functions.Func4;
import rx.functions.Func6;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class SampleManager {
    private static final String TAG = "SampleManager";

    private LocationManager mLocationManager;
    private SignalManager mSignalManager;
    private BaseStationManager mBaseStationManager;
    private BatteryManager mBatteryManager;
    private GeomagneticManager mGeomagneticManager;
    private BarometricManager mBarometricManager;
    private Context mContext;

    public SampleManager(Context context) {
        mContext = context;

        mLocationManager = new LocationManager(mContext);
        mSignalManager = new SignalManager(mContext);
        mBaseStationManager = new BaseStationManager(mContext);
        mBatteryManager = new BatteryManager(mContext);
        mGeomagneticManager = new GeomagneticManager(mContext);
        mBarometricManager = new BarometricManager(mContext);
    }

    public Observable<Sample> fetchRecord() {
        return Observable.combineLatest(mSignalManager.observeOnce(),
                mLocationManager.getLocation(),
                mBaseStationManager.nerbyTower(),
                mBatteryManager.getBatteryLevel(),
                mGeomagneticManager.getGeomagneticInfo(),
                mBarometricManager.getBarometerPressure(),
                new Func6<Signal, Location, List<BaseStation>, Battery, Geomagnetism, Barometric,Sample>() {
                    @Override
                    public Sample call(Signal signalRecord, Location location, List<BaseStation> cellularTowers, Battery battery, Geomagnetism geomagnetic, Barometric barometric) {
                        Log.v(TAG, "start creating record...");
                        Sample record = new Sample();
                        if (location != null) {
                            Log.v(TAG, "Location user : " + location.getLatitude() + " : " + location.getLongitude());
                            LatLng latLng = new LatLng();
                            latLng.setLatitude(location.getLatitude());
                            latLng.setLongitude(location.getLongitude());
                            latLng.setAltitude(location.getAltitude());
                            latLng.setAccuracy(location.getAccuracy());
                            latLng.setSpeed(location.getSpeed());
                            record.setLatLng(latLng);
                        } else {
                            LatLng latLng = new LatLng();
                            latLng.setLatitude(0);
                            latLng.setLongitude(0);
                            latLng.setAltitude(0);
                            latLng.setAccuracy(0);
                            latLng.setSpeed(0);
                            record.setLatLng(latLng);
                        }
                        if (cellularTowers != null) {
                            RealmList<BaseStation> cellularTowerRealmList = new RealmList<>();
                            for (BaseStation tower : cellularTowers) {
                                cellularTowerRealmList.add(tower);
                            }
                            record.setBSList(cellularTowerRealmList);
                        }
                        record.setBtry(battery);
                        record.setSignal(signalRecord);
                        record.setMBS(mBaseStationManager.getConnectedTower());
                        record.setGm(geomagnetic);
                        record.setBaro(barometric);

                        return record;
                    }

                });
    }

    public Observable<Sample> fetchPredictRecord() {
        return Observable.combineLatest(mSignalManager.observeOnce(),
                mBaseStationManager.nerbyTower(),
                mBatteryManager.getBatteryLevel(),
                mGeomagneticManager.getGeomagneticInfo(),
                new Func4<Signal, List<BaseStation>, Battery, Geomagnetism, Sample>() {

                    @Override
                    public Sample call(Signal signalRecord, List<BaseStation> cellularTowers, Battery battery, Geomagnetism geomagnetic) {
                        Sample record = new Sample();
                        if (cellularTowers != null) {
                            RealmList<BaseStation> cellularTowerRealmList = new RealmList<>();
                            for (BaseStation tower : cellularTowers) {
                                cellularTowerRealmList.add(tower);
                            }
                            record.setBSList(cellularTowerRealmList);
                        }
                        record.setBtry(battery);
                        record.setSignal(signalRecord);
                        record.setMBS(mBaseStationManager.getConnectedTower());
                        record.setGm(geomagnetic);
                        return record;
                    }
                });
    }
}
