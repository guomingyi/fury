//////////////////////////////////////////////////////////////////////////////////	 
//������ֻ��ѧϰʹ�ã�δ��������ɣ��������������κ���;
//�о�԰����
//���̵�ַ��http://shop73023976.taobao.com/?spm=2013.1.0.0.M4PqC2
//
//  �� �� ��   : main.c
//  �� �� ��   : v2.0
//  ��    ��   : HuangKai
//  ��������   : 2014-0101
//  ����޸�   : 
//  ��������   : OLED 4�ӿ���ʾ����(51ϵ��)
//              ˵��: 
//              ----------------------------------------------------------------
//              GND    ��Դ��
//              VCC  ��5V��3.3v��Դ
//              D0   P1^0��SCL��
//              D1   P1^1��SDA��
//              RES  ��P12
//              DC   ��P13
//              CS   ��P14               
//              ----------------------------------------------------------------
// �޸���ʷ   :
// ��    ��   : 
// ��    ��   : HuangKai
// �޸�����   : �����ļ�
//��Ȩ���У�����ؾ���
//Copyright(C) �о�԰����2014/3/16
//All rights reserved
//******************************************************************************/
//#include "REG51.h"
#include "oled.h"
#include "bmp.h"


//int oled_test(void)
int main(void)
{	
	return oled_test();
}

int oled_test(void) {
	u8 t =' ';

	printf("%s:%d:%s\n",__FILE__,__LINE__,__func__);

	WIRINGPI_INIT() ; 
	OLED_GPIO_INIT();

	OLED_Init();
	OLED_Clear(); 

	//OLED_Clear();
	OLED_ShowCHinese(0,0,0);//��
	OLED_ShowCHinese(18,0,1);//��
	OLED_ShowCHinese(36,0,2);//԰
	OLED_ShowCHinese(54,0,3);//��
	OLED_ShowCHinese(72,0,4);//��
	OLED_ShowCHinese(90,0,5);//��
	OLED_ShowCHinese(108,0,6);//��
	OLED_ShowString(0,2,(unsigned char*)"1.3' OLED TEST");
	OLED_ShowString(8,2,(unsigned char*)"ZHONGJINGYUAN");  
	OLED_ShowString(20,4,(unsigned char*)"2014/05/01");  
	OLED_ShowString(0,6,(unsigned char*)"ASCII:");  
	OLED_ShowString(63,6,(unsigned char*)"CODE:");  
	OLED_ShowChar(48,6,t);   
	t++;
	if(t>'~')t=' ';
	OLED_ShowNum(103,6,t,3,16);	
			
#if 0
	delay_ms(500);
	OLED_Clear();
	delay_ms(500);
	OLED_DrawBMP(0,0,128,8,BMP1);  
	delay_ms(500);
	OLED_DrawBMP(0,0,128,8,BMP2);
	delay_ms(500);
#endif

	for(;;)
	{
		delay(200);

		OLED_ShowChar(48,6,t);

		if(t++>'~')t=' ';

		OLED_ShowNum(103,6,t,3,16);	
	}

	return 0;
}

