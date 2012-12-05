package com.example.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

public class NonPannableMapView extends MapView {

	public NonPannableMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NonPannableMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonPannableMapView(Context context, String apiKey) {
		super(context, apiKey);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Handle a move so there is no panning or zoom
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			return true;
		}
		return super.onTouchEvent(ev);
	}
}
