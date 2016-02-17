package com.anguanjia.framework.preferece;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.anguanjia.framework.config.Const;
import com.anguanjia.framework.utils.SSLUtilExt;
import com.anguanjia.framework.utils.SslException;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用状态存储工具类，封装自SharedPreferences
 * Created by wuwei on 2015/7/28.
 */	
public class CommonPreference {
    private SharedPreferences mPreference;
    private static Map<String, WeakReference<CommonPreference>> mPrefs = new HashMap<String, WeakReference<CommonPreference>>();

    /**
     * 默认preference
     */
    public static CommonPreference getDefault(Context context){
        return getInstance(context, Const.AGJ_COMMON_PREF);
    }

    /**
     * 根据label创建不同的preference文件
     */
    public static CommonPreference getInstance(Context context, String label){
        WeakReference<CommonPreference> wpref = mPrefs.get(label);
        if (wpref != null) {
            CommonPreference pref = wpref.get();
            if (pref != null){
                return pref;
            }
        }
        CommonPreference pref = new CommonPreference(context, label);
        mPrefs.put(label, new WeakReference<CommonPreference>(pref));
        return pref;
    }

    private CommonPreference(Context context, String label) {
        this.mPreference = context.getSharedPreferences(label, Context.MODE_PRIVATE);
    }

    public void saveString(String key, String value) {
        mPreference.edit().putString(key, value).commit();
    }

    public String loadString(String key, String defValue) {
        return mPreference.getString(key, defValue);
    }

    public void saveInt(String key, int value) {
        mPreference.edit().putInt(key, value).commit();
    }

    public int loadInt(String key, int defValue) {
        return mPreference.getInt(key, defValue);
    }

    public void saveBoolean(String key, boolean value) {
        mPreference.edit().putBoolean(key, value).commit();
    }

    public boolean loadBoolean(String key, boolean defValue) {
        return mPreference.getBoolean(key, defValue);
    }

    public void saveLong(String key, long value) {
        mPreference.edit().putLong(key, value).commit();
    }

    public long loadLong(String key, long defValue) {
        return mPreference.getLong(key, defValue);
    }

    public void saveFloat(String key, float value) {
        mPreference.edit().putFloat(key, value).commit();
    }

    public float loadFloat(String key, float defValue){
        return mPreference.getFloat(key,defValue);
    }

    public float loadString(String key, float defValue) {
        return mPreference.getFloat(key, defValue);
    }

    public void clear() {
        mPreference.edit().clear().commit();
    }
}


