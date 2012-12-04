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
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
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

public class MapViewActivity extends MapActivity implements Compass{

	private static final String SAVED_STATE_COMPASS_MODE = "com.touchboarder.example.modecompass";
	@SuppressWarnings("unused")
	private final String TAG = RoadMapActivity.class.getSimpleName();
	private MapView mapView;
	private MapController mc;
	private boolean mModeCompass = false;
	private LocationOverlay selectedOverlay;
	private GP selectedLocation;

	private MyLocationOverlay mMyLocationOverlay = null;
	private SensorManager mSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;
	private GeoPoint userPoint;
	private ArrayList<GP> all_geo_points;
	/* HaptiMap attributes */
	private MyLocationModule myLocation;
	private Location currentPos;
	private Location nextPos;
	private HapticGuide theGuide;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* HaptiMap code */
		startHapticGuide();

		setContentView(R.layout.map_view_activity);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
		mRotateView = new RotateView(this);

		mapView = (MapView) findViewById(R.id.mapview);
		mMyLocationOverlay = new MyLocationOverlay(this, mapView);

		mapView.setBuiltInZoomControls(false);

		mc = mapView.getController();
		// ArrayList<GeoPoint> all_geo_points = getDirections(55.70462000000001,
		// 13.191360, 55.604640, 13.00382);
		all_geo_points = new ArrayList<GP>();
		addGeoPoints(all_geo_points);

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
		SensorManager sensorManager = (SensorManager) this
				.getSystemService(SENSOR_SERVICE);
		final SensorEventListener mEventListener = new CompassListener(
				sensorManager, this);
		setListners(sensorManager, mEventListener);

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

	/* HaptiMap function */
	private void startHapticGuide() {
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

	}

	/* HaptiMap function */
	private void fetchAndSetCurrentPosition() {
		// currentPos = myLocation.getCurrentLocation();
		// Location tmpPos = myLocation.getCurrentLocation();

		// Lund central
		currentPos = GeoToLocation(new GeoPoint(55705644, 13186916));

		// currentPos.setLatitude(55.600459);
		// currentPos.setLongitude(12.96725);

		if (myLocation.getCurrentLocation() == null) {
			Toast.makeText(
					MapViewActivity.this,
					"No GPS signal - using Designcentrum IKDC fixed position instead",
					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "No GPS signal - no current position set");

		} else {
			// Toast.makeText(
			// MapViewActivity.this,
			// "Current location set to: " + currentPos.getLatitude()
			// + ", " + currentPos.getLongitude(),
			// Toast.LENGTH_SHORT).show();
			// Log.i(TAG, "Current location set to: " + currentPos.getLatitude()
			// + ", " + currentPos.getLongitude());
			Toast.makeText(MapViewActivity.this,
					"GPS signal is good - current position is set",
					Toast.LENGTH_SHORT).show();

		}
		// Log.i(TAG, "" + myLocation.getCurrentLocation());
	}

	/* HaptiMap function */
	private void guideToSavedPosition() {
		// nextPos = GeoToLocation(new GeoPoint(55705248, 13186763));

		if (currentPos != null) {

			WayPoint goal = new WayPoint("goal", currentPos);

			theGuide.setNextDestination(goal);

			theGuide.onStart();
			Log.i(TAG, "Guiding to " + currentPos.getLatitude() + ", "
					+ currentPos.getLongitude());
		} else {
			// Toast.makeText(GuidingService.this,
			// "no GPS signal - cannot guide",
			// Toast.LENGTH_SHORT).show();
			Log.i(TAG, "no GPS signal - cannot guide");
		}
		// Log.i(TAG, "guide button");
	}

	/* HaptiMap function */
	private Location GeoToLocation(GeoPoint geoPoint) {
		Location location = new Location("dummyProvider");
		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
		return location;
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
					new CircleOverlay(null, currentLocation.getLat(),
							currentLocation.getLongi(), step * i));
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
								.getLongi(), radius,Color.RED));
			}
		}

	}

	private void addGeoPoints(ArrayList<GP> all_geo_points) {// 55.70462000000001,
																// 13.191360
																// all_geo_points.add(new
																// GP(55.70462000000001,
																// 13.191360));
																// // Stora
																// gr�br�dersgatan
																// Lund
		all_geo_points.add(new GP(55.714976, 13.212644, "Designcentrum (IKDC)")); // Designcentrum IKDC
		all_geo_points.add(new GP(55.721056, 13.21277,"Magistratsvägen 57O"));
		all_geo_points.add(new GP(55.709114, 13.167778, "Vildandsvägen 18H"));
		all_geo_points.add(new GP(55.724313, 13.204009, "Fäladstorget 12"));
		all_geo_points.add(new GP(55.698377, 13.216635, "Dalbyvägen 38"));
		all_geo_points.add(new GP(55.705644, 13.186916, "Bangatan 1")); // Lunds
															// centralstation
		// all_geo_points.add(new GP(55.707095, 13.189404));// Close to
		// epicentrum
		// of Lund
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
		CharSequence locationName;

		public GP(double lat, double longi, CharSequence locationName) {
			this.lat = lat;
			this.longi = longi;
			this.locationName = locationName;
		}

		public CharSequence getName(){
			return locationName;
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

	@Override
	public void setBearing(double deg) {
		double min_diff = Double.MAX_VALUE;
		Toast toast = null;
		int min_index = -1;
		for (int i = 1; i < all_geo_points.size(); i++) {
			double diff = Math.abs(deg
					- CalcAngleFromNorth.calculateAngle(
							all_geo_points.get(0).lat,
							all_geo_points.get(0).longi,
							all_geo_points.get(i).lat,
							all_geo_points.get(i).longi));
			if (diff < min_diff) {
				min_diff = diff;
				min_index = i;
			}
		}
		if (min_diff < 10 && min_index != -1) {

			Log.e("Found", "Pointing at lat:"
					+ all_geo_points.get(min_index).lat + " longi:"
					+ all_geo_points.get(min_index).longi);

			if (selectedOverlay == null) {
				GP active = all_geo_points.get(min_index);
				selectedOverlay = new LocationOverlay(null, active.getLat(),
						active.getLongi(), 400, Color.GREEN);
				mapView.getOverlays().add(selectedOverlay);
				selectedLocation = active;
				toast = Toast.makeText(this,selectedLocation.getName(), 1);
				toast.show();
			}
			// Vibrate for 300 milliseconds
			//v.vibrate(50);
		}else{
			mapView.getOverlays().remove(selectedOverlay);
			selectedOverlay = null;
			selectedLocation = null;
		}
		
	}

}
