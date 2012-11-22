package com.example.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class StartActivityRadar extends Activity {
	private ImageView imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_activity_radar);
		imageView = (ImageView) findViewById(R.id.radar_image);

		imageView.setImageResource(R.drawable.radar_2_black);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start_activity_radar, menu);
		return true;
	}
}
