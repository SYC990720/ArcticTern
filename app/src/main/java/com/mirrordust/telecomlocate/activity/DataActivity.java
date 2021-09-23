package com.mirrordust.telecomlocate.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.adapter.DataAdapter;
import com.mirrordust.telecomlocate.interf.DataContract;
import com.mirrordust.telecomlocate.model.DeviceManager;
import com.mirrordust.telecomlocate.presenter.DataPresenter;
import com.mirrordust.telecomlocate.service.DataService;
import com.mirrordust.telecomlocate.util.AlarmManagerUtils;

import java.util.List;

public class DataActivity extends AppCompatActivity implements DataContract.View {

    public static final String TAG = "DataActivity";

    //view
    private RecyclerView mRecyclerView;

    private DataAdapter mAdapter;

    // presenter
    private DataContract.Presenter mPresenter;

    //alarm
    private AlarmManagerUtils alarmManagerUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setPresenter(new DataPresenter(this, new DeviceManager(this)));
        mPresenter.subscribe();
        mPresenter.bindService(this);

//        alarmManagerUtils = AlarmManagerUtils.getInstance(this);
//        alarmManagerUtils.createGetUpAlarmManager();
//        alarmManagerUtils.getUpAlarmManagerStartWork();
//        Toast.makeText(getApplicationContext(),"The timing is set successfully",Toast.LENGTH_SHORT).show();

        initMainView();

        Intent intent = new Intent(this, DataService.class);
        startService(intent);

    }

    private void initMainView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.data_set_rv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new DataAdapter(this, mPresenter.getDataSets());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setPresenter(@NonNull DataContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.checkDataSetStatus();
        mPresenter.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
        mPresenter.unBindService(this);
        mPresenter.unsubscribe();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public DataContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void checkExported(boolean exported, long index, String name, String desc) {
        if (exported) {
            Toast.makeText(this, "Already exported", Toast.LENGTH_SHORT).show();
        } else {
            mPresenter.exportDataSet(index, name, desc);
        }
    }

    @Override
    public void checkUploaded(boolean exported, boolean uploaded,
                              long index, String name, String desc) {
        if (!exported) {
            Toast.makeText(this, "Export file first", Toast.LENGTH_LONG).show();
            return;
        }
        if (uploaded) {
            Toast.makeText(this, "Already uploaded", Toast.LENGTH_SHORT).show();
        } else {
            mPresenter.uploadDataSet(index, name, desc);
        }
    }

    @Override
    public void showConfirmDeleteDialog(final long index, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format("Sure to delete data set [%s]?", name))
                .setTitle("warning")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.deleteDataSet(index);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void showExternalStorageNotWritable() {
        Toast.makeText(this, "export data failed, external storage not writable", Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void showExportSuccess() {
        Toast.makeText(this,
                "export data successfully, data in /Documents/TelecomLocate/ExportedData",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void showExportFail() {
        Toast.makeText(this, "export data failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void deleteDataSet() {
        mAdapter.onDataSetRemove();
    }

    @Override
    public void updateDataSetStatus() {
        mAdapter.onDataSetUpdate();
    }

    @Override
    public String uploadUrl() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString("upload_url", getString(R.string.pref_default_upload_url));
    }

    /**
     * 判断服务是否运行
     */
    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

}
