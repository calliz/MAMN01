package com.example.map;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.hardware.SensorManager;
import android.hardware.SensorListener;

@SuppressWarnings("deprecation")
public class SensorTest extends Activity implements SensorListener {
        
        final String tag = "SensorData";
        SensorManager sm = null;
        volatile boolean shouldVibrate;
        int interval;
        float y;
      
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//
    	shouldVibrate = false;
    	interval = 30;
    	//
        super.onCreate(savedInstanceState);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.main);

    }
    
    public void onSensorChanged(int sensor, float[] values) 
    {
        synchronized (this) 
        {
            try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            this.y = values[1];
        	if(this.y <= interval && this.y >= -interval){
            	shouldVibrate = true;
            	Log.d(tag, "true y = " + y);
            }else{
            	shouldVibrate = false;
            	Log.d(tag, "false y = " + y);
            }
        	
            //Log.d(tag, "onSensorChanged: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
            
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
