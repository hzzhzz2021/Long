
#include <stdio.h>

#include "ls1b.h"
#include "mips.h"
#include "go.h"
#include "uart.h"
//-------------------------------------------------------------------------------------------------
// BSP
//-------------------------------------------------------------------------------------------------

#include "bsp.h"
#include "ns16550.h"
 #include "ls1b_gpio.h"
#include "pwm.h"
#include "ls1x_pwm.h"
#include "pose.h"
#include "UWB.h"

#define yuyin 10    //B5端口语音播报控制
//-------------------------------------------------------------------------------------------------
// 主程序
//-------------------------------------------------------------------------------------------------

int main(void)
{
    pwminit();
    speed(5);  //速度设置
    ls1x_pwm_init(devPWM2,&cfg);
    ls1x_pwm_init(devPWM3,&cfg);
    ls1x_pwm_open(devPWM2,&cfg);
    ls1x_pwm_open(devPWM3,&cfg);

    int rxcnt;
    int Car_Spend;
    uartinit();
    
    for (;;)
    {

    //lookfor(2000,3000);   //导航测试函数


    
    ls1x_uart_write(devUART1,"hello",8,NULL);
    rxcnt=ls1x_uart_read(devUART1,USART1_RX_BUF,200,NULL);
    /*if(rxcnt>7)
    {
 			if(USART1_RX_BUF[0]==0x55)  // 接收到55开头数据
			{
					 
			  	usart1_data();
			}
			else
            {
			  	usart1_data_abnormal();      // 异常数据处理
			}
        
    }*/
    if(rxcnt>1)   //wya 如果接收到正确指令
    {
        if(USART1_RX_BUF[1]==0xAA) 	   //主车控制
   		{
    	    switch(USART1_RX_BUF[2])
			{
			    case 0x01:	//停止
			        Car_Spend = USART1_RX_BUF[3];
				    stop();
				    //Control(Car_Spend,Car_Spend);
                    ls1x_uart_write(devUART1,"STOP",8,NULL);
					break;
  				case 0x22:	//前进
				    Car_Spend = USART1_RX_BUF[3];
				    advance();
				    //Control(Car_Spend,Car_Spend);
                    ls1x_uart_write(devUART1,"FORWARD",8,NULL);
					break;
				case 0x23:	//后退
					Car_Spend = USART1_RX_BUF[3];
					back();
					//Control(-Car_Spend,-Car_Spend);
					ls1x_uart_write(devUART1,"BACK",8,NULL);
					break;
				case 0x24:	//左转
					Car_Spend = USART1_RX_BUF[3];
					//Control(-Car_Spend,Car_Spend);
					//Control(-100,100);
					left() ;
					ls1x_uart_write(devUART1,"LEFT",8,NULL);
					break;
				case 0x25:	//右转
					Car_Spend = USART1_RX_BUF[3];
					right();
					//Control(Car_Spend,-Car_Spend);
					ls1x_uart_write(devUART1,"RIGHT",8,NULL);
					break;
                case 0x26://开启播报
                    gpio_enable(yuyin,DIR_OUT);
                    gpio_write(yuyin,1);
                    ls1x_uart_write(devUART1,"yyk",8,NULL);
                    break;
                case 0x27://关闭播报
                    gpio_enable(yuyin,DIR_OUT);
                    gpio_write(yuyin,0);
                    ls1x_uart_write(devUART1,"yyg",8,NULL);
                    break;
                case 0x30:  //wya 接收到key-value 消息
					//tempMP=USART1_RX_BUF[5];tempMP<<=8;tempMP|=USART1_RX_BUF[4];
					//shortTemp=tempMP;   //wya 将接收到的数据转为16位，保存
					break;
   			}
            //flag1=0;
   	    }
   	    
    }
    
    }

    return 0;
}




