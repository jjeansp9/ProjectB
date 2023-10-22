package kr.jeet.edu.bus.activity;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.UiAutomation;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.accessibility.AccessibilityManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.adapter.BusInfoListAdapter;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.common.IntentParams;
import kr.jeet.edu.bus.model.data.BusDriveSeqData;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.request.BusDriveRequest;
import kr.jeet.edu.bus.model.response.BusDriveResponse;
import kr.jeet.edu.bus.model.response.BusInfoResponse;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.server.RetrofitClient;
import kr.jeet.edu.bus.utils.LifeCycleChecker;
import kr.jeet.edu.bus.utils.LogMgr;
import kr.jeet.edu.bus.utils.PreferenceUtil;
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

    private boolean impossibleDrive = false;
    private boolean startDrive = false;

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        LogMgr.w("result =" + result);
        if(result.getResultCode() != RESULT_CANCELED) {
            Intent intent = result.getData();
            boolean finished;

            if(intent != null && intent.hasExtra(IntentParams.PARAM_DRIVE_FINISH)) {
                finished = intent.getBooleanExtra(IntentParams.PARAM_DRIVE_FINISH, false);

                if(finished) {
                    impossibleDrive = false;
                    requestBusInfo();
                }
            }
        }
    });

    private AppCompatActivity activity;
    LifeCycleChecker checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        mContext = this;

        if (checker == null){
            checker = new LifeCycleChecker(activity);
            checker.onCreate();
        }

        initView();
        initAppbar();

        // 접근성 권한이 없으면 접근성 권한 설정하는 다이얼로그 띄워주는 부분
        if(!checkAccessibilityPermissions()) {
            setAccessibilityPermissions();
        }
    }

    public boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(this.ACCESSIBILITY_SERVICE);

        // getEnabledAccessibilityServiceList use for getting a list of apps that have accessibility permission.
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);
        LogMgr.e(TAG,"packageName Service: " + list.size());

        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);

            // Check current app exist in the list.
            // Compare with package name of app.
            if (info.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Execute to permission settings.
     */
    public void setAccessibilityPermissions() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("Setting Accessibility Permission");
        gsDialog.setMessage("Need Accessibility Permission");
        gsDialog.setPositiveButton("Check", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Start setting permission activity
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }
        }).create().show();
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
    }

    @Override
    void initView() {
        initData();

        tvBusInfoEmpty = findViewById(R.id.tv_bus_info_empty);

        mRecyclerBusInfo = findViewById(R.id.recycler_bus_info);
        mBusInfoAdapter = new BusInfoListAdapter(mContext, busInfoList, this::driving, this::startDrive, startDrive);
        mRecyclerBusInfo.setAdapter(mBusInfoAdapter);

        tvBusInfoEmpty.setVisibility(busInfoList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // 운행중 클릭
    private void driving(BusInfoData item, int position){
        if (item != null){
            if (item.busDriveSeq != Constants.NOT_DRIVING) startDriveActivity(item, position);
            else requestDriveStart(item, position);
        }
    }

    // 운행시작 클릭
    private void startDrive(BusInfoData item, int position){
        for (BusInfoData data : busInfoList) {
            if (data.busDriveSeq != Constants.NOT_DRIVING) {
                impossibleDrive = true;
                break;
            }
        }

        if (!startDrive){
            startDrive = true;
            if (impossibleDrive) {
                Toast.makeText(mContext, R.string.drive_not_start, Toast.LENGTH_SHORT).show();
                startDrive = false;
            }
            else {
                if (item != null){
                    if (item.busDriveSeq != Constants.NOT_DRIVING) startDriveActivity(item, position);
                    else requestDriveStart(item, position);
                }else{
                    startDrive = false;
                }
            }
        }
    }

    // 운행시작
    private void requestDriveStart(BusInfoData item, int position){

        showProgressDialog();

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
                            item.busDriveSeq = getData.busDriveSeq;
                            startDriveActivity(item, position);
                            Toast.makeText(mContext, R.string.drive_start, Toast.LENGTH_SHORT).show();

                            busInfoList.get(position).busDriveSeq = getData.busDriveSeq;
                            mBusInfoAdapter.notifyItemChanged(position, busInfoList);
                        }

                    } else {
                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.bus_info_impossible, Toast.LENGTH_SHORT).show();

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.bus_info_not_found, Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }
                    }
                    startDrive = false;
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BusDriveResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestDriveStart() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_start_server_fail, Toast.LENGTH_SHORT).show();
                    startDrive = false;
                    hideProgressDialog();
                }
            });
        }
    }

    // 버스정보 조회
    private void requestBusInfo(){

        showProgressDialog();

        String phoneNum = PreferenceUtil.getPhoneNumber(mContext);

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusInfo(phoneNum).enqueue(new Callback<BusInfoResponse>() {
                @Override
                public void onResponse(Call<BusInfoResponse> call, Response<BusInfoResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            List<BusInfoData> getDataList = response.body().data;
                            if (getDataList != null && !getDataList.isEmpty()){

                                if (busInfoList != null) {
                                    if (busInfoList.size() > 0) busInfoList.clear();
                                    busInfoList.addAll(getDataList);
                                }

                                if (mBusInfoAdapter != null) mBusInfoAdapter.notifyDataSetChanged();

                                DataManager.getInstance().setbusInfoList(getDataList);
                            }
                        }
                    } else {
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

    private void startDriveActivity(BusInfoData item, int position){
        if (item != null) {
            LogMgr.e(TAG, "driveSeq: " + item.busDriveSeq);
            Intent intent = new Intent(mContext, BusDriveInfoActivity.class);
            intent.putExtra(IntentParams.PARAM_BUS_INFO, item);
            resultLauncher.launch(intent);
        }
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