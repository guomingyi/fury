package com.fury;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import com.fury.MainActivity;

public class MySurfaceViewTest extends SurfaceView implements Callback
{
public static final boolean dbg = true;
private static final String TAG = "fury-MySurfaceViewTest";
private SurfaceHolder sfh;
private Canvas canvas;
URL videoUrl;
private String urlstr;
HttpURLConnection conn;
Bitmap bmp;
private Paint p;
InputStream inputstream = null;
private Bitmap mBitmap;
private static int mScreenWidth;
private static int mScreenHeight;
public boolean Is_Scale = false;
private boolean stop_flag = true;
private boolean start_run = false;

protected static final int MSG_RECV_FROM_FURY_SERVER = 10;
protected static final int MSG_RECV_FROM_MJPG_SERVER = 11;
private static final int BUF_SIZE = 1024 * 1024;

public final static int SERVER_SEND_PORT = MainActivity.CAM_SERVER_PORT;
private final static int SERVER_RECV_PORT = MainActivity.CAM_SERVER_UDP_RECV_PORT;

private String host_ip = "192.168.43.91";
private String host_port = "8080";

public MySurfaceViewTest(Context context, AttributeSet attrs) {
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

private void initialize() { 
    DisplayMetrics dm = getResources().getDisplayMetrics(); 
    mScreenWidth = dm.widthPixels; 
    mScreenHeight = dm.heightPixels;    
    this.setKeepScreenOn(true);
}
 
public void surfaceCreated(SurfaceHolder holder) {
	Log.i(TAG, "surfaceCreated:"+this);
}
public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	Log.i(TAG, "surfaceChanged:"+this);
}
public void surfaceDestroyed(SurfaceHolder holder) {
	Log.i(TAG, "surfaceDestroyed:" + this);
}

public void setHostIp(String ip){
    host_ip = ip;
}
public void setHost(String ip, String port){
	host_ip = ip;
	host_port = port;
}

public void start() {
	if(stop_flag) {
		stop_flag = false;
        drawVideoSurface(true);
	}
}
public void stop() {
	if(!stop_flag) {
		stop_flag = true;

		if(mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

        drawVideoSurface(false);
		mCount = 1;
	}
}

private void drawVideoSurface(boolean r) {
    start_run = r;
    if(!r) {
        Log.i(TAG, "drawVideoSurface stop..");
        return;
    }

	MySurfaceViewTest.this.setBackgroundColor(Color.TRANSPARENT);

	new Thread() {
        
		@Override
		public void run() {
			Log.i(TAG, "drawVideoSurface start:"+host_ip +"/"+host_port);

        //show fps.
			String str_fps = "0 fps";
			long times;
			long span;
			int fps = 0;
			Paint pt = new Paint();
			pt.setAntiAlias(true);
			pt.setColor(Color.GREEN);
			pt.setTextSize(20);
			pt.setStrokeWidth(1);
            
        //recv jpeg fram from mjpg server,prot 8080+1.
            byte[] frame_buf = new byte[BUF_SIZE];
            Arrays.fill(frame_buf, (byte) 0);
            DatagramSocket recv_socket = null;
            
            try {
                recv_socket = new DatagramSocket(null);
				recv_socket.setReuseAddress(true);
				recv_socket.bind(new InetSocketAddress(Integer.parseInt(host_port) + 1));
            }
            catch (Exception e) {
                Log.i(TAG, "e" + e.toString());
				e.printStackTrace();
                return;
            }

			scheduleSendUdp();

			times = System.currentTimeMillis();

			byte[] show_buf = new byte[1024*1024];
			int show_length = 0;

			Matrix mMatrix = new Matrix();
			mMatrix.setRotate(180);

			Log.i(TAG, "udp recv_socket wait..");

			//DatagramPacket recv_pkt = new DatagramPacket(frame_buf, frame_buf.length);

			while(true) {
				try {

					DatagramPacket recv_pkt = new DatagramPacket(frame_buf, frame_buf.length);
					recv_socket.receive(recv_pkt);
					mCount = 0;

					byte[] tmp = recv_pkt.getData();
					//pkg idx,number.
					if(tmp[1] <= tmp[0]) {
						if(dbg)
						Log.i(TAG, " pkg :"+tmp[0]+"/"+tmp[1]);

						System.arraycopy(tmp,2,show_buf,show_length,recv_pkt.getLength());
						show_length += recv_pkt.getLength()-2;

						if(tmp[1] != tmp[0]){
							continue;
						}
					}

					if(dbg)
					Log.i(TAG, "show_length:"+show_length);

					show_length = 0;

					fps++;
					span = System.currentTimeMillis() -times;
					if(span > 1000L) {
						times = System.currentTimeMillis();
						str_fps = String.valueOf(fps)+" fps";
						fps = 0;
					}

					BitmapFactory.Options opts=new BitmapFactory.Options();
					//opts.inJustDecodeBounds = false;
					opts.inPreferredConfig = Bitmap.Config.RGB_565;
					opts.inSampleSize = 1;
					Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(show_buf),null,opts);

					int width = mScreenWidth;
					int height = mScreenHeight;

					if(Is_Scale) {
						float rate_width = (float) mScreenWidth / (float) bmp.getWidth();
						float rate_height = (float) mScreenHeight / (float) bmp.getHeight();
						if(rate_width > rate_height) width = (int)((float)bmp.getWidth()*rate_height);
						if(rate_width < rate_height) height = (int)((float)bmp.getHeight()*rate_width);

						if(dbg)
							Log.i(TAG, "mScreen:"+mScreenWidth+"/"+mScreenHeight
											+" rate:"+rate_width+"/"+rate_height
											+" rate:"+rate_width+"/"+rate_height
											+" width:"+width+"/"+width
											+" height:"+height+"/"+height);
					}

					int x,y;
                    x = (mScreenWidth - width) /2;
					y = Is_Scale ? 0 : ((mScreenHeight - height) /2);

					Bitmap bb = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mMatrix, false);
					mBitmap = Bitmap.createScaledBitmap(bb, width, height, false);

					canvas = sfh.lockCanvas();
					canvas.drawColor(Color.BLACK);
					canvas.drawBitmap(mBitmap, x, y, null);
					canvas.drawText(str_fps, 2, 22, pt);
					sfh.unlockCanvasAndPost(canvas);

					if(!mBitmap.isRecycled()) {
						mBitmap.recycle();
						System.gc();
					}

					if(!start_run) {
						Log.i(TAG,"STOP_RUN..");
						break;
					}

					mCount = 1;
					send_socket.send(send_pkt);

				}
				catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "Ex:" + e.toString());
					mCount = 1;
					try {
						send_socket.send(send_pkt);
					}
					catch (Exception ex){

					}

				}
			}

			try {
				if(send_socket != null) {
					send_socket.close();
					send_socket = null;
				}

				if(recv_socket != null) {
					recv_socket.close();
					recv_socket = null;
				}
				Log.i(TAG, " exit loop success!");
			}
			catch (Exception e) {
				e.printStackTrace();
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

private Timer mTimer = null;
private int mCount = 0;

private DatagramSocket send_socket = null;
private DatagramPacket send_pkt = null;

private void scheduleSendUdp() {
	Log.i(TAG, "scheduleSendUdp..");
	mCount = 1;

	mTimer = new Timer();
	mTimer.schedule(new TimerTask() {
		@Override
		public void run() {
			//if(true) {
			if (mCount == 1) {
				//send msg to mjpg server,port 8080.
				try {
					Log.i(TAG, "timer: udp send_socket to jpg server..");
					final String cmd = "Udp req";
					send_pkt = new DatagramPacket(cmd.getBytes(), cmd.length(),
							InetAddress.getByName(host_ip), Integer.parseInt(host_port));
					if (send_socket == null) {
						send_socket = new DatagramSocket();
						send_socket.setReuseAddress(true);
					}
					send_socket.send(send_pkt);
				}
				catch (Exception e) {
					Log.e(TAG, "exception:" + e);
					e.printStackTrace();
				}
			}
			else {
				Log.i(TAG, "timer run empty loop...." );
			}
		}
	}, 0, 1000);

}



}    
