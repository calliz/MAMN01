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

import android.os.Bundle;
import android.view.Menu;

public class MapViewActivity extends MapActivity {

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_view_activity);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        
        MapController mc = mapView.getController();
        ArrayList<GeoPoint> all_geo_points = getDirections(55.70462000000001, 13.191360, 55.604640, 13.00382);
        GeoPoint moveTo = (GeoPoint) all_geo_points.get(0);
        mc.animateTo(moveTo);
        mc.setZoom(12);
        //mapView.getOverlays().add(new RoadOverlay(all_geo_points));
        mapView.getOverlays().add(new CircleOverlay(null, 1, 1, 2000));
        mapView.getOverlays().add(new CircleOverlay(null, 1, 1, 3000));
        mapView.getOverlays().add(new CircleOverlay(null, 1, 1, 4000));
        
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

