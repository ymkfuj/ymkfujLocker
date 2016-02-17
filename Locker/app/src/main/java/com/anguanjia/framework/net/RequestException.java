package com.anguanjia.framework.net;

public class RequestException extends Exception{
    public static final int ERR_KEY_EXPIRATION = 300;//密钥过期
    public static final int ERR_KEY_ENCRYPTION = 301;//加解密失败
    public static final int ERR_KEY_COMPRESS = 302;//压缩解压缩失败
    public static final int ERR_KEY_UNLL = 309;//密钥为空
    public static final int ERR_PARAMETER = 400;//参数错误
    public static final int ERR_SERVER = 500;//服务器错误
    public static final int ERR_DISTAL = 600;//远端无响应
    public static final int ERR_INTERFACE = 700;//第三方接口错误
    public static final int ERR_NET_NO_CONNECTION = 801;//无网络连接
    public static final int ERR_NET_UNKOWN = 802;//未知网络类型
    public static final int ERR_NET_WRON_TYPE = 803;//错误网络类型（非wifi）

    public static final int ERR_UNKOWN = 0;//未知异常

    private int mErrCode = 0;

    public RequestException(int err_code) {
        super();
        this.mErrCode = err_code;
    }

    public int getErrCode(){
        return mErrCode;
    }

    @Override
    public String getMessage() {
        String message = "";
        switch (mErrCode) {
            case ERR_KEY_EXPIRATION: {
                message = "密钥过期";
                break;
            }
            case ERR_KEY_ENCRYPTION: {
                message = "加解密失败";
                break;
            } case ERR_KEY_COMPRESS: {
                message = "压缩解压缩失败";
                break;
            } case ERR_KEY_UNLL: {
                message = "密钥为空";
                break;
            }
            case ERR_PARAMETER: {
                message = "参数错误";
                break;
            }
            case ERR_SERVER: {
                message = "服务器错误";
                break;
            }
            case ERR_DISTAL: {
                message = "远端无响应";
                break;
            }
            case ERR_INTERFACE: {
                message = "第三方接口错误";
                break;
            }
            case ERR_NET_NO_CONNECTION: {
                message = "无网络连接";
                break;
            }
            case ERR_NET_UNKOWN: {
                message = "未知网络类型";
                break;
            }
            case ERR_NET_WRON_TYPE: {
                message = "错误网络类型";
                break;
            }
            case ERR_UNKOWN: {
                message = "未知异常";
                break;
            }
        }
        return message;
    }
}
