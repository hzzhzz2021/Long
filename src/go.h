/*
 * go.h
 *
 * created: 2022/6/16
 *  author: 
 */

#ifndef _GO_H
#define _GO_H
#define LIEF_PWMA 4 //LCD CLK�� ǰ����������A
#define LIEF_DIRI 5 //LCD VSYNC�� ǰ����������B
#define RIGHT_PWMA 21 //LCD R5�� ������������A
#define RIGHT_DIRI 18 //LCD G7�� ������������B


#define DIR_IN      1
#define DIR_OUT     0

void advance(); //ǰ������
void stop(); //ֹͣ����
void back(); //���˺���
void left() ;//��ת
void right();  //��ת

#endif // _GO_H

