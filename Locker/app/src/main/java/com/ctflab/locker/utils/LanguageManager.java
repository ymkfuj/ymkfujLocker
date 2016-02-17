package com.ctflab.locker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.anguanjia.framework.preferece.CommonPreference;
import com.ctflab.locker.R;
import com.ctflab.locker.widget.BottomAlertDialogBuilder;
import com.ctflab.locker.widget.BottomListDialog;

import java.util.Locale;

/**
 * Created by wuwei on 2016/1/5.
 */
public class LanguageManager {
    public static String L_EN = "en";
    public static String L_ZH = "zh";

    private static Locale myLocale;

    public static void changeLang(Context context, String lang) {
        if (lang.equalsIgnoreCase("")) {
            return;
        }
        myLocale = new Locale(lang);
        saveLocaleString(context, lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        context.getApplicationContext().getResources().updateConfiguration(config,
                context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public static void saveLocaleString(Context context, String lang) {
        if (lang.equals(L_EN)) {
            CommonPreference.getDefault(context).saveInt("Language", 1);
        } else if (lang.equals(L_ZH)) {
            CommonPreference.getDefault(context).saveInt("Language", 0);
        } else {
            CommonPreference.getDefault(context).saveInt("Language", 1);
        }

    }

    public static void saveLocale(Context context, int lang) {
        CommonPreference.getDefault(context).saveInt("Language", lang);
    }

    public static int loadLocale(Context context) {
        return CommonPreference.getDefault(context).loadInt("Language", 1);
    }

    public static String loadLocaleString(Context context) {
//        int index = CommonPreference.getDefault(context).loadInt("Language", -1);
//        if (index == 0) {
//            return L_ZH;
//        } else if (index == 1) {
//            return L_EN;
//        } else {
//            return "";
//        }
        return L_EN;
    }

    public static BottomListDialog getLanguageSettingDialog(final Activity context, final BottomListDialog.IDialogSelectListener listener) {
        String[] lang = context.getResources().getStringArray(R.array.dialog_language);
        final int delay = loadLocale(context);
        final BottomListDialog dialog = new BottomListDialog(context);
        dialog.initDialog(lang, delay, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int) v.getTag();
                saveLocale(context, index);
                changeLang(context, loadLocaleString(context));
                listener.onSelected(index);
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static void showLanguageSettingDialog(final Activity context, DialogInterface.OnClickListener clickListener) {
        BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(context);
        String[] lang = context.getResources().getStringArray(R.array.dialog_language);
        builder.setSingleChoiceItems(lang, loadLocale(context), clickListener);

        builder.setPositiveButton(R.string.dialog_cancel, null);
        builder.create().show();
    }

    public static String getLanguageString(Context context) {
        String[] langArray = context.getResources().getStringArray(R.array.dialog_language);
        return langArray[loadLocale(context)];
    }
}
