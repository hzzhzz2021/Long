package org.tensorflow.lite.examples.detection.global;

import java.util.HashMap;
import java.util.Map;


public class Global {
	public static float Xdiff;  //wya 定义目标框中心点与视场中心点的x偏差
	public static float Ydiff;  //wya 定义目标框中心点与视场中心点的y偏差

	public static int rectArea;
	public static boolean isHavePerson;
	public static boolean isTrack;
	public static boolean enableTrack = false;//默认关闭追踪
	public static boolean isPlayer = true;//默认不提示佩戴口罩


	public static char CAMSEL;  //wya 相机选择 0： 后置  1： 前置

	public static final int FRONT=1;
	public static final int BACK=0;

	public static boolean isShowSettingView = false;


	public static final String CRCNAME="crcName.txt";
	public static String COLOR="黄色";
	public static boolean DEBUG = false;
	public static final int MAXGEAR = 4;
	public static int line_xy=0;		//行进中坐标计步
	public static boolean fidFlag = false;//sfq  用于设置  RFID 读取的数据的标志位

	public static byte MBYTE=0;//从车状态
	public static char CRCH=1;//CRC 高八位
	public static short CRC[]=new short[4];//CRC传入数组


	public static Map<String,Integer> shape_num=new HashMap<String,Integer>();  
	public enum Turn_45{ // 45°转向
		right,left
	};
	public enum showType{   //立体显示 类型
		color,shape,distance,license,road,coordinate //颜色,形状,距离,车牌,路况,坐标
	}
	

	public synchronized static void setRectArea(int area){
		rectArea = area;
	}

}