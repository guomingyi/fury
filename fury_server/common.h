#include <linux/input.h>
#include <softPwm.h>
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

//////////////////////////////////////////////////////////////////////
#define printf(fmt, args...) do{ \
    if(1) {\
		printf("\n%s:%d: " fmt "\n", __FILE__, __LINE__, ##args); \
    } \
}while(0)


#define UBUNTU_HOST 0

#if UBUNTU_HOST
#define pinMode(...)
#define digitalWrite(...)
#define wiringPiSetup()
#endif

#define USE_TCP 0   //TCP or UDP

#if UBUNTU_HOST
  #define EXEC_CAMERA_CMD ". /home/android/pi/github/mjpg-streamer/exec &"
#else
  #define EXEC_CAMERA_CMD ". /home/pi/coding/github/mjpg-streamer/exec udp &"
#endif

#define SERVER_IP_ADDR "192.168.43.91"

#define DEFAULT_PORT  9080

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

#define INPUT_DEV "/dev/input/event15"   //gamepad.

///////////////////////////////////////////////////////////////////////
//协议
/*
private final static int CMD_LENGTH = 4;

private final static int MSG_TANK_STOP_RUN =  1099;
private final static int MSG_TANK_GO_FORWARD =  1100;
private final static int MSG_TANK_GO_BACK =  1101;
private final static int MSG_TANK_GO_LEFT =  1102;
private final static int MSG_TANK_GO_RIGHT =  1103;

private final static int MSG_LED_OPEN =  1104;
private final static int MSG_LED_CLOSE =  1105;

private final static int MSG_FAN_OPEN =  1106;
private final static int MSG_FAN_CLOSE =  1107;

private final static int MSG_BEEP_PLAY =  1108;
private final static int MSG_BEEP_PLAY_REPEED =  1109;

private final static int MSG_TANK_SPEED_INC =  1110;
private final static int MSG_TANK_SPEED_DEC =  1111;
private final static int MSG_GET_SPEED_VALUE =  1112;

private final static int MSG_SYS_SLEEP = 2000;
private final static int MSG_SYS_SHUT_DOWN =  2001;
private final static int MSG_SYS_REBOOT =  2002;
private final static int MSG_CAMERA_OPEN =  2003;
private final static int MSG_CAMERA_CLOSE =  2004;
*/

#define CMD_LENGTH 4

#define MSG_TANK_STOP_RUN  1099
#define MSG_TANK_GO_FORWARD  1100
#define MSG_TANK_GO_BACK  1101
#define MSG_TANK_GO_LEFT  1102
#define MSG_TANK_GO_RIGHT  1103

#define MSG_LED_OPEN  1104
#define MSG_LED_CLOSE  1105

#define MSG_FAN_OPEN  1106
#define MSG_FAN_CLOSE  1107

#define MSG_BEEP_PLAY  1108
#define MSG_BEEP_PLAY_REPEED  1109

#define MSG_TANK_SPEED_INC  1110
#define MSG_TANK_SPEED_DEC  1111
#define MSG_GET_SPEED_VALUE  1112

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
void socket_thread(void);
int sendto_server(char *buf);
int sendto_server(input_event *evt);
int do_action(char *cmd, char *info);
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












