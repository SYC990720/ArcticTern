package com.mirrordust.telecomlocate.model;

import android.content.Context;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.mirrordust.telecomlocate.entity.BaseStation;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.functions.FuncN;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class BaseStationManager {
    private static final String TAG = "BaseStationManager";

    private TelephonyManager mTelephonyManager;
    private Context mContext;

    public BaseStationManager(Context context) {
        mContext = context;
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private BaseStation checkInvalidData(BaseStation cellularTower) {
        if (cellularTower == null) {
            return null;
        }
        if (cellularTower.getMcc() != Integer.MAX_VALUE &&
                cellularTower.getMnc() != Integer.MAX_VALUE &&
                cellularTower.getCid() != Integer.MAX_VALUE &&
                cellularTower.getLac() != Integer.MAX_VALUE) {
            return cellularTower;
        }
        return null;
    }

    private BaseStation bindData(CellInfo cellInfo) {
        BaseStation baseStation = null;
        Log.v(TAG, cellInfo.toString());
        Log.v(TAG, "" + Integer.MAX_VALUE);

        if (cellInfo instanceof CellInfoWcdma) {
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("WCDMA");
            baseStation.setCid(cellIdentityWcdma.getCid());
            baseStation.setLac(cellIdentityWcdma.getLac());
            baseStation.setMcc(cellIdentityWcdma.getMcc());
            baseStation.setMnc(cellIdentityWcdma.getMnc());
            baseStation.setBsic_psc_pci(cellIdentityWcdma.getPsc());
            if (cellInfoWcdma.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoWcdma.getCellSignalStrength().getAsuLevel()); //Get the signal level as an asu value between 0..31, 99 is unknown Asu is calculated based on 3GPP RSRP.
                baseStation.setSignalLevel(cellInfoWcdma.getCellSignalStrength().getLevel()); //Get signal level as an int from 0..4
                baseStation.setDbm(cellInfoWcdma.getCellSignalStrength().getDbm()); //Get the signal strength as dBm
            }
        } else if (cellInfo instanceof CellInfoLte) {
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("LTE");
            baseStation.setCid(cellIdentityLte.getCi());
            baseStation.setMnc(cellIdentityLte.getMnc());
            baseStation.setMcc(cellIdentityLte.getMcc());
            baseStation.setLac(cellIdentityLte.getTac());
            baseStation.setBsic_psc_pci(cellIdentityLte.getPci());
            if (cellInfoLte.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoLte.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoLte.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoLte.getCellSignalStrength().getDbm());
            }
        } else if (cellInfo instanceof CellInfoGsm) {
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
            baseStation = new BaseStation();
            baseStation.setType("GSM");
            baseStation.setCid(cellIdentityGsm.getCid());
            baseStation.setLac(cellIdentityGsm.getLac());
            baseStation.setMcc(cellIdentityGsm.getMcc());
            baseStation.setMnc(cellIdentityGsm.getMnc());
            baseStation.setBsic_psc_pci(cellIdentityGsm.getPsc());
            if (cellInfoGsm.getCellSignalStrength() != null) {
                baseStation.setAsuLevel(cellInfoGsm.getCellSignalStrength().getAsuLevel());
                baseStation.setSignalLevel(cellInfoGsm.getCellSignalStrength().getLevel());
                baseStation.setDbm(cellInfoGsm.getCellSignalStrength().getDbm());
            }
        } else {
            Log.e(TAG, "CDMA CellInfo................................................");
        }
        return baseStation;
    }

    public BaseStation getConnectedTower() {
        String operator = mTelephonyManager.getNetworkOperator();
        int mcc, mnc;
        try {
            mcc = Integer.parseInt(operator.substring(0, 3));
        } catch (Exception e) {
            mcc = -2;
        }
        try {
            mnc = Integer.parseInt(operator.substring(3));
        } catch (Exception e) {
            mnc = -2;
        }
        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        BaseStation tower = new BaseStation();
        tower.setMcc(mcc);
        tower.setMnc(mnc);

        if (cellLocation instanceof GsmCellLocation) {
            tower.setBsic_psc_pci(((GsmCellLocation) cellLocation).getPsc());
            tower.setCid(((GsmCellLocation) cellLocation).getCid());
            tower.setLac(((GsmCellLocation) cellLocation).getLac());
            tower.setType("");
            Log.v(TAG, "Get connected tower GSM");
            Log.v(TAG, cellLocation.toString());
        }
        return tower;
    }

    /**
     * Get information of all cells that can be listened by the phone.
     * Call getAllCellInfo() and getNeighboringCellInfo() respectively,
     * since one or two of these api may return null.
     *
     * @return List of BaseStation, BaseStation is a class contains cell info
     */
    public List<BaseStation> getTowerList() {
        List<BaseStation> cellularTowerList = new ArrayList<>();

        // Firstly try getAllCellInfo() api
        List<CellInfo> cellInfoList = null;
        cellInfoList = mTelephonyManager.getAllCellInfo();
        // then the
        List<NeighboringCellInfo> neighboringCellInfoList = null;
        neighboringCellInfoList = mTelephonyManager.getNeighboringCellInfo();

        /*
        * decide which list will be used:
        * flag = 0, use cellInfoList,
        * flag = 1, use neighboringCellInfoList,
        * flag = -1, use getCellLocation.
        * */
        int flag = 0;
        if (cellInfoList == null && neighboringCellInfoList == null) {
            flag = -1;
        } else if (cellInfoList == null) {
            flag = 1;
        } else {
            flag = 0;
        }

        Log.v(TAG, "flag = " + flag);

        switch (flag) {
            case 0: {
                for (int i = 0; i < cellInfoList.size(); i++) {
                    final CellInfo cellInfo = cellInfoList.get(i);
                    final BaseStation cellularTower = bindData(cellInfo);
                    if (cellularTower != null) {
                        cellularTowerList.add(cellularTower);
                    }
                }
                return cellularTowerList;
            }
            case 1: {
                for (int i = 0; i < neighboringCellInfoList.size(); i++) {
                    final NeighboringCellInfo ninfo = neighboringCellInfoList.get(i);
                    final BaseStation cellularTower = new BaseStation();
                    if (ninfo.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS ||
                            ninfo.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
                        cellularTower.setType("GSM");
                    } else {
                        cellularTower.setType("");
                    }
                    String operator = mTelephonyManager.getNetworkOperator();
                    int mcc, mnc;
                    try {
                        mcc = Integer.parseInt(operator.substring(0, 3));
                    } catch (Exception e) {
                        mcc = -2;
                    }
                    try {
                        mnc = Integer.parseInt(operator.substring(3));
                    } catch (Exception e) {
                        mnc = -2;
                    }
                    cellularTower.setMcc(mcc);
                    cellularTower.setMnc(mnc);
                    cellularTower.setLac(ninfo.getLac());
                    cellularTower.setCid(ninfo.getCid());
                    cellularTower.setBsic_psc_pci(ninfo.getPsc());
                    cellularTower.setAsuLevel(ninfo.getRssi());
                    cellularTower.setDbm(-113 + 2 * ninfo.getRssi());
                    cellularTowerList.add(cellularTower);
                }
                return cellularTowerList;
            }
            case -1: {
                BaseStation cellularTower = getConnectedTower();
                if (cellularTower != null) {
                    cellularTowerList.add(cellularTower);
                }
                return cellularTowerList;
            }
            default: {
                Log.v(TAG, "getAllCellInfo() & getNeighboringCellInfo() both return null");
                return cellularTowerList;
            }
        }
    }

    private rx.Observable<BaseStation> locationTower(final BaseStation cellularTower) {
        return rx.Observable.create(new Observable.OnSubscribe<BaseStation>() {
            @Override
            public void call(final Subscriber<? super BaseStation> subscriber) {
                subscriber.onNext(cellularTower);
                subscriber.onCompleted();
                /*ServiceGenerator.changeApiBaseUrl("http://opencellid.org");
                final CellIdClient cellIdClient = ServiceGenerator.createService(CellIdClient.class);
                Call<CellIdResponse> cellIdResponseCall = cellIdClient.cellInformations("1085e718-c5a6-4392-9062-e57527c7bd97",
                        cellularTower.getMcc(),
                        cellularTower.getMnc(),
                        cellularTower.getLac(),
                        cellularTower.getCid(),
                        "json");

                cellIdResponseCall.enqueue(new Callback<CellIdResponse>() {
                    @Override
                    public void onResponse(Call<CellIdResponse> call, Response<CellIdResponse> response) {
                        Log.v(TAG, "request : " + call.request().toString());
                        Log.v(TAG, "response request : " + response.body());
                        Log.v(TAG, "response errorBody : " + response.errorBody());

                        CellIdResponse cellIdResponse = response.body();

                        if (cellIdResponse != null) {
                            cellularTower.setLat(cellIdResponse.getLat());
                            cellularTower.setLon(cellIdResponse.getLon());
                        }

                        subscriber.onNext(cellularTower);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(Call<CellIdResponse> call, Throwable t) {
                        Log.e(TAG, call.request().toString());
                        Log.e(TAG, t.toString());
//                        subscriber.onError(t);
                        subscriber.onNext(cellularTower);
                        subscriber.onCompleted();
                    }
                });*/
            }
        });
    }

    public rx.Observable<List<BaseStation>> nerbyTower() {
        return rx.Observable.create(new Observable.OnSubscribe<List<BaseStation>>() {
            @Override
            public void call(final Subscriber<? super List<BaseStation>> subscriber) {
                final List<BaseStation> cellularTowerList = getTowerList();
                List<Observable<BaseStation>> observableList = new ArrayList<>();

                if (cellularTowerList.size() == 0) {
                    subscriber.onNext(cellularTowerList);
                    subscriber.onCompleted();
                }

                for (BaseStation tower : cellularTowerList) {
                    Observable<BaseStation> observable = locationTower(tower);
                    observableList.add(observable);
                }

                rx.Observable.zip(observableList, new FuncN<List<BaseStation>>() {
                    @Override
                    public List<BaseStation> call(Object... args) {
                        List<BaseStation> listResponse = new ArrayList<>();

                        for (int i = 0; i < args.length; i++) {
                            listResponse.add((BaseStation) args[0]);
                        }
                        Log.v(TAG, "Request finished length data : " + args.length);
                        Log.v(TAG, "Get list response : " + listResponse.size());
                        return listResponse;
                    }
                }).subscribe(new Subscriber<List<BaseStation>>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "Completed all services API");
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "get error : " + e.toString());
                    }

                    @Override
                    public void onNext(List<BaseStation> towerList) {
                        Realm realm = Realm.getDefaultInstance();

                        realm.beginTransaction();
                        realm.copyToRealm(towerList);
                        realm.commitTransaction();

                        Log.v(TAG, "Get list response cell Id : " + towerList.size());
                        subscriber.onNext(cellularTowerList);
                    }
                });
            }
        });
    }
}
