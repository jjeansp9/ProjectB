package kr.jeet.edu.bus.activity;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import kr.jeet.edu.bus.view.CustomAppbarLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusDriveInfoActivity extends BaseActivity {

    private static final String TAG = "BusDriveInfoActivity";

    private RecyclerView mRecyclerRoute;
    private TextView mTvListEmpty, mTvBcName, mTvBusInfo;
    private RelativeLayout mProgress;

    private List<BusInfoData> busInfoList;

    private BusRouteListAdapter mAdapter;
    private ArrayList<BusRouteData> mList = new ArrayList<>();

    private String _bcName = "";
    private int _busCode = -1;
    private int _busDriveSeq = -1;
    private String _phoneNumber = "";

    private static final String DRIVE = "Y";
    private static final String NOT_DRIVE = "N";

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
    }

    private void initData(){

        busInfoList = DataManager.getInstance().getBusInfoList();

        _bcName = busInfoList.get(0).bcName;
        _busCode = busInfoList.get(0).busCode;
        _busDriveSeq = PreferenceUtil.getDriveSeq(mContext);
        _phoneNumber = PreferenceUtil.getPhoneNumber(mContext);
    }

    @Override
    void initView() {
        initData();
        mRecyclerRoute = findViewById(R.id.recycler_bus_route);
        mTvListEmpty = findViewById(R.id.tv_route_list_empty);
        mTvBcName = findViewById(R.id.tv_bc_name);
        mTvBusInfo = findViewById(R.id.tv_bus_info);

        mProgress = findViewById(R.id.progress);

        mTvBcName.setText(_bcName);
        mTvBusInfo.setText(_phoneNumber);

        setRecycler();
        requestDriveStart();
    }
    BusRouteData item;
    BusRouteData item2;
    BusRouteData item3;
    private void setRecycler(){

//        item = new BusRouteData();
//        item.bpName = "aaaaaa1";
//        item.bpCode = "aaaaaa2";
//        item.isArrive = "N";
//        item.isSuccess = true;
//        item.setClickable = false;
//
//        item2 = new BusRouteData();
//        item2.bpName = "bbbbbbb1";
//        item2.bpCode = "bbbbbbb2";
//        item2.isArrive = "N";
//        item2.isSuccess = true;
//        item2.setClickable = false;
//
//        item3 = new BusRouteData();
//        item3.bpName = "aaaaaa1";
//        item3.bpCode = "aaaaaa2";
//        item3.isArrive = "N";
//        item3.isSuccess = true;
//        item3.setClickable = true;
//
//        mList.add(item3);
//        mList.add(item2);
//        mList.add(item);
//        mList.add(item);
//        mList.add(item);

        mAdapter = new BusRouteListAdapter(mContext, mList, this::clickArrive);
        mRecyclerRoute.setAdapter(mAdapter);
    }

    private void clickArrive(ArrayList<BusRouteData> item, int position){
        if (mList.get(position).setClickable){
            if (mList.size() - 1 == position) requestBusStop(item, position, NOT_DRIVE);
            else requestBusStop(item, position, DRIVE);
        }else{
            Toast.makeText(mContext, R.string.bus_route_impossible_click, Toast.LENGTH_SHORT).show();
        }
    }

    private void requestDriveStart(){

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
                        PreferenceUtil.setDriveSeq(mContext, 0);

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
                    LogMgr.e(TAG, "requestDriveStart() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_start_server_fail, Toast.LENGTH_SHORT).show();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                    hideProgressDialog();

                }
            });
        }
    }

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
                                Toast.makeText(mContext, R.string.bus_route_finish, Toast.LENGTH_SHORT).show();

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

                        }
                    }
                    //hideProgressDialog();
                    mProgress.setVisibility(View.GONE);
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
}