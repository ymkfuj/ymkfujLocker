package com.ctflab.locker.services;

import android.content.Context;
import android.content.Intent;

import com.anguanjia.framework.components.AnguanjiaReceiver;
import com.anguanjia.framework.report.ReportRequestCache;
import com.anguanjia.framework.userhabit.UHAnalyticsReporter;
import com.anguanjia.framework.utils.NetwokUtil;
import com.ctflab.locker.net.AppListReport;
import com.ctflab.locker.services.pulse.PulseManager;

public class LockerReceiver extends AnguanjiaReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        PulseManager.pulseRequest(context);
        UHAnalyticsReporter.report(context);
        if (NetwokUtil.isWifiNetwork(context)){
            ReportRequestCache.uploadCache(context);
            AppListReport.report(context);
        }
    }
}
