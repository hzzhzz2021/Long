/*
 * UWB.h
 *
 * created: 2022/7/2
 *  author: 
 */

#ifndef _UWB_H
#define _UWB_H

void uartinit(); //uwb���ڳ�ʼ��
void uwbread();   //��ȡuwbʵʱ����
int navigation(int x1,int y1); //�����㣨x1��y1),�жϵ������ڳ��ķ�λ
void lookfor(int x1,int y1);//��������
#endif // _UWB_H

