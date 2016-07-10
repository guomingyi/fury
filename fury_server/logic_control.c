
#include <wiringPi.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include<sys/types.h> 
#include<sys/socket.h> 
#include<unistd.h> 
#include<netinet/in.h> 
#include<arpa/inet.h> 
#include<errno.h> 
#include<netdb.h> 
#include<stdarg.h> 
#include<string.h> 

#include "common.h"

int gpio_init_flag = 0;
int tank_enable_flag = 0;
int led_flag = 0, beep_flag = 0, fan_flag = 0;
int pwmValue = 0;

int curr_cmd = 1; //default 

int servo_v = 90;
int servo_l = 90;

// 1 前
// 2 后
// 3 左
// 4 右
// 5 停

void tank_run_logic(int d) 
{
    if(gpio_init_flag == 0) {
        tank_gpio_init();
        delay(100) ;
    }

	if(d != 5)
    	curr_cmd = d;

    switch(d)
    {
        case 4:
        enable_tank(1);
        digitalWrite(GPIO_PIN_IN1,1);
        digitalWrite(GPIO_PIN_IN2,0);
        digitalWrite(GPIO_PIN_IN3,1);
        digitalWrite(GPIO_PIN_IN4,0);
        break;
        
        case 3:
        enable_tank(1);
        digitalWrite(GPIO_PIN_IN1,0);
        digitalWrite(GPIO_PIN_IN2,1);
        digitalWrite(GPIO_PIN_IN3,0);
        digitalWrite(GPIO_PIN_IN4,1);
        break;
        
        case 1:
        enable_tank(1);
        digitalWrite(GPIO_PIN_IN1,0);
        digitalWrite(GPIO_PIN_IN2,1);
        digitalWrite(GPIO_PIN_IN3,1);
        digitalWrite(GPIO_PIN_IN4,0);
        break;
        
        case 2:
        enable_tank(1);
        digitalWrite(GPIO_PIN_IN1,1);
        digitalWrite(GPIO_PIN_IN2,0);
        digitalWrite(GPIO_PIN_IN3,0);
        digitalWrite(GPIO_PIN_IN4,1);
        break;
        
        case 5:
        enable_tank(0);
        break;
    }
    
    delay(50) ;
}


void enable_tank(int enable)
{
    if(enable) {
    #ifndef PWM_SPEED_CHANGE
    	digitalWrite(GPIO_PIN_EN1,1);
    	digitalWrite(GPIO_PIN_EN2,1);
    #endif
    }
    else {
    #ifndef PWM_SPEED_CHANGE
    	digitalWrite(GPIO_PIN_EN1,0);
    	digitalWrite(GPIO_PIN_EN2,0);
	#endif
        digitalWrite(GPIO_PIN_IN1,0);
        digitalWrite(GPIO_PIN_IN2,0);
        digitalWrite(GPIO_PIN_IN3,0);
        digitalWrite(GPIO_PIN_IN4,0);
    }
}

void enable_tank_led(void)
{  
    if(led_flag == 0) {
        play_beep(1);
        pinMode(GPIO_PIN_LED, OUTPUT) ;
        digitalWrite(GPIO_PIN_LED,0);
        led_flag =1;
    }
    else {
        digitalWrite(GPIO_PIN_LED,1);
        pinMode(GPIO_PIN_LED, OUTPUT);
        led_flag = 0;
    }
}

void play_beep(int n)
{  
    int i;
    pinMode(GPIO_PIN_BEEP, OUTPUT) ;
    
    for(i = 0; i < n; i++) {
        digitalWrite(GPIO_PIN_BEEP,0);
        delay(200);
        digitalWrite(GPIO_PIN_BEEP,1);
        delay(200);
    }
}

void play_mini_fan(int e)
{  
    pinMode(GPIO_PIN_MINI_FAN, OUTPUT) ;
    if(e) {
        digitalWrite(GPIO_PIN_MINI_FAN,0);
    }
    else {
        digitalWrite(GPIO_PIN_MINI_FAN,1);
    }
}
int tank_gpio_init(void)
{
#if !UBUNTU_HOST
    printf("gpio 初始化：%s,%d\n",__func__,gpio_init_flag);

    if(gpio_init_flag == 1) {
        return 0;
    }

    //wiringPiSetup();  

    pinMode(GPIO_PIN_IN1, OUTPUT); 
    pinMode(GPIO_PIN_IN2, OUTPUT);
    pinMode(GPIO_PIN_IN3, OUTPUT);
    pinMode(GPIO_PIN_IN4, OUTPUT);
    enable_tank(0);
    
    pinMode(GPIO_PIN_LED, OUTPUT);
    digitalWrite(GPIO_PIN_LED,1);
    
    pinMode(GPIO_PIN_BEEP, OUTPUT);
    digitalWrite(GPIO_PIN_BEEP,1);
    
	play_mini_fan(0);


//Interrupt
	wiringPiISR(0, INT_EDGE_FALLING, &myInterrupt0) ;
	wiringPiISR(2, INT_EDGE_FALLING, &myInterrupt2) ;
	wiringPiISR(3, INT_EDGE_BOTH, &myInterrupt3) ;

//SWPwm
#ifdef PWM_SPEED_CHANGE
    softPwmCreate(GPIO_PIN_EN1, 0, PWM_RANGE) ;
    softPwmCreate(GPIO_PIN_EN2, 0, PWM_RANGE) ;
#else
    pinMode(GPIO_PIN_EN1, OUTPUT);
    pinMode(GPIO_PIN_EN2, OUTPUT);
    digitalWrite(GPIO_PIN_EN1,0);
    digitalWrite(GPIO_PIN_EN2,0);
#endif
    gpio_init_flag = 1;
    printf("gpio 初始化：ok!");
    play_beep(1);
#endif

	return 0;
}

void myInterrupt0(void) {
    int tmp = digitalRead(0);
    delay(100);
    tmp = digitalRead(0);

    if(tmp == 0) {
		speed_change(1);
		tank_run_logic(1);
	}
}
void myInterrupt2(void) {
    int tmp = digitalRead(2);
    delay(100);
    tmp = digitalRead(2);

    if(tmp == 0) {
		speed_change(0);
		tank_run_logic(1);
	}
}
void myInterrupt3(void) {
    printf ("=============myInterrupt3=========\n") ;

    int tmp = digitalRead(3);
    printf ("===tmp:%d====\n", tmp) ;
    //tank_run_logic(tmp == 0 ? 3 : 5);
       digitalWrite(GPIO_PIN_BEEP, tmp);
       digitalWrite(GPIO_PIN_LED,tmp);
}

void speed_change(int a) {
#ifdef PWM_SPEED_CHANGE
	if(a) {
		pwmValue += CHANGE_STEP;
		if(pwmValue > PWM_RANGE) {
			pwmValue = PWM_RANGE;
			return;
		}
		printf ("===pwmValue:%d====\n", pwmValue) ;
        play_beep(1);
        softPwmWrite(GPIO_PIN_EN1, pwmValue) ;
		softPwmWrite(GPIO_PIN_EN2, pwmValue) ;
       // tank_run_logic(curr_cmd);
	}
	else {
		pwmValue -= CHANGE_STEP;
		if(pwmValue < 0) {
			pwmValue = 0;
			return;
		}
        printf ("===pwmValue:%d====\n", pwmValue) ;
        play_beep(1);
        softPwmWrite(GPIO_PIN_EN1, pwmValue) ;
        softPwmWrite(GPIO_PIN_EN2, pwmValue) ;
       // tank_run_logic(curr_cmd);
	}
#endif
}


int test(void)
{

    printf ("===================start!=====================\n") ;
    // Start the standard way
    
    wiringPiSetup() ; 
    gpio_init_flag =1; 

//beep
    pinMode(GPIO_PIN_BEEP, OUTPUT);
    digitalWrite(GPIO_PIN_BEEP, 0);
    delay(500) ; 
    digitalWrite(GPIO_PIN_BEEP, 1);

//led
    pinMode(GPIO_PIN_LED, OUTPUT) ;
    digitalWrite(GPIO_PIN_LED,0);
    delay(500) ; 
    digitalWrite(GPIO_PIN_LED,1);

//mini fan
    pinMode(GPIO_PIN_MINI_FAN, OUTPUT) ;
    digitalWrite(GPIO_PIN_MINI_FAN,0);
    
//engine
    pinMode(GPIO_PIN_IN1, OUTPUT); 
    pinMode(GPIO_PIN_IN2, OUTPUT);
    pinMode(GPIO_PIN_IN3, OUTPUT);
    pinMode(GPIO_PIN_IN4, OUTPUT);
    tank_run_logic(5);

//Interrupt
	wiringPiISR(0, INT_EDGE_BOTH, &myInterrupt0) ;
	wiringPiISR(2, INT_EDGE_BOTH, &myInterrupt2) ;
	wiringPiISR(3, INT_EDGE_BOTH, &myInterrupt3) ;

//SWPwm
#ifdef PWM_SPEED_CHANGE
  softPwmCreate(GPIO_PIN_EN1, 0, PWM_RANGE) ;
  softPwmCreate(GPIO_PIN_EN2, 0, PWM_RANGE) ;
#else
    pinMode(GPIO_PIN_EN1, OUTPUT);
    pinMode(GPIO_PIN_EN2, OUTPUT);
    digitalWrite(GPIO_PIN_EN1,0);
    digitalWrite(GPIO_PIN_EN2,0);
#endif

    for(;;)   
    {  
        delay(5000) ; 
    }

    printf ("=================@stop!=====================\n") ;
    return 0 ;
}


int parse_event(input_event *e, char *str)
{
	int j = 0;
	char *p = str,*q = str;
	char tmp[10] = {0};
	int tmp2[10] = {0};

	while(*p++ != '\0')
	{
		if(*p == '\n') 
		{
		    memset(tmp,0,sizeof(tmp));
			memcpy(tmp, q, p-q);
			q = p + 1;
			tmp2[j++] = atoi(tmp);
		}
	} 

	e->code = tmp2[0];
	e->value = tmp2[1];
	e->type = tmp2[2];

	printf("%d,%d,%d\n", e->code,e->value,e->type);

	return 0;
}


int do_action(char *cmd)
{
    int tmp = atoi(cmd);

	printf("do_action cmd:%d\n", tmp);
    switch(tmp)
    {
        case MSG_LED_OPEN:
        case MSG_LED_CLOSE:
        	enable_tank_led();
        	break;

        case MSG_BEEP_PLAY:
        	play_beep(1);
        	break;

        case MSG_BEEP_PLAY_REPEED:
			play_beep(3);
        	break;

        case MSG_TANK_SPEED_DEC:
        	speed_change(0);
        	break;

        case MSG_TANK_SPEED_INC:
        	speed_change(1);
        	break;

        case MSG_TANK_GO_FORWARD:
        	tank_run_logic(1);
        	break;

        case MSG_TANK_GO_BACK:
        	tank_run_logic(2);
        	break;

        case MSG_TANK_GO_LEFT:
        	tank_run_logic(3);
        	break;

        case MSG_TANK_GO_RIGHT:
        	tank_run_logic(4);
        	break;

        case MSG_TANK_STOP_RUN:
        	tank_run_logic(5);
        	break;

        case MSG_CAMERA_OPEN:
        	start_camera();
        	break;

		case MSG_SYS_SLEEP:
        case MSG_CAMERA_CLOSE:
        	stop_camera();
        	break;

        case MSG_SERVO_GO_UP:
        	servo_run_logic(1);
        	break;

        case MSG_SERVO_GO_DOWN:
        	servo_run_logic(2);
        	break;

        case MSG_SERVO_GO_LEFT:
        	servo_run_logic(3);
        	break;

        case MSG_SERVO_GO_RIGHT:
        	servo_run_logic(4);
        	break;

        case MSG_SERVO_STOP_RUN:
        	servo_run_logic(5);
        	break;


        default:
        	break;
    }

    return 0;
}

int do_action(int cmd) {
    char buf[CMD_LENGTH+1] = {0};
    if(sprintf(buf,"%d",cmd) > 0) {
    	return do_action(buf);
    }
	return -1;
}


int camera_work_flag = 0;

int exec_system_call(char *cmd) {
#if 1
	int ret = -1;
	if((ret = system(cmd)) < 0) {
		printf("system call: %s :%d\n",cmd, ret);
		return ret;
	}
#else
    FILE *pp = NULL;

    if((pp = popen(cmd, "r")) == NULL) {
        printf("exec :%s fail!\n", cmd);
        return -1;
    }
	
	printf("system call: %s success!\n",cmd);
	
    pclose(pp);
#endif
    return 0;
}

int get_pid_by_proc_name(char *proc) {
    char tmp[50] = {0};
    char cmd[50] = {0};
    FILE *pp;
    
    sprintf(cmd,"ps -e |grep %s |awk '{print $1}' ", proc);
    if((pp = popen(cmd, "r")) == NULL) {
        return -1;
    }
    while(fgets(tmp, sizeof(tmp), pp) != NULL) {
        if (tmp[strlen(tmp) - 1] == '\n') {
            tmp[strlen(tmp) - 1] = '\0'; 
        }
    }
    
    pclose(pp); 
    printf("pid: %s\n",tmp);
    return atoi(tmp);
}
int get_host_ip(char *ip) {
    char tmp[50] = {0};
    char cmd[50] = {0};
    FILE *pp;
    
    sprintf(cmd,"ip route show |awk '{print $9}'");
    if((pp = popen(cmd, "r")) == NULL) {
        return -1;
    }
    while(fgets(tmp, sizeof(tmp), pp) != NULL) {
        if (tmp[strlen(tmp) - 1] == '\n') {
            tmp[strlen(tmp) - 1] = '\0'; 
        }
    }
    
    pclose(pp); 
    printf("%s\n",tmp);
	strcpy(ip,tmp);
    return 0;
}
int send_signal_to_proc(int sig, char *proc) {
    int pid = get_pid_by_proc_name(proc);
    if(pid > 0) {
        printf("kill : %s success!\n", proc);
        return kill(pid,sig);
    }
    printf("kill : %s fail!\n",proc);
    return -1;
}

int kill_process(char *proc) {
    return send_signal_to_proc(SIGINT, proc);
}

static void *work_thread(void *args) {
    if(exec_system_call((char *)EXEC_CAMERA_CMD) == 0) {
        camera_work_flag = 1;
    }
    return NULL;
}

void start_camera(void) {
    if(camera_work_flag == 0) {
        pthread_t thd = 0;
        pthread_create(&thd, NULL, work_thread, NULL);
        pthread_join(thd, NULL); 
    }
}

void stop_camera(void) {
    //if(camera_work_flag == 1) {
        kill_process((char *)"mjpg_streamer");
        camera_work_flag = 0;
    //}
}

int servo_run_logic(int cmd) {
	if(cmd == 1) { //up
	    servo_v -= 10;
		if(servo_v >= 250) {
			servo_v = 250;
		}
		write_to_servo(6,servo_v);
	}
	else
	if(cmd == 2) { //down
	    servo_v += 10;
		if(servo_v <= 50) {
			servo_v = 50;
		}
		write_to_servo(6,servo_v);
	}
	else
	if(cmd == 3) { //left
	    servo_l += 10;
		if(servo_l >= 250) {
			servo_l = 250;
		}
		write_to_servo(5,servo_l);
	}
	else
	if(cmd == 4) { //right
	    servo_l -= 10;
		if(servo_l <= 50) {
			servo_l = 50;
		}
		write_to_servo(5,servo_l);
	}
	else
	if(cmd == 5) {
		//servo_v = servo_l = 90;
		write_to_servo(6,0);
		write_to_servo(5,0);

	}

	return 0;
}



int write_to_servo(int pin, int duty) {
    #define MAX_BUFFER_SIZE 20
    #define DEVFILE	"/dev/servoblaster"

	int ret;
	int fd;
	int len;
	char buf[MAX_BUFFER_SIZE];

	memset(buf, 0, MAX_BUFFER_SIZE);

	// echo 0=150 > /dev/servoblaster
	len = sprintf(buf,"%d=%d\n",pin, duty);  

	printf("len:%d\n", len);
	printf("echo %s >%s \n", buf, DEVFILE);

	fd = open(DEVFILE, O_WRONLY);
	if(fd < 0) {
		printf("---open %s err!---\n", DEVFILE);
		return -1;
	}

	if((ret = write(fd, buf, len)) < 0) {
		printf("---write :%s fail---\n", buf);
	}

	close(fd);

    delay(300);
	return 0;
}


