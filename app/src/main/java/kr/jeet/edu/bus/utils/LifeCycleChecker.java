package kr.jeet.edu.bus.utils;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class LifeCycleChecker extends Application implements LifecycleEventObserver {

    private static final String TAG = "LifeCycleChecker";

    public boolean isForeground = false;

    // Handler를 사용하기 위한 상수 정의
    private static final int DELAY_ONE_HOUR = 3600000; // 1시간

    // Handler를 위한 변수
    private Handler handler;
    private AppCompatActivity activity;

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
            }
            isForeground = true;
            LogMgr.d(TAG, "앱 포그라운드로 전환");
        }
    }
}
