/*
 * go.c
 *
 * created: 2022/6/16
 *  author: 
 */
#include "bsp.h"
#include "go.h"
#include "ls1b_gpio.h"

void advance() //ǰ������
{
    gpio_enable(LIEF_PWMA,DIR_OUT);
    gpio_enable(LIEF_DIRI,DIR_OUT);

    gpio_enable(RIGHT_PWMA,DIR_OUT);
    gpio_enable(RIGHT_DIRI,DIR_OUT);
    

    gpio_write(LIEF_PWMA,0);
    gpio_write(LIEF_DIRI,1);
    gpio_write(RIGHT_PWMA,0);
    gpio_write(RIGHT_DIRI,1);

}

void stop() //ֹͣ����
{
    gpio_enable(LIEF_PWMA,DIR_OUT);
    gpio_enable(LIEF_DIRI,DIR_OUT);
    
    gpio_enable(RIGHT_PWMA,DIR_OUT);
    gpio_enable(RIGHT_DIRI,DIR_OUT);
    

   

    gpio_write(LIEF_PWMA,0);
    gpio_write(LIEF_DIRI,0);
    gpio_write(RIGHT_PWMA,0);
    gpio_write(RIGHT_DIRI,0);

}
void back() //���˺���
{
    gpio_enable(LIEF_PWMA,DIR_OUT);
    gpio_enable(LIEF_DIRI,DIR_OUT);
    
    gpio_enable(RIGHT_PWMA,DIR_OUT);
    gpio_enable(RIGHT_DIRI,DIR_OUT);
   

    gpio_write(LIEF_PWMA,1);
    gpio_write(LIEF_DIRI,0);
    gpio_write(RIGHT_PWMA,1);
    gpio_write(RIGHT_DIRI,0);

}

void left() //��ת
{
    gpio_enable(LIEF_PWMA,DIR_OUT);
    gpio_enable(LIEF_DIRI,DIR_OUT);
    
    gpio_enable(RIGHT_PWMA,DIR_OUT);
    gpio_enable(RIGHT_DIRI,DIR_OUT);
   

    gpio_write(LIEF_PWMA,1);
    gpio_write(LIEF_DIRI,0);
    gpio_write(RIGHT_PWMA,0);
    gpio_write(RIGHT_DIRI,1);
}

void right()  //��ת
{
    gpio_enable(LIEF_PWMA,DIR_OUT);
    gpio_enable(LIEF_DIRI,DIR_OUT);
    
    gpio_enable(RIGHT_PWMA,DIR_OUT);
    gpio_enable(RIGHT_DIRI,DIR_OUT);
    

    gpio_write(LIEF_PWMA,0);
    gpio_write(LIEF_DIRI,1);
    gpio_write(RIGHT_PWMA,1);
    gpio_write(RIGHT_DIRI,0);
}
