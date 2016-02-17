package com.anguanjia.framework.utils;

public class SSLUtilExt extends com.anguanjia.framework.utils.SslUtil{
    private static SSLUtilExt mInstance;

    private SSLUtilExt(){
        try {
            get_client_pg();
        } catch (SslException e) {
            e.printStackTrace();
        }
    }

    public static synchronized SSLUtilExt getInstance(){
        if (null == mInstance){
            mInstance = new SSLUtilExt();
        }
        return mInstance;
    }

    public String data_to_compress(String in_src, int src_len) throws SslException {
        try {
            return super.data_to_compress(in_src, src_len);
        } catch (UnsatisfiedLinkError u) {
            throw new SslException();
        }
    }

    public String data_to_uncompress(String in_src) throws SslException {
        try {
            return super.data_to_uncompress(in_src);
        } catch (UnsatisfiedLinkError u) {
            throw new SslException();
        }
    }

    /////////////////////////////////////////////////////////////
    public String data_to_encrypt(String in_src) throws SslException {
        try {
            return super.data_to_encrypt(in_src);
        } catch (UnsatisfiedLinkError u) {
            throw new SslException();
        }

    }

    public String data_to_decrypt(String in_src, int src_len) throws SslException {
        try {
            return super.data_to_decrypt(in_src, src_len);
        } catch (UnsatisfiedLinkError u) {
            throw new SslException();
        }
    }

    public String file_to_decrypt(String inPath) throws SslException{
        try {
            return super.file_to_decrypt(inPath);
        }catch (UnsatisfiedLinkError u) {
            throw new SslException();
        }
    }
}
