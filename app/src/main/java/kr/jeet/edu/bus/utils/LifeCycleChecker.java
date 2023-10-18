package kr.jeet.edu.bus.utils;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public class LifeCycleChecker extends Application implements LifecycleEventObserver {

    private static final String TAG = "LifeCycleChecker";

    public boolean isForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "옵저버 생성");
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        Log.d(TAG, "targetState : " + event.getTargetState() + " event : " + event);
        if (event == Lifecycle.Event.ON_STOP) {
            isForeground = false;
            Log.d(TAG, "앱이 백그라운드로 전환");
        } else if (event == Lifecycle.Event.ON_START) {
            isForeground = true;
            Log.d(TAG, "앱이 포그라운드로 전환");
        }
    }
}
