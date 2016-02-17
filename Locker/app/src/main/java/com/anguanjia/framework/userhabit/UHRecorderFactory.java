package com.anguanjia.framework.userhabit;

import android.content.Context;

public class UHRecorderFactory {

    //DBRecorder
    public static IUHRecorder createDBRecorder(Context context){
        return new UHRecorderDB(context);
    }
}
