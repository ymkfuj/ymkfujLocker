package com.anguanjia.framework.net;

public class RequestManager {
    public static String REQUEST_URL = "http://applock.ctflab.com/test.cgi";//"http://applock.ctflab.com/applocker.cgi";
    public static final int REQUEST_VER = 3;

    public static void init(boolean isTest){
        if (isTest){
            REQUEST_URL = "http://applock.ctflab.com/test.cgi";
        }else{
            REQUEST_URL = "http://applock.ctflab.com/applocker.cgi";
        }
    }
}
