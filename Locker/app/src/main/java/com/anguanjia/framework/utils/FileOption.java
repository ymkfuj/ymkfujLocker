package com.anguanjia.framework.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileOption {

	static public boolean copyFileFromAssets(Context aContext, String aSrc,
			String aTar, boolean bForce) {
		File src = new File(aTar);
		if (src.exists()) {
			if (bForce)
				src.delete();
			else
				return true;
		}
		MyLog.e("cfver", "copyFileFromAssets:" + aSrc);
		try {
			src.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		AssetManager assetManager = aContext.getAssets();

		try {
			InputStream inputStream = assetManager.open(aSrc);
			OutputStream outStream = new FileOutputStream(aTar);
			BufferedInputStream bin;
			BufferedOutputStream bout;

			bin = new BufferedInputStream(inputStream);

			bout = new BufferedOutputStream(outStream);

			byte[] b = new byte[1024];

			int len = bin.read(b);

			while (len != -1) {
				bout.write(b, 0, len);
				len = bin.read(b);
			}

			bin.close();
			bout.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectorys(String sPath) {
		boolean flag;
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		if(files != null){
			for (File file : files) {
				// 删除子文件
				if (file.isFile()) {
					flag = deleteFile(file.getAbsolutePath());
					if (!flag)
						break;
				} // 删除子目录
				else {
					flag = deleteDirectorys(file.getAbsolutePath());
					if (!flag)
						break;
				}
			}
		}		
		if (!flag)
			return false;
		// 删除当前目录
		return dirFile.delete();
	}

	/**
	 * 删除单个文件
	 *
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}
}
