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

public class CircleOverlay extends Overlay {

    Context context;
    double mLat;
    double mLon;
    float mRadius;

     public CircleOverlay(Context _context, double _lat, double _lon, float radius ) {//lat/long är current location
            context = _context;
            mLat = _lat;
            mLon = _lon;
            mRadius = radius;
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
         innerCirclePaint.setColor(Color.CYAN);
         innerCirclePaint.setAlpha(40);
         innerCirclePaint.setAntiAlias(true);

         innerCirclePaint.setStyle(Paint.Style.FILL);

         canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, innerCirclePaint);
    }
}
