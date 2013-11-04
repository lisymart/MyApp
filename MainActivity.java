package com.example.myapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
        static final String CALIB_FILE = "calib.txt";
        public static final int IDX_X = 0;
        public static final int IDX_Y = 1;
        public static final int IDX_Z = 2;
        
    private GraphView mGraphView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mCompass;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGraphView = new GraphView(this);
        mSensorManager.registerListener(mGraphView, mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mGraphView, mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mGraphView, mCompass,SensorManager.SENSOR_DELAY_NORMAL);
        setContentView(mGraphView);
        
    }

    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onStop() {
            super.onStop();
            mSensorManager.unregisterListener(mGraphView);
            finish();
            System.exit(0);
    }
    
    protected void onPause() {
            super.onPause();
    }
   
    private boolean readStoredCalibrationParameters() {
                gyroXDrift = 0.0;
                gyroYDrift = 0.0;
                gyroZDrift = 0.0;
                stationaryAverageGravity = 0.0;
                averageCompassLength = 0.0;
                compassOffset = new double[3];
                vectorZero( compassOffset );
                File dir = getFilesDir();
                File calibFile = new File( dir, CALIB_FILE );
                try {
                        BufferedReader rdr = new BufferedReader( new FileReader( calibFile ));
                        String line = null;
                        while( ( line = rdr.readLine() ) != null ) {
                                if( line.startsWith( "gyroXDrift="))
                                        gyroXDrift = getCalibValue( line );
                                else
                                if( line.startsWith( "gyroYDrift="))
                                        gyroYDrift = getCalibValue( line );
                                else
                                if( line.startsWith( "gyroZDrift="))
                                        gyroZDrift = getCalibValue( line );
                                else
                                if( line.startsWith( "compassOffsetX="))
                                        compassOffset[IDX_X] = getCalibValue( line );
                                else
                                if( line.startsWith( "compassOffsetY="))
                                        compassOffset[IDX_Y] = getCalibValue( line );
                                else
                                if( line.startsWith( "compassOffsetZ="))
                                        compassOffset[IDX_Z] = getCalibValue( line );
                                else
                                if( line.startsWith( "stationaryAverageGravity="))
                                        stationaryAverageGravity = getCalibValue( line );
                                else
                                if( line.startsWith( "averageCompassLength="))
                                        averageCompassLength = getCalibValue( line );
                        }
                } catch( IOException ex ) {
                        Log.d("read_calib", "readStoredCalibrationParameters: IOException",ex );
                        return false;
                }
                return true;
        }
    
    private double getCalibValue( String line ) {
                int idx = line.indexOf( '=');
                String valStr = line.substring( idx+1 );
                double val = 0.0;
                try {
                        val = Double.parseDouble( valStr );
                } catch( NumberFormatException ex ) {
                        Log.e( "get_calib_values", "Invalid calibration line: "+line);
                }
                return val;
        }
        
    private void vectorZero( double vec[] ) {
                vec[IDX_X] = 0.0;
                vec[IDX_Y] = 0.0;
                vec[IDX_Z] = 0.0;
        }
    
    private void writeCalibrationParameters() {
                try {
                        File dir = getFilesDir();
                        File calibFile = new File( dir, CALIB_FILE );
                        PrintWriter pw = new PrintWriter( new FileWriter( calibFile ));
                        pw.println( "gyroXDrift="+Double.toString( gyroXDrift ));
                        pw.println( "gyroYDrift="+Double.toString( gyroYDrift ));
                        pw.println( "gyroZDrift="+Double.toString( gyroZDrift ));
                        pw.println( "compassOffsetX="+Double.toString( compassOffset[IDX_X] ));
                        pw.println( "compassOffsetY="+Double.toString( compassOffset[IDX_Y] ));
                        pw.println( "compassOffsetZ="+Double.toString( compassOffset[IDX_Z] ));
                        pw.println( "stationaryAverageGravity="+Double.toString(stationaryAverageGravity));
                        pw.println( "averageCompassLength="+Double.toString( averageCompassLength));
                        pw.close();
                } catch( IOException ex ) {
                        Log.e( "calib", "writeCalibrationParameters",ex);
                }
        }
    
 
    private double gyroXDrift;
    private double gyroYDrift;
    private double gyroZDrift;
    private double compassOffset[];
    private double stationaryAverageGravity = 0.0;
    private double averageCompassLength = 0.0;
}
