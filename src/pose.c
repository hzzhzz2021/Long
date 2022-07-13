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

void angle0() //调整姿态只0度
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//读取当前角度
    speed(1); //速度降到最低
    while(angle==0) //一直左转直至角度为0
    {
        left();
    }
}

void angle90() //调整姿态只90度
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//读取当前角度
    speed(1); //速度降到最低
    while(angle==90) //一直左转直至角度为90
    {
        left();
    }
}

void angle180() //调整姿态只180度
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//读取当前角度
    speed(1); //速度降到最低
    while(angle==180) //一直左转直至角度为180
    {
        left();
    }
}
void angle270() //调整姿态只270度
{

    ls1x_i2c_initialize(busI2C0);
    ls1x_i2c_read_bytes(busI2C0,iicbuff,200);
    angle=iicbuff[18];//读取当前角度
    speed(1); //速度降到最低
    while(angle==270) //一直左转直至角度为270
    {
        left();
    }
}
