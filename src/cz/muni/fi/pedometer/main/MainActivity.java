package cz.muni.fi.pedometer.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import cz.muni.fi.pedometer.R;
import cz.muni.fi.pedometer.main.Fusion;
import cz.muni.fi.pedometer.main.ISService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int MENU_RECALIBRATE = 1;
	private static final int MENU_EXPORT_CONFIG = 2;
	public static GraphView graphView = null;
	private boolean samplingServiceRunning = false;
	private SServiceConnection sServiceConnection = null;
	private ISService sService = null;
	private String LOG_TAG = "Main activity";
	private int state = SService.ENGINESTATES_IDLE;
	public String sampleCounterText = null;
	private TextView statusMessageTV;
	private static Button button;
	private TextView stepCounter;
	private int  steps = 1;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
        bindSamplingService();   
        setContentView(R.layout.activity_main); 
        statusMessageTV = (TextView)findViewById( R.id.status );
        graphView = (GraphView)findViewById( R.id.graphView);
        button = (Button)findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        stepCounter = (TextView)findViewById( R.id.steps);
        stepCounter.setText("No Steps taken yet.");
        startSService();  
        
    }
	
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add( Menu.NONE, MENU_RECALIBRATE, Menu.NONE, R.string.recalib );
        menu.add( Menu.NONE, MENU_EXPORT_CONFIG, Menu.NONE, R.string.export_config );
        return result;
    }
    
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
                case MENU_RECALIBRATE: {
              		stopSamplingService();
            		File dir = getFilesDir();
            		File calibFile = new File( dir, SService.CALIB_FILE );
            		calibFile.delete();
           			startSService();
                	return true;            
                }

                case MENU_EXPORT_CONFIG: {
                	exportCalibrationData();
                	return true;
                }
        }
        return false;
    }

	public static Button getButton(){
		return button;
	}

	protected void onDestroy() {
    	super.onDestroy();
    	releaseSService();
    }
        
    private void bindSamplingService() {
    	sServiceConnection = new SServiceConnection();
    	Intent i = new Intent();
    	i.setClassName( "cz.muni.fi.pedometer","cz.muni.fi.pedometer.main.SService" );
    	bindService( i, sServiceConnection, Context.BIND_AUTO_CREATE);
   }
    
    private void startSService() {
    	if( samplingServiceRunning )        // shouldn't happen
    		stopSamplingService();
        Intent i = new Intent();
        i.setClassName( "cz.muni.fi.pedometer","cz.muni.fi.pedometer.main.SService" );
        startService( i );
        samplingServiceRunning = true;
    }
        
    private void releaseSService() {
    	releaseCallbackOnService();
    	unbindService( sServiceConnection );          
    	sServiceConnection = null;
    }
        
    private void stopSamplingService() {
    	Log.d( LOG_TAG, "stopSamplingService" );
    	if( samplingServiceRunning ) {
    		stopSampling();
    		samplingServiceRunning = false;
    		steps = 0;
    	}
    }
        
    private void releaseCallbackOnService() {
    	if( sService == null )
    		Log.e( LOG_TAG, "releaseCallbackOnService: Service not available" );
    	else {
    		try {
    			sService.removeCallback();
    		} catch( DeadObjectException ex ) {
    			Log.e( LOG_TAG, "DeadObjectException",ex );
    		} catch( RemoteException ex ) {
    			Log.e( LOG_TAG, "RemoteException",ex );
    		}
    	}
    }
   
    private void stopSampling() {
            Log.d( LOG_TAG, "stopSampling" );
            if( sService == null )
                    Log.e( LOG_TAG, "stopSampling: Service not available" );
            else {
                    try {
                            sService.stopSampling();
                    } catch( DeadObjectException ex ) {
                            Log.e( LOG_TAG, "DeadObjectException",ex );
                    } catch( RemoteException ex ) {
                            Log.e( LOG_TAG, "RemoteException",ex );
                    }
            }
    }
    
    private void setCallbackOnService() {
            if( sService == null )
                    Log.e( LOG_TAG, "setCallbackOnService: Service not available" );
            else {
                    try {
                            sService.setCallback( fusion.asBinder() );
                    } catch( DeadObjectException ex ) {
                            Log.e( LOG_TAG, "DeadObjectException",ex );
                    } catch( RemoteException ex ) {
                            Log.e( LOG_TAG, "RemoteException",ex );
                    }
            }
    }
    
    private void updateSamplingServiceRunning() {
            if( sService == null )
                    Log.e( LOG_TAG, "updateSamplingServiceRunning: Service not available" );
            else {
                    try {
                            samplingServiceRunning = sService.isSampling();
                    } catch( DeadObjectException ex ) {
                            Log.e( LOG_TAG, "DeadObjectException",ex );
                    } catch( RemoteException ex ) {
                            Log.e( LOG_TAG, "RemoteException",ex );
                    }
            }
    }
    
    private void updateState() {
            if( sService == null )
                    Log.e( LOG_TAG, "updateState: Service not available" );
            else {
                    try {
                            state = sService.getState();
                    } catch( DeadObjectException ex ) {
                            Log.e( LOG_TAG, "DeadObjectException",ex );
                    } catch( RemoteException ex ) {
                            Log.e( LOG_TAG, "RemoteException",ex );
                    }
            }
    }
    
    private String getStateName( int state ) {
                String stateName = null;
                switch( state ) {
                case SService.ENGINESTATES_IDLE:
                	stateName = "Idle";
                	break;
                case SService.ENGINESTATES_WAIT_FOR_TOUCH:
                	stateName = "Lay the device on the horizontal surface and then touch the OK button.";
                	break;
                case SService.ENGINESTATES_STATIONARY_CALIBRATING:
                	stateName = "Processing stationary calibration...";
                	break;
                case SService.ENGINESTATES_COMPASS_CALIBRATING_XNEG:
                	stateName = "Hold the device on the RIGHT edge.";
                	break;
                case SService.ENGINESTATES_COMPASS_CALIBRATING_XPOS:
                	stateName = "Hold the device on the LEFT edge.";
                	break;
                case SService.ENGINESTATES_COMPASS_CALIBRATING_YNEG:
                	stateName = "Hold the device on the UPPER edge.";
                	break;
                case SService.ENGINESTATES_COMPASS_CALIBRATING_YPOS:
                	stateName = "Hold the device on the LOWER edge.";
                	break;
                case SService.ENGINESTATES_COMPASS_CALIBRATING_ZNEG:
                	stateName = "Hold the device SCREEN DOWN.";
                	break;
                case SService.ENGINESTATES_COMPASS_CALIBRATING_ZPOS:
                	stateName = "Hold the device SCREEN UP.";
                	break;                
                case SService.ENGINESTATES_COMPASS_CALIBRATING_FIN:
                	stateName = "Finalizing calibration...";
                	break;
                case SService.ENGINESTATES_MEASURING:
                	stateName = "Sampling";
                	break;
                default:
                	stateName = "N/A";
                	break;
                }
                return stateName;
        }
    
    private void exportCalibrationData() {
		File dir = getFilesDir();
		File calibFile = new File( dir, SService.CALIB_FILE );
  		File exportFile = new File( 
  				Environment.getExternalStorageDirectory(), 
  				SService.CALIB_FILE );
		try {
			BufferedReader rdr = new BufferedReader( new FileReader( calibFile ));
			PrintWriter pw = new PrintWriter( new FileWriter( exportFile ));
			String line = null;
			while( ( line = rdr.readLine() ) != null ) {
				pw.println( line );
			}
			pw.close();
			rdr.close();
		} catch( IOException ex ) {
			Toast.makeText(this, R.string.ioerror, Toast.LENGTH_LONG).show();
		}
    }
    
    private Fusion.Stub fusion = new Fusion.Stub() {
            @Override
            public void sampleCounter(int count) throws RemoteException {
                    Log.d( LOG_TAG, "sample count: "+count );
                    sampleCounterText = Integer.toString( count );

            }

            public void statusMessage( int newState ) {
                    Log.d(LOG_TAG, "statusMessage: "+newState );
                    if( newState < SService.ENGINESTATES_TRANSIENT ) {
                            state = newState;
                            if( statusMessageTV  != null ) {
                                    statusMessageTV.setText( getStateName( newState ) );
                            }
                    }
                    switch( newState ) {
                            case SService.ENGINESTATES_IDLE:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_WAIT_FOR_TOUCH:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.VISIBLE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_STATIONARY_CALIBRATING:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_XNEG:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_XPOS:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_YNEG:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_YPOS:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_ZNEG:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_ZPOS:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_COMPASS_CALIBRATING_FIN:
                            	graphView.setVisibility( View.GONE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.GONE);
                            	break;
                            case SService.ENGINESTATES_MEASURING:
                            	graphView.setVisibility( View.VISIBLE);
                            	button.setVisibility(View.GONE);
                            	stepCounter.setVisibility(View.VISIBLE);
                            	break;
                    }
            }
            @Override
            public void draw(int type, int sensorType, double[] values  ) throws RemoteException {
                    if( graphView != null ) {
                            SurfaceHolder holder = graphView.getHolder();
                            Canvas c = holder.lockCanvas();
                            if( c != null ) {
                                    float[] vals = {(float) values[0], (float) values[1], (float) values[2]};
                                    graphView.drawGraph(c, sensorType, vals);
                                    holder.unlockCanvasAndPost(c);
                            }
                    }
            }
            
            @Override
            public void displayStepDetected(){
            	stepCounter.setText(steps + " steps taken.");
            	steps++;
            }
    };

    class SServiceConnection implements ServiceConnection {
            public void onServiceConnected(ComponentName className,IBinder boundService ) {
                    Log.d( LOG_TAG, "onServiceConnected" );                
                    sService = ISService.Stub.asInterface((IBinder)boundService);
                    setCallbackOnService();
                    updateSamplingServiceRunning();
                    updateState();
                    if( statusMessageTV  != null ) {
                            statusMessageTV.setText( getStateName( state ) );
                    }
                    if( state == SService.ENGINESTATES_MEASURING )
                            graphView.setVisibility( View.VISIBLE);
                    Log.d( LOG_TAG,"onServiceConnected" );
            }
          
            public void onServiceDisconnected(ComponentName className) {
                    sService = null;
                    Log.d( LOG_TAG,"onServiceDisconnected" );
        }        
    };        

}
