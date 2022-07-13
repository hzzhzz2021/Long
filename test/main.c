
#include <stdio.h>

#include "ls1b.h"
#include "mips.h"
#include "go.h"
#include "uart.h"
//-------------------------------------------------------------------------------------------------
// BSP
//-------------------------------------------------------------------------------------------------
#include "ls1x_pwm.h"
#include "bsp.h"
#include "ns16550.h"
 #include "ls1b_gpio.h"
#include "pwm.h"

//-------------------------------------------------------------------------------------------------
// ������
//-------------------------------------------------------------------------------------------------

int main(void)
{
    pwminit();
    speed(1);
    ls1x_pwm_open(devPWM2,&cfg);
    ls1x_pwm_open(devPWM3,&cfg);
    int rxcnt;
    int Car_Spend;
    unsigned int BaudRate = 9600;
    ls1x_uart_init(devUART1,(void *)BaudRate); //��ʼ������
    ls1x_uart_open(devUART1,NULL); //�򿪴���
    for (;;)
    {

    //ls1x_uart_write(devUART1,"hell",8,NULL);
    rxcnt=ls1x_uart_read(devUART1,USART1_RX_BUF,235,NULL);
    if(rxcnt>0)
    {
        //ls1x_uart_write(devUART1,"sfsf",8,NULL);
        delay_ms(100);
        //if(strncmp(pop,"U",1) == 0)
        //{
        //    ls1x_uart_write(devUART1,"led_on",8,NULL);
        //}

        if(USART1_RX_BUF[0]==0x55)
        {
           // ls1x_uart_write(devUART1,"led_on",8,NULL);
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
                case 0x30:  //wya ���յ�key-value ��Ϣ
					//tempMP=USART1_RX_BUF[5];tempMP<<=8;tempMP|=USART1_RX_BUF[4];
					//shortTemp=tempMP;   //wya �����յ�������תΪ16λ������
					break;


   			}

   		  }



        }



    }
    delay_ms(100);
     // UART5_Test();//���ڿ��ƺ���
    }

    return 0;
}




