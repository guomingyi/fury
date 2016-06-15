package com.fury;

import android.view.View;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FloatWindowView extends LinearLayout {

    private static final String TAG = "FloatWindowView";
    private Context mContext;


    public FloatWindowView(Context context) {
        super(context);
        mContext = context;
        Log.i(TAG, "---init---FloatWindowView-------");

        WindowManager wm = (WindowManager)mContext.
                getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        View v = LayoutInflater.from(context).inflate(R.layout.float_window_view, null);
        this.addView(v);


       // View v = LinearLayout float_window_layout_main = (LinearLayout)findViewById(R.id.float_window_layout_main);
        Log.i(TAG, "---init---view-------");
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "---onKeyUp-------"+keyCode);
        return super.onKeyUp(keyCode, event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "---onKeyDown-------" + keyCode);
        return super.onKeyDown(keyCode, event);
    }

}
