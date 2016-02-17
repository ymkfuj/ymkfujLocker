package com.anguanjia.framework.utils;

public class FileUtils {
    public static String getFileName(String path) {
        if (null == path) {
            return "";
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
