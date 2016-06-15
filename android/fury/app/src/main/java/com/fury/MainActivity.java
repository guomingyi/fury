package com.fury;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.net.InetAddress;
import java.util.Arrays;

import com.fury.ControlService.ControlServiceBinder;
import android.content.Context;
import com.fury.MySurfaceView;


public class MainActivity extends Activity implements  View.OnTouchListener {
  private final String TAG = "fury-MainActivity";
  
  protected static final int MSG_CLIENT_SOCKET_INIT = 0;
  protected static final int MSG_CONSOLE_INFO_SHOW = 1;
  protected static final int MSG_SEND_CMD_TO_SERVER = 2;

//host
  private final String SERVER_HOST_IP = "192.168.43.91";
  //private final String SERVER_HOST_IP = "192.168.42.155";

//port
  private final int SERVER_HOST_PORT = 9426;
  private final int CAMERA_SERVER_PORT = 8080;

//协议
  private final static String BTN_LED = "1000";
  private final static String BTN_SPEED_DEC = "1001";
  private final static String BTN_SPEED_INC = "1002";
  private final static String BTN_UP_DOWN = "1003";
  private final static String BTN_UP_UP = "1004";
  private final static String BTN_DOWN_DOWN = "1005";
  private final static String BTN_DOWN_UP = "1006";
  private final static String BTN_LEFT_DOWN = "1007";
  private final static String BTN_LEFT_UP = "1008";
  private final static String BTN_RIGHT_DOWN = "1009";
  private final static String BTN_RIGHT_UP = "1010";
  private final static String MSG_STOP = "2000";
//协议
  
  private Button btnConnect;
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

  private MySurfaceView sf;
  
  private int speed_count = 0;

  private ControlService mControlService;


  public void toastText(String message)
  {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  public void handleException(Exception e, String prefix)
  {
    e.printStackTrace();
    toastText(prefix + e.toString());
  }

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

    setContentView(R.layout.main);
    initView();

    Intent intent = new Intent();
    intent.setClassName(getPackageName(),"com.chariot.ControlService");
    startService(intent);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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

    btn_up = (Button)findViewById(R.id.btn_up);
    btn_down = (Button)findViewById(R.id.btn_down);
    btn_left = (Button)findViewById(R.id.btn_left);
    btn_right = (Button)findViewById(R.id.btn_right);
    btn_mid = (Button)findViewById(R.id.btn_mid);
    btn_speed_dec = (Button)findViewById(R.id.btn_speed_dec);
    btn_speed_inc = (Button)findViewById(R.id.btn_speed_inc);
    
    console_show = (EditText)findViewById(R.id.console_show);
    
    host_ip_view = (EditText)findViewById(R.id.host_ip);
    host_ip = SERVER_HOST_IP;
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
      }
    });
    
    host_port_view = (EditText)findViewById(R.id.host_port);
    host_port = SERVER_HOST_PORT+"";
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
      }
    });

    btnConnect.setOnTouchListener(this);
    btn_mid.setOnTouchListener(this);
    btn_speed_dec.setOnTouchListener(this);
    btn_speed_inc.setOnTouchListener(this);
    btn_up.setOnTouchListener(this);
    btn_down.setOnTouchListener(this);
    btn_left.setOnTouchListener(this);
    btn_right.setOnTouchListener(this);
    setBtnEnable(false);


    sf = (MySurfaceView)findViewById(R.id.mySurfaceViewVideo);
    sf.setUrl(host_ip, CAMERA_SERVER_PORT);
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
  }
  
  
  public void closeSocket()
  {
	Log.i(TAG, "closeSocket");
    sendCmdToServerTcp("" + MSG_STOP);

    try
    {
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
    catch (IOException e)
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

  private void initClientSocketUdp()
  {
    InetAddress local = null;
    String msg = ""+counter++;

    try {
      local = InetAddress.getByName(host_ip);
    } catch (UnknownHostException e) {
      Log.e(TAG, "Host exception:" + e);
    }
    Log.i(TAG, "Host IP："+local.getHostAddress());

    try {
      dSocket = new DatagramSocket(); // <uses-permission android:name="android.permission.INTERNET" />
      msg = "初始化成功!";
    } catch (SocketException e) {
      msg = ""+e;
    }

    mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, 1,1, msg).sendToTarget();
  }

  private void sendCmdToServerTcp(final String cmd) {
    sendMessage(cmd);
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
        Log.i(TAG,"e:"+e.toString());
    }

  }

private void sendCmdToServerUdp(final String cmd) {
	if(dSocket == null) {
		mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, 
				"dSocket is null!,please do init!").sendToTarget();
		return;
	}
	  
	InetAddress local = null;
	String msg = "";
	
	try {
	  local = InetAddress.getByName(host_ip);
	} 
	catch (UnknownHostException e) {
	  Log.e(TAG, "Host exception:" + e);
	  msg = e.toString();
	}
	
	if(local != null) {
	    DatagramPacket dPacket = new DatagramPacket(cmd.getBytes(), cmd.length(), local, Integer.parseInt(host_port));
	    try {
	      dSocket.send(dPacket);
	      Log.i(TAG, "send success:"+cmd);
	  msg = "Send :  { " + cmd + " }  to [ "+host_ip+" : "+host_port+" ]";
	} 
	catch (IOException e) {
	  Log.e(TAG, "send fail:"+e);
	      msg = e.toString();
	    }
	}
	//mHandler.obtainMessage(MSG_CONSOLE_INFO_SHOW, msg).sendToTarget();
}
  
  private void sendMessage(String msg) {
    if(output != null)
      output.print(msg);
  }

  public void onDestroy() {
	closeSocket();
    super.onDestroy();
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

@SuppressLint("HandlerLeak")
private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
    	switch (msg.what) {
        case MSG_CLIENT_SOCKET_INIT:
          if(msg.obj != null && msg.obj.equals(MSG_STOP)) {
            setBtnEnable(false);
          }
          else {
            new Thread() {
              @Override
              public void run() {
                initClientSocketTcp();
              }
            }.start();
          }
          break;

          case MSG_CONSOLE_INFO_SHOW:
            if(msg.obj != null) {
              console_show.setText((String)msg.obj);
            }

            if(msg.arg1 == 1 && msg.arg2 == 1) {
              setBtnEnable(true);
              sf.start();
            }
            else if(msg.arg1 == 1 && msg.arg2 == -1) {
              setBtnEnable(false);
              sf.stop();
            }
            break;
	    		
          case MSG_SEND_CMD_TO_SERVER:
            if(msg.obj != null) {
            final String cmd = (String)msg.obj;
              new Thread() {
                @Override
                public void run() {
                  sendCmdToServerTcp(cmd);
                }
              }.start();
            }
            break;
    	}
    }
};


  public boolean onKeyDown(int keyCode, KeyEvent event) {
    Log.i(TAG," onKeyDown:"+keyCode);

    return super.onKeyDown(keyCode, event);
  }

  public boolean onKeyUp(int keyCode, KeyEvent event) {
    Log.i(TAG," onKeyUp:"+keyCode);

    return super.onKeyUp(keyCode, event);
  }
  
}