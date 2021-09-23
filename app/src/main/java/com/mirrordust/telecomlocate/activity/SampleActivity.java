package com.mirrordust.telecomlocate.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.adapter.SampleAdapter;
import com.mirrordust.telecomlocate.interf.SampleContract;
import com.mirrordust.telecomlocate.presenter.SamplePresenter;

public class SampleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SampleContract.View {

    private static final String TAG = "SampleActivity";

    private static final int REQUEST_PERMISSION_CODE = 123;

    // views
    private RecyclerView mRecyclerView;

    private SampleAdapter mAdapter;

    private TextView mTextView;

    private FloatingActionButton mFab;

    private PowerManager.WakeLock wakeLock;

    // presenter
    private SampleContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the presenter
        setPresenter(new SamplePresenter(this));
        mPresenter.subscribe();
        mPresenter.bindService(this);

        initMainView();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onFabClick();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "arctic:sampling");
        wakeLock.acquire();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkGPSStatus();
    }

    private void checkGPSStatus() {
        android.location.LocationManager locationManager =
                (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS is not enabled, open it now?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecyclerView.setAdapter(null);
        mPresenter.unBindService(this);
        mPresenter.unsubscribe();
        if (wakeLock != null && wakeLock.isHeld()){
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_sample, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            showSaveDataDialog();
        } else if (id == R.id.action_discard) {
            showConfirmDiscardDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle sample_detail_navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_test:
                // TODO: 2017/07/30/030 test activity
                Toast.makeText(this, "Not available now", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_data:
                Intent data_intent = new Intent(this, DataActivity.class);
                startActivity(data_intent);
                break;
            case R.id.nav_prediction:
                // TODO: 2017/07/30/030 prediction
                // Toast.makeText(this, "Not available now", Toast.LENGTH_SHORT).show();
                // TODO: 2021/07/21 add prediction
                Intent prediction_intent = new Intent(this, PredictionActivity.class);
                startActivity(prediction_intent);
                break;
            case R.id.nav_setting:
                Intent setting_intent = new Intent(this, SettingsActivity.class);
                startActivity(setting_intent);
                break;
            case R.id.nav_about:
                Intent about_intent = new Intent(this, AboutActivity.class);
                startActivity(about_intent);
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initMainView() {
        mTextView = (TextView) findViewById(R.id.placeholder_text_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.new_sample_rv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SampleAdapter(mPresenter.getNewSample());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setPresenter(@NonNull SampleContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
    }

    private boolean isPermissionGranted() {
        return !(
                ActivityCompat
                        .checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        ||
                        ActivityCompat
                                .checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.READ_PHONE_STATE)
                                != PackageManager.PERMISSION_GRANTED
                        ||
                        ActivityCompat
                                .checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED
        );
    }

    private void requestPermissions() {
        ActivityCompat
                .requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (!(grantResults.length == 3
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                showPermissionErrorToast();
            }
        }
    }

    private void showPermissionErrorToast() {
        Toast.makeText(this, "Error permissions", Toast.LENGTH_LONG).show();
    }

    @Override
    public void addSample() {
        mAdapter.addSample();
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    public void setFabIconSampling(boolean isSampling) {
        if (isSampling) {
            mFab.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.ic_fab_stop));
        } else {
            mFab.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.ic_fab_start));
        }
    }

    @Override
    public void checkPermission() {
        if (isPermissionGranted()) {
            showModeDialog();
        } else {
            requestPermissions();
        }
    }

    @Override
    public void showModeDialog() {
        final String[] modes = getResources().getStringArray(R.array.travel_mode);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Mode")
                .setItems(modes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.startSampling(modes[which], getApplicationContext());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public void showConfirmStopDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Stop recording?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.stopSampling();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void showControlPanel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        final View controlPanelView = inflater.inflate(R.layout.controlpanel, null);
        final RadioGroup radioGroup = (RadioGroup) controlPanelView.findViewById(R.id.motion_modes);
        final EditText customMode = (EditText) controlPanelView.findViewById(R.id.mode_custom);

        builder.setView(controlPanelView)
                .setPositiveButton("Record", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isPermissionGranted()) {
                            String theMode;
                            // start using selected or typed mode
                            int selectedID = radioGroup.getCheckedRadioButtonId();
                            RadioButton selectedButton = (RadioButton) controlPanelView.findViewById(selectedID);
                            String mode1 = String.valueOf(selectedButton.getText());

                            String mode2 = String.valueOf(customMode.getText());

                            if (!mode2.equals("")) {
                                theMode = mode2;
                            } else {
                                theMode = mode1;
                                if (selectedID == R.id.mode_other) {
                                    theMode = "not-set";
                                }
                            }

                            mPresenter.startSampling(theMode, getApplicationContext());
                        } else {
                            requestPermissions();
                        }
                    }
                })
                .setNeutralButton("Stop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.stopSampling();
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
    public void showConfirmDiscardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are your sure to delete all data?")
                .setTitle("warning")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.discardNewData();
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
    public void showSaveDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input)
                .setTitle("Save new samples")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        mPresenter.saveNewData(name);
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
    public void switchMainView(boolean hasNewSample) {
        if (hasNewSample) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setActivityTitle(String title) {
        setTitle(title);
    }

    // FIXME: 2017/07/29/029 delete unused method
    private int getScreenWidthDp() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    // FIXME: 2017/07/29/029 delete unused method
    private float convertPitoSp(float px) {
        return px / getResources().getDisplayMetrics().scaledDensity;
    }
}
