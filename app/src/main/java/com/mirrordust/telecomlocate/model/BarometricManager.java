package com.mirrordust.telecomlocate.model;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.mirrordust.telecomlocate.entity.Barometric;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class BarometricManager {
    private static final String TAG = "BarometricManager";
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mPressure;
    private boolean hasPressure;

    private Subscriber<? super Barometric> mSubscriber;

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float millibarsOfPressure = event.values[0];
            measuring(millibarsOfPressure);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Do something here if sensor accuracy changes.
        }
    };

    public BarometricManager(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (mPressure == null) {
            Log.e(TAG, "没有气压计");
            hasPressure = false;
        } else {
            hasPressure = true;
        }
    }

    private void measuring(double pressure) {
        Barometric barometric = new Barometric();
        barometric.setPressure(pressure);

        mSubscriber.onNext(barometric);
        mSubscriber.onCompleted();
    }

    private void startListening() {
        if (hasPressure) {
            mSensorManager.registerListener(
                    mSensorEventListener,
                    mPressure,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            measuring(-999.999);
        }

    }

    private void stopListening() {
        if (hasPressure) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
    }

    public rx.Observable<Barometric> getBarometerPressure() {
        return observe().flatMap(new Func1<Barometric, Observable<Barometric>>() {
            @Override
            public Observable<Barometric> call(Barometric barometricRecord) {
                stopListening();
                return rx.Observable.just(barometricRecord);
            }
        });
    }

    public rx.Observable<Barometric> observe() {
        return rx.Observable.create(new rx.Observable.OnSubscribe<Barometric>() {

            @Override
            public void call(Subscriber<? super Barometric> subscriber) {
                mSubscriber = subscriber;
                startListening();
            }
        });
    }

}
