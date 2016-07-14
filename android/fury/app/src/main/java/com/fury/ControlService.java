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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import com.fury.jni.Native;

public class ControlService extends Service {
    private static final String TAG = "fury-ControlService";
    private Handler mHandler;
    private Context mContext;

    private View mView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;

    private DatagramSocket send_socket = null;
    private DatagramPacket send_pkt = null;
    private String host_ip, tank_server_port,jpg_server_port;

    @Override
    public IBinder onBind(Intent intent) {
        return new ControlServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "----onCreate----");
        mContext = this;

        //nativeKeyCheck();
    }

    private boolean native_loop_exit = false;
    public void nativeKeyCheck() {
        new Thread() {
            @Override
            public void run() {
                if(Native.native_init() < 0) {
                    Log.e(TAG, "native_init: fail");
                    return;
                }

                for(;;) {
                    if(native_loop_exit) {
                        break;
                    }
                    int v = Native.native_monitor();
                    Log.i(TAG, "native_monitor:"+v);
                }
            }
        }.start();

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
        native_loop_exit = true;
    }


    public class ControlServiceBinder extends Binder {
        public ControlService getService() {
            return ControlService.this;
        }
    }

    public void setHandler(Handler h) {
        mHandler = h;
        Log.d(TAG, "setHandler:" + h);
    }

    public void setHostInfo(String ip, String tank_port, String jpg_port) {
        host_ip = ip;
        tank_server_port = tank_port;
        jpg_server_port = jpg_port;
    }

    public void udpSendMsgToTankServer(final String cmd) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.i(TAG, "udpSendMsgToServer: " + cmd);
                    send_pkt = new DatagramPacket(cmd.getBytes(), cmd.length(),
                            InetAddress.getByName(host_ip), Integer.parseInt(tank_server_port));
                    if(send_socket == null) {
                        send_socket = new DatagramSocket();
                        send_socket.setReuseAddress(true);
                    }
                    send_socket.send(send_pkt);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void udpRecivFromServer(final int msgId) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                DatagramSocket socket = null;

                try {
                    Log.i(TAG, "new socket..");
                    socket = new DatagramSocket(null);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(Integer.parseInt(tank_server_port)+1));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "udpRecivFromServer:"+e.toString());
                    return;
                }

                while(true) {
                    try {
                        Log.i(TAG, "waiting server reply...");

                        byte[] buf = new byte[10];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String recv = new String(packet.getData());
                        Log.i(TAG, "Reciv:" + recv + "  " + packet.getLength());
                        mHandler.obtainMessage(msgId,recv);
                    }
                    catch (Exception e) {
                        socket.close();
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}



