package com.anguanjia.framework.userhabit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 实时存入数据库
 */
public class UHRecorderDB extends UHRecorderMem {
    private static final String TB_UH_INT = "PICTUH_INT";       //value为整型
    private static final String TB_UH_LONG = "PICTUH_LONG";     //value为Long
    private static final String TB_UH_STRING = "PICTUH_STRING";//value为String

    private static final String TB_UH_COL_KEY = "k";        //KEY
    private static final String TB_UH_COL_VALUE = "v";      //VALUE
    private static final String TB_UH_COL_TIME = "tm";      //插入时间
    private static final String TB_UH_COL_TYPE = "tp";      //UH类型 5种
    private static final String TB_UH_COL_OPTYPE = "otp";   //合并操作类型：覆盖，累加

    private static final int TB_UH_OPTYPE_OVERRIDE = 0; //覆盖
    private static final int TB_UH_OPTYPE_ACC = 1;//叠加

    UHDBHelper mDbService;
    UHRecorderDB(Context context){
       mDbService = new UHDBHelper(context);
    }
    @Override
    public boolean changeUHOpen(String key) {
        if (super.changeUHOpen(key)){
            insertInt(key, 1, UHAnalytics.UH_TYPE2, TB_UH_OPTYPE_OVERRIDE);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeUHClose(String key) {
        if (super.changeUHClose(key)){
            insertInt(key, 0, UHAnalytics.UH_TYPE2, TB_UH_OPTYPE_OVERRIDE);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeDataCount(String key) {
        if (super.changeDataCount(key)){
            insertInt(key, 1, UHAnalytics.UH_TYPE1, TB_UH_OPTYPE_ACC);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeSetDataCount(String key, String setItem) {
        if (super.changeSetDataCount(key, setItem)){
            insertString(key, setItem, UHAnalytics.UH_TYPE3, TB_UH_OPTYPE_OVERRIDE);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeContentDataCount(String key, String content) {
        if (super.changeContentDataCount(key, content)){
            insertString(key, content, UHAnalytics.UH_TYPE5, TB_UH_OPTYPE_OVERRIDE);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeAccumulateContentDataCount(String key, String content) {
        if (super.changeAccumulateContentDataCount(key, content)){
            insertString(key, content, UHAnalytics.UH_TYPE5, TB_UH_OPTYPE_ACC);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeNumDataCount(String key, long num) {
        if (super.changeNumDataCount(key, num)){
            insertLong(key, num, UHAnalytics.UH_TYPE4, TB_UH_OPTYPE_OVERRIDE);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeAccumulateNumDataCount(String key, long num) {
        if (super.changeAccumulateNumDataCount(key, num)){
            insertLong(key, num, UHAnalytics.UH_TYPE4, TB_UH_OPTYPE_ACC);
            return true;
        }
        return false;
    }

    @Override
    public boolean clear(long time) {
        if (super.clear(time)){
            removeall(time);//先删除掉已经上传的
            load();//再将剩余的加载到内存中
            return true;
        }
        return false;
    }

    @Override
    public boolean save() {
        return false;
    }

    @Override
    public boolean load() {
        mIsLoading = true;
        loadValueFromCursor(mDbService.queryAll(TB_UH_INT));
        loadValueFromCursor(mDbService.queryAll(TB_UH_LONG));
        loadValueFromCursor(mDbService.queryAll(TB_UH_STRING));
        mIsLoading = false;
        return true;
    }

    void loadValueFromCursor(Cursor c){
        if(c != null){
            if(c.moveToFirst()) {
                do {
                    int iType = c.getColumnIndex(TB_UH_COL_TYPE);
                    int iKey = c.getColumnIndex(TB_UH_COL_KEY);
                    int iValue = c.getColumnIndex(TB_UH_COL_VALUE);
                    int iOp = c.getColumnIndex(TB_UH_COL_OPTYPE);
                    int type = c.getInt(iType);
                    String key = c.getString(iKey);
                    int op = c.getInt(iOp);
                    switch (type) {
                        case UHAnalytics.UH_TYPE1: {
                            super.changeDataCount(key);
                            break;
                        }
                        case UHAnalytics.UH_TYPE2: {
                            int value = c.getInt(iValue);
                            mapType2.put(key, value);
                            break;
                        }
                        case UHAnalytics.UH_TYPE3: {
                            String value = c.getString(iValue);
                            super.changeSetDataCount(key, value);
                            break;
                        }
                        case UHAnalytics.UH_TYPE4: {
                            long value = c.getLong(iValue);
                            if (op == TB_UH_OPTYPE_OVERRIDE) {
                                super.changeNumDataCount(key, value);
                            } else {
                                super.changeAccumulateNumDataCount(key, value);
                            }
                            break;
                        }
                        case UHAnalytics.UH_TYPE5: {
                            String value = c.getString(iValue);
                            if (op == TB_UH_OPTYPE_OVERRIDE) {
                                super.changeContentDataCount(key, value);
                            } else {
                                super.changeAccumulateContentDataCount(key, value);
                            }
                            break;
                        }
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    private void insertInt(String key, int v, int type, int optype){
        ContentValues cv = new ContentValues();
        cv.put(TB_UH_COL_KEY, key);
        cv.put(TB_UH_COL_VALUE, v);
        cv.put(TB_UH_COL_TYPE, type);
        cv.put(TB_UH_COL_OPTYPE, optype);
        cv.put(TB_UH_COL_TIME, System.currentTimeMillis());
        mDbService.insert(TB_UH_INT, cv);
    }
    private void insertLong(String key, long v, int type, int optype){
        ContentValues cv = new ContentValues();
        cv.put(TB_UH_COL_KEY, key);
        cv.put(TB_UH_COL_VALUE, v);
        cv.put(TB_UH_COL_TYPE, type);
        cv.put(TB_UH_COL_OPTYPE, optype);
        cv.put(TB_UH_COL_TIME, System.currentTimeMillis());
        mDbService.insert(TB_UH_LONG, cv);
    }
    private void insertString(String key, String v, int type, int optype){
        ContentValues cv = new ContentValues();
        cv.put(TB_UH_COL_KEY, key);
        cv.put(TB_UH_COL_VALUE, v);
        cv.put(TB_UH_COL_TYPE, type);
        cv.put(TB_UH_COL_OPTYPE, optype);
        cv.put(TB_UH_COL_TIME, System.currentTimeMillis());
        mDbService.insert(TB_UH_STRING, cv);
    }

    private void removeall(long time){
        mDbService.removeall(time);
    }

    class UHDBHelper extends SQLiteOpenHelper {
        UHDBHelper(Context context){
            super(context, "uhrecorder", null, 1);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_UH_INT + " (_id INTEGER PRIMARY KEY," +  TB_UH_COL_KEY + " INTENGER," +  TB_UH_COL_VALUE + " INTENGER,"+ TB_UH_COL_TYPE + " INTENGER,"+ TB_UH_COL_OPTYPE + " INTENGER," + TB_UH_COL_TIME + " BIGINT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_UH_LONG + " (_id INTEGER PRIMARY KEY," +  TB_UH_COL_KEY + " INTENGER," +  TB_UH_COL_VALUE + " BIGINT,"+ TB_UH_COL_TYPE + " INTENGER,"+ TB_UH_COL_OPTYPE + " INTENGER," +TB_UH_COL_TIME + " BIGINT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_UH_STRING + " (_id INTEGER PRIMARY KEY," + TB_UH_COL_KEY + " INTENGER," + TB_UH_COL_VALUE + " TEXT," + TB_UH_COL_TYPE + " INTENGER," + TB_UH_COL_OPTYPE + " INTENGER," + TB_UH_COL_TIME + " BIGINT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        synchronized void removeall(long time){
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TB_UH_INT, TB_UH_COL_TIME + "<=?", new String[]{time + ""});
            db.delete(TB_UH_LONG, TB_UH_COL_TIME + "<=?", new String[]{time + ""});
            db.delete(TB_UH_STRING, TB_UH_COL_TIME + "<=?", new String[]{time+""});
        }

        synchronized void insert(String tb, ContentValues cv){
            SQLiteDatabase db = getWritableDatabase();
            try {
                db.insert(tb,null, cv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        synchronized Cursor queryAll(String tb){
            SQLiteDatabase db = getReadableDatabase();
            return db.rawQuery("select * from " + tb, null);
        }
    }
}
