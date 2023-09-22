package kr.jeet.edu.bus.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.demogorgorn.monthpicker.MonthPickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.view.DrawableAlwaysCrossFadeFactory;

public class Utils {
    /**
     * 파라미터로 받은 editText의 개수만큼 focus 얻어오고 키보드를 내리는 메소드
     * */
    public static void hideKeyboard(Context mContext, View[] focusList) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        for (int i = 0; i < focusList.length; i++) {
            View view = focusList[i];
            if (view != null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideKeyboard(Context mContext, View focus) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) return;
        inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
    }

    /**
     * 리스트뷰 구분선에 margin을 주기위한 customDivider
     * */
    public static DividerItemDecoration setDivider(Context mContext) {
        int[] attrs = new int[]{android.R.attr.listDivider};

        TypedArray a = mContext.obtainStyledAttributes(attrs);
        Drawable divider = a.getDrawable(0);
        int inset = mContext.getResources().getDimensionPixelSize(R.dimen.layout_margin);
        InsetDrawable insetDivider = new InsetDrawable(divider, inset, 0, inset, 0);
        a.recycle();

        DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(insetDivider);
        return itemDecoration;
    }

    public static void yearMonthPicker(Context mContext, MonthPickerDialog.OnDateSetListener listener, int year, int month){

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(mContext, listener, year, month);

        try{
            builder.setYearRange(Constants.PICKER_MIN_YEAR, Constants.PICKER_MAX_YEAR)
                    .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                    .setOnYearChangedListener(v -> {})
                    .setOnMonthChangedListener(v -> {})
                    .setMonthNamesArray(R.array.month_names)
                    .setPositiveText(R.string.ok)
                    .setNegativeText(R.string.cancel)
                    .build().show();
        }catch(Exception e){

        }
    }

    /**
     * 날짜 포맷 [ yyyy-MM-dd , HH:mm -> M월 d일 (*요일)\n HH시 ss분 ]
     * */
    public static String formatDate(String inputDate, String inputTime, boolean isDetail) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("M월 d일 (E요일) ", Locale.KOREA);

        SimpleDateFormat inputTimeFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);
        SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH시 mm분", Locale.KOREA);

        try {
            Date date = inputDateFormat.parse(inputDate);
            Date time = inputTimeFormat.parse(inputTime);

            String formattedDate = "";
            String formattedTime = "";

            if (time != null) formattedTime = outputTimeFormat.format(time);
            if (date != null) formattedDate = outputFormat.format(date);

            if (isDetail) return formattedDate + "\n" + formattedTime;
            else return formattedDate + inputTime;

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getStr(String s){ return TextUtils.isEmpty(s) ? "" : s; }

    /**
     * 현재날짜 가져오기
     * pattern : 날짜 형식 입력 ex) "yyyy.MM.dd HH:ss"
     * */
    public static String currentDate(String pattern){
        Date currentDate = new Date(); // 현재 날짜 가져오기
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault()); // 날짜 형식 지정
        String formattedDate = dateFormat.format(currentDate); // 형식에 맞춰 날짜 문자열로 변환

        return formattedDate;
    }

    /**
     * 휴대폰번호 유효성검사
     * */
    public static boolean checkPhoneNumber(String str) {
        if(TextUtils.isEmpty(str)) return false;
        return Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", str);
    }

    /**
    * 이미지가 변경될 때 Animation 효과
    * */
    public static void animateImageChange(Context mContext, ImageView imageView, int resourceId) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.0f, // 시작 스케일 X
                1.0f, 0.0f, // 시작 스케일 Y
                Animation.RELATIVE_TO_SELF, 0.5f, // 중심 축 X
                Animation.RELATIVE_TO_SELF, 0.5f  // 중심 축 Y
        );

        scaleAnimation.setDuration(200); // 애니메이션 지속 시간
        imageView.startAnimation(scaleAnimation);

        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 애니메이션 시작 시 처리할 작업
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 애니메이션 종료 시 Glide를 사용하여 이미지 변경
                Glide.with(mContext)
                        .load(resourceId)
                        .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                        .into(imageView);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 애니메이션 반복 시 처리할 작업
            }
        });
    }
}
