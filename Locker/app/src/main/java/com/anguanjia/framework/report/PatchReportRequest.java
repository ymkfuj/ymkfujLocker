package com.anguanjia.framework.report;

import com.anguanjia.framework.net.Request;
import com.anguanjia.framework.net.RequestException;
import com.anguanjia.framework.utils.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatchReportRequest extends Request {
    JSONArray mUserdata;
    PatchReportRequest(JSONArray userdata){
        super("1002", true, true);
        mUserdata = userdata;
    }

    @Override
    public void buildRequestP(JSONObject request) throws RequestException {
        try {
            request.put("userdata", mUserdata);
        } catch (JSONException e) {
            throw new RequestException(RequestException.ERR_PARAMETER);
        }
    }

    @Override
    protected void parseNetworkResponse(JSONObject response) {
        ReportRequestCache.uploadFinished(true);
        MyLog.e("reports", "uploadCache ok");
    }

    @Override
    protected void onErrorResponse(RequestException e) {
        ReportRequestCache.uploadFinished(false);
        MyLog.e("reports", "uploadCache err:" + e.getMessage());
    }
}
