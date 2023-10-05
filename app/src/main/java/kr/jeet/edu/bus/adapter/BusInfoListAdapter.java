package kr.jeet.edu.bus.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.utils.Utils;

public class BusInfoListAdapter extends RecyclerView.Adapter<BusInfoListAdapter.ViewHolder>{

    public interface PossibleListener{ public void possibleDrive(BusInfoData item);}
    public interface ImpossibleListener{ public void impossibleDrive();}

    private Context mContext;
    private List<BusInfoData> mList;
    private PossibleListener _possibleListener;
    private ImpossibleListener _impossibleListener;

    public BusInfoListAdapter(Context mContext, List<BusInfoData> mList, PossibleListener _possibleListener, ImpossibleListener _impossibleListener) {
        this.mContext = mContext;
        this.mList = mList;
        this._possibleListener = _possibleListener;
        this._impossibleListener = _impossibleListener;
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

        if (!item.bcName.contains("캠퍼스")) holder.tvBcName.setText(Utils.getStr(item.bcName+"캠퍼스"));
        else holder.tvBcName.setText(Utils.getStr(item.bcName));

        holder.tvBusName.setText(Utils.getStr(item.busName));
        holder.tvPhoneNum.setText(Utils.getStr(item.busPhoneNumber));

        if (item.busDriveSeq == Constants.NOT_DRIVE) {
            holder.btnDrive.setText(mContext.getString(R.string.btn_start_drive));
            holder.btnDrive.setOnClickListener(v -> _impossibleListener.impossibleDrive());
        } else {
            holder.btnDrive.setText(mContext.getString(R.string.btn_go_driving));
            holder.btnDrive.setOnClickListener(v -> {if (mList.size() > 0) _possibleListener.possibleDrive(item);});
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvBcName, tvBusName, tvPhoneNum;
        private AppCompatButton btnDrive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBcName = itemView.findViewById(R.id.tv_bc_name);
            tvBusName = itemView.findViewById(R.id.tv_bus_name);
            tvPhoneNum = itemView.findViewById(R.id.tv_phone_number);
            btnDrive = itemView.findViewById(R.id.btn_start_drive);
        }
    }
}
