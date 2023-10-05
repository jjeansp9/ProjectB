package kr.jeet.edu.bus.activity;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.adapter.BusRouteListAdapter;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.common.IntentParams;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.data.BusRouteData;
import kr.jeet.edu.bus.model.response.BaseResponse;
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
    private TextView mTvListEmpty, mTvBcName, mTvBusInfo;
    private RelativeLayout mProgress;
    private AppBarLayout appbar;

    private List<BusInfoData> busInfoList;

    private BusRouteListAdapter mAdapter;
    private ArrayList<BusRouteData> mList = new ArrayList<>();

    private String _bcName = "";
    private int _busCode = -1;
    private int _busDriveSeq = -1;
    private String _phoneNumber = "";

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
        if (intent != null && intent.hasExtra(IntentParams.PARAM_BUS_INFO)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mInfo = intent.getParcelableExtra(IntentParams.PARAM_BUS_INFO, BusInfoData.class);
            }else{
                mInfo = intent.getParcelableExtra(IntentParams.PARAM_BUS_INFO);
            }
        }

        busInfoList = DataManager.getInstance().getBusInfoList();

        _bcName = Utils.getStr(mInfo.bcName);
        _busCode = mInfo.busCode;
        _busDriveSeq = mInfo.busDriveSeq;
        _phoneNumber = Utils.getStr(mInfo.busPhoneNumber);
    }

    @Override
    void initView() {
        initData();

        findViewById(R.id.btn_finish).setOnClickListener(this);

        mRecyclerRoute = findViewById(R.id.recycler_bus_route);
        mTvListEmpty = findViewById(R.id.tv_route_list_empty);
        mTvBcName = findViewById(R.id.tv_bc_name);
        mTvBusInfo = findViewById(R.id.tv_bus_info);
        appbar = findViewById(R.id.appbar);

        mProgress = findViewById(R.id.progress);

        mTvBcName.setText(Utils.getStr(mInfo.bcName));
        mTvBusInfo.setText(Utils.formatNum(Utils.getStr(mInfo.busPhoneNumber.replace("-", ""))));

        setRecycler();
        requestRouteList();
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

                            List<BusRouteData> getData = response.body().data;
                            if (getData != null){

                                for (int i = getData.size()-1; 0 <= i ; i--){
                                    if (i < getData.size() - 1){
                                        if (getData.get(i).isArrive.equals("Y")){
                                            getData.get(i+1).setClickable = true;
                                            break;

                                        }else if (i == 0){
                                            getData.get(i).setClickable = true;
                                        }
                                    }
                                }
                                for (BusRouteData data : getData) {
                                    if (data.isArrive.equals("Y")) {
                                        data.setClickable = true;
                                        data.isSuccess = true;
                                    }
                                }

                                if (mList != null) mList.addAll(getData);

                                //Toast.makeText(mContext, R.string.drive_start, Toast.LENGTH_SHORT).show();
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
        mProgress.setVisibility(View.VISIBLE);

        String bpCode = item.get(position).bpCode;

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
                    mProgress.setVisibility(View.GONE);

                    scrollToPosition(position);
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestBusStop() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_stop_server_fail, Toast.LENGTH_SHORT).show();
                    //hideProgressDialog();
                    mProgress.setVisibility(View.GONE);

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
}