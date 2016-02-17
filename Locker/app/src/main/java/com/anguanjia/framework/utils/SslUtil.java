package com.anguanjia.framework.utils;

public class SslUtil {

    static {
        try {
            System.loadLibrary("aes128");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public String P;
    public String G;
    public String CPub;
    public String CPri;

    private static final int TYPE_P = 1;
    private static final int TYPE_G = 2;
    private static final int TYPE_CPub = 3;

    protected native String data_to_encrypt(String in_src) throws SslException ;

    protected native String data_to_decrypt(String in_src, int src_len) throws  SslException;

    protected native String data_to_encrypt_with_seed(String seed, String in_src) throws  SslException;

    protected native String data_to_decrypt_with_seed(String seed, String in_src, int src_len) throws  SslException;

    protected native String data_to_compress(String in_src, int src_len) throws  SslException;

    protected native String data_to_uncompress(String in_src) throws  SslException;

    protected native String data_to_encrypt_with_key(String key, int KeyLen, String in_src) throws SslException;

    protected native String data_to_decrypt_with_key(String key, int KeyLen, String in_src, int SrcLen) throws SslException;

    protected native String file_to_decrypt(String path) throws SslException;

    protected native int get_client_pg() throws SslException;

    protected native String computer_c_key(String ServerPublicString) throws SslException;

    public void CallBackInitData(String buff, int err) {
        //Log.e("ad_buff", buff);
        //1=p 2=g 3=cpub
        switch (err) {
            case TYPE_P: {
                P = buff;
                break;
            }
            case TYPE_G: {
                G = buff;
                break;
            }
            case TYPE_CPub: {
                CPub = buff;
                break;
            }
        }
    }

//
//    public void initCodes(boolean isForce) {
//        get_client_pg();
////        if (isForce) {
////            get_client_pg();
////        } else {
////
////        }
//    }

}
