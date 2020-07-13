package com.yanye.flixcnc.adapter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class StatusBarItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpace;
    private int mStart;

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if(parent.getChildAdapterPosition(view) >= mStart) {
            outRect.right = mSpace;
        }
    }

    public StatusBarItemDecoration(int start, int space) {
        this.mSpace = space;
        this.mStart = start;
    }
}
