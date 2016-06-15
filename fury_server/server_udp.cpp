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

static struct sockaddr_in server;//服务器地址
static struct sockaddr_in client;//客户端地址
int socket_fd = -1;

//int main(int argc,char *argv[])
int server_socket_init(void)
{
/*
	printf("argc:%d\n",argc);

	if(argc == 2) {
		return test();
	}
*/


	int port = DEFAULT_PORT;
	socklen_t len;
	char recv_buf[BUFSIZE] = {0};

	if((socket_fd = socket(AF_INET,SOCK_DGRAM,0)) < 0)
	{
		perror("socket");
		exit(EXIT_FAILURE);
	}
	 
	int on=1;  
	if((setsockopt(socket_fd,SOL_SOCKET,SO_REUSEADDR,&on,sizeof(on)))<0)  {  
		perror("setsockopt failed");  
		exit(EXIT_FAILURE);  
	}  

	memset((char*)&server,0,sizeof(server));
	server.sin_family=AF_INET;
	server.sin_addr.s_addr=htonl(INADDR_ANY);
	server.sin_port=htons(port);

	if(bind(socket_fd,(struct sockaddr *)&server,sizeof(server))<0)
	{
		perror("bind");
		exit(EXIT_FAILURE);
	}

	len = sizeof(client);

    printf("mini_car_gpio_init\n");
	mini_car_gpio_init();

    start_camera();

	printf("wainting for connection...\n");
	while(1) {
		if(recvfrom(socket_fd,recv_buf,BUFSIZE-1,0,(struct sockaddr*)&client,&len) < 0) {
			break;
		}

		printf("receive %s\n",recv_buf); 

		char info[BUFSIZE];
		memset(info, 0, BUFSIZE);
        do_action(recv_buf,info);
/*
		char send_buf[BUFSIZE] = {0};
		int sn = sprintf(send_buf,"from server:%s",info);
		if(sendto(socket_fd,send_buf,sn,0,(struct sockaddr *)&client,len)<0) {
			break;
		}
*/
	}

    printf(" exit.");
	close(socket_fd);
	return EXIT_SUCCESS;
}

#endif

