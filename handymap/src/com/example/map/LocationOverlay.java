package com.example.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class LocationOverlay extends Overlay {

	Context context;
	double mLat;
	double mLon;
	float mRadius;
	Canvas canvas;
	MapView mapView;
	private int color;

	public LocationOverlay(Context _context, double _lat, double _lon,
			float radius, int color) {
		context = _context;
		mLat = _lat;
		mLon = _lon;
		mRadius = radius;
		this.color = color;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		this.canvas = canvas;

		Projection projection = mapView.getProjection();

		Point pt = new Point();

		GeoPoint geo = new GeoPoint((int) (mLat * 1e6), (int) (mLon * 1e6));

		projection.toPixels(geo, pt);
		float circleRadius = projection.metersToEquatorPixels(mRadius);

		Paint innerCirclePaint;

		innerCirclePaint = new Paint();
		innerCirclePaint.setColor(color);
		innerCirclePaint.setAlpha(175);
		innerCirclePaint.setAntiAlias(true);

		innerCirclePaint.setStyle(Paint.Style.FILL);

		canvas.drawCircle((float) pt.x, (float) pt.y, circleRadius,
				innerCirclePaint);
	}
}
