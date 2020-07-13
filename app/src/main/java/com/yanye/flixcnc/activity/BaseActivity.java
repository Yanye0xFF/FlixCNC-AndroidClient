package com.yanye.flixcnc.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import com.yanye.flixcnc.R;


public class BaseActivity extends AppCompatActivity {

    public BaseActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置状态栏透明，底栏(虚拟按键)颜色与colorAccent一致
        Window window = getWindow();
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);

    }

    // 布局组件初始化
    public void initView(){

    }

    // 参数初始化
    public void initParam(){

    }
}
