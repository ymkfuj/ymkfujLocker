package com.ctflab.locker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import com.anguanjia.framework.preferece.CommonPreference;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.widget.BottomAlertDialogBuilder;
import com.ctflab.locker.widget.BottomListDialog;

/**
 * Created by wuwei on 2016/1/8.
 */
public class SettingsUtil {
    ////////////////////////////////////////////////////////
    public static BottomListDialog getDelaySettingDialog(final Activity context, final BottomListDialog.IDialogSelectListener listener) {
        String[] time = context.getResources().getStringArray(R.array.dialog_lock_delay);
        final int delay = getDelay(context);
        final BottomListDialog dialog = new BottomListDialog(context);
        dialog.initDialog(time, delay, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int) v.getTag();
                setDelay(context, index);
                listener.onSelected(index);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static void showDelaySettingDialog(Activity context, DialogInterface.OnClickListener clickListener) {
        BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(context);
        String[] time = context.getResources().getStringArray(R.array.dialog_lock_delay);
        builder.setSingleChoiceItems(time, getDelay(context), clickListener);

        builder.setPositiveButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UHAnalytics.changeDataCount(UserHabitID.LM_26);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static int getDelay(Context context) {
        return CommonPreference.getDefault(context).loadInt("setting_delay", 0);
    }

    public static void setDelay(Context context, int index) {
        CommonPreference.getDefault(context).saveInt("setting_delay", index);
    }


    public static String getDelayString(Context context) {
        String[] delay = context.getResources().getStringArray(R.array.dialog_lock_delay);
        return delay[getDelay(context)];
    }

    ////////////////////////////////////////////////////////
    public static int getLockStyle(Context context) {
        return PreferencesData.getPasswordModle(context);
    }

    public static void setLockStyle(Context context, int lock_style) {
        CommonPreference.getDefault(context).saveInt("setting_delay", lock_style);
    }

    public static String getLockStyleString(Context context) {
        int style = getLockStyle(context);
        String[] styles = context.getResources().getStringArray(R.array.setting_lock_style);
        return styles[style];
    }

    public static void showLockTypeSettingDialog(Activity context, DialogInterface.OnClickListener clickListener) {
        BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(context);
        String[] time = context.getResources().getStringArray(R.array.setting_lock_style);
        builder.setSingleChoiceItems(time, getLockStyle(context), clickListener);

        builder.setPositiveButton(R.string.dialog_cancel, null);
        builder.create().show();
    }

    ////////////////////////////////////////////////////////
    public static String getEmail(Context context) {
        return CommonPreference.getDefault(context).loadString("setting_email", "");
    }

    public static void setEmail(Context context, String email) {
        CommonPreference.getDefault(context).saveString("setting_email", email);
    }

    ////////////////////////////////////////////////////////
    public static void sendEmail(Context context) {
        //系统邮件系统的动作为android.content.Intent.ACTION_SEND
        Intent email = new Intent(android.content.Intent.ACTION_SEND);
        email.setType("plain/text");
        String[] emailReciver = new String[]{context.getResources().getString(R.string.email_address)};
        String emailSubject = context.getString(R.string.email_title);
        String emailBody = getAppInifoString(context);

//设置邮件默认地址
        email.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
//设置邮件默认标题
        email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailSubject);
//设置要默认发送的内容
        email.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
//调用系统的邮件系统
        context.startActivity(Intent.createChooser(email, context.getResources().getString(R.string.email_choose)));
    }

    private static String getAppInifoString(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\n\n\n==========================================================").append("\n")
                .append("Version:").append(SystemUtil.getVersion(context)).append("\n")
                .append("Platform:").append(Build.VERSION.SDK_INT).append("\n")
                .append("Product:").append(Build.MANUFACTURER + "  " + Build.PRODUCT).append("\n")
                .append("Launguage:").append(LanguageManager.getLanguageString(context)).append("\n")
                .append("==========================================================").append("\n").append("\n");

        return sb.toString();
    }

    ////////////////////////////////////////////////////////
    private static long TIME_SPAN = 86400000l;

    public static void showRateDialog(final Activity context) {
        boolean time = (getUnlockTime(context) > 0) && ((System.currentTimeMillis() - getUnlockTime(context) > TIME_SPAN));
        boolean version = SystemUtil.getVersionCode(context) > getSavedVersionCode(context);
        if (time && version) {
            BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(context);
            builder.setView(R.layout.dialog_rate);
            builder.setTitle(R.string.rate_title);
            builder.setPositiveButton(R.string.dialog_rate, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
//                    try {
//                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//                    } catch (android.content.ActivityNotFoundException anfe) {
//                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//                    }
                    go2GooglePlay(context);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, null);
            builder.create().show();
            saveCurrentVersionCode(context);
        }
    }

    public static void setUnlockTime(Context context, long time) {
        PreferencesData.setLastUnlockTime(context, time);
        if ((SystemUtil.getVersionCode(context) > getSavedVersionCode(context)) && (getUnlockTime(context) <= 0)) {
            CommonPreference.getDefault(context).saveLong("unlock_time", time);
        }
    }

    public static long getUnlockTime(Context context) {
        return CommonPreference.getDefault(context).loadLong("unlock_time", -1l);
    }

    public static void saveCurrentVersionCode(Context context) {
        CommonPreference.getDefault(context).saveInt("version_code", SystemUtil.getVersionCode(context));
    }

    public static int getSavedVersionCode(Context context) {
        return CommonPreference.getDefault(context).loadInt("version_code", 0);
    }

    ////////////////////////////////////////////////////////
    public static void go2GooglePlay(Context context) {
         String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object

        try {
            // get the Twitter app if possible
            context.getPackageManager().getPackageInfo("com.android.vending", 0);
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (Exception e) {
            // no Twitter app, revert to browser
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
//        context.startActivity(intent);

    }

    ////////////////////////////////////////////////////////
    public static void saveGuideLaunched(Context context) {
        CommonPreference.getDefault(context).saveInt("guide", SystemUtil.getVersionCode(context));
    }

    public static boolean ifFirst(Context context) {
        int ver = CommonPreference.getDefault(context).loadInt("guide", 0);
        return (SystemUtil.getVersionCode(context) > ver);
    }
}
