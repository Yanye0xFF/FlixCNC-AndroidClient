package com.yanye.flixcnc.adapter;

import android.animation.TimeInterpolator;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.handler.ItemTouchListener;
import com.yanye.flixcnc.model.DpadItem;

import java.util.List;

public class DpadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DpadItem> dataSet;
    private ItemTouchListener listener = null;

    private TimeInterpolator interpolator= new DecelerateInterpolator();
    private static final float scale = 0.8f;
    private static final int duration = 150;

    public DpadAdapter(List<DpadItem> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dpad, parent, false);
        return new DpadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof DpadViewHolder) {
            DpadItem item = dataSet.get(position);
            DpadViewHolder viewHolder = (DpadViewHolder)holder;

            if(item.getResId() != 0) {
                viewHolder.icon.setImageResource(item.getResId());
            }
            if(listener != null && holder.itemView.getTag() == null){
                holder.itemView.setTag(200);
                viewHolder.itemView.setOnTouchListener((View view, MotionEvent event) -> {

                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        view.animate().scaleX(scale).scaleY(scale).setDuration(duration).setInterpolator(interpolator);
                        view.setPressed(true);
                    }else if(event.getAction() == MotionEvent.ACTION_MOVE) {
                        float x = event.getX();
                        float y = event.getY();
                        boolean isInside = (x > 0 && x < view.getWidth() && y > 0 && y < view.getHeight());
                        if(view.isPressed() != isInside) {
                            view.setPressed(isInside);
                        }
                    }else if(event.getAction() == MotionEvent.ACTION_CANCEL) {
                        view.setPressed(false);
                        view.animate().scaleX(1).scaleY(1).setInterpolator(interpolator);
                    }else if(event.getAction() == MotionEvent.ACTION_UP) {
                        view.animate().scaleX(1).scaleY(1).setInterpolator(interpolator);
                        if (view.isPressed()) {
                            view.setPressed(false);
                            view.performClick();
                        }

                    }
                    return listener.onItemTouched(holder.getAdapterPosition(), view, event);

                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public DpadItem getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setItemTouchListener(ItemTouchListener listener) {
        this.listener = listener;
    }



    private static class DpadViewHolder extends ViewHolder {
        private ImageView icon;
        private DpadViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_dpad_icon);
        }
    }

}

