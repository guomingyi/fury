#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <termios.h>

#include "common.h"

#if !USE_TCP

static int socket_fd = -1;
static int send_socket_fd = -1;

static int send_socket_init(void)
{
    int on = 1; 
    
    printf("send_socket_init\n");
    if(send_socket_fd > 0) {
    	return 0;
    }
    
    if((send_socket_fd = socket(AF_INET,SOCK_DGRAM,0)) < 0) {
    	perror("socket");
    	return -1;
    }
    
    if((setsockopt(send_socket_fd,SOL_SOCKET,SO_REUSEADDR,&on,sizeof(on)))<0)  {  
    	perror("setsockopt failed");  
    	return -1;
    }  
    
    return 0;
}

static int sendToRemoteServer(struct sockaddr_in *client, char *data) {
    int size = sizeof(struct sockaddr_in);
    int len = sprintf(data, "reply:%s", data);
    
    client->sin_port = htons(DEFAULT_PORT +1);
    if(sendto(send_socket_fd, data, len, 0, (struct sockaddr*)client, size) < 0) {
    	printf("sendto err!\n");
    	return -1;
    }
    return 0;
}

static void signalHandler(int sig) {
    printf("Opps! release udp socket ! \n");
    
    if(socket_fd > 0) {
        close(socket_fd);
        socket_fd = 0;
    }
    
    if(send_socket_fd > 0) {
        close(send_socket_fd);
        send_socket_fd = 0;
    }
    exit(0);
}

int server_socket_init(void)
{
    struct sockaddr_in server;
    struct sockaddr_in client;
    
    int port = DEFAULT_PORT;
    char recv_buf[BUFFER_SIZE] = {0};
    char reply_buf[BUFFER_SIZE] = {0};
    int on = 1;
    
    //reigster ctrl+c ,ctrl+z signal.
    signal(SIGINT, signalHandler); 
    signal(SIGTSTP, signalHandler);
    
    if((socket_fd = socket(AF_INET,SOCK_DGRAM,0)) < 0) {
    	perror("socket");
        goto error;
    }
     
    if((setsockopt(socket_fd,SOL_SOCKET,SO_REUSEADDR,&on, sizeof(on)))<0) {  
    	perror("setsockopt failed");  
        goto error;  
    }  
    
    memset((char*)&server,0,sizeof(server));
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = htonl(INADDR_ANY);
    server.sin_port = htons(port);
    
    if(bind(socket_fd,(struct sockaddr *)&server, sizeof(server))<0) {
    	perror("bind");
        goto error;  
    }
    
    mini_car_gpio_init();
    send_socket_init();
	
    while(1) {
        socklen_t len = sizeof(client);
        memset((char*)&client, 0, len);
        memset(recv_buf, 0, BUFFER_SIZE);
        
        printf("waiting recv client data...\n");
        if(recvfrom(socket_fd, recv_buf, CMD_LENGTH, 0,(struct sockaddr*)&client, &len) < 0) {
        	printf("receive err!\n"); 
        	break;
        }
        
        printf("receive %s\n", recv_buf); 
        do_action(recv_buf, NULL);
        
        memset(reply_buf, 0, BUFFER_SIZE);
        //int send_len = sprintf(reply_buf, "reply:%s", recv_buf);
        if(sendToRemoteServer(&client, reply_buf) < 0) {
            printf("send err!\n"); 
        }
    }

error:
	
    if(socket_fd > 0) {
        close(socket_fd);
        socket_fd = 0;
    }
    
    return EXIT_SUCCESS;
}

#endif

