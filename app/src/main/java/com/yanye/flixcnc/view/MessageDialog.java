package com.yanye.flixcnc.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.handler.ItemClickListener;


public class MessageDialog extends AppCompatDialog implements View.OnClickListener {

    public static final int SINGLE_BUTTON = 0;
    public static final int DOUBLE_BUTTON = 1;

    private TextView tvTitle, tvMessage;
    private TextView tvPositive, tvNegative;
    private int viewType;

    public static final int BUTTON_POSITIVE = 0;
    public static final int BUTTON_NEGATIVE = 1;

    private ItemClickListener listener;

    public MessageDialog(@NonNull Context context, int viewType) {
        super(context, R.style.DialogStyle);

        this.viewType = viewType;
        View contentView;
        LinearLayout parentLayout;

        if(viewType == SINGLE_BUTTON) {
            contentView = View.inflate(context, R.layout.view_message_dialog, null);
            parentLayout = contentView.findViewById(R.id.parent_layout);
            tvTitle = contentView.findViewById(R.id.tv_dialog_title);
            tvMessage = contentView.findViewById(R.id.tv_dialog_message);
            tvNegative = contentView.findViewById(R.id.tv_cancel);
            tvNegative.setOnClickListener(this);
        }else {
            contentView = View.inflate(context, R.layout.view_message_dialog2, null);
            parentLayout = contentView.findViewById(R.id.parent_layout2);
            tvTitle = contentView.findViewById(R.id.tv_title2);
            tvMessage = contentView.findViewById(R.id.tv_message2);
            tvPositive = contentView.findViewById(R.id.tv_positive);
            tvNegative = contentView.findViewById(R.id.tv_negative);
            tvPositive.setOnClickListener(this);
            tvNegative.setOnClickListener(this);
        }
        parentLayout.setLayoutParams(new FrameLayout.LayoutParams((int) (getMobileWidth(context) * 0.8),
                LayoutParams.WRAP_CONTENT));
        this.setContentView(contentView);
    }

    private int tag;
    public void setTag(int tag) {
        this.tag = tag;
    }
    public int getTag() {
        return tag;
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    private static int getMobileWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public void setDialogTitle(String title) {
        tvTitle.setText(title);
    }

    public void setDialogMessage(String message) {
        tvMessage.setText(message);
    }

    public void setButtonText(String text1, String text2) {
        if(viewType == SINGLE_BUTTON) {
            tvNegative.setText(text1);
        }else {
            tvPositive.setText(text1);
            tvNegative.setText(text2);
        }
    }

    public void setMessageColor(String color) {
        tvMessage.setTextColor(Color.parseColor(color));
    }

    @Override
    public void onClick(View view) {
        dismiss();
        if((view.getId() == R.id.tv_cancel) || (listener == null)) {
            return;
        }
        if(view.getId() == R.id.tv_positive) {
            listener.onItemClicked(BUTTON_POSITIVE, tag);
        }else if(view.getId() == R.id.tv_negative) {
            listener.onItemClicked(BUTTON_NEGATIVE, tag);
        }
    }
}
