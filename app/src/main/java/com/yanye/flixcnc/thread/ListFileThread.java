package com.yanye.flixcnc.thread;

import android.os.Handler;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.model.FileItem;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListFileThread extends Thread {

    private String path;
    private List<FileItem> dest;
    private Handler handler;

    public ListFileThread(String path, List<FileItem> dest, Handler handler) {
        this.path = path;
        this.dest = dest;
        this.handler = handler;
    }

    @Override
    public void run() {
        File directory = new File(path);
        File[] files = directory.listFiles();

        if(files != null && files.length > 0) {

            List<File> fileList = Arrays.asList(files);
            Collections.sort(fileList, (File o1, File o2) -> {
                if(o1.isDirectory() && o2.isFile()) {
                    return -1;
                }
                if(o1.isFile() && o2.isDirectory()) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            });

            for(File file : files) {
                if(file.getName().charAt(0) == '.') {
                    continue;
                }
                if(file.isDirectory()) {
                    dest.add(new FileItem(FileItem.TYPE_FILE, file.getName(), file.getAbsolutePath(),
                            false, file.listFiles().length, R.mipmap.ic_folder));
                }else {
                    String fname = file.getName();
                    dest.add(new FileItem(FileItem.TYPE_FILE, fname, file.getAbsolutePath(),
                            true, file.length(), getFileIcon(fname)));
                }
            }
            handler.sendEmptyMessage(dest.isEmpty() ? 100 : 200);
            return;
        }
        handler.sendEmptyMessage(100);
    }

    private int getFileIcon(String fileName) {
        // 音频 图片 pdf 文本 文档 压缩
        final String[] suffix = new String[]{".mp3",".flac",".wav",".bmp",".jpg",".png",".pdf",
                ".txt",".gcode",".iso",".doc",".docx",".zip",".rar",".tar",".gz"};
        final int[] keys = new int[]{2, 5, 7, 9, 12, 16};
        final int[] mipmaps = new int[]{R.mipmap.ic_file_music, R.mipmap.ic_file_image, R.mipmap.ic_file_paf,
                R.mipmap.ic_file_text, R.mipmap.ic_file_word, R.mipmap.ic_file_zip, R.mipmap.ic_file_unknown};

        int match = 0, index;
        for(; match < suffix.length; match++) {
            if(fileName.endsWith(suffix[match])) {
                for(index = 0; index < keys.length; index++) {
                    if(match < keys[index]) {
                        return mipmaps[index];
                    }
                }
            }
        }
        return mipmaps[keys.length];
    }

}
