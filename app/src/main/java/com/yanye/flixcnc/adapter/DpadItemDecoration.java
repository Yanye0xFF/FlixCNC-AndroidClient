package com.yanye.flixcnc.adapter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DpadItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;
    private int column;

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        // 每行最后一个元素不加right padding
        if(position >= 0 && (((position + 1) % column) != 0)) {
            outRect.right = mSpace;
        }
    }

    public DpadItemDecoration(int space, int column) {
        this.mSpace = space;
        this.column = column;
    }
}
