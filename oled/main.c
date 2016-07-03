//////////////////////////////////////////////////////////////////////////////////	 
//本程序只供学习使用，未经作者许可，不得用于其它任何用途
//中景园电子
//店铺地址：http://shop73023976.taobao.com/?spm=2013.1.0.0.M4PqC2
//
//  文 件 名   : main.c
//  版 本 号   : v2.0
//  作    者   : HuangKai
//  生成日期   : 2014-0101
//  最近修改   : 
//  功能描述   : OLED 4接口演示例程(51系列)
//              说明: 
//              ----------------------------------------------------------------
//              GND    电源地
//              VCC  接5V或3.3v电源
//              D0   P1^0（SCL）
//              D1   P1^1（SDA）
//              RES  接P12
//              DC   接P13
//              CS   接P14               
//              ----------------------------------------------------------------
// 修改历史   :
// 日    期   : 
// 作    者   : HuangKai
// 修改内容   : 创建文件
//版权所有，盗版必究。
//Copyright(C) 中景园电子2014/3/16
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
	OLED_ShowCHinese(0,0,0);//中
	OLED_ShowCHinese(18,0,1);//景
	OLED_ShowCHinese(36,0,2);//园
	OLED_ShowCHinese(54,0,3);//电
	OLED_ShowCHinese(72,0,4);//子
	OLED_ShowCHinese(90,0,5);//科
	OLED_ShowCHinese(108,0,6);//技
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

