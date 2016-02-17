package com.ctflab.locker.common;

/**
 * Created by xiang on 15-9-14.
 */
public class SystemDef {

    public class Version{

        /**
         *  Android 2.1
         */
        public static final int ECLAIR_MR1 = 7;

        /**
         *  Android 2.2
         */
        public static final int FROYO = 8;

        /**
         *  Android 2.3
         */
        public static final int GINGERBREAD = 9;

        /**
         *  Android 2.3.3.
         */
        public static final int GINGERBREAD_MR1 = 10;

        /**
         *  Android 3.0.
         */
        public static final int HONEYCOMB = 11;

        /**
         *  Android 3.1
         */
        public static final int HONEYCOMB_MR1 = 12;

        /**
         *  Android 3.2
         */
        public static final int HONEYCOMB_MR2 = 13;

        /**
         *  Android 4.0
         */
        public static final int ICE_CREAM_SANDWICH = 14;

        /**
         *  Android 4.0.3.
         */
        public static final int ICE_CREAM_SANDWICH_MR1 = 15;

        /**
         *  Android 4.1
         */
        public static final int JELLY_BEAN = 16;

        /**
         *  Android 4.2
         */
        public static final int JELLY_BEAN_MR1 = 17;

        /**
         *  Android 4.3
         */
        public static final int JELLY_BEAN_MR2 = 18;

        /**
         *  Android 4.4
         */
        public static final int KITKAT = 19;

        /**
         * Android 4.4W
         */
        public static final int KITKAT_WATCH = 20;

        /**
         * Android 5.0
         */
        public static final int LOLLIPOP = 21;

        /**
         * Android 5.1
         */
        public static final int LOLLIPOP_MR1 = 22;

        /**
         * Android 6.0
         */
        public static final int MARSHMALLOW = 23;
    }

    public static final String[] PER_STR_ARR = new String[]{
            Permission.PER_PHONE_STATE,
            Permission.PER_SMS,
            Permission.PER_CONTACTS,
            Permission.PER_CAMERA,
            Permission.PER_LOCATION,
            Permission.PER_STORAGE
    };

    public class Permission{
        public static final String PER_PHONE_STATE = "android.permission.READ_PHONE_STATE";
        public static final String PER_SMS = "android.permission.READ_SMS";
        public static final String PER_CONTACTS = "android.permission.WRITE_CONTACTS";//android.permission.READ_CONTACTS
        public static final String PER_CAMERA = "android.permission.CAMERA";
        public static final String PER_LOCATION="android.permission.ACCESS_FINE_LOCATION";
        public static final String PER_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    }

    public class PermissionId{
        public static final int PERMISSION_REQ_ALL = 0;
        public static final int PERMISSION_PHONE_STATE = PERMISSION_REQ_ALL + 1;
        public static final int PERMISSION_SMS = PERMISSION_REQ_ALL + 2;
        public static final int PERMISSION_CONTACTS = PERMISSION_REQ_ALL + 3;
        public static final int PERMISSION_CAMERA = PERMISSION_REQ_ALL + 4;
        public static final int PERMISSION_LOCATION = PERMISSION_REQ_ALL + 5;
        public static final int PERMISSION_STORAGE = PERMISSION_REQ_ALL + 6;
    }
}
