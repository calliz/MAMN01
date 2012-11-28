package com.example.map;

import java.util.ArrayList;

import org.haptimap.hcimodules.guiding.HapticGuide;
import org.haptimap.hcimodules.guiding.HapticGuideEventListener;
import org.haptimap.hcimodules.util.MyLocationModule;
import org.haptimap.hcimodules.util.WayPoint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
	private Location nextPos;
	private HapticGuide theGuide;

	/** For showing and hiding our notification. */
	NotificationManager mNM;
	/** Keeps track of all current registered clients. */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	/** Holds last value set by a client. */
	int mValue = 0;

	/**
	 * Command to the service to register a client, receiving callbacks from the
	 * service. The Message's replyTo field must be a Messenger of the client
	 * where callbacks should be sent.
	 */
	static final int MSG_REGISTER_CLIENT = 1;

	/**
	 * Command to the service to unregister a client, ot stop receiving
	 * callbacks from the service. The Message's replyTo field must be a
	 * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
	 */
	static final int MSG_UNREGISTER_CLIENT = 2;

	/**
	 * Command to service to set a new value. This can be sent to the service to
	 * supply a new value, and will be sent by the service to any registered
	 * clients with the new value.
	 */
	static final int MSG_SET_NEXT_POSITION = 3;

	/**
	 * Command to service to get a new value. Will be sent by the service to any
	 * registered clients with the new value.
	 */
	static final int MSG_GET_CURRENT_POSITION = 4;

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_NEXT_POSITION:
				mValue = msg.arg1;
				nextPos = toLocationFormat(msg.arg1);
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(
								Message.obtain(null, MSG_SET_NEXT_POSITION,
										mValue, 0));
					} catch (RemoteException e) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					}
				}
				break;
			case MSG_GET_CURRENT_POSITION:
				int currPos = toGeoPointFormat(currentPos);
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(
								Message.obtain(null, MSG_GET_CURRENT_POSITION,
										currPos, 0));
					} catch (RemoteException e) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					}
				}
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

	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// Display a notification about us starting.
		showNotification();

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
		// Cancel the persistent notification.
		mNM.cancel(R.string.remote_service_started);

		// Tell the user we stopped.
		Toast.makeText(this, TAG + " : " + R.string.remote_service_stopped,
				Toast.LENGTH_SHORT).show();

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

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.remote_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.pigeon, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MapViewActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.guidingservice),
				text, contentIntent);

		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.remote_service_started, notification);
	}

	private int toGeoPointFormat(Location currPos) {
		// ENDAST LATITUD FÖR TILLFÄLLET!
//		return (int) (currPos.getLatitude() * 1E6);
		return (int) (80.200 * 1E6);
	}

	private Location toLocationFormat(int nextPos) {
		// ENDAST LATITUD FÖR TILLFÄLLET!
		Location location = new Location("dummyProvider");
		location.setLatitude(nextPos / 1E6);
		location.setLongitude(nextPos / 1E6);
		return location;
	}

}
