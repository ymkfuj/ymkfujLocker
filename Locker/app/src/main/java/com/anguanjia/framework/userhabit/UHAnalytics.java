package com.anguanjia.framework.userhabit;

import java.util.HashMap;
import java.util.Map;

public class UHAnalytics {

    public static final int UH_TYPE1 = 0;
    public static final int UH_TYPE2 = 1;
    public static final int UH_TYPE3 = 2;
    public static final int UH_TYPE4 = 3;
    public static final int UH_TYPE5 = 4;

    public static void setRecorder(IUHRecorder recorder){
        mRecorder = recorder;
    }

    /**
     * H_type 2 Open
     */
    public static void changeUHOpen(String key) {
        if (isEnabled())
            getRecorder().changeUHOpen(key);
    }

    /**
     * H_type 2 Close
     */
    public static void changeUHClose(String key) {
        if (isEnabled())
            getRecorder().changeUHClose(key);
    }

    /**
     * H_type1
     */
    public static void changeDataCount(String key) {
        if (isEnabled())
            getRecorder().changeDataCount(key);
    }

    /**
     * H_type3
     */
    public static void changeSetDataCount(String key, String setItem) {
        if (isEnabled())
            getRecorder().changeSetDataCount(key, setItem);
    }

    /**
     * H_type5不累加
     */
    public static void changeContentDataCount(String key, String content) {
        if (isEnabled())
            getRecorder().changeContentDataCount(key, content);
    }

    /**
     * H_type5去重
     * content以"||"开头，以"||"分割
     */
    public static void changeAccumulateContentDataCount(String key, String content) {
        if (isEnabled())
            getRecorder().changeAccumulateContentDataCount(key, content);
    }

    /**
     * H_type4不累加
     */
    public static void changeNumDataCount(String key, long num) {
        if (isEnabled())
            getRecorder().changeNumDataCount(key, num);
    }

    /**
     * H_type4累加
     */
    public static void changeAccumulateNumDataCount(String key, long num) {
        if (isEnabled())
            getRecorder().changeAccumulateNumDataCount(key, num);
    }

    private static Map<String,Long> mResidentTime = new HashMap<String, Long>();


    /**
     * H_type4累加
     * 驻留时间开始
     */
    public static void startResident(String key){
        mResidentTime.put(key, System.currentTimeMillis());
    }

    /**
     * H_type4累加
     * 驻留时间结束
     */
    public static void stopResident(String key){
        if (mResidentTime.containsKey(key)) {
            long t = System.currentTimeMillis() - mResidentTime.get(key);
            if (t > 0) {
                getRecorder().changeAccumulateNumDataCount(key, t);
            }
        }
    }

    public static boolean isEmpty(){
        return getRecorder().isEmpty();
    }

    public static String getUHString(int type){
        return getRecorder().getUHString(type);
    }

    public static long getBeginTime(){
        return getRecorder().getTime();
    }


    public static void clear(long time){
        getRecorder().clear(time);
    }

    public static void save() {
        if (isEnabled())
            getRecorder().save();
    }

    public static void load(){
        if (isEnabled()){
            getRecorder().load();
        }
    }

    private static IUHRecorder mRecorder;

    private static IUHRecorder getRecorder() {
        return mRecorder;
    }

    private static boolean isEnabled() {
        return mIsEnabled;
    }

    private static boolean mIsEnabled = true;
}
