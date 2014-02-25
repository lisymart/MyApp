package cz.muni.fi.myapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import cz.muni.fi.filters.CumulativeSignalPowerTD;
import cz.muni.fi.filters.MovingAverageTD;

/**
 * MovingAverageStepDetector class, step detection filter based on two moving averages
 * with minimum signal power threshold 
 * 
 * @author Michal Holcik
 * 
 */
public class MovingAverageStepDetector extends StepDetector {
        
        private float[] maValues;
        private MovingAverageTD[] ma;
        private CumulativeSignalPowerTD asp;
        private boolean mMASwapState;
        private boolean stepDetected;
        private boolean signalPowerCutoff;
        private long mLastStepTimestamp;
        private double strideDuration;
        
        private static final long SECOND_IN_NANOSECONDS = (long) Math.pow(10, 9);
        public static final double MA1_WINDOW = 0.2;
        public static final double MA2_WINDOW = 5 * MA1_WINDOW;
        private static final long POWER_WINDOW = SECOND_IN_NANOSECONDS / 10;
        private static final double MAX_STRIDE_DURATION = 2.0; // in seconds
        private static final double  MIN_STRIDE_DURATION = 0.6;
        
        public static final float POWER_CUTOFF_VALUE = 1000.0f;
        
        private double mWindowMa1;
        private double mWindowMa2;
        private long mWindowPower;
        private static float mPowerCutoff;
        
        public MovingAverageStepDetector() {
                this(MA1_WINDOW, MA2_WINDOW, POWER_CUTOFF_VALUE);
        }

        public MovingAverageStepDetector(double windowMa1, double windowMa2, double powerCutoff) {
                
                mWindowMa1 = windowMa1;
                mWindowMa2 = windowMa2;
                mPowerCutoff = (float)powerCutoff;
                
                maValues = new float[4];
                mMASwapState = true;
                ma = new MovingAverageTD[] { new MovingAverageTD(mWindowMa1), new MovingAverageTD(mWindowMa1), new MovingAverageTD(mWindowMa2) };
                asp = new CumulativeSignalPowerTD();
                stepDetected = false;
                signalPowerCutoff = true;
        }

        public class MovingAverageStepDetectorState {
                float[] values;
                public boolean[] states;
                double duration;

                MovingAverageStepDetectorState(float[] values, boolean[] states, double duration) {
                        this.values = values;
                        this.states = states;
                }
        }

        public MovingAverageStepDetectorState getState() {
                return new MovingAverageStepDetectorState(new float[] { maValues[0],
                                maValues[1], maValues[2], maValues[3] }, new boolean[] {
                                stepDetected, signalPowerCutoff }, strideDuration);
        }

        public float getPowerThreshold() {
                return mPowerCutoff;
        }
        
		public float processAccelerometerValues(long timestamp, float[] values) {

                float value = (float) Math.sqrt(values[0] * values[0] +  values[1] * values[1] + values[2] * values[2]);

                // compute moving averages
                maValues[0] = value;
                for (int i = 1; i < 3; i++) {
                        ma[i].push(timestamp, value);
                        maValues[i] = (float) ma[i].getAverage();
                        value = maValues[i];
                }

                // detect moving average crossover
                stepDetected = false;
                boolean newSwapState = maValues[1] > maValues[2] && maValues[1]>1;
                if (newSwapState != mMASwapState) {
                        mMASwapState = newSwapState;
                        if (mMASwapState) {
                                stepDetected = true;
                        }
                }

                // compute signal power
                asp.push(timestamp, maValues[1] - maValues[2]);
                // maValues[3] = (float)sp.getPower();
                maValues[3] = (float) asp.getValue();
                signalPowerCutoff = maValues[3] < mPowerCutoff;

                if (stepDetected) {
                        asp.reset();
                }

             // step event
        		if (stepDetected && !signalPowerCutoff) {
        			final long strideDuration = getStrideDuration(timestamp);
        			if ( MIN_STRIDE_DURATION < strideDuration && strideDuration< MAX_STRIDE_DURATION) {
        				notifyOnStep(new StepEvent(1.0, strideDuration));
        				mLastStepTimestamp = timestamp;
        			} else {
        				signalPowerCutoff = true; //jen kvůli správnému obarvování v StepDetectionDemo
        				mLastStepTimestamp = 0; //jinak by čas od posledního kroku stále rostl a vše by bylo false
        			}
        		}

                return maValues[1];
        }

        /* 
    	 * @return stride duration
    	 */
    	private long getStrideDuration(long currentTimestamp) {
    		if (mLastStepTimestamp == 0) {
    			mLastStepTimestamp = currentTimestamp;
    		}
    		
    		return currentTimestamp - mLastStepTimestamp;
    	}

        @Override
        public void onSensorChanged(SensorEvent event) {
                synchronized (this) {
                        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                                processAccelerometerValues(event.timestamp, event.values);
                        }
                }
        }

}
