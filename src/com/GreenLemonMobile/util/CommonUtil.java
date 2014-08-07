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
	 * ȡ��Mac��ִ�еĺ���
	 */
	public interface MacAddressListener {

		void setMacAddress(String macAddress);

	}	
}
