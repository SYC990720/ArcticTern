package com.mirrordust.telecomlocate.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.activity.SampleActivity;
import com.mirrordust.telecomlocate.entity.Sample;
import com.mirrordust.telecomlocate.model.SampleManager;
import com.mirrordust.telecomlocate.presenter.SamplePresenter;

import rx.functions.Action1;

public class SampleService extends Service {
    public static final String TAG = "SampleService";
    public static final int NOTIFICATION_ID = 9981;
    private final IBinder mBinder = new LocalBinder();
    private String mode;
    private SampleManager mSampleManager;
    private SamplePresenter mPresenter;
    private Handler mHandler = new Handler();
    private Runnable mDataCollection = new Runnable() {
        @Override
        public void run() {
            try {
                requestRecord();
            } finally {
                SharedPreferences sharedPref =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String stringValue = sharedPref.getString("sampling_interval",
                        getString(R.string.pref_default_sampling_interval));
                long samplingInterval = Long.parseLong(stringValue); // in seconds
                mHandler.postDelayed(mDataCollection, samplingInterval * 1000);
            }
        }
    };

    public SampleService() {
    }

    private void requestRecord() {
        mSampleManager.fetchRecord().subscribe(new Action1<Sample>() {
            @Override
            public void call(Sample sample) {
                sample.setIndex(0);
                sample.setMode(mode);
                mPresenter.addOrUpdateSample(sample);
            }
        });
    }

    public void startCollecting() {
        mDataCollection.run();
    }

    public void stopCollecting() {
        mHandler.removeCallbacks(mDataCollection);
    }

    public void setRecordManager(Context context) {
        mSampleManager = new SampleManager(context);
    }

    public boolean isRecordManagerInitialized() {
        return mSampleManager != null;
    }

    public void setSamplePresenter(SamplePresenter presenter) {
        mPresenter = presenter;
    }

    public boolean isSamplePresenterInitialized() {
        return mPresenter != null;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Intent nfIntent = new Intent(this, SampleActivity.class);
        nfIntent.setAction(Intent.ACTION_MAIN);
        nfIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        nfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, nfIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Sample telco-data")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public SampleService getService() {
            return SampleService.this;
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
