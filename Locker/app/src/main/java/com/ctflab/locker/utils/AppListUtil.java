package com.ctflab.locker.utils;

import android.content.Context;

import com.ctflab.locker.common.AppInfoEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wuwei on 2016/1/7.
 */
public class AppListUtil {
    private static String PERSET_FILENAME = "packagelist.perset";

    public static boolean IS_LOADING = false;

    private static IAppLoadListener mListener;

    public static ArrayList<AppInfoEntity> appInfoEntities = new ArrayList<>();

    public static ArrayList<String> locksAppList = new ArrayList<>();

//    public static void setIAppLoadListener(IAppLoadListener listener) {
//        mListener = listener;
//    }

    public static ArrayList<String> getPersetList(Context context) {
        ArrayList<String> persetList = new ArrayList<>();
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(PERSET_FILENAME));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
//            String Result="";
            while ((line = bufReader.readLine()) != null)
                persetList.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return persetList;
    }

    public static void getInstalledAppList(final Context context, IAppLoadListener listener) {
        mListener = listener;
        if (IS_LOADING) {
            if (mListener != null) {
                mListener.onLoading();
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                IS_LOADING = true;
                if (mListener != null) {
                    mListener.onLoading();
                }
                appInfoEntities = SystemUtil.getSysAppList(context);
                ArrayList<String> persets = getPersetList(context);
                locksAppList = getLockedPackageList(context);

                for (AppInfoEntity app : appInfoEntities) {

                    if (persets.contains(app.packgeName)) {
                        app.isPerset = true;
                    }

                    if (locksAppList.contains(app.packgeName)) {
                        app.isLoacked = true;
                    }
                    //Log.e("Package", app.packgeName);
                }

                Collections.sort(appInfoEntities);
                if (mListener != null) {
                    if (appInfoEntities.size() > 0) {
                        mListener.onLoadSuccessed();
                    } else {
                        mListener.onLoadFailed();
                    }
                }
                IS_LOADING = false;
            }
        }).run();
    }

    public static ArrayList<String> getLockedPackageList(Context context) {
        LockDB lockDB = new LockDB(context);
        return lockDB.getAllLockedApp();
    }


    public interface IAppLoadListener {
        public void onLoadSuccessed();

        public void onLoadFailed();

        public void onLoading();
    }
}
