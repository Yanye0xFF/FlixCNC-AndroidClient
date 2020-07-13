package com.yanye.flixcnc.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ToolBarItemDecoration extends RecyclerView.ItemDecoration {

    private Paint mPaint;

    public ToolBarItemDecoration() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFFfffef9);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        canvas.save();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = parent.getChildAt(i);
            canvas.drawLine(child.getLeft(), child.getTop() + 20,
                    child.getLeft(), child.getBottom() - 20, mPaint);
        }
        canvas.restore();
    }
}
