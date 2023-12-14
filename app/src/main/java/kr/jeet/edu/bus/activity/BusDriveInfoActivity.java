package kr.jeet.edu.bus.activity;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.adapter.BusRouteListAdapter;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.common.IntentParams;
import kr.jeet.edu.bus.model.data.BusDriveHistoryData;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.data.BusRouteData;
import kr.jeet.edu.bus.model.response.BaseResponse;
import kr.jeet.edu.bus.model.response.BusDriveHistoryResponse;
import kr.jeet.edu.bus.model.response.BusInfoResponse;
import kr.jeet.edu.bus.model.response.BusRouteResponse;
import kr.jeet.edu.bus.server.RetrofitApi;
import kr.jeet.edu.bus.server.RetrofitClient;
import kr.jeet.edu.bus.utils.LogMgr;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import kr.jeet.edu.bus.utils.Utils;
import kr.jeet.edu.bus.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusDriveInfoActivity extends BaseActivity {

    private static final String TAG = "BusDriveInfoActivity";

    private RecyclerView mRecyclerRoute;
    private TextView mTvListEmpty, mTvBcName, mTvBusPhone, mTvBusStartDate;
    private AppBarLayout appbar;

    private BusRouteListAdapter mAdapter;
    private ArrayList<BusRouteData> mList = new ArrayList<>();

    private String _bcName = "";
    private int _busCode = -1;
    private int _busDriveSeq = -1;
    private String _phoneNumber = "";
    private int _position = -1;

    private static final String DRIVE = "Y";
    private static final String NOT_DRIVE = "N";

    private BusInfoData mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_drive_info);
        mContext = this;
        initView();
        initAppbar();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestBusInfo();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(mInfo != null) {
            requestBusesList(mInfo);
        }
    }

    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_bus_info);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //customAppbar.setTvRight(getString(R.string.menu_item_drive_cancel), v -> requestDriveCancel());
    }

    private void initData(){
        Intent intent = getIntent();
        if (intent != null){
            if (intent.hasExtra(IntentParams.PARAM_BUS_INFO)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_BUS_INFO, BusInfoData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_BUS_INFO);
                }
            }
            if (intent.hasExtra(IntentParams.PARAM_BUS_INFO_POSITION)) {
                _position = intent.getIntExtra(IntentParams.PARAM_BUS_INFO_POSITION, _position);
            }
        }

        if (mInfo != null){
            _bcName = Utils.getStr(mInfo.bcName);
            _busCode = mInfo.busCode;
            _busDriveSeq = mInfo.busDriveSeq;
            _phoneNumber = Utils.getStr(mInfo.busPhoneNumber);
        } else {
            finish();
            Toast.makeText(mContext, R.string.bus_route_load_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    void initView() {
        initData();

        findViewById(R.id.btn_finish).setOnClickListener(this);

        mRecyclerRoute = findViewById(R.id.recycler_bus_route);
        mTvListEmpty = findViewById(R.id.tv_route_list_empty);
        mTvBcName = findViewById(R.id.tv_bc_name);
        mTvBusPhone = findViewById(R.id.tv_bus_phone);
        appbar = findViewById(R.id.appbar);

        String str = "";

        str = TextUtils.isEmpty(mInfo.bcName) ? "(정보 없음)" : mInfo.bcName;
        mTvBcName.setText(str);

        str = TextUtils.isEmpty(mInfo.busPhoneNumber) ? "(정보 없음)" : Utils.formatNum(mInfo.busPhoneNumber.replace("-", ""));
        mTvBusPhone.setText(str);

        setRecycler();
        if(mInfo != null) {
            requestBusDriveHistory(mInfo);
        }
        LogMgr.e(TAG, "mInfo = " + mInfo.startDate);
    }

    private void setRecycler(){

        mAdapter = new BusRouteListAdapter(mContext, mList, this::clickArrive);
        mRecyclerRoute.setAdapter(mAdapter);
    }

    private void clickArrive(ArrayList<BusRouteData> item, int position){

        if (mList.get(position).setClickable){
            showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.dialog_drive_arrive_confirm, item.get(position).bpName), ok -> {
                if (mList.size() - 1 == position) requestBusStop(item, position, NOT_DRIVE);
                else requestBusStop(item, position, DRIVE);
                hideMessageDialog();
            }, cancel -> {
                hideMessageDialog();
                return;
            });

        }else{
            Toast.makeText(mContext, R.string.bus_route_impossible_click, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mInfo != null) {
            if(TextUtils.isEmpty(mInfo.busFile1) && TextUtils.isEmpty(mInfo.busFile2)) return true;
            getMenuInflater().inflate(R.menu.menu_timetable, menu);
        }
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_timetable:
                ArrayList<String> items = new ArrayList<>();
                if(!Utils.isEmptyContainSpace(mInfo.busFile1)) items.add(mInfo.busFile1);
                if(!Utils.isEmptyContainSpace(mInfo.busFile2)) items.add(mInfo.busFile2);
                Intent photoIntent = new Intent(BusDriveInfoActivity.this, PhotoViewActivity.class);
                photoIntent.putStringArrayListExtra(IntentParams.PARAM_WEB_DETAIL_IMG, items);
                photoIntent.putExtra(IntentParams.PARAM_WEB_DETAIL_IMG_POSITION, 0);
                startActivity(photoIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.btn_finish:
                requestDriveCancel();
                break;
        }
    }

    // 버스 노선조회
    private void requestRouteList(){

        showProgressDialog();

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusRoute(_bcName, _busCode).enqueue(new Callback<BusRouteResponse>() {
                @Override
                public void onResponse(Call<BusRouteResponse> call, Response<BusRouteResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            if (mList!=null && mList.size() > 0) mList.clear();
                            LogMgr.e(TAG, "mInfo startDate = " + mInfo.startDate);
                            if (response.body().data != null){
                                BusRouteData startData = null;
                                startData = new BusRouteData();
                                startData.bpName = getString(R.string.title_bus_start);
                                startData.isArrive = "Y";
                                startData.startDate = mInfo.startDate;
                                startData.setClickable = true;
                                startData.isSuccess = true;
                                List<BusRouteData> getData = response.body().data;
                                if (getData.size() > 0){

                                    try {
                                        for (int i = getData.size()-1; 0 <= i ; i--){
                                            if (i < getData.size() - 1){
                                                if (getData.get(i).isArrive != null && getData.get(i).isArrive.equals("Y")){
                                                    getData.get(i+1).setClickable = true;
                                                    break;

                                                }
                                                else if (i == 0){
                                                    getData.get(i).setClickable = true;
                                                }
                                            }
                                        }
                                        for (BusRouteData data : getData) {
                                            if (data.isArrive != null && data.isArrive.equals("Y")) {
                                                data.setClickable = true;
                                                data.isSuccess = true;
                                            }
                                        }
                                        mList.add(startData);
                                        if (mList != null) mList.addAll(getData);
                                    }catch (Exception e) {
                                        Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    //Toast.makeText(mContext, R.string.drive_start, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    } else {
                        //PreferenceUtil.setDriveSeq(mContext, 0);

                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.bus_info_impossible, Toast.LENGTH_SHORT).show();

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.bus_info_not_found, Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    if (mList != null) mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BusRouteResponse> call, Throwable t) {
                    LogMgr.e(TAG, "request() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_start_server_fail, Toast.LENGTH_SHORT).show();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();

                }
            });
        }
    }

    private void scrollToPosition(int position) {
        mRecyclerRoute.post(() -> {
            if (!mRecyclerRoute.hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {
                mRecyclerRoute.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
            }
            // 아이템의 높이를 얻어옵니다.
            int itemHeight = mRecyclerRoute.getChildAt(0).getHeight();

            // 디바이스 화면의 높이를 얻어옵니다.
            int screenHeight = getResources().getDisplayMetrics().heightPixels;

            // position 번째 아이템을 화면 가운데에 위치하도록 스크롤합니다.
            int scrollToY = (position * itemHeight) - (screenHeight / 2) + (itemHeight * 2);
            mRecyclerRoute.smoothScrollBy(0, scrollToY);
        });
    }

    // 정류장 도착
    private void requestBusStop(ArrayList<BusRouteData> item, int position, String isDrive){

        //showProgressDialog();

        String bpCode = item.get(position).bpCode;

        item.get(position).isLoading = true;
        mAdapter.notifyItemChanged(position);
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusStop(_busDriveSeq, bpCode, isDrive).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            if (isDrive.equals(NOT_DRIVE)) {
                                PreferenceUtil.setDriveSeq(mContext, 0);
                                item.get(position).setClickable = true;
                                mList.set(position, item.get(position));
                                mAdapter.notifyItemChanged(position);
                                Toast.makeText(mContext, R.string.bus_route_drive_success, Toast.LENGTH_SHORT).show();

                                Intent intent = getIntent();

                                intent.putExtra(IntentParams.PARAM_DRIVE_FINISH, true);
                                setResult(RESULT_OK, intent);
                                finish();

                            } else {
                                item.get(position).isSuccess = true;
                                mList.set(position, item.get(position));
                                mAdapter.notifyItemChanged(position);

                                if (mList.size() - 2 >= position) {
                                    item.get(position + 1).setClickable = true;
                                    mList.set(position + 1, item.get(position + 1));
                                    mAdapter.notifyItemChanged(position + 1);
                                }
                            }

                        }
                    } else {
                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.bus_route_binding_error, Toast.LENGTH_SHORT).show();

                        } else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                            Toast.makeText(mContext, R.string.bus_route_not_found, Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                        }
                    }
                    //hideProgressDialog();
                    item.get(position).isLoading = false;
                    mAdapter.notifyItemChanged(position);
                    scrollToPosition(position);
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestBusStop() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_stop_server_fail, Toast.LENGTH_SHORT).show();
                    //hideProgressDialog();
                    item.get(position).isLoading = false;
                    mAdapter.notifyItemChanged(position);
                }
            });
        }
    }

    private void requestDriveCancel(){
        showMessageDialog(getString(R.string.dialog_title_alarm), getString(R.string.dialog_drive_cancel_confirm), ok -> {

            hideMessageDialog();

            if(RetrofitClient.getInstance() != null) {
                showProgressDialog();
                RetrofitClient.getApiInterface().getBusDriveFinish(_busDriveSeq).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if(response.isSuccessful()) {
                            if(response.body() != null) {
                                PreferenceUtil.setDriveSeq(mContext, 0);
                                Toast.makeText(mContext, R.string.bus_route_drive_finish, Toast.LENGTH_SHORT).show();

                                Intent intent = getIntent();

                                intent.putExtra(IntentParams.PARAM_DRIVE_FINISH, true);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                                Toast.makeText(mContext, R.string.bus_route_drive_finish_binding_error, Toast.LENGTH_SHORT).show();

                            } else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND) {
                                Toast.makeText(mContext, R.string.bus_route_drive_finish_not_found, Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(mContext, R.string.server_data_empty, Toast.LENGTH_SHORT).show();
                            }
                        }
                        hideProgressDialog();
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        LogMgr.e(TAG, "requestDriveCancel() onFailure >> " + t.getMessage());
                        Toast.makeText(mContext, R.string.bus_stop_server_fail, Toast.LENGTH_SHORT).show();
                        hideProgressDialog();
                    }
                });
            }

        }, cancel -> {
            hideMessageDialog();
            return;
        });
    }
    //버스 목록 조회 (등/하원 시간표 용)
    private void requestBusesList(BusInfoData item){
        if (RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusesInfo(item.bcName, item.busCode).enqueue(new Callback<BusInfoResponse>() {
                @Override
                public void onResponse(Call<BusInfoResponse> call, Response<BusInfoResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                List<BusInfoData> list = response.body().data;
                                if (list != null && !list.isEmpty()) {
                                    BusInfoData data = list.get(0);
                                    item.busFile1 = data.busFile1;
                                    item.busFile2 = data.busFile2;
                                }
                            }
                        } else {

                        }
                    } catch (Exception e) {
                        LogMgr.e(TAG + "requestTestReserveList() Exception : ", e.getMessage());
                    }finally{
                        invalidateOptionsMenu();
                    }


                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    invalidateOptionsMenu();
                }
            });
        }
    }
    // 버스 운행이력조회 (출발시간용)
    private void requestBusDriveHistory(BusInfoData data){
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusDriveHistory(data.bcName, data.busCode).enqueue(new Callback<BusDriveHistoryResponse>() {
                @Override
                public void onResponse(Call<BusDriveHistoryResponse> call, Response<BusDriveHistoryResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                BusDriveHistoryData getData = response.body().data;
                                if (getData != null) {
                                    //운행중?
                                    LogMgr.e(TAG, "isDrive = " + getData.isDrive + " /startDate=" + getData.startDate);
                                    data.isDrive = getData.isDrive;

                                    if (getData.isDrive.equals("Y")) {
                                        data.startDate = getData.startDate;
                                    }
                                }
                            }

                        }
                    }catch(Exception ex) {}
                    finally {
                        requestRouteList();
                    }
                }

                @Override
                public void onFailure(Call<BusDriveHistoryResponse> call, Throwable t) {
                    LogMgr.e(TAG, "request() onFailure >> " + t.getMessage());
                    requestRouteList();
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
                                
                                for (int i = 0; i < getDataList.size(); i++) {
                                    if (i == _position) {
                                        if (getDataList.get(i).busDriveSeq == Constants.NOT_DRIVING) {
                                            finish();
                                            Toast.makeText(mContext, R.string.bus_route_already_end, Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                }

                                DataManager.getInstance().setbusInfoList(getDataList);
                            }
                        }
                    } else {
                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){
                            Toast.makeText(mContext, R.string.bus_route_wrong_approach, Toast.LENGTH_SHORT).show();
                            finish();
                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){
                            Toast.makeText(mContext, R.string.write_phone_not_found, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<BusInfoResponse> call, Throwable t) {
                    LogMgr.e(TAG, "onFailure >> " + t.getMessage());
                    hideProgressDialog();
                    Toast.makeText(mContext, R.string.bus_route_server_fail, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }
}