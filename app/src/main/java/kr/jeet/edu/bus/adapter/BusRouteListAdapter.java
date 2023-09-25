package kr.jeet.edu.bus.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.annotation.SuppressLint;
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
import kr.jeet.edu.bus.utils.LogMgr;
import kr.jeet.edu.bus.utils.Utils;
import kr.jeet.edu.bus.view.DrawableAlwaysCrossFadeFactory;

public class BusRouteListAdapter extends RecyclerView.Adapter<BusRouteListAdapter.ViewHolder>{

    public interface ItemClickListener{ public void onItemClick(ArrayList<BusRouteData> item, int position); }

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

//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        BusRouteData item = mList.get(position);
//
//        LogMgr.e("clickable: " + item.setClickable);
//
//        holder.tvBpName.setText(Utils.getStr(item.bpName));
//        setImage(holder.cbArrive, holder.imgIconBus, false, position);
//
//        if (!item.setClickable){
//            holder.cbArrive.setOnClickListener(null);
//            holder.cbArrive.setTextColor(ContextCompat.getColorStateList(mContext, R.color.gray));
//            holder.cbArrive.setBackgroundResource(R.drawable.bg_arrive_default);
//        }else{
//
//            holder.cbArrive.setOnClickListener(v -> {
//                setImage(holder.cbArrive, holder.imgIconBus, true, position);
//                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position), position);
//            });
//        }
//    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusRouteData item = mList.get(position);

        holder.tvBpName.setText(Utils.getStr(item.bpName));

        if (!item.setClickable) {

            LogMgr.e("EVENT1");
            holder.cbArrive.setOnClickListener(v -> {if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList, position);});
            holder.cbArrive.setTextColor(ContextCompat.getColorStateList(mContext, R.color.gray));
            holder.cbArrive.setBackgroundResource(R.drawable.bg_arrive_default);
            holder.cbArrive.setChecked(true);

        } else {
            holder.cbArrive.setOnClickListener(v -> {
                LogMgr.e("EVENT2", item.setClickable + "" + position + ", " + item.isSuccess);
                if (item.isSuccess) {

                    setImage(holder.cbArrive, holder.imgIconBus, true, item);
                } else {

                    holder.cbArrive.setTextColor(ContextCompat.getColorStateList(mContext, R.color.gray));
                    holder.cbArrive.setBackgroundResource(R.drawable.bg_arrive_default);
                    holder.cbArrive.setChecked(true);
                }

                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList, position);
            });
        }

        setImage(holder.cbArrive, holder.imgIconBus, false, item);
    }

    @SuppressLint("ResourceType")
    private void setImage(CheckBox cb, ImageView img, boolean isChanged, BusRouteData item){
        if (isChanged) {
            if (cb.isChecked()) setCheckImg(cb, img);
            else setDefaultImg(cb, img, R.drawable.selector_tv_arrive_check);

        } else {
            if (cb.isChecked()) {
                if (item.isSuccess) setCheckImg(cb, img);
                else if (!item.setClickable) Glide.with(mContext).load(R.drawable.icon_bus).into(img);

            } else {
                if (item.isSuccess) setCheckImg(cb, img);
                else setDefaultImg(cb, img, R.drawable.selector_tv_arrive_check);
            }
        }
    }

    private void setCheckImg(CheckBox cb, ImageView img){
        //Utils.animateImageChange(mContext, img, R.drawable.icon_bus_arrive);
        Glide.with(mContext).load(R.drawable.icon_bus_arrive).into(img);
        cb.setChecked(false);
        cb.setOnClickListener(null);
        cb.setBackgroundResource(R.drawable.bg_arrive_checked);
        cb.setTextColor(ContextCompat.getColor(mContext, R.color.white));
    }

    private void setDefaultImg(CheckBox cb, ImageView img, int redId){
        //Utils.animateImageChange(mContext, img, R.drawable.icon_bus);
        Glide.with(mContext).load(R.drawable.icon_bus).into(img);
        cb.setBackgroundResource(R.drawable.selector_arrive_check);
        cb.setTextColor(ContextCompat.getColor(mContext, redId));
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
