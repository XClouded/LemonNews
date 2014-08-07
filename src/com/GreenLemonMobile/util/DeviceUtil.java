
package com.GreenLemonMobile.util;

import java.util.UUID;

import com.GreenLemonMobile.util.CommonUtil.MacAddressListener;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

public final class DeviceUtil {

    private static final String MATCHER_TEST_KEY = "test-keys";

    private static final String MATCHER_RELEASE_KEY = "release-keys";

    private static final String MATCHER_SDK = "sdk";

    private DeviceUtil() {
    }

    public static boolean isEmulator() {

        boolean isEmulator = false;

        final String model = Build.MODEL;
        final String product = Build.PRODUCT;
        final String tags = Build.TAGS;
        final String fingerprint = Build.FINGERPRINT;

        Log.d("Device", "Build.MODEL = " + Build.MODEL);
        Log.d("Device", "Build.PRODUCT= " + Build.PRODUCT);
        Log.d("Device", "Build.DEVICE = " + Build.DEVICE);
        Log.d("Device", "Build.BRAND = " + Build.BRAND);
        Log.d("Device", "Build.TAGS = " + Build.TAGS);
        Log.d("Device", "Build.FINGERPRINT = " + Build.FINGERPRINT);

        isEmulator = match(model, MATCHER_SDK) || match(product, MATCHER_SDK)
                || match(tags, MATCHER_TEST_KEY) || !match(tags, MATCHER_RELEASE_KEY)
                || match(fingerprint, MATCHER_TEST_KEY) || !match(fingerprint, MATCHER_RELEASE_KEY);

        return isEmulator;
    }

    public static String getMODEL(){
        return Build.BRAND+"/"+Build.MODEL;
//        final String fingerprint = Build.FINGERPRINT;
    }

    private static boolean match(String input, String pattern) {
        return input.contains(pattern);
    }

    /**
     * 获取UUID <br>
     * 2011-3-9
     *
     * @param
     * @return String uuid
     */
    public static String getUUID() {
        final UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * 获取到移动设备的IMEI号, <br>
     * 2011-1-5 刘卫欢 增加
     *
     * @param context
     * @return
     */
    public static String getDeviceImei(Context context) {
        String imei = null;
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            imei = tm.getDeviceId();
        }
        return imei;
    }


    public static String getManufacturerInfo() {
        return System.getProperty("ro.product.manufacturer");
    }

    public static String getModelInfo() {
        return android.os.Build.MODEL;
    }
    
	/**
	 * 取得WIFI MAC地址（新）
	 */
	public synchronized static void getLocalMacAddress(final MacAddressListener listener) {

		if (Log.D) {
			Log.d("Temp", "getMacAddress() -->> ");
		}

		final WifiManager wifi = (WifiManager) MyApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
		final WifiInfo info = wifi.getConnectionInfo();
		String macAddress = info.getMacAddress();

		if (Log.D) {
			Log.d("Temp", "getMacAddress() macAddress without open -->> " + macAddress);
		}

		if (null != macAddress) {
			listener.setMacAddress(macAddress);
		} else {

			final Object waiter = new Object();

			// 线程
			Thread thread = new Thread() {

				@Override
				public void run() {

					if (Log.D) {
						Log.d("Temp", "run() -->> ");
					}

					wifi.setWifiEnabled(true);// 打开WIFI

					if (Log.D) {
						Log.d("Temp", "run() setWifiEnabled -->> true");
					}

					String macAddress = null;
					int times = 0;
					while (null == (macAddress = wifi.getConnectionInfo().getMacAddress()) && times < 60) {
						times++;
						synchronized (waiter) {
							try {
								if (Log.D) {
									Log.d("Temp", "getMacAddress() wait start 500 -->> ");
								}
								waiter.wait(500);
								if (Log.D) {
									Log.d("Temp", "getMacAddress() wait end 500 -->> ");
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

					wifi.setWifiEnabled(false);// 关闭WIFI

					if (Log.D) {
						Log.d("Temp", "run() setWifiEnabled -->> false");
					}

					if (Log.D) {
						Log.d("Temp", "getMacAddress() macAddress with open -->> " + macAddress);
					}

					listener.setMacAddress(macAddress);
				}

			};
			thread.start();

		}

	}    

	/**
	 * 取得DeviceId
	 */
	public static String getDeviceId() {
		TelephonyManager tm = (TelephonyManager) MyApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 检测SDcard是否存在
	 * 
	 * @return true:存在、false:不存在
	 */
	public static boolean checkSDcard() {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}    
}
