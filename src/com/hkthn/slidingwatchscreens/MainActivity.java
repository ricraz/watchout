package com.hkthn.slidingwatchscreens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class MainActivity extends FragmentActivity {
	public static final String TAG = "MainActivity";
	public static final int SLIDE_PAGES = 2;
	
	ViewPager pager;
	PagerAdapter adapter;
	
	BroadcastReceiver br;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_viewpager);
		
		pager = (ViewPager) this.findViewById(R.id.mainPager);
		adapter = new SlideScreenPagerAdapter(this.getSupportFragmentManager());
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(new OnPageChangeListener(){
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				log("Page selected: " + arg0);
			}
		});
		pager.setCurrentItem(0);
		
		br = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				//Send intent to the watch activity and switch back to that
				pager.setCurrentItem(adapter.getCount() - 2, true);
				((SlideScreenPagerAdapter)adapter).sendIntentToHome(intent);
			}
		};
		IntentFilter intf = new IntentFilter();
		intf.addAction(PhoneLink.ACK_INTENT);
		intf.addAction(PhoneLink.NOTIFICATION_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(br, intf);
	}

	@Override
	public void onBackPressed(){
		if(pager.getCurrentItem() == adapter.getCount() - 2){
			super.onBackPressed();
		} else {
			pager.setCurrentItem(0);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	private void log(String text){
		Log.d(TAG, text);
	}
	
	private class SlideScreenPagerAdapter extends FragmentStatePagerAdapter {
		WatchScreenFragment watchScreen;
		AppLauncherFragment appLauncher;
		
		public SlideScreenPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if(position == getCount() - 1){ //last position --> app launcher
				log("Returning applauncher");
				if(appLauncher == null){
					appLauncher = new AppLauncherFragment();
				}
				return appLauncher;
			} else if (position == getCount() - 2){ //second to last position --> watch screen
				log("Returning watchscreen");
				if(watchScreen == null){
					watchScreen = new WatchScreenFragment();
				} 
				return watchScreen;
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			return SLIDE_PAGES;
		}
		
		public void sendIntentToHome(Intent i){
			if(watchScreen == null){
				watchScreen = new WatchScreenFragment();
			}
			watchScreen.newReceive(i);
		}
	}
}
