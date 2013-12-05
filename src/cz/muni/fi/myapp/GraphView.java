package cz.muni.fi.myapp;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GraphView extends SurfaceView implements SurfaceHolder.Callback{
	float panelHeight;
	float panelWidth;
    private Paint paint = new Paint();
    private Path mPath = new Path();
    private RectF mRect = new RectF();
    private int mColors[] = new int[3 * 2];
    private float val = 0 ;
    private int  width;
    ArrayList<Float> aPointsX = new ArrayList<Float>();
    private ArrayList<Float> aPointsY = new ArrayList<Float>();
    private ArrayList<Float> aPointsZ = new ArrayList<Float>();
    private ArrayList<Float> cPointsX = new ArrayList<Float>();
    private ArrayList<Float> cPointsY = new ArrayList<Float>();
    private ArrayList<Float> cPointsZ = new ArrayList<Float>();
    private float[] points ;
    private float[] cVals = new float[3];
    private float[] aVals = new float[3];
    private float x = 0;
    private float[]mPoints;
    private int counter = 0;
    private static MovingAverageStepDetector mStepDetector;

    public static MovingAverageStepDetector getmStepDetector() {
		return mStepDetector;
	}


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

    	paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    	mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
    	mPath.arcTo(mRect, 0, 1000);  
    	
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	width = metrics.widthPixels;
    	mPoints = new float[width * 5];
    	
    	aPointsX.add(Float.valueOf(0));
    	aPointsX.add(Float.valueOf(130));
    	aPointsY.add(Float.valueOf(0));
    	aPointsY.add(Float.valueOf(130));
    	aPointsZ.add(Float.valueOf(0));
    	aPointsZ.add(Float.valueOf(130));
   	
    	cPointsX.add(Float.valueOf(0));
    	cPointsX.add(Float.valueOf(380));
    	cPointsY.add(Float.valueOf(0));
    	cPointsY.add(Float.valueOf(380));
    	cPointsZ.add(Float.valueOf(0));
    	cPointsZ.add(Float.valueOf(380));
    	this.setKeepScreenOn(true);  	
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double movingAverage1 = MovingAverageStepDetector.MA1_WINDOW;
        double movingAverage2 = MovingAverageStepDetector.MA2_WINDOW;
        double powerCutoff = MovingAverageStepDetector.POWER_CUTOFF_VALUE;
        if (prefs != null) {
                try {
                        movingAverage1 = Double.valueOf(prefs.getString(
                                        "short_moving_average_window_preference", "0.2"));
                } catch (NumberFormatException e) {
                        e.printStackTrace();
                }
                try {
                        movingAverage2 = Double.valueOf(prefs.getString(
                                        "long_moving_average_window_preference", "1.0"));
                } catch (NumberFormatException e) {
                        e.printStackTrace();
                }
                try {
                        powerCutoff = Double.valueOf(prefs.getString(
                                        "step_detection_power_cutoff_preference", "1000"));
                } catch (NumberFormatException e) {
                        e.printStackTrace();
                }
        }
        
        mStepDetector = new MovingAverageStepDetector(movingAverage1, movingAverage2, powerCutoff);
    }
    

    public void drawGraph(Canvas canvas,int sensorType, float[] values ) {
		switch( sensorType ) {
		case SService.SENSORTYPE_COMPASS:
			cVals = values;
			break;
		case SService.SENSORTYPE_ACCEL:
			aVals = values;
			break;
		}	
		drawGraph( canvas );
	}

      
    public void drawGraph(Canvas canvas) {
    	canvas.drawColor(0xFFFFFFFF);
    	paint.setColor(0xFFAAAAAA);
    	canvas.drawLine(0, 130, width, 130, paint);
    	canvas.drawLine(5, 40, 5, 220, paint);
    	canvas.drawText("0", 7, 140, paint);
    	canvas.drawText("10", 7, 50, paint);
    	canvas.drawText("-10", 7, 220, paint);
    	canvas.drawText("Linear Acceleration values : ", 5, 15, paint);
    	canvas.drawLine(0, 380, width, 380, paint);
    	canvas.drawLine(5, 290, 5, 470, paint);
    	canvas.drawText("0", 7, 390, paint);
    	canvas.drawText("+val", 7, 300, paint);
    	canvas.drawText("-val", 7, 470, paint);
    	canvas.drawText("Extern Magnetic field values : ", 5, 265, paint);
    	paint.setColor(mColors[0]);
    	canvas.drawLine(0, 240, width, 240, paint);
    	counter++;

    	// Linear Acceleration values
    	if (aVals[0] > 10) aVals[0] = 10; 
    	if (aVals[0] < -10) aVals[0] = -10;
    	mPoints = toArray(-aVals[0] * 9 + 130, aPointsX);
    	paint.setColor(mColors[2]);                       
    	canvas.drawText("X-Axis : " + aVals[0] , 5, 30, paint);
    	canvas.drawLines(mPoints, paint); 
				
    	if (aVals[1] > 10) aVals[1] = 10; 
    	if (aVals[1] < -10) aVals[1] = -10;
    	mPoints = toArray(-aVals[1] * 9 + 130, aPointsY);
    	paint.setColor(mColors[1]);
    	canvas.drawText("Y-Axis : " + aVals[1] , 170, 30, paint);
    	canvas.drawLines(mPoints, paint);

    	if (aVals[2] > 10) aVals[2] = 10; 
    	if (aVals[2] < -10) aVals[2] = -10;
    	mPoints = toArray(-aVals[2] * 9 + 130, aPointsZ);
    	paint.setColor(mColors[4]);
    	canvas.drawText("Z-Axis : " + aVals[2] , 340, 30, paint);
    	canvas.drawLines(mPoints,  paint); 
    	
    	// Ext Mag Field values
    	if (cVals[0] > 90) cVals[0] = 90; 
    	if (cVals[0] < -90) cVals[0] = -90;
    	paint.setColor(mColors[2]);                       
    	canvas.drawText("X-Axis : " + cVals[0] , 5, 280, paint);        
    	mPoints = toArray( -cVals[0]  + 380, cPointsX);
    	canvas.drawLines(mPoints, paint);
			
    	if (cVals[1] > 90) cVals[1] = 90; 
    	if (cVals[1] < -90) cVals[1] = -90;
    	paint.setColor(mColors[1]);
    	canvas.drawText("Y-Axis : " + cVals[1] , 170, 280, paint);
    	mPoints = toArray( -cVals[1]  + 380, cPointsY);
    	canvas.drawLines(mPoints, paint);
    	
    	if (cVals[2] > 90) cVals[2] = 90; 
    	if (cVals[2] < -90) cVals[2] = -90;
    	paint.setColor(mColors[4]);
    	canvas.drawText("Z-Axis : " + cVals[2] , 340, 280, paint);
    	mPoints = toArray( -cVals[2]  + 380, cPointsZ);
    	canvas.drawLines(mPoints, paint);  
    }	

    private float[] toArray(float y, ArrayList<Float> mPoints) {
    	points = new float[width * 5];              
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
    	Canvas c = new Canvas();
    	c = holder.lockCanvas();    	
    	drawGraph( c );
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