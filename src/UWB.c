/*
 * UWB.c
 *
 * created: 2022/7/2
 *  author: 
 */

#include "bsp.h"
#include "ns16550.h"
#include "uart.h"
#include "pwm.h"
#include "pose.h"

char uwbdadt[200];//���ڽ���uwb����

void uartinit() //uwb���ڳ�ʼ��
{
    unsigned int BaudRate=115200;
    ls1x_uart_init(devUART0,(void*)BaudRate);
    ls1x_uart_open(devUART0,NULL);
}

int *uwbread()   //��ȡuwbʵʱ����
{

    int coordinate[2];
    ls1x_uart_read(devUART0,uwbdadt,200,NULL);
    coordinate[0]=uwbdadt[52]; //��ǩ0x����ֵ
    coordinate[1]=uwbdadt[53]; //��ǩ0y����ֵ
    return coordinate;
}
int navigation(int x1,int y1) //�����㣨x1��y1),�жϵ������ڳ��ķ�λ
{
    int *coordinate;
    int x0,y0;
    int key;

    coordinate=wbread();
    x0=coordinate[0];
    y0=coordinate[1];
    
    if(x1>x0)  //�������ڳ��ұ�
    {
        if(y1>y0)key=1;  //�������ڳ����Ϸ�
        if(y1==y0)key=2;  //�������ڳ����ҷ�
        if(y1<y0)key=3;  //�������ڳ����·�
    }
    if(x1==x0)
    {
        if(y1>y0)key=4; //�������ڳ���ǰ��
        if(y1==y0)key=5; //������ͳ��غ�
        if(y1<y0)key=6; //�������ڳ�����
    }
    if(x1<x0)  //�������ڳ����
    {
        if(y1>y0)key=7;  //�������ڳ����Ϸ�
        if(y1==y0)key=8;  //�������ڳ�����
        if(y1<y0)key=9;  //�������ڳ����·�
    }
    return key;
}

void lookfor(int x1,int y1)//��������
{
    int *p,x0,y0,key;
    int xtime,ytime;
    
    p=uwbread();
    x0=p[0];
    y0=p[1];  //��ȡ��ǰ��������
    key=navigation(x1,y1);   //�ж�Ŀ����λ��
    speed(50);   //�����ٶ�Ϊ10mm/s
    switch(key)
    
    {
        case 1:
            xtime=(x1-x0)/10*1000; //ʱ��ת��Ϊms
            ytime=(y1-y0)/10*1000;
            angle0();   //������̬��0��
            delay_ms(100);  //�ȴ���̬�������
            advance();  //ǰ��
            delay_ms(ytime);  //��������yֵ���
            angle90();  //������̬
            delay_ms(100);   //�ȴ�
            advance(); // ǰ��
            delay_ms(xtime);   // ���������x���
            break;
        case 2:
            xtime=(x1-x0)/10*1000; //ʱ��ת��Ϊms
            angle90();  //������̬
            delay_ms(100);   //�ȴ�
            advance(); // ǰ��
            delay_ms(xtime);   // ���������x���
            break;
        case 3:
            xtime=(x1-x0)/10*1000; //ʱ��ת��Ϊms
            ytime=(y0-y1)/10*1000;
            angle180();   //������̬��180��
            delay_ms(100);  //�ȴ���̬�������
            advance();  //ǰ��
            delay_ms(ytime);  //��������yֵ���
            angle90();  //������̬
            delay_ms(100);   //�ȴ�
            advance(); // ǰ��
            delay_ms(xtime);   // ���������x���
            break;
        case 4:
            ytime=(y1-y0)/10*1000;//ʱ��ת��Ϊms
            angle0();   //������̬��0��
            delay_ms(100);  //�ȴ���̬�������
            advance();  //ǰ��
            delay_ms(ytime);  //��������yֵ���
            break;
        case 5:
            delay_ms(100);
            break;
        case 6:
            ytime=(y0-y1)/10*1000;//ʱ��ת��Ϊms
            angle180();   //������̬��180��
            delay_ms(100);  //�ȴ���̬�������
            advance();  //ǰ��
            delay_ms(ytime);  //��������yֵ���
            break;
        case 7:
            xtime=(x0-x1)/10*1000; //ʱ��ת��Ϊms
            ytime=(y1-y0)/10*1000;
            angle0();   //������̬��0��
            delay_ms(100);  //�ȴ���̬�������
            advance();  //ǰ��
            delay_ms(ytime);  //��������yֵ���
            angle270();  //������̬
            delay_ms(100);   //�ȴ�
            advance(); // ǰ��
            delay_ms(xtime);   // ���������x���
            break;
        case 8:
            xtime=(x0-x1)/10*1000; //ʱ��ת��Ϊms
            angle270();  //������̬
            delay_ms(100);   //�ȴ�
            advance(); // ǰ��
            delay_ms(xtime);   // ���������x���
            break;
        case 9:
            xtime=(x0-x1)/10*1000; //ʱ��ת��Ϊms
            ytime=(y0-y1)/10*1000;
            angle180();   //������̬��180��
            delay_ms(100);  //�ȴ���̬�������
            advance();  //ǰ��
            delay_ms(ytime);  //��������yֵ���
            angle270();  //������̬
            delay_ms(100);   //�ȴ�
            advance(); // ǰ��
            delay_ms(xtime);   // ���������x���
            break;
    }
}

