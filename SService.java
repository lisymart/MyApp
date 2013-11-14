package cz.muni.fi.myapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import cz.muni.fi.myapp.*;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SService extends Service implements SensorEventListener{
	
	static final String LOG_TAG = "Fusion";
    public static final int IDX_X = 0;
    public static final int IDX_Y = 1;
    public static final int IDX_Z = 2;
    static final String CALIB_FILE = "calib.txt";
	static final boolean DEBUG = true;
	public static final int  ENGINESTATES_IDLE = 0;
	public static final int ENGINESTATES_STATIONARY_CALIBRATING = 1;
	public static final int ENGINESTATES_COMPASS_CALIBRATING = 2;
	public static final int ENGINESTATES_MEASURING = 3;
	public static final int SENSORTYPE_NA = 0;
	public static final int SENSORTYPE_GYRO = 2;
	public static final int SENSORTYPE_ACCEL = 1;
	public static final int SENSORTYPE_COMPASS = 0;
	
	static final int SAMPLECTR_MOD = 100;
	static final double CALIBRATING_ACCEL_LIMIT = 0.1;
	static final double NOMOTION_ACCEL_LIMIT = 0.1;
	static final long MAX_ACCEL_COMPASS_DIFF = 60L * 1000L * 1000L; 
	static final long DIFF_UPDATE_TIMEOUT = 100L;
	static final int STATIONARY_ACCEL_SAMPLE_LIMIT = 250;
	static final double STATIONARY_GRAVITY_EXP_AVG = 0.01;
	static final double COMPASS_EXP_AVG = 0.01;
	static final double GYRO_NOISE_LIMIT = 0.01;
	static final int COMPASS_COLLECT_BASEVECTOR_LENGTH = 50;
	static final double BASEVECTOR_EXP_AVG = 0.1;
	static final double COMPASS_MARGIN = 0.1;
	
	public static final int SUBSTATE_COMPASS_COLLECT_BASEVECTOR = 200;
	public static final int SUBSTATE_COMPASS_MEASURE = 201;
	public static final int LAST_GRAPH_INDEX = 6;
	public static final int ENGINESTATES_TRANSIENT = 100;
	public static final int ENGINESTATES_XGONE = 100;
	public static final int ENGINESTATES_YGONE = 101;
	public static final int ENGINESTATES_ZGONE = 102;
	public static final int NO_LINEAR_ACCELERATION_VECTOR = 3;
	public static final int LINEAR_ACCELERATION_VECTOR = 2;
	public static final int RAW_COMPASS_ANGLE = 0;
	public static final int CORRECTED_COMPASS_ANGLE = 1;
	public static final int EXT_MAGNETIC_FIELD_VECTOR = 4;
	public static final int NO_EXT_MAGNETIC_FIELD_VECTOR = 5;
	
    
	IIR gravitycompensatedangle_filter;
	private long graphTimestamp[];
	private int compassSubstate;
	private int compassSubstateCounter;
    private double baseCompassVector[];
    private double gyroAccelVector[];
    private double gyroCompassVector[];
	private double compassLowLimit;
	private double compassHighLimit;
    private double gravityNoMotionLowLimit;
    private double gravityNoMotionHighLimit;
	private int state;
	private boolean samplingStarted = false;
    private PrintWriter captureFile = null;
    private SensorManager sensorManager;
	private Sensor accelSensor;
	private Sensor compassSensor;
	private Sensor gyroSensor;
    private double gyroXDrift;
    private double gyroYDrift;
    private double gyroZDrift;
    private double compassOffset[];
    private double stationaryAverageGravity = 0.0;
    private double averageCompassLength = 0.0;
    private boolean XMinReady;
    private boolean XMaxReady;
    private boolean YMinReady;
    private boolean YMaxReady;
    private boolean ZMinReady;
    private boolean ZMaxReady;
    private boolean compassXMinSet;
    private double compassXMin;
    private double compassXMinVec[] = new double[3];
    private boolean compassXMaxSet;
    private double compassXMax;
    private double compassXMaxVec[] = new double[3];
    private boolean compassYMinSet;
    private double compassYMin;
    private double compassYMinVec[] = new double[3];
    private boolean compassYMaxSet;
    private double compassYMax;
    private double compassYMaxVec[] = new double[3];
    private boolean compassZMinSet;
    private double compassZMin;
    private double compassZMinVec[] = new double[3];
    private boolean compassZMaxSet;
    private double compassZMax;
    private double compassZMaxVec[] = new double[3];
    private double calibrationGravityLowLimit;
    private double calibrationGravityHighLimit;
    private long lastCalibratingCompassTimeStamp;
    private int sampleCounter;
    private int stationaryCalibratingAccelCounter = 0;
    private int stationaryCalibratingGyroCounter = 0;
    private long stationaryCalibratingGyroTimeStamp = 0L;
    private double gyroXPos = 0.0;
    private double gyroYPos = 0.0;
    private double gyroZPos = 0.0;
    private long gyroLastTimeStamp = 0L;
    
    
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand( intent, flags, startId );
		stopSampling();		// just in case the activity-level service management fails
		sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE );
		startSampling();
		return START_NOT_STICKY;
	}
    
	private void startSampling() {
		if( samplingStarted )
			return;
       	List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ACCELEROMETER  );
      	accelSensor = sensors.size() == 0 ? null : sensors.get( 0 );
       	sensors = sensorManager.getSensorList( Sensor.TYPE_MAGNETIC_FIELD );
      	compassSensor = sensors.size() == 0 ? null : sensors.get( 0 );
       	sensors = sensorManager.getSensorList( Sensor.TYPE_GYROSCOPE );
       	gyroSensor = sensors.size() == 0 ? null : sensors.get( 0 );
      	initSampling();
       	
      	if( ( accelSensor != null ) && 
      		( compassSensor != null ) &&
      		( gyroSensor != null ) ) {
			Log.d( LOG_TAG, "registerListener/SamplingService" );
           	sensorManager.registerListener( 
                            this, 
                            accelSensor,
                            SensorManager.SENSOR_DELAY_FASTEST );
           	sensorManager.registerListener( 
                    this, 
                    compassSensor,
                    SensorManager.SENSOR_DELAY_FASTEST );
           	sensorManager.registerListener( 
                    this, 
                    gyroSensor,
                    SensorManager.SENSOR_DELAY_GAME );
		} else {
			Log.d( LOG_TAG, 
				"Sensor(s) missing: accelSensor: "+
				accelSensor+
				"; compassSensor: "+
				compassSensor+
				"; gyroSensor: "+
				gyroSensor);
		}
      	captureFile = null;
      	if( DEBUG ) {
      		Date d = new Date();
      		String filename = "capture_"+
      					Integer.toString( d.getYear()+1900 )+
      					"_"+
      					Integer.toString( d.getMonth()+1 )+
      					"_"+
      					Integer.toString( d.getDate())+
      					"_"+
      					Integer.toString( d.getHours())+
      					"_"+
      					Integer.toString( d.getMinutes())+
      					".csv";
      		File captureFileName = new File( 
      				Environment.getExternalStorageDirectory(), 
      				filename );
      		try {
      			captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
      		} catch( IOException ex ) {
      			Log.e( LOG_TAG, ex.getMessage(), ex );
      		}
      	}
      	samplingStarted = true;
	}
	

	private void stopSampling() {
		if( !samplingStarted )
			return;
        if( sensorManager != null ) {
			Log.d( LOG_TAG, "unregisterListener/SamplingService" );
            sensorManager.unregisterListener( this );
		}
        if( captureFile != null ) {
            captureFile.close();
			captureFile = null;
        }
		samplingStarted = false;
		setState( ENGINESTATES_IDLE);
	}
	
	
	private void initSampling() {
		sampleCounter = 0;
	    compassXMinSet = false;
	    compassXMaxSet = false;
	    compassYMinSet = false;
	    compassYMaxSet = false;
	    compassZMinSet = false;
	    compassZMaxSet = false;
		XMinReady = false;
		XMaxReady = false;
		YMinReady = false;
		YMaxReady = false;
		ZMinReady = false;
		ZMaxReady = false;
		lastCalibratingCompassTimeStamp = 0L;
		stationaryCalibratingAccelCounter = 0;
		stationaryCalibratingGyroCounter = 0;
		gyroXPos = 0.0;
		gyroYPos = 0.0;
		gyroZPos = 0.0;
		gyroLastTimeStamp = 0L;
		if( readStoredCalibrationParameters() ) {
			initCompassCalibration();
			initMeasuring();
			setState( ENGINESTATES_MEASURING );
		}
		else
			setState( ENGINESTATES_STATIONARY_CALIBRATING );
	}
	
    @Override
    public void onSensorChanged(SensorEvent event) {
    	processSample( event );
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
    
	public IBinder onBind(Intent intent) {
		return serviceBinder;	// cannot bind
	}
	
	private void setState( int newState ) {
		Log.d( LOG_TAG, "Transitioning from "+getStateName( state )+
				" to "+getStateName( newState )+
				" at sample counter "+sampleCounter );
		if( state != newState ) {
			state = newState;
			if( fusion != null ) {
				try {
					fusion.statusMessage( state );
				} catch( DeadObjectException ex ) {
					Log.e( LOG_TAG,"step() callback", ex );
				} catch( RemoteException ex ) {
					Log.e( LOG_TAG, "RemoteException",ex );
				}
			} else
				Log.d( LOG_TAG, "setState: cannot call back activity");
		}
	}
	
	private String getStateName( int state ) {
		String stateName = null;
		switch( state ) {
		case ENGINESTATES_IDLE:
			stateName = "Idle";
			break;
		
		case ENGINESTATES_STATIONARY_CALIBRATING:
			stateName = "Calibrating gyro and accelerometer";
			break;

		case ENGINESTATES_COMPASS_CALIBRATING:
			stateName = "Calibrating compass";
			break;
						
		case ENGINESTATES_MEASURING:
			stateName = "Measuring";
			break;
			
		default:
			stateName = "N/A";
			break;
		}
		return stateName;
	}
	
	private void processSample( SensorEvent sensorEvent ) {
		float values[] = sensorEvent.values;
		if( values.length < 3 )
				return;
		String sensorName = "n/a";
		int sensorType = SENSORTYPE_NA;
		if( sensorEvent.sensor == accelSensor ) {
			sensorName="accel";
			sensorType = SENSORTYPE_ACCEL;
		} else
		if( sensorEvent.sensor == compassSensor ) {
			sensorName = "compass";
			sensorType = SENSORTYPE_COMPASS;
		} else
		if( sensorEvent.sensor == gyroSensor ) {
			sensorName = "gyro";
			sensorType = SENSORTYPE_GYRO;
		}
		if( captureFile != null ) {
			captureFile.println( sensorEvent.timestamp+
					","+
					sensorName+
					","+
					values[0]+
					","+
					values[1]+
					","+
					values[2]);
		}
		updateSampleCounter();
		switch( state ) {
		case ENGINESTATES_STATIONARY_CALIBRATING:
			processStationaryCalibrating( sensorEvent.timestamp, sensorType, values );
			break;
			
		case ENGINESTATES_COMPASS_CALIBRATING:
			processCompassCalibrating( sensorEvent.timestamp, sensorType, values );
			break;
			
		case ENGINESTATES_MEASURING:
			processMeasuring( sensorEvent.timestamp, sensorType, values );
			break;
		}
	}
	
	private void initCompassCalibration() {
		calibrationGravityLowLimit = -( stationaryAverageGravity * ( 1.0 - CALIBRATING_ACCEL_LIMIT ) );
		calibrationGravityHighLimit = stationaryAverageGravity * ( 1.0 - CALIBRATING_ACCEL_LIMIT ); 		
		gravityNoMotionLowLimit = stationaryAverageGravity * ( 1.0 - NOMOTION_ACCEL_LIMIT );
		gravityNoMotionHighLimit = stationaryAverageGravity * ( 1.0 + NOMOTION_ACCEL_LIMIT );		
	}
	
	private void initMeasuring() {
		compassLowLimit = averageCompassLength * COMPASS_MARGIN;
		compassHighLimit = averageCompassLength * ( 1+COMPASS_MARGIN);
		gyroCompassVector = null;
		baseCompassVector = new double[3];
		vectorZero( baseCompassVector );
		compassSubstate = SUBSTATE_COMPASS_COLLECT_BASEVECTOR;
		compassSubstateCounter = COMPASS_COLLECT_BASEVECTOR_LENGTH;
		gyroAccelVector = null;
		graphTimestamp = new long[ LAST_GRAPH_INDEX ];
		for( int i = 0 ; i < LAST_GRAPH_INDEX ; ++i )
			graphTimestamp[i] = -1L;
		double n_coeffs[] = 			{
				0.00970018, -0.03780079,  0.05621527, -0.03780079,  0.00970018
			};
		double dn_coeffs[] =
			{
				1.        , -3.88471159,  5.66462122, -3.67459706,  0.89470165
			};
		gravitycompensatedangle_filter = new IIR(n_coeffs,dn_coeffs);
	}
	
	private void updateSampleCounter() {		
		++sampleCounter;
		if( ( sampleCounter % SAMPLECTR_MOD ) == 0 ) {
			if( fusion == null )
				Log.d( 
					LOG_TAG, 
					"updateSampleCounter() callback: cannot call back (sampleCounter: "+
					sampleCounter+
					")" );
			else {
				Log.d( 
					LOG_TAG, 
					"updateSampleCounter() callback: sampleCounter: "+
					sampleCounter );
				try {
					fusion.sampleCounter( sampleCounter );
				} catch( DeadObjectException ex ) {
					Log.e( LOG_TAG,"step() callback", ex );
				} catch( RemoteException ex ) {
					Log.e( LOG_TAG, "RemoteException",ex );
				}
			}
		}
	}
	
	
	private void processStationaryCalibrating( long timeStamp, int sensorType, float values [] ) {
		double dValues[] = new double[3];
		dValues[IDX_X] = (double)values[IDX_X];
		dValues[IDX_Y] = (double)values[IDX_Y];
		dValues[IDX_Z] = (double)values[IDX_Z];
		if( sensorType == SENSORTYPE_ACCEL) {
			double vl = vectorLength( dValues );
			stationaryAverageGravity = 
					( stationaryAverageGravity * ( 1.0 - STATIONARY_GRAVITY_EXP_AVG ) ) +
					( vl * STATIONARY_GRAVITY_EXP_AVG );
			++stationaryCalibratingAccelCounter;
			if( stationaryCalibratingAccelCounter >= STATIONARY_ACCEL_SAMPLE_LIMIT) {
				double gyroMeasurementTime = 
						(double)( gyroLastTimeStamp - 
						stationaryCalibratingGyroTimeStamp ) / 1000000000.0;
				gyroXDrift = gyroXPos / gyroMeasurementTime;
				gyroYDrift = gyroYPos / gyroMeasurementTime;
				gyroZDrift = gyroZPos / gyroMeasurementTime;
				Log.d( LOG_TAG, "Gyro drifts: x: "+gyroXDrift+
								"; y: "+gyroYDrift+
								"; z: "+gyroZDrift);
				initCompassCalibration();
				setState( ENGINESTATES_COMPASS_CALIBRATING );
				Log.d( LOG_TAG, "Gyro calibration: "+
						stationaryCalibratingGyroCounter+
						" samples; "+
						"gravity calibration: "+
						stationaryCalibratingAccelCounter+
						" samples"
						);
				Log.d(LOG_TAG, 
						"reference gravity: "+stationaryAverageGravity);
			}
		} else
		if( sensorType == SENSORTYPE_GYRO ) {
			if( stationaryCalibratingGyroCounter == 0 )
				stationaryCalibratingGyroTimeStamp = timeStamp;
			if( gyroLastTimeStamp > 0L) {
				double dt = (double)(timeStamp - gyroLastTimeStamp) / 1000000000.0;
				double dx = gyroNoiseLimiter( dValues[IDX_X] )*dt;
				double dy = gyroNoiseLimiter( dValues[IDX_Y] )*dt;
				double dz = gyroNoiseLimiter( dValues[IDX_Z] )*dt;
				gyroXPos += dx;
				gyroYPos += dy;
				gyroZPos += dz;
			}
			gyroLastTimeStamp = timeStamp;
			++stationaryCalibratingGyroCounter;
		}
	}
	
	
	private void processCompassCalibrating( long timeStamp, int sensorType, float values [] ) {
		double dValues[] = new double[3];
		dValues[IDX_X] = (double)values[IDX_X];
		dValues[IDX_Y] = (double)values[IDX_Y];
		dValues[IDX_Z] = (double)values[IDX_Z];
		if( sensorType == SENSORTYPE_ACCEL ) {
			if(  ( timeStamp - lastCalibratingCompassTimeStamp ) < MAX_ACCEL_COMPASS_DIFF ) {
				boolean XReadyTransition = false;
				boolean YReadyTransition = false;
				boolean ZReadyTransition = false;
// X max/min
				if( dValues[IDX_X] < calibrationGravityLowLimit ) {
					if( !XMinReady && XMaxReady )
						XReadyTransition = true;
					XMinReady = true;
				}
				if( dValues[IDX_X] > calibrationGravityHighLimit ) {
					if( !XMaxReady && XMinReady )
						XReadyTransition = true;
					XMaxReady = true;
				}
// Y max/min
				if( dValues[IDX_Y] < calibrationGravityLowLimit ) {
					if( !YMinReady && YMaxReady )
						YReadyTransition = true;
					YMinReady = true;
				}
				if( dValues[IDX_Y] > calibrationGravityHighLimit ) {
					if( !YMaxReady && YMinReady )
						YReadyTransition = true;
					YMaxReady = true;
				}
// Z max/min
				if( dValues[IDX_Z] < calibrationGravityLowLimit ) {
					if( !ZMinReady && ZMaxReady )
						ZReadyTransition = true;
					ZMinReady = true;
				}
				if( dValues[IDX_Z] > calibrationGravityHighLimit ) {
					if( !ZMaxReady && ZMinReady )
						ZReadyTransition = true;
					ZMaxReady = true;
				}
				if( XReadyTransition )
					setStatus( ENGINESTATES_XGONE );
				if( YReadyTransition )
					setStatus( ENGINESTATES_YGONE );
				if( ZReadyTransition )
					setStatus( ENGINESTATES_ZGONE );
				if( XMinReady && 
					XMaxReady &&
					YMinReady &&
					YMaxReady &&
					ZMinReady &&
					ZMaxReady ) {
// Calibration finished
					resolveCompassOffsets();
					compassOffset[IDX_X] = ( compassXMin + compassXMax ) / 2;
					compassOffset[IDX_Y]= ( compassYMin + compassYMax ) / 2;
					compassOffset[IDX_Z] = ( compassZMin + compassZMax ) / 2;
					Log.d( LOG_TAG, "Offsets; x: "+compassOffset[IDX_X]+
							"; y: "+compassOffset[IDX_Y]+
							"; z: "+compassOffset[IDX_Z]);
					writeCalibrationParameters();
					initMeasuring();
					setState( ENGINESTATES_MEASURING );
				}
			}
		} else
		if( sensorType == SENSORTYPE_COMPASS ) {
			double compassLen = vectorLength( dValues );
			averageCompassLength = averageCompassLength * ( 1 - COMPASS_EXP_AVG ) +
								compassLen * COMPASS_EXP_AVG;
			lastCalibratingCompassTimeStamp = timeStamp;
			if( !compassXMinSet || ( dValues[IDX_X] < compassXMin ) ) {
				compassXMinSet = true;
				compassXMin = dValues[IDX_X];
				vectorCopy( compassXMinVec,dValues );
			}
			if( !compassXMaxSet || ( dValues[IDX_X] > compassXMax ) ) {
				compassXMaxSet = true;
				compassXMax = dValues[IDX_X];
				vectorCopy( compassXMaxVec,dValues );
			}
			if( !compassYMinSet || ( dValues[IDX_Y] < compassYMin ) ) {
				compassYMinSet = true;
				compassYMin = dValues[IDX_Y];
				vectorCopy( compassYMinVec,dValues );
			}
			if( !compassYMaxSet || ( dValues[IDX_Y] > compassYMax ) ) {
				compassYMaxSet = true;
				compassYMax = dValues[IDX_Y];
				vectorCopy( compassYMaxVec,dValues );
			}
			if( !compassZMinSet || ( dValues[IDX_Z] < compassZMin ) ) {
				compassZMinSet = true;
				compassZMin = dValues[IDX_Z];
				vectorCopy( compassZMinVec,dValues );
			}
			if( !compassZMaxSet || ( dValues[IDX_Z] > compassZMax ) ) {
				compassZMaxSet = true;
				compassZMax = dValues[IDX_Z];
				vectorCopy( compassZMaxVec,dValues );
			}
		}
	}
	
	
	private void processMeasuring( long timeStamp, int sensorType, float values [] ) {
		double dValues[] = new double[3];
		dValues[IDX_X] = (double)values[IDX_X];
		dValues[IDX_Y] = (double)values[IDX_Y];
		dValues[IDX_Z] = (double)values[IDX_Z];
		if( sensorType == SENSORTYPE_ACCEL ) {
			double accelLength = vectorLength( dValues );
			if( ( accelLength > gravityNoMotionLowLimit ) && 
				( accelLength < gravityNoMotionHighLimit) ) {
// No motion acceleration - save the acceleration vector
				if( gyroAccelVector == null )
					gyroAccelVector = new double[3];
				vectorCopy( gyroAccelVector, dValues );
			} else {
				if( gyroAccelVector != null ) {
					double linAccel[] = vectorSub( dValues,gyroAccelVector);
					if( DEBUG )
						captureFile.println( 
								timeStamp+
								","+
								"linAccel"+
								","+
								linAccel[IDX_X]+
								","+
								linAccel[IDX_Y]+
								","+
								linAccel[IDX_Z]);
					double degree = Math.atan2( linAccel[IDX_Y], 
							linAccel[IDX_X]);
					if( degree < 0)
						degree = 2*Math.PI + degree;
					double len = vector2DLength( 
							linAccel[IDX_Y], 
							linAccel[IDX_X] ) * 10.0;
				}
			}
		} else
		if( sensorType == SENSORTYPE_GYRO ) {
			if( gyroLastTimeStamp > 0L ) {
				double dt = (double)(timeStamp - gyroLastTimeStamp) / 1000000000.0;
				double dx = gyroNoiseLimiter( dValues[IDX_X] )*dt;
				dx -= dt*gyroXDrift;
				double dy = gyroNoiseLimiter( dValues[IDX_Y] )*dt;
				dy -= dt*gyroYDrift;
				double dz = gyroNoiseLimiter( dValues[IDX_Z] )*dt;
				dz -= dt*gyroZDrift;
				if( gyroCompassVector != null ) {
					rotx( gyroCompassVector,-dx);
					roty( gyroCompassVector,-dy);
					rotz( gyroCompassVector,-dz);
				}
				if( gyroAccelVector != null ) {
					rotx( gyroAccelVector, -dx );
					roty( gyroAccelVector,-dy);
					rotz( gyroAccelVector,-dz);
				}
				if( DEBUG) {
					if( gyroCompassVector != null )
						captureFile.println( 
								timeStamp+
								","+
								"gyroCompassVector"+
								","+
								gyroCompassVector[IDX_X]+
								","+
								gyroCompassVector[IDX_Y]+
								","+
								gyroCompassVector[IDX_Z]);
					if( gyroAccelVector != null )
						captureFile.println( 
								timeStamp+
								","+
								"gyroAccelVector"+
								","+
								gyroAccelVector[IDX_X]+
								","+
								gyroAccelVector[IDX_Y]+
								","+
								gyroAccelVector[IDX_Z]);
				}
			}
			gyroLastTimeStamp = timeStamp;
		} else
		if( sensorType == SENSORTYPE_COMPASS ) {
			switch( compassSubstate ) {
			case SUBSTATE_COMPASS_COLLECT_BASEVECTOR:
				averageInNewCompassValue( dValues );
				if( --compassSubstateCounter <= 0 ) {
					gyroCompassVector = new double[3];
					vectorCopy( gyroCompassVector,baseCompassVector);
					compassSubstate = SUBSTATE_COMPASS_MEASURE;
				}
				break;
					
			case SUBSTATE_COMPASS_MEASURE:
				double currentCompassVecLen = vectorLength( dValues );
				if( ( currentCompassVecLen > compassLowLimit ) && 
					( currentCompassVecLen < compassHighLimit ) ) {
// Compass measurement considered reliable - update the reference vector used as
// a base for gyro
					dValues = vectorSub( dValues,compassOffset );
					vectorCopy( gyroCompassVector, dValues );
					if( DEBUG)
						captureFile.println( 
							timeStamp+
							","+
							"correctedCompassVector"+
							","+
							gyroCompassVector[IDX_X]+
							","+
							gyroCompassVector[IDX_Y]+
							","+
							gyroCompassVector[IDX_Z]);
				} else {
					double extMagneticField[] = vectorSub( dValues,gyroCompassVector);
					if( DEBUG)
						captureFile.println( 
							timeStamp+
							","+
							"extMagneticField"+
							","+
							extMagneticField[IDX_X]+
							","+
							extMagneticField[IDX_Y]+
							","+
							extMagneticField[IDX_Z]);
					double degree = Math.atan2( extMagneticField[IDX_Y], 
							extMagneticField[IDX_X]);
					if( degree < 0)
						degree = 2*Math.PI + degree;
					double len = vector2DLength( 
							extMagneticField[IDX_Y], 
							extMagneticField[IDX_X] ) / 10.0 * 25.0;
				}
				double degree = Math.atan2( gyroCompassVector[IDX_Y], 
											gyroCompassVector[IDX_X]);
				if( degree < 0)
					degree = 2*Math.PI + degree;
				if( DEBUG )
					captureFile.println(
							timeStamp+
							","+
							"raw_angle"+
							","+
							degree);
				// Do we have a usable acceleration vector?
				if( gyroAccelVector != null ) {
					double rotatedCompass[] = rotateByAccel( gyroCompassVector,gyroAccelVector );
//					double rotatedCompass[] = new double[3];
//					vectorCopy( rotatedCompass, gyroCompassVector);
					degree = Math.atan2( rotatedCompass[IDX_Y], rotatedCompass[IDX_X]);
					if( degree < 0)
						degree = 2*Math.PI + degree;
					degree = gravitycompensatedangle_filter.filter( degree );
					if( DEBUG )
						captureFile.println(
								timeStamp+
								","+
								"corrected_angle"+
								","+
								degree);
				}
			}
		}
	}		
	
	private double vectorLength( double vec[] ) {
		return Math.sqrt( ( vec[IDX_X]*vec[IDX_X] )+
						  ( vec[IDX_Y]*vec[IDX_Y] )+
						  ( vec[IDX_Z]*vec[IDX_Z] ) );
	}
	
	private double vector2DLength( double x, double y ) {
		return Math.sqrt( ( x*x )+ ( y*y ) );
	}
	
	private double gyroNoiseLimiter( double gyroValue ) {
		double v = gyroValue;
		if( Math.abs( v ) < GYRO_NOISE_LIMIT )
			v = 0.0;
		return v;			
	}
	
	private void setStatus( int status ) {
		Log.d( LOG_TAG, "Setting status "+status+
				" at sample counter "+sampleCounter );
		if( fusion != null ) {
			try {
				fusion.statusMessage( status );
			} catch( DeadObjectException ex ) {
				Log.e( LOG_TAG,"step() callback", ex );
			} catch( RemoteException ex ) {
				Log.e( LOG_TAG, "RemoteException",ex );
			}
		} else
			Log.d( LOG_TAG, "setStatus: cannot call back activity");
	}
	
	
	private void resolveCompassOffsets() {
		double p1[] = compassXMinVec;
		double p2[] = compassXMaxVec;
		double p3[] = compassYMinVec;
		double p4[] = compassZMaxVec;
		double a00=2*(p2[0]-p1[0]);
		double a01=2*(p2[1]-p1[1]);
		double a02=2*(p2[2]-p1[2]);
		double a10=2*(p3[0]-p1[0]);
		double a11=2*(p3[1]-p1[1]);
		double a12=2*(p3[2]-p1[2]);
		double a20=2*(p4[0]-p1[0]);
		double a21=2*(p4[1]-p1[1]);
		double a22=2*(p4[2]-p1[2]);
		double y0=(p2[0]*p2[0])+(p2[1]*p2[1])+(p2[2]*p2[2])-(p1[0]*p1[0])-(p1[1]*p1[1])-(p1[2]*p1[2]);
		double y1=(p3[0]*p3[0])+(p3[1]*p3[1])+(p3[2]*p3[2])-(p1[0]*p1[0])-(p1[1]*p1[1])-(p1[2]*p1[2]);
		double y2=(p4[0]*p4[0])+(p4[1]*p4[1])+(p4[2]*p4[2])-(p1[0]*p1[0])-(p1[1]*p1[1])-(p1[2]*p1[2]);
		double Aa[][] = {{a00,a01,a02},{a10,a11,a12},{a20,a21,a22}};
		Matrix A = new Matrix( Aa );
		double ya[][] = { {y0},{y1},{y2} };
		Matrix y = new Matrix( ya );
		Matrix x = A.solve(y);
		double xa[][] = x.getArray();
		compassOffset[IDX_X] = xa[0][0];
		compassOffset[IDX_Y] = xa[1][0];
		compassOffset[IDX_Z] = xa[2][0];
	}
	
	
	private void vectorCopy( double target[], double source[] ) {
		target[IDX_X] = source[IDX_X];
		target[IDX_Y] = source[IDX_Y];
		target[IDX_Z] = source[IDX_Z];
	}
	
	private double[] vectorSub( double v1[],double v2[]) {
		double r[] = new double[3];
		r[IDX_X] = v1[IDX_X]-v2[IDX_X];
		r[IDX_Y] = v1[IDX_Y]-v2[IDX_Y];
		r[IDX_Z] = v1[IDX_Z]-v2[IDX_Z];
		return r;
	}
	
	private void rotz( double vec[], double dz ) {
		double x = vec[IDX_X];
		double y = vec[IDX_Y];
		double z = vec[IDX_Z];
		vec[IDX_X] = x*Math.cos(dz)-y*Math.sin(dz);
		vec[IDX_Y] = x*Math.sin(dz)+y*Math.cos(dz);
	}

	private void rotx( double vec[], double dx ) {
		double x = vec[IDX_X];
		double y = vec[IDX_Y];
		double z = vec[IDX_Z];
		vec[IDX_Y] = y*Math.cos(dx)-z*Math.sin(dx);
		vec[IDX_Z] = y*Math.sin(dx)+z*Math.cos(dx);
	}
				
	private void roty( double vec[], double dy ) {
		double x = vec[IDX_X];
		double y = vec[IDX_Y];
		double z = vec[IDX_Z];
		vec[IDX_Z] = z*Math.cos(dy)-x*Math.sin(dy);
		vec[IDX_X] = z*Math.sin(dy)+x*Math.cos(dy);
	}

	
	private void averageInNewCompassValue( double dValues[] ) {
		baseCompassVector[IDX_X] = 
				( baseCompassVector[IDX_X] * ( 1-BASEVECTOR_EXP_AVG ) ) +
				( dValues[IDX_X] * BASEVECTOR_EXP_AVG );
		baseCompassVector[IDX_Y] = 
				( baseCompassVector[IDX_Y] * ( 1-BASEVECTOR_EXP_AVG ) ) +
				( dValues[IDX_Y] * BASEVECTOR_EXP_AVG );
		baseCompassVector[IDX_Z] = 
				( baseCompassVector[IDX_Z] * ( 1-BASEVECTOR_EXP_AVG ) ) +
				( dValues[IDX_Z] * BASEVECTOR_EXP_AVG );
	}
	
	private double[] rotateByAccel( double compassVector[], double accelVector[] ) {
		double zaxis[] = { 0.0, 0.0, 1.0 };
		Object r[] = calculateRotationVector( accelVector, zaxis );
		double u[] = (double[])r[0];
		Double angle = (Double)r[1];
		double rotatedCompass[] = rotateAroundAxis( compassVector, u, angle.doubleValue());
		return rotatedCompass;
	}
	
	private Object[] calculateRotationVector( double v1[], double v2[]) {
		double angle=vectorAngle(v1,v2 );
		double u[] = vectorCrossProductU( v1,v2);
		Object r[] = new Object[2];
		r[0] = u;
		r[1] = new Double( angle );
		return r;
	}
	
	private double[] rotateAroundAxis( double vec[], double u[], double angle) {
		double ca = Math.cos( angle );
		double sa = Math.sin( angle );
		double omca = 1 - ca;
		double r[][] = new double[3][3];
		r[0][0]=ca+(u[0]*u[0]*omca);
		r[0][1]=(u[0]*u[1]*omca)-u[2]*sa;
		r[0][2]=(u[0]*u[2]*omca)+u[1]*sa;
		r[1][0]=(u[1]*u[0]*omca)+u[2]*sa;
		r[1][1]=ca+(u[1]*u[1]*omca);
		r[1][2]=(u[1]*u[2]*omca)-u[0]*sa;
		r[2][0]=(u[2]*u[0]*omca)-u[1]*sa;
		r[2][1]=(u[2]*u[1]*omca)+u[0]*sa;
		r[2][2]=ca+(u[2]*u[2]*omca);
		double rv[]=matrixMul(r,vec);
		return rv;
	}
	
	private double vectorAngle( double v1[],double v2[]) {
		double ab=vectorLength(v1)*vectorLength(v2);
		double cosfi=( (v1[IDX_X]*v2[IDX_X])+
					   (v1[IDX_Y]*v2[IDX_Y])+
					   (v1[IDX_Z]*v2[IDX_Z])) / ab;
		if( cosfi>=1.0 )		// Shouldn't happen but rounding errors cause it sometimes
					cosfi=1.0;
		if( cosfi<=-1.0 )
					cosfi=-1.0;
		return Math.acos(cosfi);		
	}
	
	private double[] vectorCrossProductU( double a[], double b[]) {
		double cp[] = new double[3];
		cp[IDX_X]= a[IDX_Y]*b[IDX_Z]-a[IDX_Z]*b[IDX_Y];
		cp[IDX_Y]= a[IDX_Z]*b[IDX_X]-a[IDX_X]*b[IDX_Z];
		cp[IDX_Z]= a[IDX_X]*b[IDX_Y]-a[IDX_Y]*b[IDX_X];
		double amp = vectorLength( cp );
		if( amp==0.0 ) {
			cp[IDX_X] = 0.0;
			cp[IDX_Y] = 0.0;
			cp[IDX_Z] = 0.0;
		} else {
			cp[IDX_X] /= amp;
			cp[IDX_Y] /= amp;
			cp[IDX_Z] /= amp;
		}
		return cp;		
	}
	
	private double[] matrixMul( double r[][], double vec[] ) {
		double rv[] = new double[3];
		rv[IDX_X]=r[0][0]*vec[IDX_X]+r[0][1]*vec[IDX_Y]+r[0][2]*vec[IDX_Z];
		rv[IDX_Y]=r[1][0]*vec[IDX_X]+r[1][1]*vec[IDX_Y]+r[1][2]*vec[IDX_Z];
		rv[IDX_Z]=r[2][0]*vec[IDX_X]+r[2][1]*vec[IDX_Y]+r[2][2]*vec[IDX_Z];
		return rv;		
	}	
	
	
	private Fusion fusion = null;
    private final ISService.Stub serviceBinder = 
			new ISService.Stub() {
		public void setCallback( IBinder binder ) {
			fusion = Fusion.Stub.asInterface( binder );
      	}

		public void removeCallback() {
			fusion = null;
		}

		public boolean isSampling() {
			return samplingStarted;
		}

		public void stopSampling() {
			SService.this.stopSampling();
			stopSelf();
		}

		@Override
		public int getState() throws RemoteException {
			return state;
		}

    };
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	class IIR {
		public IIR( double n_coeffs[],double dn_coeffs[] ) {
			this.n_coeffs = n_coeffs;
			this.dn_coeffs = dn_coeffs;
			n_len = n_coeffs.length;
			dn_len = dn_coeffs.length -1;
			n_buf = new double[ n_len ];
			dn_buf = new double[ dn_len ];
			n_ptr = 0;
			dn_ptr = 0;
			for( int i = 0 ; i < n_len ; ++i )
				n_buf[i] = 0.0;
			for( int i = 0 ; i < dn_len ; ++i )
				dn_buf[i] = 0.0;
		}
		
		public double filter( double inp ) {
			n_buf[ n_ptr ] = inp;
			int tmp_ptr = n_ptr;
			double mac = 0.0;
			for(int i = 0 ; i < n_len ; ++i ) {
				mac = mac + n_coeffs[i]*n_buf[tmp_ptr];
				--tmp_ptr;
				if( tmp_ptr < 0 )
					tmp_ptr = n_len - 1;
			}
			n_ptr = ( ++n_ptr ) % n_len;
			tmp_ptr = dn_ptr - 1;
			if( tmp_ptr < 0 )
				tmp_ptr = dn_len -1;
			for(int i = 0 ; i < dn_len ; ++i ) {
				mac = mac - dn_coeffs[i+1]*dn_buf[tmp_ptr];
				--tmp_ptr;
				if( tmp_ptr < 0 )
					tmp_ptr = dn_len - 1;
			}
			dn_buf[dn_ptr] = mac;
			dn_ptr = ( ++dn_ptr ) % dn_len;
			return mac;
		}
		
		double n_coeffs[];
		double dn_coeffs[];
		double n_buf[];
		double dn_buf[];
		int n_ptr;
		int n_len;
		int dn_ptr;
		int dn_len;
	}
	
	

}
