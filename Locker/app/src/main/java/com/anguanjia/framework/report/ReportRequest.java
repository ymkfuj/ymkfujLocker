package com.anguanjia.framework.report;


import com.anguanjia.framework.net.Request;
import com.anguanjia.framework.net.RequestException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

abstract public class ReportRequest extends Request {
    private long mTimeStamp;
    private boolean mCacheFailed;
    private String mType;
    private JSONObject mUserData;
    public ReportRequest(String type, boolean cacheFailed){
        super("1002", true, true);
        mCacheFailed = cacheFailed;
        mType = type;
        mTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void buildRequestP(JSONObject request) throws RequestException {
        try {
            JSONArray UserDatas = new JSONArray();
            mUserData = new JSONObject();
            mUserData.put("type", mType);
            mUserData.put("time", ""+mTimeStamp);
            mUserData.put("data", buildUserData());
            UserDatas.put(mUserData);
            request.put("userdata", UserDatas);
        } catch (JSONException e) {
            throw new RequestException(RequestException.ERR_PARAMETER);
        }
    }

    abstract protected JSONObject buildUserData();

    @Override
    protected void onErrorResponse(RequestException e) {
        if (mCacheFailed){
            ReportRequestCache.saveCache(mUserData);
        }
    }
}
