package kr.jeet.edu.bus.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.transition.Transition;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.ViewPropertyTransition;

import java.util.ArrayList;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.model.data.BusRouteData;
import kr.jeet.edu.bus.utils.Utils;
import kr.jeet.edu.bus.view.DrawableAlwaysCrossFadeFactory;

public class BusRouteListAdapter extends RecyclerView.Adapter<BusRouteListAdapter.ViewHolder>{

    public interface ItemClickListener{ public void onItemClick(BusRouteData item, int position); }

    private Context mContext;
    private ArrayList<BusRouteData> mList;
    private ItemClickListener _listener;

    public BusRouteListAdapter(Context mContext, ArrayList<BusRouteData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_bus_route_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusRouteData item = mList.get(position);

        holder.tvBpName.setText(Utils.getStr(item.bpName));
        setImage(holder.cbArrive, holder.imgIconBus, false, position);

        holder.cbArrive.setOnClickListener(v -> {
            setImage(holder.cbArrive, holder.imgIconBus, true, position);
            if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position), position);
        });
    }

    private void setImage(CheckBox cb, ImageView img, boolean isChanged, int position){
        if (isChanged){
            if (cb.isChecked()) {
                Utils.animateImageChange(mContext, img, R.drawable.icon_bus_arrive);
                cb.setOnClickListener(null);
                cb.setBackgroundResource(R.drawable.bg_arrive_checked);
                cb.setTextColor(ContextCompat.getColor(mContext, R.color.white));

            } else {
                Utils.animateImageChange(mContext, img, R.drawable.icon_bus);
            }
        }else{
            if (cb.isChecked()) Glide.with(mContext).load(R.drawable.icon_bus_arrive).into(img);
            else Glide.with(mContext).load(R.drawable.icon_bus).into(img);
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvBpName;
        private ImageView imgIconBus;
        private CheckBox cbArrive;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBpName = itemView.findViewById(R.id.tv_bp_name);
            imgIconBus = itemView.findViewById(R.id.img_icon_bus);
            cbArrive = itemView.findViewById(R.id.cb_arrive);

//            itemView.setOnClickListener(v -> {
//                int position = getBindingAdapterPosition();
//                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
//            });
        }
    }
}
