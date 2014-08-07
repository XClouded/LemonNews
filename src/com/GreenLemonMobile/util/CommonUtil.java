package com.GreenLemonMobile.util;

import com.GreenLemonMobile.constant.Constant;

import android.content.Context;
import android.content.SharedPreferences;

public class CommonUtil {
	public static SharedPreferences getDefaultSharedPreferences() {
		SharedPreferences sharedPreferences = MyApplication.getInstance().getSharedPreferences(Constant.DEFAULT_SHARE_PREFERENCE, Context.MODE_PRIVATE);
		return sharedPreferences;
	}
	
	/**
	 * 取得Mac后，执行的函数
	 */
	public interface MacAddressListener {

		void setMacAddress(String macAddress);

	}	
}
