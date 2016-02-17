package com.ctflab.locker.model;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.anguanjia.framework.net.RequestException;
import com.anguanjia.framework.preferece.CommonPreference;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.MyLog;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.net.CheckUpdateRequest;
import com.ctflab.locker.utils.LanguageManager;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.widget.BottomAlertDialogBuilder;
import com.ctflab.locker.widget.LoadingDialog;

import org.json.JSONObject;

public class UpdateManager {

    public interface ICheckUpdateCallback {
        void onNewVersionFound(String ver, String desc);
    }

    public static void checkUpdateSilent(final Context context, final ICheckUpdateCallback callback) {
        if (System.currentTimeMillis() - getLastCheckUpdateTime(context) > 24 * 60 * 60 * 1000) {
            MyLog.e("update", "checkUpdateSilent");
            new CheckUpdateRequest(LanguageManager.loadLocaleString(context)) {

                @Override
                protected void onErrorResponse(RequestException e) {

                }

                @Override
                protected void parseNetworkResponse(JSONObject response) {
                    super.parseNetworkResponse(response);
                    setLastCheckUpdateTime(context);
                }

                @Override
                protected void newVersion(final String ver, final String des) {
                    callback.onNewVersionFound(ver, des);
                }
            }.commit(context);
        }
    }

    public static void checkUpdate(final Activity context) {
        final LoadingDialog loadingDialog = new LoadingDialog(context, context.getString(R.string.update_checking), false);
        loadingDialog.show();
        new CheckUpdateRequest(LanguageManager.loadLocaleString(context)) {

            @Override
            protected void onErrorResponse(RequestException e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        showUpToDateDialog(context);
                    }
                });
            }

            @Override
            protected void newVersion(final String ver, final String des) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        showNewVersionDialog(context, ver, des);
                    }
                });
            }
        }.commit(context);
    }

    public static void showUpToDateDialog(Context context) {
        BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(context);
        builder.setTitle(R.string.update_tips_title).setMessage(R.string.update_up_to_date);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).create().show();
    }

    public static void showNewVersionDialogInMain(Context context, String ver, String desc) {
        UHAnalytics.changeDataCount(UserHabitID.LM_34);
        MyLog.e("update", "showNewVersionDialogInMain");
        if (getLastCheckUpdateVersion(context).compareTo(ver) < 0) {
            showNewVersionDialog(context, ver, desc);
        }
    }

    private static void showNewVersionDialog(final Context context, String ver, String desc) {
        MyLog.e("update", "showNewVersionDialog");
        setLastCheckUpdateVersion(context, ver);
        BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(context);
        builder.setTitle(context.getString(R.string.update_newversion_title, ver));
        builder.setMessage(desc);
        builder.setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UHAnalytics.changeDataCount(UserHabitID.LM_36);
                SettingsUtil.go2GooglePlay(context);
            }
        });

        builder.setNegativeButton(R.string.update_notnow, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UHAnalytics.changeDataCount(UserHabitID.LM_37);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    ////////////////////////////////////////////////////////
    private static long getLastCheckUpdateTime(Context context) {
        return CommonPreference.getDefault(context).loadLong("cu_time", 0);
    }

    private static void setLastCheckUpdateTime(Context context) {
        CommonPreference.getDefault(context).saveLong("cu_time", System.currentTimeMillis());
    }

    private static String getLastCheckUpdateVersion(Context context) {
        return CommonPreference.getDefault(context).loadString("cu_ver", "");
    }

    private static void setLastCheckUpdateVersion(Context context, String version) {
        CommonPreference.getDefault(context).saveString("cu_ver", version);
    }
}
