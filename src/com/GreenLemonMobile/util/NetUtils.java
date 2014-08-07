package com.GreenLemonMobile.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetUtils {

	public static final int NO_NET = 2147483647;
	public static final int UNKNOWN = 2147483646;
	public static final int WIFI = 2147483645;
	public static final int ROAMING = 2147483644;

	public static int getType() {

		int result = NO_NET;

		try {

			ConnectivityManager cm = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();

			if (netInfo != null && netInfo.isConnected() && netInfo.isAvailable()) {
				// if (netInfo.isRoaming()) {
				// // 漫游
				// }
				if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
					if (Log.D) {
						Log.d("Temp", "netInfo.getType() == ConnectivityManager.TYPE_MOBILE -->> ");
					}
					TelephonyManager tm = (TelephonyManager) MyApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
					result = tm.getNetworkType();
				} else if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					if (Log.D) {
						Log.d("Temp", "netInfo.getType() == ConnectivityManager.TYPE_WIFI -->> ");
					}
					result = WIFI;
				} else {
					if (Log.D) {
						Log.d("Temp", "netInfo.getType() == ConnectivityManager.UNKNOWN -->> ");
					}
					result = UNKNOWN;
				}
			} else {
				if (Log.D) {
					Log.d("Temp", "netInfo.getType() == ConnectivityManager.NO_NET -->> ");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (Log.D) {
			Log.d("Temp", "getType() result -->> " + result);
		}
		if (Log.D) {
			Log.d("Temp", "getType() result toTypeName() -->> " + toTypeName(result));
		}
		return result;
	}

	public static String toTypeName(int code) {
		switch (code) {
		case WIFI:
			return "WIFI";
		case NO_NET:
			return "NO_NET";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GPRS";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
//		case TelephonyManager.NETWORK_TYPE_HSDPA:
//			return "HSDPA";
//		case TelephonyManager.NETWORK_TYPE_HSUPA:
//			return "HSUPA";
//		case TelephonyManager.NETWORK_TYPE_HSPA:
//			return "HSPA";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "CDMA - EvDo rev. 0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "CDMA - EvDo rev. A";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "CDMA - 1xRTT";
		default:
			return "UNKNOWN";
		}
	}

	public static String getProxyHost() {
		if (getType() != TelephonyManager.NETWORK_TYPE_EDGE) {
			return null;
		}
		String defaultHost = android.net.Proxy.getDefaultHost();
		if (Log.D) {
			Log.d("Temp", "getProxyHost() -->> " + defaultHost);
		}
		return defaultHost;
	}

}
