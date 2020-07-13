package com.yanye.flixcnc.model;

public class ConnectionInfo {

    private String ip;
    private int port;

    public ConnectionInfo() {
    }

    public ConnectionInfo(String ip,int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return this.ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return this.port;
    }
    public void setPort(int port) {
        this.port = port;
    }
}
