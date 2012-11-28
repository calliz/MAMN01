package com.example.map;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class GuidingService extends Service implements SensorEventListener {

	private static final String TAG = GuidingService.class.getSimpleName();

	/** Command to the service to display a message */
	static final int MSG_SAY_HELLO = 1;

	private SensorManager sensorManager;
	private long lastUpdate;
	private MyLocationModule myLocation;
	private Location currentPos;
	private Location nextPos;
	private HapticGuide theGuide;

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SAY_HELLO:
				Log.i(TAG, "Hello from client");
				Toast.makeText(getApplicationContext(), "Hello from Client",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(getApplicationContext(), "Binding client to service",
				Toast.LENGTH_SHORT).show();
		Log.i(TAG, "Binding client to service");
		return mMessenger.getBinder();
	}

	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service creating");

		checkEnableGPS();

		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPos = null;

		nextPos = null;

		theGuide = new HapticGuide(this);

		fetchAndSetCurrentPosition();

		guideToSavedPosition();

		theGuide.registerHapticGuideEventListener(new HapticGuideEventListener() {

			public void onRateIntervalChanged(int millis) {

			}

			public void onPrepared(boolean onPrepared) {

			}

			public void onDestinationReached(long[] pattern) { //
				// Toast.makeText(GuidingService.this, "You have arrived!", //
				// Toast.LENGTH_SHORT).show();
				Log.i(TAG, "You have arrived at your final destination!!!");
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
		if (nextPos != null) {

			WayPoint goal = new WayPoint("goal", nextPos);

			theGuide.setNextDestination(goal);

			theGuide.onStart();
		} else {
			// Toast.makeText(GuidingService.this,
			// "no GPS signal - cannot guide",
			// Toast.LENGTH_SHORT).show();
			Log.i(TAG, "no GPS signal - cannot guide");
		}
		// Log.i(TAG, "guide button");
	}

	private void fetchAndSetCurrentPosition() {
		currentPos = myLocation.getCurrentLocation();
		// currentPos.setLatitude(55.600459);
		// currentPos.setLongitude(12.96725);
		if (currentPos == null) {
			// Toast.makeText(GuidingService.this,
			// "No GPS signal - no current position set",
			// Toast.LENGTH_SHORT).show();
			Log.i(TAG, "No GPS signal - no current position set");

		} else {
			// Toast.makeText(
			// GuidingService.this,
			// "Current location set to: " + currentPos.getLatitude()
			// + ", " + currentPos.getLongitude(),
			// Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Current location set to: " + currentPos.getLatitude()
					+ ", " + currentPos.getLongitude());
		}
		// Log.i(TAG, "" + myLocation.getCurrentLocation());
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
			// Toast.makeText(this, "A new position has been chosen",
			// Toast.LENGTH_SHORT).show();
			//
			// Log.i(TAG, "" + myLocation.getCurrentLocation());

			// UPDATE POSITION ON RADARMAP AND START GUIDING WITH A DETAILED
			// MAPVIEW - probably NOT!

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
			// Toast.makeText(GuidingService.this, "GPS Enabled: " + provider,
			// Toast.LENGTH_LONG).show();
			Log.i(TAG, "GPS Enabled: " + provider);
		} else {
			// Toast.makeText(GuidingService.this, "GPS not Enabled: " +
			// provider,
			// Toast.LENGTH_LONG).show();
			Log.i(TAG, "GPS not Enabled: " + provider);
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		}

	}

}
