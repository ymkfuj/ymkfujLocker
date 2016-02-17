package com.ctflab.locker.services.pulse;

import com.anguanjia.framework.net.Request;
import com.anguanjia.framework.net.RequestException;
import com.anguanjia.framework.utils.MyLog;

import org.json.JSONObject;

public class PulseRequest extends Request {
    public PulseRequest() {
        super("1001", true, true);
    }

    @Override
    public void buildRequestP(JSONObject request) throws RequestException {

    }

    @Override
    protected void parseNetworkResponse(JSONObject response) {
        PulseManager.pulseCount();
    }

    @Override
    protected void onErrorResponse(RequestException e) {
        MyLog.e("pulse", "pulseRequest:" + e.getMessage());
    }
}
