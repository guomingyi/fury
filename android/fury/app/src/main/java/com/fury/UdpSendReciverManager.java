package com.fury;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.os.Handler;

public class UdpSendReciverManager {
	private static final String TAG = "fury-UdpSendReciverManager";

	private String server_ip = "192.168.42.170";
	private int send_fury_server_port = 9426;
	private int send_mjpg_server_port = 8080;
	private int recv_mjpg_server_port = 8081;

	private int send_server_port = 8080;

	private boolean isSend = true;


	private DatagramSocket send_socket = null;
	private DatagramPacket send_packet = null;

	private DatagramSocket recv_socket = null;
	private DatagramPacket recv_packet = null;

	private InetAddress local = null;
	private boolean loop = false;
	private boolean recv_loop = false;

	private Handler mHandler;


	protected static final int MSG_CLIENT_SOCKET_INIT = 0;
	protected static final int MSG_CONSOLE_INFO_SHOW = 1;
	protected static final int MSG_SEND_CMD_TO_SERVER = 2;


	protected static final int MSG_RECV_FROM_FURY_SERVER = 10;
	protected static final int MSG_RECV_FROM_MJPG_SERVER = 11;

	public UdpSendReciverManager(final boolean isSend, final String ip, final int port) {
		this.isSend = isSend;
		new Thread() {
			@Override
			public void run() {
				if (isSend) {
					udpSendInit(ip, port);
				} else {
					udpRecvInit(ip, port);
				}
			}
		}.start();
	}

	public void setHandler(Handler h) {
		mHandler = h;
	}

	private void udpSendInit(String ip, int port) {
		Log.i(TAG, "udpSendInit:"+ip+" /"+port);

		try {
			send_socket = new DatagramSocket();
			//local = InetAddress.getByName(ip);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "E:" + e.toString());
			return;
		}

		server_ip = ip;
		send_server_port = port;
	}
	private void udpRecvInit(String ip, int port) {
		Log.i(TAG, "udpRecvInit:"+ip+" /"+port);

		try {
			recv_socket = new DatagramSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "E:" + e.toString());
		}
	}

	public void udpSend(final byte[] data) {
		Log.i(TAG, "udpSend:");

		new Thread() {
			@Override
			public void run() {
				do {
					try {
						InetAddress local = InetAddress.getByName(server_ip);
						DatagramPacket pkt = new DatagramPacket(data, data.length, local, send_server_port);
						send_socket.send(pkt);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(TAG, "E:" + e.toString());
						break;
					}
				} while(loop);
			}
		}.start();
	}

	public void udpRecvData(DatagramPacket pkt) {
		try {
			recv_socket.receive(pkt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setLoop(boolean b) {
		loop = b;
	}
	public boolean getLoop() {
		return loop;
	}
	public void close() {
		if(send_socket != null) {
			send_socket.close();
			send_socket = null;
		}
		if(recv_socket != null) {
			recv_socket.close();
			recv_socket = null;
		}
	}
}


