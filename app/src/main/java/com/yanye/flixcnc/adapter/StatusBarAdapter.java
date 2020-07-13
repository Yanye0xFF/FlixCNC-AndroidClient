package com.yanye.flixcnc.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.handler.ItemClickListener;
import com.yanye.flixcnc.model.StatusBarItem;

import java.util.List;

public class StatusBarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<StatusBarItem> dataSet;
    private ItemClickListener listener = null;

    public StatusBarAdapter(List<StatusBarItem> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_status_bar, parent, false);
        return new TitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            StatusBarItem item = dataSet.get(position);
            int padding = item.getPadding();
            TitleViewHolder viewHolder = (TitleViewHolder)holder;

            viewHolder.icon.setImageResource(item.getResId());
            viewHolder.icon.setColorFilter(item.getColor());
            viewHolder.title.setText(item.getTitle());
            viewHolder.title.setTextColor(item.getColor());
            if(padding > 0) {
                viewHolder.icon.setPadding(padding, padding, padding, padding);
            }

            if(listener != null && holder.itemView.getTag() == null){
                holder.itemView.setTag(200);
                holder.itemView.setOnClickListener((View view) ->
                    listener.onItemClicked(holder.getAdapterPosition(), 0));
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public StatusBarItem getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private static class TitleViewHolder extends ViewHolder {
        private ImageView icon;
        private TextView title;
        private TitleViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_title_icon);
            title = itemView.findViewById(R.id.tv_title_text);
        }
    }

}

