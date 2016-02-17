package com.ctflab.locker.net;

import com.anguanjia.framework.net.Request;
import com.anguanjia.framework.net.RequestException;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class CheckUpdateRequest extends Request{
    String mLang;
    public CheckUpdateRequest(String lang){
        super("1005", true, true);
        mLang = lang;
    }

    @Override
    public void buildRequestP(JSONObject request) throws RequestException {
        try {
            request.put("lang", mLang);
        } catch (JSONException e) {
            throw new RequestException(RequestException.ERR_PARAMETER);
        }
    }

    @Override
    protected void parseNetworkResponse(JSONObject response) {
        try {
            String ver = response.getString("version");
            String desc = response.getString("desc");
            newVersion(ver, desc);
        } catch (JSONException e) {
            onErrorResponse(new RequestException(RequestException.ERR_PARAMETER));
            e.printStackTrace();
        }
    }

    abstract protected void newVersion(String ver, String des);
}
