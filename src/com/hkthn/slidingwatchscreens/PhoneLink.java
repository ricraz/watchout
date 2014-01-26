package com.hkthn.slidingwatchscreens;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android.app.Notification;
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
	
	Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//Object always going to be a string
			log("Handler received message");
			String s = (String) msg.obj;
			handleInput(s);
		}
	};
	
	static final String TAG = "PhoneLink";
	
	public static final String EOS = "<!EOS!>";
	
	public static final String HELP_INTENT = "com.hkthn.slidingwatchscreens.help";
	public static final String NOTIFICATION_INTENT = "com.hkthn.slidingwatchscreens.notification";
	public static final String DISMISS_INTENT = "com.hkthn.slidingwatchscreens.dismiss";
	public static final String HELP_RESULT_INTENT = "com.hkthn.slidingwatchscreens.help_result";
	public static final String ACK_INTENT = "com.hkthn.slidingwatchscreens.help_ack";
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
						io.write("HELP_REQUEST|Generic help request");
					}
				} else if(intent.getAction().equals(DISMISS_INTENT)){
					log("Got dismiss request");
					//Send to phone if possible
					if(io != null){
						io.write("DISMISS|" + intent.getStringExtra("data"));
					}
				}
			}
		};
		IntentFilter intf = new IntentFilter();
		intf.addAction(HELP_INTENT);
		intf.addAction(DISMISS_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(br, intf);
		
		Notification n = new Notification();
		Intent startSettings = new Intent(this, BluetoothSetup.class);
		startSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, -1, startSettings, 0);
		n.setLatestEventInfo(this, "Link to phone is running", "Phone link active", pi);
		this.startForeground(NOTIFICATION_ID, n);
		
		ba = BluetoothAdapter.getDefaultAdapter();
	
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
	
	private void handleInput(String dataIn){
		log("Data in: " + dataIn);
		dataIn = (String) dataIn.subSequence(0, dataIn.indexOf(EOS));
		int barPos = dataIn.indexOf("|");
		if(barPos != -1){
			String requestType = dataIn.substring(0, barPos);
			String requestData = dataIn.substring(barPos+1);
			log("Request type: " + requestType);
			log("Request data: " + requestData);
			if(requestType.equals("HELP_ACK")){
				Intent i = new Intent();
				i.setAction(ACK_INTENT);
				LocalBroadcastManager.getInstance(this).sendBroadcast(i);
			} else if (requestType.equals("NOTIFICATION")){
				Intent i = new Intent();
				i.setAction(NOTIFICATION_INTENT);
				i.putExtra("data", requestData);
				LocalBroadcastManager.getInstance(this).sendBroadcast(i);
			}
		} else {
			log("Error! Improper formatting");
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
			StringBuilder stringToSend = new StringBuilder();
			byte[] readBuffer = new byte[1024];
			int newBytes;

			while(true){
				try {
					while((newBytes = is.read(readBuffer)) != -1){ //So long as we're not at the end of stream
						stringToSend.append(new String(readBuffer, 0, newBytes, Charset.defaultCharset()));
						int eosIndex = stringToSend.indexOf(EOS);
						if(eosIndex != -1){
							String toSend = stringToSend.toString();
							Message m = h.obtainMessage(1, toSend);
							h.sendMessage(m);
							stringToSend = new StringBuilder();
						}
					}
				} catch (Exception e) {
					log("IOThread done; connection lost");
					this.cancel();
					attemptToJoin();
					break; //done -- connection lost
				}
			}
		}
		
		public void write(String dataIn){
			log("Writing bytes to output streams");
			dataIn = dataIn + EOS;
			try {
				byte[] dataBytes = dataIn.getBytes();
				os.write(dataBytes);
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
		
		public JoinThread(BluetoothDevice bd){
			log("Create BluetoothSocket from UUID");
			BluetoothSocket temp = null;
			
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
