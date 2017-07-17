#include <linux/input.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <signal.h>
#include <pthread.h> 
#include <fcntl.h>

#include <stdio.h>
#include <errno.h>
#include <string.h>

#include <wiringPi.h>
//#include <softServo.h>
#include <softPwm.h>
#include <time.h>
#include <sys/time.h>
#include "../oled/oled.h"

//////////////////////////////////////////////////////////////////////
extern int debug;

#define printf(fmt, args...) do{ \
    if(debug) {\
		printf("\n%s:%d: " fmt "\n", __FILE__, __LINE__, ##args); \
    } \
}while(0)


#define UBUNTU_HOST 0

#if UBUNTU_HOST
#define pinMode(...)
#define digitalWrite(...)
#define wiringPiISR(...)
#define wiringPiSetup()
#endif

#define OLED_DEFINE 1

#define USE_TCP 0   //TCP or UDP

#if UBUNTU_HOST
  #define EXEC_CAMERA_CMD ". /home/android/pi/github/mjpg-streamer/exec &"
#else
  #define EXEC_CAMERA_CMD "/home/pi/coding/github/mjpg-streamer/exec udp &"
#endif

#define SERVER_IP_ADDR "192.168.43.91"

#define DEFAULT_PORT  9090

#ifdef BUFFER_SIZE
#undef BUFFER_SIZE
#endif
#define BUFFER_SIZE 1024 

#define GPIO_PIN_IN1 29  //马达 N1-N4逻辑输入.高电平使能
#define GPIO_PIN_IN2 21
#define GPIO_PIN_IN3 22
#define GPIO_PIN_IN4 23

#define GPIO_PIN_EN1 24  //通道A使能，高电平使能
#define GPIO_PIN_EN2 25  //通道B使能

#define GPIO_PIN_BEEP 26 //蜂鸣器 ,低电平有效
#define GPIO_PIN_LED 27  //LED灯 ,低电平有效
#define GPIO_PIN_MINI_FAN 28 //小风扇 ,低电平有效

#define CHANGE_STEP 10
#define PWM_RANGE 100

#define PWM_SPEED_CHANGE 1

// SPEED GPIO PIN.
#define LEFT_SPEED_A 	1
#define LEFT_SPEED_B 	6
#define RIGHT_SPEED_A 	10
#define RIGHT_SPEED_B 	11


#define INPUT_DEV "/dev/input/event15"   //gamepad.

///////////////////////////////////////////////////////////////////////
//协议
/*

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

public final static int MSG_SERVO_GO_UP = 1200;
public final static int MSG_SERVO_GO_DOWN = 1201;
public final static int MSG_SERVO_GO_LEFT = 1202;
public final static int MSG_SERVO_GO_RIGHT = 1203;
public final static int MSG_SERVO_STOP_RUN = 1204;

public final static int MSG_SYS_SLEEP = 2000;
public final static int MSG_SYS_SHUT_DOWN = 2001;
public final static int MSG_SYS_REBOOT = 2002;
public final static int MSG_CAMERA_OPEN = 2003;
public final static int MSG_CAMERA_CLOSE = 2004;

*/

#define CMD_LENGTH 4

// TANK.
#define MSG_TANK_STOP_RUN  1099
#define MSG_TANK_GO_FORWARD  1100
#define MSG_TANK_GO_BACK  1101
#define MSG_TANK_GO_LEFT  1102
#define MSG_TANK_GO_RIGHT  1103
#define MSG_TANK_SPEED_INC  1110
#define MSG_TANK_SPEED_DEC  1111
#define MSG_GET_SPEED_VALUE  1112

// LED,FAN,BEEP.
#define MSG_LED_OPEN  1104
#define MSG_LED_CLOSE  1105
#define MSG_FAN_OPEN  1106
#define MSG_FAN_CLOSE  1107
#define MSG_BEEP_PLAY  1108
#define MSG_BEEP_PLAY_REPEED  1109


// SERVO.
#define MSG_SERVO_GO_UP  1200
#define MSG_SERVO_GO_DOWN  1201
#define MSG_SERVO_GO_LEFT  1202
#define MSG_SERVO_GO_RIGHT  1203
#define MSG_SERVO_STOP_RUN  1204

//SYS.
#define MSG_SYS_SLEEP  2000
#define MSG_SYS_SHUT_DOWN  2001
#define MSG_SYS_REBOOT  2002
#define MSG_CAMERA_OPEN  2003
#define MSG_CAMERA_CLOSE  2004
//协议
//////////////////////////////////////////////////////////////////////

int sendToServer(char *buffer);
int sendToServer(int evt);
int sendToServer();
int sendToServer(input_event *evt);
int client_socket_init(const char *ip_addr, int port_num);
int reciver(void);
int event_register(void);
void input_event_thread(void);
int test_input_key_event(void);
void socket_thread(void);
int sendto_server(char *buf);
int sendto_server(input_event *evt);
int do_action(char *cmd);
int do_action(int cmd);
int parse_event(input_event *e, char *str);
int test(void);
void enable_tank(int enable);
void enable_tank_led();
void speed_change(int a);
int server_socket_init(void);
void play_mini_fan(int e);
void stop_camera(void);
void start_camera(void);
int send_signal_to_proc(int sig, char *proc);
void myInterrupt0(void);
void myInterrupt2(void);
void myInterrupt3(void);
void tank_run_logic(int d);
int tank_gpio_init(void);
void play_beep(int n);
int servo_run_logic(int cmd);
int write_to_servo(int pin, int duty) ;
int get_host_ip(char *ip);
int kill_process(char *proc);










