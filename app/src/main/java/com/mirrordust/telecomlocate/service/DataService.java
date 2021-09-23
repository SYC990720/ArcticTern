package com.mirrordust.telecomlocate.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.entity.*;
import com.mirrordust.telecomlocate.interf.DataContract;
import com.mirrordust.telecomlocate.model.DataHelper;
import com.mirrordust.telecomlocate.model.DeviceManager;
import com.mirrordust.telecomlocate.pojo.UploadResponse;
import com.mirrordust.telecomlocate.presenter.DataPresenter;
import com.mirrordust.telecomlocate.util.AlarmManagerUtils;
import com.mirrordust.telecomlocate.util.Utils;
import io.realm.Realm;
import io.realm.RealmResults;
import net.gotev.uploadservice.*;

import java.io.*;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataService extends Service {
    private static final String TAG = "DataService";
    private static final String sep = ",";
    private long currentUploadedIndex;
    private LongSparseArray<String> mUploadIDs = new LongSparseArray<>();
    private boolean hasRun = false;
    private Handler mHandler = new Handler();
    private final IBinder mBinder = new DataService.LocalBinder();
    private DataPresenter mPresenter;
    private Realm mRealm; //for export
    private Realm uRealm; //for upload

    private UploadServiceBroadcastReceiver uploadServiceBroadcastReceiver =
            new UploadServiceBroadcastReceiver() {

                @Override
                public void onProgress(Context context, UploadInfo uploadInfo) {
                }

                @Override
                public void onError(Context context, UploadInfo uploadInfo,
                                    ServerResponse serverResponse, Exception exception) {
                    Toast.makeText(context, "upload failed!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                    if (serverResponse != null) {
                        UploadResponse response = new Gson()
                                .fromJson(serverResponse.getBodyAsString(), UploadResponse.class);
                        if (response.isSuccess()) {
                            changeDataSetUploadStatus(currentUploadedIndex, true);
                        }
                    }
                }

                @Override
                public void onCancelled(Context context, UploadInfo uploadInfo) {
                }
            };

    private Runnable mDataSave = new Runnable() {
        @Override
        public void run() {
            getTime();
            System.out.println("a save");
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String stringValue = sharedPref.getString("save_interval",
                    getString(R.string.pref_default_save_interval));
            long saveInterval = Long.parseLong(stringValue); // in seconds
            mHandler.postDelayed(mDataSave, saveInterval * 1000);
        }
    };

    private Runnable mDataExport = new Runnable() {
        @Override
        public void run() {
            getTime();
            System.out.println("an export");
            subscribe();
            exportAllDatasets();
            unsubscribe();
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String stringValue = sharedPref.getString("export_interval",
                    getString(R.string.pref_default_export_interval));
            long exportInterval = Long.parseLong(stringValue); // in seconds
            mHandler.postDelayed(mDataExport, exportInterval * 1000);
        }
    };

    private Runnable mDataUpload = new Runnable() {
        @Override
        public void run() {
            getTime();
            System.out.println("an upload");
            uRealm = Realm.getDefaultInstance();
            uploadAllDatasets();
            uRealm.close();
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String stringValue = sharedPref.getString("upload_interval",
                    getString(R.string.pref_default_upload_interval));
            long uploadInterval = Long.parseLong(stringValue); // in seconds
            mHandler.postDelayed(mDataUpload, uploadInterval * 1000);
        }
    };

    public DataService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uploadServiceBroadcastReceiver.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uploadServiceBroadcastReceiver.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("启动");
        if (!hasRun) {
            // new Thread(mDataSave).start();
            new Thread(mDataExport).start();
            new Thread(mDataUpload).start();
            // AlarmManagerUtils.getInstance(getApplicationContext()).getUpAlarmManagerWorkOnOthers();
            hasRun = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class LocalBinder extends Binder {
        public DataService getService() {
            return DataService.this;
        }
    }

    public void subscribe() {
        mRealm = Realm.getDefaultInstance();
    }

    public void unsubscribe() {
        mRealm.close();
    }

    public void startSaving() {
        mDataSave.run();
    }

    public void stopSaving() {
        mHandler.removeCallbacks(mDataSave);
    }

    public void startExporting() {
        mDataExport.run();
    }

    public void stopExporting() {
        mHandler.removeCallbacks(mDataExport);
    }

    public void startUploading() {
        mDataUpload.run();
    }

    public void stopUploading() {
        mHandler.removeCallbacks(mDataUpload);
    }

    public void exportDataSet(long index, String name, String desc) {
        if (!isExternalStorageWritable()) {
            return;
        }
        RealmResults<Sample> samples = DataHelper.getSamplesByIndex(mRealm, index);
        File dir1 = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "TelecomLocate");
        File dir = new File(dir1, "ExportedData");
        if (!dir.mkdirs()) {
            Log.e(TAG, "Directory not created or exist");
        }
        try {
            String filename = exportName(name, desc);
            File file = new File(dir, filename);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(TAG, "File not created");
                }
            }
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath(), true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // write file
            bufferedWriter.write(deviceInfo());
            bufferedWriter.write(MRHeader(samples.size()));
            for (Sample s : samples) {
                bufferedWriter.write(sampleMR(s));
            }

            bufferedWriter.close();
            fileWriter.close();
            changeDataSetExportStatus(mRealm, index, true);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Saving data error");
            changeDataSetExportStatus(mRealm, index, false);
        }
    }

    public void uploadDataSet(long index, String name, String desc) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sharedPref.getString("upload_url", getString(R.string.pref_default_upload_url));
        String fileName = exportName(name, desc);
        File dir1 = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "TelecomLocate");
        File dir = new File(dir1, "ExportedData");
        File file = new File(dir, fileName);
        String filePath = file.getAbsolutePath();
        currentUploadedIndex = index;
        String uploadID = uploadMultipart(this, url, filePath);
        if (uploadID != null)
            mUploadIDs.put(index, uploadID);
    }

    public void exportAllDatasets() {
        RealmResults<DataSet> dataSets = getDataSetsforExport();
        for (int i = 0; i < dataSets.size(); i++) {
            if (!dataSets.get(i).isExported()) {
                exportDataSet(dataSets.get(i).getIndex(), dataSets.get(i).getName(), dataSets.get(i).getDesc());
            }
        }
    }

    public void uploadAllDatasets() {
        RealmResults<DataSet> dataSets = getDataSetsforUpload();
        for (int i = 0; i < dataSets.size(); i++) {
            if ((!dataSets.get(i).isUploaded()) && (!dataSets.get(i).getName().equals("NewSamples"))) {
                uploadDataSet(dataSets.get(i).getIndex(), dataSets.get(i).getName(), dataSets.get(i).getDesc());
            }
        }
    }

    private String uploadMultipart(Context context, String url, String filePath) {
        String uploadID = null;
        try {
            uploadID = new MultipartUploadRequest(context, url)
                    .setUtf8Charset()
                    .addFileToUpload(filePath, "file")
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(3)
                    .startUpload();
        } catch (FileNotFoundException | MalformedURLException e) {
            Log.e("AndroidUploadService", e.getMessage(), e);
        }
        return uploadID;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private String exportName(DataSet dataSet) {
        return exportName(dataSet.getName(), dataSet.getDesc());
    }

    private String exportName(String name, String desc) {
        String suffix = Utils.dataSetDesc2FileSuffix(desc);
        return String.format("%s_%s.txt", name, suffix);
    }

    private String deviceInfo() {
        StringBuilder sb = new StringBuilder();
        Device device = new DeviceManager(this).information();
        sb.append("IMEI").append(":")
                .append(device.getIMEI() == null ? "null" : device.getIMEI()).append("\n");
        sb.append("IMSI").append(":")
                .append(device.getIMSI() == null ? "null" : device.getIMSI()).append("\n");
        sb.append("OSVersion").append(":")
                .append(device.getOSVersion()).append("\n");
        sb.append("apiLevel").append(":")
                .append(device.getApiLevel()).append("\n");
        sb.append("model").append(":")
                .append(device.getModel()).append("\n");
        sb.append("device").append(":")
                .append(device.getDevice()).append("\n");
        sb.append("product").append(":")
                .append(device.getProduct()).append("\n");
        return sb.toString();
    }

    private String MRHeader(int size) {
        return String.format("%s\n", size);
    }

    private String sampleMR(Sample s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.getTime()).append(sep)//1
                .append(s.getMode()).append(sep)//2
                .append(buildLatLongString(s.getLatLng())).append(sep)//3，4，5，6，7
                .append(buildSignalString(s.getSignal())).append(sep)//8，9，10，11，12
                .append(buildBatteryString(s.getBtry())).append(sep)//13，14
                .append(buildGeomagnetismString(s.getGm())).append(sep)//15，16，17，18，19，20
                .append(buildBarometricString(s.getBaro())).append(sep)//21
                .append(buildBaseStationString(s.getMBS())).append(sep)//22，23，24，25，26，27，28，29，30，31，32，33
                .append(s.getBSList().size());//34
        for (int i = 0; i < s.getBSList().size(); i++) {
            sb.append(sep).append(buildBaseStationString(s.getBSList().get(i)));//每次12个
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildLatLongString(LatLng latLng) {
        return String.valueOf(latLng.getLongitude()) + sep + latLng.getLatitude() + sep +
                latLng.getAltitude() + sep + latLng.getAccuracy() + sep +
                latLng.getSpeed();
    }

    private String buildSignalString(Signal signal) {
        return String.valueOf(signal.getDbm()) + sep + signal.isGsm() + sep +
                signal.getSignalToNoiseRatio() + sep +
                signal.getEvdoEcio() + sep + signal.getLevel();
    }

    private String buildBatteryString(Battery btry) {
        return String.valueOf(btry.getLevel()) + sep + btry.getCapacity();
    }

    private String buildGeomagnetismString(Geomagnetism gm) {
        return String.valueOf(gm.getX()) + sep + gm.getY() + sep +
                gm.getZ() + sep + gm.getAlpha() + sep +
                gm.getBeta() + sep + gm.getGamma();
    }

    private String buildBarometricString(Barometric baro) {
        return String.valueOf(baro.getPressure());
    }

    private String buildBaseStationString(BaseStation bs) {
        return String.valueOf(bs.getMcc()) + sep + bs.getMnc() + sep +
                bs.getLac() + sep + bs.getCid() + sep +
                bs.getArfcn() + sep + bs.getBsic_psc_pci() + sep +
                bs.getLon() + sep + bs.getLat() + sep +
                bs.getAsuLevel() + sep + bs.getSignalLevel() + sep +
                bs.getDbm() + sep + bs.getType();
    }

    public void changeDataSetExportStatus(long index, boolean done) {
        DataHelper.updateDataSetExport(mRealm, index, done);
    }

    public void changeDataSetExportStatus(Realm mRealm, long index, boolean done) {
        DataHelper.updateDataSetExport(mRealm, index, done);
    }

    public void changeDataSetUploadStatus(long index, boolean done) {
        Realm realm = Realm.getDefaultInstance();
        DataHelper.updateDataSetUpload(realm, index, done);
        realm.close();
    }

    public RealmResults<DataSet> getDataSetsforExport() {
        return DataHelper.getAllDataSet(mRealm);
    }

    public RealmResults<DataSet> getDataSetsforUpload() {
        return DataHelper.getAllDataSet(uRealm);
    }

    public void getTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        System.out.println(Thread.currentThread().getName());
    }
}
