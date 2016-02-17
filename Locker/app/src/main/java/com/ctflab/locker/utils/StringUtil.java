package com.ctflab.locker.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wuwei on 2016/1/8.
 */
public class StringUtil {

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    /**
     * 确认字符串是否为email格式
     *
     * @param strEmail
     * @return
     */
    public static boolean isEmail(String strEmail) {
//        String strPattern = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//        Pattern p = Pattern.compile(strPattern);
//        Matcher m = p.matcher(strEmail);
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

}
