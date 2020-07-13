package com.yanye.flixcnc.model;

public class DpadItem {

    private int resId;
    private int group;
    private byte axisId;
    private byte axisDir;

    public DpadItem() {
    }

    public DpadItem(int resId,int group,byte axisId,byte axisDir) {
        this.resId = resId;
        this.group = group;
        this.axisId = axisId;
        this.axisDir = axisDir;
    }

    public int getResId() {
        return this.resId;
    }
    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getGroup() {
        return this.group;
    }
    public void setGroup(int group) {
        this.group = group;
    }

    public byte getAxisId() {
        return this.axisId;
    }
    public void setAxisId(byte axisId) {
        this.axisId = axisId;
    }

    public byte getAxisDir() {
        return this.axisDir;
    }
    public void setAxisDir(byte axisDir) {
        this.axisDir = axisDir;
    }
}
