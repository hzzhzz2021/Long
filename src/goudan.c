/*
 * goudan.c
 *
 * created: 2022/6/23
 *  author: 
 */

#include "goudan.h"
#include "bsp.h"
#include "ls1b_gpio.h"

unsigned int key()
{
    unsigned int k1,k2,k3,swc;
    gpio_enable(y1,DIR_IN);
    gpio_enable(y2,DIR_IN);
    gpio_enable(y3,DIR_IN);
    
    k1=gpio_read(y1);
    k2=gpio_read(y2);
    k3=gpio_read(y3);
    if(k1==0&&k2==1&&k3==1)
    swc=1;
    if(k1==1&&k2==0&&k3==0)
    swc=2;
    if(k1==0&&k2==1&&k3==0)
    swc=3;
    if(k1==0&&k2==0&&k3==1)
    swc=4;
    if(k1==0&&k2==0&&k3==0)
    swc=5;
    return swc;
}
key2()
{

switch(key())
    {
        case 1:
            advance();
            break;
        case 2:
            back();
            break;
        case 3:
            left();
            break;
        case 4:
            right();
            break;
        case 5:
            stop();
            break;
    }
}
