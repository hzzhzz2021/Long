/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.examples.detection.tracking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.global.SettingParam;
import org.tensorflow.lite.examples.detection.tflite.Detector.Recognition;
import org.tensorflow.lite.examples.detection.global.Global;

/**
 * A tracker that handles non-max suppression and matches existing objects to new detections.
 */
public class MultiBoxTracker {
    private static final float TEXT_SIZE_DIP = 18;
    private static final float MIN_SIZE = 16.0f;
    private static final int[] COLORS = {
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.WHITE,
            Color.parseColor("#55FF55"),
            Color.parseColor("#FFA500"),
            Color.parseColor("#FF8888"),
            Color.parseColor("#AAAAFF"),
            Color.parseColor("#FFFFAA"),
            Color.parseColor("#55AAAA"),
            Color.parseColor("#AA33AA"),
            Color.parseColor("#0D0068")
    };
    final List<Pair<Float, RectF>> screenRects = new LinkedList<Pair<Float, RectF>>();
    private final Logger logger = new Logger();
    private final Queue<Integer> availableColors = new LinkedList<Integer>();
    private final List<TrackedRecognition> trackedObjects = new LinkedList<TrackedRecognition>();
    private final List<TrackedRecognition> targetObjects = new LinkedList<TrackedRecognition>();  //wya 感兴趣的目标集合
    private final Paint boxPaint = new Paint();
    private final float textSizePx;
    private final BorderedText borderedText;
    private Matrix frameToCanvasMatrix;
    private int frameWidth;
    private int frameHeight;
    private int sensorOrientation;
    private Context context;
    private MediaPlayer pleaseplayer;
    private MediaPlayer thankplayer;
    private boolean isPlayer = false;

    public MultiBoxTracker(final Context context) {
        for (final int color : COLORS) {
            availableColors.add(color);
        }
        this.context = context;
        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Style.STROKE);
        boxPaint.setStrokeWidth(10.0f);
        boxPaint.setStrokeCap(Cap.ROUND);
        boxPaint.setStrokeJoin(Join.ROUND);
        boxPaint.setStrokeMiter(100);

        pleaseplayer = MediaPlayer.create(context, R.raw.please);
        thankplayer = MediaPlayer.create(context, R.raw.thank);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
    }

    public synchronized void setFrameConfiguration(
            final int width, final int height, final int sensorOrientation) {
        frameWidth = width;
        frameHeight = height;
        this.sensorOrientation = sensorOrientation;
    }

    public synchronized void drawDebug(final Canvas canvas) {
        final Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60.0f);

        final Paint boxPaint = new Paint();
        boxPaint.setColor(Color.RED);
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Style.STROKE);

        for (final Pair<Float, RectF> detection : screenRects) {
            final RectF rect = detection.second;
            canvas.drawRect(rect, boxPaint);
            canvas.drawText("" + detection.first, rect.left, rect.top, textPaint);
            borderedText.drawText(canvas, rect.centerX(), rect.centerY(), "" + detection.first);
        }
    }

    public synchronized void trackResults(final List<Recognition> results, final long timestamp) {
        logger.i("Processing %d results from %d", results.size(), timestamp);
        processResults(results);
    }

    private Matrix getFrameToCanvasMatrix() {
        return frameToCanvasMatrix;
    }

    public synchronized void draw(final Canvas canvas) {
        float left = 0;
        float right = 0;
        float top = 0;
        float bottom = 0;
        float width = 0;
        float height = 0;

        float maxwidth = 0;      //wya 记录一个最大目标框宽度
        TrackedRecognition MaxSizeRecognition = new TrackedRecognition();  //wya 记录一个最大尺寸的识别框
        float CenterX, CenterY;   //wya 视场中心点坐标
        Global.Xdiff = 0;        //wya 默认偏差值为9999，如果一直为此值，说明没有目标
        CenterY = canvas.getHeight() / 2;
        CenterX = canvas.getWidth() / 2;

        final boolean rotated = sensorOrientation % 180 == 90;
        Log.i("track", "Bool rotated: " + rotated);
        final float multiplier =
                Math.min(
                        canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                        canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
        frameToCanvasMatrix =
                ImageUtils.getTransformationMatrix(
                        frameWidth,
                        frameHeight,
                        (int) (multiplier * (rotated ? frameHeight : frameWidth)),
                        (int) (multiplier * (rotated ? frameWidth : frameHeight)),
                        sensorOrientation,
                        false);
        Log.i("track", "Tracked Object Num: " + Integer.toString(trackedObjects.size()));    //wya求识别结果 数量
        //wya  遍历识别结果列表 trackedObjects

        if (Global.isHavePerson) {
            Global.isHavePerson = false;
        }
        for (final TrackedRecognition recognition : trackedObjects) {
            final RectF trackedPos = new RectF(recognition.location);
            if (!Global.isHavePerson) {
                Global.isHavePerson = true;
            }
            getFrameToCanvasMatrix().mapRect(trackedPos);
            boxPaint.setColor(recognition.color);

            //if (recognition.title.equals("unmask"))  //wya 只有识别的是 "unmask"类目标，才在界面上显示 20220207
            {
                Log.i("track", "Has unmask object " + recognition.title);    //wya求识别结果类型
                if (trackedPos.width() >= maxwidth + SettingParam.minFaceDifference)   //wya 找出最大尺寸的目标框
                {
                    maxwidth = trackedPos.width();
                    MaxSizeRecognition = recognition; //wya 保存最大识别框信息
                }
            }
        }

        canvas.drawLine(CenterX - 20, CenterY, CenterX + 20, CenterY, boxPaint);
        canvas.drawLine(CenterX, CenterY - 20, CenterX, CenterY + 20, boxPaint); //wya 视场中心十字线

        if (MaxSizeRecognition.title != null)     //wya 如果对象非空
        {
            RectF targetpos = new RectF(MaxSizeRecognition.location);
            //if(MaxSizeRecognition.title.equals("unmask"))    //wya 如果最大识别框是 "unmask"
            boxPaint.setColor(MaxSizeRecognition.color);
            if (Global.CAMSEL == Global.FRONT)   //wya 如果是前置摄像头，则进行目标框坐标映射变换
            {
                targetpos.left = MaxSizeRecognition.location.left;
                targetpos.right = MaxSizeRecognition.location.right;
                targetpos.top = frameHeight - MaxSizeRecognition.location.bottom;
                targetpos.bottom = frameHeight - MaxSizeRecognition.location.top;
//          Log.i("track","Canvas Width" + canvas.getWidth());
//          Log.i("track","Canvas Height" + canvas.getHeight());
//          Log.i("track","Frame  Width" + frameWidth);
//          Log.i("track","Frame  Height" + frameHeight);
//          Log.i("track","Using Front Camera,value:" + Global.FRONT);
//          Log.i("track","CenterX:" + CenterX );
//          Log.i("track","CenterY:" + CenterY );
//          Log.i("track","location.left:" + MaxSizeRecognition.location.left);
//          Log.i("track","location.right:" + MaxSizeRecognition.location.right);
//          Log.i("track","location.width:" + MaxSizeRecognition.location.width());
//          Log.i("track","location.height:" + MaxSizeRecognition.location.height());
//          Log.i("track","location.top:" + MaxSizeRecognition.location.top);
//          Log.i("track","location.bottom:" + MaxSizeRecognition.location.bottom);
//          Log.i("track","target.left:" + targetpos.left);
//          Log.i("track","target.right:" + targetpos.right);
//          Log.i("track","target.top:" + targetpos.top);
//          Log.i("track","target.bottom:" + targetpos.bottom);
//          Log.i("track","target.width:" + targetpos.width());
//          Log.i("track","target.height:" + targetpos.height());
            }
            getFrameToCanvasMatrix().mapRect(targetpos);    //wya 将原始相机frame图上的目标框位置映射到canvas界面上去
//        Log.i("track","mapped_target.left:" + targetpos.left);
//        Log.i("track","mapped_target.right:" + targetpos.right);
//        Log.i("track","mapped_target.top:" + targetpos.top);
//        Log.i("track","mapped_target.bottom:" + targetpos.bottom);
//        Log.i("track","mapped_target.width:" + targetpos.width());
//        Log.i("track","mapped_target.height:" + targetpos.height());

            Global.Xdiff = targetpos.centerX() - CenterX;    //wya 求目标框与视场中心点的X偏差
            Global.setRectArea((int) (MaxSizeRecognition.location.width() * MaxSizeRecognition.location.height()));
            String sXdiff = String.valueOf(Global.Xdiff);

            final String labelString =
                    !TextUtils.isEmpty(MaxSizeRecognition.title)
                            ? String.format("%s：%.2f", MaxSizeRecognition.title, (100 * MaxSizeRecognition.detectionConfidence))
                            : String.format("%.2f", (100 * MaxSizeRecognition.detectionConfidence));
            Log.d("receive", "draw: " + Global.isPlayer);
            if (!Global.isPlayer){
                if (MaxSizeRecognition.title.equals("unmask") && !pleaseplayer.isPlaying() && !thankplayer.isPlaying()) {
                    pleaseplayer.start();
                    if (!isPlayer) {
                        isPlayer = true;
                    }
                } else if (isPlayer && MaxSizeRecognition.title.trim().equals("mask") && !pleaseplayer.isPlaying() && !thankplayer.isPlaying()) {
                    thankplayer.start();
                    isPlayer = false;
                }
            }else{

                pleaseplayer.stop();
                thankplayer.stop();
                try {
                    pleaseplayer.prepare();
                    thankplayer.prepare();
                    pleaseplayer = MediaPlayer.create(context, R.raw.please);
                    thankplayer = MediaPlayer.create(context, R.raw.thank);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (MaxSizeRecognition.title.equals("unmask") && !Global.isTrack) {
                Global.isTrack = true;
            } else if (MaxSizeRecognition.title.trim().equals("mask") && Global.isTrack) {
                Global.isTrack = false;
            }
            //前进<3000<后退
            Log.d("area:", "area:" + (int) (MaxSizeRecognition.location.width() * MaxSizeRecognition.location.height()));
            //wya corner_size 是目标框的圆角半径
            float cornerSize = Math.min(targetpos.width(), targetpos.height()) / 8.0f;
            borderedText.drawText(
                    canvas, targetpos.left + cornerSize, targetpos.top, labelString + "%" + " Xdiff: " + sXdiff, boxPaint);
            canvas.drawRoundRect(targetpos, cornerSize, cornerSize, boxPaint);  //wya  根据识别结果在图上画识别框
        }
    }

    private void processResults(final List<Recognition> results) {
        final List<Pair<Float, Recognition>> rectsToTrack = new LinkedList<Pair<Float, Recognition>>();

        screenRects.clear();
        final Matrix rgbFrameToScreen = new Matrix(getFrameToCanvasMatrix());
        //wya遍历识别结果
        for (final Recognition result : results) {
            if (result.getLocation() == null) {
                continue;
            }
            final RectF detectionFrameRect = new RectF(result.getLocation());   //wya 从结果中获取位置

            final RectF detectionScreenRect = new RectF();
            rgbFrameToScreen.mapRect(detectionScreenRect, detectionFrameRect);

            logger.v(
                    "Result! Frame: " + result.getLocation() + " mapped to screen:" + detectionScreenRect);
            //wya 添加置信度和位置框对,保存在泛型列表screenRects中
            screenRects.add(new Pair<Float, RectF>(result.getConfidence(), detectionScreenRect));
            if (detectionFrameRect.width() < MIN_SIZE || detectionFrameRect.height() < MIN_SIZE) {
                logger.w("Degenerate rectangle! " + detectionFrameRect);
                continue;
            }
            // wya 将结果置信度和识别结果组成一个Pair，保存在泛型列表rectsToTrack
            rectsToTrack.add(new Pair<Float, Recognition>(result.getConfidence(), result));
        }

        trackedObjects.clear();
        if (rectsToTrack.isEmpty()) {
            logger.v("Nothing to track, aborting.");
            return;
        }

        for (final Pair<Float, Recognition> potential : rectsToTrack) {
            final TrackedRecognition trackedRecognition = new TrackedRecognition();
            trackedRecognition.detectionConfidence = potential.first;
            trackedRecognition.location = new RectF(potential.second.getLocation());
            trackedRecognition.title = potential.second.getTitle();
            trackedRecognition.color = COLORS[trackedObjects.size()];
            trackedObjects.add(trackedRecognition);

            if (trackedObjects.size() >= COLORS.length) {
                break;
            }
        }
    }

    private static class TrackedRecognition {
        RectF location;
        float detectionConfidence;
        int color;
        String title;
    }
}
