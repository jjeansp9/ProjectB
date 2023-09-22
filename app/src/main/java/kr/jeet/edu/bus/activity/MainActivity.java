package kr.jeet.edu.bus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import java.io.IOException;
import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.common.IntentParams;
import kr.jeet.edu.bus.model.data.ACAData;
import kr.jeet.edu.bus.model.data.BusDriveSeqData;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.request.BusDriveRequest;
import kr.jeet.edu.bus.model.response.BusDriveResponse;
import kr.jeet.edu.bus.model.response.GetACAListResponse;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.server.RetrofitClient;
import kr.jeet.edu.bus.utils.LogMgr;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import kr.jeet.edu.bus.utils.Utils;
import kr.jeet.edu.bus.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private TextView tvPhoneNum, tvBcName, tvBusName, tvBusCode, tvDate;
    private AppCompatButton btnStartDrive;
    List<BusInfoData> busInfoList;

    private int _busDriveSeq = 0;

    private final int CMD_GET_ACALIST = 1;  // ACA정보 가져오기

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CMD_GET_ACALIST :
                    //requestACAList();
                    break;
            }
        }
    };

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            boolean finished = false;

            if(intent != null && intent.hasExtra(IntentParams.PARAM_DRIVE_FINISH)) {
                finished = intent.getBooleanExtra(IntentParams.PARAM_DRIVE_FINISH, false);

                if(finished) {
                    _busDriveSeq = PreferenceUtil.getDriveSeq(mContext);
                    btnStartDrive.setText(getString(R.string.btn_start_drive));
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initView();
        initAppbar();
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setLogoVisible(true);
        setSupportActionBar(customAppbar.getToolbar());
    }


    private void initData(){

        busInfoList = DataManager.getInstance().getBusInfoList();
        _busDriveSeq = PreferenceUtil.getDriveSeq(mContext);

        //mHandler.sendEmptyMessage(CMD_GET_ACALIST);
    }

    @Override
    void initView() {
        initData();

        btnStartDrive = findViewById(R.id.btn_start_drive);
        btnStartDrive.setOnClickListener(this);

        tvPhoneNum = findViewById(R.id.tv_phone_number);
        tvBusName = findViewById(R.id.tv_bus_name);
        tvDate = findViewById(R.id.tv_date);

        tvDate.setText(Utils.currentDate("yyyy-MM-dd (E)"));
        //tvPhoneNum.setText(Utils.getStr(busInfoList.get(0).busPhoneNumber));
        tvBusName.setText(Utils.getStr(busInfoList.get(0).busName));

        if (_busDriveSeq != 0) btnStartDrive.setText(getString(R.string.btn_go_driving));
        else btnStartDrive.setText(getString(R.string.btn_start_drive));
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_start_drive:
                LogMgr.e(TAG, _busDriveSeq+"");
                if (_busDriveSeq != 0) startDriveActivity();
                else requestDriveStart();
                break;
        }
    }

    private void requestDriveStart(){
        BusDriveRequest request = new BusDriveRequest();
        request.bcName = busInfoList.get(0).bcName;
        request.busName = busInfoList.get(0).busName;
        request.busCode = busInfoList.get(0).busCode;

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusDrive(request).enqueue(new Callback<BusDriveResponse>() {
                @Override
                public void onResponse(Call<BusDriveResponse> call, Response<BusDriveResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            BusDriveSeqData getData = response.body().data;
                            PreferenceUtil.setDriveSeq(mContext, getData.busDriveSeq); // drive seq
                            _busDriveSeq = getData.busDriveSeq;
                            startDriveActivity();
                            Toast.makeText(mContext, R.string.drive_start, Toast.LENGTH_SHORT).show();
                            btnStartDrive.setText(getString(R.string.btn_go_driving));
                        }

                    } else {
                        PreferenceUtil.setDriveSeq(mContext, 0);

                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.bus_info_impossible, Toast.LENGTH_SHORT).show();

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.bus_info_not_found, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BusDriveResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestDriveStart() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_start_server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void startDriveActivity(){
        resultLauncher.launch(new Intent(mContext, BusDriveInfoActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_setting:
//                Intent intent = new Intent(mContext, SettingsActivity.class);
//                resultLauncher.launch(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}