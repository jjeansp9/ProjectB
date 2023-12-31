package kr.jeet.edu.bus.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.dialog.AuthorizeDialog;
import kr.jeet.edu.bus.model.data.ACAData;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.response.BusInfoResponse;
import kr.jeet.edu.bus.model.response.GetACAListResponse;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.server.RetrofitClient;
import kr.jeet.edu.bus.utils.LogMgr;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import kr.jeet.edu.bus.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "loginActivity";

    private EditText mEditPhoneNum;
    private CheckBox mAutoLoginCb;
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
            Toast.makeText(mContext, R.string.auth_incomplete2, Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        initView();
    }

    @Override
    void initView() {
        findViewById(R.id.login_root).setOnClickListener(this);
        findViewById(R.id.ly_choice).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);

        mEditPhoneNum = findViewById(R.id.edit_phone_num);
        mAutoLoginCb = findViewById(R.id.checkbox_login);
    }

    @Override
    void initAppbar() {}

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.login_root:
                mEditPhoneNum.clearFocus();
                Utils.hideKeyboard(mContext, mEditPhoneNum);
                break;

            case R.id.ly_choice:
                mAutoLoginCb.setChecked(!mAutoLoginCb.isChecked());
                break;

            case R.id.btn_login:
                if (mAutoLoginCb != null) PreferenceUtil.setAutoLogin(mContext, mAutoLoginCb.isChecked());
                PreferenceUtil.setPrefIsAuthorized(mContext, false);
                if (checkLogin()) requestLogin();
                break;
        }
    }

    private void requestLogin(){

        showProgressDialog();

        String phoneNum = mEditPhoneNum.getText().toString();

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
                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.write_phone_impossible, Toast.LENGTH_SHORT).show();

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.write_phone_not_found, Toast.LENGTH_SHORT).show();
                        }
                    }

                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    LogMgr.e(TAG, "onFailure >> " + t.getMessage());
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkLogin(){
        if (TextUtils.isEmpty(mEditPhoneNum.getText().toString().trim())){
            Toast.makeText(mContext, R.string.login_phone_num_empty, Toast.LENGTH_SHORT).show();
            return false;
        }else if(!Utils.checkPhoneNumber(mEditPhoneNum.getText().toString())){
            Toast.makeText(mContext, R.string.write_phone_impossible, Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
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
    private void startMain(){
        startActivity(new Intent(mContext, MainActivity.class));
        finish();
    }
}