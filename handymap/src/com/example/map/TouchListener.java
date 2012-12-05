package com.example.map;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TouchListener implements OnTouchListener {

	private Touch touch;

	public TouchListener(Touch touch) {
		this.touch = touch;
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.i("TouchListener", "ACTION_DOWN: ");
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			Log.i("TouchListener", "ACTION_UP: ");
			touch.touched();
			return false;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			Log.i("TouchListener", "ACTION_MOVE: ");
			return false;
		}
		return false;
	}
}
