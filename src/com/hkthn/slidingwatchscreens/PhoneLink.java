package com.hkthn.slidingwatchscreens;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class PhoneLink extends Service {
	SharedPreferences prefs;
	BroadcastReceiver br;
	
	static final String TAG = "PhoneLink";
	
	public static final String HELP_INTENT = "com.hkthn.slidingwatchscreens.help";
	public static final String NOTIFICATION_INTENT = "com.hkthn.slidingwatchscreens.notification";
	public static final String HELP_RESULT_INTENT = "com.hkthn.slidingwatchscreens.help_result";
	public static final int NOTIFICATION_ID = 300;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(){
		super.onCreate();
		log("On create");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		br = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(HELP_INTENT)){
					log("Got help request");
					//Send to phone if possible
				}
			}
		};
		IntentFilter intf = new IntentFilter();
		intf.addAction(HELP_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(br, intf);
		
		Notification n = new Notification();
		n.setLatestEventInfo(this, "Link to phone is running", "Not yet connected", null);
		this.startForeground(300, n);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		log("On destroy");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void log(String text){
		Log.d(TAG, text);
	}
}
