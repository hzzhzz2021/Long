/*
 * uart.h
 *
 * created: 2022/6/23
 *  author:
 */

#ifndef _UART_H
#define _UART_H

#define RXD1_MAX_NUM 200

char USART1_RX_BUF[RXD1_MAX_NUM];     //���ջ���,���200���ֽ�.
extern unsigned char flag1;


extern void usart1_data(void);
extern void usart1_data_abnormal(void);   //�����쳣����
extern void uartinit();//��ʼ������

#endif // _UART_H
