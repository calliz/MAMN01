package com.example.map;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;

public class MyLocationListener implements LocationListener {
	private MyLocation myLoc;

	public MyLocationListener(MyLocation myLoc) {
		this.myLoc = myLoc;
	}

	public void onLocationChanged(Location location) {
		int lat = (int) (location.getLatitude() * 1e6);
		int lng = (int) (location.getLongitude() * 1e6);
		myLoc.setCurrentLocation(new GeoPoint(lat, lng));
	}

	public void onProviderDisabled(String provider) {
		// Toast.makeText(this, "Disabled provider " + provider,
		// Toast.LENGTH_SHORT).show();
	}

	public void onProviderEnabled(String provider) {
		// Toast.makeText(this, "Enabled new provider " + provider,
		// Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Toast.makeText(this,
		// "Status changed of provider " + provider + " to " + status,
		// Toast.LENGTH_SHORT).show();
	}

}
