/*
 * UWB.h
 *
 * created: 2022/7/2
 *  author: 
 */

#ifndef _UWB_H
#define _UWB_H

void uartinit(); //uwb串口初始化
void uwbread();   //读取uwb实时坐标
int navigation(int x1,int y1); //导航点（x1，y1),判断导航点在车的方位
void lookfor(int x1,int y1);//导航函数
#endif // _UWB_H

