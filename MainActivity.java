package com.example.myapp;

import org.openintents.sensorsimulator.hardware.Sensor;
import org.openintents.sensorsimulator.hardware.SensorEvent;
import org.openintents.sensorsimulator.hardware.SensorEventListener;
import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements SensorEventListener {
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManagerSimulator mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInitialized = false;
        mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);
        mSensorManager.connectSimulator();        
        
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
        
        Log.i("Ahoj", mAccelerometer.toString());
        Log.i("Ahoj", mCompass.toString());
        if (mGyroscope == null) Log.i("Ahoj", "Gyroskop je furt null.");
        
    }

    protected void onResume() {
        super.onResume();
               
    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mSensorManager.disconnectSimulator();
        
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
        	float values[] = event.values;
        	TextView tvX= (TextView)findViewById(R.id.ax_axis);
            TextView tvY= (TextView)findViewById(R.id.ay_axis);
            TextView tvZ= (TextView)findViewById(R.id.az_axis);
            
            float x = values[0];
            float y = values[1];
            float z = values[2];
            
            if (!mInitialized) {
                mLastX = x +1;
                mLastY = y;
                mLastZ = z;
                tvX.setText("0.0");
                tvY.setText("0.0");
                tvZ.setText("0.0");
                mInitialized = true;
            } else {
                float deltaX = Math.abs(mLastX - x);
                float deltaY = Math.abs(mLastY - y);
                float deltaZ = Math.abs(mLastZ - z);
                mLastX = x;
                mLastY = y;
                mLastZ = z;
                tvX.setText(Float.toString(deltaX));
                tvY.setText(Float.toString(deltaY));
                tvZ.setText(Float.toString(deltaZ));  
                Log.i("Ahoj", "1. if");
                
            }
        
        } else if (mGyroscope != null && event.sensor == mGyroscope) {
        	TextView tvX= (TextView)findViewById(R.id.gx_axis);
            TextView tvY= (TextView)findViewById(R.id.gy_axis);
            TextView tvZ= (TextView)findViewById(R.id.gz_axis);
            
        	try{
        	float values[] = event.values;            
            float x = values[0];
            float y = values[1];
            float z = values[2];
            
            if (!mInitialized) {
                tvX.setText("0.0");
                tvY.setText("0.0");
                tvZ.setText("0.0");
                mInitialized = true;
            } else {
                tvX.setText(Float.toString(x));
                tvY.setText(Float.toString(y));
                tvZ.setText(Float.toString(z));    
                Log.i("Ahoj", "2. if");
            }
        	} catch (Exception e) {
        		tvX.setText("No Data");
        		tvX.setText("From");
        		tvX.setText("Gyroscope");
        	}
        	
        	
        } else if (event.sensor == mCompass){
        	float values[] = event.values;
        	TextView tvX = (TextView)findViewById(R.id.cx_axis);
        	TextView tvY = (TextView)findViewById(R.id.cy_axis);
        	TextView tvZ = (TextView)findViewById(R.id.cz_axis);
        	float x = values[0];
            float y = values[1];
            float z = values[2];
                 tvX.setText(Float.toString(x));
                 tvY.setText(Float.toString(y));
                 tvZ.setText(Float.toString(z));
                 Log.i("Ahoj", "3. if");
                	 
        }   	
    }      
}

