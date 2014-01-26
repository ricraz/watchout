package com.hkthn.slidingwatchscreens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WatchScreenFragment extends Fragment {
	public static final float NOT_TRIGGERED = -2000f;
	public static final String TAG = "WatchScreenFragment";

	SimpleDateFormat timeFormat;
	SimpleDateFormat dateFormat;
	
	LayoutInflater li;

	View rootView;

	TextView time;
	TextView date;
	RelativeLayout mainLayout;
	
	LinearLayout helpLayout;
	Button helpButton;
	Button dismissButton;
	
	LinearLayout notificationLayout;
	TextView notificationText;
	TextView notificationText2;
	
	ImageView hideNL;
	ImageView dismissAll;
	LinearLayout nContainer;
	LinearLayout nView;

	DisplayMetrics dm;

	List<NotificationContainer> allNotifications;

	Timer t;

	float startX = NOT_TRIGGERED;
	float startY = NOT_TRIGGERED;

	private class NotificationContainer {
		String mainText;
		String infoText;
		String packageName;
		String tag;
		String id;

		public NotificationContainer(String mainText, String infoText,
				String packageName, String tag, String id) {
			super();
			this.mainText = mainText;
			this.infoText = infoText;
			this.packageName = packageName;
			this.tag = tag;
			this.id = id;
		}

		public String getMainText() {
			return mainText;
		}

		public String getInfoText() {
			return infoText;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getTag() {
			return tag;
		}

		public String getId() {
			return id;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup mainView = (ViewGroup) inflater.inflate(
				R.layout.activity_main, container, false);
		return mainView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		dm = this.getResources().getDisplayMetrics();
		li = this.getLayoutInflater(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();

		log("On pause");

		if (t != null) {
			t.cancel();
			t.purge();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				WatchScreenFragment.this.updateTime();
			}
		}, 0, 500);

		log("on resume");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
		time = (TextView) view.findViewById(R.id.time);
		date = (TextView) view.findViewById(R.id.date);
		helpButton = (Button) view.findViewById(R.id.helpButton);
		dismissButton = (Button) view.findViewById(R.id.dismissButton);
		helpLayout = (LinearLayout) view.findViewById(R.id.helpLayout);
		notificationLayout = (LinearLayout) view
				.findViewById(R.id.notificationLayout);
		notificationText = (TextView) view.findViewById(R.id.text);
		notificationText2 = (TextView) view.findViewById(R.id.text2);
		hideNL = (ImageView) view.findViewById(R.id.hideNL);
		dismissAll = (ImageView) view.findViewById(R.id.dismissAll);
		nContainer = (LinearLayout) view.findViewById(R.id.nContainer);
		nView = (LinearLayout) view.findViewById(R.id.allNotifications);

		mainLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				float deltaY = 0;
				if (startX != NOT_TRIGGERED) {
					deltaY = startY - event.getRawY();
				}
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					log("Action up");
					if (-deltaY > toPixel(50)) {
						helpLayout.setVisibility(View.VISIBLE);
					} else if (deltaY > toPixel(50)){
						notificationLayout.setVisibility(View.GONE);
						nView.setVisibility(View.VISIBLE);
					} else {
						notificationLayout.setVisibility(View.GONE);
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
		
		mainLayout.setLongClickable(true);

		dismissButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				helpButton.setText("HELP");
				helpLayout.setVisibility(View.GONE);
			}
		});

		helpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(PhoneLink.HELP_INTENT);
				LocalBroadcastManager.getInstance(
						WatchScreenFragment.this.getActivity())
						.sendBroadcast(i);
			}
		});
		
		dismissAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nContainer.removeAllViews();
				allNotifications = new ArrayList<NotificationContainer>();
			}
		});
		
		hideNL.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nView.setVisibility(View.GONE);
			}
		});

		timeFormat = new SimpleDateFormat("h:mm", Locale.US);
		dateFormat = new SimpleDateFormat("EEEE, MMM d", Locale.US);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		allNotifications = new ArrayList<NotificationContainer>();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void newReceive(Intent intent) {
		log("Recieved new intent");
		if(this.getView() != null){ //Check to make sure we don't do something stupid
			if (intent.getAction().equals(PhoneLink.ACK_INTENT)) {
				log("Help request acknowledged");
				helpButton.setText("SENT");
			} else if (intent.getAction().equals(PhoneLink.NOTIFICATION_INTENT)) {
				log("Notification intent received");
				String data = intent.getStringExtra("data");
				log("Data: " + data);
				
				String[] allData = data.split("\\|");
				
				//Set on main watchface
				notificationLayout.setVisibility(View.VISIBLE);
				notificationText.setText(allData[0]);
				notificationText.setText(allData[0]);
				
				//Set in notification container below
				
				NotificationContainer nc = new NotificationContainer(allData[0], allData[1], allData[2], allData[3], allData[4]);
				allNotifications.add(nc);
				addNotification(nc);
			}
		}
	}
	
	void addNotification(final NotificationContainer nc){
		View nnc = li.inflate(R.layout.notification_layout, null);
		((TextView)nnc.findViewById(R.id.mainText)).setText(nc.getMainText());
		if(nc.getInfoText() != null && !nc.getInfoText().equals("null")){
			((TextView)nnc.findViewById(R.id.infoText)).setText(nc.getInfoText());
		} else {
			((TextView)nnc.findViewById(R.id.infoText)).setVisibility(View.GONE);
		}
		((TextView)nnc.findViewById(R.id.timeText)).setText(timeFormat.format(GregorianCalendar.getInstance().getTime()));
		
		nnc.findViewById(R.id.dismissNotification).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nContainer.removeView((View) v.getParent());
				allNotifications.remove(nc);
				
				//Send a dismiss intent
				Intent i = new Intent();
				i.setAction(PhoneLink.DISMISS_INTENT);
				i.putExtra("data", nc.getPackageName() + "|" + nc.getTag() + "|" + nc.getId());
				LocalBroadcastManager.getInstance(
						WatchScreenFragment.this.getActivity())
						.sendBroadcast(i);
			}
		});
		
		nnc.findViewById(R.id.hideNotification).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				nContainer.removeView((View) v.getParent());
				allNotifications.remove(nc);
			}
		});
		
		nContainer.addView(nnc);
	}

	void updateTime() {
		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Calendar c = GregorianCalendar.getInstance();
				time.setText(timeFormat.format(c.getTime()));
				date.setText(dateFormat.format(c.getTime()));
			}
		});
	}

	private int toPixel(int pixels) {
		return (int) Math.ceil(pixels * dm.density);
	}

	private void log(String text) {
		Log.d(TAG, text);
	}
}
