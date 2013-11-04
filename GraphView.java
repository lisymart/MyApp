package com.example.myapp;

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
    private int aCounter = 0;
    private int gCounter = 0;
    private int cCounter = 0;
    private float val = 0 ;
    private int  width;
    private int height;
    private Vector<Float> mPointsX = new Vector<Float>();
    private Vector<Float> mPointsY = new Vector<Float>();
    private Vector<Float> mPointsZ = new Vector<Float>();
    private float[] points ;
    private float x = 0;
    private float[] xPoints;
    private float[] yPoints;
    private float[] zPoints;

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
            xPoints = new float[width * 4];
            yPoints = new float[width * 4];
            zPoints = new float[width * 4];  
            mPointsX.add(Float.valueOf(0));
            mPointsX.add(Float.valueOf(130));
            mPointsY.add(Float.valueOf(0));
            mPointsY.add(Float.valueOf(130));
            mPointsZ.add(Float.valueOf(0));
            mPointsZ.add(Float.valueOf(130));
            
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
            synchronized (this) {
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
                    cavas.drawText("+ val", 7, 280, paint);
                    cavas.drawText("- val", 7, 450, paint);
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
                    xPoints = toArray(aCounter, -aValues[0] * 9 + 130, mPointsX);
                    paint.setColor(mColors[2]);                       
                    cavas.drawText("X-Axis : " + aValues[0] , 5, 30, paint);
                    cavas.drawLines(xPoints, paint); 
                    
                    if (aValues[1] > 10) aValues[1] = 10; 
                    if (aValues[1] < -10) aValues[1] = -10;
                    yPoints = toArray(aCounter, -aValues[1] * 9 + 130, mPointsY);
                    paint.setColor(mColors[1]);
                    cavas.drawText("Y-Axis : " + aValues[1] , 170, 30, paint);
                    cavas.drawLines(yPoints, paint);

                    if (aValues[2] > 10) aValues[2] = 10; 
                    if (aValues[2] < -10) aValues[2] = -10;
                    zPoints = toArray(aCounter, -aValues[2] * 9 + 130, mPointsZ);
                    paint.setColor(mColors[4]);
                    cavas.drawText("Z-Axis : " + aValues[2] , 340, 30, paint);
                    cavas.drawLines(zPoints,  paint);    
                    
                    // Gyro values plot
                    
                    paint.setColor(mColors[2]);                       
                    cavas.drawText("X-Axis : " + gValues[0] , 5, 260, paint);                    

                    paint.setColor(mColors[1]);
                    cavas.drawText("Y-Axis : " + gValues[1] , 170, 260, paint);

                    paint.setColor(mColors[4]);
                    cavas.drawText("Z-Axis : " + gValues[2] , 340, 260, paint);
                    
                    // Compass values plot   

                    paint.setColor(mColors[2]);                       
                    cavas.drawText("X-Axis : " + cValues[0] , 5, 490, paint);                    

                    paint.setColor(mColors[1]);
                    cavas.drawText("Y-Axis : " + cValues[1] , 170, 490, paint);

                    paint.setColor(mColors[4]);
                    cavas.drawText("Z-Axis : " + cValues[2] , 340, 490, paint);
  
                    

                    canvas.drawBitmap(mBitmap, 0, 0, null); 
                    }
            }
    }
    
    private float[] toArray(int counter, float y, Vector<Float> mPoints) {
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
            synchronized (this) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        aValues = event.values;
                        aCounter ++;
                    } else 
                    if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        gValues = event.values;
                        gCounter ++;
                    } else 
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        cValues = event.values;
                        cCounter ++;
                    }
                    
                    invalidate();
            }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
