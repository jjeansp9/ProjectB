package kr.jeet.edu.bus.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.activity.MainActivity;
import kr.jeet.edu.bus.utils.LogMgr;

//public class MyAccessibilityService extends android.accessibilityservice.AccessibilityService {
//
//    // 적용해야하는 앱의 minSDK : 16, targetSDK : 23. 기기동작은 33도 가능
//
//    private static final String TAG = "MyAccessibilityService";
//
//    private AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//
//
//    // 실행동안 반응 대기 (필수)
//    @Override
//    public void onInterrupt() {
//
//    }
//
//    // 서비스가 종료될 때 (선택)
//    @Override
//    public boolean onUnbind(Intent intent) {
//        return super.onUnbind(intent);
//    }
//
//    public void stopAccessibilityService() {
//        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
//        ComponentName componentName = new ComponentName(this, MyAccessibilityService.class);
//
//        if (accessibilityManager.isEnabled()) {
//            // Accessibility Service가 활성화되어 있는 경우 비활성화합니다.
//            Settings.Secure.putString(
//                    getContentResolver(),
//                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
//                    Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
//                            .replaceFirst(componentName.flattenToShortString() + ":*", "")
//                            .replaceAll(":$", "")
//            );
//
//            // Accessibility Service가 중지되도록 설정 변경을 적용합니다.
//            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
//        }
//    }
//
//    // 이벤트가 발생할때마다 실행되는 부분
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//        LogMgr.e(TAG, "Catch Event : " + event.toString());
//        LogMgr.e(TAG, "\nCatch Event Type : " + event.getEventType());
//        LogMgr.e(TAG, "\nCatch Event Package Name : " + event.getPackageName());
//        LogMgr.e(TAG, "\nCatch Event Time : " + event.getEventTime());
//        LogMgr.e(TAG, "\nCatch Event TEXT : " + event.getText());
//        //LogMgr.e(TAG, "Catch Event ContentDescription  : " + event.getContentDescription());
//        LogMgr.e(TAG, "\nCatch Event getSource : " + event.getSource());
//        LogMgr.e(TAG, "\nCatch Event getAction : " + event.getAction());
//        LogMgr.e(TAG, "\nCatch Event getScrollX : " + event.getScrollX());
//        LogMgr.e(TAG, "\nCatch Event getScrollY : " + event.getScrollY());
//        LogMgr.e(TAG, "\n=========================================================================");
//
//        int eventType = event.getEventType();
//
//        switch (eventType) {
//            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
//                // 터치 상호작용이 시작될 때 실행할 코드
//                LogMgr.d(TAG, "TYPEA_TOUCH_INTERACTION_START");
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
//                // 터치 상호작용이 종료될 때 실행할 코드
//                LogMgr.d(TAG, "TYPEA_TOUCH_INTERACTION_END");
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
//                LogMgr.d(TAG, "TYPEA_TOUCH_EXPLORATION_GESTURE_START");
//                break;
//            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
//                LogMgr.d(TAG, "TYPEA_TOUCH_EXPLORATION_GESTURE_END");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
//                LogMgr.d(TAG, "TYPEA_VIEW_HOVER_ENTER");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
//                LogMgr.d(TAG, "TYPEA_VIEW_HOVER_EXIT");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_CLICKED:
//                LogMgr.d(TAG, "TYPEA_VIEW_CLICKED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:
//                LogMgr.d(TAG, "TYPEA_VIEW_CONTEXT_CLICKED");
//                break;
//            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
//                LogMgr.d(TAG, "TYPEA_VIEW_LONG_CLICKED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
//                LogMgr.d(TAG, "TYPEA_VIEW_ACCESSIBILITY_FOCUSED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
//                LogMgr.d(TAG, "TYPEA_VIEW_ACCESSIBILITY_FOCUS_CLEARED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
//                LogMgr.d(TAG, "TYPEA_VIEW_FOCUSED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                LogMgr.d(TAG, "TYPEA_WINDOW_STATE_CHANGED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
//                LogMgr.d(TAG, "TYPEA_ANNOUNCEMENT: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
//                LogMgr.d(TAG, "TYPEA_GESTURE_DETECTION_START: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
//                LogMgr.d(TAG, "TYPEA_GESTURE_DETECTION_END: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
//                LogMgr.d(TAG, "TYPEA_VIEW_TEXT_CHANGED: " + event.getPackageName() + "\n" + event.getText());
//                break;
//            // 다른 이벤트 유형에 대한 처리 추가 가능
//        }
//    }
//
//    @Override
//    protected void onServiceConnected() {
//        super.onServiceConnected();
//        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
//        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
//            public void run() {
//                serviceChecker();
//            }
//        }, 0, 5, TimeUnit.SECONDS);
//    }
//
//    private void serviceChecker(){
//        if(!isActivityRunning(MainActivity.class)) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                disableSelf();
//            }
//        }
//    }
//    protected Boolean isActivityRunning(Class activityClass)
//    {
//        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
//
//        for (ActivityManager.RunningTaskInfo task : tasks) {
//            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
//                return true;
//        }
//
//        return false;
//    }
//}
