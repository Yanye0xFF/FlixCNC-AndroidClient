package com.yanye.flixcnc.thread;

public class QueryStateThread extends Thread {

    private int queryLevel;
    private FlixAgency flixAgency;

    public QueryStateThread(int queryLevel) {
        this.queryLevel = queryLevel;
        flixAgency = FlixAgency.getInstance();
    }

    @Override
    public void run() {
        flixAgency.queryMachineState();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if((--queryLevel) <= 0) {
            return;
        }

        flixAgency.querySpindleState();
    }
}
