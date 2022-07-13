package org.tensorflow.lite.examples.detection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.tensorflow.lite.examples.detection.global.Global;

import javax.microedition.khronos.opengles.GL;


public class Client {
	//类的重构
	private Handler quanHandler;//全自动处理
	private Context context;
	private MediaPlayer startTrack,stopTrack;
	private MediaPlayer went,backed,lefted,righted,stoped;
	private MediaPlayer openeded,closetded;
	private MediaPlayer ICameHere,pleaseShowHealthCode;
	private MediaPlayer greenCode,yellowCode;
	private boolean isPlay = false;

	public Client(Context context, Handler qHandler) {
		this.quanHandler = qHandler;
		this.context = context;
		startTrack = MediaPlayer.create(context,R.raw.starttracked );
		stopTrack = MediaPlayer.create(context, R.raw.stoptracked);
		went = MediaPlayer.create(context,R.raw.went );
		backed = MediaPlayer.create(context, R.raw.backed);
		lefted = MediaPlayer.create(context, R.raw.lefted);
		righted = MediaPlayer.create(context, R.raw.righted);
		stoped  = MediaPlayer.create(context, R.raw.stoped);
		openeded = MediaPlayer.create(context, R.raw.opentded);
		closetded = MediaPlayer.create(context, R.raw.closetded);
		ICameHere = MediaPlayer.create(context, R.raw.icamehere);
		pleaseShowHealthCode = MediaPlayer.create(context,R.raw.pleaseshowhealthcode);
		greenCode = MediaPlayer.create(context,R.raw.greencode);
		yellowCode = MediaPlayer.create(context,R.raw.yellowcode);
	}
	
	private static int portCar = 60000;//小车wifi端口号
	
	/***
	 * TCP连接，并时时接受发送来的数据，500毫秒后发送时主界面UI更新
	 */
	// 数据输出和输入流
	private  DataInputStream bInputStream;
	private  DataOutputStream bOutputStream;
	//插口,创建socket
	private static Socket socket;
	//时间定时器

	public  void connect(final Handler rHandler, String IP) {
		try {
			socket = new Socket(IP,portCar);

			Log.i("wya","In connecting@connect()");
			bInputStream = new DataInputStream(socket.getInputStream());
			bOutputStream = new DataOutputStream(socket.getOutputStream());
			reThread.start();//wya 开启接收数据的子线程
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//健康码语音播报以及显示结果
	private AlertDialog alertDialog = null;
	private AlertDialog.Builder builder = null;
	private Handler dialog = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1){
				if (builder == null && alertDialog == null){
					builder= new AlertDialog.Builder(context)
							.setTitle("结果")
							.setMessage("您的健康码为绿码");
					alertDialog = builder.create();
					alertDialog.show();
				}
				alertDialog.show();

			}else if (msg.what == 2){
				alertDialog.dismiss();
			}
		}
	};
	/***
	 * 接收数据线程
	 */
	//小车状态信息的高5位，用来获取小车执行到了第几步。（小车的低3位是用来记录小车的运行状态）
	private static int getStep = 0;//记录小车运行到第几步
	private static byte[] rbyte = new byte[8];
	private static byte prev = 0x00;
	private boolean initProgram = true;
	private  Thread reThread = new Thread(new Runnable() {
		@Override
		public void run() {
			if(socket==null) Log.i("receive","socket ==nul");
			if(socket.isClosed()) Log.i("receive","socket is Closed");
			while (socket != null && !socket.isClosed()) {
				try {
					bInputStream.read(rbyte);
					if (rbyte[0] == 0x55){
						Log.d("receive", "run: " + Byte.toString(rbyte[2]));
						if (initProgram){
							prev = rbyte[2];
							initProgram = false;
						}
						if (prev != rbyte[2]){
							prev = rbyte[2];

							switch (rbyte[2]) {
								case 1: {//启动追踪
									while (isPlay){}
									isPlay = true;
									startTrack.start();

									delayPlay(startTrack);

									Global.enableTrack = true;
									Global.isPlayer = false;
									break;
								}
								case 2: {//停止追踪
									Global.enableTrack = false;
									Global.isPlayer = true;

									while (isPlay){}
									isPlay = true;
									stopTrack.start();
									delayPlay(stopTrack);

									break;
								}

								case 3:{//打开测温
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}
									while (isPlay){}
									isPlay = true;
									openeded.start();
									delayPlay(openeded);

									break;
								}
								case 4:{//关闭测温
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}
									while (isPlay){}
									isPlay = true;
									closetded.start();
									delayPlay(closetded);

									while (isPlay){}
									isPlay = true;
									pleaseShowHealthCode.start();
									delayPlay(pleaseShowHealthCode);

									new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												Thread.sleep(5000);
												dialog.sendEmptyMessage(1);

												while (isPlay){}
												isPlay = true;
												greenCode.start();
												delayPlay(greenCode);

												Thread.sleep(3000);
												dialog.sendEmptyMessage(2);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}
									}).start();
									break;
								}
								case 5:{//已停车
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}
									while (isPlay){}
									isPlay = true;
									stoped.start();
									delayPlay(stoped);


									break;
								}
								case 6:{//前进
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}
									while (isPlay){}
									isPlay = true;
									went.start();
									delayPlay(went);


									break;
								}
								case 7:{//后退
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}
									while (isPlay){}
									isPlay = true;
									backed.start();
									delayPlay(backed);


									break;
								}
								case 8:{//左转
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}
									while (isPlay){}
									isPlay = true;
									lefted.start();
									delayPlay(lefted);


									break;
								}
								case 9:{//右转
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}

									while (isPlay){}
									isPlay = true;
									righted.start();
									delayPlay(righted);


									break;
								}
								case 10:{//我再
									if (Global.enableTrack || Global.isPlayer){
										Global.enableTrack = false;
										Global.isPlayer = true;
									}

									while (isPlay){}
									isPlay = true;
									ICameHere.start();
									delayPlay(ICameHere);

									break;
								}
							}
						}
					}


				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	});

	/**
	 * 等待语音播放结束然后改变isPlayer的值，以让后面的语音开始播放
	 * @param player
	 */
	private void delayPlay(MediaPlayer player){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (player.isPlaying()){}
				isPlay = false;
			}
		}).start();
	}

	private void showHex(byte [] bytes){
		StringBuilder builder = new StringBuilder();
		System.out.print("receive");
		for (byte b : bytes){
			builder.append(Byte.toString(b));
			System.out.printf("%x",b);
		}
		System.out.println();
		Log.d("receive", "showHex: " + builder);
	}

	/***
	 *发送命令
	*/
	public byte TYPE = (byte) 0xAA;
	public short MAJOR = 0x00;
	public short FIRST = 0x00;
	public short SECOND = 0x00;
	public short THRID = 0x00;
	public short CHECKSUM = 0x00;
	public void send(String Content) {
			CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THRID) % 256);
			// 发送数据字节数组
			byte[] sbyte = { 0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST,
				(byte) SECOND, (byte) THRID, (byte) CHECKSUM, (byte) 0xBB };
		    final byte[] ssbyte = { 0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST,
				(byte) SECOND, (byte) THRID, (byte) CHECKSUM, (byte) 0xBB };

			XcApplication.executorServicetor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						if (socket != null && !socket.isClosed()) {
							bOutputStream.write(ssbyte, 0, ssbyte.length);
							bOutputStream.flush();
							Log.i("wya",Content);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
	}

	// wya 发送 任何自定义的 key-value对
	public void sendVal(int key,int value) {
		MAJOR = 0x30;
		FIRST = (byte) (key & 0xFF);
		SECOND = (byte) (value & 0xff);
		THRID = (byte) (value >> 8);
		send("go");
	}

	/**
	 * 运动状态形式1
	 */
	// 停车
	public void stop() {
		MAJOR = 0x01;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send("stop");
	}
	// 前进
	public void go(int sp_n, int en_n) {
		MAJOR = 0x02;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("go");
	}
	// 后退
	public void back(int sp_n, int en_n) {
		MAJOR = 0x03;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("back");


	}
	// 左转
	public void left(int sp_n, int en_n) {
		MAJOR = 0x04;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("left");
	}
	// 右转
	public void right(int sp_n, int en_n) {
		MAJOR = 0x05;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("right");
	}
	
	/**
	 * 运动状态形式2
	 */
	// 前进
	public void go2(int sp_n, int en_n) {

		MAJOR = 0x22;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("go2");
		
	}
	
	// 后退
	public void back2(int sp_n, int en_n) {

		MAJOR = 0x23;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("back2");
	}

	// 左转
	public void left2(int sp_n, int en_n) {

		MAJOR = 0x24;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("left2");

	}

	// 右转
	public void right2(int sp_n, int en_n) {

		MAJOR = 0x25;
		FIRST = (byte) (sp_n & 0xFF);
		SECOND = (byte) (en_n & 0xff);
		THRID = (byte) (en_n >> 8);
		send("right2");
	}
//26开27关
	public void openSpeech(){
		MAJOR = 0x26;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send("openSpeech");
	}

	public void closeSpeech(){
		MAJOR = 0x27;
		FIRST = 0x00;
		SECOND = 0x00;
		THRID = 0x00;
		send("closeSpeech");
	}
//	public int mark = -50;//负数就是不让进小车进入自动执行，给mark赋值执行到任务的下一步设置。（mark是返回数据的低3位）
	// 沉睡
	public void yanchi(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
