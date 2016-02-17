package com.anguanjia.framework.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Parcelable;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class StorageUtils {

    public static ArrayList<StorageVolume> getStorageList(Context context) {
        ArrayList<StorageVolume> array = new ArrayList<StorageVolume>();
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            Object storageBinder = context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Method mt = storageBinder.getClass().getMethod("getVolumeList");
                Method mt_state = storageBinder.getClass().getMethod("getVolumeState", String.class);
                mt.setAccessible(true);
                Parcelable[] ps = (Parcelable[]) mt.invoke(storageBinder);
                if (ps != null) {
                    for (Parcelable parcelable : ps) {
                        StorageVolume item = (StorageVolume) parcelable;
                        String state = (String) mt_state.invoke(storageBinder, item.getPath());
                        if ("mounted".equals(state)) {
                            array.add(item);
                        }

                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
//                e.printStackTrace();
            }

        }
        return array;
    }
//
//	static public void TestApi(Context context) {
//		if (android.os.Build.VERSION.SDK_INT >= 14) {
//			Object storageBinder = context.getSystemService(Context.STORAGE_SERVICE);
//			try {
//				Method mt = storageBinder.getClass().getMethod("getVolumeList");
//				mt.setAccessible(true);
//				 mt.invoke(storageBinder);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//	}

    static public ArrayList<StorageVolume> getVolumeList(Context context) {
        ArrayList<StorageVolume> array = new ArrayList<StorageVolume>();
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            Object storageBinder = context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Method mt = storageBinder.getClass().getMethod("getVolumeList");
                Method mt_state = storageBinder.getClass().getMethod("getVolumeState", String.class);
                mt.setAccessible(true);
                Parcelable[] ps = (Parcelable[]) mt.invoke(storageBinder);
                if (ps != null) for (Parcelable parcelable : ps) {
                    StorageVolume item = (StorageVolume) parcelable;
                    String path = item.getPath().toLowerCase();

                    if (path.equals("/mnt/sdcard") || path.equals("/sdcard")) {
                        continue;
                    }

                    String state = (String) mt_state.invoke(storageBinder, new Object[]{item.getPath()});
                    if ("mounted".equals(state)) {
                        array.add(item);
                    }
                }
                }catch(Exception e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return array;
        }

        /**
         * 得到存储器的路径
         */
        public static ArrayList<String> getVolumePath (Context context){
            ArrayList<String> pathList = new ArrayList<String>();
            if (android.os.Build.VERSION.SDK_INT < 14) {
                boolean sdCardExist = Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED);
                if (sdCardExist) {
                    String sdDir;
                    sdDir = Environment.getExternalStorageDirectory().getPath();// 获取跟目录
                    pathList.add(sdDir);
                }
            } else {
                ArrayList<StorageVolume> list = StorageUtils.getStorageList(context);
                for (int i = 0; i < list.size(); i++) {
                    pathList.add(list.get(i).getPath());
                }
            }
            if (pathList.size() == 2) {
                String path_1 = pathList.get(0);
                String path_2 = pathList.get(1);
                if (path_1.startsWith(path_2)) {
                    pathList.remove(path_1);
                }
                if (path_2.startsWith(path_1)) {
                    pathList.remove(path_2);
                }
            }

            return pathList;
        }
    }
