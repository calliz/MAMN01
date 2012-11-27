package com.example.map;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.SensorManager;
import android.hardware.SensorListener;

@SuppressWarnings("deprecation")
public class CopyOfSensorTest extends Activity implements SensorListener {
        
        final String tag = "IBMEyes";
        SensorManager sm = null;
      
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.main);

    }
    
    public void onSensorChanged(int sensor, float[] values) 
    {
        synchronized (this) 
        {
            Log.d(tag, "onSensorChanged: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
            if (sensor == SensorManager.SENSOR_ORIENTATION){
     
            }
         }
      
    }
    
    public void onAccuracyChanged(int sensor, int accuracy) 
    {
        Log.d(tag,"onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
        
    }
 

    @Override
    protected void onResume() 
    {
        super.onResume();
        sm.registerListener(this, 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    
   
    
    
}
