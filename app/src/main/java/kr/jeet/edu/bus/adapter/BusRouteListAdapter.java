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
import android.widget.ProgressBar;
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusRouteData item = mList.get(position);

        holder.tvBpName.setText(Utils.getStr(item.bpName));

        if (item.isLoading) holder.progressBar.setVisibility(View.VISIBLE);
        else holder.progressBar.setVisibility(View.GONE);

        if (!item.setClickable) {

            LogMgr.e("EVENT1", position+"");
            if (position == mList.size() - 1) Glide.with(mContext).load(R.drawable.icon_bus_garage_yet).into(holder.imgIconBus);
            else Glide.with(mContext).load(R.drawable.icon_bus_yet).into(holder.imgIconBus);
            holder.cbArrive.setOnClickListener(v -> {if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList, position);});
            holder.cbArrive.setTextColor(ContextCompat.getColorStateList(mContext, R.color.gray));
            holder.cbArrive.setBackgroundResource(R.drawable.bg_arrive_default);
            holder.cbArrive.setChecked(true);

        } else {
            holder.cbArrive.setOnClickListener(v -> {
                LogMgr.e("EVENT2", item.setClickable + "" + position + ", " + item.isSuccess);
                setImage(holder.cbArrive, holder.imgIconBus, item, position);
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList, position);
            });
            setImage(holder.cbArrive, holder.imgIconBus, item, position);
        }

        if (position == 0) setLine(holder.lineStart, holder.lineDriving, holder.lineEnd, View.VISIBLE, View.GONE, View.GONE);
        else if (position == mList.size() - 1) setLine(holder.lineStart, holder.lineDriving, holder.lineEnd, View.GONE, View.GONE, View.VISIBLE);
        else setLine(holder.lineStart, holder.lineDriving, holder.lineEnd, View.GONE, View.VISIBLE, View.GONE);

    }

    private void setLine(View start ,View driving , View end,int lineStart, int lineDriving, int lineEnd){
        start.setVisibility(lineStart);
        driving.setVisibility(lineDriving);
        end.setVisibility(lineEnd);
    }

    @SuppressLint("ResourceType")
    private void setImage(CheckBox cb, ImageView img, BusRouteData item, int position){
        if (item.isSuccess) setCheckImg(cb, img, position);
        else setDefaultImg(cb, img, R.drawable.selector_tv_arrive_check);
    }

    private void setCheckImg(CheckBox cb, ImageView img, int position){
        //Utils.animateImageChange(mContext, img, R.drawable.icon_bus_arrive);
        if (position == 0) Glide.with(mContext).load(R.drawable.icon_bus_garage_arrive).into(img);
        else Glide.with(mContext).load(R.drawable.icon_bus_arrive).into(img);
        cb.setChecked(false);
        cb.setOnClickListener(null);
        cb.setBackgroundResource(R.drawable.bg_arrive_checked);
        cb.setTextColor(ContextCompat.getColor(mContext, R.color.white));
    }

    private void setDefaultImg(CheckBox cb, ImageView img, int redId){
        //Utils.animateImageChange(mContext, img, R.drawable.icon_bus);
        Glide.with(mContext).load(R.drawable.icon_bus).into(img);
        cb.setBackgroundResource(R.drawable.bg_arrive_default);
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
        private View lineDriving, lineStart, lineEnd;
        private ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBpName = itemView.findViewById(R.id.tv_bp_name);
            imgIconBus = itemView.findViewById(R.id.img_icon_bus);
            cbArrive = itemView.findViewById(R.id.cb_arrive);
            lineDriving = itemView.findViewById(R.id.line_driving);
            lineStart = itemView.findViewById(R.id.line_start);
            lineEnd = itemView.findViewById(R.id.line_end);
            progressBar = itemView.findViewById(R.id.progress_bar);

//            itemView.setOnClickListener(v -> {
//                int position = getBindingAdapterPosition();
//                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
//            });
        }
    }
}
