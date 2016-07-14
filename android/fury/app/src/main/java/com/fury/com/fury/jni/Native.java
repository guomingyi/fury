package com.fury.jni;

/**
 * Created by android on 16-7-12.
 */

public class Native {

    static
    {
        System.loadLibrary("fury_jni");
    }

    //native init.
    public static native int native_init();
    public static native int native_monitor();
    //native call.
    public static boolean onKeyEvent(int keycode, int keytype) {

        return true;
    }
}
