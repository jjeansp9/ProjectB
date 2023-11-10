package kr.jeet.edu.bus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
import kr.jeet.edu.bus.utils.Utils;
import kr.jeet.edu.bus.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    // 버스 노선 리스트
    // http://m.jeet.kr/intro/table/index.jsp?route_type=1&campus_fk=

    private static final String TAG = "MainActivity";

    private TextView tvBusInfoEmpty;
    private AppCompatButton btnStartDrive;
    private List<BusInfoData> busInfoList = new ArrayList<>();
    private RecyclerView mRecyclerBusInfo;
    private BusInfoListAdapter mBusInfoAdapter;
    private String _phoneNum = "";

    private int _busDriveSeq = 0;

    private boolean impossibleDrive = false;
    private boolean startDrive = false;
    boolean doubleBackToExitPressedOnce = false;

    private AppCompatActivity activity;
    private LifeCycleChecker checker;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdateScheduler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdateScheduler();
    }

    private ScheduledExecutorService scheduler;

    private void startUpdateScheduler() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(this::updateBusInfoInBackground, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void stopUpdateScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    private void updateBusInfoInBackground() {

        LogMgr.e(TAG, "Scheduler Event");

        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusInfo(_phoneNum).enqueue(new Callback<BusInfoResponse>() {
                @Override
                public void onResponse(Call<BusInfoResponse> call, Response<BusInfoResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            final List<BusInfoData> getDataList = response.body().data;
                            if (getDataList != null && !getDataList.isEmpty()) {

                                if (busInfoList != null) {

                                    int checkDriving = 0;

                                    if (busInfoList.size() > 0) busInfoList.clear();
                                    for (int i = 0; i < getDataList.size(); i++) {

                                        busInfoList.add(getDataList.get(i));
                                        mBusInfoAdapter.notifyItemChanged(i, busInfoList);

                                        checkDriving += getDataList.get(i).busDriveSeq;
                                    }

                                    if (checkDriving == Constants.NOT_DRIVING) {
                                        PreferenceUtil.setStartDate(mContext, "");
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    LogMgr.e(TAG, "onFailure >> " + t.getMessage());
                }
            });
        }
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
        _phoneNum = PreferenceUtil.getPhoneNumber(mContext);
    }

    @Override
    void initView() {
        initData();

        tvBusInfoEmpty = findViewById(R.id.tv_bus_info_empty);

        mRecyclerBusInfo = findViewById(R.id.recycler_bus_info);
        mBusInfoAdapter = new BusInfoListAdapter(mContext, busInfoList, this::driving, this::startDrive, startDrive);
        LayoutAnimationController anim = AnimationUtils.loadLayoutAnimation(mContext, R.anim.anim_slide);
        mRecyclerBusInfo.setLayoutAnimation(anim);
        mRecyclerBusInfo.scheduleLayoutAnimation();
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
            } else {
                impossibleDrive = false;
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
                    else {
                        showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.dialog_drive_start_confirm), ok -> {
                            hideMessageDialog();
                            requestDriveStart(item, position);

                        }, cancel -> {
                            hideMessageDialog();
                            startDrive = false;
                            return;
                        });
                    }
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
                            // TODO 다른 디바이스에서 운행시작하면 디바이스끼리 출발시간 공유 안됨
                            String startDate = Utils.currentDate(Constants.DATE_FORMATTER_YYYY_MM_DD_E_HH_mm_ss);
                            PreferenceUtil.setStartDate(mContext, startDate);

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

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusInfo(_phoneNum).enqueue(new Callback<BusInfoResponse>() {
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

                                //DataManager.getInstance().setbusInfoList(getDataList);
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
            intent.putExtra(IntentParams.PARAM_BUS_INFO_POSITION, position);
            resultLauncher.launch(intent);
            overridePendingTransition(R.anim.horizontal_enter, R.anim.horizontal_out);
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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.msg_backbutton_to_exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}