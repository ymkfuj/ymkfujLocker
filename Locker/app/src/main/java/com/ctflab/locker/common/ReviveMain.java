package com.ctflab.locker.common;

import android.content.Context;
import android.os.Build;

import com.anguanjia.framework.utils.MyLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 进程复活
 */
public class ReviveMain {
	private static final String DaemonProcessName = "htr_kworker";
    private static File APP_LOCK_DIR = new File("/data/data/com.ctflab.locker/");

	public static void main(String[] args) {
		Method setArgV0;
		MyLog.d("hejw","start main");
		try {
			setArgV0 = android.os.Process.class.getDeclaredMethod("setArgV0",
					new Class[] { String.class });
			setArgV0.setAccessible(true);
			setArgV0.invoke(android.os.Process.class,
					new Object[] { DaemonProcessName });
		} catch (Exception e1) {
			e1.printStackTrace();
			MyLog.d("hejw", "start main exception:"+ e1.getMessage());
		}
		// 主进程ID
		final int hostProcessID;
		if (args.length > 0) {
			try {
				hostProcessID = Integer.parseInt(args[0]);
				MyLog.d(DaemonProcessName, "host pid:" + hostProcessID);
			} catch (NumberFormatException e) {
				MyLog.d(DaemonProcessName, "UnKnow pid");
				return;
			}
		} else {
			MyLog.d(DaemonProcessName, "Args not find");
			return;
		}

		while (true) {
			MyLog.d(DaemonProcessName, " RebootMain******");
			try {
				File hostProcess = new File("/proc/" + hostProcessID);
				if (!hostProcess.exists()) {
                    if(APP_LOCK_DIR.exists()){
                        // 证明主进程已经被kill掉，所以重新启动服务
                        MyLog.d(DaemonProcessName, "start cmdLine ******");
                        String cmdLine = "am startservice --user 0 -a com.ctflab.locker.daemon";
                        if (Build.VERSION.SDK_INT <= 15) {
                            cmdLine = "am startservice -a com.ctflab.locker.daemon";
                        }
                        try {
                            Runtime.getRuntime().exec(cmdLine);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
					MyLog.d(DaemonProcessName, "killed myself---------");
					android.os.Process.killProcess(android.os.Process.myPid());
				}
				Thread.sleep(10000);// 十秒
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 得到管家守护进程的ID
	 */
	public static int getDaemonProcessID() {
		for (File processDir : getAllProcessDirs()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(new File(processDir, "cmdline"))));
				String processName = br.readLine();
				if (processName == null) {
					continue;
				}
				processName = processName.trim();
				if (DaemonProcessName.equals(processName)) {
					try {
						return Integer.parseInt(processDir.getName());
					} catch (NumberFormatException e) {
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return -1;
	}

	private static File[] getAllProcessDirs() {
		File procDir = new File("/proc");
		return procDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				try {
					Integer.parseInt(pathname.getName());
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		});
	}

	public static void startDaemonProcess(Context context) {
		MyLog.d(DaemonProcessName, "startDaemonProcess");
		int daemonPid = getDaemonProcessID();
		MyLog.d("hejw","id:"+daemonPid);
		if (daemonPid == -1) {
			try {
				// System.getenv()读取系统环境变量
				Map<String, String> envMap = new HashMap<String, String>(
						System.getenv());
				envMap.put("CLASSPATH", context.getApplicationInfo().sourceDir);
				String[] envs = new String[envMap.size()];
				int index = 0;
				for (Entry<String, String> entry : envMap.entrySet()) {
					envs[index++] = entry.getKey() + "=" + entry.getValue();
				}
				// app_process用于启动java类,最终会调main函数
				Runtime.getRuntime().exec(
						"/system/bin/app_process /system/bin "
								+ ReviveMain.class.getName() + " "
								+ android.os.Process.myPid() + "\n", envs);
				MyLog.d(DaemonProcessName, "startDaemonProcess finish");
			} catch (IOException e) {
				e.printStackTrace();
				MyLog.d("hejw","exception:"+e.getMessage());
			}
		}
	}

	/**
	 * 停止守护进程
	 */
	public static void stopDaemonProcess() {
		int daemonPid = getDaemonProcessID();
		if (daemonPid != -1) {
			android.os.Process.killProcess(daemonPid);
		}
	}
}
