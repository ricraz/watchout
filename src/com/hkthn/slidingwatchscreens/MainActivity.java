package com.hkthn.slidingwatchscreens;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	public static final float NOT_TRIGGERED = -2000f;
	
	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	
	BroadcastReceiver br;
	
	DisplayMetrics dm;
	
	TextView time;
	TextView date;
	RelativeLayout mainLayout;
	LinearLayout helpLayout;
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
		helpLayout = (LinearLayout) this.findViewById(R.id.helpLayout);
		
		timeFormat = new SimpleDateFormat("h:mm", Locale.US);
		dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.US);
		
		br = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(PhoneLink.ACK_INTENT)){
					log("Help request acknowledged");
					helpButton.setText("SENT");
				} else if(intent.getAction().equals(PhoneLink.NOTIFICATION_INTENT)){
					log("Notification intent received");
				}
			}
		};
		IntentFilter intf = new IntentFilter();
		intf.addAction(PhoneLink.ACK_INTENT);
		intf.addAction(PhoneLink.NOTIFICATION_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(br, intf);

		mainLayout.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				float deltaY = 0;
				if(startX != NOT_TRIGGERED){
					deltaY = startY - event.getRawY();
				}
				switch(event.getAction()){
				case MotionEvent.ACTION_UP:
					log("Action up");
					if(-deltaY > toPixel(50)){
						helpLayout.setVisibility(View.VISIBLE);
					}
					break;
				case MotionEvent.ACTION_DOWN:
					startX = event.getRawX();
					startY = event.getRawY();
					log("Action down");
					break;
				case MotionEvent.ACTION_MOVE:
					log("Action move");
					break;
				}
				return true;
			}
		});
		
		dismissButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				helpButton.setText("HELP");
				helpLayout.setVisibility(View.GONE);
			}	
		});
		
		helpButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(PhoneLink.HELP_INTENT);
				LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(i);
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
	
	private void log(String text){
		Log.d(TAG, text);
	}
	
	private int toPixel(int pixels){
		return (int) Math.ceil(pixels * dm.density);
	}
}
