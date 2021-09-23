package com.mirrordust.telecomlocate.broadcastreceiver;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.mirrordust.telecomlocate.pojo.UploadResponse;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

/**
 * Created by liaoshanhe on 2017/10/9.
 */

public class MyReceiver extends UploadServiceBroadcastReceiver {

    public static final String TAG = "MyReceiver";

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        Log.e(TAG, "onProgress");
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo,
                        ServerResponse serverResponse, Exception exception) {
        Log.e(TAG, "onError");
        if (serverResponse != null) {
            UploadResponse response = new Gson()
                    .fromJson(serverResponse.getBodyAsString(), UploadResponse.class);
            Log.e(TAG, response.isSuccess() + "," + response.getMessage());
        }
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        Log.e(TAG, "onCompleted");
        if (serverResponse != null) {
            UploadResponse response = new Gson()
                    .fromJson(serverResponse.getBodyAsString(), UploadResponse.class);
            Log.e(TAG, response.isSuccess() + "," + response.getMessage());
        }
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        Log.e(TAG, "onCancelled");
    }
}
