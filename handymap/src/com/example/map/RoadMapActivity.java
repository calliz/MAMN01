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
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.android.maps.Overlay;

public class RoadMapActivity extends MapActivity  implements Tiltable, Compass {
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
	private GeoPoint userPoint;
	
	//HaptiAttributes
	private MyLocationModule myLocation;
	private Location currentPos;
	private Location nextPos;
	private HapticGuide theGuide;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startHapticGuide();
		
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
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GeoPoint currPos = getCurrentGeoPoint();

		all_geo_points = getDirections(((double)currPos.getLatitudeE6()) / ((double)(1e6)), ((double)(currPos.getLongitudeE6())) / ((double)(1e6)), 55.720622, 13.212985);

		if (all_geo_points.size() == 0) {
			Log.d("RoadMap", "Arraylist zero elem");
		}
		GeoPoint moveTo = all_geo_points.get(0);
		mc.animateTo(moveTo);// ska ha current location
		mc.setZoom(14);
		roadOverlay = new RoadOverlay(all_geo_points);
		mapView.getOverlays().add(roadOverlay);// For the next view
		// createRightZoomLevel(mc, all_geo_points);

		all_geo_points.remove(0);// remove the first node
		
		//kommer att använda all_geo... för att få ut första guid punkten
		
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
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

	}
	
	public GeoPoint getTargetDestination(){
		
		return null;
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
	
	public GeoPoint getCurrentPos(){//Måste implementeras
		return null;
	}
	
	public void setNextNode(GeoPoint newPoint){//Måste implementeras
		
		WayPoint nextNode = new WayPoint("nextNode", GeoToLocation(newPoint));

		theGuide.setNextDestination(nextNode);
	}

	public boolean pointReached() {// returns true if finaldestination reached.

		if (all_geo_points.size() == 0)
			return true;

		all_geo_points.remove(0);
		currentTarget = all_geo_points.get(0);
		setNextNode(currentTarget);
		
		all_geo_points.add(0, getCurrentGeoPoint());//lägg till current position för uppritning, tas bort sen.
		
		
		mapView.getOverlays().remove(roadOverlay);
		roadOverlay = new RoadOverlay(all_geo_points);
		all_geo_points.remove(0);//för att hålla listan i ok state
		mapView.getOverlays().add(roadOverlay);

		return false;

	}
	
	public GeoPoint getCurrentGeoPoint(){
		Location loc = myLocation.getCurrentLocation();
		
		if(loc  == null){
			Log.d("getCurrentGeoPoint", "Null i location");
			System.exit(1);
		}
		
		GeoPoint currPos = new GeoPoint((int) (loc.getLatitude()*1e6), (int)(loc.getLongitude() * 1e6));
				
		return currPos;
	}

	public void setNewRoad(RoadOverlay newOverlay) {
		mapView.getOverlays().remove(roadOverlay);
		mapView.getOverlays().add(newOverlay);
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
		// else mc.animateTo(userPoint);
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
				// Toast.LENGTH_SHORT).show());
				//stefan mod:
				if(pointReached()){
					Log.d("onDestinationReached", "Final destination reached!!!!!!!!!!!!!!!!");
					//Toast.makeText(RoadMapActivity.this, "You have arrived!", Toast.LENGTH_SHORT).show);
				}
				
			}
		});

	}

	/* HaptiMap function */
	private void fetchAndSetCurrentPosition() {
		 currentPos = myLocation.getCurrentLocation();
		 Location tmpPos = myLocation.getCurrentLocation();

		// Lund central
		//currentPos = GeoToLocation(new GeoPoint(55705644, 13186916));

		 //currentPos.setLatitude(55.600459);
		 //currentPos.setLongitude(12.96725);

		if (myLocation.getCurrentLocation() == null) {
			Toast.makeText(
					RoadMapActivity.this,
					"No GPS signal - using Designcentrum IKDC fixed position instead",
					Toast.LENGTH_SHORT).show();
			Log.i(TAG, "No GPS signal - no current position set");

		} else {
			Toast.makeText(
			RoadMapActivity.this,
			"Current location set to: " + currentPos.getLatitude()
			 + ", " + currentPos.getLongitude(),
			Toast.LENGTH_SHORT).show();
			Log.i(TAG, "Current location set to: " + currentPos.getLatitude()
			+ ", " + currentPos.getLongitude());
			Toast.makeText(RoadMapActivity.this,
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
	

}
