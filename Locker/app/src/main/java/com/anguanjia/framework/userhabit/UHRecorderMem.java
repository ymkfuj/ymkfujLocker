package com.anguanjia.framework.userhabit;


import com.anguanjia.framework.AnguanjiaFramewok;
import com.anguanjia.framework.preferece.CommonPreference;

import java.util.HashMap;
import java.util.Map;

/**
 * 只记录到内存
 */
public class UHRecorderMem implements IUHRecorder {
    Map<String, Integer> mapType1 = new HashMap<String, Integer>();
    Map<String, Integer> mapType2 = new HashMap<String, Integer>();
    Map<String, String> mapType3 = new HashMap<String, String>();
    Map<String, Long> mapType4 = new HashMap<String, Long>();
    Map<String, String> mapType5 = new HashMap<String, String>();

    boolean mIsLoading = true;

    @Override
    public boolean changeUHOpen(String key) {
        if (key == null || key.length() == 0) {
            return false;
        }
        synchronized (this) {
            mapType2.put(key, 1);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean changeUHClose(String key) {
        if (key == null || key.length() == 0) {
            return false;
        }
        synchronized (this) {
            mapType2.put(key, 0);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean changeDataCount(String key) {
        if (key == null || key.length() == 0) {
            return false;
        }
        synchronized (this) {
            Integer cur = mapType1.get(key);
            cur = cur != null ? cur + 1 : 1;
            mapType1.put(key, cur);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean changeSetDataCount(String key, String setItem) {
        if (key == null || key.length() == 0 || setItem == null) {
            return false;
        }
        synchronized (this) {
            mapType3.put(key, setItem);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean changeContentDataCount(String key, String content) {
        if (key == null || key.length() == 0 || content == null) {
            return false;
        }
        synchronized (this) {
            mapType5.put(key, content);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean changeAccumulateContentDataCount(String key, String content) {
        if (key == null || key.length() == 0 || content == null || !content.startsWith("||")) {
            return false;
        }
        synchronized (this) {
            String realContent = content.substring(2);
            String cur = mapType5.get(key);
            if (cur != null) {
                String[] contents = cur.split("\\|\\|");
                for (String str : contents) {
                    if (str.equals(realContent)) {
                        return false;
                    }
                }
                mapType5.put(key, cur + content);
                onContentChanged();
            } else {
                mapType5.put(key, realContent);
                onContentChanged();
            }
        }
        return true;
    }

    @Override
    public boolean changeNumDataCount(String key, long num) {
        if (key == null || key.length() == 0) {
            return false;
        }
        synchronized (this) {
            mapType4.put(key, num);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean changeAccumulateNumDataCount(String key, long num) {
        if (key == null || key.length() == 0) {
            return false;
        }
        synchronized (this) {
            Long cur = mapType4.get(key);
            cur = cur != null ? cur + num : num;
            mapType4.put(key, cur);
            onContentChanged();
        }
        return true;
    }

    @Override
    public boolean clear(long time) {
        synchronized (this) {
            mapType1.clear();
            mapType2.clear();
            mapType3.clear();
            mapType4.clear();
            mapType5.clear();
            setTime(time);
        }
        return true;
    }

    @Override
    public boolean save() {
        return false;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Override
    public String getUHString(int uhType) {
        switch (uhType) {
            case UHAnalytics.UH_TYPE1:
                return mapToString(mapType1);
            case UHAnalytics.UH_TYPE2:
                return mapToString(mapType2);
            case UHAnalytics.UH_TYPE3:
                return mapToString(mapType3);
            case UHAnalytics.UH_TYPE4:
                return mapToString(mapType4);
            case UHAnalytics.UH_TYPE5:
                return mapToString(mapType5);
        }
        return "";
    }

    private String mapToString(Map<String, ?> map) {
        StringBuilder type1 = new StringBuilder();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            type1.append(entry.getKey());
            type1.append("=");
            type1.append(entry.getValue());
            type1.append(";");
        }
        return type1.toString();
    }


    @Override
    public void setTime(long time) {
        CommonPreference.getDefault(AnguanjiaFramewok.mApplicationContext).saveLong("uh_time", time);
    }

    @Override
    public long getTime() {
        long t =  CommonPreference.getDefault(AnguanjiaFramewok.mApplicationContext).loadLong("uh_time", 0);
        if (t == 0){
            t = System.currentTimeMillis();
            CommonPreference.getDefault(AnguanjiaFramewok.mApplicationContext).saveLong("uh_time", t);
        }
        return t;
    }

    @Override
    public boolean isEmpty() {
        return mIsLoading || (mapType1.isEmpty() && mapType2.isEmpty() && mapType3.isEmpty() && mapType4.isEmpty() && mapType5.isEmpty());
    }

    void onContentChanged() {
    }
}
