package kr.jeet.edu.bus.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.utils.PreferenceUtil;
import kr.jeet.edu.bus.utils.Utils;

public class BusInfoListAdapter extends RecyclerView.Adapter<BusInfoListAdapter.ViewHolder>{

    public interface DrivingListener{ public void driving(BusInfoData item, int position);}
    public interface DriveListener{ public void drive(BusInfoData item, int position);}

    private Context mContext;
    private List<BusInfoData> mList;
    private DrivingListener _drivingListener;
    private DriveListener _driveListener;
    private boolean startDrive = false;

    public BusInfoListAdapter(Context mContext, List<BusInfoData> mList, DrivingListener _drivingListener, DriveListener _driveListener, boolean startDrive) {
        this.mContext = mContext;
        this.mList = mList;
        this._drivingListener = _drivingListener;
        this._driveListener = _driveListener;
        this.startDrive = startDrive;
    }

    public BusInfoListAdapter(Context mContext, List<BusInfoData> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_bus_info_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        BusInfoData item = mList.get(position);
        String startDate = TextUtils.isEmpty(PreferenceUtil.getStartDate(mContext)) ? "출발시간 정보없음" : "출발시간 : " + PreferenceUtil.getStartDate(mContext);

        if (TextUtils.isEmpty(item.bcName)){
            holder.tvBcName.setText("");
        }else{
            if (!item.bcName.contains("캠퍼스")) holder.tvBcName.setText(Utils.getStr(item.bcName+"캠퍼스"));
            else holder.tvBcName.setText(Utils.getStr(item.bcName));
        }

        holder.tvBusName.setText(Utils.getStr(item.busName));
        holder.tvPhoneNum.setText(Utils.getStr(item.busPhoneNumber));

        if (item.busDriveSeq == Constants.NOT_DRIVING) {
            holder.btnDrive.setOnClickListener(v -> _driveListener.drive(item, position));
            setView(holder.btnDrive, holder.iconDrive, R.drawable.icon_bus_off, R.string.btn_start_drive);
            holder.tvStartDate.setVisibility(View.INVISIBLE);

        } else {
            holder.btnDrive.setOnClickListener(v -> _drivingListener.driving(item, position));
            setView(holder.btnDrive, holder.iconDrive, R.drawable.icon_bus_on, R.string.btn_go_driving);
            holder.tvStartDate.setText(startDate);
            holder.tvStartDate.setVisibility(View.VISIBLE);
        }
    }

    private void setView(AppCompatButton btn, ImageView img, int iconId, int strId){
        btn.setText(mContext.getString(strId));
        Glide.with(mContext).load(iconId).into(img);
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvBcName, tvBusName, tvPhoneNum, tvStartDate;
        private AppCompatButton btnDrive;
        private ImageView iconDrive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBcName = itemView.findViewById(R.id.tv_bc_name);
            tvBusName = itemView.findViewById(R.id.tv_bus_name);
            tvPhoneNum = itemView.findViewById(R.id.tv_phone_number);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            btnDrive = itemView.findViewById(R.id.btn_start_drive);
            iconDrive = itemView.findViewById(R.id.icon_drive);
        }
    }
}
