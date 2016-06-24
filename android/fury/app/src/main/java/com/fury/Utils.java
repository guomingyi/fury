package com.fury;

/**
 * Created by android on 16-6-22.
 */
public class Utils {

    //协议
    public final static int CMD_LENGTH = 4;

    public final static int MSG_TANK_STOP_RUN = 1099;
    public final static int MSG_TANK_GO_FORWARD = 1100;
    public final static int MSG_TANK_GO_BACK = 1101;
    public final static int MSG_TANK_GO_LEFT = 1102;
    public final static int MSG_TANK_GO_RIGHT = 1103;

    public final static int MSG_LED_OPEN = 1104;
    public final static int MSG_LED_CLOSE = 1105;

    public final static int MSG_FAN_OPEN = 1106;
    public final static int MSG_FAN_CLOSE = 1107;

    public final static int MSG_BEEP_PLAY = 1108;
    public final static int MSG_BEEP_PLAY_REPEED = 1109;

    public final static int MSG_TANK_SPEED_INC = 1110;
    public final static int MSG_TANK_SPEED_DEC = 1111;
    public final static int MSG_GET_SPEED_VALUE = 1112;

    public final static int MSG_SERVO_GO_FORWARD = 1200;
    public final static int MSG_SERVO_GO_BACK = 1201;
    public final static int MSG_SERVO_GO_LEFT = 1202;
    public final static int MSG_SERVO_GO_RIGHT = 1203;

    public final static int MSG_SYS_SLEEP = 2000;
    public final static int MSG_SYS_SHUT_DOWN = 2001;
    public final static int MSG_SYS_REBOOT = 2002;
    public final static int MSG_CAMERA_OPEN = 2003;
    public final static int MSG_CAMERA_CLOSE = 2004;


    public final static String parseCmd(int cmd) {
        return String.valueOf(cmd);
    }
//协议
    
    
}
