#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <termios.h>
#include <fcntl.h>

#include "common.h"

#undef BUFFER_SIZE
#define BUFFER_SIZE 65000 

#define MJPG_IP_ADDR "172.16.20.112"


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
    int len = sprintf(data, "1001");
    
    client->sin_port = htons(8080);
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


#if 1
char *udpFileName = "/home/android/pi/udpFile.jpg";
int writeToFile(char *frame, int frame_size) {
int fd;

if(strlen(udpFileName) > 0) {
    printf("writing file: %s\n", udpFileName);

    if((fd = open(udpFileName, O_CREAT | O_RDWR | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH)) < 0) {
        printf("could not open the file %s\n", udpFileName);
        return -1;
    }

    if(write(fd, frame, frame_size) < 0) {
        printf("could not write to file %s\n", udpFileName);
        perror("write()");
        close(fd);
        return -1;
    }

    close(fd);
}

return 0;
}
#endif



char recv_buf[BUFFER_SIZE] = {0};
char reply_buf[BUFFER_SIZE] = {0};
int test_socket_init(void)
{
    struct sockaddr_in server;
    struct sockaddr_in client;
    
    int port = DEFAULT_PORT;
    int on = 1;
    char send_buf[10+1];
	int recv_len = 0;
	socklen_t len = sizeof(client);

    
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
    
    send_socket_init();
    memset((char*)&client,0,sizeof(client));
    client.sin_family = AF_INET;
    client.sin_addr.s_addr = inet_addr(MJPG_IP_ADDR);
    client.sin_port = htons(8080);
	
    while(1) {
		printf("send data...\n");
		
        memset(send_buf, 0, sizeof(send_buf));
        if(sendToRemoteServer(&client, send_buf) < 0) {
            printf("send err!\n"); 
        }

        memset((char*)&client, 0, len);
        memset(recv_buf, 0, BUFFER_SIZE);
		
        printf("waiting recv client data...\n");
        if((recv_len = recvfrom(socket_fd, recv_buf, BUFFER_SIZE, 0,(struct sockaddr*)&client, &len)) < 0) {
        	printf("receive err!\n"); 
        	break;
        }
		
        printf("receive: %s, recv_len:%d\n", recv_buf,recv_len); 
		writeToFile(recv_buf, recv_len);
    }

error:
	
    if(socket_fd > 0) {
        close(socket_fd);
        socket_fd = 0;
    }
    
    return EXIT_SUCCESS;
}

#endif

