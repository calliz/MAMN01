package com.example.map;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.hardware.SensorManager;
import android.hardware.SensorListener;

public class TiltSensor implements SensorEventListener{
	
	
	public TiltSensor(){
//		SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
//		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
//                SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	
	boolean checkIfInHorisontalInterval(){
		
		float[] mValuesOrientation = new float[3];
		float[] mRotationMatrix = new float[3];

		
		
		
		return false;
		
		
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	
	

}
