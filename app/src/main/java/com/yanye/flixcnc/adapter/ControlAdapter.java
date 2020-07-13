package com.yanye.flixcnc.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.handler.ItemClickListener;
import com.yanye.flixcnc.model.ControlItem;

import java.util.List;

public class ControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ControlItem> dataSet;
    private ItemClickListener listener = null;

    public ControlAdapter(List<ControlItem> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_control, parent, false);
        return new ControlHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        ControlItem item = dataSet.get(position);

        ControlHolder viewHolder = (ControlHolder)holder;
        viewHolder.image.setImageResource(item.getResId());
        viewHolder.title.setText(item.getTitle());
        viewHolder.title.setTextColor(item.getTitleColor());
        viewHolder.subTitle.setText(item.getSubTitle());

        if(listener != null && holder.itemView.getTag() == null) {
            holder.itemView.setTag(200);
            viewHolder.itemView.setOnClickListener((View view) ->
                    listener.onItemClicked(holder.getAdapterPosition(), 0));
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private static class ControlHolder extends ViewHolder {
        private ImageView image;
        private TextView title;
        private TextView subTitle;
        private ControlHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_control_image);
            title = itemView.findViewById(R.id.tv_control_title);
            subTitle = itemView.findViewById(R.id.tv_control_subtitle);
        }
    }

}

