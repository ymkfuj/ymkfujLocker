package com.anguanjia.framework.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageInfoUtil {
    /**
     * 获取自身的version name
     */
    public static String getVersionNM(Context aContext) {
        String vRet = null;
        if (aContext != null) {
            PackageManager vManager = aContext.getPackageManager();
            if (vManager != null) {
                try {
                    PackageInfo vInfo = vManager.getPackageInfo(aContext.getPackageName(), 0);
                    if (vInfo != null) {
                        vRet = vInfo.versionName;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (vRet == null) {
            vRet = "UNKNOWN";
        }
        return vRet;
    }

    /**
     * 获取自身的version code
     */
    public static int getVersionCD(Context aContext) {
        int vRet = 0;
        if (aContext != null) {
            PackageManager vManager = aContext.getPackageManager();
            if (vManager != null) {
                try {
                    PackageInfo vInfo = vManager.getPackageInfo(aContext.getPackageName(), 0);
                    if (vInfo != null) {
                        vRet = vInfo.versionCode;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return vRet;
    }
}
