package cz.muni.fi.myapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;

	public class MainActivity extends Activity {
		public static GraphView graphView = null;
		private boolean samplingServiceRunning = false;
		private SServiceConnection sServiceConnection = null;
		private ISService sService = null;
		private String LOG_TAG = "Main activity";
		private int state = SService.ENGINESTATES_IDLE;
		private TextView sampleCounterTV;
		public String sampleCounterText = null;
		private TextView statusMessageTV;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
        bindSamplingService();
        setContentView(R.layout.activity_main); 
		statusMessageTV = (TextView)findViewById( R.id.status );
        sampleCounterTV = (TextView)findViewById( R.id.counter);
		graphView = (GraphView)findViewById( R.id.graphView);
        startSamplingService();  
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	releaseSamplingService();
    }
        
    private void bindSamplingService() {
    	sServiceConnection = new SServiceConnection();
    	Intent i = new Intent();
    	i.setClassName( "cz.muni.fi.myapp","cz.muni.fi.myapp.SService" );
    	bindService( i, sServiceConnection, Context.BIND_AUTO_CREATE);
   }
    
    private void startSamplingService() {
    	if( samplingServiceRunning )        // shouldn't happen
    		stopSamplingService();
        Intent i = new Intent();
        i.setClassName( "cz.muni.fi.myapp","cz.muni.fi.myapp.SService" );
        startService( i );
        samplingServiceRunning = true;
    }
        
    private void releaseSamplingService() {
    	releaseCallbackOnService();
    	unbindService( sServiceConnection );          
    	sServiceConnection = null;
    }
        
    private void stopSamplingService() {
    	Log.d( LOG_TAG, "stopSamplingService" );
    	if( samplingServiceRunning ) {
    		stopSampling();
    		samplingServiceRunning = false;
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
		case SService.ENGINESTATES_STATIONARY_CALIBRATING:
			stateName = "Stationary calibration";
			break;
		case SService.ENGINESTATES_COMPASS_CALIBRATING:
			stateName = "Compass calibration";
			break;
		case SService.ENGINESTATES_MEASURING:
			stateName = "Measuring";
			break;
		default:
			stateName = "N/A";
			break;
		}
		return stateName;
	}
    
    private Fusion.Stub fusion = new Fusion.Stub() {
    	@Override
    	public void sampleCounter(int count) throws RemoteException {
    		Log.d( LOG_TAG, "sample count: "+count );
    		sampleCounterText = Integer.toString( count );
    		sampleCounterTV.setText( sampleCounterText );
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
    				break;
    			case SService.ENGINESTATES_STATIONARY_CALIBRATING:
    				graphView.setVisibility( View.GONE);
    				break;
    			case SService.ENGINESTATES_COMPASS_CALIBRATING:
    				graphView.setVisibility( View.GONE);
    				break;
    			case SService.ENGINESTATES_MEASURING:
    				graphView.setVisibility( View.VISIBLE);
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
