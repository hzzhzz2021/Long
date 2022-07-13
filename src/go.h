/*
 * go.h
 *
 * created: 2022/6/16
 *  author: 
 */

#ifndef _GO_H
#define _GO_H
#define LIEF_PWMA 4 //LCD CLK口 前驱输入引脚A
#define LIEF_DIRI 5 //LCD VSYNC口 前驱输入引脚B
#define RIGHT_PWMA 21 //LCD R5口 后驱输入引脚A
#define RIGHT_DIRI 18 //LCD G7口 后驱输入引脚B


#define DIR_IN      1
#define DIR_OUT     0

void advance(); //前进函数
void stop(); //停止函数
void back(); //后退函数
void left() ;//左转
void right();  //右转

#endif // _GO_H

