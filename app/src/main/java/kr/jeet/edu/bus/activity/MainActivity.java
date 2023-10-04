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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.adapter.BusInfoListAdapter;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.common.IntentParams;
import kr.jeet.edu.bus.model.data.BusDriveSeqData;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.data.BusRouteData;
import kr.jeet.edu.bus.model.request.BusDriveRequest;
import kr.jeet.edu.bus.model.response.BusDriveResponse;
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

    // 버스 노선 리스트
    // http://m.jeet.kr/intro/table/index.jsp?route_type=1&campus_fk=

    private static final String TAG = "MainActivity";

    private TextView tvPhoneNum, tvBcName, tvBusName, tvBusCode, tvDate, tvBusInfoEmpty;
    private AppCompatButton btnStartDrive;
    private List<BusInfoData> busInfoList = new ArrayList<>();
    private RecyclerView mRecyclerBusInfo;
    private BusInfoListAdapter mBusInfoAdapter;

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
                    //btnStartDrive.setText(getString(R.string.btn_start_drive));
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

//        btnStartDrive = findViewById(R.id.btn_start_drive);
//        btnStartDrive.setOnClickListener(this);
//
//        tvPhoneNum = findViewById(R.id.tv_phone_number);
//        tvBusName = findViewById(R.id.tv_bus_name);
//        //tvDate = findViewById(R.id.tv_date);
//
//        //tvDate.setText(Utils.currentDate("yyyy-MM-dd (E)"));
//        //tvPhoneNum.setText(Utils.getStr(busInfoList.get(0).busPhoneNumber));
//        tvBusName.setText(Utils.getStr(busInfoList.get(0).busName));
//
//        if (_busDriveSeq != 0) btnStartDrive.setText(getString(R.string.btn_go_driving));
//        else btnStartDrive.setText(getString(R.string.btn_start_drive));

        tvBusInfoEmpty = findViewById(R.id.tv_bus_info_empty);

        mRecyclerBusInfo = findViewById(R.id.recycler_bus_info);
        mBusInfoAdapter = new BusInfoListAdapter(mContext, busInfoList, this::clickBusInfoItem);
        mRecyclerBusInfo.setAdapter(mBusInfoAdapter);

        tvBusInfoEmpty.setVisibility(busInfoList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void clickBusInfoItem(BusInfoData item){
        //requestDriveStart(item);

        startDriveActivity();
    }

//    @Override
//    public void onClick(View v) {
//        super.onClick(v);
//        switch (v.getId()){
//            case R.id.btn_start_drive:
//                LogMgr.e(TAG, _busDriveSeq+"");
//                if (_busDriveSeq != 0) startDriveActivity();
//                else requestDriveStart();
//                break;
//        }
//    }

    // TODO : 3. 로그아웃 했을 때 busDriveSeq 관련 이슈
    // TODO : 4. 버스정보조회 api에서 isDrive 데이터를 가져올 수 있는지 확인

    private void requestDriveStart(BusInfoData item){

        BusDriveRequest request = new BusDriveRequest();
        request.bcName = item.bcName;
        request.busName = item.busName;
        request.busCode = item.busCode;

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusDriveStart(request).enqueue(new Callback<BusDriveResponse>() {
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
                Intent intent = new Intent(mContext, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}