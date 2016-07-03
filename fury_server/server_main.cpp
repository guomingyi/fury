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



static pthread_t socket_thd = 0;
static pthread_t work_thd = 0;
static pthread_t display_thd = 0;
static int mCurrTmp = 0;

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

void *server_socket_thread(void *args)
{
	server_socket_init();

	pthread_exit(NULL); 
	return NULL;
}
void *display_thread(void *args)
{
    char buf[100] = {0};

	OLED_GPIO_INIT();
	OLED_Init();
	OLED_Clear(); 

	while(1) {
        OLED_ShowString(20,2,(unsigned char*)"2016/07/03"); 
        memset(buf,0,sizeof(buf));
		sprintf(buf,"temp:%d.%d C",mCurrTmp/100,mCurrTmp%100);
		OLED_ShowString(20,5,(unsigned char*)buf);  
        delay(200); 
	}

	pthread_exit(NULL); 
	return NULL;
}
int main(int argc,char *argv[])
{
	printf("%s\n",__func__);

#if 0
{
	int test_socket_init(void);
	return test_socket_init();
}
	
#endif

    wiringPiSetup();

	pthread_create(&work_thd, NULL, work_main_thread, NULL);
	pthread_create(&socket_thd, NULL, server_socket_thread, NULL);
	pthread_create(&display_thd, NULL, display_thread, NULL);

    pthread_join(work_thd, NULL); 
    pthread_join(socket_thd, NULL); 
    pthread_join(display_thd, NULL); 

	return EXIT_SUCCESS;
}


