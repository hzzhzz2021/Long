/*
 * pose.c
 *
 * created: 2022/7/3
 *  author: 
 */

#include "bsp.h"
#include "ls1x_i2c_bus.h"
#include "pose.h"
#include "pwm.h"
#include "go.h"

void angle0() //������ֻ̬0��
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//��ȡ��ǰ�Ƕ�
    speed(1); //�ٶȽ������
    while(angle==0) //һֱ��תֱ���Ƕ�Ϊ0
    {
        left();
    }
}

void angle90() //������ֻ̬90��
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//��ȡ��ǰ�Ƕ�
    speed(1); //�ٶȽ������
    while(angle==90) //һֱ��תֱ���Ƕ�Ϊ90
    {
        left();
    }
}

void angle180() //������ֻ̬180��
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//��ȡ��ǰ�Ƕ�
    speed(1); //�ٶȽ������
    while(angle==180) //һֱ��תֱ���Ƕ�Ϊ180
    {
        left();
    }
}
void angle270() //������ֻ̬270��
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//��ȡ��ǰ�Ƕ�
    speed(1); //�ٶȽ������
    while(angle==270) //һֱ��תֱ���Ƕ�Ϊ270
    {
        left();
    }
}
