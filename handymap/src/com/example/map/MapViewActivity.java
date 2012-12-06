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
import org.haptimap.hcimodules.util.MyLocationModule;
import org.haptimap.hcimodules.util.WayPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MapViewActivity extends MapActivity implements Compass, Touch,
		LocationListener {

	private static final String SAVED_STATE_COMPASS_MODE = "com.touchboarder.example.modecompass";
	@SuppressWarnings("unused")
	private final String TAG = MapViewActivity.class.getSimpleName();
	private NonPannableMapView nonPannableMapView;
	private MapController mc;
	private boolean mModeCompass = false;
	private LocationOverlay selectedOverlay;
	private GeoPoint selectedLocation;

	private MyLocationOverlay mMyLocationOverlay = null;
	private SensorManager mSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;
	private GeoPoint userPoint;
	private ArrayList<GeoPoint> all_geo_points;

	/* HaptiMap attributes */
	private MyLocationModule myLocation;
	private Location currentPos;
	private Location nextPos;
	private HapticGuide theGuide;
	private LocationManager locationManager;
	private String provider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* HaptiMap code */
		// startHapticGuide();

		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use
		// default
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		// Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
			
			setContentView(R.layout.map_view_activity);
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
			mRotateView = new RotateView(this);

			nonPannableMapView = (NonPannableMapView) findViewById(R.id.mapview);
			mMyLocationOverlay = new MyLocationOverlay(this, nonPannableMapView);

			nonPannableMapView.setBuiltInZoomControls(false);

			mc = nonPannableMapView.getController();
			// ArrayList<GeoPoint> all_geo_points = getDirections(55.70462000000001,
			// 13.191360, 55.604640, 13.00382);
			all_geo_points = new ArrayList<GeoPoint>();
			addGeoPoints(all_geo_points);

			mc.animateTo(userPoint);// ska ha current location
			mc.setZoom(14);
			// mapView.getOverlays().add(new RoadOverlay(all_geo_points));//For the
			// next view
			// createRightZoomLevel(mc, all_geo_points);
			int nbrOfCircles = 3;
			blackBackround(nonPannableMapView, userPoint);
			addCircles(nonPannableMapView, all_geo_points, nbrOfCircles, userPoint);
			addLocationMarkers(nonPannableMapView, all_geo_points);
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
			final TouchListener touchListener = new TouchListener(this);

			// mapView.setOnTouchListener(touchListener);

			nonPannableMapView.setOnTouchListener(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						touched();
						return true;
					}
					return false;
				}
			});

			mMyLocationOverlay.isCompassEnabled();
			toogleRotateView(mModeCompass);
			nonPannableMapView.setBuiltInZoomControls(false);
		} else {
			Toast.makeText(this, "No GPS signal - restart application ",
					Toast.LENGTH_SHORT).show();
			this.finish();
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

	/* HaptiMap function */
	private void startHapticGuide() {
		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPos = null;

		nextPos = null;

		theGuide = new HapticGuide(this);

		while (!fetchAndSetCurrentPosition())
			;

		// guideToSavedPosition();

		// theGuide.registerHapticGuideEventListener(new
		// HapticGuideEventListener() {
		//
		// public void onRateIntervalChanged(int millis) {
		//
		// }
		//
		// public void onPrepared(boolean onPrepared) {
		//
		// }
		//
		// public void onDestinationReached(long[] pattern) { //
		// // Toast.makeText(GuidingService.this, "You have arrived!", //
		// // Toast.LENGTH_SHORT).show();
		// Log.i(TAG, "You have arrived at your final destination!!!");
		// }
		// });

	}

	/* HaptiMap function */
	private boolean fetchAndSetCurrentPosition() {
		currentPos = myLocation.getCurrentLocation();

		// IKDC 55.714928,13.212816 JAJJAJA
		// currentPos = geoToLocation(new GeoPoint(55714928, 13212816));

		if (currentPos == null) {
			Toast.makeText(MapViewActivity.this, "No GPS signal - waiting",
					Toast.LENGTH_SHORT).show();
			// Log.i(TAG, "No GPS signal - no current position set");
			return false;
		} else {
			userPoint = new GeoPoint(convertGeoToInt(currentPos.getLatitude()),
					convertGeoToInt(currentPos.getLongitude()));
			Toast.makeText(
					MapViewActivity.this,
					"Current location set to: " + currentPos.getLatitude()
							+ ", " + currentPos.getLongitude(),
					Toast.LENGTH_SHORT).show();
			// Log.i(TAG, "Current location set to: " + currentPos.getLatitude()
			// + ", " + currentPos.getLongitude());
			// Toast.makeText(MapViewActivity.this,
			// "GPS signal is good - current position is set",
			// Toast.LENGTH_SHORT).show();
			return true;
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
	private Location geoToLocation(GeoPoint geoPoint) {
		Location location = new Location("dummyProvider");
		location.setLatitude(geoPoint.getLatitudeE6() / 1E6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1E6);
		return location;
	}

	public void blackBackround(MapView mapView, GeoPoint currentLocation) {
		mapView.getOverlays().add(
				new BlackOverlay(null, convertGeoToDouble(currentLocation
						.getLatitudeE6()), convertGeoToDouble(currentLocation
						.getLongitudeE6()), 4000));
	}

	public void addCircles(MapView mapView, ArrayList<GeoPoint> all_gp,
			int nbrOfCircles, GeoPoint currentLocation) {

		int radius = getRadius(all_gp, currentLocation);
		int step = radius / nbrOfCircles;
		step *= 90;// 85

		for (int i = 1; i <= nbrOfCircles; i++) {
			mapView.getOverlays()
					.add(new CircleOverlay(
							null,
							convertGeoToDouble(currentLocation.getLatitudeE6()),
							convertGeoToDouble(currentLocation.getLongitudeE6()),
							step * i));
		}

	}

	private int getRadius(ArrayList<GeoPoint> all_gp, GeoPoint currentLocation) {

		int longestDistance = 0;

		for (GeoPoint item : all_gp) {
			int lat = item.getLatitudeE6();
			int lon = item.getLongitudeE6();

			int thisDistance = (int) Math.sqrt(lat * lat + lon * lon);
			if (thisDistance > longestDistance) {

				longestDistance = thisDistance;
			}
		}
		Log.d("getRadius", "Radius = " + longestDistance);
		return longestDistance;
	}

	private void addLocationMarkers(MapView mapView,
			ArrayList<GeoPoint> all_geo_points) {
		int radius = 200;

		mapView.getOverlays().add(
				new CurrentPositionOverlay(null, userPoint.getLatitudeE6(),
						userPoint.getLongitudeE6(), radius - 50));// currentposition

		for (GeoPoint point : all_geo_points) {
			mapView.getOverlays().add(
					new LocationOverlay(null, convertGeoToDouble(point
							.getLatitudeE6()), convertGeoToDouble(point
							.getLongitudeE6()), radius, Color.RED));
		}

	}

	private void addGeoPoints(ArrayList<GeoPoint> all_geo_points) {
		all_geo_points.add(new GeoPoint(userPoint.getLatitudeE6(), userPoint
				.getLongitudeE6()));
		all_geo_points.add(new GeoPoint(55721056, 1321277));
		all_geo_points.add(new GeoPoint(55709114, 13167778));
		all_geo_points.add(new GeoPoint(55724313, 13204009));
		all_geo_points.add(new GeoPoint(55698377, 13216635));
		all_geo_points.add(new GeoPoint(55705644, 13186916));
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

	@SuppressWarnings("deprecation")
	private void toogleRotateView(boolean compassMode) {
		if (compassMode) {
			mSensorManager.unregisterListener(mRotateView);
			mRotateView.removeAllViews();
			mRotateViewContainer.removeAllViews();
			mRotateViewContainer.addView(nonPannableMapView);
			mMyLocationOverlay.disableCompass();
			mModeCompass = false;
		} else {
			mRotateViewContainer.removeAllViews();
			mRotateView.removeAllViews();
			mRotateView.addView(nonPannableMapView);
			mRotateViewContainer.addView(mRotateView);
			nonPannableMapView.setClickable(true);
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

		// toogleRotateView(!mModeCompass);
		// ToggleButton toggleCompassButton = (ToggleButton)
		// findViewById(R.id.button_compass);
		// toggleCompassButton.setChecked(mModeCompass);

		// shows the my location dot centered on your last known location
		mMyLocationOverlay.enableMyLocation();

		/*
		 * if (userPointInt == null) mMyLocationOverlay.runOnFirstFix(new
		 * Runnable() { public void run() { userPointInt =
		 * mMyLocationOverlay.getMyLocation(); // if(userPoint!=null) //
		 * mc.animateTo(userPoint);
		 * 
		 * } }); // else mc.animateTo(userPoint);
		 */
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

	// private class GP {// possible extends GeoPoint
	// double lat;
	// double longi;
	// CharSequence locationName;
	//
	// public GP(double lat, double longi, CharSequence locationName) {
	// this.lat = lat;
	// this.longi = longi;
	// this.locationName = locationName;
	// }
	//
	// public CharSequence getName() {
	// return locationName;
	// }
	//
	// public int getLongiE6() {
	// return (int) (longi * 1e6);
	// }
	//
	// public int getLatE6() {
	// return (int) (lat * 1e6);
	// }
	//
	// public double getLongi() {
	// return longi;
	// }
	//
	// public double getLat() {
	// return lat;
	// }
	// }

	public void setBearing(double deg) {
		double min_diff = Double.MAX_VALUE;
		Toast toast = null;
		int min_index = -1;
		for (int i = 1; i < all_geo_points.size(); i++) {
			double diff = Math.abs(deg
					- CalcAngleFromNorth.calculateAngle(
							convertGeoToDouble(userPoint.getLatitudeE6()),
							convertGeoToDouble(userPoint.getLongitudeE6()),
							convertGeoToDouble(all_geo_points.get(i)
									.getLatitudeE6()),
							convertGeoToDouble(all_geo_points.get(i)
									.getLongitudeE6())));
			if (diff < min_diff) {
				min_diff = diff;
				min_index = i;
			}
		}
		if (min_diff < 10 && min_index != -1) {

			// Log.e("Found", "Pointing at lat:"
			// + all_geo_points.get(min_index).lat + " longi:"
			// + all_geo_points.get(min_index).longi);

			if (selectedOverlay == null) {
				GeoPoint active = all_geo_points.get(min_index);
				selectedOverlay = new LocationOverlay(null,
						convertGeoToDouble(active.getLatitudeE6()),
						convertGeoToDouble(active.getLongitudeE6()), 400,
						Color.GREEN);
				nonPannableMapView.getOverlays().add(selectedOverlay);
				selectedLocation = active;
				// toast = Toast.makeText(this, selectedLocation.getName(), 1);
				// toast.show();
			}
			// Vibrate for 300 milliseconds
			// v.vibrate(50);
		} else {
			nonPannableMapView.getOverlays().remove(selectedOverlay);
			selectedOverlay = null;
			selectedLocation = null;
		}

	}

	public void touched() {
		Toast.makeText(MapViewActivity.this, "touched screen", //
				Toast.LENGTH_SHORT).show();
		if (selectedLocation != null) {
			Log.i("MapViewActivity", "selectedLocation lat: "
					+ selectedLocation.getLatitudeE6() + "longi: "
					+ selectedLocation.getLongitudeE6());
			int lat = selectedLocation.getLatitudeE6();
			int longi = selectedLocation.getLongitudeE6();

			Intent roadMapIntent = new Intent(MapViewActivity.this,
					RoadMapActivity.class);
			roadMapIntent.putExtra("lat", lat);
			roadMapIntent.putExtra("longi", longi);
			startActivity(roadMapIntent);
		} else {
			Log.i("MapViewActivity", "selectedLocation is NULL");
		}
	}

	public void onLocationChanged(Location location) {
		int lat = (int) (location.getLatitude());
		int lng = (int) (location.getLongitude());
		currentPos = location;
		userPoint = new GeoPoint(convertGeoToInt(currentPos.getLatitude()),
				convertGeoToInt(currentPos.getLongitude()));
	}

	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
