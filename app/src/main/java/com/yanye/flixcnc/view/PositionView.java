package com.yanye.flixcnc.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yanye.flixcnc.R;

import java.text.DecimalFormat;


public class PositionView extends LinearLayout {

    private TextView tvRealX;
    private TextView tvRealY;
    private TextView tvRealZ;
    private TextView tvWorkX;
    private TextView tvWorkY;
    private TextView tvWorkZ;

    private static final int STEPS_PER_MM = 800;

    private DecimalFormat formatter;

    public PositionView(Context context) {
        super(context);
        initView(context);
    }

    public PositionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_position, this);

        tvRealX = findViewById(R.id.tv_real_x);
        tvRealY = findViewById(R.id.tv_real_y);
        tvRealZ = findViewById(R.id.tv_real_z);

        tvWorkX = findViewById(R.id.tv_work_x);
        tvWorkY = findViewById(R.id.tv_work_y);
        tvWorkZ = findViewById(R.id.tv_work_z);

        formatter = new DecimalFormat("000.00000");
    }

    private int xhome, yhome, zhome;

    @SuppressLint("SetTextI18n")
    public void setRealPosition(int xSteps, int ySteps, int zSteps) {
        double displayX = xSteps * 1.0D / STEPS_PER_MM;
        double displayY = ySteps * 1.0D / STEPS_PER_MM;
        double displayZ = zSteps * 1.0D / STEPS_PER_MM;

        tvRealX.setText("X轴: " + formatter.format(displayX));
        tvRealY.setText("Y轴: " + formatter.format(displayY));
        tvRealZ.setText("Z轴: " + formatter.format(displayZ));

        displayX = (xSteps - xhome) * 1.0D / STEPS_PER_MM;
        displayY = (ySteps - yhome) * 1.0D / STEPS_PER_MM;
        displayZ = (zSteps - zhome) * 1.0D / STEPS_PER_MM;

        tvWorkX.setText("X轴: " + formatter.format(displayX));
        tvWorkY.setText("Y轴: " + formatter.format(displayY));
        tvWorkZ.setText("Z轴: " + formatter.format(displayZ));
    }

    public void setWorkPosition(int xSteps, int ySteps, int zSteps) {
        xhome = xSteps;
        yhome = ySteps;
        zhome = zSteps;
    }

}
