
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

#if USE_TCP

#define PORT DEFAULT_PORT
int server_sockfd = 0;

static void signalHandler(int sig) {
    if(server_sockfd > 0) {
        printf("Opps! release socket resource! \n");
        close(server_sockfd);
        server_sockfd = 0;
    }
    exit(0);
}

int server_socket_init(void)
{
    struct sockaddr_in server_addr;
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY); 
    server_addr.sin_port = htons(PORT);
    int client_sockfd =0;
    
    int size;
    char buffer[BUFFER_SIZE + 1];
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

        memset(buffer, 0, BUFFER_SIZE);
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
