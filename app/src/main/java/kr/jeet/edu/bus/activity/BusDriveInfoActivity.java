package kr.jeet.edu.bus.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.adapter.BusRouteListAdapter;
import kr.jeet.edu.bus.common.DataManager;
import kr.jeet.edu.bus.model.data.BusDriveSeqData;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.model.data.BusRouteData;
import kr.jeet.edu.bus.model.request.BusDriveRequest;
import kr.jeet.edu.bus.model.response.BaseResponse;
import kr.jeet.edu.bus.model.response.BusDriveResponse;
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
    private TextView mTvListEmpty;

    private List<BusInfoData> busInfoList;

    private BusRouteListAdapter mAdapter;
    private ArrayList<BusRouteData> mList = new ArrayList<>();

    private String _bcName = "";
    private int _busCode = -1;
    private int _busDriveSeq = -1;

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
    }

    @Override
    void initView() {
        initData();
        mRecyclerRoute = findViewById(R.id.recycler_bus_route);
        mTvListEmpty = findViewById(R.id.tv_route_list_empty);

        setRecycler();
        requestDriveStart();
    }

    private void setRecycler(){
        mAdapter = new BusRouteListAdapter(mContext, mList, this::clickArrive);
        mRecyclerRoute.setAdapter(mAdapter);
    }

    private void clickArrive(BusRouteData item, int position){
        if (mList.size() - 1 == position){
            requestBusStop(item.bpCode);
        }
    }

    private void requestDriveStart(){

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusRoute(_bcName, _busCode).enqueue(new Callback<BusRouteResponse>() {
                @Override
                public void onResponse(Call<BusRouteResponse> call, Response<BusRouteResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {
                            if (mList!=null && mList.size() > 0) mList.clear();

                            List<BusRouteData> getData = response.body().data;
                            if (getData != null){
                                if (mList != null) mList.addAll(getData);

                                Toast.makeText(mContext, R.string.drive_start, Toast.LENGTH_SHORT).show();
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
                }

                @Override
                public void onFailure(Call<BusRouteResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestDriveStart() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_start_server_fail, Toast.LENGTH_SHORT).show();
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    mTvListEmpty.setVisibility(mList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private void requestBusStop(String bpCode){

        String isFinish = "Y";

        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().getBusStop(_busDriveSeq, bpCode, isFinish).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if(response.isSuccessful()) {
                        if(response.body() != null) {


                        }

                    } else {

                        if (response.code() == RetrofitApi.RESPONSE_CODE_BINDING_ERROR){

                        }else if (response.code() == RetrofitApi.RESPONSE_CODE_NOT_FOUND){

                        }else{

                        }
                    }

                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    LogMgr.e(TAG, "requestBusStop() onFailure >> " + t.getMessage());
                    Toast.makeText(mContext, R.string.bus_stop_server_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}