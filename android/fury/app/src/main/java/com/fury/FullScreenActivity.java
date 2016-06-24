package com.fury;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.net.InetAddress;
import java.util.Arrays;

import com.fury.ControlService.ControlServiceBinder;
import android.content.Context;
import com.fury.MySurfaceView;
import com.fury.MySurfaceViewTest;



public class FullScreenActivity extends Activity implements  View.OnTouchListener {
    public static final String TAG = "fury-FullScreenActivity";
    public static final int MSG_RECV_FROM_MJPG_SERVER = 11;

    private Button btn_forward,btn_turn_left,btn_led,btn_turn_right,btn_back;
    private Button btn_forward_cam,btn_turn_left_cam,btn_cam,btn_turn_right_cam,btn_back_cam;

    private MySurfaceViewTest mMySurfaceViewTest;
    private SharedPreferences sp;
    private SharedPreferences.Editor mEditor;

    private String host_ip;
    private String tank_server_port;
    private String cam_server_port;
    private ControlService mControlService;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate...");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.full_screen);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = sp.edit();

        host_ip = sp.getString("host_ip", null);
        tank_server_port = sp.getString("host_port", null);
        cam_server_port = sp.getString("cam_server_port", null);

        Log.i(TAG, "host_ip:" + host_ip
                + " tank_server_port:" + tank_server_port
                + " cam_server_port:" + cam_server_port);

        Intent intent = new Intent();
        intent.setClassName(getPackageName(), "com.fury.ControlService");
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        initView();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,IBinder service) {
            if(service != null) {
                Log.i(TAG,"ControlService bind success: ");
                mControlService = ((ControlService.ControlServiceBinder)service).getService();
                mControlService.setHandler(mHandler);
                mControlService.setHostInfo(host_ip,tank_server_port,cam_server_port);
                mControlService.udpRecivFromServer(MSG_RECV_FROM_MJPG_SERVER);
            }
        }
        public void onServiceDisconnected(ComponentName arg0) {
            mControlService = null;
            Log.i(TAG,"ControlService onServiceDisconnected!");
        }
    };

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy:");
        if(mMySurfaceViewTest != null)
            mMySurfaceViewTest.stop();
    }
    public void onStop() {
        super.onStop();
        if(mMySurfaceViewTest != null)
            mMySurfaceViewTest.stop();
    }

    public void onResume() {
        super.onResume();
        if(mMySurfaceViewTest != null)
            mMySurfaceViewTest.start();
    }

    public void initView() {
        Log.i(TAG, "initView:");

        btn_forward = (Button)findViewById(R.id.btn_forward);
        btn_turn_left = (Button)findViewById(R.id.btn_turn_left);
        btn_led = (Button)findViewById(R.id.btn_led);
        btn_turn_right = (Button)findViewById(R.id.btn_turn_right);
        btn_back = (Button)findViewById(R.id.btn_back);

        btn_forward_cam = (Button)findViewById(R.id.btn_forward_cam);
        btn_turn_left_cam = (Button)findViewById(R.id.btn_turn_left_cam);
        btn_cam = (Button)findViewById(R.id.btn_cam);
        btn_turn_right_cam = (Button)findViewById(R.id.btn_turn_right_cam);
        btn_back_cam = (Button)findViewById(R.id.btn_back_cam);


        btn_forward.setOnTouchListener(this);
        btn_turn_left.setOnTouchListener(this);
        btn_led.setOnTouchListener(this);
        btn_turn_right.setOnTouchListener(this);
        btn_back.setOnTouchListener(this);

        btn_forward_cam.setOnTouchListener(this);
        btn_turn_left_cam.setOnTouchListener(this);
        btn_cam.setOnTouchListener(this);
        btn_turn_right_cam.setOnTouchListener(this);
        btn_back_cam.setOnTouchListener(this);


        mMySurfaceViewTest = (MySurfaceViewTest)findViewById(R.id.mySurfaceViewVideoFullScreen);
        mMySurfaceViewTest.setOnTouchListener(this);
        mMySurfaceViewTest.setVisibility(View.VISIBLE);
        mMySurfaceViewTest.setHost(host_ip, cam_server_port);
        mMySurfaceViewTest.start();

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG,"msg.what:"+msg.what);

            switch (msg.what) {
                case MSG_RECV_FROM_MJPG_SERVER: {

                }
                break;
            }
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        //Log.i(TAG, "onTouch:"+v);

        switch (v.getId()) {

            case R.id.mySurfaceViewVideoFullScreen: {
                if(action == MotionEvent.ACTION_DOWN) {

                }
                else {

                }
            } break;
            case R.id.btn_forward: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_TANK_GO_FORWARD));
                }
                else {

                }
            } break;
            case R.id.btn_back: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_TANK_GO_BACK));
                }
                else {

                }
            } break;
            case R.id.btn_turn_left: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_TANK_GO_LEFT));
                }
                else {

                }
            } break;
            case R.id.btn_turn_right: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_TANK_GO_RIGHT));
                }
                else {

                }
            } break;
            case R.id.btn_led: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_LED_OPEN));
                }
                else {

                }
            } break;
            case R.id.btn_forward_cam: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_SERVO_GO_FORWARD));
                }
                else {

                }
            } break;
            case R.id.btn_back_cam: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_SERVO_GO_BACK));
                }
                else {

                }
            } break;
            case R.id.btn_turn_left_cam: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_SERVO_GO_LEFT));
                }
                else {

                }
            } break;
            case R.id.btn_turn_right_cam: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_SERVO_GO_RIGHT));
                }
                else {

                }
            } break;
            case R.id.btn_cam: {
                if(action == MotionEvent.ACTION_DOWN) {
                    mControlService.udpSendMsgToTankServer(
                            Utils.parseCmd(Utils.MSG_CAMERA_OPEN));
                }
                else {

                }
            } break;

        }

        return false;
    }



    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG," onKeyDown:"+keyCode);

        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG," onKeyUp:"+keyCode);

        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(mMySurfaceViewTest != null) {
                mMySurfaceViewTest.stop();
                mMySurfaceViewTest = null;
            }
            Intent i = new Intent();
            i.setClassName(this.getPackageName(),"com.fury.MainActivity");
            startActivity(i);
            finish();
        }
        return true;
    }



}