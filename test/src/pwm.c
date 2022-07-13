/*
 * pwm.c
 *
 * created: 2022/6/23
 *  author: 
 */
#include "bsp.h"
#include "ls1x_pwm.h"
#include "pwm.h"
void pwminit() //pwm��ʼ��
{
    pwm_cfg_t cfg;
    cfg.mode=PWM_CONTINUE_PULSE;
    cfg.cb=NULL;
    cfg.isr=NULL;
}

void speed(int data) //�ٶ�����1-10
{
    cfg.hi_ns=data*500;
    cfg.lo_ns=(10-data)*500;
}
