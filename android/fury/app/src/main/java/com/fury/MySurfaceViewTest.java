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
import java.net.InetSocketAddress;
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
InputStream inputstream = null;
private Bitmap mBitmap;
private static int mScreenWidth;
private static int mScreenHeight;
public boolean Is_Scale = false;
private boolean stop_flag = true;
private boolean start_run = false;

protected static final int MSG_RECV_FROM_FURY_SERVER = 10;
protected static final int MSG_RECV_FROM_MJPG_SERVER = 11;
private static final int BUF_SIZE = 512 * 1024;

public final static int SERVER_SEND_PORT = MainActivity.CAM_SERVER_PORT;
private final static int SERVER_RECV_PORT = MainActivity.CAM_SERVER_UDP_RECV_PORT;

private String host_ip;

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
 
public void surfaceCreated(SurfaceHolder holder) {}
public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}    
public void surfaceDestroyed(SurfaceHolder holder) {}   

public void setHostIp(String ip){
    host_ip = ip;
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
        drawVideoSurface(false);
	}
}

private void drawVideoSurface(boolean r) {
    start_run = r;
    if(!r) {
        Log.i(TAG, "drawVideoSurface stop..");
        return;
    }
    
	new Thread() {
        
		@Override
		public void run() {
			Log.i(TAG, "drawVideoSurface start..");

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
				recv_socket.bind(new InetSocketAddress(SERVER_RECV_PORT));
            }
            catch (Exception e) {
                Log.i(TAG, "e"+e.toString());
				e.printStackTrace();
                return;
            }

        //send msg to mjpg server,port 8080.
            final String cmd = "1001";
            DatagramSocket send_socket = null;
            DatagramPacket send_pkt = null;
        
            try {
                send_pkt = new DatagramPacket(cmd.getBytes(), cmd.length(),
                    InetAddress.getByName(host_ip), SERVER_SEND_PORT);
				send_socket = new DatagramSocket();
				send_socket.setReuseAddress(true);
                send_socket.send(send_pkt);
            }
            catch (Exception e) {
                Log.e(TAG, "exception:" + e);
				e.printStackTrace();
                return;
            }

			times = System.currentTimeMillis();

			byte[] show_buf = new byte[1024*1024];
			int show_length = 0;

			while(true) {
				try {

                    DatagramPacket recv_pkt = new DatagramPacket(frame_buf, frame_buf.length);
					recv_socket.receive(recv_pkt);

					byte[] tmp = recv_pkt.getData();

					//pkg idx,number.
					if(tmp[1] <= tmp[0]) {
						Log.i(TAG, " pkg :"+tmp[0]+"/"+tmp[1]);

                        System.arraycopy(tmp,2,show_buf,show_length,recv_pkt.getLength());
						show_length += recv_pkt.getLength()-2;

						if(tmp[1] != tmp[0]){
							continue;
						}
					}

					Log.i(TAG, "show_length:"+show_length);
					//send_socket.send(send_pkt);
					//if(true)
					//	continue;

					fps++;
					span = System.currentTimeMillis() -times;
					if(span > 1000L) {
						times = System.currentTimeMillis();
						str_fps = String.valueOf(fps)+" fps";
						fps = 0;
					}

					canvas = sfh.lockCanvas();
					canvas.drawColor(Color.RED);

					Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(show_buf));

					int width = mScreenWidth;
					int height = mScreenHeight;

					float rate_width = (float) mScreenWidth / (float) bmp.getWidth();
					float rate_height = (float) mScreenHeight / (float) bmp.getHeight();

					mBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
					canvas.drawBitmap(mBitmap, (mScreenWidth - width) / 2, (mScreenHeight - height) / 2, null);

					canvas.drawText(str_fps, 2, 22, pt);
					sfh.unlockCanvasAndPost(canvas);

					if(!start_run) {
						break;
					}
					send_socket.send(send_pkt);
					Arrays.fill(show_buf, (byte) 0);
					show_length = 0;
				}
				catch (Exception e) {
					e.printStackTrace();
					break;
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
			catch (Exception e){
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
}    
