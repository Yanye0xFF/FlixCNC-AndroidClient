package com.yanye.flixcnc.handler;

public interface StreamSubscriber {
    void onMessageReceived(int type, String message);
    void onDataReceived(byte[] array);
}
