package com.example.map;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TiltListener implements SensorEventListener {
	private float[] mValuesMagnet = new float[3];
	private float[] mValuesAccel = new float[3];
	private float[] mValuesOrientation = new float[3];
	private float[] mRotationMatrix = new float[9];
	private SensorManager sensorManager;
	private Tiltable tiltable;
	
	public TiltListener(SensorManager sensorManager, Tiltable tiltable) {
		this.sensorManager = sensorManager;
		this.tiltable = tiltable;
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

		SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel,
				mValuesMagnet);
		SensorManager.getOrientation(mRotationMatrix, mValuesOrientation);

		if (Math.abs(mValuesOrientation[1]) < 0.5) {
			tiltable.setTilted(true);
		} else {
			tiltable.setTilted(false);
		}
	};
}
