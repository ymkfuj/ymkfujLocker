package com.anguanjia.framework.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetwokUtil {
    public static ConnectivityManager getConnectivityManager(Context context) {
        ConnectivityManager connectivityManager = null;

        try {
            connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return connectivityManager;
    }

    public static boolean isNetworkAvaliable(Context e) {
        try {
            ConnectivityManager manager = getConnectivityManager(e);
            if(manager != null) {
                NetworkInfo[] info = manager.getAllNetworkInfo();
                if(info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    public static int getNetworkType(Context context){
        ConnectivityManager manager = getConnectivityManager(context);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null ? info.getType() : -1;
        }else{
            return -1;
        }
    }

    public static String getNetworkTypeName(Context context){
        ConnectivityManager manager = getConnectivityManager(context);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE){
                    return info.getSubtypeName();
                }else{
                    return info.getTypeName();
                }
            }
        }
        return "UNKNOWN";
    }

    public static boolean isWifiNetwork(Context context){
        int type = getNetworkType(context);
        return type == ConnectivityManager.TYPE_WIFI;
    }
}
