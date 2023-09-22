package kr.jeet.edu.bus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.DataManager;
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

    private final int HANDLER_AUTO_LOGIN = 1;  // 자동로그인
    private final int HANDLER_REQUEST_LOGIN = 2;       // 로그인 화면으로 이동

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
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

        startIntro();
    }

    private void startIntro(){
        long delayTime = 2000;
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
                                startMain();
                            }
                        }
                    } else {
                        PreferenceUtil.setPhoneNumber(mContext, "");
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
}