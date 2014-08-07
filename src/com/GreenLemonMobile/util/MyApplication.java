package com.GreenLemonMobile.util;

import android.app.Application;

public class MyApplication extends Application {

	private static MyApplication instance = null;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		if (getInstance() == null)
			setInstance(this);
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	public static MyApplication getInstance() {
		return instance;
	}

	public static void setInstance(MyApplication instance) {
		MyApplication.instance = instance;
	}

}
