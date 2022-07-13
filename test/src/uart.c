/*
 * uart.c
 *
 * created: 2022/6/23
 *  author:
 */

#include "bsp.h"
#include "ns16550.h"
#include "uart.h"




char USART1_RX_BUF[RXD1_MAX_NUM];     //���ջ���,���200���ֽ�.
unsigned char flag1;


void uartinit()
{
    unsigned int BaudRate=115200;
    ls1x_uart_init(devUART1,(void*)BaudRate);
    ls1x_uart_open(devUART1,NULL);
}



/***************************************************************
** ���ܣ�     WiFi���ڽ�������У��
** ������   �޲���
** ���ߣ�     wya
** ����ֵ��    ��
****************************************************************/

void usart1_data(void)
{
 unsigned char sum=0;

 if(USART1_RX_BUF[7]==0xbb)  // �жϰ�β
  {
  //��ָ������λ��ָ�������У��
  //ע�⣺��������ʱӦ�öԺ���256ȡ�ࡣ
   sum=(USART1_RX_BUF[2]+USART1_RX_BUF[3]+USART1_RX_BUF[4]+USART1_RX_BUF[5])%256;
  if(sum==USART1_RX_BUF[6])
  {
      flag1=1;
  }
  else flag1=0;
  }



}

void usart1_data_abnormal(void)   //�����쳣����
{
 unsigned int i,j;
 unsigned int sum=0;

 if(USART1_RX_BUF<8)   // �쳣�����ֽ���С��8�ֽڲ�������
 {
    flag1=0;
 }
 else {
  for(i=0;i<=(USART1_RX_BUF-7);i++)
  {
   if(USART1_RX_BUF[i]==0x55)    // Ѱ�Ұ�ͷ
   {
      if(USART1_RX_BUF[i+7]==0xbb) // �жϰ�β
      {
           sum=(USART1_RX_BUF[i+2]+USART1_RX_BUF[i+3]+USART1_RX_BUF[i+4]+USART1_RX_BUF[i+5])%256;

             if(sum==USART1_RX_BUF[i+6])  // �ж����
               {
            for(j=0;j<8;j++)
      {
        USART1_RX_BUF[j]=USART1_RX_BUF[j+i];  // ���ݰ���
      }
     flag1=1;
            }
               else flag1=0;
         }
   }
     }

 }


}
