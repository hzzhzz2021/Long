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

char uwbdadt[200];//串口接收uwb数据

void uartinit() //uwb串口初始化
{
    unsigned int BaudRate=115200;
    ls1x_uart_init(devUART0,(void*)BaudRate);
    ls1x_uart_open(devUART0,NULL);
}

int *uwbread()   //读取uwb实时坐标
{

    int coordinate[2];
    ls1x_uart_read(devUART0,uwbdadt,200,NULL);
    coordinate[0]=uwbdadt[52]; //标签0x坐标值
    coordinate[1]=uwbdadt[53]; //标签0y坐标值
    return coordinate;
}
int navigation(int x1,int y1) //导航点（x1，y1),判断导航点在车的方位
{
    int *coordinate;
    int x0,y0;
    int key;

    coordinate=wbread();
    x0=coordinate[0];
    y0=coordinate[1];
    
    if(x1>x0)  //导航点在车右边
    {
        if(y1>y0)key=1;  //导航点在车右上方
        if(y1==y0)key=2;  //导航点在车正右方
        if(y1<y0)key=3;  //导航点在车右下方
    }
    if(x1==x0)
    {
        if(y1>y0)key=4; //导航点在车正前方
        if(y1==y0)key=5; //导航点和车重合
        if(y1<y0)key=6; //导航点在车正后方
    }
    if(x1<x0)  //导航点在车左边
    {
        if(y1>y0)key=7;  //导航点在车左上方
        if(y1==y0)key=8;  //导航点在车正左方
        if(y1<y0)key=9;  //导航点在车左下方
    }
    return key;
}

void lookfor(int x1,int y1)//导航函数
{
    int *p,x0,y0,key;
    int xtime,ytime;
    
    p=uwbread();
    x0=p[0];
    y0=p[1];  //读取当前车的坐标
    key=navigation(x1,y1);   //判断目标点的位置
    speed(50);   //设置速度为10mm/s
    switch(key)
    
    {
        case 1:
            xtime=(x1-x0)/10*1000; //时间转化为ms
            ytime=(y1-y0)/10*1000;
            angle0();   //调整姿态至0度
            delay_ms(100);  //等待姿态调整完毕
            advance();  //前进
            delay_ms(ytime);  //走完两点y值间距
            angle90();  //调整姿态
            delay_ms(100);   //等待
            advance(); // 前进
            delay_ms(xtime);   // 走完两点间x间距
            break;
        case 2:
            xtime=(x1-x0)/10*1000; //时间转化为ms
            angle90();  //调整姿态
            delay_ms(100);   //等待
            advance(); // 前进
            delay_ms(xtime);   // 走完两点间x间距
            break;
        case 3:
            xtime=(x1-x0)/10*1000; //时间转化为ms
            ytime=(y0-y1)/10*1000;
            angle180();   //调整姿态至180度
            delay_ms(100);  //等待姿态调整完毕
            advance();  //前进
            delay_ms(ytime);  //走完两点y值间距
            angle90();  //调整姿态
            delay_ms(100);   //等待
            advance(); // 前进
            delay_ms(xtime);   // 走完两点间x间距
            break;
        case 4:
            ytime=(y1-y0)/10*1000;//时间转化为ms
            angle0();   //调整姿态至0度
            delay_ms(100);  //等待姿态调整完毕
            advance();  //前进
            delay_ms(ytime);  //走完两点y值间距
            break;
        case 5:
            delay_ms(100);
            break;
        case 6:
            ytime=(y0-y1)/10*1000;//时间转化为ms
            angle180();   //调整姿态至180度
            delay_ms(100);  //等待姿态调整完毕
            advance();  //前进
            delay_ms(ytime);  //走完两点y值间距
            break;
        case 7:
            xtime=(x0-x1)/10*1000; //时间转化为ms
            ytime=(y1-y0)/10*1000;
            angle0();   //调整姿态至0度
            delay_ms(100);  //等待姿态调整完毕
            advance();  //前进
            delay_ms(ytime);  //走完两点y值间距
            angle270();  //调整姿态
            delay_ms(100);   //等待
            advance(); // 前进
            delay_ms(xtime);   // 走完两点间x间距
            break;
        case 8:
            xtime=(x0-x1)/10*1000; //时间转化为ms
            angle270();  //调整姿态
            delay_ms(100);   //等待
            advance(); // 前进
            delay_ms(xtime);   // 走完两点间x间距
            break;
        case 9:
            xtime=(x0-x1)/10*1000; //时间转化为ms
            ytime=(y0-y1)/10*1000;
            angle180();   //调整姿态至180度
            delay_ms(100);  //等待姿态调整完毕
            advance();  //前进
            delay_ms(ytime);  //走完两点y值间距
            angle270();  //调整姿态
            delay_ms(100);   //等待
            advance(); // 前进
            delay_ms(xtime);   // 走完两点间x间距
            break;
    }
}

