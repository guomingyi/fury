package com.fury;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.content.Context;
import android.view.WindowManager;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;


public class ControlService extends Service {
    private static final String TAG = "chariot-ControlService";
    private Handler mHandler;
    private Context mContext;

    private View mView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;



    @Override
    public IBinder onBind(Intent intent) {
        return new ControlServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "----onCreate----");
        mContext = this;
        //createFloatWindow();

    }

    public void createFloatWindow() {
        mView = new FloatWindowView(this);
        WindowManager mWindowManager = (WindowManager) getApplicationContext().getSystemService("window");
        mLayout = new WindowManager.LayoutParams();
        mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayout.format = PixelFormat.RGBA_8888;
        mLayout.gravity = Gravity.TOP | Gravity.LEFT;
        mLayout.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayout.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowManager.addView(mView,mLayout);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    public class ControlServiceBinder extends Binder {
        public ControlService getService() {
            return ControlService.this;
        }
    }

    public void setHandler(Handler h) {
        mHandler = h;
        Log.d(TAG, "setHandler:"+h);
    }

}



