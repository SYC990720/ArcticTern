package com.mirrordust.telecomlocate.model;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.mirrordust.telecomlocate.entity.Device;

/**
 * Created by LiaoShanhe on 2017/07/18/018.
 */

public class DeviceManager {

    private static final String TAG = "DeviceManager";

    private Context mContext;

    public DeviceManager(Context context) {
        mContext = context;
    }

    public Device information() {
        Device device = new Device();

        String serviceName = Context.TELEPHONY_SERVICE;
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(serviceName);

        device.setIMEI(telephonyManager.getDeviceId());
        device.setIMSI(telephonyManager.getSubscriberId());

        device.setOSVersion(System.getProperty("os.version"));
        device.setApiLevel(android.os.Build.VERSION.RELEASE);
        device.setModel(android.os.Build.MODEL);
        device.setProduct(android.os.Build.PRODUCT);
        device.setDevice(android.os.Build.DEVICE);

        return device;
    }
}
