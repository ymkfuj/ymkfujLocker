package com.anguanjia.framework.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TwoSdcardUtils {

	/**
	 * 多个SD卡时 取外置SD卡<br>
	 * 参考：http://blog.csdn.net/bbmiku/article/details/7937745<br>
	 * 
	 * @return
	 */
	public static List<String> getExternalStorageDirectory(Context context) {
		List<String> list = new ArrayList<String>();

		if (android.os.Build.VERSION.SDK_INT < 14) {
			// 手机存储空间(内置和外置)
			File path = Environment.getExternalStorageDirectory();

			if (path.exists()) {
				list.add(path.getPath());
			}
		} else {
			ArrayList<StorageVolume> list1 = StorageUtils
					.getStorageList(context);
			String path_1 = "", path_2 = "";
			for (int i = 0; i < list1.size(); i++) {
				// android4.0手机的外置存储器
				if (list1.get(i).isRemovable()) {					
					path_1 = list1.get(i).getPath();
					list.add(path_1);				
				}

				// android 4.0手机的内置存储器
				if (!list1.get(i).isRemovable()) {					
					path_2 = list1.get(i).getPath();
					list.add(0,path_2);
				}
			}
		}

		//去除包含的目录
		for (int i = list.size() - 1; i >= 0; --i) {
			final String q = list.get(i);

			boolean e = false;

			for (int j = list.size() - 1; j >= 0; --j) {
				if (i == j) {
					continue;
				}

				String p = list.get(j);
				if (!p.endsWith("/")) {
					p = p + "/";
				}

				if (q.startsWith(p)) {
					e = true;
					break;
				}
			}
			
			if(e){
				list.remove(i);
			}
		}
		

		return list;
	}
	
	public static boolean checkCanModify(String dir) {
		boolean ret = false; 
		File cacheDir = new File(dir+ "/anguanjia.txt");
		if(cacheDir.exists()){
			cacheDir.delete();
		}
		cacheDir.mkdirs();
		if(cacheDir.exists()){
			ret = true;
			cacheDir.delete();
		}else{
			ret = false;
		}
		return ret;
	}
}
