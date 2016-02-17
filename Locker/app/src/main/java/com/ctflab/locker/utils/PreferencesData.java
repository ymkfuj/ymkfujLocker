package com.ctflab.locker.utils;

import android.content.Context;

import com.anguanjia.framework.preferece.CommonPreference;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.SSLUtilExt;
import com.anguanjia.framework.utils.SslException;
import com.ctflab.locker.common.UserHabitID;

/**
 * Created by hejw on 2016/1/12.
 */
public class PreferencesData {

    private static boolean showAuthentication;
    private static boolean go2Permission;
    private static boolean permissionUsable;

    //密码输入方式：0 数字键盘，1 图形键盘
    public static int getPasswordModle(Context mContext){
        return CommonPreference.getDefault(mContext).loadInt("passwordModle", 0);
    }

    public static void setPasswordModle(Context mContext,int patternModle){
        if(patternModle==0){
            UHAnalytics.changeDataCount(UserHabitID.LM_19);
        }else if(patternModle==1){
            UHAnalytics.changeDataCount(UserHabitID.LM_20);
        }
        CommonPreference.getDefault(mContext).saveInt("passwordModle", patternModle);
    }

    //密码锁密码
    public static String getPassword(Context mContext){
        String password = CommonPreference.getDefault(mContext).loadString("pwd", "");
        try {
            password = SSLUtilExt.getInstance().data_to_decrypt(password,password.length());
        } catch (SslException e) {
            e.printStackTrace();
        }
        return password;
    }

    public static void setPassword(Context mContext,String password){
        CommonPreference.getDefault(mContext).saveString("pwd",password);
    }

    //锁定状态
    public static void setLockStatus(Context mContext,boolean status){
        CommonPreference.getDefault(mContext).saveBoolean("lockStatus", status);
    }

    public static boolean getLockStatus(Context mContext){
        return CommonPreference.getDefault(mContext).loadBoolean("lockStatus", false);
    }

    //最后一次锁定的时间、
    public static void setLastUnlockTime(Context mContext, long time){
        CommonPreference.getDefault(mContext).saveLong("last_unlock_time",time);
    }

    public static long getLastUnlockTime(Context mContext){
        return CommonPreference.getDefault(mContext).loadLong("last_unlock_time",0);
    }

    //权限已开
    public static void setPermissionUsable(boolean usable){
        permissionUsable = usable;
    }

    public static boolean isPermissionUsable(){
        return permissionUsable;
    }

    //是否需要密码验证
    public static boolean isShowAuthentication(){
        return showAuthentication;
    }

    public static void setShowAuthentication(boolean show){
        showAuthentication = show;
    }

    //开启权限的外部跳转,与app共同控制，判断加锁逻辑
    public static boolean go2Permission(){
        return go2Permission;
    }

    public static void setGo2Permission(boolean permission){
        go2Permission = permission;
    }

    public static void setPermissionCheckTime(Context mContext){
        CommonPreference.getDefault(mContext).saveLong("permissionCheckTime",System.currentTimeMillis()+7*24*3600);
    }

    public static long getPermissionCheckTime(Context mContext){
        return CommonPreference.getDefault(mContext).loadLong("permissionCheckTime",0);
    }
}
