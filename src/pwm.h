/*
 * pwm.h
 *
 * created: 2022/6/23
 *  author: 
 */

#ifndef _PWM_H
#define _PWM_H
#include "ls1x_pwm.h"
pwm_cfg_t cfg;

void pwminit();
void speed(int data);

#endif // _PWM_H

