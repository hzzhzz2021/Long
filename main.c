
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

#define yuyin 10    //B5�˿�������������
//-------------------------------------------------------------------------------------------------
// ������
//-------------------------------------------------------------------------------------------------

int main(void)
{
    pwminit();
    speed(5);  //�ٶ�����
    ls1x_pwm_init(devPWM2,&cfg);
    ls1x_pwm_init(devPWM3,&cfg);
    ls1x_pwm_open(devPWM2,&cfg);
    ls1x_pwm_open(devPWM3,&cfg);

    int rxcnt;
    int Car_Spend;
    uartinit();
    
    for (;;)
    {

    //lookfor(2000,3000);   //�������Ժ���


    
    ls1x_uart_write(devUART1,"hello",8,NULL);
    rxcnt=ls1x_uart_read(devUART1,USART1_RX_BUF,200,NULL);
    /*if(rxcnt>7)
    {
 			if(USART1_RX_BUF[0]==0x55)  // ���յ�55��ͷ����
			{
					 
			  	usart1_data();
			}
			else
            {
			  	usart1_data_abnormal();      // �쳣���ݴ���
			}
        
    }*/
    if(rxcnt>1)   //wya ������յ���ȷָ��
    {
        if(USART1_RX_BUF[1]==0xAA) 	   //��������
   		{
    	    switch(USART1_RX_BUF[2])
			{
			    case 0x01:	//ֹͣ
			        Car_Spend = USART1_RX_BUF[3];
				    stop();
				    //Control(Car_Spend,Car_Spend);
                    ls1x_uart_write(devUART1,"STOP",8,NULL);
					break;
  				case 0x22:	//ǰ��
				    Car_Spend = USART1_RX_BUF[3];
				    advance();
				    //Control(Car_Spend,Car_Spend);
                    ls1x_uart_write(devUART1,"FORWARD",8,NULL);
					break;
				case 0x23:	//����
					Car_Spend = USART1_RX_BUF[3];
					back();
					//Control(-Car_Spend,-Car_Spend);
					ls1x_uart_write(devUART1,"BACK",8,NULL);
					break;
				case 0x24:	//��ת
					Car_Spend = USART1_RX_BUF[3];
					//Control(-Car_Spend,Car_Spend);
					//Control(-100,100);
					left() ;
					ls1x_uart_write(devUART1,"LEFT",8,NULL);
					break;
				case 0x25:	//��ת
					Car_Spend = USART1_RX_BUF[3];
					right();
					//Control(Car_Spend,-Car_Spend);
					ls1x_uart_write(devUART1,"RIGHT",8,NULL);
					break;
                case 0x26://��������
                    gpio_enable(yuyin,DIR_OUT);
                    gpio_write(yuyin,1);
                    ls1x_uart_write(devUART1,"yyk",8,NULL);
                    break;
                case 0x27://�رղ���
                    gpio_enable(yuyin,DIR_OUT);
                    gpio_write(yuyin,0);
                    ls1x_uart_write(devUART1,"yyg",8,NULL);
                    break;
                case 0x30:  //wya ���յ�key-value ��Ϣ
					//tempMP=USART1_RX_BUF[5];tempMP<<=8;tempMP|=USART1_RX_BUF[4];
					//shortTemp=tempMP;   //wya �����յ�������תΪ16λ������
					break;
   			}
            //flag1=0;
   	    }
   	    
    }
    
    }

    return 0;
}




