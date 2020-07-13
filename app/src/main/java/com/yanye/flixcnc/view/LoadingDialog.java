package com.yanye.flixcnc.view;
import android.content.Context;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.yanye.flixcnc.R;


public class LoadingDialog extends AppCompatDialog {

    private TextView tvMessage;

    public LoadingDialog(@NonNull Context context) {
        this(context, 0, null);
    }

    private LoadingDialog(Context context, int theme, View contentView) {
        super(context, theme == 0 ? R.style.DialogStyle : theme);
        View view = contentView;
        if (view == null) {
            view = View.inflate(context, R.layout.view_loading_dialog, null);
        }
        this.setContentView(view);
        LinearLayout parentLayout = view.findViewById(R.id.dialog_loading_view);
        tvMessage = view.findViewById(R.id.tipTextView);
        parentLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
    }

    /*
    private static int getMobileWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
    */
    /**
     * 弹出对话框并设置显示文本
     * @param str 显示的文本
     * */
    public void showMessage(String str) {
        tvMessage.setText(str);
        show();
    }

    /**
     *
     * */
    public void updateMessage(String msg) {
        tvMessage.setText(msg);
    }

}