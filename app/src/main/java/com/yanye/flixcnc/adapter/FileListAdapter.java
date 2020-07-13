package com.yanye.flixcnc.adapter;

import android.annotation.SuppressLint;
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
import com.yanye.flixcnc.model.FileItem;
import com.yanye.flixcnc.utils.Misc;

import java.util.List;
import java.util.Locale;

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FileItem> dataSet;
    private ItemClickListener listener = null;

    public FileListAdapter(List<FileItem> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(FileItem.TYPE_OPERATOR == viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_operator, parent, false);
            return new OperatorHolder(view);
        }else if(FileItem.TYPE_PLACE_HOLDER == viewType) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_holder, parent, false);
            return new EmptyFolderHolder(view);
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_sub, parent, false);
            return new FileViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        FileItem item = dataSet.get(position);

        if(FileItem.TYPE_OPERATOR == item.getViewType()) {
            OperatorHolder viewHolder = (OperatorHolder)holder;
            viewHolder.image.setImageResource(item.getResId());
            viewHolder.title.setText(item.getFileName());

            if(listener != null && holder.itemView.getTag() == null){
                holder.itemView.setTag(200);
                holder.itemView.setOnClickListener((View view) ->
                    listener.onItemClicked(holder.getAdapterPosition(), 100));
            }

        }else if(FileItem.TYPE_FILE == item.getViewType()) {
            FileViewHolder viewHolder = (FileViewHolder)holder;
            viewHolder.name.setText(item.getFileName());
            viewHolder.icon.setImageResource(item.getResId());
            if(item.isFile()) {
                viewHolder.detail.setText("文件大小: " + Misc.generateReadableFileSize(item.getSize()));
            }else {
                viewHolder.detail.setText(String.format(Locale.CHINA, "总计%d个文件及文件夹", item.getSize()));
            }
            if(listener != null && holder.itemView.getTag() == null){
                holder.itemView.setTag(200);
                holder.itemView.setOnClickListener((View view) ->
                    listener.onItemClicked(holder.getAdapterPosition(), 200));
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public FileItem getItem(int position) {
        return dataSet.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position).getViewType();
    }


    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private static class FileViewHolder extends ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView detail;
        private FileViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_file_type);
            name = itemView.findViewById(R.id.tv_file_name);
            detail = itemView.findViewById(R.id.tv_detail);
        }
    }

    private static class EmptyFolderHolder extends ViewHolder {
        public EmptyFolderHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    private static class OperatorHolder extends ViewHolder {

        private ImageView image;
        private TextView title;

        public OperatorHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ic_operator_icon);
            title = itemView.findViewById(R.id.tv_operator_title);
        }
    }


}

