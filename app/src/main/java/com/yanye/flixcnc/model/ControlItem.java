package com.yanye.flixcnc.model;

public class ControlItem {

    private String title;
    private String subTitle;
    private int resId;
    private int titleColor;

    public ControlItem() {
    }

    public ControlItem(String title, String subTitle, int resId) {
        this(title, subTitle, resId, 0xFF000000);
    }

    public ControlItem(String title, String subTitle, int resId, int titleColor) {
        this.title = title;
        this.subTitle = subTitle;
        this.resId = resId;
        this.titleColor = titleColor;
    }

    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return this.subTitle;
    }
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getResId() {
        return this.resId;
    }
    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getTitleColor() {
        return titleColor;
    }
    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }
}
