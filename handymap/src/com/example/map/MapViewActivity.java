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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class MapViewActivity extends MapActivity {
	
	private static final String SAVED_STATE_COMPASS_MODE = "com.touchboarder.example.modecompass";
	@SuppressWarnings("unused")
	private final String TAG = RoadMapActivity.class.getSimpleName();
	private MapView mapView;
	private MapController mc;
	private boolean mModeCompass = false;

	private MyLocationOverlay mMyLocationOverlay = null;
	private SensorManager mSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;
	private GeoPoint userPoint;
	
	//new
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view_activity);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
		mRotateView = new RotateView(this);
        //end new
		
        mapView = (MapView) findViewById(R.id.mapview);
        mMyLocationOverlay = new MyLocationOverlay(this, mapView);
        
        mapView.setBuiltInZoomControls(false);              
        
        mc = mapView.getController();
        //ArrayList<GeoPoint> all_geo_points = getDirections(55.70462000000001, 13.191360, 55.604640, 13.00382);
        ArrayList<GP> all_geo_points = new ArrayList<GP>();
        addGeoPoints(all_geo_points);     
        
        GeoPoint moveTo = new GeoPoint(all_geo_points.get(0).getLatE6() , all_geo_points.get(0).getLongiE6());
        mc.animateTo(moveTo);//ska ha current location
        mc.setZoom(14);
        //mapView.getOverlays().add(new RoadOverlay(all_geo_points));//For the next view
        //createRightZoomLevel(mc, all_geo_points);
        int nbrOfCircles = 3;
        GP currentLocation = all_geo_points.get(0);
        blackBackround(mapView, currentLocation);
        addCircles(mapView, all_geo_points, nbrOfCircles, currentLocation);
        addLocationMarkers(mapView, all_geo_points);
//        mc.animateTo(new GeoPoint(latitudeE6, longitudeE6));

        
		 if (savedInstanceState != null) {
			 mModeCompass = savedInstanceState.getBoolean(SAVED_STATE_COMPASS_MODE, false); 
		 }
    }
    
    public void blackBackround(MapView mapView, GP currentLocation){
    	mapView.getOverlays().add(new BlackOverlay(null, currentLocation.getLat(), currentLocation.getLongi(), 4000));
    }
    
    public void addCircles(MapView mapView, ArrayList<GP> all_gp, int nbrOfCircles, GP currentLocation){
    	
    	int radius = getRadius(all_gp, currentLocation);
    	int step = radius / nbrOfCircles;
    	step *= 90;//85
    	
    	for(int i = 1; i <= nbrOfCircles; i++){
    		mapView.getOverlays().add(new CircleOverlay(null, currentLocation.getLongi(), currentLocation.getLat(), step * i));
    	}
    	
    }
    
    private int getRadius(ArrayList<GP> all_gp, GP currentLocation) {
	
    	
    	int longestDistance = 0;

    	for (GP item : all_gp) {
	    	int lat = (int) item.getLat();
	    	int lon = (int) item.getLongi();
	    	
	    	int thisDistance = (int) Math.sqrt(lat*lat + lon*lon);
	    	if(thisDistance > longestDistance){
	    		
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
		mapView.getOverlays().add(new CurrentPositionOverlay(null, currentLocation.getLat(), currentLocation.getLongi(), radius -50));//currentposition
		
		
    	for(GP point: all_geo_points){
    		if(point != currentLocation){
    			mapView.getOverlays().add(new LocationOverlay(null, point.getLat(), point.getLongi(), radius));
    		}
    	}
		
	}
	
    private void addGeoPoints(ArrayList<GP> all_geo_points) {//55.70462000000001, 13.191360
    	all_geo_points.add(new GP(55.70462000000001,  13.191360));
		all_geo_points.add(new GP(55.721056,  13.21277));
		all_geo_points.add(new GP(55.709114,  13.167778));
		all_geo_points.add(new GP(55.724313, 13.204009));
		all_geo_points.add(new GP(55.698377, 13.216635));
		all_geo_points.add(new GP(55.707095,13.189404));//Close to epicentrum of Lund
	}

	public void createRightZoomLevel(MapController mc, ArrayList<GP> all_geo_points){
    	
    	int minLatitude = Integer.MAX_VALUE;
    	int maxLatitude = Integer.MIN_VALUE;
    	int minLongitude = Integer.MAX_VALUE;
    	int maxLongitude = Integer.MIN_VALUE;

    	// Find the boundaries of the item set
    	for (GP item : all_geo_points) { //item Contain list of Geopints
	    	int lat = item.getLatE6();
	    	int lon = item.getLongiE6();
	
	    	maxLatitude = Math.max(lat, maxLatitude);
	    	minLatitude = Math.min(lat, minLatitude);
	    	maxLongitude = Math.max(lon, maxLongitude);
	    	minLongitude = Math.min(lon, minLongitude);
    	 }
    	 mc.zoomToSpan(Math.abs(maxLatitude - minLatitude), Math.abs(maxLongitude - minLongitude));
    
    	 //mc.animateTo(new GeoPoint((maxLatitude + minLatitude)/2, (maxLongitude + minLongitude)/2 )); 

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
	private void toogleRotateView(boolean compassMode){
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
		
		//shows the my location dot centered on your last known location
		mMyLocationOverlay.enableMyLocation();
		if(userPoint==null)
			mMyLocationOverlay.runOnFirstFix(new Runnable() { public void run() {
				userPoint=mMyLocationOverlay.getMyLocation();
				if(userPoint!=null)
					mc.animateTo(userPoint);

		        }});
		else mc.animateTo(userPoint);
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

    
    public static ArrayList<GeoPoint> getDirections(double lat1, double lon1, double lat2, double lon2) {
    	
        String url = "http://maps.googleapis.com/maps/api/directions/xml?origin=" +lat1 + "," + lon1  + "&destination=" + lat2 + "," + lon2 + "&sensor=false&units=metric&mode=walking";

        String tag[] = { "lat", "lng" };

        ArrayList<GeoPoint> list_of_geopoints = new ArrayList<GeoPoint>();

        HttpResponse response = null;

        try {

            HttpClient httpClient = new DefaultHttpClient();

            HttpContext localContext = new BasicHttpContext();

            HttpPost httpPost = new HttpPost(url);

            response = httpClient.execute(httpPost, localContext);

            InputStream in = response.getEntity().getContent();

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

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

                        list_of_geopoints.add(new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)));

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
    
    private class GP{//possible extends GeoPoint
    	double lat;
    	double longi;
    	
    	public GP(double lat, double longi){
    		this.lat = lat;
    		this.longi = longi;
    	}
    	
    	public int getLongiE6(){
    		return (int) (longi * 1e6);
    	}
    	
    	public int getLatE6(){
    		return (int) (lat * 1e6);
    	}
    	
    	public double getLongi(){
    		return longi;
    	}
    	
    	public double getLat(){
    		return lat;
    	}
    }

}

