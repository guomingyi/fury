
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
#include "common.h"

#ifdef USE_TCP


#define PORT DEFAULT_PORT
#define MAX_BUFFER BUFSIZE//1024


int server_sockfd = 0;

int exec_system_call(char *cmd) 
{
    FILE *pp = NULL;

    printf("exec_system_call: %s\n",cmd);
    if((pp = popen(cmd, "r")) == NULL) {
        return -1;
    }

    pclose(pp);
    return 0;
}

int get_pid_by_proc_name(char *proc) 
{
    char tmp[50] = {0};
    char cmd[50] = {0};
    FILE *pp;
    
    sprintf(cmd,"ps -a |grep %s |awk '{print $1}' ", proc);
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

int send_signal_to_proc(int sig, char *proc) 
{
    int pid = get_pid_by_proc_name(proc);
    return kill(pid,sig);
}

void signalHandler(int sig) 
{
    if(server_sockfd > 0) {
        close(server_sockfd);
        server_sockfd = 0;
        printf("----close sock fd----\n");
    }
    exit(0);
}

static void *work_thread(void *args) 
{
    //exec_system_call("/home/pi/mjpg.sh");
    return NULL;
}

void start_camera(void) {
    pthread_t thd = 0;
    pthread_create(&thd, NULL, work_thread, NULL);
    pthread_join(thd, NULL); 
}

//int main(int argc,char *argv[])
int server_socket_init(void)
{
    struct sockaddr_in server_addr;
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY); 
    server_addr.sin_port = htons(PORT);
    int client_sockfd =0;
	int on = 1; 
	int need_connect = 1;

    int size;
    char buffer[MAX_BUFFER + 1];
    struct sockaddr_in client_addr;
    socklen_t len = sizeof(client_addr);

    //reigster ctrl+c /ctrl+z signal.
    signal(SIGINT, signalHandler); 
    signal(SIGTSTP, signalHandler); 

    server_sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if((setsockopt(server_sockfd,SOL_SOCKET,SO_REUSEADDR,&on,sizeof(on))) < 0) {  
        perror("setsockopt failed");  
        exit(EXIT_FAILURE);  
    }  

    bind(server_sockfd, (struct sockaddr *)&server_addr, sizeof(server_addr));
    
    /* listen */
    listen(server_sockfd, 5);
    
    #ifndef UBUNTU_HOST
        mini_car_gpio_init();
    #endif

    static char reply[] = "ok";

    while(1) 
    {
        if(need_connect) {
            printf("waiting connection...\n");
            client_sockfd = accept(server_sockfd, (struct sockaddr *)&client_addr, &len);
            need_connect = 0;
			printf("connect client send 'ok' to client!\n");
			
            write(client_sockfd, reply, 2);
			start_camera();
        }

        memset(buffer, 0, MAX_BUFFER);
        size = read(client_sockfd, buffer, 4);
        // printf("Got %d bytes: %s\n", size, buffer);
        
        if(size == 0) {
            continue;
        }
        
        do_action(buffer, NULL);
		write(client_sockfd, reply, 2);

        if(atoi(buffer) == MSG_STOP) {
            printf("disconnect client !\n");
            close(client_sockfd);
            client_sockfd = 0;
            need_connect = 1;
            //send_signal_to_proc(SIGINT, "mjpg_streamer");
        }
    }
    
    close(server_sockfd);
    return 0;
}

#endif
