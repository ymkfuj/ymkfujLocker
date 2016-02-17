package com.anguanjia.framework.net;

import android.content.Context;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.NetwokUtil;
import com.anguanjia.framework.utils.PhoneInfoUtil;
import com.anguanjia.framework.utils.SSLUtilExt;
import com.anguanjia.framework.utils.SslException;
import com.ctflab.locker.common.UserHabitID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

abstract public class Request {

    protected String mRequestId;
    private boolean mCompresed = false;
    private boolean mEncryption = false;

    public Request(String requesId, boolean zip, boolean encrypt){
        mRequestId = requesId;
        mCompresed = zip;
        mEncryption = encrypt;
    }

    void submitRequest(Context context){
        try {
            URL url = new URL(RequestManager.REQUEST_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(20 * 1000);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("UserID", PhoneInfoUtil.getExtId(context));
            setHeader(conn);

            OutputStream os=conn.getOutputStream();
            os.write(("data=" + URLEncoder.encode(buildReqestData(context), "UTF-8")).getBytes());
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            InputStream is;
            if (isStatusCodeError(code)){
                is = conn.getErrorStream();
            }else{
                is = conn.getInputStream();
            }

            char[] buf = new char[64];
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(is);
            int len = isr.read(buf);
            while(len != -1){
                sb.append(buf, 0, len);
                len = isr.read(buf);
            }
            isr.close();
            is.close();

            String result = sb.length() > 5 ? sb.substring(5,sb.length()) :sb.toString();
            if(mEncryption){
                result = SSLUtilExt.getInstance().data_to_decrypt(result, result.length());
            }
            if(mCompresed){
                result = SSLUtilExt.getInstance().data_to_uncompress(result);
            }

            JSONObject respons = new JSONObject(result);
            int st = respons.getInt("status");
            if (st == 200){
                parseNetworkResponse(respons.getJSONObject("response"));
            }else {
                UHAnalytics.changeDataCount(String.valueOf(st));
                onErrorResponse(new RequestException(st));
            }

        }  catch (IOException e){
            UHAnalytics.changeDataCount(String.valueOf(RequestException.ERR_DISTAL));
            onErrorResponse(new RequestException(RequestException.ERR_DISTAL));
        } catch (RequestException e){
            UHAnalytics.changeDataCount(String.valueOf(e.getErrCode()));
            onErrorResponse(e);
        } catch (SslException e){
            UHAnalytics.changeDataCount(String.valueOf(RequestException.ERR_KEY_ENCRYPTION));
            onErrorResponse(new RequestException(RequestException.ERR_KEY_ENCRYPTION));
        } catch (JSONException e){
            UHAnalytics.changeDataCount(String.valueOf(RequestException.ERR_PARAMETER));
            onErrorResponse(new RequestException(RequestException.ERR_PARAMETER));
        } catch (Throwable e){
            UHAnalytics.changeDataCount(String.valueOf(RequestException.ERR_NET_UNKOWN));
            onErrorResponse(new RequestException(RequestException.ERR_NET_UNKOWN));
        }
    }

    public void commit(Context context){
        if(!NetwokUtil.isNetworkAvaliable(context)){
            onErrorResponse(new RequestException(RequestException.ERR_NET_NO_CONNECTION));
        }else {
            RequestQueue.commitRequest(context, this);
            UHAnalytics.changeDataCount(UserHabitID.lmn_all);
        }
    }

    abstract public void buildRequestP(JSONObject request) throws RequestException;

    private void setHeader(HttpURLConnection conn){
        @SuppressWarnings("Convert2Diamond") Map<String, String> map = new HashMap<String, String>();
        buildHeader(map);
        for( Map.Entry<String, String> e : map.entrySet()){
            conn.setRequestProperty(e.getKey(), e.getValue());
        }
    }

    protected void buildHeader(Map<String, String> headers){
        headers.put("Charset", "UTF-8");
        headers.put("RequestVer", String.valueOf(RequestManager.REQUEST_VER));
        headers.put("RequestID", mRequestId);
        headers.put("Zip", String.valueOf(mCompresed));
        headers.put("Encryption", String.valueOf(mEncryption));
        headers.put("Connection", "Keep-Alive");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
    }

    private String buildReqestData(Context context) throws RequestException{
        JSONObject body = new JSONObject();
        try {
            body.put("common",  CommonParameter.build(context));
            JSONObject request = new JSONObject();
            buildRequestP(request);
            body.put("request", request);
        } catch (JSONException e) {
            throw new RequestException(RequestException.ERR_PARAMETER);
        }

        String request = body.toString();
        if (mCompresed){
            try {
                request = SSLUtilExt.getInstance().data_to_compress(request, request.length());
            } catch (SslException e) {
                throw new RequestException(RequestException.ERR_KEY_COMPRESS);
            }
        }

        if (mEncryption){
            try {
                request = SSLUtilExt.getInstance().data_to_encrypt(request);
            } catch (SslException e) {
                throw new RequestException(RequestException.ERR_KEY_ENCRYPTION);
            }
        }

        return request;
    }

    abstract protected void parseNetworkResponse(JSONObject response);

    abstract protected void onErrorResponse(RequestException e);

    private static boolean isStatusCodeError(int sc) {
        final int i = sc / 100;
        return i == 4 || i == 5;
    }
}
