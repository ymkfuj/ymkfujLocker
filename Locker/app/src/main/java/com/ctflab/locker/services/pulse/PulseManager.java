package com.ctflab.locker.services.pulse;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.anguanjia.framework.utils.MyLog;
import com.ctflab.locker.services.LockerReceiver;

public class PulseManager {
    static long PULSE_SUCCESS_TIMESTAMP = 0;
    private static final int PULSE_SUCCESS_INTERVAL = 240 * 60 * 1000;

    public static void startPulseCounter(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, 60 * 1000, PULSE_SUCCESS_INTERVAL, PendingIntent.getBroadcast(context, 0, new Intent(context, LockerReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void pulseRequest(Context context){
        MyLog.e("pulse", "PulseManager.pulseRequest");
        if (System.currentTimeMillis() - PULSE_SUCCESS_TIMESTAMP > PULSE_SUCCESS_INTERVAL) {
            new PulseRequest().commit(context);
        }
    }

    static void pulseCount(){
        PulseManager.PULSE_SUCCESS_TIMESTAMP = System.currentTimeMillis();
    }
}
