
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
int mini_car_enable_flag = 0;
int led_flag = 0, beep_flag = 0, fan_flag = 0;
int pwmValue = 0;

int curr_cmd = 1; //default 

void mini_car_run_logic(int d) 
{
    if(gpio_init_flag == 0) {
        mini_car_gpio_init();
        delay(100) ;
    }

	if(d != 5)
    	curr_cmd = d;

    switch(d)
    {
        case 1://前进
        printf("-----前进----\n");
        enable_mini_car(1);
        digitalWrite(GPIO_PIN_IN1,1);
        digitalWrite(GPIO_PIN_IN2,0);
        digitalWrite(GPIO_PIN_IN3,1);
        digitalWrite(GPIO_PIN_IN4,0);
        break;
        
        case 2://后退
        printf("-----后退----\n");
        enable_mini_car(1);
        digitalWrite(GPIO_PIN_IN1,0);
        digitalWrite(GPIO_PIN_IN2,1);
        digitalWrite(GPIO_PIN_IN3,0);
        digitalWrite(GPIO_PIN_IN4,1);
        break;
        
        case 4://左转
        printf("-----左转----\n");
        enable_mini_car(1);
        digitalWrite(GPIO_PIN_IN1,0);
        digitalWrite(GPIO_PIN_IN2,1);
        digitalWrite(GPIO_PIN_IN3,1);
        digitalWrite(GPIO_PIN_IN4,0);
        break;
        
        case 3://右转
        printf("-----右转----\n");
        enable_mini_car(1);
        digitalWrite(GPIO_PIN_IN1,1);
        digitalWrite(GPIO_PIN_IN2,0);
        digitalWrite(GPIO_PIN_IN3,0);
        digitalWrite(GPIO_PIN_IN4,1);
        break;
        
        case 5://停止
        printf("-----停止----\n");
        enable_mini_car(0);
        break;
    }
    
    delay(50) ;
}


void enable_mini_car(int enable)
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

void enable_mini_car_led(void)
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
int mini_car_gpio_init(void)
{
#ifndef UBUNTU_HOST

    if(gpio_init_flag == 1) {
        return 0;
    }
    
    printf("gpio 初始化：%s,%d\n",__func__,gpio_init_flag);
    
    wiringPiSetup();  

    pinMode(GPIO_PIN_IN1, OUTPUT); 
    pinMode(GPIO_PIN_IN2, OUTPUT);
    pinMode(GPIO_PIN_IN3, OUTPUT);
    pinMode(GPIO_PIN_IN4, OUTPUT);
    enable_mini_car(0);
    
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
		mini_car_run_logic(1);
	}
}
void myInterrupt2(void) {
    int tmp = digitalRead(2);
    delay(100);
    tmp = digitalRead(2);

    if(tmp == 0) {
		speed_change(0);
		mini_car_run_logic(1);
	}
}
void myInterrupt3(void) {
    printf ("=============myInterrupt3=========\n") ;

    int tmp = digitalRead(3);
    printf ("===tmp:%d====\n", tmp) ;
    //mini_car_run_logic(tmp == 0 ? 3 : 5);
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
       // mini_car_run_logic(curr_cmd);
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
       // mini_car_run_logic(curr_cmd);
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
    mini_car_run_logic(5);

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


int do_action(char *cmd, char *info)
{
int evt_up = 0;

/*
if(strcmp(cmd, BTN_LED) == 0) {
    enable_mini_car_led();
}
else 
if(strcmp(cmd, BTN_SPEED_DEC) == 0) {
    speed_change(0);
}
else 
if(strcmp(cmd, BTN_SPEED_INC) == 0) {
    speed_change(1);
}

else 
if(strcmp(cmd, BTN_UP_DOWN) == 0) {
    mini_car_run_logic(1);
}
else 
if(strcmp(cmd, BTN_UP_UP) == 0) {
    evt_up = 1;
}

else 
if(strcmp(cmd, BTN_DOWN_DOWN) == 0) {
    mini_car_run_logic(2);
}
else 
if(strcmp(cmd, BTN_DOWN_UP) == 0) {
    evt_up = 1;
}

else 
if(strcmp(cmd, BTN_LEFT_DOWN) == 0) {
    mini_car_run_logic(3);
}
else 
if(strcmp(cmd, BTN_LEFT_UP) == 0) {
    evt_up = 1;
}

else 
if(strcmp(cmd, BTN_RIGHT_DOWN) == 0) {
    mini_car_run_logic(4);
}
else 
if(strcmp(cmd, BTN_RIGHT_UP) == 0) {
    evt_up = 1;
}
*/

int tmp = atoi(cmd);

printf("do_action:cmd:%d\n",tmp);

switch(tmp)
{
    case BTN_LED:
    	enable_mini_car_led();
    	break;
    case BTN_SPEED_DEC:
    	speed_change(0);
    	break;
    case BTN_SPEED_INC:
    	speed_change(1);
    	break;
    case BTN_UP_DOWN:
    	mini_car_run_logic(1);
    	break;
    case BTN_UP_UP:
    	evt_up = 1;
    	break;
    case BTN_DOWN_DOWN:
    	mini_car_run_logic(2);
    	break;
    case BTN_DOWN_UP:
    	evt_up = 1;
    	break;
    case BTN_LEFT_DOWN:
    	mini_car_run_logic(3);
    	break;
    case BTN_LEFT_UP:
    	evt_up = 1;
    	break;
    case BTN_RIGHT_DOWN:
    	mini_car_run_logic(4);
    	break;
    case BTN_RIGHT_UP:
    	evt_up = 1;
    	break;
    default:
    	break;
}


if(evt_up == 1) {
	mini_car_run_logic(5);
}


return 0;
}


