package com.example.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StartActivity extends Activity {
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private String[] values;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		listView = (ListView) findViewById(R.id.start_list);
		values = new String[] { "Min position", "Sök", "Hotel", "Pub" };
		adapter = new ArrayAdapter<String>(this, R.layout.row_layout,
				R.id.list_item, values);
		listView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}
}
