package com.anguanjia.framework.utils;

import android.os.Build.VERSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Android Zip压缩解压缩
 */
public class XZip {

	public XZip() {

	}

	/**
	 * 取得压缩包中的 文件列表(文件夹,文件自选)
	 *
	 * @param zipFileString
	 *            压缩包名字
	 * @param bContainFolder
	 *            是否包括 文件夹
	 * @param bContainFile
	 *            是否包括 文件
	 */
	public static List<File> GetFileList(String zipFileString,
			boolean bContainFolder, boolean bContainFile) throws Exception {

		List<File> fileList = new ArrayList<File>();
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(
				zipFileString));
		ZipEntry zipEntry;
		String szName = "";

		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();

			if (zipEntry.isDirectory()) {

				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				File folder = new File(szName);
				if (bContainFolder) {
					fileList.add(folder);
				}

			} else {
				File file = new File(szName);
				if (bContainFile) {
					fileList.add(file);
				}
			}
		}// end of while

		inZip.close();

		return fileList;
	}

	public static void UpZip(String zipFileString, String fileString,
			String desFileString) throws Exception {
		ZipFile zipFile = new ZipFile(zipFileString);
		ZipEntry zipEntry = zipFile.getEntry(fileString);

		InputStream inZip = zipFile.getInputStream(zipEntry);

		File file = new File(desFileString);
		file.createNewFile();
		// get the output stream of the file
		FileOutputStream out = new FileOutputStream(file);
		int len;
		byte[] buffer = new byte[1024];
		// read (len) bytes into buffer
		while ((len = inZip.read(buffer)) != -1) {
			// write (len) byte from buffer at the position 0
			out.write(buffer, 0, len);
			out.flush();
		}
		out.close();

		inZip.close();
		zipFile.close();
	}

	/**
	 * 解压一个压缩文档 到指定位置
	 * 
	 * @param zipFileString
	 *            压缩包的名字
	 * @param outPathString
	 *            指定的路径
	 * @throws Exception
	 */
	public static void UnZipFolder(String zipFileString, String outPathString)
			throws Exception {
		ZipInputStream inZip = new ZipInputStream(new FileInputStream(
				zipFileString));
		ZipEntry zipEntry;
		String szName;

		try {
			while ((zipEntry = inZip.getNextEntry()) != null) {
				szName = zipEntry.getName();
				// 过滤掉不可写的文件
				if (szName.contains("lib") || szName.contains("opt")) {
					continue;
				}
				if (zipEntry.isDirectory()) {
					// get the folder name of the widget
					szName = szName.substring(0, szName.length() - 1);
					File folder = new File(outPathString + File.separator
							+ szName);
					folder.mkdirs();
					if (VERSION.SDK_INT >= 9) {
						folder.setWritable(true);
					}
				} else {

					File file = new File(outPathString + File.separator
							+ szName);
					// 当目录不存在时，应该创建
					file.getParentFile().mkdirs();
					if (VERSION.SDK_INT >= 9) {
						file.setWritable(true);
					}
					file.createNewFile();
					FileOutputStream out = new FileOutputStream(file);
					int len;
					byte[] buffer = new byte[1024];
					// read (len) bytes into buffer
					while ((len = inZip.read(buffer)) != -1) {
						// write (len) byte from buffer at the position 0
						out.write(buffer, 0, len);
						out.flush();
					}
					out.close();

					// get the output stream of the file

				}
			}// end of while
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			inZip.close();
		}

	}// end of func

	/**
	 * 压缩文件,文件夹
	 * 
	 * @param srcFileString
	 *            要压缩的文件/文件夹名字
	 * @param zipFileString
	 *            指定压缩的目的和名字
	 * @throws Exception
	 */
	public static void ZipFolder(String srcFileString, String zipFileString)
			throws Exception {

		// 创建Zip包
		// 当这个目录结构不存在的时候，应该创建
		File mfile = new File(zipFileString);
		mfile.getParentFile().mkdirs();
		mfile.createNewFile();
		FileOutputStream fileOutputStream = new FileOutputStream(mfile);
		ZipOutputStream outZip = new ZipOutputStream(fileOutputStream);

		// 打开要输出的文件
		File file = new File(srcFileString);

		// 压缩
		ZipFiles(file.getParent() + File.separator, file.getName(), outZip);

		// 完成,关闭
		outZip.finish();
		outZip.close();
	}// end of func

	public static void ZipFiles(String[] srcFileString, String zipFileString)
			throws Exception {

		// 创建Zip包
		ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(
				zipFileString));

		for (String string : srcFileString) {
			ZipFile(string, outZip);
		}

		// 完成,关闭
		outZip.finish();
		outZip.close();
	}// end of func

	public static void ZipFile(String srcFileString, String zipFileString)
			throws Exception {
		// 创建Zip包
		ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(
				zipFileString));

		ZipFile(srcFileString, outZip);

		// 完成,关闭
		outZip.finish();
		outZip.close();
	}// end of func

	private static void ZipFile(String fileString,
			ZipOutputStream zipOutputSteam) throws Exception {
		if (zipOutputSteam == null)
			return;

		File file = new File(fileString);
		if (file.exists()) {
			ZipEntry zipEntry = new ZipEntry(file.getName());
			FileInputStream inputStream = new FileInputStream(file);
			zipOutputSteam.putNextEntry(zipEntry);

			int len;
			byte[] buffer = new byte[4096];

			while ((len = inputStream.read(buffer)) != -1) {
				zipOutputSteam.write(buffer, 0, len);
			}
			zipOutputSteam.closeEntry();
		}

	}// end of func

	/**
	 * 压缩文件
	 */
	private static void ZipFiles(String folderString, String fileString,
			ZipOutputStream zipOutputSteam) throws Exception {
		if (zipOutputSteam == null)
			return;

		File file = new File(folderString + fileString);

		// 判断是不是文件
		if (file.isFile()) {

			ZipEntry zipEntry = new ZipEntry(fileString);
			FileInputStream inputStream = new FileInputStream(file);
			zipOutputSteam.putNextEntry(zipEntry);

			int len;
			byte[] buffer = new byte[4096];

			while ((len = inputStream.read(buffer)) != -1) {
				zipOutputSteam.write(buffer, 0, len);
			}

			zipOutputSteam.closeEntry();
		} else {

			// 文件夹的方式,获取文件夹下的子文件
			String fileList[] = file.list();
			if(fileList != null){
				// 如果没有子文件, 则添加进去即可
				if (fileList.length <= 0) {
					ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
					zipOutputSteam.putNextEntry(zipEntry);
					zipOutputSteam.closeEntry();
				}

				// 如果有子文件, 遍历子文件
				for (String aFileList : fileList) {
					ZipFiles(folderString, fileString + File.separator
							+ aFileList, zipOutputSteam);
				}// end of for
			}
			
		}// end of if

	}// end of func

	public void finalize() throws Throwable {

	}
}
