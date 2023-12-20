package kr.jeet.edu.bus.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {
    private static final String PREFERENCE_NAME = "kr.jeet.edu.bus.preferences";
    public static final String PREF_AUTO_LOGIN = "auto_login"; // 자동로그인

    public static final String PREF_BC_NAME = "bc_name"; // 버스 캠퍼스 이름
    public static final String PREF_BUS_NAME = "bus_name"; // 버스 이름
    public static final String PREF_BUS_CODE = "bus_code"; // 버스 코드
    public static final String PREF_PHONE_NUMBER = "phone_number"; // 기사(동승자) 휴대폰번호
    public static final String PREF_COMPARE_PHONE_NUMBER = "compare_phone_number"; // 로그인 번호 비교
    public static final String PREF_IS_AUTHORIZED = "is_authorized"; // 로그인 시 번호 인증됨

    public static final String PREF_DRIVE_SEQ = "drive_seq"; // 버스 운행 seq

    //Auto Login
    public static void setAutoLogin(Context context, boolean set) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_AUTO_LOGIN, set).apply();
    }

    public static boolean getAutoLogin(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_AUTO_LOGIN, false);
    }

    // bc name
    public static void setBcName(Context context, String bcName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_BC_NAME, bcName).apply();
    }

    public static String getBcName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_BC_NAME, "");
    }

    // bus name
    public static void setBusName(Context context, String busName) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_BUS_NAME, busName).apply();
    }

    public static String getBusName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_BUS_NAME, "");
    }

    // bus code
    public static void setBusCode(Context context, int busCode) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_BUS_CODE, busCode).apply();
    }

    public static int getBusCode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_BUS_CODE, 0);
    }

    // phone number
    public static void setPhoneNumber(Context context, String phoneNum) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_PHONE_NUMBER, phoneNum).apply();
    }

    public static String getPhoneNumber(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_PHONE_NUMBER, "");
    }

    public static void setComparePhoneNumber(Context context, String phoneNum) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_PHONE_NUMBER, phoneNum).apply();
    }

    public static String getComparePhoneNumber(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_PHONE_NUMBER, "");
    }

    // bus code
    public static void setDriveSeq(Context context, int driveSeq) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_DRIVE_SEQ, driveSeq).apply();
    }

    public static int getDriveSeq(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_DRIVE_SEQ, 0);
    }

    //is Authorized
    public static void setPrefIsAuthorized(Context context, boolean isAuthorized){
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_IS_AUTHORIZED, isAuthorized).apply();
    }
    public static boolean getIsAuthorized(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_IS_AUTHORIZED, false);
    }
}
