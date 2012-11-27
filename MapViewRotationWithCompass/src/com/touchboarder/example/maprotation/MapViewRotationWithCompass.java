/*
 * 2012 Touchboarder (http://touchboarder.com/)
 *
 * Licensed under the "THE BEER-WARE LICENSE" :
 * 
 *   http://stackoverflow.com/users/546054/hsigmond wrote this file. As long as you retain this notice 
 *   you can do whatever you want with this stuff. If we meet some day, and you think this stuff is worth it, 
 *   you can buy me a beer in return
 *   
 */

package com.touchboarder.example.maprotation;

import android.R;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/**
 * Example activity on how to display a google map view rotation with compass
 * To make it work you need to add:
 *  - <uses-library android:name="com.google.android.maps" /> in the manifest.xml file
 *  - Your Android Maps API Key from https://developers.google.com/android/maps-api-signup
 *  - Set the project build target to "Google APIs"
 *  - The RotateView class located in the com.touchboarder.example.maprotation package.
 *  
 *  Note: if you are missing the RotateView class???
 *  - you can extract and add the two inner classes RotateView and SmoothCanvas of MapViewCompassDemo.java found at: 
 *  ..\Android\Android SDK Tools\add-ons\addon-google_apis-google-#\samples\MapsDemo\src\com\example\android\apis\view\
 *  to this class.
 * 
 * @author hsigmond - touchboarder.com - 
 *
 */

public class MapViewRotationWithCompass extends MapActivity {

	private static final String SAVED_STATE_COMPASS_MODE = "com.touchboarder.example.modecompass";
	@SuppressWarnings("unused")
	private final String TAG = MapViewRotationWithCompass.class.getSimpleName();
	private MapView mMapView;
	private MapController mMapControl;
	private MyLocationOverlay mMyLocationOverlay = null;
	private boolean mModeCompass = false;
	private SensorManager mSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;
	private GeoPoint userPoint;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.maplayout);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
		mRotateView = new RotateView(this);

		// Note: To display Google Maps you need to sign Up for the Android Maps API at:
		// https://developers.google.com/android/maps-api-signup
		// and add the generated key to the MapView in the maplayout.xml file 
		mMapView = (MapView) findViewById(R.id.map_view);// Note: We can only have one instance of this view.
		mMyLocationOverlay = new MyLocationOverlay(this, mMapView);

		// Optional MapView settings
		mMapView.getOverlays().add(mMyLocationOverlay);// for displaying the my location dot!
		mMapView.setBuiltInZoomControls(false);
		mMapView.setTraffic(false);
		mMapView.setSatellite(false);
		int maxZoom = mMapView.getMaxZoomLevel();
		int initZoom = (int) (0.8 * (double) maxZoom);
		// Add map controller with zoom controls
		mMapControl = mMapView.getController();
		mMapControl.setZoom(initZoom);
		
		// savedState could be null
		 if (savedInstanceState != null) {
			 mModeCompass = savedInstanceState.getBoolean(SAVED_STATE_COMPASS_MODE, false); 
		 }

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_compass:
			//MyLocationOverlay.isCompassEnabled()
			toogleRotateView(mModeCompass);
			break;
		}
	}
	
	/**
	 * Since we only can have one instance of the Google APIs MapView, we
	 * add/remove the same MapView between the mRotateViewContainer 
	 * and the RotateView when we toggle.
	 *  
	 * @param compassMode - if false : turns it on 
	 */
	@SuppressWarnings("deprecation")
	private void toogleRotateView(boolean compassMode){
		if (compassMode) {
			mSensorManager.unregisterListener(mRotateView);
			mRotateView.removeAllViews();
			mRotateViewContainer.removeAllViews();
			mRotateViewContainer.addView(mMapView);
			mMyLocationOverlay.disableCompass();
			mModeCompass = false;
		} else {
			mRotateViewContainer.removeAllViews();
			mRotateView.removeAllViews();
			mRotateView.addView(mMapView);
			mRotateViewContainer.addView(mRotateView);
			mMapView.setClickable(true);
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
		
		//shows the my location dot centered on your last known location
		mMyLocationOverlay.enableMyLocation();
		if(userPoint==null)
			mMyLocationOverlay.runOnFirstFix(new Runnable() { public void run() {
				userPoint = mMyLocationOverlay.getMyLocation();
				if(userPoint!=null)
					mMapControl.animateTo(userPoint);

		        }});
		else mMapControl.animateTo(userPoint);
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
		//remember the compass mode state
		outState.putBoolean(SAVED_STATE_COMPASS_MODE, mModeCompass);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(mRotateView);	
		mMyLocationOverlay.disableMyLocation();
		super.onStop();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return (false);// Don't display a route
	}

}