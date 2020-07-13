package com.yanye.flixcnc.handler;

import android.view.MotionEvent;
import android.view.View;

@FunctionalInterface
public interface ItemTouchListener {
    boolean onItemTouched(int position, View view, MotionEvent event);
}
