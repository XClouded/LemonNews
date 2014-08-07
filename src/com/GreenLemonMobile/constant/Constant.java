package com.GreenLemonMobile.constant;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.GreenLemonMobile.util.MyApplication;

public class Constant {
	public final static String sCharset = "UTF-8";
	
	public final static String DEFAULT_SHARE_PREFERENCE = "default_preference";//默认shared preference文件名	
	public static String DIR_ROOT_NAME = MyApplication.getInstance().getPackageName();
	
	static {
		PackageManager pm = MyApplication.getInstance().getPackageManager();
		
		try {
			ApplicationInfo ai = pm.getApplicationInfo(DIR_ROOT_NAME, PackageManager.GET_META_DATA);
			if (ai != null) {
				DIR_ROOT_NAME = pm.getApplicationLabel(ai).toString();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
