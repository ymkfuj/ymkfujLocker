package com.anguanjia.framework.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.anguanjia.framework.AnguanjiaFramewok;
import com.anguanjia.framework.preferece.CommonPreference;
import com.anguanjia.framework.utils.FileOption;
import com.anguanjia.framework.utils.MyLog;
import com.anguanjia.framework.utils.XZip;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CFManager {

    private static final String CONFIG_FILE_VER = "cf.ver"; //配置文件初始版本

    //目录 Or 文件 （单层，直接释放到files目录）
    public static String getCFFilePath(String cf) {
        return AnguanjiaFramewok.mApplicationContext.getFilesDir().getAbsolutePath() + "/" + cf;
    }

    //目录 + 文件 （双层，释放到files子目录）
    public static String getCFFilePath(String dir, String cf_s) {
        return dir + "/" + cf_s;
    }

    //目录 + 版本号组成的路径
    public static synchronized String getCFDirPath(String cf) {
        String cv = getContentVersion(cf);
        return AnguanjiaFramewok.mApplicationContext.getFilesDir().getAbsolutePath() + "/" + cf + "/" + cv;
    }

    public static synchronized void removeOldCF(String cf, String cv){
        File f = new File(getCFFilePath(cf));
        String path = getCFFilePath(cf) + "/" + cv;
        File [] files = f.listFiles();
        if (files==null){
            return;
        }
        for (File sf : files){
            if(sf.isDirectory() && sf.getAbsolutePath().compareTo(path) < 0){
                FileOption.deleteDirectorys(sf.getAbsolutePath());
            }
        }
    }

    //配置文件的初始版本，存在assets中
    static Map<String, String> releaseDefaultVersionFromAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        @SuppressWarnings("Convert2Diamond") Map<String, String> ret = new HashMap<String, String>();
        try {
            InputStream inputStream = assetManager.open(CONFIG_FILE_VER);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = br.readLine();
            while (line != null) {
                String[] cols = line.split(":");
                line = br.readLine();
                if (cols.length == 2) {
                    MyLog.e("cfver:", cols[0] + ":" + cols[1]);
                    ret.put(cols[0], cols[1]);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    //配置文件当前版本
    public static String getContentVersion(String cf) {
        return CommonPreference.getInstance(AnguanjiaFramewok.mApplicationContext, Const.AGJ_CONFIG_FILE_VER_PREF).loadString(cf, "");
    }

    //配置文件当前版本
    static void setContentVersion(String cf, String version) {
        CommonPreference.getInstance(AnguanjiaFramewok.mApplicationContext, Const.AGJ_CONFIG_FILE_VER_PREF).saveString(cf, version);
    }

    //asset释放配置文件
    static boolean releaseFileFromAssets(Context context, String cf, boolean bOverWrite) {
        if (FileOption.copyFileFromAssets(context, cf, getCFFilePath(cf) + ".tmp", bOverWrite)) {
            File f = new File(getCFFilePath(cf));
            if (f.exists()) {
                if(!f.delete())
                    return false;
            }
            f = new File(getCFFilePath(cf) + ".tmp");
            boolean ret = f.renameTo(new File(getCFFilePath(cf)));
            notifyCFReady(cf);
            return ret;
        }
        return false;
    }

    static boolean releaseDirFromAssets(Context context, String cf, String cv, boolean bOverWrite) {
        if (TextUtils.isEmpty(getContentVersion(cf)) || bOverWrite) {
            if (FileOption.copyFileFromAssets(context, cf, getCFFilePath(cf) + ".zip", bOverWrite)) {
                return releaseFromZipFile(getCFFilePath(cf) + ".zip", cf, cv);
            }
        }
        return false;
    }

    //压缩包释放配置文件
    static synchronized boolean releaseFromZipFile(String file, String cf, String cv) {
        try {
            XZip.UnZipFolder(file, getCFFilePath(getCFFilePath(cf), cv));
            setContentVersion(cf, cv);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //配置文件释放通知
    static void notifyCFReady(String cv) {

    }
}
