package com.GreenLemonMobile.RssReader;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		setupView();
	}

	void setupView() {
		TextView appVersion = (TextView)findViewById(R.id.app_version);
		
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			if (pi != null)
				appVersion.setText(getResources().getString(R.string.app_version) + pi.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
