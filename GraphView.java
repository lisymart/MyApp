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
    private float values[];
    private int counter = 0;
    private float val = 0 ;
    private int  width;
    private Vector<Float> mPointsX = new Vector<Float>();
    private Vector<Float> mPointsY = new Vector<Float>();
    private Vector<Float> mPointsZ = new Vector<Float>();
    private float[] points ;
    private float x = 0;
    private int height;
    private float[] xPoints;
    private float[] yPoints;
    private float[] zPoints;

    public GraphView(Context context) {
    	
            super(context);
            mColors[0] = Color.argb(192, 255, 64, 64);	// red
            mColors[1] = Color.argb(192, 64, 128, 64);  // green
            mColors[2] = Color.argb(192, 64, 64, 255);	// blue 
            mColors[3] = Color.argb(192, 64, 255, 255);	// light blue
            mColors[4] = Color.argb(192, 128, 64, 128);	// violet
            mColors[5] = Color.argb(192, 255, 255, 64);	// yellow

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
            mPointsX.add(Float.valueOf(0));
            mPointsY.add(Float.valueOf(0));
            mPointsY.add(Float.valueOf(0));
            mPointsZ.add(Float.valueOf(0));
            mPointsZ.add(Float.valueOf(0));
            
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
                    
                    xPoints = toArray(values[0] * 10 + (height - 400) / 2, mPointsX);
                    paint.setColor(mColors[2]);                       
                    cavas.drawText("Acc X-Axis : " + values[0] , 5, 20, paint);
                    cavas.drawLines(xPoints, paint); 
                    
                    yPoints = toArray(values[1] * 30 + (height - 700) / 2, mPointsY);
                    paint.setColor(mColors[1]);
                    cavas.drawText("Acc Y-Axis : " + values[1] , 165, 20, paint);
                    cavas.drawLines(yPoints, paint);

                    zPoints = toArray(values[2] * 20 + (height + 100) / 2, mPointsZ);
                    paint.setColor(mColors[4]);
                    cavas.drawText("Acc Z-Axis : " + values[2] , 320, 20, paint);
                    cavas.drawLines(zPoints,  paint);    

                    canvas.drawBitmap(mBitmap, 0, 0, null); 
                    }
            }
    }
    
    private float[] toArray(float y, Vector<Float> mPoints) {
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
                    	values = event.values;
                    	counter += 1;
                        invalidate();
                    }
            }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
