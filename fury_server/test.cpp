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
char *udpFileName = (char *)"/home/android/pi/udpFile.jpg";
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

#define PKG_BUF_SIZE 4096 //byte
#define TAG_SIZE 2
#define TOTAL_SEND_SIZE (TAG_SIZE+PKG_BUF_SIZE)


static int sendToRemoteServer(struct sockaddr_in *client, char *frame, int len) {
    int size = sizeof(struct sockaddr_in);
    client->sin_port = htons(9091);


	int i;
	int mj = len % PKG_BUF_SIZE;
	int n = (len / PKG_BUF_SIZE + (mj > 0 ? 1 : 0));
	
	char *p = frame;
	char s_buf[TOTAL_SEND_SIZE*5] = {0};
	
	printf("sendToRemoteServer:port:%d,len:%d,n:%d,mj:%d\n",9091,len,n,mj);
	
	int send_len = 0;
	
	for(i = 1; i <= n; i++) {
	
		memset(s_buf, 0, TOTAL_SEND_SIZE);
		s_buf[0] = n;
		s_buf[1] = i;
	
		if(i == n && mj > 0) {
			send_len = (len -PKG_BUF_SIZE*(i-1)+1+1);
			memcpy(s_buf+TAG_SIZE, (void*)p, send_len);
		}
		else {
			send_len = TOTAL_SEND_SIZE;
			memcpy(s_buf+TAG_SIZE, (void*)p, send_len-1-1);
		}

		printf("%d/%d/%d\n%s\n",i,n,send_len,s_buf);
		if(sendto(send_socket_fd, s_buf, send_len, 0, (struct sockaddr*)client, size) < 0) {
			perror("sendto");
			return -1;
		}

		p = p+PKG_BUF_SIZE;
	
	}

    return 0;
}


int writeServo(int pin, int duty) 
{
#define MAX_BUFFER_SIZE 20
#define DEVFILE	"/dev/servoblaster"


int ret;
int fd;
int len;
char buf[MAX_BUFFER_SIZE];

memset(buf, 0, MAX_BUFFER_SIZE);

// echo 0=150 > /dev/servoblaster
len = sprintf(buf,"%d=%d",pin, duty);  

printf("len:%d\n", len);
printf("echo %s >%s \n", buf, DEVFILE);

fd = open(DEVFILE, O_WRONLY);
if(fd < 0) {
	printf("---open %s err!---\n", DEVFILE);
	return -1;
}

if((ret = write(fd, buf, len)) < 0) {
	printf("---write :%s fail---\n", buf);
}

close(fd);

return 0;

}

static int count = 0;
static void sigroutine(int sig) {
printf("---%s:%d:%d---\n",__func__,sig,++count);

switch(sig) {
	case SIGALRM:
		signal(SIGALRM,sigroutine);
		break;
	case SIGVTALRM:
		signal(SIGVTALRM,sigroutine);
		break;
}

}


char recv_buf[BUFFER_SIZE] = {0};
char reply_buf[BUFFER_SIZE] = {0};
int test_socket_init(void)
{
    struct sockaddr_in server;
    struct sockaddr_in client;
    
    int port = DEFAULT_PORT;
    int on = 1;
	int recv_len = 0;
	socklen_t len = sizeof(client);

	#define buf_size (8194)
	char send_buf[buf_size+1] = {0};
	int i,j = 0;


#if 0
{
	printf("---test---\n");

struct itimerval value,ovalue,value2;

if(signal(SIGALRM,sigroutine) == SIG_ERR) {
printf("---err1---\n");
return -1;
}
if(signal(SIGVTALRM,sigroutine) == SIG_ERR) {
printf("---err2---\n");
return -1;
}

printf("---%d,%d--\n",SIGALRM,SIGVTALRM);

value.it_value.tv_sec = 1;
value.it_value.tv_usec = 0;
value.it_interval.tv_sec = 1;
value.it_interval.tv_usec = 0;
//setitimer(ITIMER_REAL,&value,&ovalue);

value2.it_value.tv_sec = 0;
value2.it_value.tv_usec = 500000;
value2.it_interval.tv_sec = 0;
value2.it_interval.tv_usec = 500000;
setitimer(ITIMER_REAL,&value2,&ovalue);
	for (;;)
		delay (20) ;
}



#endif












	for(i = 0; i < buf_size; i++) {
		if(i < 1024)
	    	send_buf[i] = 'a';
		else
		if(i < 2048)
			send_buf[i] = 'b';
		else
		if(i < 3072)
			send_buf[i] = 'c';
		else
		if(i < 4096)
			send_buf[i] = 'd';
		else{
			send_buf[i] = 'e';
			j++;
		}
			

	}

    printf("%s,j:%d\n",send_buf,j);
	
	
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
    client.sin_port = htons(9090);
	
    while(1) {

        memset((char*)&client, 0, len);
        memset(recv_buf, 0, BUFFER_SIZE);
		
        printf("waiting recv client data...\n");
        if((recv_len = recvfrom(socket_fd, recv_buf, BUFFER_SIZE, 0,(struct sockaddr*)&client, &len)) < 0) {
        	printf("receive err!\n"); 
        	break;
        }
		
        printf("receive: %s, recv_len:%d\n", recv_buf,recv_len); 
		//writeToFile(recv_buf, recv_len);
		
       // memset(send_buf, 0, sizeof(send_buf));
        if(sendToRemoteServer(&client, send_buf, buf_size) < 0) {
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

#define DEV_PATH "/dev/input/event0"   //difference is possible


void js_event_callback(int axes, int keycode, int is_down, int x, int y)
{
    printf("%d,%d,%d,(%d,%d)",axes, keycode, is_down, x, y);
    return;
}


int test_input_key_event(void)
{
    int keys_fd;
    struct input_event t;
    keys_fd=open(DEV_PATH, O_RDONLY);
    if(keys_fd <= 0)
    {
        printf("open %s device error!\n", DEV_PATH);
        return -1;
    }

#if 1
{
    JS_MAIN(js_event_callback);
    return 1;
}
#endif

    while(1)
    {
        if(read(keys_fd, &t, sizeof(t)) == sizeof(t))
        {
           // printf("type,code,value:(%d,%d,%d)\n",t.type,t.code,t.value);

            if(t.type==EV_KEY) {
                if(t.value == 1)
                {
                    printf("key %d %s\n", t.code, (t.value) ? "Pressed" : "Released");
                    if (t.code == 304) {
                         do_action(MSG_TANK_GO_FORWARD);  
                    }
                    else
                    if (t.code == 306) {
                         do_action(MSG_TANK_GO_BACK);  
                    }
                    else
                    if (t.code == 307) {
                        do_action(MSG_TANK_GO_LEFT);
                    }
                    else
                    if (t.code == 305) {
                         do_action(MSG_TANK_GO_RIGHT);  
                    }  
                    else
                    if (t.code == 311) {
                        do_action(MSG_TANK_SPEED_DEC);  
                    } 
                    else
                    if (t.code == 310) {
                        do_action(MSG_TANK_SPEED_INC);  
                    } 
                }
                else
                if (t.value == 0) {
                    do_action(MSG_TANK_STOP_RUN);
                }
            }
            else {
              //  printf("t.type,tcode,tvalue:%d,%d,%d\n",t.type,t.code,t.value);
            }

        }
    }
    close(keys_fd);


return 0;
}



