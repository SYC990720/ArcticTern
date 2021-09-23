package com.mirrordust.telecomlocate.app;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.mirrordust.telecomlocate.BuildConfig;
import com.mirrordust.telecomlocate.entity.DataSet;
import com.mirrordust.telecomlocate.model.DataHelper;

import net.gotev.uploadservice.UploadService;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by LiaoShanhe on 2017/07/06/006.
 */

public class TelecomLocate extends Application {
    public static final String TAG = "TelecomLocate";
    private long mTimeInterval; // in milliseconds

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(0)
                .initialData(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        DataSet dataSet = realm.createObject(DataSet.class);
                        dataSet.setIndex(0);
                        dataSet.setName("NewSamples");
                        dataSet.setExported(false);
                        dataSet.setUploaded(false);
                        dataSet.setExportedPath("");
                        dataSet.setDesc("unavailable,0,0");
                    }
                })
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

    }
}
