package com.anguanjia.framework.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.anguanjia.framework.config.Const;
import com.anguanjia.framework.preferece.CommonPreference;

import java.util.UUID;

public class PhoneInfoUtil {

    private static String mImei;

    public static String getIMEI(Context context) {
        if(mImei == null) {
            try {
                TelephonyManager e = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                mImei = e.getDeviceId();
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        return mImei == null ? "UNKNOWN" : mImei;
    }

    public static String getExtId(Context aContext) {
        String vRet = CommonPreference.getInstance(aContext, Const.AGJ_COMMON_PREF).loadString(Const.key_device_id, "");
        if (vRet != null && vRet.length() > 0) {
            return vRet;
        }
        if (vRet == null || vRet.length() <= 0) {
            vRet = getMacAdd(aContext);
        }
        if (vRet == null || vRet.length() <= 0) {
            vRet = getBTMac();
        }
        if (vRet == null || vRet.length() <= 0) {
            if (Build.VERSION.SDK_INT >= 10) {
                vRet = Build.SERIAL;
                if (vRet != null && vRet.length() > 0) {
                    vRet = vRet + "-SN";
                }
            }
        }
        if (vRet == null || vRet.length() <= 0) {
            UUID vUuid = UUID.randomUUID();
            vRet = vUuid.toString();
            if (vRet.length() > 0) {
                vRet = vRet + "-UD";
            }
        }
        if (vRet.length() > 0) {
            CommonPreference.getInstance(aContext, Const.AGJ_COMMON_PREF).saveString(Const.key_device_id, vRet);
        } else {
            vRet = "UNKNOWN";
        }
        String imei = getIMEI(aContext);
        return imei+vRet;
    }

    public static String getBTMac() {
        String vRet = null;
        try {
            BluetoothAdapter vAdapter = BluetoothAdapter.getDefaultAdapter();
            if (vAdapter != null) {
                vRet = vAdapter.getAddress();
                if (vRet != null && vRet.length() > 0) {
                    vRet = vRet.replace(":", "") + "-BT";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vRet;
    }

    public static String getMacAdd(Context aContext) {
        String vRet = null;
        if (aContext != null) {
            try {
                WifiManager vWifiMgr = (WifiManager) aContext.getSystemService(Context.WIFI_SERVICE);
                if (vWifiMgr != null) {
                    WifiInfo vInfo = vWifiMgr.getConnectionInfo();
                    if (vInfo != null) {
                        vRet = vInfo.getMacAddress();
                        if (vRet != null && vRet.length() > 0) {
                            vRet = vRet.replace(":", "") + "-MA";
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return vRet;
    }

    public static String getDisplayMetrics(Context cx) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        WindowManager wm = ((WindowManager) cx
                .getSystemService(Context.WINDOW_SERVICE));
        wm.getDefaultDisplay().getMetrics(localDisplayMetrics);

        return localDisplayMetrics.widthPixels + "*"
                + localDisplayMetrics.heightPixels;
    }
}
