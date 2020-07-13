package com.yanye.flixcnc.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yanye.flixcnc.R;


public class HeaderView extends RelativeLayout {

    private ImageView imageLeft, imageRight;
    private TextView title;

    public HeaderView(Context context) {
        super(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_header, this);
        title = findViewById(R.id.tv_header_title);

        imageLeft =  findViewById(R.id.iv_header_left);
        imageRight = findViewById(R.id.iv_header_right);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.HeaderView);
        String str = typedArray.getString(R.styleable.HeaderView_title);
        typedArray.recycle();
        title.setText(str);
    }

    public void setHeaderTitle(String str){
        title.setText(str);
    }

    public void setHeaderLeftClickListener(OnClickListener listener) {
        imageLeft.setOnClickListener(listener);
    }

    public void setHeaderRightClickListener(OnClickListener listener) {
        imageRight.setOnClickListener(listener);
    }

    public void setHeaderRightImage(int resId){
        imageRight.setVisibility(View.VISIBLE);
        imageRight.setImageResource(resId);
    }
}
