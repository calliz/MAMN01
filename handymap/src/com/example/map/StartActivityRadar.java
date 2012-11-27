package com.example.map;

import org.haptimap.hcimodules.guiding.HapticGuide;
import org.haptimap.hcimodules.guiding.HapticGuideEventListener;
import org.haptimap.hcimodules.util.MyLocationModule;
import org.haptimap.hcimodules.util.WayPoint;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class StartActivityRadar extends Activity implements SensorEventListener {
	private ImageView imageView;
	private SensorManager sensorManager;
	private long lastUpdate;
	private Button button1;
	private Button button2;
	private final String TAG = "MyActivity";
	private MyLocationModule myLocation;
	private Location currentPos;
	private HapticGuide theGuide;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);

		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPos = null;

		theGuide = new HapticGuide(this);

		setContentView(R.layout.main);

		button1 = (Button) findViewById(R.id.set);

		button1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				CheckEnableGPS();

				currentPos = myLocation.getCurrentLocation();
				// currentPos.setLatitude(55.600459);
				// currentPos.setLongitude(12.96725);
				if (currentPos == null) {
					Toast.makeText(StartActivityRadar.this,
							"no GPS signal - no position set",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(
							StartActivityRadar.this,
							"location set= " + currentPos.getLatitude() + ", "
									+ currentPos.getLongitude(),
							Toast.LENGTH_SHORT).show();
				}
				Log.i(TAG, "" + myLocation.getCurrentLocation());

			}

		});

		button2 = (Button) findViewById(R.id.guide);

		button2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (currentPos != null) {

					WayPoint goal = new WayPoint("goal", currentPos);

					theGuide.setNextDestination(goal);

					theGuide.onStart();
				} else {
					Toast.makeText(StartActivityRadar.this,
							"no GPS signal - cannot guide", Toast.LENGTH_SHORT)
							.show();
				}
				Log.i(TAG, "guide button");
			}
		});

		theGuide.registerHapticGuideEventListener(new HapticGuideEventListener() {

			public void onRateIntervalChanged(int millis) {

			}

			public void onPrepared(boolean onPrepared) {

			}

			public void onDestinationReached(long[] pattern) {
				Toast.makeText(StartActivityRadar.this, "You have arrived!",
						Toast.LENGTH_SHORT).show();
			}
		});

		imageView = (ImageView) findViewById(R.id.radar_image);
		imageView.setImageResource(R.drawable.radar_2_black);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		lastUpdate = System.currentTimeMillis();
	}

	protected void onPause() {
		// unregister listener
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}

	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z)
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		long actualTime = System.currentTimeMillis();
		if (accelationSquareRoot >= 2) //
		{
			if (actualTime - lastUpdate < 200) {
				return;
			}
			lastUpdate = actualTime;
			Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
					.show();

			Intent myIntent = new Intent(StartActivityRadar.this,
					MapViewActivity.class);
			StartActivityRadar.this.startActivity(myIntent);
		}
	}

	@Override
	protected void onDestroy() {

		myLocation.onDestroy();
		theGuide.onDestroy();

		super.onDestroy();
	}

	private void CheckEnableGPS() {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			// GPS Enabled
			Toast.makeText(StartActivityRadar.this, "GPS Enabled: " + provider,
					Toast.LENGTH_LONG).show();
		} else {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

	}
}
