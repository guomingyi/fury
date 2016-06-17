package com.fury;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Handler;
import com.fury.MainActivity;

public class MySurfaceViewTest extends SurfaceView implements Callback
{
	private static final String TAG = "fury-MySurfaceViewTest";
    private SurfaceHolder sfh;
    private Canvas canvas;
    URL videoUrl;
    private String urlstr;
    HttpURLConnection conn;
    Bitmap bmp;
    private Paint p;
    InputStream inputstream=null;
    private Bitmap mBitmap;
    private static int mScreenWidth;
    private static int mScreenHeight;
    public boolean Is_Scale = false;
	private boolean stop_flag = true;

	UdpSendReciverManager mUdpRecv;
	UdpSendReciverManager mUdpSend;

	protected static final int MSG_RECV_FROM_FURY_SERVER = 10;
	protected static final int MSG_RECV_FROM_MJPG_SERVER = 11;

	private int bufSize = 512 * 1024;
	private byte[] buf = new byte[bufSize];

	private String host_ip;
	private int host_port = 9467;

    public MySurfaceViewTest(Context context, AttributeSet attrs)
    {
    	super(context, attrs);
    	initialize();
    	p = new Paint();
	
    	p.setAntiAlias(true);
    	sfh = this.getHolder();
    	sfh.addCallback(this);
    	this.setKeepScreenOn(true);
    	setFocusable(true);
    	this.getWidth();
    	this.getHeight();  
	  
	   }

	 private void initialize() 
	 { 
		DisplayMetrics dm = getResources().getDisplayMetrics(); 
		mScreenWidth = dm.widthPixels; 
		mScreenHeight = dm.heightPixels;    
		this.setKeepScreenOn(true);
	 }

	 private class DrawVideo extends Thread
	 {

		public DrawVideo(){

		}
		 
		 @Override
		 protected Object clone() throws CloneNotSupportedException 
		 {
			 return super.clone();
		 }
		 
		public void run() 
	    {
			Log.i(TAG, "----run----");

	        Paint pt = new Paint();
			pt.setAntiAlias(true);
			pt.setColor(Color.GREEN);
			pt.setTextSize(20);
			pt.setStrokeWidth(1); 

			Arrays.fill(buf, (byte) 0);

			DatagramSocket socket = null;

			try {
				socket = new DatagramSocket(9426);
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}

            String str_fps = "0 fps";
            long times = System.currentTimeMillis();
            long span;
            int fps = 0;
            
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            Log.i(TAG, "receive from server..");
            
			while (true) {
				try {
					socket.receive(packet);
					if(packet.getLength() == 0)
						continue;

                    fps++;
                    span = System.currentTimeMillis() - times;
                    if(span > 1000L) {
                        times = System.currentTimeMillis();
                        str_fps = String.valueOf(fps)+" fps";
                        fps = 0;
                    }

					canvas = sfh.lockCanvas();
					canvas.drawColor(Color.RED);

					byte[] jpg_pkg = packet.getData();
					Log.i(TAG, " jpg_pkg.length:" + jpg_pkg.length);

					Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(jpg_pkg));

					int width = mScreenWidth;
					int height = mScreenHeight;

					float rate_width = (float) mScreenWidth / (float) bmp.getWidth();
					float rate_height = (float) mScreenHeight / (float) bmp.getHeight();

					mBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
					canvas.drawBitmap(mBitmap, (mScreenWidth - width) / 2, (mScreenHeight - height) / 2, null);

					canvas.drawText(str_fps, 2, 22, pt);
					sfh.unlockCanvasAndPost(canvas);
				}
				catch (Exception e) {
					break;
				}
			}
	    }
	}

    public void surfaceChanged(SurfaceHolder holder, int format, int width,    
            int height) {    
    }    
    public void surfaceDestroyed(SurfaceHolder holder) {    
    }   
    public void setUrl(String host, int port){
		urlstr = "http://" + host + ":" + port + "/?action=stream";
		Log.i(TAG,"urlstr:"+urlstr);
	}
	public void reConnectHost() {
		try {
			URL url = new URL(urlstr);
			//urlConn = (HttpURLConnection)url.openConnection();
		}
		catch (Exception e) {
			Log.i(TAG,"reConnectHost:"+e.toString());
		}
	}
	public void start(String ip, int port) {
		if(stop_flag) {
			host_ip = ip;
			host_port = port;
			sendToMjpgServer(ip, port);
			drawVideoSurface();
			stop_flag = false;
		}
	}
	public void stop() {
		if(!stop_flag) {
			stop_flag = true;
			if(mUdpSend != null)
				mUdpSend.setLoop(false);
				mUdpSend.close();
		}
	}

	public void sendToMjpgServer(String ip, int port) {
		//if(mUdpSend == null) {
			mUdpSend = new UdpSendReciverManager(true,ip,port);
			mUdpSend.setLoop(true);
			mUdpSend.udpSend("1001".getBytes());
		//}
	}

	public void recivFromMjpgServer(String ip, int port) {
		//if(mUdpRecv == null) {
			mUdpRecv = new UdpSendReciverManager(false,ip,port);
		//}
	}

	public void release() {
		if(mUdpRecv != null) {
			mUdpRecv.close();
		}
		if(mUdpSend != null) {
			mUdpSend.close();
		}
	}
	public void surfaceCreated(SurfaceHolder holder) {
		//new DrawVideo().start();
		//drawVideoSurface();
	}

	private void drawVideoSurface() {
		new Thread() {
			@Override
			public void run() {
				Paint pt = new Paint();
				pt.setAntiAlias(true);
				pt.setColor(Color.GREEN);
				pt.setTextSize(20);
				pt.setStrokeWidth(1);

				int bufSize = 512 * 1024;
				byte[] buf = new byte[bufSize];

				Arrays.fill(buf, (byte) 0);
				DatagramPacket packet = new DatagramPacket(buf, buf.length);

				String str_fps = "0 fps";
				long times = System.currentTimeMillis();
				long span;
				int fps = 0;

				Log.i(TAG, "drawVideoSurface...");
				//recivFromMjpgServer(host_ip, 9427);

				DatagramSocket recv_socket = null;
				InetAddress local;

				try {
					recv_socket = new DatagramSocket(MainActivity.CAMERA_SERVER_SEND_PORT);
				}
				catch (Exception e) {
					Log.i(TAG, "e"+e.toString());
					return;
				}

				DatagramPacket packet2 = new DatagramPacket(buf, buf.length);

				while (true) {
					if(mUdpSend != null && mUdpSend.getLoop() == false) {
						break;
					}

					try {
						recv_socket.receive(packet2);

						fps++;
						span = System.currentTimeMillis() - times;
						if(span > 1000L) {
							times = System.currentTimeMillis();
							str_fps = String.valueOf(fps)+" fps";
							fps = 0;
						}

						canvas = sfh.lockCanvas();
						canvas.drawColor(Color.RED);

						Log.i(TAG, " packet.length:" + packet2.getLength());

						Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(packet2.getData()));

						int width = mScreenWidth;
						int height = mScreenHeight;

						float rate_width = (float) mScreenWidth / (float) bmp.getWidth();
						float rate_height = (float) mScreenHeight / (float) bmp.getHeight();

						mBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
						canvas.drawBitmap(mBitmap, (mScreenWidth - width) / 2, (mScreenHeight - height) / 2, null);

						canvas.drawText(str_fps, 2, 22, pt);
						sfh.unlockCanvasAndPost(canvas);
					}
					catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}.start();
	}

	@SuppressLint("HandlerLeak")
	private android.os.Handler mHandler = new android.os.Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_RECV_FROM_MJPG_SERVER: {
				}
				break;
			}
		}
	};
}    
