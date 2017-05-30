package com.study.mp3lame;

import android.util.Log;

public class LogUtil {

    private static final String TAG = "basePlatform";
    private static boolean isDebug = true;

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, buildMessage(msg));
    }

    public static void v(String msg, Throwable thr) {
        if (isDebug)
            Log.v(TAG, buildMessage(msg), thr);

    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, buildMessage(msg));

    }

    public static void d(String msg, Throwable thr) {
        if (isDebug)
            Log.d(TAG, buildMessage(msg), thr);
    }

    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, buildMessage(msg));
    }

    public static void i(String msg, Throwable thr) {
        if (isDebug)
            Log.i(TAG, buildMessage(msg), thr);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, buildMessage(msg));
    }


    public static void w(String msg) {
        if (isDebug)
            Log.w(TAG, buildMessage(msg));
    }

    public static void w(String msg, Throwable thr) {
        if (isDebug)
            Log.w(TAG, buildMessage(msg), thr);
    }

    public static void w(Throwable thr) {
        if (isDebug)
            Log.w(TAG, buildMessage(""), thr);
    }

    public static void e(String msg, Throwable thr) {
        if (isDebug)
            Log.e(TAG, buildMessage(msg), thr);
    }

    protected static String buildMessage(String msg) {
        StackTraceElement caller = new Throwable().fillInStackTrace()
                .getStackTrace()[2];
        return caller.toString() + "\n" + msg;
    }

}
