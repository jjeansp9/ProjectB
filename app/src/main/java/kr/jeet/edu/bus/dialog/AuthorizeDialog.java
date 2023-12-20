package kr.jeet.edu.bus.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import kr.jeet.edu.bus.view.AuthPhoneNumberView;

public class AuthorizeDialog extends Dialog {
    private Context context;
    private AuthPhoneNumberView authView;
    private TextView tvTitle, tvError, tvDescription;
    private LinearLayout layoutAutoLogin;
    private CheckBox cbAutoLogin;
    private RelativeLayout cancelBtn, okBtn;
    //checkbox

    private ViewGroup titleLayout;
    public AuthorizeDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public AuthorizeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    protected AuthorizeDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        initView();
    }

    private void initView() {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.layout_authorize_dialog);
        authView = findViewById(R.id.auth_view);
        authView.setShowProgressDelegate(new AuthPhoneNumberView.ShowProgressDialogDelegate() {
            @Override
            public void onRequest() {
                setEnabledOkButton(true);

            }
        });
        tvTitle = findViewById(R.id.title);
        tvError = findViewById(R.id.tv_error);
        // 버튼 테두리 라운드 적용
        findViewById(R.id.dialog_ly).setClipToOutline(true);
        cbAutoLogin = findViewById(R.id.check_autologin);
        cbAutoLogin.setChecked(PreferenceUtil.getAutoLogin(context));
        layoutAutoLogin = findViewById(R.id.layout_autologin);
        layoutAutoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbAutoLogin.setChecked(!cbAutoLogin.isChecked());
            }
        });
        titleLayout = findViewById(R.id.title_ly);
        cancelBtn = findViewById(R.id.cancelBtn);
        okBtn = findViewById(R.id.okBtn);


    }
    public void setPhoneNumber(String str) {
        if(authView != null) {
            authView.setLoggedInPhoneNumber(str);
        }
    }
    public void setError() {
        String errorMsg = authView.getErrorMsg();
        tvError.setText(errorMsg);

    }
    public void setTitle(String str) {
        if(!TextUtils.isEmpty(str)) {
            tvTitle.setText(str);
//            titleLayout.setVisibility(View.VISIBLE);
        } else {
//            titleLayout.setVisibility(View.GONE);
        }
    }
    public boolean checkValid() {
        if(authView != null) {
            return authView.checkValid();
        }
        return false;
    }
    public boolean getCheckedAutoLogin() {
        if(cbAutoLogin != null) {
            return cbAutoLogin.isChecked();
        }
        return false;
    }
//    public void setOkButtonText(String str) {
//        okBtn.setText(str);
//    }

    public void setOnOkButtonClickListener(View.OnClickListener listener) {
        okBtn.setOnClickListener(listener);
    }
    public void setEnabledOkButton(boolean flag) {
        if(okBtn != null) okBtn.setEnabled(flag);
    }
    public void setOnCancelButtonClickListener(View.OnClickListener listener) {
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setOnClickListener(listener);
    }


    @Override
    public void dismiss() {
        super.dismiss();
        if(authView != null) authView.release();
    }
}
