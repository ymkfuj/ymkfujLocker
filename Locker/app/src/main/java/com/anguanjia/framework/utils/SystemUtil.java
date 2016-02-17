package com.anguanjia.framework.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SystemUtil {
    /**
     * load so 库的统一方法,后续可以封装保证成功率
     *
     * @param aLibNm so 的文件名:例如：libaes128.so 只需传入aes128
     * @return 是否load 成功
     */
    public static void loadLib(Context aContext, String aLibNm) {
        boolean vRet = false;
        if (aLibNm != null && aLibNm.length() > 0) {
            if (aContext != null) {
                String vAbsolutePath = aContext.getFilesDir().getParentFile().getAbsolutePath();
                if (vAbsolutePath != null && vAbsolutePath.length() > 0) {
                    String vLib = vAbsolutePath + "/lib/lib" + aLibNm + ".so";
                    if (new File(vLib).exists()) {
                        try {
                            System.load(vLib);
                            vRet = true;
                        } catch (Throwable e) {
                            vRet = false;
                        }
                    }
                }
            }
            if (!vRet) {
                try {
                    System.loadLibrary(aLibNm);
                } catch (Throwable e) {

                }
            }
        }
    }

    public static String getSignature(Context context){
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = pi.signatures;
            // and here we have the DER encoded X.509 certificate
            byte[] certificate = signatures[0].toByteArray();

            ByteArrayInputStream in = new ByteArrayInputStream(certificate);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            java.security.cert.Certificate c = cf.generateCertificate(in);
            in.close();

            X509Certificate t = (X509Certificate) c;
            String signnumber = t.getSerialNumber().toString(16);
            // 补齐前面的0
            if (signnumber.length() % 2 != 0) {
                signnumber = "0" + signnumber;
            }
            return signnumber;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
