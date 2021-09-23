package com.mirrordust.telecomlocate.interf;

import android.content.Context;

import com.mirrordust.telecomlocate.entity.DataSet;

import io.realm.RealmResults;

/**
 * Created by LiaoShanhe on 2017/07/30/030.
 */

public interface DataContract {

    interface View extends BaseView<Presenter> {

        Presenter getPresenter();

        void checkExported(boolean exported, long index, String name, String desc);

        void checkUploaded(boolean exported, boolean uploaded,
                           long index, String name, String desc);

        void showConfirmDeleteDialog(long index, String name);

        void showExternalStorageNotWritable();

        void showExportSuccess();

        void showExportFail();

        void deleteDataSet();

        void updateDataSetStatus();

        String uploadUrl();
    }

    interface Presenter extends BasePresenter {

        RealmResults<DataSet> getDataSets();

        void exportDataSet(long index, String name, String desc);

        void uploadDataSet(long index, String name, String desc);

        void changeDataSetExportStatus(long index, boolean done);

        void changeDataSetUploadStatus(long index, boolean done);

        void deleteDataSet(long index);

        void checkDataSetStatus();

        void onResume(Context context);

        void onPause(Context context);

        void bindService(Context context);

        void unBindService(Context context);
    }

}
