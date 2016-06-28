package com.fury;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * Created by android on 16-6-28.
 */
public class VirtualJoystick extends RelativeLayout implements View.OnTouchListener {

    private static final String TAG = "VirtualJoystick-cls";
    public static final int ACTION_UP = 1;
    public static final int ACTION_DOWN = 2;
    public static final int ACTION_LEFT = 3;
    public static final int ACTION_RIGHT = 4;
    public static final int MSG_FROM_VJS = 100;

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

    private boolean boundary_top,boundary_bottom,boundary_left,boundary_right;
    private Handler mHandler;

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

        mMoveButton = (ImageButton)findViewById(R.id.move_button);
        mParamsBak = mMoveButton.getLayoutParams();
        mMoveButton.setOnTouchListener(this);
    }

    public void setHandler(Handler h) {
        mHandler = h;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Log.i(TAG, "$$$ onTouch:" + v);

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            {
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
            }
            break;

            case MotionEvent.ACTION_UP: {
                v.setLayoutParams(mParamsBak);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                moveView(v, (int) event.getRawX(),(int) event.getRawY());
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
            }
            break;

            case MotionEvent.ACTION_OUTSIDE: {

            }
            break;

        }

        return false;
    }


    private void moveView(View v, int x, int y) {
        int dx = x - lastX;
        int dy = y - lastY;

        int l = v.getLeft() +dx;
        int b = v.getBottom() +dy;
        int r = v.getRight() +dx;
        int t = v.getTop() +dy;

        if(l < 0){
            l = 0;
            r = l +v.getWidth();
            if(!boundary_left) {
                Log.i(TAG,"----LEFT----");

                boundary_top = false;
                boundary_left = true;
                boundary_right = false;
                boundary_bottom = false;
                if(mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS,ACTION_LEFT).sendToTarget();
                }
            }
        }

        if(t < 0){
            t = 0;
            b = t +v.getHeight();
            if(!boundary_top) {
                Log.i(TAG,"----TOP----");

                boundary_top = true;
                boundary_left = false;
                boundary_right = false;
                boundary_bottom = false;
                if(mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS,ACTION_UP).sendToTarget();
                }
            }
        }

        if(r > layout_w){
            r = layout_w;
            l = r - v.getWidth();
            if(!boundary_right) {
                Log.i(TAG,"----RIGHT----");

                boundary_top = false;
                boundary_left = false;
                boundary_right = true;
                boundary_bottom = false;
                if(mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS,ACTION_RIGHT).sendToTarget();
                }
            }
        }

        if(b > layout_h){
            b = layout_h;
            t = b -v.getHeight();
            if(!boundary_bottom) {
                Log.i(TAG,"----BOTTOM----");

                boundary_top = false;
                boundary_left = false;
                boundary_right = false;
                boundary_bottom = true;
                if(mHandler != null) {
                    mHandler.obtainMessage(MSG_FROM_VJS,ACTION_DOWN).sendToTarget();
                }
            }
        }

        v.layout(l, t, r, b);
        v.postInvalidate();
    }


}
