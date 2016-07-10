#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h> 
#include <fcntl.h>

#include <sys/types.h>  
#include <sys/stat.h>  
  
#define TEMP_PATH "/sys/class/thermal/thermal_zone0/temp"  
#define MAX_SIZE 32  


#include "common.h"

/*

DEPSFLAGS := -lpthread 

*/

int debug = 0;
static int mCurrTmp = 0;

static pthread_t socket_thd = 0;
static pthread_t work_thd = 0;
static pthread_t display_thd = 0;
static pthread_t speed_thd = 0;


static double getCpuTmp(void)
{
    int fd;  
    double temp = 0;  
    char buf[MAX_SIZE];  

    fd = open(TEMP_PATH, O_RDONLY);  
    if (fd < 0) {  
        fprintf(stderr, "failed to open thermal_zone0/temp\n");  
        return -1;  
    }  

    if (read(fd, buf, MAX_SIZE) < 0) {  
        fprintf(stderr, "failed to read temp\n");  
        return -1;  
    }  

    temp = atoi(buf) / 1000.0;  
    printf("temp: %.2f\n", temp);  

    close(fd);
  
    return temp;
}


static void myInterrupt10(void) {
    printf ("a-1111\n") ;

}
static void myInterrupt11(void) {
    printf ("a-2222\n") ;

}
static void myInterrupt12(void) {
    printf ("b-3333\n") ;

}
static void myInterrupt13(void) {
    printf ("b-4444\n") ;

}
static void *speed_monitor_thread(void *args) {

    pinMode(1, INPUT); 
    pinMode(6, INPUT); 
    pinMode(10, INPUT);
    pinMode(11, INPUT);

	wiringPiISR(1, INT_EDGE_FALLING, &myInterrupt10) ;
	wiringPiISR(6, INT_EDGE_FALLING, &myInterrupt11) ;
	wiringPiISR(10, INT_EDGE_FALLING, &myInterrupt12) ;
    wiringPiISR(11, INT_EDGE_FALLING, &myInterrupt13) ;

	pthread_exit(NULL);   
	return NULL;
}

static void *work_main_thread(void *args) 
{
	while(1) 
	{
		double tmp = getCpuTmp();
        mCurrTmp = (int)(tmp*100);

		if(tmp >= 42) {
			play_mini_fan(((int)tmp >= 42) ? 1 : 0);
			sleep(10);
		}
		else {
			sleep(5);
		}
	}

	pthread_exit(NULL);   
	return NULL;
}

void getLocalTime(int *y,int *mon, int *d, int *h, int *m, int *s) {
	time_t timep;
	struct tm *p;

	time(&timep);
    p = localtime(&timep);

    *y = p->tm_year + 1900;
    *mon = p->tm_mon + 1;
    *d = p->tm_mday;
    *h = p->tm_hour;
    *m = p->tm_min;
    *s = p->tm_sec;
}

void *server_socket_thread(void *args)
{
	server_socket_init();

	pthread_exit(NULL); 
	return NULL;
}
void *display_thread(void *args)
{
    char buf[20] = {0};
    char time[20] = {0};
	char ip[20] = {0};
    int y, mon, d, h, m, s;

	OLED_GPIO_INIT();
	OLED_Init();
    delay(1000); 
    OLED_Init();
	OLED_Clear();
    OLED_ShowString(0,0,(unsigned char*)"Oled init..");  

	while(1) {
		if(strlen(ip) == 0) {
			get_host_ip(ip);
			delay(500); 
			continue;
		}
        
		OLED_Clear(); 
		OLED_ShowString(0,0,(unsigned char*)ip); 
		getLocalTime(&y,&mon,&d,&h,&m,&s);
		memset(time,0,sizeof(time));
		sprintf(time,"%02d/%02d-%02d:%02d:%02d",mon,d,h,m,s);
        OLED_ShowString(0,3,(unsigned char*)time); 
        memset(buf,0,sizeof(buf));
		sprintf(buf,"CPU:%d.%d C",mCurrTmp/100,mCurrTmp%100);
		OLED_ShowString(0,6,(unsigned char*)buf);  
        delay(1000); 
	}

	pthread_exit(NULL); 
	return NULL;
}
int main(int argc,char *argv[])
{
	int i;

	printf("%s\n",__func__);

#if 0
{
	int test_socket_init(void);
	return test_socket_init();
}
	
#endif

	for(i = 0; i < argc; i++) {
		if(strcmp(argv[i],"-c") == 0) {
			kill_process((char *)"fury_server");
			return 1;
		}
		else
		if(strcmp(argv[i],"-d") == 0) {
			debug = 1;
			printf("set debug mode\n");
		}
	}

    wiringPiSetup();

	pthread_create(&work_thd, NULL, work_main_thread, NULL);
	pthread_create(&socket_thd, NULL, server_socket_thread, NULL);
	pthread_create(&display_thd, NULL, display_thread, NULL);
    pthread_create(&speed_thd, NULL, speed_monitor_thread, NULL);

    pthread_join(work_thd, NULL); 
    pthread_join(socket_thd, NULL); 
    pthread_join(display_thd, NULL); 
    pthread_join(speed_thd, NULL);

	return EXIT_SUCCESS;
}


