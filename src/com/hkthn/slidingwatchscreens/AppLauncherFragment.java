package com.hkthn.slidingwatchscreens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AppLauncherFragment extends Fragment {
	private class AppButton {
		Intent launchIntent;
		String label;
		Drawable icon;
		
		public AppButton(Intent launchIntent, String label, Drawable icon){
			this.launchIntent = launchIntent;
			this.label = label;
			this.icon = icon;
		}

		public Intent getLaunchIntent() {
			return launchIntent;
		}

		public String getLabel() {
			return label;
		}

		public Drawable getIcon() {
			return icon;
		}
	}
	
	public static final String TAG = "AppLauncherFragment";
	
	ListView appView;
	PackageManager pm;
	LayoutInflater li;
	ActivityManager am;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		ViewGroup mainView = (ViewGroup) inflater.inflate(R.layout.fragment_app_launcher, container, false);
		return mainView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		pm = this.getActivity().getPackageManager();
		am = (ActivityManager) this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		
		li = this.getLayoutInflater(savedInstanceState);
		
		AppListGetter getter = new AppListGetter();
		getter.execute();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		appView = (ListView) view.findViewById(R.id.launcher);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	private void updateUI(List<AppButton> list){
		if(appView != null){
			AppAdapter aa = new AppAdapter(list);
			appView.setAdapter(aa);
		}
	}
	
	private class AppListGetter extends AsyncTask<Void, Void, List<AppButton>>{
		@Override
		protected void onPostExecute(List<AppButton> result) {
			//Update UI
			updateUI(result);
		}

		@Override
		protected List<AppButton> doInBackground(Void... arg0) {
			List<AppButton> appList = new ArrayList<AppButton>();
			List<PackageInfo> pi = pm.getInstalledPackages(0);
			for(PackageInfo p : pi){
				Intent launchIntent = pm.getLaunchIntentForPackage(p.packageName);
				if(launchIntent != null){
					try {
						appList.add(new AppButton(launchIntent, (String) pm.getApplicationLabel(pm.getApplicationInfo(p.packageName, 0)), pm.getApplicationIcon(p.packageName)));
					} catch (Exception e) {
					}
				}
			}
			Collections.sort(appList, new Comparator<AppButton>(){
				@Override
				public int compare(AppButton arg0, AppButton arg1) {
					return arg0.label.compareTo(arg1.label);
				}
			});
			return appList;
		}	
	}
	
	private class AppAdapter implements ListAdapter {
		List<AppButton> appList;
		
		public AppAdapter(List<AppButton> list){
			this.appList = list;
		}
		
		@Override
		public int getCount() {
			return appList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return appList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public int getItemViewType(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int pos, View convertView, ViewGroup parentView) {
			View newView = convertView;
			if(convertView == null){
				newView = li.inflate(R.layout.app_button, null);
			} 
			((TextView)newView.findViewById(R.id.appText)).setText(appList.get(pos).getLabel());
			((ImageView)newView.findViewById(R.id.appIcon)).setImageDrawable(appList.get(pos).getIcon());
			newView.findViewById(R.id.appButton).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					AppLauncherFragment.this.startActivity(appList.get(pos).getLaunchIntent());
				}
			});
			return newView;
		}

		@Override
		public int getViewTypeCount() {
			return appList.size();
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isEmpty() {
			return appList.isEmpty();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
		}
	}
	
	private void log(String text){
		Log.d(TAG, text);
	}
}
