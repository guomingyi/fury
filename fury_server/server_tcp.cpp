
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

#define EXEC_CAMERA_CMD ". /home/android/pi/github/mjpg-streamer/exec &"

int server_sockfd = 0;
int camera_work_flag = 0;

int exec_system_call(char *cmd) {
    FILE *pp = NULL;

    printf("system call: %s\n",cmd);
    if((pp = popen(cmd, "r")) == NULL) {
        printf("exec :%s fail!\n", cmd);
        return -1;
    }

    pclose(pp);
    return 0;
}

int get_pid_by_proc_name(char *proc) {
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

int send_signal_to_proc(int sig, char *proc) {
    int pid = get_pid_by_proc_name(proc);
    if(pid > 0) {
        printf("kill proc:%s\n", proc);
        return kill(pid,sig);
    }
    printf("kill fail!\n");
    return -1;
}

void signalHandler(int sig) {
    if(server_sockfd > 0) {
        printf("Opps! release socket resource! \n");
        close(server_sockfd);
        server_sockfd = 0;
		stop_camera();
    }
    exit(0);
}

static void *work_thread(void *args) {
    if(exec_system_call(EXEC_CAMERA_CMD) == 0) {
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
    if(camera_work_flag == 1) {
        send_signal_to_proc(SIGINT, "mjpg_streamer");
        camera_work_flag = 0;
    }
}

int server_socket_init(void)
{
    struct sockaddr_in server_addr;
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY); 
    server_addr.sin_port = htons(PORT);
    int client_sockfd =0;
    
    int size;
    char buffer[MAX_BUFFER + 1];
    struct sockaddr_in client_addr;
    socklen_t len = sizeof(client_addr);
    char reply[] = "ok";
    
    //reigster ctrl+c ,ctrl+z signal.
    signal(SIGINT, signalHandler); 
    signal(SIGTSTP, signalHandler); 
    
    server_sockfd = socket(AF_INET, SOCK_STREAM, 0);

    int on = 1; 
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

    while(1) 
    {
        if(!client_sockfd) {
            printf("waiting connection...\n");
            client_sockfd = accept(server_sockfd, (struct sockaddr *)&client_addr, &len);
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
            stop_camera();
        }
    }
    
    close(server_sockfd);
    return 0;
}

#endif
