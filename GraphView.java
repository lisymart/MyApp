package com.example.myapp;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;


public class GraphView extends View implements SensorEventListener {
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    private int mColors[] = new int[3 * 2];
    private Canvas mCanvas = new Canvas();
    private float mLastX;
    private float[] mYOffset = new float[4];
    private float mScale[] = new float[3];    
    private float mWidth;
    private float mMaxX;
    private float mHeight;
    private float aValues[];
    private float gValues[];
    private float cValues[];
    private int counter = 0;
    private float val = 0 ;
    private int  width;
    private int height;
    private ArrayList<Float> aPointsX = new ArrayList<Float>();
    private ArrayList<Float> aPointsY = new ArrayList<Float>();
    private ArrayList<Float> aPointsZ = new ArrayList<Float>();
    private ArrayList<Float> gPointsX = new ArrayList<Float>();
    private ArrayList<Float> gPointsY = new ArrayList<Float>();
    private ArrayList<Float> gPointsZ = new ArrayList<Float>();
    private ArrayList<Float> cPointsX = new ArrayList<Float>();
    private ArrayList<Float> cPointsY = new ArrayList<Float>();
    private ArrayList<Float> cPointsZ = new ArrayList<Float>();
    private float[] points ;
    private float x = 0;
    private float[] axPoints;
    private float[] ayPoints;
    private float[] azPoints;
    private float[] gxPoints;
    private float[] gyPoints;
    private float[] gzPoints;
    private float[] cxPoints;
    private float[] cyPoints;
    private float[] czPoints;

    public GraphView(Context context) {
            
            super(context);
            mColors[0] = Color.argb(192, 255, 64, 64);     		 // red
            mColors[1] = Color.argb(192, 64, 128, 64); 			 // green
            mColors[2] = Color.argb(192, 64, 64, 255);      	 // blue 
            mColors[3] = Color.argb(192, 64, 255, 255);          // light blue
            mColors[4] = Color.argb(192, 128, 64, 128);          // violet
            mColors[5] = Color.argb(192, 255, 255, 64);          // yellow

            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
            mPath.arcTo(mRect, 0, 1000);  
            
           
            DisplayMetrics metrics = this.getResources().getDisplayMetrics();
            width = metrics.widthPixels;
            height = metrics.heightPixels;
            axPoints = new float[width * 4];
            ayPoints = new float[width * 4];
            azPoints = new float[width * 4];  
            
            gxPoints = new float[width * 4];
            gyPoints = new float[width * 4];
            gzPoints = new float[width * 4]; 
            
            cxPoints = new float[width * 4];
            cyPoints = new float[width * 4];
            czPoints = new float[width * 4]; 
            
            aPointsX.add(Float.valueOf(0));
            aPointsX.add(Float.valueOf(130));
            aPointsY.add(Float.valueOf(0));
            aPointsY.add(Float.valueOf(130));
            aPointsZ.add(Float.valueOf(0));
            aPointsZ.add(Float.valueOf(130));

            gPointsX.add(Float.valueOf(0));
            gPointsX.add(Float.valueOf(350));
            gPointsY.add(Float.valueOf(0));
            gPointsY.add(Float.valueOf(350));
            gPointsZ.add(Float.valueOf(0));
            gPointsZ.add(Float.valueOf(350));
            
            cPointsX.add(Float.valueOf(0));
            cPointsX.add(Float.valueOf(580));
            cPointsY.add(Float.valueOf(0));
            cPointsY.add(Float.valueOf(580));
            cPointsZ.add(Float.valueOf(0));
            cPointsZ.add(Float.valueOf(580));
            }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawColor(0xFFFFFFFF);
            mYOffset[0] = h * 0.5f;
            mYOffset[1] = h * 0.25f;
            mYOffset[2] = h * 0.25f;
            mYOffset[3] = h * 0.75f;
            mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
            mScale[2] = -(h * 0.5f * (1.0f / 100000));
            mWidth = w;
            mHeight = h;
            if (mWidth < mHeight) {
                    mMaxX = w;
            } else {
                    mMaxX = w - 50;
            }
            mLastX = mMaxX;
            super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
            if (mBitmap != null) {
                    final Paint paint = mPaint;
                    final Canvas cavas = mCanvas;
                    cavas.drawColor(0xFFFFFFFF);
                    paint.setColor(0xFFAAAAAA);
                    cavas.drawLine(0, 130, width, 130, paint);
                    cavas.drawLine(5, 40, 5, 220, paint);
                    cavas.drawText("0", 7, 140, paint);
                    cavas.drawText("10", 7, 50, paint);
                    cavas.drawText("-10", 7, 220, paint);
                    cavas.drawText("Accelerometer values : ", 5, 15, paint);
                    cavas.drawLine(0, 360, width, 360, paint);
                    cavas.drawLine(5, 270, 5, 450, paint);
                    cavas.drawText("0", 7, 360, paint);
                    cavas.drawText("10", 7, 280, paint);
                    cavas.drawText("-10", 7, 450, paint);
                    cavas.drawText("Gyroscope values : ", 5, 245, paint);
                    cavas.drawLine(0, 590, width, 590, paint);
                    cavas.drawLine(5, 500, 5, 680, paint);
                    cavas.drawText("0", 7, 590, paint);
                    cavas.drawText("+ val", 7, 510, paint);
                    cavas.drawText("- val", 7, 680, paint);
                    cavas.drawText("Compass values : ", 5, 475, paint);
                    paint.setColor(mColors[0]);
                    cavas.drawLine(0, 232, width, 230, paint);
                    cavas.drawLine(0, 462, width, 462, paint);
                    
                    
                    // Accelerometer values plot
                    if (aValues[0] > 10) aValues[0] = 10; 
                    if (aValues[0] < -10) aValues[0] = -10;
                    axPoints = toArray( -aValues[0] * 9 + 130, aPointsX);
                    paint.setColor(mColors[2]);                       
                    cavas.drawText("X-Axis : " + aValues[0] , 5, 30, paint);
                    cavas.drawLines(axPoints, paint); 
                    
                    if (aValues[1] > 10) aValues[1] = 10; 
                    if (aValues[1] < -10) aValues[1] = -10;
                    ayPoints = toArray( -aValues[1] * 9 + 130, aPointsY);
                    paint.setColor(mColors[1]);
                    cavas.drawText("Y-Axis : " + aValues[1] , 170, 30, paint);
                    cavas.drawLines(ayPoints, paint);

                    if (aValues[2] > 10) aValues[2] = 10; 
                    if (aValues[2] < -10) aValues[2] = -10;
                    azPoints = toArray( -aValues[2] * 9 + 130, aPointsZ);
                    paint.setColor(mColors[4]);
                    cavas.drawText("Z-Axis : " + aValues[2] , 340, 30, paint);
                    cavas.drawLines(azPoints,  paint);    
                    
                    // Gyro values plot
                    
                    paint.setColor(mColors[2]);                       
                    cavas.drawText("X-Axis : " + gValues[0] , 5, 260, paint);  
                    gxPoints = toArray(-gValues[0] * 10 + 350, gPointsX);
                    cavas.drawLines(gxPoints, paint);

                    paint.setColor(mColors[1]);
                    cavas.drawText("Y-Axis : " + gValues[1] , 170, 260, paint);
                    gyPoints = toArray(-gValues[1] * 10 + 350, gPointsY);
                    cavas.drawLines(gyPoints, paint);

                    paint.setColor(mColors[4]);
                    cavas.drawText("Z-Axis : " + gValues[2] , 340, 260, paint);
                    gzPoints = toArray( -gValues[2] * 10 + 350, gPointsZ);
                    cavas.drawLines(gzPoints, paint);
                    
                    // Compass values plot   

                    paint.setColor(mColors[2]);                       
                    cavas.drawText("X-Axis : " + cValues[0] , 5, 490, paint);        
                    cxPoints = toArray( -cValues[0] /2 + 580, cPointsX);
                    cavas.drawLines(cxPoints, paint);

                    paint.setColor(mColors[1]);
                    cavas.drawText("Y-Axis : " + cValues[1] , 170, 490, paint);
                    cyPoints = toArray( -cValues[1] /2 + 580, cPointsY);
                    cavas.drawLines(cyPoints, paint);

                    paint.setColor(mColors[4]);
                    cavas.drawText("Z-Axis : " + cValues[2] , 340, 490, paint);
                    czPoints = toArray( -cValues[2] /2 + 580, cPointsZ);
                    cavas.drawLines(czPoints, paint);
                    

                    canvas.drawBitmap(mBitmap, 0, 0, null); 
                    }
            }

    private float[] toArray(float y, ArrayList<Float> mPoints) {
            points = new float[width * 4];              
        if (counter >= 350) {
                for (int i = 7 ; i <= mPoints.size(); i +=4){
                        val = mPoints.get(i);
                        mPoints.set(i-2, val);
                        mPoints.set(i-4, val);
                }
                mPoints.set(mPoints.size() - 2, 350.f);
                mPoints.set(mPoints.size() - 1, y); 
                
        } else {                
            x = counter - 1;
            if (mPoints.size() == 2) {
                    mPoints.add(x);
                    mPoints.add(y);
            } else {
                mPoints.add(mPoints.get(mPoints.size() - 2));
                mPoints.add(mPoints.get(mPoints.size() - 2));
                mPoints.add(x);                
                mPoints.add(y);
            }
        }
        
        for (int i = 0; i < mPoints.size(); i++) {
            Float f = mPoints.get(i);
            points[i] = (f != null ? f : Float.NaN);
        }
        return points;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        aValues = event.values.clone();
                    } else 
                    if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        gValues = event.values.clone();
                    } else 
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        cValues = event.values.clone();
                    }
                    counter++;
                    invalidate();
            }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
