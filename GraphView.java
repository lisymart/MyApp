package cz.muni.fi.myapp;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GraphView extends SurfaceView implements SurfaceHolder.Callback{
	float panelHeight;
	float panelWidth;
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    private int mColors[] = new int[3 * 2];
    private Canvas mCanvas = new Canvas();
    private float val = 0 ;
    private int  width;
    ArrayList<Float> aPointsX = new ArrayList<Float>();
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
    private float[]mPoints;
    private int aCounter = 0;
    private int gCounter = 0;
    private int cCounter = 0;


    public GraphView(Context context, AttributeSet attrs) {
            super(context, attrs);
            getHolder().addCallback(this);
            setFocusable(true);          
            
            mColors[0] = Color.argb(192, 255, 64, 64);           // red
            mColors[1] = Color.argb(192, 64, 128, 64);           // green
            mColors[2] = Color.argb(192, 64, 64, 255);           // blue 
            mColors[3] = Color.argb(192, 64, 255, 255);          // light blue
            mColors[4] = Color.argb(192, 128, 64, 128);          // violet
            mColors[5] = Color.argb(192, 255, 255, 64);          // yellow

            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
            mPath.arcTo(mRect, 0, 1000);  
           
            DisplayMetrics metrics = this.getResources().getDisplayMetrics();
            width = metrics.widthPixels;
            mPoints = new float[width * 4];
            
            aPointsX.add(Float.valueOf(0));
            aPointsX.add(Float.valueOf(130));
            aPointsY.add(Float.valueOf(0));
            aPointsY.add(Float.valueOf(130));
            aPointsZ.add(Float.valueOf(0));
            aPointsZ.add(Float.valueOf(130));

            gPointsX.add(Float.valueOf(0));
            gPointsX.add(Float.valueOf(360));
            gPointsY.add(Float.valueOf(0));
            gPointsY.add(Float.valueOf(360));
            gPointsZ.add(Float.valueOf(0));
            gPointsZ.add(Float.valueOf(360));
            
            cPointsX.add(Float.valueOf(0));
            cPointsX.add(Float.valueOf(590));
            cPointsY.add(Float.valueOf(0));
            cPointsY.add(Float.valueOf(590));
            cPointsZ.add(Float.valueOf(0));
            cPointsZ.add(Float.valueOf(590));
            this.setKeepScreenOn(true);
            }


    public void draw(Canvas canvas, long stamp, int sensorType, float[] values ) {
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
                    cavas.drawText("5", 7, 280, paint);
                    cavas.drawText("-5", 7, 450, paint);
                    cavas.drawText("Gyroscope values : ", 5, 245, paint);
                    cavas.drawLine(0, 590, width, 590, paint);
                    cavas.drawLine(5, 500, 5, 680, paint);
                    cavas.drawText("0", 7, 590, paint);
                    cavas.drawText("90", 7, 510, paint);
                    cavas.drawText("- 90", 7, 680, paint);
                    cavas.drawText("Compass values : ", 5, 475, paint);
                    paint.setColor(mColors[0]);
                    cavas.drawLine(0, 232, width, 230, paint);
                    cavas.drawLine(0, 462, width, 462, paint);
                    
                    
                    switch ( sensorType ) {
                    case Sensor.TYPE_ACCELEROMETER: 
                    	// Accelerometer values plot
                    	if (values[0] > 10) values[0] = 10; 
                    	if (values[0] < -10) values[0] = -10;
                    	mPoints = toArray( aCounter, -values[0] * 9 + 130, aPointsX);
                    	paint.setColor(mColors[2]);                       
                    	cavas.drawText("X-Axis : " + values[0] , 5, 30, paint);
                    	cavas.drawLines(mPoints, paint); 
                    
                    	if (values[1] > 10) values[1] = 10; 
                    	if (values[1] < -10) values[1] = -10;
                    	mPoints = toArray(aCounter,  -values[1] * 9 + 130, aPointsY);
                    	paint.setColor(mColors[1]);
                    	cavas.drawText("Y-Axis : " + values[1] , 170, 30, paint);
                    	cavas.drawLines(mPoints, paint);

                    	if (values[2] > 10) values[2] = 10; 
                    	if (values[2] < -10) values[2] = -10;
                    	mPoints = toArray( aCounter, -values[2] * 9 + 130, aPointsZ);
                    	paint.setColor(mColors[4]);
                    	cavas.drawText("Z-Axis : " + values[2] , 340, 30, paint);
                    	cavas.drawLines(mPoints,  paint);  
                    	break;
                    
                    case Sensor.TYPE_GYROSCOPE: 
                    	// Gyro values plot
                    	if (values[0] > 5) values[0] = 5; 
                    	if (values[0] < -5) values[0] = -5;
                    	paint.setColor(mColors[2]);                       
                    	cavas.drawText("X-Axis : " + values[0] , 5, 260, paint);  
                    	mPoints = toArray(gCounter, -values[0] * 18 + 360, gPointsX);
                    	cavas.drawLines(mPoints, paint);

                    	if (values[1] > 5) values[1] = 5; 
                    	if (values[1] < -5) values[1] = -5;
                    	paint.setColor(mColors[1]);
                    	cavas.drawText("Y-Axis : " + values[1] , 170, 260, paint);
                    	mPoints = toArray(gCounter, -values[1] * 18 + 360, gPointsY);
                    	cavas.drawLines(mPoints, paint);

                    	if (values[2] > 5) values[2] = 5; 
                    	if (values[2] < -5) values[2] = -5;
                    	paint.setColor(mColors[4]);
                    	cavas.drawText("Z-Axis : " + values[2] , 340, 260, paint);
                    	mPoints = toArray( gCounter, -values[2] * 18 + 360, gPointsZ);
                    	cavas.drawLines(mPoints, paint);
                    	break;
                    
                    case Sensor.TYPE_MAGNETIC_FIELD: 
                    	// Compass values plot 
                    	paint.setColor(mColors[2]);                       
                    	cavas.drawText("X-Axis : " + values[0] , 5, 490, paint);        
                    	mPoints = toArray( cCounter, -values[0]  + 590, cPointsX);
                    	cavas.drawLines(mPoints, paint);

                    	paint.setColor(mColors[1]);
                    	cavas.drawText("Y-Axis : " + values[1] , 170, 490, paint);
                    	mPoints = toArray( cCounter, -values[1]  + 590, cPointsY);
                    	cavas.drawLines(mPoints, paint);
                    
                    	paint.setColor(mColors[4]);
                    	cavas.drawText("Z-Axis : " + values[2] , 340, 490, paint);
                    	mPoints = toArray( cCounter, -values[2]  + 590, cPointsZ);
                    	cavas.drawLines(mPoints, paint);
                    	break;
                    }
                    

                    canvas.drawBitmap(mBitmap, 0, 0, null); 
                    }
            }

    private float[] toArray(int counter, float y, ArrayList<Float> mPoints) {
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
        public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
    		this.panelHeight = (float)height;
    		this.panelWidth = (float)width;
    		Canvas c = holder.lockCanvas();
    		draw( c );
    		holder.unlockCanvasAndPost( c );
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
    		MainActivity.graphView = this;
                
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        	MainActivity.graphView = this;
                
        }
}
