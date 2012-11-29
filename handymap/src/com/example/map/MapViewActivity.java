package com.example.map;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.haptimap.hcimodules.guiding.HapticGuide;
import org.haptimap.hcimodules.guiding.HapticGuideEventListener;
import org.haptimap.hcimodules.util.MyLocationModule;
import org.haptimap.hcimodules.util.WayPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MapViewActivity extends MapActivity implements SensorEventListener {

	private static final String SAVED_STATE_COMPASS_MODE = "com.touchboarder.example.modecompass";
	@SuppressWarnings("unused")
	private final String TAG = RoadMapActivity.class.getSimpleName();
	private MapView mapView;
	private MapController mc;
	private boolean mModeCompass = false;
	private MyLocationOverlay mMyLocationOverlay = null;
	private SensorManager compassSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;
	private GeoPoint userPoint;

	private SensorManager confirmSensorManager;
	private long lastUpdate;

	private MyLocationModule myLocation;
	private Location currentPos;
	private Location nextPos;
	private HapticGuide theGuide;

	// new

	/** Messenger for communicating with the service. */
	Messenger mService = null;

	/** Flag indicating whether we have called bind on the service. */
	boolean mBound;

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GuidingService.MSG_SET_NEXT_LATITUDE:
				// Toast.makeText(getApplicationContext(),
				// "Received from service: " + msg.arg1,
				// Toast.LENGTH_SHORT).show();
				break;
			case GuidingService.MSG_GET_CURRENT_POSITION:
				// Toast.makeText(getApplicationContext(),
				// "Received from service: " + msg.arg1,
				// Toast.LENGTH_SHORT).show();
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
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			// We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			// Toast.makeText(getApplicationContext(), "Attached",
			// Toast.LENGTH_SHORT).show();

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						GuidingService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

				// Give it some value as an example.
				// msg = Message.obtain(null,
				// GuidingService.MSG_GET_CURRENT_POSITION, 0, 0);

				// msg = Message.obtain(null, GuidingService.MSG_SET_VALUE,
				// (int) (55.698377 * 1E6), (int) (13.216635 * 1E6), 0);
				// mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be
				// restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			// Toast.makeText(getApplicationContext(),
			// "Remote service connected",
			// Toast.LENGTH_SHORT).show();
			// Log.i("MapViewActivity", "Remote service connected");

			// MapView mapView = (MapView) findViewById(R.id.mapview);
			//
			// sendChosenPositionToService(mapView);
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;

			// As part of the sample, tell the user what happened.
			// Toast.makeText(getApplicationContext(),
			// "Remote service disconnected", Toast.LENGTH_SHORT).show();
			Log.i("MapViewActivity", "Remote service disconnected");
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(getApplicationContext(), GuidingService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mBound = true;
		// Toast.makeText(getApplicationContext(), "Binding",
		// Toast.LENGTH_SHORT)
		// .show();
	}

	void doUnbindService() {
		if (mBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							GuidingService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mBound = false;
			// Toast.makeText(getApplicationContext(), "Unbinding",
			// Toast.LENGTH_SHORT).show();
		}
	}

	// public void sendChosenPositionToService(View v) {
	// if (!mBound)
	// return;
	// // Create and send a message to the service, using a supported 'what'
	// // value
	// Message msg = Message.obtain(null, GuidingService.MSG_SAY_HELLO, 0, 0);
	// try {
	// mService.send(msg);
	// } catch (RemoteException e) {
	// e.printStackTrace();
	// }
	// }

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to the service
//		doBindService();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view_activity);
		compassSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
		mRotateView = new RotateView(this);
		// end new

		/* Added by CALLE */
		// Toast.makeText(getApplicationContext(), "Not attached",
		// Toast.LENGTH_SHORT).show();
		startHapticGuide();

		mapView = (MapView) findViewById(R.id.mapview);
		mMyLocationOverlay = new MyLocationOverlay(this, mapView);

		mapView.setBuiltInZoomControls(false);

		mc = mapView.getController();
		// ArrayList<GeoPoint> all_geo_points = getDirections(55.70462000000001,
		// 13.191360, 55.604640, 13.00382);
		ArrayList<GP> all_geo_points = new ArrayList<GP>();
		addGeoPoints(all_geo_points);

		// Skickar slutposition till GuidingService
		// sendPointToGuidingService(all_geo_points.get(4));

		GeoPoint moveTo = new GeoPoint(all_geo_points.get(0).getLatE6(),
				all_geo_points.get(0).getLongiE6());
		mc.animateTo(moveTo);// ska ha current location
		mc.setZoom(14);
		// mapView.getOverlays().add(new RoadOverlay(all_geo_points));//For the
		// next view
		// createRightZoomLevel(mc, all_geo_points);
		int nbrOfCircles = 3;
		GP currentLocation = all_geo_points.get(0);
		blackBackround(mapView, currentLocation);
		addCircles(mapView, all_geo_points, nbrOfCircles, currentLocation);
		addLocationMarkers(mapView, all_geo_points);
		// mc.animateTo(new GeoPoint(latitudeE6, longitudeE6));

		if (savedInstanceState != null) {
			mModeCompass = savedInstanceState.getBoolean(
					SAVED_STATE_COMPASS_MODE, false);
		}

		confirmSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// register this class as a listener for the orientation and
		// accelerometer sensors
		confirmSensorManager.registerListener(this, confirmSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);

		lastUpdate = System.currentTimeMillis();
	}

	private void startHapticGuide() {
		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPos = null;

		nextPos = null;

		theGuide = new HapticGuide(this);

		fetchAndSetCurrentPosition();

		

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

	}

	private void guideToSavedPosition() {
//		nextPos = GeoToLocation(new GeoPoint(55705248, 13186763));

		if (currentPos != null) {

			WayPoint goal = new WayPoint("goal", currentPos);

			theGuide.setNextDestination(goal);

			theGuide.onStart();
			Log.i(TAG,
					"Guiding to " + currentPos.getLatitude() + ", "
							+ currentPos.getLongitude());
		} else {
			// Toast.makeText(GuidingService.this,
			// "no GPS signal - cannot guide",
			// Toast.LENGTH_SHORT).show();
			Log.i(TAG, "no GPS signal - cannot guide");
		}
		// Log.i(TAG, "guide button");
	}

	private Location GeoToLocation(GeoPoint gp) {
		Location location = new Location("dummyProvider");
		location.setLatitude(gp.getLatitudeE6() / 1E6);
		location.setLongitude(gp.getLongitudeE6() / 1E6);
		return location;
	}

	private void fetchAndSetCurrentPosition() {
		// currentPos = myLocation.getCurrentLocation();
//		Location tmpPos = myLocation.getCurrentLocation();

		// Lund central
		currentPos = GeoToLocation(new GeoPoint(55705249,13186836));

		// currentPos.setLatitude(55.600459);
		// currentPos.setLongitude(12.96725);
		if (myLocation.getCurrentLocation() == null) {
			 Toast.makeText(MapViewActivity.this,
			 "No GPS signal - using Designcentrum IKDC fixed position instead",
			 Toast.LENGTH_SHORT).show();
			Log.i(TAG, "No GPS signal - no current position set");

		} else {
//			Toast.makeText(
//					MapViewActivity.this,
//					"Current location set to: " + currentPos.getLatitude()
//							+ ", " + currentPos.getLongitude(),
//					Toast.LENGTH_SHORT).show();
//			Log.i(TAG, "Current location set to: " + currentPos.getLatitude()
//					+ ", " + currentPos.getLongitude());
			Toast.makeText(MapViewActivity.this,
					 "GPS signal is good - current position is set",
					 Toast.LENGTH_SHORT).show();
			
		}
		// Log.i(TAG, "" + myLocation.getCurrentLocation());
	}

	public void blackBackround(MapView mapView, GP currentLocation) {
		mapView.getOverlays().add(
				new BlackOverlay(null, currentLocation.getLat(),
						currentLocation.getLongi(), 4000));
	}

	public void addCircles(MapView mapView, ArrayList<GP> all_gp,
			int nbrOfCircles, GP currentLocation) {

		int radius = getRadius(all_gp, currentLocation);
		int step = radius / nbrOfCircles;
		step *= 90;// 85

		for (int i = 1; i <= nbrOfCircles; i++) {
			mapView.getOverlays().add(
					new CircleOverlay(null, currentLocation.getLongi(),
							currentLocation.getLat(), step * i));
		}

	}

	private int getRadius(ArrayList<GP> all_gp, GP currentLocation) {

		int longestDistance = 0;

		for (GP item : all_gp) {
			int lat = (int) item.getLat();
			int lon = (int) item.getLongi();

			int thisDistance = (int) Math.sqrt(lat * lat + lon * lon);
			if (thisDistance > longestDistance) {

				longestDistance = thisDistance;
			}
		}
		Log.d("getRadius", "Radius = " + longestDistance);
		return longestDistance;
	}

	private void addLocationMarkers(MapView mapView,
			ArrayList<GP> all_geo_points) {
		int radius = 200;
		GP currentLocation = all_geo_points.get(0);
		mapView.getOverlays().add(
				new CurrentPositionOverlay(null, currentLocation.getLat(),
						currentLocation.getLongi(), radius - 50));// currentposition

		for (GP point : all_geo_points) {
			if (point != currentLocation) {
				mapView.getOverlays().add(
						new LocationOverlay(null, point.getLat(), point
								.getLongi(), radius));
			}
		}

	}

	private void addGeoPoints(ArrayList<GP> all_geo_points) {// 55.70462000000001,
																// 13.191360
		all_geo_points.add(new GP(55.714721, 13.212725)); // Designcentrum IKDC
		// all_geo_points.add(new GP(55.594958,12.972125)); // Major
		// Nilssonsgatan Malmö
		all_geo_points.add(new GP(55.721056, 13.21277));
		all_geo_points.add(new GP(55.7216,13.219979));
		all_geo_points.add(new GP(55.724313, 13.204009));
		all_geo_points.add(new GP(55.698377, 13.216635));
		all_geo_points.add(new GP(55.705248, 13.186763));// Lund centralstation
		// all_geo_points.add(new GP(55.599141,12.983584));// Kronprinsen malmö
	}

	public void createRightZoomLevel(MapController mc,
			ArrayList<GP> all_geo_points) {

		int minLatitude = Integer.MAX_VALUE;
		int maxLatitude = Integer.MIN_VALUE;
		int minLongitude = Integer.MAX_VALUE;
		int maxLongitude = Integer.MIN_VALUE;

		// Find the boundaries of the item set
		for (GP item : all_geo_points) { // item Contain list of Geopints
			int lat = item.getLatE6();
			int lon = item.getLongiE6();

			maxLatitude = Math.max(lat, maxLatitude);
			minLatitude = Math.min(lat, minLatitude);
			maxLongitude = Math.max(lon, maxLongitude);
			minLongitude = Math.min(lon, minLongitude);
		}
		mc.zoomToSpan(Math.abs(maxLatitude - minLatitude),
				Math.abs(maxLongitude - minLongitude));

		// mc.animateTo(new GeoPoint((maxLatitude + minLatitude)/2,
		// (maxLongitude + minLongitude)/2 ));

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	// new
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_compass:
			mMyLocationOverlay.isCompassEnabled();
			toogleRotateView(mModeCompass);
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private void toogleRotateView(boolean compassMode) {
		if (compassMode) {
			compassSensorManager.unregisterListener(mRotateView);
			mRotateView.removeAllViews();
			mRotateViewContainer.removeAllViews();
			mRotateViewContainer.addView(mapView);
			mMyLocationOverlay.disableCompass();
			mModeCompass = false;
		} else {
			mRotateViewContainer.removeAllViews();
			mRotateView.removeAllViews();
			mRotateView.addView(mapView);
			mRotateViewContainer.addView(mRotateView);
			mapView.setClickable(true);
			compassSensorManager.registerListener(mRotateView,
					SensorManager.SENSOR_ORIENTATION,
					SensorManager.SENSOR_DELAY_UI);
			mMyLocationOverlay.enableCompass();
			mModeCompass = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		theGuide.onResume();
		toogleRotateView(!mModeCompass);
		ToggleButton toggleCompassButton = (ToggleButton) findViewById(R.id.button_compass);
		toggleCompassButton.setChecked(mModeCompass);

		// shows the my location dot centered on your last known location
		mMyLocationOverlay.enableMyLocation();

		if (userPoint == null)
			mMyLocationOverlay.runOnFirstFix(new Runnable() {
				public void run() {
					userPoint = mMyLocationOverlay.getMyLocation();
					// if(userPoint!=null)
					// mc.animateTo(userPoint);

				}
			});
		// else mc.animateTo(userPoint);*/
	}

	@Override
	public void onPause() {
		super.onPause();
		mMyLocationOverlay.disableCompass();
		theGuide.onPause();
	}

	// Called during the activity life cycle,
	// when instance state should be saved/restored
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save instance-specific state
		super.onSaveInstanceState(outState);
		// remember the compass mode state
		outState.putBoolean(SAVED_STATE_COMPASS_MODE, mModeCompass);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStop() {
		compassSensorManager.unregisterListener(mRotateView);
		mMyLocationOverlay.disableMyLocation();
//		doUnbindService();
		super.onStop();
	}

	public static ArrayList<GeoPoint> getDirections(double lat1, double lon1,
			double lat2, double lon2) {

		String url = "http://maps.googleapis.com/maps/api/directions/xml?origin="
				+ lat1
				+ ","
				+ lon1
				+ "&destination="
				+ lat2
				+ ","
				+ lon2
				+ "&sensor=false&units=metric&mode=walking";

		String tag[] = { "lat", "lng" };

		ArrayList<GeoPoint> list_of_geopoints = new ArrayList<GeoPoint>();

		HttpResponse response = null;

		try {

			HttpClient httpClient = new DefaultHttpClient();

			HttpContext localContext = new BasicHttpContext();

			HttpPost httpPost = new HttpPost(url);

			response = httpClient.execute(httpPost, localContext);

			InputStream in = response.getEntity().getContent();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document doc = builder.parse(in);

			if (doc != null) {

				NodeList nl1, nl2;

				nl1 = doc.getElementsByTagName(tag[0]);

				nl2 = doc.getElementsByTagName(tag[1]);

				if (nl1.getLength() > 0) {

					list_of_geopoints = new ArrayList<GeoPoint>();

					for (int i = 0; i < nl1.getLength(); i++) {

						Node node1 = nl1.item(i);

						Node node2 = nl2.item(i);

						double lat = Double.parseDouble(node1.getTextContent());

						double lng = Double.parseDouble(node2.getTextContent());

						list_of_geopoints.add(new GeoPoint((int) (lat * 1E6),
								(int) (lng * 1E6)));

					}

				} else {

					// No points found

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return list_of_geopoints;

	}

	private class GP {// possible extends GeoPoint
		double lat;
		double longi;

		public GP(double lat, double longi) {
			this.lat = lat;
			this.longi = longi;
		}

		public int getLongiE6() {
			return (int) (longi * 1e6);
		}

		public int getLatE6() {
			return (int) (lat * 1e6);
		}

		public double getLongi() {
			return longi;
		}

		public double getLat() {
			return lat;
		}
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {

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
			
			guideToSavedPosition();

			// Log.i(TAG, "" + myLocation.getCurrentLocation());

			// UPDATE POSITION ON RADARMAP AND START GUIDING WITH A DETAILED
			// MAPVIEW

			// Intent myIntent = new Intent(MapViewActivity.this,
			// RoadMapActivity.class);
			// MapViewActivity.this.startActivity(myIntent);
		}
	}

	@Override
	protected void onDestroy() {
		myLocation.onDestroy();
		theGuide.onDestroy();
		super.onDestroy();
	}

	
}
