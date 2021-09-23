package com.mirrordust.telecomlocate.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mirrordust.telecomlocate.R;
import com.mirrordust.telecomlocate.util.C;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initView();
    }

    private void initView() {
        ScrollView scroll_about = (ScrollView) findViewById(R.id.scroll_about);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        scroll_about.startAnimation(animation);

        // version
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setStartOffset(600);

        TextView tvVersion = (TextView) findViewById(R.id.tv_about_version);
        tvVersion.setText(getVersionName());
        tvVersion.startAnimation(alphaAnimation);

        // check new version
        Button btn_checkNewVersion = (Button) findViewById(R.id.btn_check_new_version);
        btn_checkNewVersion.setOnClickListener(this);

        // github
        LinearLayout llGithub = (LinearLayout) findViewById(R.id.ll_github);
        llGithub.setOnClickListener(this);

        //share
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_share);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();

        switch (v.getId()) {
            case R.id.btn_check_new_version:
                // TODO: 2017/08/06/006 check new app version
                break;

            case R.id.ll_github:
                intent.setData(Uri.parse(C.GITHUB));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                break;

            case R.id.fab_share:
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, C.SHARE_CONTENT);
                intent.setType("text/plain");
                startActivity(intent);
                break;
        }
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

    public String getVersionName() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return getString(R.string.about_version_prefix) + " " + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
