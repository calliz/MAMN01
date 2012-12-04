package com.example.map;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassListener implements SensorEventListener {
	private float[] mValuesMagnet = new float[3];
	private float[] mValuesAccel = new float[3];
	private float[] mValuesOrientation = new float[3];
	private float[] mRotationMatrix = new float[9];
	private SensorManager sensorManager;
	private MapViewActivity mapView;

	public CompassListener(SensorManager sensorManager, MapViewActivity mapView) {
		this.sensorManager = sensorManager;
		this.mapView = mapView;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		// Handle the events for which we registered
		double azimut = 0;
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

		// Lägg till kod här
		float R[] = new float[9];
		float I[] = new float[9];
		boolean success = SensorManager.getRotationMatrix(R, I, mValuesAccel,
				mValuesMagnet);
		if (success) {
			float orientation[] = new float[3];
			SensorManager.getOrientation(R, orientation);
			azimut = orientation[0] * 180.0 / Math.PI;
			if (azimut < 0.0)
				azimut += 360.0;
			else if (azimut > 360.0)
				azimut -= 360;

			mapView.setBearing(azimut);
		}
	};
}
