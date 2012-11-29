package com.example.map;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class BlackOverlay extends Overlay {

    Context context;
    double mLat;
    double mLon;
    float mRadius;

     public BlackOverlay(Context _context, double _lat, double _lon, float radius ) {
            context = _context;
            mLat = 55.715024;//_lat;
            mLon = 13.212687;//_lon;
            mRadius = 15000;
     }

     public void draw(Canvas canvas, MapView mapView, boolean shadow) {

         super.draw(canvas, mapView, shadow); 

         Projection projection = mapView.getProjection();

         Point pt = new Point();

         GeoPoint geo = new GeoPoint((int) (mLat *1e6), (int)(mLon * 1e6));

         projection.toPixels(geo ,pt);
         float circleRadius = projection.metersToEquatorPixels(mRadius);

         Paint innerCirclePaint;

         innerCirclePaint = new Paint();
         innerCirclePaint.setColor(Color.BLACK);
         innerCirclePaint.setAlpha(120);
         innerCirclePaint.setAntiAlias(true);

         innerCirclePaint.setStyle(Paint.Style.FILL);

         canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, innerCirclePaint);
    }
}

