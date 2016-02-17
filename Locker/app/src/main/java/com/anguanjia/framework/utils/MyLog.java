package com.anguanjia.framework.utils;

import android.util.Log;

public class MyLog {

    private static boolean isDebug;

    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void i(String tag, String log) {
        if (isDebug())
            Log.i(tag, log);
    }

    public static void d(String tag, String log) {
        if (isDebug())
        Log.d(tag, log);
    }

    public static void e(String tag, String log) {
        if (isDebug())
        Log.e(tag, log);
    }

}
