#
# Auto-Generated file. Do not edit!
#

# Add inputs and outputs from these tool invocations to the build variables
C_SRCS += \
../src/go.c \
../src/pwm.c \
../src/uart.c \
../src/goudan.c \
../src/UWB.c \
../src/pose.c

OBJS += \
./src/go.o \
./src/pwm.o \
./src/uart.o \
./src/goudan.o \
./src/UWB.o \
./src/pose.o

C_DEPS += \
./src/go.d \
./src/pwm.d \
./src/uart.d \
./src/goudan.d \
./src/UWB.d \
./src/pose.d

# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: SDE Lite C Compiler'
	D:/LoongIDE/mips-2015.05/bin/mips-sde-elf-gcc.exe -mips32 -G0 -EL -msoft-float -DLS1B -DOS_NONE  -O0 -g -Wall -c -fmessage-length=0 -pipe -I"../" -I"../include" -I"../core/include" -I"../core/mips" -I"../ls1x-drv/include" -I"../src" -I"D:/LoongIDE/wj/test/ls1x-drv/pwm" -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

