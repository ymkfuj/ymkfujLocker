package com.anguanjia.framework;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.anguanjia.framework.net.RequestManager;
import com.anguanjia.framework.preferece.CommonPreference;
import com.anguanjia.framework.report.ReportRequestCache;
import com.anguanjia.framework.thread.ThreadPool;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.userhabit.UHRecorderFactory;
import com.anguanjia.framework.utils.MyLog;
import com.anguanjia.framework.utils.PackageInfoUtil;

public class AnguanjiaFramewok {
    public static Context mApplicationContext;
    private static boolean isDebug;
    private static String channel = "unknow";

    public static void init(Context context){
        mApplicationContext = context.getApplicationContext();
        try {
            ApplicationInfo appInfo = mApplicationContext.getPackageManager().getApplicationInfo(mApplicationContext.getPackageName(), PackageManager.GET_META_DATA);
            isDebug = appInfo.metaData.getBoolean("DEBUG");
            channel = appInfo.metaData.getString("CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        MyLog.setDebug(isDebug());

        RequestManager.init(channel.equalsIgnoreCase("test"));

        UHAnalytics.setRecorder(UHRecorderFactory.createDBRecorder(mApplicationContext));
        ThreadPool.mService.execute(new Runnable() {
            @Override
            public void run() {
                if(isNewVer()){
                    UHAnalytics.clear(System.currentTimeMillis());
                    ReportRequestCache.clear();
                }else{
                    UHAnalytics.load();
                }
            }
        });
    }

    public static String getChannel(){
        return channel;
    }

    public static boolean isDebug(){
        return isDebug;
    }

    private static boolean isNewVer(){
        int now = CommonPreference.getDefault(mApplicationContext).loadInt("cm_cur_ver", 0);
        int cur = PackageInfoUtil.getVersionCD(mApplicationContext);
        CommonPreference.getDefault(mApplicationContext).saveInt("cm_cur_ver", cur);
        return now != cur;
    }
}
