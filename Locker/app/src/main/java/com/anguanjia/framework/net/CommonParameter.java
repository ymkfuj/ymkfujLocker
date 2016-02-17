package com.anguanjia.framework.net;

import android.content.Context;
import android.os.Build;

import com.anguanjia.framework.AnguanjiaFramewok;
import com.anguanjia.framework.utils.NetwokUtil;
import com.anguanjia.framework.utils.PackageInfoUtil;
import com.anguanjia.framework.utils.PhoneInfoUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class CommonParameter {

    public static JSONObject build(Context context){
        JSONObject obj = new JSONObject();
        try {
            obj.put("product_model", Build.MODEL);
            obj.put("manufacturer", Build.MANUFACTURER);
            obj.put("sdk_version", ""+Build.VERSION.SDK_INT);
            obj.put("IMEI", PhoneInfoUtil.getIMEI(context));
            obj.put("vcode", ""+PackageInfoUtil.getVersionCD(context));
            obj.put("vname", ""+PackageInfoUtil.getVersionNM(context));
            obj.put("net_type", ""+NetwokUtil.getNetworkTypeName(context));
            obj.put("id_ext",PhoneInfoUtil.getExtId(context));
            obj.put("market_channel", AnguanjiaFramewok.getChannel());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
