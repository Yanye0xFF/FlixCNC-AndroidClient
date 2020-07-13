package com.yanye.flixcnc.utils;

public class Constant {

    public static final int STEPS_PER_MM = 800;
    public static final int STEPS_PER_CM = 8000;

    public static final String CONTROLLER_NAME = "droid002";

    public static final int CONNECTION_TAG = 100;
    public static final int HOMING_TAG = 101;

    public static final int QUERY_MACHINE_STATE_RESULT = 0x2;
    public static final int QUERY_SPINDLE_STATE_RESULT = 0x3;

    public static final int MOTION_FINISHED_RESULT = 0x30;
    public static final int MOTION_PROGRESS_RESULT = 0x31;

    public static final int SET_WORKHOME_RESULT = 0x11;
}
