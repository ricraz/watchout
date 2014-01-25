package com.hkthn.slidingwatchscreens;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	public static final float NOT_TRIGGERED = -2000f;
	
	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	
	DisplayMetrics dm;
	
	TextView time;
	TextView date;
	RelativeLayout mainLayout;
	RelativeLayout helpLayout;
	Button helpButton;
	Button dismissButton;
	
	Timer t;
	
	float startX = NOT_TRIGGERED;
	float startY = NOT_TRIGGERED;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dm = this.getResources().getDisplayMetrics();
		
		mainLayout = (RelativeLayout) this.findViewById(R.id.mainLayout);
		time = (TextView) this.findViewById(R.id.time);
		date = (TextView) this.findViewById(R.id.date);
		helpButton = (Button) this.findViewById(R.id.helpButton);
		dismissButton = (Button) this.findViewById(R.id.dismissButton);
		helpLayout = (RelativeLayout) this.findViewById(R.id.helpLayout);
		
		timeFormat = new SimpleDateFormat("h:mm", Locale.US);
		dateFormat = new SimpleDateFormat("EEEE MMM d", Locale.US);

		mainLayout.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				float deltaX = 0;
				float deltaY = 0;
				if(startX != NOT_TRIGGERED){
					deltaX = startX - event.getRawX();
					deltaY = startY - event.getRawY();
				}
				switch(event.getAction()){
				case MotionEvent.ACTION_UP:
					log("Action up");
					break;
				case MotionEvent.ACTION_DOWN:
					startX = event.getRawX();
					startY = event.getRawY();
					helpLayout.setVisibility(View.VISIBLE);
					helpLayout.offsetTopAndBottom(-dm.heightPixels);
					log("Action down");
					break;
				case MotionEvent.ACTION_MOVE:
					helpLayout.setVisibility(View.VISIBLE);
					if(deltaY > 20){
						helpLayout.offsetTopAndBottom((int)deltaY);
					}
					log("Action move");
					break;
				}
				return true;
			}
		});
		
		dismissButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				helpLayout.setVisibility(View.GONE);
			}	
		});
		
		helpButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
			}
		});
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		t = new Timer();
		
		t.schedule(new TimerTask(){
			@Override
			public void run() {
				updateTime();
			}
		}, 0, 1000);
	}
	
	void updateTime(){
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Calendar c = GregorianCalendar.getInstance();
				time.setText(timeFormat.format(c.getTime()));
				date.setText(dateFormat.format(c.getTime()));
			}
		});
	}
	
	@Override
	public void onPause(){
		super.onPause();
		t.cancel();
		t.purge();
	}
	
	public void log(String text){
		Log.d(TAG, text);
	}
}
