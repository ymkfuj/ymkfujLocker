package com.anguanjia.framework.report;

import android.content.Context;

import com.anguanjia.framework.config.CFManager;
import com.anguanjia.framework.utils.MyLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReportRequestCache {
    private static final String REQUEST_CACHE_FILE = "agjrrcache";

    private static boolean mUploading = false;

    public synchronized static void uploadCache(Context context){
        if (!mUploading) {
            MyLog.e("reports", "uploadCache");
            mUploading = true;
            JSONArray array = readCache();
            if (array.length() > 0) {
                new PatchReportRequest(array).commit(context);
            } else {
                mUploading = false;
            }
        }
    }

    static synchronized void uploadFinished(boolean ok){
        mUploading = false;
        if (ok) {
            clear();
        }
    }

    static synchronized void saveCache(JSONObject userdata) {
        if (userdata == null){
            return;
        }
        File f = new File(CFManager.getCFFilePath(REQUEST_CACHE_FILE));
        FileWriter fw = null;
        try {
            fw = new FileWriter(f, true);
            fw.append(userdata.toString()).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static JSONArray readCache(){
        JSONArray userdatas = new JSONArray();
        File f = new File(CFManager.getCFFilePath(REQUEST_CACHE_FILE));
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while(line != null){
                userdatas.put(new JSONObject(line));
                line = br.readLine();
            }
            br.close();
        } catch (Exception ignored) {

        }
        return userdatas;
    }

    public static void clear(){
        File f = new File(CFManager.getCFFilePath(REQUEST_CACHE_FILE));
        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }
}
