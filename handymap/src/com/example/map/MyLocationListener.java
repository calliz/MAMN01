package com.example.map;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

public class MyLocationListener implements LocationListener

{
	private Activity activity;
	public MyLocationListener(Activity act){
		this.activity = act;
	}

	@Override

public void onLocationChanged(Location loc)

{

loc.getLatitude();

loc.getLongitude();

String Text ="My current location is:  Latitud ="  + loc.getLatitude() + "Longitud = " + loc.getLongitude();

Toast.makeText( activity.getApplicationContext(), Text,

Toast.LENGTH_SHORT).show();

}

	@Override

public void onProviderDisabled(String provider)

{

Toast.makeText( activity.getApplicationContext(),"Gps Disabled",

Toast.LENGTH_SHORT ).show();

}

	@Override

public void onProviderEnabled(String provider)

{

Toast.makeText( activity.getApplicationContext(),

"Gps Enabled",

Toast.LENGTH_SHORT).show();

}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)

	{

	}



}/* End of Class MyLocationListener */