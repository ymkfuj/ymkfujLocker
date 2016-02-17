package com.anguanjia.framework.userhabit;

public interface IUHRecorder {
    boolean changeUHOpen(String key);

    boolean changeUHClose(String key);

    boolean changeDataCount(String key);

    boolean changeSetDataCount(String key, String setItem);

    boolean changeContentDataCount(String key, String content);

    boolean changeAccumulateContentDataCount(String key, String content);

    boolean changeNumDataCount(String key, long num);

    boolean changeAccumulateNumDataCount(String key, long num);

    boolean clear(long time);

    boolean save();

    boolean load();

    String getUHString(int uhType);

    void setTime(long time);

    long getTime();

    boolean isEmpty();
}
