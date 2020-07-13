package com.yanye.flixcnc.thread;

import com.yanye.flixcnc.handler.SocketCallback;
import com.yanye.flixcnc.handler.StreamSubscriber;
import com.yanye.flixcnc.utils.Misc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlixAgency {

    public static final byte Major_Version = 0x2;
    public static final byte Minor_Version = 0x1;

    private static final int ARRAY_MAX = 16;

    private static FlixAgency instance;

    // 0:socket connect flag, 1:cncAck flag,
    private boolean[] systemState;
    public static final int SOCKET_BIT = 0;
    public static final int ACK_BIT = 1;

    private int[] spindleParams;

    private String cncName;
    private int hardwareVer, softwareVer;

    private static final int Max_Subscribers = 8;
    private List<StreamSubscriber> subscribers;

    private String hostIp;
    private int hostPort;
    private byte[] socketBuffer;
    private SocketThread socketThread;

    public static FlixAgency getInstance() {
        if(instance == null) {
            instance = new FlixAgency();
        }
        return instance;
    }

    private FlixAgency() {
        systemState = new boolean[ARRAY_MAX];
        spindleParams = new int[ARRAY_MAX];

        socketBuffer = new byte[SocketThread.BUFFER_SIZE];
        subscribers = new ArrayList<>(Max_Subscribers);
    }

    private SocketCallback socketCallback = new SocketCallback() {
        @Override
        public void onConnectResult(int type, String msg) {
            systemState[SOCKET_BIT] = (type == 200);
            for(StreamSubscriber sub : subscribers) {
                sub.onMessageReceived(type, msg);
            }
        }
        @Override
        public void onSocketReceived(byte[] array) {
            for(StreamSubscriber sub : subscribers) {
                sub.onDataReceived(array);
            }
        }
    };

    public void openConnection(String ip, int port) {
        this.hostIp = ip;
        this.hostPort = port;
        if(socketThread != null) {
            socketThread.disconnect();
            socketThread = null;
        }
        socketThread = new SocketThread(ip, port, socketCallback);
        socketThread.start();
    }

    public void closeConnection() {
        if(socketThread != null) {
            socketThread.disconnect();
            socketThread = null;
        }
        Arrays.fill(systemState, false);
    }

    public void updateSystemState(int position, boolean value) {
        if(position < 0 || (position >= ARRAY_MAX)) {
            return;
        }
        systemState[position] = value;
    }

    public boolean getSystemState(int position) {
        if(position < 0 || (position >= ARRAY_MAX)) {
            return false;
        }
        return systemState[position];
    }

    public void setMachineName(String name) {
        this.cncName = name;
    }

    public String getMachineName() {
        return cncName;
    }

    public int getHardwareVersion() {
        return hardwareVer;
    }

    public int getSoftwareVersion() {
        return softwareVer;
    }

    public void setMachineVersion(int hardVer, int softVer) {
        this.hardwareVer = hardVer;
        this.softwareVer = softVer;
    }

    public boolean registerSubscriber(StreamSubscriber subscriber) {
        if((subscriber != null) && (subscribers.size() < Max_Subscribers)) {
            subscribers.add(subscriber);
            return true;
        }
        return false;
    }

    public void unregisterSubscriber(StreamSubscriber subscriber) {
        if(subscriber != null) {
            subscribers.remove(subscriber);
        }
    }

    /**
     * 调用socketThread发送之前检查systemState中的标记位
     * @param length 缓冲区有效数据长度
     * @param  conditions 条件可变参
     * */
    private boolean socketSendEx(int length, int ... conditions) {
        int len = conditions.length;
        for(int i = 0; i < len; i++) {
            if(!systemState[conditions[i]]) {
                System.out.println("conditions not match");
                return false;
            }
        }
        socketThread.send(socketBuffer, length);
        return true;
    }

    /**
     * app发送建立连接包
     * @param controllerName app控制端名称, 仅支持最大9个ascii字符
     * @param isBeep 是否响铃
     * */
    public void sendAck(String controllerName, boolean isBeep) {
        if(!systemState[SOCKET_BIT]) {
            return;
        }
        byte[] nameArray = null;
        try {
            nameArray = controllerName.getBytes("ISO8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        int copySize = (nameArray.length > 9) ? 9 : nameArray.length;
        socketBuffer[0] = 0x00;
        socketBuffer[1] = Major_Version;
        socketBuffer[2] = Minor_Version;
        socketBuffer[3] = (isBeep ? (byte)0x1 : (byte)0x0);
        // 拷贝控制器名称+结束符
        System.arraycopy(nameArray, 0, socketBuffer, 4, copySize);
        socketBuffer[copySize + 4] = 0x00;
        socketThread.send(socketBuffer, 14);
    }

    /**
     * app发送断开连接包
     * @param isBeep 是否响铃
     * */
    public void sendNAck(boolean isBeep) {
        socketBuffer[0] = 0x01;
        socketBuffer[1] = (isBeep ? (byte)0x1 : (byte)0x0);
        if(socketSendEx(2, SOCKET_BIT, ACK_BIT)) {
            systemState[ACK_BIT] = false;
        }
    }

    /**
     * 查询雕刻机状态
     * */
    public void queryMachineState() {
        socketBuffer[0] = 0x02;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    /**
     * 查询主轴状态
     * */
    public void querySpindleState() {
        socketBuffer[0] = 0x03;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    public void setWorkHome() {
        socketBuffer[0] = 0x11;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    public void setSpindleSpeed(int lowSpeed, int highSpeed) {
        socketBuffer[0] = 0x17;
        byte[] arrayLow = Misc.short2Bytes(lowSpeed);
        byte[] arrayHigh = Misc.short2Bytes(highSpeed);
        System.arraycopy(arrayLow, 0, socketBuffer, 1, 2);
        System.arraycopy(arrayHigh, 0, socketBuffer, 3, 2);
        socketSendEx(5, SOCKET_BIT, ACK_BIT);
    }

    public void setVssTime(int time, boolean force) {
        socketBuffer[0] = 0x18;
        byte[] array = Misc.short2Bytes(time);
        System.arraycopy(array, 0, socketBuffer, 1, 2);
        socketBuffer[3] = (force ? (byte)0x1 : (byte)0x0);
        socketSendEx(4, SOCKET_BIT, ACK_BIT);
    }

    public void goMachineHome() {
        socketBuffer[0] = 0x40;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    public void goWorkHome() {
        socketBuffer[0] = 0x41;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    public void jogMotion(byte axisTag, byte axisDirs, int steps) {
        final int[] offset = new int[]{2, 6, 10};
        socketBuffer[0] = 0x42;
        socketBuffer[1] = axisTag;
        byte[] positiveArray = Misc.int2Bytes(steps);
        byte[] negativeArray = Misc.int2Bytes(-steps);
        for(int i = 0; i < 3; i++) {
            if(((axisTag >> i) & 0x1) == 1) {
                // 轴有效, 填充数据
                if(((axisDirs >> i) & 0x1) == 1) {
                    // axisDirs标识位 1为负方向
                    System.arraycopy(negativeArray, 0, socketBuffer, offset[i], 4);
                }else {
                    System.arraycopy(positiveArray, 0, socketBuffer, offset[i], 4);
                }
            }else {
                // 轴无效, 填充0
                Arrays.fill(socketBuffer, offset[i], (offset[i] + 4), (byte)0x00);
            }
        }
        socketSendEx(14, SOCKET_BIT, ACK_BIT);
    }

    public void startLongMotion(byte axisTag, byte axisDirs) {
        socketBuffer[0] = 0x43;
        socketBuffer[1] = axisTag;
        socketBuffer[2] = axisDirs;
        socketSendEx(3, SOCKET_BIT, ACK_BIT);
    }

    public void stopLongMotion(byte axisTag) {
        socketBuffer[0] = 0x44;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    public void switchSpindleDir(byte state) {
        socketBuffer[0] = 0x19;
        socketBuffer[1] = state;
        socketSendEx(2, SOCKET_BIT, ACK_BIT);
    }

    public void openSpindle(boolean isBeep) {
        socketBuffer[0] = 0x45;
        socketBuffer[1] = (isBeep ? (byte)0x1 : (byte)0x0);
        socketSendEx(2, SOCKET_BIT, ACK_BIT);
    }

    public void closeSpindle(boolean isBeep) {
        socketBuffer[0] = 0x46;
        socketBuffer[1] = (isBeep ? (byte)0x1 : (byte)0x0);
        socketSendEx(2, SOCKET_BIT, ACK_BIT);
    }

    public void sendFileInfo(int fileSize, int crc16, String fileName) {
        byte[] sizeBuffer = Misc.int2Bytes(fileSize);
        byte[] crc16Buffer = Misc.short2Bytes(crc16);
        socketBuffer[0] = 0x50;
        System.arraycopy(sizeBuffer, 0, socketBuffer, 1, 4);
        System.arraycopy(crc16Buffer, 0, socketBuffer, 5, 2);
        socketSendEx(7, SOCKET_BIT, ACK_BIT);
    }

    public void sendFile(byte[] data, int offset, int length) {
        socketBuffer[0] = 0x51;
        socketBuffer[1] = (byte)(length & 0xFF);
        System.arraycopy(data, offset, socketBuffer, 2, length);
        socketSendEx((length + 2), SOCKET_BIT, ACK_BIT);
    }

    public void sendParseGcode() {
        socketBuffer[0] = 0x52;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

    public void sendRunGcode() {
        socketBuffer[0] = 0x53;
        socketSendEx(1, SOCKET_BIT, ACK_BIT);
    }

}
