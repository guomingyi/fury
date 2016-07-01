package com.fury;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by android on 16-6-28.
 */
public class VirtualJoystick extends RelativeLayout implements View.OnTouchListener {

    private static final String TAG = "VirtualJoystick-cls";
    public static final int MOVE_TOP = 1;
    public static final int MOVE_BUTTOM = 2;
    public static final int MOVE_LEFT = 3;
    public static final int MOVE_RIGHT = 4;
    public static final int MOVE_CANCEL = 5;

    public static final int MSG_FROM_VJS = 100;

    public static final int MSG_TIMER1_OUT = 200;
    public static final int MSG_TIMER2_OUT = 201;


    public static final int LAY_GAP = 10;

    private RelativeLayout mMoveLayout;
    private ImageButton mMoveButton;

    private ViewGroup.LayoutParams mViewLayoutParams,mParamsBak;

    private int mMove_x, mMove_y;
    private int lastX;
    private int lastY;
    private int screenWidth;
    private int screenHeight;
    private int layout_w;
    private int layout_h;
    private int layout_w2;
    private int layout_h2;

    private boolean boundary_top,boundary_bottom,boundary_left,boundary_right;
    private Handler mHandler;

    private OnMoveListerner mOnMoveListerner = null;
    private OnLongMoveListerner mOnLongMoveListerner = null;

    private Timer mTimer;
    private Timer mTimer2;

    public VirtualJoystick(Context context, AttributeSet attr) {
        super(context, attr);

        LayoutInflater.from(context).inflate(R.layout.virtual_joystick_layout, this);

        initUI();
    }

    private void initUI() {
        mMoveLayout = (RelativeLayout)findViewById(R.id.move_layout);
        layout_w = mMoveLayout.getLayoutParams().width;
        layout_h = mMoveLayout.getLayoutParams().height;

        Log.i(TAG, "w:" + layout_w + " h:" + layout_h);

        //Log.i(TAG, "getPivotX:" + mMoveLayout.getPivotX() + " getPivotY:" + mMoveLayout.getPivotY());
        //Log.i(TAG, "getBottom:" + mMoveLayout.getBottom() + " getRight:" + mMoveLayout.getRight());

        mMoveButton = (ImageButton)findViewById(R.id.move_button);
        mParamsBak = mMoveButton.getLayoutParams();
        layout_w2 = mParamsBak.width;
        layout_h2 = mParamsBak.height;

        Log.i(TAG, "w2:" + layout_w2 + " h2:" + layout_h2);

        mMoveButton.setOnTouchListener(this);
    }

    public void setHandler(Handler h) {
        mHandler = h;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
            }
            break;

            case MotionEvent.ACTION_UP: {
                stoptimer();

                v.setLayoutParams(mParamsBak);
                if(boundary_top || boundary_left || boundary_right || boundary_bottom) {
                    boundary_top = false;
                    boundary_left = false;
                    boundary_right = false;
                    boundary_bottom = false;
                    if(mOnMoveListerner != null) {
                        mOnMoveListerner.onMove(VirtualJoystick.this,MOVE_CANCEL);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                moveView(v, (int) event.getRawX(), (int) event.getRawY(), (int) event.getX(), (int) event.getY());
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
            }
            break;
        }

        return false;
    }

    private void moveView(View v, int x, int y, int x0, int y0) {
        int dx = x - lastX;
        int dy = y - lastY;

        int l = v.getLeft() +dx;
        int b = v.getBottom() +dy;
        int r = v.getRight() +dx;
        int t = v.getTop() +dy;

       // Log.i(TAG, "[ " + x + "," + y + " ]"+" "+"[ " + x0 + "," + y0 + " ]");

 //LEFT
        if(l < 0){
            l = 0;
            r = l +v.getWidth();

            if(!boundary_left && t > LAY_GAP && b < layout_h-LAY_GAP) {
                Log.i(TAG,"----left----");

                boundary_top = false;
                boundary_left = true;
                boundary_right = false;
                boundary_bottom = false;

                if(mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS,MOVE_LEFT).sendToTarget();
                }

                if(mOnMoveListerner != null) {
                    mOnMoveListerner.onMove(VirtualJoystick.this,MOVE_LEFT);
                    startTimer(MOVE_LEFT);
                }
            }
        }
//TOP
        if(t < 0){
            t = 0;
            b = t + v.getHeight();
            if (!boundary_top && l > LAY_GAP && r < layout_w-LAY_GAP) {
                Log.i(TAG, "----top----");

                boundary_top = true;
                boundary_left = false;
                boundary_right = false;
                boundary_bottom = false;

                if (mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS, MOVE_TOP).sendToTarget();
                }

                if (mOnMoveListerner != null) {
                    mOnMoveListerner.onMove(VirtualJoystick.this, MOVE_TOP);
                    startTimer(MOVE_TOP);
                }
            }
        }
//RIGHT
        if(r > layout_w){
            r = layout_w;
            l = r - v.getWidth();

            if (!boundary_right && t > LAY_GAP && b < layout_h-LAY_GAP) {
                Log.i(TAG, "----right----");

                boundary_top = false;
                boundary_left = false;
                boundary_right = true;
                boundary_bottom = false;

                if (mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS, MOVE_RIGHT).sendToTarget();
                }

                if (mOnMoveListerner != null) {
                    mOnMoveListerner.onMove(VirtualJoystick.this, MOVE_RIGHT);
                    startTimer(MOVE_RIGHT);
                }
            }
        }
//BOTTOM
        if(b > layout_h){
            b = layout_h;
            t = b - v.getHeight();

            if (!boundary_bottom && l > LAY_GAP && r < layout_w-LAY_GAP) {
                Log.i(TAG, "----bottom----");

                boundary_top = false;
                boundary_left = false;
                boundary_right = false;
                boundary_bottom = true;

                if (mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS, MOVE_BUTTOM).sendToTarget();
                }

                if (mOnMoveListerner != null) {
                    mOnMoveListerner.onMove(VirtualJoystick.this, MOVE_BUTTOM);
                    startTimer(MOVE_BUTTOM);
                }
            }
        }

        v.layout(l, t, r, b);
        v.postInvalidate();
    }

    public interface OnMoveListerner {
        boolean onMove(View v, int event);
    }
    public interface OnLongMoveListerner {
        boolean onLongMove(View v, int event);
    }
    public void setOnMoveListerner(OnMoveListerner l) {
        mOnMoveListerner = l;
    }
    public void setOnLongMoveListerner(OnLongMoveListerner l) {
        mOnLongMoveListerner = l;
    }

    private void stoptimer() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(mTimer2 != null) {
            mTimer2.cancel();
            mTimer2 = null;
        }
    }

    private void startTimer(final int event) {
        stoptimer();
        if(mOnLongMoveListerner == null) {
            return;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mInternalHandler.obtainMessage(MSG_TIMER1_OUT, event).sendToTarget();

                mTimer2 = new Timer();
                mTimer2.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mInternalHandler.obtainMessage(MSG_TIMER2_OUT, event).sendToTarget();
                    }
                }, 0,200);
            }
        }, 1200);
    }

    @SuppressLint("HandlerLeak")
    private android.os.Handler mInternalHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIMER1_OUT: {
                    Log.i(TAG, "----Timer1 out----");
                }
                break;
                case MSG_TIMER2_OUT: {
                    Log.i(TAG, "----Timer2 out----");
                    if(mOnLongMoveListerner != null) {
                        mOnLongMoveListerner.onLongMove(VirtualJoystick.this,(int)msg.obj);
                    }
                }
                break;
            }
        }
    };
}
