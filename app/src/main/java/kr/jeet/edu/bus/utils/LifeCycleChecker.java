package kr.jeet.edu.bus.utils;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.activity.MainActivity;
import kr.jeet.edu.bus.adapter.BusInfoListAdapter;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.response.BusInfoResponse;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.server.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LifeCycleChecker extends Application implements LifecycleEventObserver {

    private static final String TAG = "LifeCycleChecker";

    public boolean isForeground = false;

    // Handler를 사용하기 위한 상수 정의
    private static final int DELAY_ONE_MINUTE = 6000; // 10분
    private static final int DELAY_ONE_HOUR = 3600000; // 1시간

    // Handler를 위한 변수
    private Handler handler;
    private AppCompatActivity activity;
    private AlertDialog mProgressDialog = null;

    public LifeCycleChecker(AppCompatActivity activity) {this.activity = activity;}

    @Override
    public void onCreate() {
        super.onCreate();
        LogMgr.d(TAG, "옵저버 생성");
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        LogMgr.d(TAG, "targetState : " + event.getTargetState() + " event : " + event);
        if (event == Lifecycle.Event.ON_STOP) {
            isForeground = false;
            // 앱이 백그라운드로 전환
            if (handler == null) handler = new Handler();

//            handler.postDelayed(() -> {
//                // 1분 후에 실행되는 코드
//                if (!isForeground) {
//                    // 1분 동안 앱이 포그라운드로 전환되지 않았다면 isForeground를 false로 설정
//                    isForeground = false;
//                    LogMgr.d(TAG, "앱이 1분 동안 백그라운드에 있음.");
//                }
//            }, DELAY_ONE_MINUTE);

            // 10분 동안 백그라운드 상태인지 확인
            handler.postDelayed(() -> {
                if (!isForeground) {
                    // 10분 동안 백그라운드 상태라면 앱을 종료
                    LogMgr.d(TAG, "앱이 1시간 동안 백그라운드에 있음. 앱을 종료합니다");
                    activity.finishAffinity();
                    System.exit(0);
                }
            }, DELAY_ONE_HOUR);
        } else if (event == Lifecycle.Event.ON_START) {
            // 앱이 포그라운드로 전환
            if (handler != null) {
                handler.removeCallbacksAndMessages(null); // 모든 대기 중인 Handler 제거
                handler = null;
                requestBusInfo();
            }
            isForeground = true;
            LogMgr.d(TAG, "앱 포그라운드로 전환");
        }
    }

    private void requestBusInfo(){

        showProgressDialog();

        String phoneNum = PreferenceUtil.getPhoneNumber(activity);

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusInfo(phoneNum).enqueue(new Callback<BusInfoResponse>() {
                @Override
                public void onResponse(Call<BusInfoResponse> call, Response<BusInfoResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            List<BusInfoData> getDataList = response.body().data;
                            if (getDataList != null && !getDataList.isEmpty()){
                                DataManager.getInstance().setbusInfoList(getDataList);
                            }
                        }
                    }
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    LogMgr.e(TAG, "onFailure >> " + t.getMessage());
                    hideProgressDialog();
                }
            });
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null){
            View view = activity.getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
            TextView txt = view.findViewById(R.id.text);
            txt.setText(activity.getString(R.string.requesting));

            mProgressDialog = new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setView(view)
                    .create();
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }
}
