package com.ctflab.locker;

import android.app.Application;
import android.content.Intent;

import com.anguanjia.framework.AnguanjiaFramewok;
import com.ctflab.locker.common.ReviveMain;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.PhoneInfoUtil;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.services.LockerService;
import com.ctflab.locker.utils.LanguageManager;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SystemUtil;
import com.tencent.bugly.crashreport.CrashReport;

public class LockerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ReviveMain.startDaemonProcess(this);
        AnguanjiaFramewok.init(this);
        initCrashReport();

        LanguageManager.changeLang(getBaseContext(), LanguageManager.loadLocaleString(this));
        PreferencesData.setPermissionUsable(SystemUtil.isUsageOpen(this));

        SystemUtil.startServiceSafeMode(this,new Intent(this, LockerService.class));
        if (SystemUtil.isGooglePatch(this))
            UHAnalytics.changeDataCount(UserHabitID.LM_11);
    }


//    public void changeLang(String lang) {
//        if (lang.equalsIgnoreCase(""))
//            return;
//        Locale myLocale = new Locale(lang);
//        LanguageManager.saveLocaleString(this, lang);
//        Locale.setDefault(myLocale);
//        android.content.res.Configuration config = new android.content.res.Configuration();
//        config.locale = myLocale;
//        getBaseContext().getResources().updateConfiguration(config,
//                getBaseContext().getResources().getDisplayMetrics());
//    }

    private void initCrashReport() {
        CrashReport.UserStrategy us = new CrashReport.UserStrategy(this);
        us.setAppChannel(AnguanjiaFramewok.getChannel());
        CrashReport.initCrashReport(this, "900016884", false, us);
        CrashReport.setUserId(PhoneInfoUtil.getExtId(this));
    }
}
