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
    //printf("temp: %.2f\n", temp);  

    close(fd);
  
    return temp;
}

static void *work_main_thread(void *args) 
{
	while(1) 
	{
		double tmp = getCpuTmp();
        play_mini_fan(((int)tmp >= 42) ? 1 : 0);
		sleep(2);
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

int main(int argc,char *argv[])
{
	printf("%s\n",__func__);

	pthread_create(&work_thd, NULL, work_main_thread, NULL);
	pthread_create(&socket_thd, NULL, server_socket_thread, NULL);

    pthread_join(work_thd, NULL); 
    pthread_join(socket_thd, NULL); 

	return EXIT_SUCCESS;
}


