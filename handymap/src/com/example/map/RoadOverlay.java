package com.example.map;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RoadOverlay extends Overlay {

	private ArrayList<GeoPoint> all_geo_points;

	public RoadOverlay(ArrayList<GeoPoint> allGeoPoints) {

		super();

		hardCopy(allGeoPoints);

	}

	public void hardCopy(ArrayList<GeoPoint> allGeoPoints) {

		all_geo_points = new ArrayList<GeoPoint>();

		for (int i = 0; i < allGeoPoints.size(); i++) {

			all_geo_points.add(new GeoPoint(
					allGeoPoints.get(i).getLatitudeE6(), allGeoPoints.get(i)
							.getLongitudeE6()));
		}

	}

	@Override
	public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {

		super.draw(canvas, mv, shadow);

		drawPath(mv, canvas);// mv

		return true;

	}

	public void drawPath(MapView mv, Canvas canvas) {

		int xPrev = -1, yPrev = -1, xNow = -1, yNow = -1;

		Paint paint = new Paint();

		paint.setStyle(Paint.Style.FILL_AND_STROKE);

		paint.setStrokeWidth(4);

		paint.setAlpha(100);

		if (all_geo_points != null)

			for (int i = 0; i < all_geo_points.size(); i++) {// prev for (int i
																// = 0; i <
																// all_geo_points.size()
																// - 4; i++) {
				if (i == 1) {
					paint.setColor(Color.GREEN);
				} else if (i == 0) {
					paint.setColor(Color.BLACK);
				} else {
					paint.setColor(Color.RED);
				}

				GeoPoint gp = (GeoPoint) all_geo_points.get(i);

				Point point = new Point();

				mv.getProjection().toPixels(gp, point);

				xNow = point.x;

				yNow = point.y;

				canvas.drawCircle(xNow, yNow, 10, paint);
			}
	}

}
