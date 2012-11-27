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

import com.example.rotate.RotateView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

public class RotatingMap extends MapActivity {
	
	private MapView mMapView;
	private MyLocationOverlay mMyLocationOverlay = null;
	private boolean mModeCompass = false;
	private SensorManager mSensorManager;
	private LinearLayout mRotateViewContainer;
	private RotateView mRotateView;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotating_map);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        /////
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
        mRotateView = new RotateView(this);
        ////
        mMapView = (MapView) findViewById(R.id.map_view);
        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);  
        /*
        MapController mc = mapView.getController();
        ArrayList<GeoPoint> all_geo_points = getDirections(55.70462000000001, 13.191360, 55.604640, 13.00382);
        GeoPoint moveTo = (GeoPoint) all_geo_points.get(0);
        mc.animateTo(moveTo);
        mc.setZoom(12);
        mapView.getOverlays().add(new RoadOverlay(all_geo_points));
        */
    }
    
    ///////
    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.maplayout);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotateViewContainer = (LinearLayout) findViewById(R.id.rotating_view);
        mRotateView = new RotateView(this);

        // Sign Up for the Android Maps API at:
        // https://developers.google.com/android/maps-api-signup
        // Add the Android Maps API key to the MapView in the maplayout.xml file 
        mMapView = (MapView) findViewById(R.id.map_view);
        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);     

    }*/

    @SuppressWarnings("deprecation")
    public void onClick(View v) {

        switch (v.getId()) {

        case R.id.button_compass:
            if (mMyLocationOverlay.isCompassEnabled()) {
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
            break;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        if (mModeCompass) {
            mMyLocationOverlay.enableCompass();
            mSensorManager.registerListener(mRotateView,
                    SensorManager.SENSOR_ORIENTATION,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMyLocationOverlay.disableCompass();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mRotateView);
        super.onStop();
    }

    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
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

}

