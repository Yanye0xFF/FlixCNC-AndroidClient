package com.yanye.flixcnc.model;

public class FileItem {

    public static final int TYPE_OPERATOR = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_PLACE_HOLDER = 2;

    private int viewType;
    private String fileName;
    private String filePath;
    private boolean singleFile;
    private long size;
    private int resId;

    public FileItem() {

    }

    public FileItem(int viewType, String fileName, int resId) {
        this.viewType = viewType;
        this.fileName = fileName;
        this.resId = resId;
    }

    public FileItem(int viewType, String fileName, String filePath, boolean singleFile, long size, int resId) {
        this.viewType = viewType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.singleFile = singleFile;
        this.size = size;
        this.resId = resId;
    }

    public int getViewType() {
        return this.viewType;
    }
    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isFile() {
        return this.singleFile;
    }
    public void setFile(boolean isFile) {
        this.singleFile = isFile;
    }

    public long getSize() {
        return this.size;
    }
    public void setSize(long size) {
        this.size = size;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
    public int getResId() {
        return resId;
    }
}
