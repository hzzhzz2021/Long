/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.text.format.Formatter;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.global.Global;
import org.tensorflow.lite.examples.detection.global.SettingParam;

public abstract class CameraActivity extends AppCompatActivity
        implements OnImageAvailableListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener{
    private static final Logger LOGGER = new Logger();

    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    private boolean debug = false;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    private LinearLayout bottomSheetLayout;
    private LinearLayout gestureLayout;
    private BottomSheetBehavior<LinearLayout> sheetBehavior;

    protected TextView frameValueTextView, cropValueTextView, inferenceTimeTextView;
    protected ImageView bottomSheetArrowImageView;
    private ImageView plusImageView, minusImageView;
    private SwitchCompat apiSwitchCompat;
    private TextView threadsTextView;

    private Button btn,settings;

    private WifiManager wifiManager;// WiFi管理器
    private DhcpInfo dhcpInfo;// 服务器管理器
    public static Client client;// wya socket类，新建类Client实现TCP通信
    public static String IPCar = "";// wya 小车wif_IP

    private EditText rightSpeedThreshold,leftSpeedThreshold,goSpeedThreshold,maxLeftSpeed,minLeftSpeed,maxRightSpeed,minRightSpeed,maxGoSpeed,minGoSpeed,backSpeed;
    private EditText maxGoFaceArea,minBackFaceArea,minFaceDifference;
    private CharSequence original;
    private List<View> EditTextViews;
    private View settingView;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LOGGER.d("onCreate " + this);
        super.onCreate(null);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tfe_od_activity_camera);
        client = new Client(CameraActivity.this, chandler);//wya cHandler是传递给client的handler
        getIP_wifi();//wya 拿到IP地址
        socketThread.start();  //wya 开启socket连接线程

        if (!hasPermission()){
            requestPermission();
            initPermission();
        }

        setFragment(0);

        threadsTextView = findViewById(R.id.threads);
        plusImageView = findViewById(R.id.plus);
        minusImageView = findViewById(R.id.minus);
        apiSwitchCompat = findViewById(R.id.api_info_switch);
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        gestureLayout = findViewById(R.id.gesture_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

        btn = findViewById(R.id.qiehuan);

        //前后置切换
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn.getText().toString() == "后置") {
                    setFragment(1);
                    Global.CAMSEL = 1;  //wya 切换到前置摄像头
                }
                if (btn.getText().toString() == "前置") {
                    setFragment(0);
                    Global.CAMSEL = 0;  //wya 切换到后置摄像头
                }
            }
        });

        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        //                int width = bottomSheetLayout.getMeasuredWidth();
                        int height = gestureLayout.getMeasuredHeight();

                        sheetBehavior.setPeekHeight(height);
                    }
                });
        sheetBehavior.setHideable(false);

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED: {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                                break;
                            case BottomSheetBehavior.STATE_SETTLING:
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    }
                });

        frameValueTextView = findViewById(R.id.frame_info);
        cropValueTextView = findViewById(R.id.crop_info);
        inferenceTimeTextView = findViewById(R.id.inference_info);

        apiSwitchCompat.setOnCheckedChangeListener(this);

        plusImageView.setOnClickListener(this);
        minusImageView.setOnClickListener(this);

        settingView = LayoutInflater.from(this).inflate(R.layout.settinglayout,null);
        EditTextViews = new ArrayList<>();
        openSettingFile();
        settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.isShowSettingView = true;
                if (builder == null && alertDialog == null){
                    builder = new AlertDialog.Builder(CameraActivity.this);
                    alertDialog = builder.setTitle("设置").setView(settingView).setNegativeButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveSettingParamVal();
                            Global.isShowSettingView = false;
                        }
                    }).create();
                    initPopUpSetting();
                }
                mhandler.sendEmptyMessage(1);
                alertDialog.show();
            }
        });
    }

    private void openSettingFile(){
        File file = getFilesDir().getAbsoluteFile();
        File config = new File(file.getAbsolutePath() + "/setting.txt");
        if (!config.exists()){
            try {
                config.createNewFile();
                PrintWriter writer = new PrintWriter(config);
                writer.println("leftSpeedThreshold=" + SettingParam.leftSpeedThreshold);
                writer.println("rightSpeedThreshold=" + SettingParam.rightSpeedThreshold);
                writer.println("goSpeedThreshold=" + SettingParam.goSpeedThreshold);
                writer.println("maxLeftSpeed=" + SettingParam.maxLeftSpeed);
                writer.println("minLeftSpeed=" + SettingParam.minLeftSpeed);
                writer.println("minRightSpeed=" + SettingParam.minRightSpeed);
                writer.println("maxRightSpeed=" + SettingParam.maxRightSpeed);
                writer.println("maxGoSpeed=" + SettingParam.maxGoSpeed);
                writer.println("minGoSpeed=" + SettingParam.minGoSpeed);
                writer.println("backSpeed=" + SettingParam.backSpeed);
                writer.println("maxGoFaceArea=" + SettingParam.maxGoFaceArea);
                writer.println("minBackFaceArea=" + SettingParam.minBackFaceArea);
                writer.println("minFaceDifference=" + SettingParam.minFaceDifference);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(config),"UTF-8"));
            String s;
            while ((s = reader.readLine()) != null){
                String []ss = s.split("\\=");
                System.out.println(s);
                switch (ss[0]){
                    case "leftSpeedThreshold":      {SettingParam.leftSpeedThreshold = Integer.parseInt(ss[1].trim());break;}
                    case "rightSpeedThreshold":     {SettingParam.rightSpeedThreshold = Integer.parseInt(ss[1].trim());break;}
                    case "goSpeedThreshold":        {SettingParam.goSpeedThreshold = Integer.parseInt(ss[1].trim());break;}
                    case "maxLeftSpeed":            {SettingParam.maxLeftSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "minLeftSpeed":            {SettingParam.minLeftSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "minRightSpeed":           {SettingParam.minRightSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "maxRightSpeed":           {SettingParam.maxRightSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "maxGoSpeed":              {SettingParam.maxGoSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "minGoSpeed":              {SettingParam.minGoSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "backSpeed":               {SettingParam.backSpeed = Integer.parseInt(ss[1].trim());break;}
                    case "maxGoFaceArea":           {SettingParam.maxGoFaceArea = Integer.parseInt(ss[1].trim());break;}
                    case "minBackFaceArea":         {SettingParam.minBackFaceArea = Integer.parseInt(ss[1].trim());break;}
                    case "minFaceDifference":       {SettingParam.minFaceDifference = Integer.parseInt(ss[1].trim());break;}
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPopUpSetting(){
        EditTextViews.add(rightSpeedThreshold = (EditText) settingView.findViewById(R.id.rightSpeedThreshold));
        EditTextViews.add(leftSpeedThreshold = (EditText) settingView.findViewById(R.id.leftSpeedThreshold));
        EditTextViews.add(goSpeedThreshold = (EditText) settingView.findViewById(R.id.goSpeedThreshold));
        EditTextViews.add(maxLeftSpeed = (EditText) settingView.findViewById(R.id.maxLeftSpeed));
        EditTextViews.add(minLeftSpeed = (EditText) settingView.findViewById(R.id.minLeftSpeed));
        EditTextViews.add(maxRightSpeed = (EditText) settingView.findViewById(R.id.maxRightSpeed));
        EditTextViews.add(minRightSpeed = (EditText) settingView.findViewById(R.id.minRightSpeed));
        EditTextViews.add(maxGoSpeed = (EditText) settingView.findViewById(R.id.maxGoSpeed));
        EditTextViews.add(minGoSpeed = (EditText) settingView.findViewById(R.id.minGoSpeed));
        EditTextViews.add(backSpeed = (EditText) settingView.findViewById(R.id.backSpeed));
        EditTextViews.add(maxGoFaceArea = (EditText) settingView.findViewById(R.id.maxGoArea));
        EditTextViews.add(minBackFaceArea = (EditText) settingView.findViewById(R.id.minBackArea));
        EditTextViews.add(minFaceDifference = (EditText) settingView.findViewById(R.id.minFaceDifference));

        //发送消息通知消息队列去设置参数值
        mhandler.sendEmptyMessage(1);

    }
    //保存设置
    private void saveSettingParamVal() {
        try {
            OutputStream out = openFileOutput("setting.txt", Context.MODE_PRIVATE);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            SettingParam.leftSpeedThreshold = Integer.parseInt(leftSpeedThreshold.getText().toString());
            SettingParam.rightSpeedThreshold = Integer.parseInt(rightSpeedThreshold.getText().toString());
            SettingParam.goSpeedThreshold = Integer.parseInt(goSpeedThreshold.getText().toString());
            SettingParam.maxLeftSpeed = Integer.parseInt(maxLeftSpeed.getText().toString());
            SettingParam.minLeftSpeed = Integer.parseInt(minLeftSpeed.getText().toString());
            SettingParam.minRightSpeed = Integer.parseInt(minRightSpeed.getText().toString());
            SettingParam.maxRightSpeed = Integer.parseInt(maxRightSpeed.getText().toString());
            SettingParam.maxGoSpeed = Integer.parseInt(maxGoSpeed.getText().toString());
            SettingParam.minGoSpeed = Integer.parseInt(minGoSpeed.getText().toString());
            SettingParam.backSpeed = Integer.parseInt(backSpeed.getText().toString());
            SettingParam.maxGoFaceArea = Integer.parseInt(maxGoFaceArea.getText().toString());
            SettingParam.minBackFaceArea = Integer.parseInt(minBackFaceArea.getText().toString());
            SettingParam.minFaceDifference = Integer.parseInt(minFaceDifference.getText().toString());
            bw.write("leftSpeedThreshold=" + SettingParam.leftSpeedThreshold + "\n");
            bw.write("rightSpeedThreshold=" + SettingParam.rightSpeedThreshold + "\n");
            bw.write("goSpeedThreshold=" + SettingParam.goSpeedThreshold + "\n");
            bw.write("maxLeftSpeed=" + SettingParam.maxLeftSpeed + "\n");
            bw.write("minLeftSpeed=" + SettingParam.minLeftSpeed + "\n");
            bw.write("minRightSpeed=" + SettingParam.minRightSpeed + "\n");
            bw.write("maxRightSpeed=" + SettingParam.maxRightSpeed + "\n");
            bw.write("maxGoSpeed=" + SettingParam.maxGoSpeed + "\n");
            bw.write("minGoSpeed=" + SettingParam.minGoSpeed + "\n");
            bw.write("backSpeed=" + SettingParam.backSpeed + "\n");
            bw.write("maxGoFaceArea=" + SettingParam.maxGoFaceArea + "\n");
            bw.write("minBackFaceArea=" + SettingParam.minBackFaceArea + "\n");
            bw.write("minFaceDifference=" + SettingParam.minFaceDifference + "\n");
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //处理设置界面的ui
    private Handler mhandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                rightSpeedThreshold.setText(String.valueOf(SettingParam.rightSpeedThreshold));
                leftSpeedThreshold.setText(String.valueOf(SettingParam.leftSpeedThreshold));
                goSpeedThreshold.setText(String.valueOf(SettingParam.goSpeedThreshold));
                maxLeftSpeed.setText(String.valueOf(SettingParam.maxLeftSpeed));
                minLeftSpeed.setText(String.valueOf(SettingParam.minLeftSpeed));
                maxRightSpeed.setText(String.valueOf(SettingParam.maxRightSpeed));
                minRightSpeed.setText(String.valueOf(SettingParam.minRightSpeed));
                maxGoSpeed.setText(String.valueOf(SettingParam.maxGoSpeed));
                minGoSpeed.setText(String.valueOf(SettingParam.minGoSpeed));
                backSpeed.setText(String.valueOf(SettingParam.backSpeed));
                maxGoFaceArea.setText(String.valueOf(SettingParam.maxGoFaceArea));
                minBackFaceArea.setText(String.valueOf(SettingParam.minBackFaceArea));
                minFaceDifference.setText(String.valueOf(SettingParam.minFaceDifference));
            }else if (msg.what == 2){
                EditText e = (EditText) msg.obj;
                e.setText(original);
            }
        }
    };
    //拿到小车IP地址
    void getIP_wifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        dhcpInfo = wifiManager.getDhcpInfo();
        int ipaddress = dhcpInfo.gateway;
        IPCar = Formatter.formatIpAddress(ipaddress);
        Log.i("wya", "IPCar: " + IPCar);

    }

    //wya 开启socket连接及接收线程
    private Thread socketThread = new Thread(new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            client.connect(rHandler, IPCar);//wya 调用socket连接函数，并开启接收线程
            Log.i("wya", "IPCar Connected @Socket Thread!");
        }
    });

    //wya 为接收线程准备的handler接口，供其操作UI
    private byte[] mByte = new byte[11];
    Handler rHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                mByte = (byte[]) msg.obj;
                if (mByte[0] == 0x55) {

                    if (mByte[1] == (byte) 0xaa) {
                        // 显示数据

                    }
                }
            }
        }

        ;
    };

    /**
     * 为client提供的handler接口,供其在内部操作UI
     * 实现消息处理方法
     */
    private Handler chandler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 11) {

            }

        }

        ;
    };
    //摄像头前后置切换
    private Handler Buttonhandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 3) {
                btn.setText("前置");
                Toast.makeText(CameraActivity.this, btn.getText().toString(), Toast.LENGTH_SHORT).show();
            } else if (msg.what == 4) {
                btn.setText("后置");
                Toast.makeText(CameraActivity.this, btn.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }

        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            return;
        }

        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        camera.addCallbackBuffer(bytes);
                        isProcessingFrame = false;
                    }
                };
        processImage();
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            Trace.beginSection("imageAvailable");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    new Runnable() {
                        @Override
                        public void run() {
                            ImageUtils.convertYUV420ToARGB8888(
                                    yuvBytes[0],
                                    yuvBytes[1],
                                    yuvBytes[2],
                                    previewWidth,
                                    previewHeight,
                                    yRowStride,
                                    uvRowStride,
                                    uvPixelStride,
                                    rgbBytes);
                        }
                    };

            postInferenceCallback =                      //wya 回调函数，当执行完 onImageAvailable后调用，关闭图片
                    new Runnable() {
                        @Override
                        public void run() {
                            image.close();
                            isProcessingFrame = false;
                        }
                    };

            processImage();                      //wya 图片识别及处理

        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onResume() {                    //wya 启动识别线程
        LOGGER.d("onResume " + this);
        super.onResume();


        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);

        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }

        super.onPause();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
        //卸载离线语音包
        Log.d("tts", "onDestroy: ");
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            if (allPermissionsGranted(grantResults)) {
                setFragment((btn.getText().toString().equals("前置")) ? 1 : 0);
            } else {
                requestPermission();
            }
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},10);
            }
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                                CameraActivity.this,
                                "Camera permission is required for this demo",
                                Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(CameraActivity.this,Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},22);
        }
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    private String chooseCamera(int n) {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //CameraCharacteristics.LENS_FACING_BACK == 1前置
                //CameraCharacteristics.LENS_FACING_FRONT == 0后置
                if (n == 1) {
                    Buttonhandler.sendEmptyMessage(3);
                } else if (n == 0) {
                    Buttonhandler.sendEmptyMessage(4);
                }

                if (facing != null && facing == n) {
                    continue;
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }

                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                useCamera2API =
                        (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                                || isHardwareLevelSupported(
                                characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                LOGGER.i("Camera API lv2?: %s", useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            LOGGER.e(e, "Not allowed to access camera");
        }

        return null;
    }

    protected void setFragment(int i) {
        //CameraCharacteristics.LENS_FACING_BACK == 1前置
        //CameraCharacteristics.LENS_FACING_FRONT == 0后置
        String cameraId = chooseCamera(i);
        //默认后置

        Fragment fragment;  //wya 实例化 Fragment对象
        if (useCamera2API) {
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize(), client);   //wya 将client对象指针传递到Fragment中，供其调用

            camera2Fragment.setCamera(cameraId);
            fragment = camera2Fragment;
        } else {
            fragment =
                    new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        }

        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setUseNNAPI(isChecked);
        if (isChecked) apiSwitchCompat.setText("NNAPI");
        else apiSwitchCompat.setText("TFLITE");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads >= 9) return;
            numThreads++;
            threadsTextView.setText(String.valueOf(numThreads));
            setNumThreads(numThreads);
        } else if (v.getId() == R.id.minus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads == 1) {
                return;
            }
            numThreads--;
            threadsTextView.setText(String.valueOf(numThreads));
            setNumThreads(numThreads);
        }
    }

    protected void showFrameInfo(String frameInfo) {
        frameValueTextView.setText(frameInfo);
    }

    protected void showCropInfo(String cropInfo) {
        cropValueTextView.setText(cropInfo);
    }

    protected void showInference(String inferenceTime) {
        inferenceTimeTextView.setText(inferenceTime);
    }


    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();

    protected abstract void setNumThreads(int numThreads);

    protected abstract void setUseNNAPI(boolean isChecked);

}
