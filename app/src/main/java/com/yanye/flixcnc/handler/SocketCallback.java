package com.yanye.flixcnc.handler;

public interface SocketCallback {
    void onConnectResult(int type, String msg);
    void onSocketReceived(byte[] array);
}
