package cz.muni.fi.filters;

public class LowPassFilter{
	
		private boolean alphaStatic = false;
        // Constants for the low-pass filters
        private float timeConstant = 0.18f;
        private float alpha = 0.1f;
        private float dt = 0;

        // Timestamps for the low-pass filters
        private float timestamp = System.nanoTime();
        private float timestampOld = System.nanoTime();

        private int count = 0;

        // Gravity and linear accelerations components for the
        // Wikipedia low-pass filter
        private double[] output = new double[]
        { 0, 0, 0 };

        // Raw accelerometer data
        private double[] input = new double[]
        { 0, 0, 0 };


        
        public double[] lpFilter(double[] acceleration)
        {
            // Get a local copy of the sensor values
            System.arraycopy(acceleration, 0, this.input, 0, acceleration.length);

            if (!alphaStatic)
            {
                    timestamp = System.nanoTime();

                    // Find the sample period (between updates).
                    // Convert from nanoseconds to seconds
                    dt = 1 / (count / ((timestamp - timestampOld) / 1000000000.0f));

                    // Calculate Wikipedia low-pass alpha
                    alpha = dt / (timeConstant + dt);

            }

            count++;

            if (count > 5)
            {
                    // Update the Wikipedia filter
                    // y[i] = y[i] + alpha * (x[i] - y[i])
                    output[0] = output[0] + alpha * (this.input[0] - output[0]);
                    output[1] = output[1] + alpha * (this.input[1] - output[1]);
                    output[2] = output[2] + alpha * (this.input[2] - output[2]);
            }

            return output;
        }
}

