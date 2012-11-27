package com.example.map;

import java.util.ArrayList;
import java.util.List;

import org.haptimap.hcimodules.guiding.HapticGuide;
import org.haptimap.hcimodules.guiding.HapticGuideEventListener;
import org.haptimap.hcimodules.util.MyLocationModule;
import org.haptimap.hcimodules.util.WayPoint;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GuidingService extends Service implements SensorEventListener {

	private static final String TAG = GuidingService.class.getSimpleName();

	private SensorManager sensorManager;
	private long lastUpdate;
	private MyLocationModule myLocation;
	private Location currentPos;
	private HapticGuide theGuide;

	// private Timer timer;
	//
	// private TimerTask updateTask = new TimerTask() {
	// @Override
	// public void run() {
	// Log.i(TAG, "Timer task doing work");
	// }
	// };

	private List<GuidingServiceListener> listeners = new ArrayList<GuidingServiceListener>();

	private GuidingServiceApi.Stub apiEndpoint = new GuidingServiceApi.Stub() {

		public void addListener(GuidingServiceListener listener)
				throws RemoteException {

			synchronized (listeners) {
				listeners.add(listener);
			}
		}

		public void removeListener(GuidingServiceListener listener)
				throws RemoteException {

			synchronized (listeners) {
				listeners.remove(listener);
			}
		}

	};

	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");

		// timer = new Timer("TweetCollectorTimer");
		// timer.schedule(updateTask, 1000L, 1000L);

		checkEnableGPS();

		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPos = null;

		theGuide = new HapticGuide(this);

		fetchAndSetCurrentPosition();

		guideToSavedPosition();

		theGuide.registerHapticGuideEventListener(new HapticGuideEventListener() {

			public void onRateIntervalChanged(int millis) {

			}

			public void onPrepared(boolean onPrepared) {

			}

			public void onDestinationReached(long[] pattern) { //
				Toast.makeText(GuidingService.this, "You have arrived!", //
						Toast.LENGTH_SHORT).show();
				Log.i(TAG, "onDestinationReached!");
			}
		});

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// register this class as a listener for the orientation and
		// accelerometer sensors
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		lastUpdate = System.currentTimeMillis();
	}

	private void guideToSavedPosition() {
		if (currentPos != null) {

			WayPoint goal = new WayPoint("goal", currentPos);

			theGuide.setNextDestination(goal);

			theGuide.onStart();
		} else {
			Toast.makeText(GuidingService.this, "no GPS signal - cannot guide",
					Toast.LENGTH_SHORT).show();
		}
		// Log.i(TAG, "guide button");
	}

	private void fetchAndSetCurrentPosition() {
		currentPos = myLocation.getCurrentLocation();
		// currentPos.setLatitude(55.600459);
		// currentPos.setLongitude(12.96725);
		if (currentPos == null) {
			Toast.makeText(GuidingService.this,
					"no GPS signal - no position set", Toast.LENGTH_SHORT)
					.show();

		} else {
			Toast.makeText(
					GuidingService.this,
					"location set= " + currentPos.getLatitude() + ", "
							+ currentPos.getLongitude(), Toast.LENGTH_SHORT)
					.show();
		}
		Log.i(TAG, "" + myLocation.getCurrentLocation());
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {
		// Log.i(TAG, "onSensorChanged");
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
			Toast.makeText(this, "A new position has been chosen",
					Toast.LENGTH_SHORT).show();

			// UPDATE POSITION ON RADARMAP AND START GUIDING WITH A DETAILED
			// MAPVIEW

			// Intent myIntent = new Intent(StartActivityRadar.this,
			// MapViewActivity.class);
			// StartActivityRadar.this.startActivity(myIntent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");
		// AVREGISTRERA SENSORLYSSNARE???
		myLocation.onDestroy();
		theGuide.onDestroy();

		// timer.cancel();
		// timer = null;
	}

	private void checkEnableGPS() {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (provider.equals(LocationManager.NETWORK_PROVIDER + ","
				+ LocationManager.GPS_PROVIDER)
				|| provider.equals(LocationManager.GPS_PROVIDER)) {
			// GPS Enabled
			Toast.makeText(GuidingService.this, "GPS Enabled: " + provider,
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(GuidingService.this, "GPS not Enabled: " + provider,
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
