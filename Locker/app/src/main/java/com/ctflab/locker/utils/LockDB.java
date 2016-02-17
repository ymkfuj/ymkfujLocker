package com.ctflab.locker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by wuwei on 2016/1/7.
 */
public class LockDB extends SQLiteOpenHelper {

    static final String TB_NAME = "lock_state";
    static final String ID = "_id";
    static final String L_PACKAGE = "package";

    static final int version = 1;

    static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TB_NAME + "(" + ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    L_PACKAGE + " TEXT )";

    private SQLiteDatabase mDefaultWritableDatabase = null;

    public LockDB(Context context) {
        super(context, TB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
        this.mDefaultWritableDatabase = db;
        insertDefaultLock();
    }
    @Override
    public SQLiteDatabase getWritableDatabase() {
        final SQLiteDatabase db;
        if(mDefaultWritableDatabase != null){
            db = mDefaultWritableDatabase;
        } else {
            db = super.getWritableDatabase();
        }
        return db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.mDefaultWritableDatabase = db;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.mDefaultWritableDatabase = db;
    }

    public synchronized void addLockedApp(String packageName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(L_PACKAGE, packageName);
        db.insert(TB_NAME, null, values);
//        db.close();
    }

    public synchronized void removedLockedApp(String packageName) {
        SQLiteDatabase db = getReadableDatabase();
        db.delete(TB_NAME, L_PACKAGE + " = ?", new String[]{packageName});
//        db.close();
    }

    public synchronized ArrayList<String> getAllLockedApp() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> list = new ArrayList<>();
        Cursor c = db.query(TB_NAME, null, null, null, null, null, null);
        while (c.moveToNext()) {
            list.add(c.getString(c.getColumnIndex(L_PACKAGE)));
        }

        c.close();
//        db.close();
        return list;
    }

    public synchronized void refreshApp(String packageName, boolean isLocked) {
        if (isLocked) {
            addLockedApp(packageName);
        } else {
            removedLockedApp(packageName);
        }

        AppListUtil.locksAppList = getAllLockedApp();
    }

    private synchronized void insertDefaultLock() {
        addLockedApp("com.google.android.packageinstaller");
        addLockedApp("com.android.packageinstaller");
        addLockedApp("com.android.settings");
        addLockedApp("com.android.vending");
    }
}
