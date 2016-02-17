package com.anguanjia.framework.userhabit;

import android.content.Context;

import com.anguanjia.framework.net.Request;
import com.anguanjia.framework.net.RequestException;

import org.json.JSONException;
import org.json.JSONObject;

public class UHAnalyticsReporter {
    static long REPORT_SUCCESS_TIMESTAMP = 0;
    private static final int REPORT_SUCCESS_INTERVAL = 240 * 60 * 1000;

    private static boolean reporting = false;
    public synchronized static void report(Context context){
        if (!reporting && ((System.currentTimeMillis() - REPORT_SUCCESS_TIMESTAMP) > REPORT_SUCCESS_INTERVAL)) {
            reporting = true;
            new UHRequest().commit(context);
        }
    }

    public static class UHRequest extends Request {
        long mRequesttime;
        public UHRequest(){
            super("1003", true, true);
        }
        @Override
        public void buildRequestP(JSONObject request) throws RequestException {
            JSONObject userdata = new JSONObject();
            try {
                userdata.put("tb", UHAnalytics.getBeginTime());
                userdata.put("H_type1", UHAnalytics.getUHString(UHAnalytics.UH_TYPE1));
                userdata.put("H_type2", UHAnalytics.getUHString(UHAnalytics.UH_TYPE2));
                userdata.put("H_type3", UHAnalytics.getUHString(UHAnalytics.UH_TYPE3));
                userdata.put("H_type4", UHAnalytics.getUHString(UHAnalytics.UH_TYPE4));
                userdata.put("H_type5", UHAnalytics.getUHString(UHAnalytics.UH_TYPE5));
                mRequesttime = System.currentTimeMillis();
                userdata.put("te", mRequesttime);
                request.put("userdata", userdata);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void parseNetworkResponse(JSONObject response) {
            UHAnalytics.clear(mRequesttime);
            REPORT_SUCCESS_TIMESTAMP = System.currentTimeMillis();
            reporting = false;
        }

        @Override
        protected void onErrorResponse(RequestException e) {
            reporting = false;
        }
    }
}
