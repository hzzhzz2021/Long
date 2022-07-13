/*
 * uart.c
 *
 * created: 2022/6/23
 *  author:
 */

#include "bsp.h"
#include "ns16550.h"
#include "uart.h"




char USART1_RX_BUF[RXD1_MAX_NUM];     //接收缓冲,最大200个字节.
unsigned char flag1;


void uartinit()
{
    unsigned int BaudRate=115200;
    ls1x_uart_init(devUART1,(void*)BaudRate);
    ls1x_uart_open(devUART1,NULL);
}



/***************************************************************
** 功能：     WiFi串口接收数据校验
** 参数：   无参数
** 作者：     wya
** 返回值：    无
****************************************************************/

void usart1_data(void)
{
 unsigned char sum=0;


 if(USART1_RX_BUF[7]==0xbb)  // 判断包尾
  {
  //主指令与三位副指令左求和校验
  //注意：在求和溢出时应该对和做256取余。
  /* sum=(USART1_RX_BUF[2]+USART1_RX_BUF[3]+USART1_RX_BUF[4]+USART1_RX_BUF[5])%256;
  if(sum==USART1_RX_BUF[6])
  {
      
  }
  else flag1=0; */
  
  flag1=1;
  }



}

void usart1_data_abnormal(void)   //数据异常处理
{
 unsigned int i,j;
 unsigned int sum=0;

 if(USART1_RX_BUF<8)   // 异常数据字节数小于8字节不做处理
 {
    flag1=0;
 }
 else {
  for(i=0;i<=(USART1_RX_BUF-7);i++)
  {
   if(USART1_RX_BUF[i]==0x55)    // 寻找包头
   {
      if(USART1_RX_BUF[i+7]==0xbb) // 判断包尾
      {
           //sum=(USART1_RX_BUF[i+2]+USART1_RX_BUF[i+3]+USART1_RX_BUF[i+4]+USART1_RX_BUF[i+5])%256;

             //if(sum==USART1_RX_BUF[i+6])  // 判断求和
               //{
        for(j=0;j<8;j++)
        {
          USART1_RX_BUF[j]=USART1_RX_BUF[j+i];  // 数据搬移
        }
        flag1=1;
           // }
             //  else flag1=0;
       }
   }
  }

 }


}
