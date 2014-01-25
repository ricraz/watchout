package com.hkthn.slidingwatchscreens;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	TextView time;
	TextView date;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
}
