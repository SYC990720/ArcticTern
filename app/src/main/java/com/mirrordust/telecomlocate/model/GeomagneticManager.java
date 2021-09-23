package com.mirrordust.telecomlocate.model;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.mirrordust.telecomlocate.entity.Geomagnetism;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class GeomagneticManager {
    private static final String TAG = "GeomagneticManager";
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private Context mContext;
    private SensorManager mSensorManager;
    private boolean mAccelero_flag = false;
    private boolean mMagneto_flag = false;

    private Subscriber<? super Geomagnetism> mSubscriber;

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
                mAccelero_flag = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
                mMagneto_flag = true;
            }
            if (mAccelero_flag && mMagneto_flag) {
                updateOrientationAngles();
                mAccelero_flag = false;
                mMagneto_flag = false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public GeomagneticManager(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        measuring(mMagnetometerReading, mOrientationAngles);
    }

    private void measuring(float[] geomag, float[] orientation) {
        Geomagnetism geomagneticRecord = new Geomagnetism();
        geomagneticRecord.setX(geomag[0]);
        geomagneticRecord.setY(geomag[1]);
        geomagneticRecord.setZ(geomag[2]);
        // azimuth
        geomagneticRecord.setAlpha(orientation[0]);
        // pitch
        geomagneticRecord.setBeta(orientation[1]);
        // roll
        geomagneticRecord.setGamma(orientation[2]);
        mSubscriber.onNext(geomagneticRecord);
        mSubscriber.onCompleted();
    }

    private void startListening() {
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopListening() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    public rx.Observable<Geomagnetism> getGeomagneticInfo() {
        Log.v(TAG, "get geomagnetic information");
        return observe().flatMap(new Func1<Geomagnetism, Observable<Geomagnetism>>() {
            @Override
            public rx.Observable<Geomagnetism> call(Geomagnetism geomagneticRecord) {
                stopListening();
                return rx.Observable.just(geomagneticRecord);
            }
        });
    }

    public rx.Observable<Geomagnetism> observe() {
        return rx.Observable.create(new rx.Observable.OnSubscribe<Geomagnetism>() {
            @Override
            public void call(Subscriber<? super Geomagnetism> subscriber) {
                mSubscriber = subscriber;
                startListening();
            }
        });
    }
}
