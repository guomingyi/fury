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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.Preference;
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
import android.widget.LinearLayout;
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
import com.fury.jni.Native;


public class MainActivity extends Activity implements  View.OnTouchListener {
  private final String TAG = "fury-MainActivity";
  
  protected static final int MSG_CLIENT_SOCKET_INIT = 0;
  protected static final int MSG_CONSOLE_INFO_SHOW = 1;
  protected static final int MSG_SEND_CMD_TO_SERVER = 2;
  protected static final int MSG_RUN_CAM_SURFACE = 3;
  protected static final int MSG_STOP_CAM_SURFACE = 4;

//camera port
  public static int CAM_SERVER_PORT = 8080;
  public static int CAM_SERVER_UDP_RECV_PORT = CAM_SERVER_PORT +1;

  private final boolean useUdp = true;
  private final boolean UdpJpgTest = true;

  //协议
  private final static int CMD_LENGTH = 4;

  private final static int MSG_TANK_STOP_RUN = 1099;
  private final static int MSG_TANK_GO_FORWARD = 1100;
  private final static int MSG_TANK_GO_BACK = 1101;
  private final static int MSG_TANK_GO_LEFT = 1102;
  private final static int MSG_TANK_GO_RIGHT = 1103;

  private final static int MSG_LED_OPEN = 1104;
  private final static int MSG_LED_CLOSE = 1105;

  private final static int MSG_FAN_OPEN = 1106;
  private final static int MSG_FAN_CLOSE = 1107;

  private final static int MSG_BEEP_PLAY = 1108;
  private final static int MSG_BEEP_PLAY_REPEED = 1109;

  private final static int MSG_TANK_SPEED_INC = 1110;
  private final static int MSG_TANK_SPEED_DEC = 1111;
  private final static int MSG_GET_SPEED_VALUE = 1112;

  private final static int MSG_SYS_SLEEP = 2000;
  private final static int MSG_SYS_SHUT_DOWN = 2001;
  private final static int MSG_SYS_REBOOT = 2002;
  private final static int MSG_CAMERA_OPEN = 2003;
  private final static int MSG_CAMERA_CLOSE = 2004;


  public final static String BTN_LED = String.valueOf(MSG_LED_OPEN);

  public final static String BTN_SPEED_DEC = String.valueOf(MSG_TANK_SPEED_DEC);
  public final static String BTN_SPEED_INC = String.valueOf(MSG_TANK_SPEED_INC);

  public final static String BTN_UP_DOWN = String.valueOf(MSG_TANK_GO_FORWARD);
  public final static String BTN_UP_UP = String.valueOf(MSG_TANK_STOP_RUN);

  public final static String BTN_DOWN_DOWN = String.valueOf(MSG_TANK_GO_BACK);
  public final static String BTN_DOWN_UP = String.valueOf(MSG_TANK_STOP_RUN);

  public final static String BTN_LEFT_DOWN = String.valueOf(MSG_TANK_GO_LEFT);
  public final static String BTN_LEFT_UP = String.valueOf(MSG_TANK_STOP_RUN);

  public final static String BTN_RIGHT_DOWN = String.valueOf(MSG_TANK_GO_RIGHT);
  public final static String BTN_RIGHT_UP = String.valueOf(MSG_TANK_STOP_RUN);


  public final static String MSG_STOP = String.valueOf(MSG_SYS_SLEEP);
  public final static String MSG_CAM_OPEN = String.valueOf(MSG_CAMERA_OPEN);
  public final static String MSG_CAM_CLOSE = String.valueOf(MSG_CAMERA_CLOSE);
  public final static String MSG_FAN = String.valueOf(MSG_FAN_OPEN);
  public final static String MSG_BEEP = String.valueOf(MSG_BEEP_PLAY);
//协议
  
  private Button btnConnect,btnVideoFullScreen;
  private Button btnSend;
  private EditText editSend;
  private Socket socket;
  private PrintStream output;
  private InputStream input;
  private DatagramSocket dSocket;
  private int counter = 0;

  private Button btn_up,btn_down,btn_left,btn_right,btn_mid;
  private Button btn_speed_inc,btn_speed_dec;
  private EditText host_ip_view, host_port_view;
  private String host_ip = null, host_port = null;
  
  private EditText console_show;

  private MySurfaceView sf = null;
  private MySurfaceViewTest sft = null;

  
  private int speed_count = 0;

  private ControlService mControlService;

  private RelativeLayout console_layout, host_layout;
  private LinearLayout video_area_layout;

  private  SharedPreferences sp;
  private SharedPreferences.Editor mEditor;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate...");

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
    //        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    sp = PreferenceManager.getDefaultSharedPreferences(this);
    mEditor = sp.edit();

    host_ip = sp.getString("host_ip", null);
    host_port = sp.getString("host_port", null);
    String port = sp.getString("cam_server_port", null);
    if(port != null) {
      CAM_SERVER_PORT = Integer.parseInt(port);
      CAM_SERVER_UDP_RECV_PORT = CAM_SERVER_PORT +1;
    }

    Log.i(TAG,"host_ip:"+host_ip
            +" host_port:"+host_port
            +" port:"+port);


    if(host_ip == null || host_port == null || port == null) {
      host_ip = getResources().getString(R.string.host_ip);
      host_port = getResources().getString(R.string.host_port);
      port = getResources().getString(R.string.cam_host_port);
      CAM_SERVER_PORT = Integer.parseInt(port);
      mEditor.putString("cam_server_port", port);
      mEditor.putString("host_ip", host_ip);
      mEditor.putString("host_port", host_port);
      mEditor.commit();
    }

    //setContentView(R.layout.main);
    setContentView(R.layout.main_new);
    initView();

    Intent intent = new Intent();
    intent.setClassName(getPackageName(), "com.fury.ControlService");
    startService(intent);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    //int ret = Native.native_init();
    //Log.i(TAG,"ret:"+ret);


    //test
    Intent i = new Intent();
    //i.setClassName(this.getPackageName(), "com.fury.FullScreenActivity");
    i.setClassName(this.getPackageName(), "com.fury.sensorDemo");
    //startActivity(i);

    //finish();
  }

  public void onStart() {
    super.onStart();
  }

  private ServiceConnection mConnection = new ServiceConnection() {

    public void onServiceConnected(ComponentName className,IBinder service) {
      if(service != null) {
        Log.i(TAG,"ControlService bind success: ");
        mControlService = ((ControlService.ControlServiceBinder)service).getService();
        mControlService.setHandler(mHandler);
      }
    }
    public void onServiceDisconnected(ComponentName arg0) {
      mControlService = null;
      Log.i(TAG,"ControlService onServiceDisconnected!");
    }
  };


  public void initView()
  {
    Log.i(TAG,"initView:");

    btnConnect = (Button)findViewById(R.id.btnConnect);
    btnVideoFullScreen = (Button)findViewById(R.id.btnVideoFullScreen);


    btn_up = (Button)findViewById(R.id.btn_up);
    btn_down = (Button)findViewById(R.id.btn_down);
    btn_left = (Button)findViewById(R.id.btn_left);
    btn_right = (Button)findViewById(R.id.btn_right);
    btn_mid = (Button)findViewById(R.id.btn_mid);
    btn_speed_dec = (Button)findViewById(R.id.btn_speed_dec);
    btn_speed_inc = (Button)findViewById(R.id.btn_speed_inc);
    
    console_show = (EditText)findViewById(R.id.console_show);
    
    host_ip_view = (EditText)findViewById(R.id.host_ip);
    host_ip_view.setText(host_ip);
    host_ip_view.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        host_ip = s.toString();
        if (s.toString().length() == 0) {
          host_ip = (String) host_ip_view.getHint();
        }
        Log.i(TAG, "host_ip:" + host_ip);
        mEditor.putString("host_ip", host_ip);
        mEditor.commit();
      }
    });
    
    host_port_view = (EditText)findViewById(R.id.host_port);
    host_port_view.setText(host_port);
    host_port_view.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        host_port = s.toString();
        if (s.toString().length() == 0) {
          host_port = (String) host_port_view.getHint();
        }
        Log.i(TAG, "host_port:" + host_port);
        mEditor.putString("host_port", host_port);
        mEditor.commit();
      }
    });

    btnConnect.setOnTouchListener(this);
    btnVideoFullScreen.setOnTouchListener(this);
    btn_mid.setOnTouchListener(this);
    btn_speed_dec.setOnTouchListener(this);
    btn_speed_inc.setOnTouchListener(this);
    btn_up.setOnTouchListener(this);
    btn_down.setOnTouchListener(this);
    btn_left.setOnTouchListener(this);
    btn_right.setOnTouchListener(this);

    video_area_layout = (LinearLayout)findViewById(R.id.video_area_layout);
    host_layout = (RelativeLayout)findViewById(R.id.host_layout);

    if(!UdpJpgTest) {
      sf = (MySurfaceView)findViewById(R.id.mySurfaceViewVideo);
      sf.setUrl(host_ip, CAM_SERVER_PORT);
      //sf.setVisibility(View.VISIBLE);
    }
    else {
      sft = (MySurfaceViewTest)findViewById(R.id.mySurfaceViewVideoTest);
      sft.setHostIp(host_ip);
      sft.setVisibility(View.VISIBLE);
      video_area_layout.setVisibility(View.VISIBLE);
    }

    setBtnEnable(false);
  }

  private void setBtnEnable(boolean b) {
    btn_mid.setEnabled(b);
    btn_speed_dec.setEnabled(b);
    btn_speed_inc.setEnabled(b);
    btn_up.setEnabled(b);
    btn_down.setEnabled(b);
    btn_left.setEnabled(b);
    btn_right.setEnabled(b);

    btnConnect.setText(b ? R.string.disconnect : R.string.connect);
    host_layout.setVisibility(b ? View.GONE : View.VISIBLE);
    //console_layout.setVisibility(b ? View.GONE : View.VISIBLE);
  }
  
  
  public void closeSocket()
  {
	Log.i(TAG, "closeSocket");

    try
    {
      Thread.sleep(500);

      if(output != null) {
        output.close();
        output = null;
      }

      if(input != null) {
        input.close();
        input = null;
      }

      if(socket != null) {
        socket.close();
        socket = null;
      }

      if(dSocket != null) {
        dSocket.close();
        dSocket = null;
      }

	}
    catch (Exception e)
    {
      Log.e(TAG, "close exception: ");
	}
  }

  private void waitServerReply() {
    new Thread() {
      private String msg = "init success!";

      @Override
      public void run()
      {
        while(true) {
          if(input != null) {
            byte[] buf = new byte[2];
            Arrays.fill(buf,(byte)0);

            try {
              int ret = input.read(buf, 0, 2);
              if(ret <= 0) {
                break;
              }

              String reply = new String(buf);
              Log.i(TAG, "input.reply: " + reply);

              if(reply.equals("ok")) {
                Log.i(TAG, "ok");
                mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, 1,1, msg).sendToTarget();
              }
            }
            catch (Exception e) {
              Log.e(TAG, "read: " + e);
              msg = e.toString();
              break;
            }
          }
          else {
            break;
          }
        }
        mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, 1,1, msg).sendToTarget();
      }
    }.start();
  }

  private void initClientSocketTcp()
  {
    String msg = "";

    if(socket != null) {
      closeSocket();
      mHandler.obtainMessage(MSG_CLIENT_SOCKET_INIT, MSG_STOP).sendToTarget();
      return;
    }

    try
    {
      Log.i(TAG, "Socket: ");
      socket = new Socket(host_ip, Integer.parseInt(host_port));

      Log.i(TAG, "PrintStream: ");
      output = new PrintStream(socket.getOutputStream(), true, "utf-8");
      input = socket.getInputStream();

      Log.i(TAG, "wait server replay..");
      waitServerReply();
      return;
    }
    catch (Exception e)
    {
      msg = e.toString();
      Log.e(TAG, " exception:" + e);
    }

    mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, 1,-1, msg).sendToTarget();
  }

  private void sendCmdToServerTcp(byte[] cmd) {

    try {
      byte[] buf = cmd;
      Socket s = new Socket(host_ip,Integer.parseInt(host_port));
      s.getOutputStream().write(buf,0,2);
      s.close();
      Thread.sleep(100);
    }
    catch (Exception e) {
      Log.i(TAG, "e:" + e.toString());
    }
  }


  private void initClientSocketUdp()
  {
    if(dSocket != null) {
      mHandler.obtainMessage(MSG_CLIENT_SOCKET_INIT, -1).sendToTarget();
      closeSocket();
      return;
    }

    try {
      dSocket = new DatagramSocket(); // <uses-permission android:name="android.permission.INTERNET" />
      dSocket.setReuseAddress(true);
    } catch (Exception e) {
      Log.e(TAG, "exception:" + e);
      e.printStackTrace();
      return;
    }

    mHandler.obtainMessage(MSG_CLIENT_SOCKET_INIT, 1).sendToTarget();
    reciverMsgFromServer();



  }

  private void sendCmdToServerTcp(final String cmd) {
    if(output != null)
      output.print(cmd);
  }

private void sendCmdToServerUdp(final String cmd) {
	if(dSocket == null) {
      Log.e(TAG, "dSocket is null");
      return;
	}
	  
	InetAddress local = null;
	
	try {
	  local = InetAddress.getByName(host_ip);
	} 
	catch (Exception e) {
	  Log.e(TAG, "exception:" + e);
      return;
	}

 // while(dSocket != null) {

      DatagramPacket dPacket = new DatagramPacket(cmd.getBytes(), cmd.length(), local, Integer.parseInt(host_port));
      try {
        Log.i(TAG, "send to server:"+cmd);
        dSocket.send(dPacket);
      }
      catch (Exception e) {
        Log.e(TAG, "send fail:"+e);
      }

 // }


}
  
private void reciverMsgFromServer() {
  DatagramSocket socket = null;

  try {
    socket = new DatagramSocket(null);
    socket.setReuseAddress(true);
    socket.bind(new InetSocketAddress(Integer.parseInt(host_port)+1));
  }
  catch (Exception e) {
    e.printStackTrace();
    return;
  }

  byte[] buf = new byte[10];
  Log.i(TAG, "waiting server reply...");

  while(true)
  {
    try {
      Arrays.fill(buf,(byte)0);
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      Log.i(TAG, ""+new String(packet.getData())+"  "+packet.getLength());
    }
    catch (Exception e) {
      socket.close();
      e.printStackTrace();
    }
  }
}

  @SuppressLint("HandlerLeak")
  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_CLIENT_SOCKET_INIT: {
          if(msg.obj != null) {
            if((int)msg.obj == 1) {
              setBtnEnable(true);
              mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,MSG_CAM_OPEN).sendToTarget();
              mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN_CAM_SURFACE, 1), 2000);
            }
            else if((int)msg.obj == -1) {
              setBtnEnable(false);
              mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,MSG_CAM_CLOSE).sendToTarget();
              mHandler.obtainMessage(MSG_RUN_CAM_SURFACE,-1).sendToTarget();
            }
          }
          else {
            new Thread() {
              @Override
              public void run() {
                initClientSocketUdp();
              }
            }.start();
          }
        }
        break;

        case MSG_RUN_CAM_SURFACE: {
          if(msg.obj != null) {
            if((int)msg.obj == 1) {
              //sft.setHostIp(host_ip);
              //sft.start();

              video_area_layout.setVisibility(View.VISIBLE);
              sft.setVisibility(View.VISIBLE);
              sft.Is_Scale = true;
              sft.start();
            }
            else  {
              sft.stop();
            }
          }
        }
        break;

        case MSG_CONSOLE_INFO_SHOW: {
          if(msg.obj != null) {
            console_show.setText((String)msg.obj);
          }
        }
        break;

        case MSG_SEND_CMD_TO_SERVER: {
          if(msg.obj != null) {
            final String cmd = (String)msg.obj;
            new Thread() {
              @Override
              public void run() {
                sendCmdToServerUdp(cmd);
              }
            }.start();
          }
        }
        break;
      }
    }
  };


  public void onPause() {
    super.onPause();
    Log.i(TAG, "onPause:");

    sft.stop();
    sft.setVisibility(View.GONE);
    video_area_layout.setVisibility(View.GONE);

  }

  public void onResume() {
    super.onResume();
    Log.i(TAG, "onResume:");
    video_area_layout.setVisibility(View.VISIBLE);
    sft.setVisibility(View.VISIBLE);
    sft.Is_Scale = true;
    sft.start();
  }
  public void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "onDestroy:");
    sft.stop();
    closeSocket();
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
	int action = event.getAction();
    //Log.i(TAG, "onTouch:"+v);

    switch(v.getId())
    {
      case R.id.btn_up:
        if(action == MotionEvent.ACTION_DOWN) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_UP_DOWN).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "前进:"+BTN_UP_DOWN).sendToTarget();
        }
        else
        if(action == MotionEvent.ACTION_UP) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_UP_UP).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "停止:"+BTN_UP_UP).sendToTarget();
        }
        break;

      case R.id.btn_down:
        if(action == MotionEvent.ACTION_DOWN) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_DOWN_DOWN).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "后退:"+BTN_DOWN_DOWN).sendToTarget();
        }
        else
        if(action == MotionEvent.ACTION_UP) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_DOWN_UP).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "停止:"+BTN_DOWN_UP).sendToTarget();
        }
        break;

      case R.id.btn_left:
        if(action == MotionEvent.ACTION_DOWN) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_LEFT_DOWN).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "左转弯:"+BTN_LEFT_DOWN).sendToTarget();
        }
        else
        if(action == MotionEvent.ACTION_UP) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_LEFT_UP).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "停止:"+BTN_LEFT_UP).sendToTarget();
        }
        break;

      case R.id.btn_right:
        if(action == MotionEvent.ACTION_DOWN) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_RIGHT_DOWN).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "右转弯:"+BTN_RIGHT_DOWN).sendToTarget();
        }
        else
        if(action == MotionEvent.ACTION_UP) {
          mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_RIGHT_UP).sendToTarget();
          mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, "停止:"+BTN_RIGHT_UP).sendToTarget();
        }
        break;
        
	    case R.id.btnConnect:
	    	if(action == MotionEvent.ACTION_DOWN) {
	    		mHandler.obtainMessage(MSG_CLIENT_SOCKET_INIT).sendToTarget();
	    	}
	    break;
      case R.id.btnVideoFullScreen:
        if(action == MotionEvent.ACTION_UP) {
          Intent i = new Intent();
          i.setClassName(this.getPackageName(),"com.fury.FullScreenActivity");
          startActivity(i);
          finish();
        }
        break;

	    case R.id.btn_speed_inc:
	    	if(action == MotionEvent.ACTION_DOWN) {
				mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_SPEED_INC).sendToTarget();
				if((speed_count += 10) > 100) {
						speed_count = 100;
						break;
				}
				
				mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, " ").sendToTarget();
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW,"速度:"+speed_count), 20);
	        }
		break;
		    
	    case R.id.btn_speed_dec:
	    	if(action == MotionEvent.ACTION_DOWN) {
		    	mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_SPEED_DEC).sendToTarget();
				if((speed_count -= 10) < 0) {
					speed_count = 0;
					break;
				}
				mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, " ").sendToTarget();
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW,"速度:"+speed_count), 20);
	    	}
	    break;
	    case R.id.btn_mid:
	    	if(action == MotionEvent.ACTION_DOWN) {
		    	mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER,BTN_LED).sendToTarget();
		    	mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, " ").sendToTarget();
		    	mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW,
                        (led_state_enable = !led_state_enable) ? "Led on" : "Led off"), 10);
	    	}
	    break;
    }

    return false;
  }

  private boolean led_state_enable = false;


  public boolean onKeyDown(int keyCode, KeyEvent event) {
    Log.i(TAG," onKeyDown:"+keyCode);

    if(keyCode == KeyEvent.KEYCODE_BACK) {

    }
    return true;
  }

  public boolean onKeyUp(int keyCode, KeyEvent event) {
    Log.i(TAG," onKeyUp:"+keyCode);

    if(keyCode == KeyEvent.KEYCODE_BACK) {
      mHandler.obtainMessage(MSG_SEND_CMD_TO_SERVER, MSG_STOP).sendToTarget();
      if(sf != null) {
        sf.stop();
      }
      if(sft != null) {
        sft.stop();
      }
      //finish();
      try {
        Thread.sleep(500);
        System.exit(0);
      }catch (Exception e) {
        e.printStackTrace();
      }

    }
    return true;
  }
  
}