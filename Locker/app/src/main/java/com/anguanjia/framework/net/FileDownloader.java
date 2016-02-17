package com.anguanjia.framework.net;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.anguanjia.framework.utils.MD5Util;
import com.anguanjia.framework.utils.PhoneInfoUtil;
import com.anguanjia.framework.thread.ThreadPool;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

abstract public class FileDownloader{
    public static final int ERR_MD5 = 100;
    public static final int ERR_NET = 101;
    public static final int ERR_UNKONW = 199;

    private String mUrl;
    private String file;
    private String md5;

    public FileDownloader(String url, String file, String md5){
        this.mUrl = url;
        this.file = file;
        this.md5 = md5;
    }

    public void start(final Context context){
        ThreadPool.mService.execute(new Runnable() {
            @Override
            public void run() {
                FileDownloader.this.download(context);
            }
        });
    }

    void download(Context context){
        try {
            // 获取文件名
            URL myURL = new URL(mUrl);

            Proxy proxy = null;

            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = null;
            if(connectivityManager!=null){
                try {
                    activeNetInfo = connectivityManager.getActiveNetworkInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (activeNetInfo != null && activeNetInfo.getType() == 0) {
                Cursor mCursor = null ;
                try {
                    Uri uri = Uri.parse("content://telephony/carriers/preferapn"); // 获取当前正在使用的APN接入点
                    mCursor = context.getContentResolver().query(uri, null,
                            null, null, null);
                    if (mCursor != null) {
                        mCursor.moveToNext(); // 游标移至第一条记录，当然也只有一条
                        String proxyStr = mCursor.getString(mCursor
                                .getColumnIndex("proxy"));
                        if (proxyStr != null && proxyStr.trim().length() > 0) {
                            proxy = new Proxy(Proxy.Type.HTTP,
                                    new InetSocketAddress(proxyStr, 80));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    if (mCursor != null){
                        mCursor.close();
                    }
                }
            }

            URLConnection conn;

            if (proxy != null)
                conn = myURL.openConnection(proxy);
            else
                conn = myURL.openConnection();

            conn.addRequestProperty("Referer", myURL.getHost());

            conn.setConnectTimeout(5000);

            String ua = "JUC (Linux; U; " + Build.VERSION.RELEASE + "; zh-cn; "
                    + Build.MODEL + "; "
                    + PhoneInfoUtil.getDisplayMetrics(context)
                    + ") UCWEB7.9.3.103/139/999";

            conn.addRequestProperty("User-Agent", ua);
            conn.connect();
            InputStream is = conn.getInputStream();
            int mFileSize = conn.getContentLength();// 根据响应获取文件大小
            if (mFileSize <= 0) {
                onErr(ERR_MD5);
                return;
            }
            if (is == null) {
                onErr(ERR_NET);
                return;
            }
            
            FileOutputStream fos = new FileOutputStream(file);

            // 把数据存入路径+文件名
            byte buf[] = new byte[1024];
            int mDownLoadFileSize = 0;
            do {
                // 循环读取
                int numread = is.read(buf);
                if (numread == -1) {
                    break;
                }
                fos.write(buf, 0, numread);
                mDownLoadFileSize += numread;

            } while (true);

            try {
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            if(mDownLoadFileSize != mFileSize){
                onErr(ERR_MD5);
                return;
            }

            if (!TextUtils.isEmpty(this.md5)){
                MD5Util util = new MD5Util();
                String dmd5 = util.getFileMD5String(file);
                if (!dmd5.equals(md5)){
                    onErr(ERR_MD5);
                    return;
                }
            }

            onFinish();
        } catch (Throwable e) {
            onErr(ERR_UNKONW);
        }
    }

    protected abstract void onErr(int err);

    protected abstract void onFinish();
}
