package com.hkthn.slidingwatchscreens;

import java.util.ArrayList;
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
	
	ListView appView;
	List<AppButton> appList;
	PackageManager pm;
	ActivityManager am;
	LayoutInflater li;
	
	@Override
	public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
		li = super.getLayoutInflater(savedInstanceState);
		return li;
	}
	
	@Override
	public View getView() {
		return li.inflate(R.layout.fragment_app_launcher, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		pm = this.getActivity().getPackageManager();
		am = (ActivityManager) this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		
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
		
		appList = new ArrayList<AppButton>();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	private void updateUI(){
		if(appView != null){
			AppAdapter aa = new AppAdapter();
			appView.setAdapter(aa);
		}
	}
	
	private class AppListGetter extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPostExecute(Void result) {
			//Update UI
			updateUI();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
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
			return null;
		}	
	}
	
	private class AppAdapter implements ListAdapter {
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
			return null;
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
}
