package com.example.map;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class TiltListener implements SensorEventListener {
	private float[] mValuesMagnet = new float[3];
	private float[] mValuesAccel = new float[3];
	private float[] mValuesOrientation = new float[3];
	private float[] mRotationMatrix = new float[9];
	private SensorManager sensorManager;
	
	public TiltListener(SensorManager sensorManager){
		this.sensorManager = sensorManager;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		// Handle the events for which we registered
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, mValuesAccel, 0, 3);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, mValuesMagnet, 0, 3);
			break;
		}
		
		SensorManager.getRotationMatrix(mRotationMatrix, null,
				mValuesAccel, mValuesMagnet);
		SensorManager.getOrientation(mRotationMatrix,
				mValuesOrientation);
		if(Math.abs(mValuesOrientation[1])<0.5){
			//Lägg till kod här
			
			Log.e("Angle between points.",Double.toString(CalcAngleFromNorth.calculateAngle(55.709114, 13.167778,55.714976, 13.212644)));
		}else{
			//Annars här
			//Log.e("Not Scanning points.","Not Scanning points.");
		}
	};
}
