package com.hkthn.slidingwatchscreens;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneLink extends Service {
	BluetoothAdapter ba;
	JoinThread jt;
	IOThread io;
	
	SharedPreferences prefs;
	BroadcastReceiver br;
	
	Handler handler;
	
	static final String TAG = "PhoneLink";
	
	public static final String HELP_INTENT = "com.hkthn.slidingwatchscreens.help";
	public static final String NOTIFICATION_INTENT = "com.hkthn.slidingwatchscreens.notification";
	public static final String HELP_RESULT_INTENT = "com.hkthn.slidingwatchscreens.help_result";
	public static final int NOTIFICATION_ID = 300;
	
	public static final String UUID = "7bcc1440-858a-11e3-baa7-0800200c9a66";
	
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
					if(io != null){
						byte[] bytes = new byte[10];
						io.write(bytes);
					}
				}
			}
		};
		IntentFilter intf = new IntentFilter();
		intf.addAction(HELP_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(br, intf);
		
		Notification n = new Notification();
		Intent startSettings = new Intent(this, BluetoothSetup.class);
		startSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, -1, startSettings, 0);
		n.setLatestEventInfo(this, "Link to phone is running", "Phone link active", pi);
		this.startForeground(NOTIFICATION_ID, n);
		
		ba = BluetoothAdapter.getDefaultAdapter();
		
		handler = new Handler(new Handler.Callback(){
			@Override
			public boolean handleMessage(Message msg) {
				//Only thing to handle is "read" data, so it's ok
				return false;
			}
		});
	
		attemptToJoin();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(jt != null){
			try {
				jt.cancel();
			} catch (Exception e) {}
		} 
		
		if(io != null){
			try {
				io.cancel();
			} catch (Exception e) {}
		}
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
	
	private void handleSocketConnection(BluetoothSocket socket){
		log("Socket connection created; not necessarily connected yet");
		io = new IOThread(socket);
		io.run();
	}
	
	private void attemptToJoin(){
		//Attempt to join
		if(!prefs.getString("pairedAddress", "null").equals("null")){
			String macAddress = prefs.getString("pairedAddress", "null");
			jt = new JoinThread(ba.getRemoteDevice(macAddress));
			jt.start();
		} else {
			Toast.makeText(this, "Please set a device in settings first", Toast.LENGTH_LONG).show();
			this.stopSelf();
		}
	}
	
	private class IOThread extends Thread {
		private final BluetoothSocket bs;
		private final InputStream is;
		private final OutputStream os;
		
		public IOThread(BluetoothSocket socket){
			log("IOThread created");
			bs = socket;
			InputStream in = null;
			OutputStream out = null;
			
			try {
				in = bs.getInputStream();
				out = bs.getOutputStream();
			} catch (IOException e) {}
			is = in;
			os = out;
		}
		
		public void run(){
			log("Running IOThread...");
			byte[] readBuffer = new byte[1024];
			int bytesIn;
			
			while(true){
				try {
					bytesIn = is.read(readBuffer);
					handler.obtainMessage(1, bytesIn, -1, readBuffer);
					//Send to UI
				} catch (Exception e) {
					log("IOThread done; connection lost");
					this.cancel();
					attemptToJoin();
					break; //Done!
				}
			}
		}
		
		public void write(byte[] bytesOut){
			log("Writing bytes to output streams");
			try {
				os.write(bytesOut);
			} catch (Exception e) {}
		}
		
		public void cancel(){
			log("Cancelling IOThread...");
			try {
				bs.close();
			} catch (IOException e) {}
		}
	}
	
	private class JoinThread extends Thread {
		private final BluetoothSocket aWildSockAppeared;
		private final BluetoothDevice yayDevice;
		
		public JoinThread(BluetoothDevice bd){
			log("Create BluetoothSocket from UUID");
			BluetoothSocket temp = null;
			yayDevice = bd;
			
			try {
				temp = bd.createRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
			} catch (Exception e) {}
			aWildSockAppeared = temp;
		}
		
		public void run(){
			log("Trying to connect...");
			ba.cancelDiscovery();
			
			try {
				aWildSockAppeared.connect();
			} catch (Exception e) {
				try {
					aWildSockAppeared.close();
				} catch (Exception e1) {}
				return;
			}
			
			handleSocketConnection(aWildSockAppeared);
		}
		
		public void cancel(){
			log("Cancelling BluetoothSocket connection...");
			try {
				aWildSockAppeared.close();
			} catch (Exception e) {}
		}
	}
}
