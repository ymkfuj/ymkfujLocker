package com.ctflab.locker.net;

import com.anguanjia.framework.net.Request;
import com.anguanjia.framework.net.RequestException;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class PwdRecoveryRequest extends Request {
    String mEmail;
    String mPwd;
    String mLang;
    String mType;
    public PwdRecoveryRequest(String email, String pwd, String type, String lang) {
        super("1004", true, true);
        mEmail = email;
        mPwd = pwd;
        mLang = lang;
        mType = type;
    }

    @Override
    public void buildRequestP(JSONObject request) throws RequestException {
        try {
            request.put("email", mEmail);
            request.put("passwd", mPwd);
            request.put("lang", mLang);
            request.put("type", mType);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RequestException(RequestException.ERR_PARAMETER);
        }
    }
}
