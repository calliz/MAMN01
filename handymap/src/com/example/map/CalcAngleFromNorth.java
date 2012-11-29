package com.example.map;

public class CalcAngleFromNorth {
	public static double calculateAngle(double lat_a, double longi_a, double lat_b, double longi_b){
		double a,b, beta = 0;
		b = lat_b - lat_a;
		a = longi_b - longi_a;
		beta = Math.atan2(a, b) * 180.0 / Math.PI;
	    if (beta < 0.0)
	        beta += 360.0;
	    else if (beta > 360.0)
	        beta -= 360;

	    return beta;
	}
}
