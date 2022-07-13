/*
 * pose.h
 *
 * created: 2022/7/3
 *  author: 
 */

#ifndef _POSE_H
#define _POSE_H

extern unsigned char iicbuff[200];
extern unsigned int angle;

void angle0(); //调整姿态只0度
void angle90(); //调整姿态只90度
void angle180(); //调整姿态只180度
void angle270(); //调整姿态只270度


#endif // _POSE_H

