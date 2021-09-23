package com.mirrordust.telecomlocate.interf;

import android.content.Context;

import com.mirrordust.telecomlocate.entity.Sample;

import io.realm.RealmResults;

/**
 * Created by LiaoShanhe on 2017/07/27/027.
 */

public interface SampleContract {

    interface View extends BaseView<Presenter> {

        void showModeDialog();

        void showConfirmStopDialog();

        void showControlPanel();

        void showConfirmDiscardDialog();

        void showSaveDataDialog();

        void switchMainView(boolean hasNewSample);

        void setActivityTitle(String title);

        void addSample();

        void setFabIconSampling(boolean isSampling);

        void checkPermission();
    }

    interface Presenter extends BasePresenter {

        void startSampling(String mode, Context context);

        void stopSampling();

        void saveNewData(String dataSetName);

        void discardNewData();

        boolean hasNewSample();

        RealmResults<Sample> getNewSample();

        void onFabClick();

        void bindService(Context context);

        void unBindService(Context context);

        void onResume();

        void onPause();

        void addOrUpdateSample(Sample sample);
    }


}
