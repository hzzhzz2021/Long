/*
 * uart.h
 *
 * created: 2022/6/23
 *  author:
 */

#ifndef _UART_H
#define _UART_H

#define RXD1_MAX_NUM 200

char USART1_RX_BUF[RXD1_MAX_NUM];     //接收缓冲,最大200个字节.
extern unsigned char flag1;


extern void usart1_data(void);
extern void usart1_data_abnormal(void);   //数据异常处理
extern void uartinit();//初始化串口

#endif // _UART_H
