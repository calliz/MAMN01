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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class RoadMapActivity extends MapActivity implements Tiltable, Compass,
		MyLocation {
	private RoadOverlay roadOverlay;
	private ArrayList<GeoPoint> all_geo_points;
	private GeoPoint currentTarget;
	private Boolean isTilted;

	private static final String SAVED_STATE_COMPASS_MODE = "com.touchboarder.example.modecompass";
	@SuppressWarnings("unused")
	private final String TAG = RoadMapActivity.class.getSimpleName();
	private MapView mapView;
	private MapController mc;
	private MyLocationOverlay mMyLocationOverlay = null;
	private boolean mModeCompass = false;
	private SensorManager mSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;
	private GeoPoint currentPosGeoPoint;

	// HaptiAttributes
	private MyLocationModule myLocation;
	private Location currentPosLocation;
	private Location nextPosLocation;
	private Location goalPosLocation;
	private HapticGuide theGuide;
	private GeoPoint goalPosGeoPoint;

	private MyLocationListener myLocationListener;
	private LocationManager locationManager;
	private String provider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// startHapticGuide();

		setContentView(R.layout.road_map_activity);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
		mRotateView = new RotateView(this);

		mapView = (MapView) findViewById(R.id.mapview);

		mMyLocationOverlay = new MyLocationOverlay(this, mapView);

		// Optional MapView settings
		mapView.getOverlays().add(mMyLocationOverlay);
		mapView.setBuiltInZoomControls(false);
		mapView.setTraffic(false);
		mapView.setSatellite(false);
		int maxZoom = mapView.getMaxZoomLevel();
		int initZoom = (int) (0.8 * (double) maxZoom);

		mapView.setBuiltInZoomControls(true);

		mc = mapView.getController();
		mc.setZoom(initZoom);

		// Start and goal GeoPoints here
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Get selectedLocation from MapViewActivity
		handleIntent();

		setLocationManager();

		all_geo_points = getDirections(
				convertGeoToDouble(currentPosGeoPoint.getLatitudeE6()),
				convertGeoToDouble(currentPosGeoPoint.getLongitudeE6()),
				goalPosLocation.getLatitude(), goalPosLocation.getLongitude());

		if (all_geo_points.size() == 0) {
			Log.e("RoadMap", "Arraylist zero elem");
		}
		
		mc.animateTo(currentPosGeoPoint);// ska ha current location mc.setZoom(14);
		roadOverlay = new RoadOverlay(all_geo_points);
		mapView.getOverlays().add(roadOverlay);// For the next view //

		all_geo_points.remove(0);// remove the first node

		// kommer att anvÃ¤nda all_geo... fÃ¶r att fÃ¥ ut fÃ¶rsta
		// guid punkten

		currentTarget = all_geo_points.get(0);
		if (savedInstanceState != null) {
			mModeCompass = savedInstanceState.getBoolean(
					SAVED_STATE_COMPASS_MODE, false);
		}
		SensorManager sensorManager = (SensorManager) this
				.getSystemService(SENSOR_SERVICE);
		final SensorEventListener mEventListener = new TiltListener(
				sensorManager, this);
		setListners(sensorManager, mEventListener);
		final SensorEventListener mEventListener2 = new CompassListener(
				sensorManager, this);
		setListners(sensorManager, mEventListener2);
		
		startHapticGuide();
	}

	private void setLocationManager() {

		myLocationListener = new MyLocationListener(this);
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			Log.i(TAG, "Location set to lat: " + location.getLatitude()
					+ " long: " + location.getLongitude());
			myLocationListener.onLocationChanged(location);
		} else {
		}
	}

	private void handleIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		int lat = extras.getInt("lat");
		int longi = extras.getInt("longi");
		if (lat != 0 && longi != 0) {
			goalPosLocation = geoToLocation(new GeoPoint(lat, longi));
			goalPosGeoPoint = new GeoPoint(
					convertGeoToInt(goalPosLocation.getLatitude()),
					convertGeoToInt(goalPosLocation.getLongitude()));

			Toast.makeText(
					RoadMapActivity.this,
					"Goal location is: " + goalPosLocation.getLatitude() + ", "
							+ goalPosLocation.getLongitude(),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void setListners(SensorManager sensorManager,
			SensorEventListener mEventListener) {
		sensorManager.registerListener(mEventListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(mEventListener,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void setNextNode(GeoPoint newPoint) {// Måste implementeras

		WayPoint nextNode = new WayPoint("nextNode", geoToLocation(newPoint));

		theGuide.setNextDestination(nextNode);
	}

	public boolean pointReached() {// returns true if finaldestination reached.

		if (all_geo_points.size() == 1)
			return true;

		all_geo_points.remove(0);
		currentTarget = all_geo_points.get(0);
		setNextNode(currentTarget);

		all_geo_points.add(0, currentPosGeoPoint);// lägg till current
													// position för uppritning,
													// tas bort sen.
		mapView.getOverlays().remove(roadOverlay);
		roadOverlay = new RoadOverlay(all_geo_points);
		all_geo_points.remove(0);// för att hålla listan i ok state
		mapView.getOverlays().add(roadOverlay);

		return false;

	}

	public GeoPoint getCurrentGeoPoint() {
		Location loc = myLocation.getCurrentLocation();

		if (loc == null) {
			Log.d("getCurrentGeoPoint", "Null i location");
			System.exit(1);
		}

		GeoPoint currPos = new GeoPoint((int) (loc.getLatitude() * 1e6),
				(int) (loc.getLongitude() * 1e6));

		return currPos;
	}

	public void createRightZoomLevel(MapController mc,
			ArrayList<GeoPoint> all_geo_points) {

		int minLatitude = Integer.MAX_VALUE;
		int maxLatitude = Integer.MIN_VALUE;
		int minLongitude = Integer.MAX_VALUE;
		int maxLongitude = Integer.MIN_VALUE;

		// Find the boundaries of the item set
		for (GeoPoint item : all_geo_points) { // item Contain list of Geopints
			int lat = item.getLatitudeE6();
			int lon = item.getLongitudeE6();

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
		private Overlay overlay;

		public GP(double lat, double longi) {
			this.lat = lat;
			this.longi = longi;
			this.overlay = overlay;
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

	// new

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_compass:
			// mMyLocationOverlay.isCompassEnabled();
			toogleRotateView(mModeCompass);
			break;
		}
	}

	/**
	 * Since we only can have one instance of the Google APIs MapView, we
	 * add/remove the same MapView between the mRotateViewContainer and the
	 * RotateView when we toggle.
	 * 
	 * @param compassMode
	 *            - if false : turns it on
	 */
	@SuppressWarnings("deprecation")
	private void toogleRotateView(boolean compassMode) {
		if (compassMode) {
			mSensorManager.unregisterListener(mRotateView);
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
			mSensorManager.registerListener(mRotateView,
					SensorManager.SENSOR_ORIENTATION,
					SensorManager.SENSOR_DELAY_UI);
			mMyLocationOverlay.enableCompass();
			mModeCompass = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 400, 1,
				myLocationListener);
		// toogleRotateView(!mModeCompass);
		// ToggleButton toggleCompassButton = (ToggleButton)
		// findViewById(R.id.button_compass);
		// toggleCompassButton.setChecked(mModeCompass);

		// shows the my location dot centered on your last known location
		mMyLocationOverlay.enableMyLocation();
		// if (currentPosGeoPoint == null)
		// mMyLocationOverlay.runOnFirstFix(new Runnable() {
		// public void run() {
		// currentPosGeoPoint = mMyLocationOverlay.getMyLocation();
		// // if(userPoint!=null)
		// // mc.animateTo(userPoint);
		//
		// }
		// });
		// else
		// mc.animateTo(currentPosGeoPoint);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMyLocationOverlay.disableCompass();
		locationManager.removeUpdates(myLocationListener);
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
		mSensorManager.unregisterListener(mRotateView);
		mMyLocationOverlay.disableMyLocation();
		super.onStop();
	}

	public void setBearing(double deg) {
		double min_diff = Double.MAX_VALUE;
		int min_index = -1;
		if (isTilted) {

		}
		// double diff = Math.abs(deg
		// - CalcAngleFromNorth.calculateAngle(
		// currentTarget.,
		// all_geo_points.get(0).longi,
		// all_geo_points.get(i).lat,
		// all_geo_points.get(i).longi));
		// if (diff < min_diff) {
		// min_diff = diff;
		// min_index = i;
		// }
		// }

		// Calculate angle between current location and next point.
		// If angle is small, vibrate or whatever.
		// Log.e("bearing","BEARING!!");
	}

	public void setTilted(boolean b) {
		this.isTilted = b;
		// Log.e("Tilted","Tilting!");
	}

	public void playNotification() {
		MediaPlayer player = MediaPlayer.create(this,
				Settings.System.DEFAULT_RINGTONE_URI);
		player.start();
	}

	/* HaptiMap function */
	private void startHapticGuide() {
		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPosLocation = geoToLocation(currentPosGeoPoint);

		theGuide = new HapticGuide(this);

		// fetchAndSetCurrentPosition();

		guideToSavedPosition();

		theGuide.registerHapticGuideEventListener(new HapticGuideEventListener() {

			public void onRateIntervalChanged(int millis) {

			}

			public void onPrepared(boolean onPrepared) {

			}

			public void onDestinationReached(long[] pattern) { //
				// Toast.makeText(GuidingService.this, "You have arrived!", //
				// Toast.LENGTH_SHORT).show());
				// stefan mod:
				if (pointReached()) {
					Log.d("onDestinationReached",
							"Final destination reached!!!!!!!!!!!!!!!!");

					Toast.makeText(RoadMapActivity.this, "Du är framme!",
							Toast.LENGTH_SHORT).show();

					RoadMapActivity.this.playNotification();

					// Toast.makeText(RoadMapActivity.this, "You have arrived!",
					// Toast.LENGTH_SHORT).show);
				}

			}
		});

	}

	/* HaptiMap function */
	private void fetchAndSetCurrentPosition() {
		currentPosLocation = myLocation.getCurrentLocation();

		// IKDC 55.714928,13.212816 JAJJAJA
		// currentPosLocation = geoToLocation(new GeoPoint(55714928, 13212816));

		if (currentPosLocation == null) {
			Toast.makeText(RoadMapActivity.this, "No GPS signal - waiting",
					Toast.LENGTH_SHORT).show();
			// Log.i(TAG, "No GPS signal - no current position set");
			this.finish();
		} else {
			currentPosGeoPoint = new GeoPoint(
					convertGeoToInt(currentPosLocation.getLatitude()),
					convertGeoToInt(currentPosLocation.getLongitude()));
			Toast.makeText(
					RoadMapActivity.this,
					"Current location set to: "
							+ currentPosLocation.getLatitude() + ", "
							+ currentPosLocation.getLongitude(),
					Toast.LENGTH_SHORT).show();
			Toast.makeText(
					RoadMapActivity.this,
					"currentPosLocation: "
							+ currentPosLocation.getLatitude()
							+ ", "
							+ currentPosLocation.getLongitude()
							+ "\ncurrentPosGeoPoint: "
							+ convertGeoToDouble(currentPosGeoPoint
									.getLatitudeE6())
							+ ", "
							+ convertGeoToDouble(currentPosGeoPoint
									.getLongitudeE6()), Toast.LENGTH_LONG)
					.show();
			// Log.i(TAG, "Current location set to: " + currentPos.getLatitude()
			// + ", " + currentPos.getLongitude());
			// Toast.makeText(MapViewActivity.this,
			// "GPS signal is good - current position is set",
			// Toast.LENGTH_SHORT).show();
		}
		// Log.i(TAG, "" + myLocation.getCurrentLocation());
	}

	private int convertGeoToInt(double degrees) {
		return (int) (degrees * 1e6);
	}

	private double convertGeoToDouble(int microdegrees) {
		return (double) microdegrees / 1e6;
	}

	/* HaptiMap function */
	private void guideToSavedPosition() {
		// nextPos = GeoToLocation(new GeoPoint(55705248, 13186763));

		if (currentTarget != null) {

			WayPoint node = new WayPoint("goal", geoToLocation(currentTarget));

			theGuide.setNextDestination(node);

			theGuide.onStart();
			Log.i(TAG, "Guiding to " + currentTarget.getLatitudeE6() + ", "
					+ currentTarget.getLongitudeE6());
		} else {
			// Toast.makeText(GuidingService.this,
			// "no GPS signal - cannot guide",
			// Toast.LENGTH_SHORT).show();
			Log.e(TAG, "no GPS signal - cannot guide");
		}
		// Log.i(TAG, "guide button");
	}

	/* HaptiMap function */
	private Location geoToLocation(GeoPoint geoPoint) {
		Location location = new Location("dummyProvider");
		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
		return location;
	}

	public void setCurrentLocation(GeoPoint geoPoint) {
		currentPosGeoPoint = geoPoint;
		// Rita om plutt!!!!!!!!!!

	}

}
