#
# Auto-Generated file. Do not edit!
#

# Add inputs and outputs from these tool invocations to the build variables
C_SRCS += \
../ls1x-drv/pwm/ls1x_pwm.c

OBJS += \
./ls1x-drv/pwm/ls1x_pwm.o

C_DEPS += \
./ls1x-drv/pwm/ls1x_pwm.d

# Each subdirectory must supply rules for building sources it contributes
ls1x-drv/pwm/%.o: ../ls1x-drv/pwm/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: SDE Lite C Compiler'
	D:/LoongIDE/mips-2015.05/bin/mips-sde-elf-gcc.exe -mips32 -G0 -EL -msoft-float -DLS1B -DOS_NONE  -O0 -g -Wall -c -fmessage-length=0 -pipe -I"../" -I"../include" -I"../core/include" -I"../core/mips" -I"../ls1x-drv/include" -I"../src" -I"D:/LoongIDE/wj/test/ls1x-drv/pwm" -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

