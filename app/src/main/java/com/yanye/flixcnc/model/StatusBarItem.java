package com.yanye.flixcnc.model;

import android.graphics.Color;

public class StatusBarItem {

    private int resId;
    private int padding;
    private String title;
    private int color;

    public StatusBarItem() {
    }

    public StatusBarItem(int resId, String title, int padding) {
        this.resId = resId;
        this.padding = padding;
        this.title = title;
        this.color = Color.WHITE;
    }

    public void setColor(int color) {
        this.color = color;
    }
    public int getColor() {
        return color;
    }

    public int getResId() {
        return this.resId;
    }
    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getPadding() {
        return this.padding;
    }
    public void setPadding(int padding) {
        this.padding = padding;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

}
