package com.example.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

	private static SensorManager sensorManager;
	private MyCompassView compassView;
	private Sensor accelerometer;
	private Sensor magnetometer;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		compassView = new MyCompassView(this);
		setContentView(compassView);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		if (accelerometer == null && magnetometer == null) {
			Toast.makeText(this, "Sensors not found", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer,
				SensorManager.SENSOR_DELAY_UI);

	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void onSensorChanged(SensorEvent event) {
		// angle between the magnetic north directio
		// 0=North, 90=East, 180=South, 270=West
		Toast.makeText(this, "onSensorChanged", Toast.LENGTH_SHORT).show();
		float azimuth = event.values[0];
		compassView.updateData(azimuth);
	}

}