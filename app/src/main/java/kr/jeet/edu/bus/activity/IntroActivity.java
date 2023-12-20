package kr.jeet.edu.bus.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.dialog.AuthorizeDialog;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.response.BusInfoResponse;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.server.RetrofitClient;
import kr.jeet.edu.bus.utils.LogMgr;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroActivity extends BaseActivity {

    private static final String TAG = "introActivity";
    private final int HANDLER_CHECK_UPDATE = 1;  // 업데이트
    private final int HANDLER_START_INTRO = 2;  // intro 시작
    private final int HANDLER_AUTO_LOGIN = 3;  // 자동로그인
    private final int HANDLER_REQUEST_LOGIN = 4;       // 로그인 화면으로 이동

    private AppUpdateManager appUpdateManager = null;
    private final int REQUEST_INAPP_UPDATE = 1000;
    AuthorizeDialog _authDialog;
    View.OnClickListener okListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(_authDialog.checkValid()) {
                _authDialog.dismiss();
                PreferenceUtil.setPrefIsAuthorized(mContext, true);
                if(_authDialog.getCheckedAutoLogin() != PreferenceUtil.getAutoLogin(mContext)) {
                    PreferenceUtil.setAutoLogin(mContext, _authDialog.getCheckedAutoLogin());
                }
                startMain();
            }else{
                _authDialog.setError();
            }
        }
    };
    View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            _authDialog.dismiss();
            mHandler.sendEmptyMessage(HANDLER_REQUEST_LOGIN);
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case HANDLER_CHECK_UPDATE :
                    checkUpdate();
                    break;
                case HANDLER_START_INTRO:
                    startIntro();
                    break;
                case HANDLER_AUTO_LOGIN :
                    requestLogin();
                    break;

                case HANDLER_REQUEST_LOGIN :
                    startLogin();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        mContext = this;
        mHandler.sendEmptyMessage(HANDLER_CHECK_UPDATE);
//        startIntro();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(appUpdateManager == null) return;
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(
                new OnSuccessListener<AppUpdateInfo>() {
                    @Override
                    public void onSuccess(AppUpdateInfo appUpdateInfo) {
                        if (appUpdateInfo.updateAvailability()
                                == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            // 인 앱 업데이트가 이미 실행중이었다면 계속해서 진행하도록
                            try {
                                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, IntroActivity.this, REQUEST_INAPP_UPDATE);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void startIntro(){
        long delayTime = 1000;
        if (PreferenceUtil.getAutoLogin(mContext)) {
            // 자동 로그인
            mHandler.sendEmptyMessageDelayed(HANDLER_AUTO_LOGIN, delayTime);
        } else {
            mHandler.sendEmptyMessageDelayed(HANDLER_REQUEST_LOGIN, delayTime);
        }
    }

    @Override
    void initView() {

    }
    private void checkUpdate() {
        if(appUpdateManager == null) {
            try {
                appUpdateManager = AppUpdateManagerFactory.create(mContext);
                Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(updateInfo -> {
                    if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        try {
                            requestVersionUpdate(updateInfo);
                        } catch (Exception e) {
                            LogMgr.e(TAG, "FAIL REQUEST UPDATE\n" + Log.getStackTraceString(e));
                        }
                    }else{
                        mHandler.sendEmptyMessage(HANDLER_START_INTRO);
                    }
                });
                appUpdateInfoTask.addOnFailureListener(e -> {
                    LogMgr.e(TAG, "FAIL REQUEST APP_UPDATE_INFO\n" + Log.getStackTraceString(e));
                    mHandler.sendEmptyMessage(HANDLER_START_INTRO);
                });
            }catch(Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(HANDLER_START_INTRO);
            }
        }else{
            mHandler.sendEmptyMessage(HANDLER_START_INTRO);
        }
    }

    private void requestVersionUpdate(AppUpdateInfo appUpdateInfo) throws IntentSender.SendIntentException {
        if (appUpdateManager != null) {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo, AppUpdateType.IMMEDIATE, IntroActivity.this,
                    REQUEST_INAPP_UPDATE
            );
        }
    }
    @Override
    void initAppbar() {}

    private void startLogin(){
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

    private void requestLogin(){

        showProgressDialog();

        String phoneNum = PreferenceUtil.getPhoneNumber(mContext);

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusInfo(phoneNum).enqueue(new Callback<BusInfoResponse>() {
                @Override
                public void onResponse(Call<BusInfoResponse> call, Response<BusInfoResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            List<BusInfoData> getDataList = response.body().data;
                            if (getDataList != null && !getDataList.isEmpty()){

                                DataManager.getInstance().setbusInfoList(getDataList);
                                PreferenceUtil.setPhoneNumber(mContext, phoneNum);
                                checkAuthorize();
                            }
                        }
                    } else {
                        PreferenceUtil.setPhoneNumber(mContext, "");
                        PreferenceUtil.setPrefIsAuthorized(mContext, false);
                        startLogin();
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    LogMgr.e(TAG, "onFailure >> " + t.getMessage());
                    hideProgressDialog();
                    startLogin();
                }
            });
        }
    }

    private void startMain(){
        startActivity(new Intent(mContext, MainActivity.class));
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INAPP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.d("AppUpdate", "Update flow failed! Result code: " + resultCode); //
                Toast.makeText(mContext, R.string.msg_inappupdate_fail, Toast.LENGTH_SHORT).show();
                finishAffinity(); // 앱 종료
            }
        }
    }
    private void checkAuthorize() {
        if(PreferenceUtil.getIsAuthorized(mContext)) {
            startMain();
        }else{
            if(_authDialog != null && _authDialog.isShowing()) {
                _authDialog.dismiss();
            }
            _authDialog = new AuthorizeDialog(mContext);
            _authDialog.setTitle(getString(R.string.auth_request));
            _authDialog.setOnOkButtonClickListener(okListener);
            _authDialog.setOnCancelButtonClickListener(cancelListener);
            _authDialog.setEnabledOkButton(false);
            _authDialog.show();
            _authDialog.setPhoneNumber(PreferenceUtil.getPhoneNumber(mContext));
//            _authDialog.setPhoneNumber("01046332026");
        }
    }
}