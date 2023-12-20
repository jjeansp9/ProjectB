package kr.jeet.edu.bus.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.common.IntentParams;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import kr.jeet.edu.bus.view.CustomAppbarLayout;

public class SettingsActivity extends BaseActivity {

    private final static String TAG = "SettingsActivity";

    private TextView mTvPrivacy, mTvService, mTvAppVersion, mTvAppVersionBadge, mTvLogout;

    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
        initAppbar();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_settings);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initData(){

    }

    @Override
    void initView() {
        initData();

        mTvAppVersionBadge = (TextView) findViewById(R.id.tv_app_version_update);
        mTvAppVersion = (TextView) findViewById(R.id.tv_app_version);
        mTvLogout = (TextView) findViewById(R.id.tv_logout);
        mTvPrivacy = (TextView) findViewById(R.id.tv_set_privacy);
        mTvService = (TextView) findViewById(R.id.tv_set_service);

        findViewById(R.id.layout_set_operation_policy).setOnClickListener(this);
        findViewById(R.id.layout_set_PI_use_consent).setOnClickListener(this);
        findViewById(R.id.layout_set_app_info).setOnClickListener(this);
        findViewById(R.id.layout_logout).setOnClickListener(this);

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;

            if (versionName != null) mTvAppVersion.setText("v"+versionName);
            else mTvAppVersionBadge.setVisibility(View.GONE);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {

            case R.id.layout_set_operation_policy:
                url = Constants.POLICY_SERVICE;
                startPvyActivity(mTvService.getText().toString());
                break;

            case R.id.layout_set_PI_use_consent:
                url = Constants.POLICY_PRIVACY;
                startPvyActivity(mTvPrivacy.getText().toString());
                break;

            case R.id.layout_set_app_info:
                break;

            case R.id.layout_logout:
                showMessageDialog(getString(R.string.settings_logout), getString(R.string.msg_confirm_logout), ok -> {
                    hideMessageDialog();

                    PreferenceUtil.setPhoneNumber(mContext, "");
                    PreferenceUtil.setAutoLogin(mContext, false);
                    PreferenceUtil.setPrefIsAuthorized(mContext, false);
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(mContext, R.string.msg_logout_success, Toast.LENGTH_SHORT).show();

                }, cancel -> hideMessageDialog());
                break;
        }
    }

    private void startPvyActivity(String title){
        Intent intent = new Intent(mContext, PrivacySeeContentActivity.class);
        intent.putExtra(IntentParams.PARAM_APPBAR_TITLE, title);
        intent.putExtra(IntentParams.PARAM_WEB_VIEW_URL, url);
        startActivity(intent);
    }
}