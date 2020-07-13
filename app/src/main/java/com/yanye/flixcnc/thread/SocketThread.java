package com.yanye.flixcnc.thread;

import com.yanye.flixcnc.handler.SocketCallback;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketThread extends Thread {

    public static final int BUFFER_SIZE = 32;

    private String ip;
    private int port;

    private Socket socket;
    private DataOutputStream outputStream;
    private BufferedInputStream inputStream;
    private byte[] recvBuffer;

    private SocketCallback callback;

    public SocketThread(String ip, int port, SocketCallback callback) {
        this.ip = ip;
        this.port = port;
        this.callback = callback;
        recvBuffer = new byte[BUFFER_SIZE];
    }

    public void disconnect() {
        this.interrupt();
        try {
            if(outputStream != null) {
                outputStream.close();
            }
            if(inputStream != null) {
                inputStream.close();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            outputStream = null;
            inputStream = null;
            recvBuffer = null;
            socket = null;
        }
    }

    public void send(byte[] array, int length) {
        if(outputStream == null) {
            return;
        }
        try {
            outputStream.write(array, 0, length);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(Inet4Address.getByName(ip), port), 3000);
            socket.setSoTimeout(0);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new BufferedInputStream(socket.getInputStream());
        }catch (IOException e) {
            e.printStackTrace();
            // 连接失败
            callback.onConnectResult(100, e.getMessage());
            return;
        }
        // 连接成功
        callback.onConnectResult(200, null);

        int counter = 0, value;

        while(!isInterrupted()) {
            try {
                while(counter < BUFFER_SIZE) {
                    value = inputStream.read();
                    if(value != -1) {
                        recvBuffer[counter] = (byte)(value & 0xFF);
                        counter++;
                    }
                }
            }catch(IOException e) {
                e.printStackTrace();
                break;
            }
            // 固定大小32字节数据包回调
            callback.onSocketReceived(recvBuffer);
            counter = 0;
        }
        // 连接已断开
        callback.onConnectResult(300, null);
    }
}
