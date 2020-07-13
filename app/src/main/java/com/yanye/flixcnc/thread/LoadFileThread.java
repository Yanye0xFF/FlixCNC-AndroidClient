package com.yanye.flixcnc.thread;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class LoadFileThread extends Thread {

    private File file;
    private StringBuilder builder;
    private Handler handler;

    public LoadFileThread(File file, StringBuilder builder, Handler handler) {
        this.file = file;
        this.builder = builder;
        this.handler = handler;
    }

    @Override
    public void run() {
        String line;
        BufferedReader buffer;
        try {
            buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            while((line = buffer.readLine()) != null) {
                builder.append(line).append("\n");
            }
            buffer.close();
        } catch (UnsupportedEncodingException e) {
            handler.sendEmptyMessage(101);
            return;
        } catch (FileNotFoundException e) {
            handler.sendEmptyMessage(100);
            return;
        } catch (IOException e) {
            handler.sendEmptyMessage(102);
            return;
        }
        handler.sendEmptyMessage(201);
    }
}
