package com.ctflab.locker.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.ctflab.locker.common.AppInfoEntity;
import com.ctflab.locker.common.SystemDef;
import com.ctflab.locker.services.LockerService;
import com.ctflab.locker.view.AuthenticationActivity;
import com.ctflab.locker.widget.ToastHelper;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SystemUtil {
    public static final String ERR_TAG = "SystemUtil.java";
    private static String AUTHORITY;


    public static boolean isAtHome(Context context, String pacname) {// 用户是否在桌面
        if (TextUtils.isEmpty(pacname)) {
            return false;
        }

        List<String> homelist = getInstalledDesktopList(context);
        boolean result = homelist.contains(pacname);

        return result;
    }

    public static List<String> getInstalledDesktopList(Context context) {// 获取用户已安装的桌面列表
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    public static boolean isCurrentHome(Context context) {
        boolean vRet = false;
        List<String> homelist = getInstalledDesktopList(context);
        String vPkg = SystemUtil.getTopPackgeName(context);
        if (homelist != null && homelist.size() > 0 && homelist.contains(vPkg)) {
            vRet = true;
        }
        return vRet;
    }

    public static boolean isCurrentSelf(Context context) {
        boolean vRet = false;
        if (context != null) {
            String vTopPkg = SystemUtil.getTopPackgeName(context);
            if (context.getPackageName().equals(vTopPkg)) {
                vRet = true;
            }
        }
        return vRet;
    }

//	/**
//	 * 获取当前运行的包名，根据sdk以及google 补丁做区分
//	 * @param aContext
//	 * @return
//	 */
//	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
//	public static String getTopPkgName(Context aContext){
//		String vRet = "";
//		if(aContext != null){
//			if(isGooglePatch(aContext)){
//				UsageStatsManager vMgr = (UsageStatsManager) aContext.getSystemService(Context.USAGE_STATS_SERVICE);
//				if(vMgr != null){
//					long vEnd = System.currentTimeMillis();
//					long vBegin = vEnd - 1000*60;
//					List<UsageStats> vStateList = vMgr.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, vBegin, vEnd);
//					if(vStateList != null && vStateList.size() > 0){
//						long vLastTime = 0;
//						for(int i = 0; i < vStateList.size(); i++){
//							UsageStats vTmp = vStateList.get(i);
//							if(vTmp != null && vTmp.getLastTimeUsed() > vLastTime){
//								vLastTime = vTmp.getLastTimeUsed();
//								vRet = vTmp.getPackageName();
//							}
//						}
//					}
//				}
//			}
//			else{
//				ActivityManager vManager = (ActivityManager) aContext.getSystemService(Context.ACTIVITY_SERVICE);
//				if(vManager != null){
//					if(Build.VERSION.SDK_INT >= SystemDef.Version.LOLLIPOP){
//						List<ActivityManager.RunningAppProcessInfo> vAppList = vManager.getRunningAppProcesses();
//						if (vAppList != null && vAppList.size()>0) {
//							ActivityManager.RunningAppProcessInfo rInfo = vAppList.get(0);
//							if (rInfo != null && rInfo.pkgList != null && rInfo.pkgList.length >0) {
//								vRet = rInfo.pkgList[0];
//							}
//						}
//					}
//					else{
//						List<ActivityManager.RunningTaskInfo> vTaskList = vManager.getRunningTasks(1);
//						if(vTaskList != null && vTaskList.size() > 0){
//							ActivityManager.RunningTaskInfo vRunningTask = vTaskList.get(0);
//							if(vRunningTask != null && vRunningTask.topActivity != null){
//								vRet = vRunningTask.topActivity.getPackageName();
//							}
//						}
//					}
//				}
//			}
//		}
//		return vRet;
//	}

    /**
     * 隐藏系统键盘
     */
    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 显示系统键盘
     */
    public static void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * 判断是否是系统应用
     */
    public static boolean isSystemApp(Context content, String pname) {
        PackageManager pm = content.getPackageManager();
        try {
            ApplicationInfo app_info = pm.getApplicationInfo(pname, 0);
            return isSystemApp(content, app_info);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (RuntimeException e) {
            //fix the crash of "Package manager has died"
            return false;
        }
    }

    /**
     * 判断是否系统应用
     */
    public static boolean isSystemApp(Context content, ApplicationInfo app_info) {
        if ((app_info.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 弹出一个Toast
     *
     * @param context
     * @param contentResId
     */
    public static void showToast(Context context, int contentResId) {
        if (contentResId <= 0 || context == null || context.getResources() == null) {
            return;
        }
        showToast(context, context.getResources().getString(contentResId));
    }

    private static void showToast(Context context, String content) {
        if (content == null || context == null || context.getResources() == null) {
            return;
        }
        //Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        ToastHelper.makeText(context, content, ToastHelper.LENGTH_SHORT).show();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static boolean isWifiNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        if (cursor.isClosed()) {
            return;
        }
        try {
            cursor.close();
        } catch (Exception e) {
        }
    }


    public static boolean startActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取当前时刻的栈顶app,(预期结果)即当前运行app
     *
     * @param mContext
     * @return appPackageName(never null)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getTopPackgeName(Context mContext) {
        String pkname = "";
        if (mContext != null) {
            if (isGooglePatch(mContext)) {
                UsageStatsManager manager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
                if (manager != null) {
                    long vEnd = System.currentTimeMillis();
                    long vBegin = vEnd - 1000 * 60;
                    List<UsageStats> list = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, vBegin, vEnd);
                    if (list != null && list.size() > 0) {
                        long mLastTime = 0;
                        for (UsageStats usageStats : list) {
                            if (usageStats != null && usageStats.getLastTimeUsed() > mLastTime) {
                                mLastTime = usageStats.getLastTimeUsed();
                                pkname = usageStats.getPackageName();
                            }

                        }
                    }
                }
            } else {
                ActivityManager vManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                if (vManager != null) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        List<ActivityManager.RunningAppProcessInfo> vAppList = vManager.getRunningAppProcesses();
                        if (vAppList != null && vAppList.size() > 0) {
                            ActivityManager.RunningAppProcessInfo rInfo = vAppList.get(0);
                            if (rInfo != null && rInfo.pkgList != null && rInfo.pkgList.length > 0) {
                                pkname = rInfo.pkgList[0];
                            }
                        }
                    } else {
                        List<ActivityManager.RunningTaskInfo> vTaskList = vManager.getRunningTasks(1);
                        if (vTaskList != null && vTaskList.size() > 0) {
                            ActivityManager.RunningTaskInfo vRunningTask = vTaskList.get(0);
                            if (vRunningTask != null && vRunningTask.topActivity != null) {
                                pkname = vRunningTask.topActivity.getPackageName();
                            }
                        }
                    }
                }
            }
        }
        return pkname;
    }

    /**
     * 是否已经打上google补丁，导致getRunningAppProcess失效
     */
    public static boolean isGooglePatch(Context mContext) {
        boolean vRet = false;
        if (mContext != null) {
            ActivityManager vManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            if (vManager != null) {
                if (Build.VERSION.SDK_INT >= 21) {
                    List<ActivityManager.RunningAppProcessInfo> vList = vManager.getRunningAppProcesses();
                    if (vList != null && vList.size() > 0) {
                        vRet = true;
                        for (int i = 0; i < vList.size(); i++) {
                            ActivityManager.RunningAppProcessInfo vTmp = vList.get(i);
                            if (vTmp != null && vTmp.pkgList != null && vTmp.pkgList.length > 0) {
                                if (!vTmp.pkgList[0].equals(mContext.getPackageName())) {
                                    vRet = false;
                                    break;
                                }
                            }
                        }
                    } else {
                        vRet = true;
                    }
                }
            }
        }
        return vRet;
    }


    /**
     * 判断是否打开了usage app查看功能
     *
     * @param aContext
     * @return
     */
    public static boolean isUsageOpen(Context aContext) {
        boolean vRet = true;
        if (aContext != null) {
            if (isGooglePatch(aContext)) {
                AppOpsManager opsManager = (AppOpsManager) aContext.getSystemService(Context.APP_OPS_SERVICE);
                vRet = opsManager.checkOp(AppOpsManager.OPSTR_GET_USAGE_STATS,aContext.getApplicationInfo().uid,aContext.getPackageName())==AppOpsManager.MODE_ALLOWED;
            }
        }
        return vRet;
    }

    /**
     * Android M 版本之后，23之后，对addview 的type 做了限制
     *
     * @param aWm
     * @param aView
     * @param aParams
     */
    public static void addWindowView(WindowManager aWm, View aView, WindowManager.LayoutParams aParams) {
        if (aWm != null && aView != null && aParams != null) {
            if (Build.VERSION.SDK_INT >= SystemDef.Version.KITKAT) {
                aParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            aWm.addView(aView, aParams);
        }
    }

    /**
     * 安全方式启动service
     *
     * @param aContext
     * @param aIntent
     */
    public static void startServiceSafeMode(Context aContext, Intent aIntent) {
        try {
            if (aContext != null && aIntent != null) {
                aIntent.setPackage(aContext.getPackageName());
                aContext.startService(aIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取系统应用列表(有icon入口的app)
     */
    public static ArrayList<AppInfoEntity> getSysAppList(Context mContext) {
        ArrayList<AppInfoEntity> datas = new ArrayList<AppInfoEntity>();
        ArrayList<String> keys = new ArrayList<String>();
        if (mContext != null) {
            PackageManager manager = mContext.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> intentList = manager.queryIntentActivities(intent, 0);//桌面应用
            Iterator<ResolveInfo> infoIterator = intentList.iterator();
            String pk;

            while (infoIterator.hasNext()) {
                ResolveInfo resolveInfo = infoIterator.next();
                if (resolveInfo != null && resolveInfo.activityInfo != null){
                    pk = resolveInfo.activityInfo.packageName;
                    if (pk != null) {
                        if (pk.equals(mContext.getPackageName())) {
                            infoIterator.remove();
                            continue;
                        }
                        if (!keys.contains(pk)){
                            AppInfoEntity entity = new AppInfoEntity();
                            entity.packgeName = pk;
                            entity.lable = resolveInfo.loadLabel(manager).toString();
                            entity.icon = resolveInfo.loadIcon(manager);
                            datas.add(entity);
                            keys.add(entity.packgeName);
                        }
                        infoIterator.remove();
                    }
                }

            }
            ApplicationInfo info;
            try {
                info = manager.getApplicationInfo("com.android.vending",0);//google play
                if (info != null ) {
                    AppInfoEntity entity = new AppInfoEntity();
                    entity.packgeName = info.processName;
                    entity.lable = info.loadLabel(manager).toString();
                    entity.icon = info.loadIcon(manager);
                    if (!keys.contains(entity.packgeName)){
                        datas.add(entity);
                        keys.add(entity.packgeName);
                    }
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            try {
                info = manager.getApplicationInfo("com.android.settings",0);//设置
                if (info != null && !keys.contains(info.packageName)) {
                    AppInfoEntity entity = new AppInfoEntity();
                    entity.packgeName = info.processName;
                    entity.lable = info.loadLabel(manager).toString();
                    entity.icon = info.loadIcon(manager);
                    datas.add(entity);
                    keys.add(entity.packgeName);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            try {
                info = manager.getApplicationInfo("com.google.android.packageinstaller",0);//安装卸载程序
                if (info != null && !keys.contains(info.packageName)) {
                    AppInfoEntity entity = new AppInfoEntity();
                    entity.packgeName = info.processName;
                    entity.lable = info.loadLabel(manager).toString();
                    entity.icon = info.loadIcon(manager);
                    datas.add(entity);
                    keys.add(entity.packgeName);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            try {
                info = manager.getApplicationInfo("com.android.packageinstaller",0);
                if (info != null && !keys.contains(info.packageName)) {
                    AppInfoEntity entity = new AppInfoEntity();
                    entity.packgeName = info.processName;
                    entity.lable = info.loadLabel(manager).toString();
                    entity.icon = info.loadIcon(manager);
                    datas.add(entity);
                    keys.add(entity.packgeName);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

        }
        return datas;
    }

    /**
     * 将dip转化为px
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return "V" + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @param resource 需要转化的xml
     * @return 返回转化好的View
     * @Title: inflateView
     * @Description: 将xml转化为View
     * @author wuxu
     * @date 2011-11-23
     */
    public static View inflateView(Context context, int resource) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return vi.inflate(resource, null, false);
    }

    public static void go2Permission(Activity mContext) {

        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SystemUtil.startActivityForResult(mContext, intent, 0);
        Intent serviceIntent = new Intent(mContext,LockerService.class);
        serviceIntent.putExtra("permission", true);
        SystemUtil.startServiceSafeMode(mContext, serviceIntent);
    }

    public static void go2Authen(final Activity mContext){
        PreferencesData.setShowAuthentication(false);
        Intent intent = new Intent(mContext, AuthenticationActivity.class);
        intent.putExtra("resumeAuthen",true);
        SystemUtil.startActivity(mContext, intent);
    }

    //隐藏app，显示桌面
    public static void showHome(Context mContext){
        if (mContext != null){
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }
    //大师锁是否在后台运行
    public static boolean isSelfBackground(Context mContext){
        boolean background = true;
        if (mContext != null) {
            ActivityManager vManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> vAppList = vManager.getRunningAppProcesses();
            if (vAppList != null && vAppList.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo rInfo:vAppList){
                    if (rInfo != null && rInfo.pkgList != null && rInfo.pkgList.length > 0) {
                        if (mContext.getPackageName().equals(rInfo.pkgList[0])){
                            if(rInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
//                                MyLog.d("my","foreground");
                                background = false;
                            }
                            break;
                        };
                    }
                }

            }
        }
        return background;
    }


    private final static int kSystemRootStateUnknow = -1;
    private final static int kSystemRootStateDisable = 0;
    private final static int kSystemRootStateEnable = 1;
    private static int systemRootState = kSystemRootStateUnknow;

    //判断是否已经root（不弹框）
    public static boolean isRootSystem() {
        if (systemRootState == kSystemRootStateEnable) {
            return true;
        } else if (systemRootState == kSystemRootStateDisable) {
            return false;
        }
        File f = null;
        final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/",
                "/system/sbin/", "/sbin/", "/vendor/bin/" };
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    systemRootState = kSystemRootStateEnable;
                    return true;
                }
            }
        } catch (Exception e) {
        }
        systemRootState = kSystemRootStateDisable;
        return false;
    }
    //判断是否存在虚拟返回键
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }

        return hasNavigationBar;

    }
}
