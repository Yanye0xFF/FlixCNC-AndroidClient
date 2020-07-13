package com.yanye.flixcnc.utils;

import android.app.Activity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Misc {

    public static String formatTime(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        return formatter.format(new Date(date));
    }

    public static boolean isStrEmpty(String str) {
        if(str == null) {
            return  true;
        }
        return str.isEmpty();
    }

    public static boolean isStrOk(String str) {
        if(str == null) {
            return false;
        }
        return !str.isEmpty();
    }


    public static String generateReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String trimFileName(String filePath) {
        int index = filePath.lastIndexOf('/') + 1;
        return filePath.substring(index, filePath.length());
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * byte转int，小端模式
     * @param b 4字节byte数组
     * @return int数据
     * */
    public static int bytes2Int(byte[] b) {
        return ((b[3] & 0xFF) << 24) | ((b[2] & 0xFF) << 16) | ((b[1] & 0xFF) << 8) | (b[0] & 0xFF);
    }

    public static short bytes2Short(byte[] b) {
        return  (short)(((b[1] & 0xFF) << 8) | (b[0] & 0xFF));
    }

    /**
     * int转byte，小端模式
     * @param a int数据
     * @return 4字节byte数组
     * */
    public static byte[] int2Bytes(int a) {
        return new byte[] {(byte)(a & 0xFF), (byte)((a >> 8) & 0xFF), (byte)((a >> 16) & 0xFF), (byte)((a >> 24) & 0xFF)};
    }

    /**
     * int转byte，小端模式
     * @param a int数据
     * @return 4字节byte数组
     * */
    public static byte[] short2Bytes(int a) {
        return new byte[] {(byte)(a & 0xFF), (byte)((a >> 8) & 0xFF)};
    }

    /**
     * double转byte数组，小端模式
     * @param doubleValue 8字节浮点数
     * @return 8字节byte数组
     * */
    public static byte[] double2Bytes(double doubleValue) {
        long value = Double.doubleToRawLongBits(doubleValue);
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) ((value >> (8 * i)) & 0xFF);
        }
        return bytes;
    }

    /**
     * byte数组转double，小端模式
     * @param array 8字节byte数组
     * @return 8字节浮点数
     * */
    public static double bytes2Double(byte[] array) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= (long) (array[i] << (8 * i));
        }
        return Double.longBitsToDouble(value);
    }
}
