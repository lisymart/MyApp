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
    private int  height;
    ArrayList<Float> aPointsX = new ArrayList<Float>();
    private ArrayList<Float> aPointsY = new ArrayList<Float>();
    private ArrayList<Float> aPointsZ = new ArrayList<Float>();
    private ArrayList<Float> cPointsX = new ArrayList<Float>();
    private ArrayList<Float> cPointsY = new ArrayList<Float>();
    private ArrayList<Float> cPointsZ = new ArrayList<Float>();
    private float[] points ;
    private float[] cVals = new float[3]; // for ext mag field
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
    	height = metrics.heightPixels;
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
		case SService.SENSORTYPE_COMPASS: // for ext mag field
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
    	int middle = height/2 - height/8;
    	paint.setColor(0xFFAAAAAA);
    	canvas.drawLine(0, middle, width, middle, paint);
    	canvas.drawLine(5, middle - height/4 - height/16, 5, middle + height/4 + height/16, paint);
    	canvas.drawText("0", 7, middle + 10, paint);
    	canvas.drawText("10", 7, middle - height/4 - height/16 + 10, paint);
    	canvas.drawText("-10", 7, middle + height/4 + height/16, paint);
    	canvas.drawText("Linear Acceleration values : ", 5, 15, paint);
    	counter += 3;
    	
    	int coef = (height/4 + height/16) / 10;
    	
    	// Linear Acceleration values
    	if (aVals[0] > 10) aVals[0] = 10; 
    	if (aVals[0] < -10) aVals[0] = -10;
    	mPoints = toArray(-aVals[0] * coef + middle, aPointsX);
    	paint.setColor(mColors[2]);                       
    	canvas.drawText("X-Axis : " + aVals[0] , 5, 30, paint);
    	canvas.drawLines(mPoints, paint);    	
    	
    	if (aVals[1] > 10) aVals[1] = 10; 
    	if (aVals[1] < -10) aVals[1] = -10;
    	mPoints = toArray(-aVals[1] * coef + middle, aPointsY);
    	paint.setColor(mColors[1]);
    	canvas.drawText("Y-Axis : " + aVals[1] , 170, 30, paint);
    	canvas.drawLines(mPoints, paint);

    	if (aVals[2] > 10) aVals[2] = 10; 
    	if (aVals[2] < -10) aVals[2] = -10;
    	mPoints = toArray(-aVals[2] * coef + middle, aPointsZ);
    	paint.setColor(mColors[4]);
    	canvas.drawText("Z-Axis : " + aVals[2] , 340, 30, paint);
    	canvas.drawLines(mPoints,  paint); 
    	
    }	

    private float[] toArray(float y, ArrayList<Float> mPoints) {
    	points = new float[width * 5];              
        if (counter >= width - 100) {
        	for (int i = 7 ; i <= mPoints.size(); i +=4){
        		val = mPoints.get(i);
        		mPoints.set(i-2, val);
        		mPoints.set(i-4, val);
        	}
        	mPoints.set(mPoints.size() - 2, (float) width-100);
        	mPoints.set(mPoints.size() - 1, y); 
        	
        } else {                
        	x = counter - 3;
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
