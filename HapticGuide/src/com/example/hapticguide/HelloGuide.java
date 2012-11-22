package com.example.hapticguide;

import org.haptimap.hcimodules.guiding.HapticGuide;
import org.haptimap.hcimodules.guiding.HapticGuideEventListener;
import org.haptimap.hcimodules.util.MyLocationModule;
import org.haptimap.hcimodules.util.WayPoint;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class HelloGuide extends Activity {
	private Button button1;
	private Button button2;
	private final String TAG = "MyActivity";
	private MyLocationModule myLocation;
	private Location currentPos;
	private HapticGuide theGuide;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		myLocation = new MyLocationModule(this);

		myLocation.onStart();

		currentPos = null;

		theGuide = new HapticGuide(this);

		setContentView(R.layout.activity_hello_guide);
		button1 = (Button) findViewById(R.id.set);

		button1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Log.i(TAG, "set button");
				currentPos = myLocation.getCurrentLocation();
				if (currentPos == null) {
					Toast.makeText(HelloGuide.this,
							"no GPS signal - no position set",
							Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(
							HelloGuide.this,
							"location set= " + currentPos.getLatitude() + ", "
									+ currentPos.getLongitude(),
							Toast.LENGTH_SHORT).show();
				}

			}

		});

		button2 = (Button) findViewById(R.id.guide);

		button2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Log.i(TAG, "guide button");
				if (currentPos != null) {

					WayPoint goal = new WayPoint("goal", currentPos);

					theGuide.setNextDestination(goal);

					theGuide.onStart();
				} else {
					Toast.makeText(HelloGuide.this,
							"no GPS signal - cannot guide", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		theGuide.registerHapticGuideEventListener(new HapticGuideEventListener() {

			public void onRateIntervalChanged(int millis) {

			}

			public void onPrepared(boolean onPrepared) {

			}

			public void onDestinationReached(long[] pattern) {
				Toast.makeText(HelloGuide.this, "You have arrived!",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onDestroy() {

		myLocation.onDestroy();
		theGuide.onDestroy();

		super.onDestroy();
	}
}