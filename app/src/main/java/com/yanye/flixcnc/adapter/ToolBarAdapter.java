package com.yanye.flixcnc.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.handler.ItemClickListener;

import java.util.List;

public class ToolBarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> dataSet;
    private ItemClickListener listener = null;

    public ToolBarAdapter(List<String> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tool_bar, parent, false);
        return new ToolHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        ToolHolder viewHolder = (ToolHolder)holder;
        viewHolder.title.setText(dataSet.get(position));

        if(listener != null && holder.itemView.getTag() == null){
            holder.itemView.setTag(200);
            holder.itemView.setOnClickListener((View view) -> {
                listener.onItemClicked(holder.getAdapterPosition(), 0);
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public String getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private static class ToolHolder extends ViewHolder {
        private TextView title;
        private ToolHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_tool_name);
        }
    }

}

