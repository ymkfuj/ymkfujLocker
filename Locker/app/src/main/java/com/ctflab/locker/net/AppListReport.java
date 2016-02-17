package com.ctflab.locker.net;


import android.content.Context;

import com.anguanjia.framework.net.RequestException;
import com.anguanjia.framework.report.ReportRequest;
import com.ctflab.locker.common.AppInfoEntity;
import com.ctflab.locker.utils.AppListUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class AppListReport {
    static long REPORT_SUCCESS_TIMESTAMP = 0;
    private static final int REPORT_SUCCESS_INTERVAL =7 * 24 * 60 * 60 * 1000;

    private static boolean reporting = false;
    public synchronized static void report(final Context context){
        if (!reporting) {
            if (System.currentTimeMillis() - REPORT_SUCCESS_TIMESTAMP > REPORT_SUCCESS_INTERVAL) {
                reporting = true;
                if (AppListUtil.appInfoEntities == null || AppListUtil.appInfoEntities.size() <= 0){
                    AppListUtil.getInstalledAppList(context, new AppListUtil.IAppLoadListener() {
                        @Override
                        public void onLoadSuccessed() {
                            request(context, AppListUtil.appInfoEntities);
                        }

                        @Override
                        public void onLoadFailed() {
                            reporting = false;
                        }

                        @Override
                        public void onLoading() {

                        }
                    });
                }else{
                    request(context, AppListUtil.appInfoEntities);
                }
            }
        }
    }

    private static void request(Context context, final List<AppInfoEntity> applist){
        new ReportRequest("apl", false) {
            @Override
            protected JSONObject buildUserData() {
                JSONObject object = new JSONObject();
                try {
                    JSONArray ar = new JSONArray();
                    for (AppInfoEntity entity : applist){
                        JSONObject app = new JSONObject();
                        app.put("pn", entity.packgeName);
                        app.put("ls", entity.isLoacked ? 1 : 0);
                        ar.put(app);
                    }
                    object.put("list", ar);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return object;
            }

            @Override
            protected void parseNetworkResponse(JSONObject response) {
                REPORT_SUCCESS_TIMESTAMP = System.currentTimeMillis();
                reporting = false;
            }

            @Override
            protected void onErrorResponse(RequestException e) {
                reporting = false;
                super.onErrorResponse(e);
            }
        }.commit(context);
    }
}
