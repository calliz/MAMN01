package com.example.map;

import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Log;

@SuppressWarnings("deprecation")
public class TiltSenser implements SensorListener{

	int interval;
	float y;
    final String tag = "TiltSenser";
    SensorManager sm = null;
    volatile boolean shouldVibrate;
	
	public TiltSenser(int interval, SensorManager sm, boolean shouldVibrate){//Måste få en sensor service från Activityn och sm = sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		this.interval = interval;
		this.sm = sm;
		this.shouldVibrate = shouldVibrate;
		register();
		
	}
	
	public void unregister(){
	        sm.unregisterListener(this);
	}
	
	public void register(){
		sm.registerListener(this, 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
		
	
	public void onSensorChanged(int sensor, float[] values) {
		Log.d(tag, "dhghjfbdbbfhbhdgb");
        synchronized (this) 
        {
            Log.d(tag, "onSensorChanged: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
            this.y = values[1];
        	if(this.y <= interval && this.y >= -interval){
            	shouldVibrate = true;
            }else{
            	shouldVibrate = false;
            }
         }
		
	}
	
	public float getY(){
		return this.y;
	}
	
	public void onAccuracyChanged(int sensor, int accuracy) {

		
	}
	
	
	
	

}
