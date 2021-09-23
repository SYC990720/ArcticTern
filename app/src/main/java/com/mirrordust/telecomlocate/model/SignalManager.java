package com.mirrordust.telecomlocate.model;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.mirrordust.telecomlocate.entity.Signal;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class SignalManager extends PhoneStateListener {
    private static final String TAG = "SignalObserver";

    private Context mContext;
    private TelephonyManager mTelephonyManager;

    private Subscriber<? super Signal> mSubscriber;

    public SignalManager(Context context) {
        mContext = context;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);
//        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void signalMeasuring(SignalStrength signalStrength) {
        String ssignal = signalStrength.toString();
        Log.e(TAG, ssignal);
        String[] parts = ssignal.split(" ");

        Log.v(TAG, ssignal);

        int dB = -120;
        if (mTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
            // For Lte SignalStrength: dbm = ASU - 140.
            //dB = Integer.parseInt(parts[8]) - 140;
            dB = signalStrength.getGsmSignalStrength() - 140;
            /*int ltesignal = Integer.parseInt(parts[9]);
            if (ltesignal < -2) {
                dB = ltesignal;
            }*/
        } else {
            if (signalStrength.getGsmSignalStrength() != 99) {
                // For GSM Signal Strength: dbm =  (2*ASU)-113.
                int strengthInteger = -113 + 2 * signalStrength.getGsmSignalStrength();
                dB = strengthInteger;
                Log.e(TAG, "getEvdoDbm: " + signalStrength.getEvdoDbm());
                Log.e(TAG, "getCdmaDbm: " + signalStrength.getCdmaDbm());
            }
        }
        Log.e(TAG, "dB: " + dB);

        Signal signalRecord = new Signal();
        signalRecord.setDbm(dB);
        signalRecord.setGsm(signalStrength.isGsm());
        signalRecord.setSignalToNoiseRatio(signalStrength.getEvdoSnr()); //Get the signal to noise ratio.
        signalRecord.setEvdoEcio(signalStrength.getEvdoEcio());  //Get the EVDO Ec/Io value in dB*10
        signalRecord.setLevel(signalStrength.getLevel());   //Retrieve an abstract level value for the overall signal strength.

        mSubscriber.onNext(signalRecord);
        mSubscriber.onCompleted();
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        Log.e(TAG, "信号强度变化");
        signalMeasuring(signalStrength);
    }

    private void startListening() {
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void stopListening() {
        mTelephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
    }

    public rx.Observable<Signal> observeOnce() {
        return observe().flatMap(new Func1<Signal, Observable<Signal>>() {
            @Override
            public rx.Observable<Signal> call(Signal signalRecord) {
                stopListening();
                return rx.Observable.just(signalRecord);
            }
        });
    }

    public rx.Observable<Signal> observe() {
        return rx.Observable.create(new rx.Observable.OnSubscribe<Signal>() {
            @Override
            public void call(Subscriber<? super Signal> subscriber) {
                mSubscriber = subscriber;
                startListening();
            }
        });
    }
}
