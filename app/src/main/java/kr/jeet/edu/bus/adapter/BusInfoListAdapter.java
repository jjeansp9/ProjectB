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
import kr.jeet.edu.bus.model.data.BusInfoData;
import kr.jeet.edu.bus.utils.Utils;

public class BusInfoListAdapter extends RecyclerView.Adapter<BusInfoListAdapter.ViewHolder>{

    public interface ItemClickListener{ public void onItemClick(BusInfoData item); }

    private Context mContext;
    private List<BusInfoData> mList;
    private ItemClickListener _listener;

    public BusInfoListAdapter(Context mContext, List<BusInfoData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_bus_info_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusInfoData item = mList.get(position);

        if (!item.bcName.contains("캠퍼스")) holder.tvBcName.setText(Utils.getStr(item.bcName+"캠퍼스"));
        else holder.tvBcName.setText(Utils.getStr(item.bcName));

        holder.tvBusName.setText(Utils.getStr(item.busName));
        holder.tvPhoneNum.setText(Utils.getStr(item.busPhoneNumber));

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
            //btnDrive = itemView.findViewById(R.id.btn_start_drive);

            itemView.findViewById(R.id.btn_start_drive).setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
