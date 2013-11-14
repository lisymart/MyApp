package cz.muni.fi.myapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);         
        setContentView(R.layout.activity_main);
        
    }

    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onStop() {
            super.onStop();
    }
    
    protected void onPause() {
            super.onPause();
    }
   

}
