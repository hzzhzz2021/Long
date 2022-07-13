package org.tensorflow.lite.examples.detection.global;

public class SettingParam {
    public static int leftSpeedThreshold = -190; //左转速度阈值
    public static int rightSpeedThreshold = 190; //右转速度阈值
    public static int goSpeedThreshold = 2000;
    public static int minLeftSpeed = 65;//最大左转速度
    public static int maxLeftSpeed = 55;//最小左转速度
    public static int minRightSpeed = 65;//最大右转速度
    public static int maxRightSpeed = 55;//最小右转速度
    public static int maxGoSpeed = 80;//最大前进速度
    public static int minGoSpeed = 60;//最小前进速度
    public static int backSpeed = 70;//后退速度
    //值越大那么会一直往前，直到值大于所规定的的值，然后才会停下
    public static int maxGoFaceArea = 3000;//最大人脸面积
    public static int minBackFaceArea = 4000;//最小人脸面积
    public static int minFaceDifference = 0;


}
