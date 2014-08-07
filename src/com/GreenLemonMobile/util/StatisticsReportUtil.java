package com.GreenLemonMobile.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.GreenLemonMobile.config.Configuration;
import com.GreenLemonMobile.util.CommonUtil.MacAddressListener;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

public class StatisticsReportUtil {

	private static String paramStr;// 公共网络请求参数字符串
	private static String paramStrWithOutDeviceUUID;// 公共网络请求参数字符串（不带deviceUUID）
	private static String deivceUUID;// deivceUUID

	private final static String DEVICE_INFO_STR = "deviceInfoStr";
	private final static String DEVICE_INFO_UUID = "uuid";

	/*
	 * JSONObject infoJsonObject = new JSONObject();
infoJsonObject.put("btMac", "11:11:11:11:test");
infoJsonObject.put("imei", "test-mobile");
infoJsonObject.put("buildInfo", "test-mobile");
infoJsonObject.put("cupId", "10");
infoJsonObject.put("imsi", "A-B-C-E-D");
infoJsonObject.put("memSize", "1000");
infoJsonObject.put("networkInfo", "test-mobile");
infoJsonObject.put("sensors", "test-mobile");
infoJsonObject.put("wifiMac", "11:11:11:11:test");
	 */
	 public static long getTotalInternalMemorySize() {
	        File path = Environment.getDataDirectory();
	        StatFs stat = new StatFs(path.getPath());
	        long blockSize = stat.getBlockSize();
	        long totalBlocks = stat.getBlockCount();
	        return totalBlocks * blockSize;
	    }
	 /*
	  *第一行是CPU的型号，第二行是CPU的频率
	  */
	 public static String[] getCpuInfo() {
		    String str1 = "/proc/cpuinfo";
		    String str2="";
		    String[] cpuInfo={"",""};
		    String[] arrayOfString;
		    try {
		        FileReader fr = new FileReader(str1);
		        BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
		        str2 = localBufferedReader.readLine();
		        arrayOfString = str2.split("\\s+");
		        for (int i = 2; i < arrayOfString.length; i++) {
		            cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
		        }
		        str2 = localBufferedReader.readLine();
		        arrayOfString = str2.split("\\s+");
		        cpuInfo[1] += arrayOfString[2];
		        localBufferedReader.close();
		    } catch (IOException e) {
		    }
		    return cpuInfo;
		}
/*
 * mac
 */
	 public static String getMacInfo(){
		 String mac = "";
		       WifiManager wifiManager = (WifiManager)MyApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		       WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		    if(wifiInfo.getMacAddress()!=null){
		    	   mac=wifiInfo.getMacAddress();
		    } else {
		    	mac = "Fail";
		    }
		       return mac;
		}

	public static String getCpaPushInfoStr(){
		JSONObject obj = new JSONObject();
		String imei = DeviceUtil.getDeviceImei(MyApplication.getInstance().getApplicationContext());
		String memory = String.valueOf(getTotalInternalMemorySize());
		String[] cpu =getCpuInfo();
		String mac = getMacInfo();
		try {
			obj.put("imei", imei);
			obj.put("buildInfo", String.valueOf(Build.MODEL));
			obj.put("wifiMac", mac);
			obj.put("cupId", cpu[0]);
			obj.put("memSize", memory);
			Log.i("zhuyang", "device  cpu :"+obj.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj.toString();
	}

	public static String getScreen(){
		Display display = ((WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		String screen = display.getWidth() + "*" + display.getHeight();
		return screen;
	}
	
	/**
	 * 设备信息串,在设备第一次安装时使用。
	 *
	 * @param mContext
	 * @return
	 */

	public static String getDeviceInfoStr() {

		JSONObject obj = new JSONObject();

		try {

			// 这部分内容较可能被缓存，但自1.0.3开始只缓存deviceUUID，不要把整块device信息缓存下来，因为其中包含了deviceUUID。
			obj.put("uuid", readDeviceUUID());// 设备唯一号
			obj.put("platform", 100);// 平台
			obj.put("brand", spilitSubString(Build.MANUFACTURER, 12));// 机器的制造商
			obj.put("model", spilitSubString((Build.MODEL), 12));// 机器的型号
			Display display = ((WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			obj.put("screen", display.getWidth() + "*" + display.getHeight());// 设备分辨率
			//obj.put("&appId=",Configuration.getProperty(Configuration.APP_ID, "1"));

			// 这部分必须每次生成
			obj.put("clientVersion", getSoftwareVersionName());// 软件版本号
			obj.put("osVersion", Build.VERSION.RELEASE);// android 版本号
			//obj.put("partner", Configuration.getProperty(Configuration.PARTNER, ""));// 合作方
			obj.put("nettype", getNetworkTypeName(MyApplication.getInstance()));// 网络类型

			if (Log.D) {
				Log.d("Temp", "getDeviceInfoStr() return -->> " + obj.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return obj.toString();
	}

	public static String getNetworkTypeName(Context myContext) {
		ConnectivityManager cm = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager tm = (TelephonyManager) myContext.getSystemService(Context.TELEPHONY_SERVICE);
		String netString = null;
		NetworkInfo[] netinfo = cm.getAllNetworkInfo();
		try {
			for (int i = 0; i < netinfo.length; i++) {
				if (netinfo[i].isConnected()) {
					if (netinfo[i].getTypeName().toUpperCase().contains("MOBILE")) {
						netString = tm.getNetworkType() + "";
					} else if (netinfo[i].getTypeName().toUpperCase().contains("WIFI")) {
						netString = "WIFI";
					} else {
						netString = "UNKNOWN";
					}
				}
			}
		} catch (Exception e) {
			netString = "UNKNOWN";
		}
		if (netString == null) {
			netString = "UNKNOWN";
		}

		return netString;

	}

	/**
	 * 用户连接串提交的信息串，每一次连接都分提交。
	 *
	 * @param mContext
	 *            上下文
	 * @return
	 */
	public static String getReportString(boolean mustDeviceUUID) {
		if (//
		mustDeviceUUID || // 如果必须deviceUUID的，或者
				null != getValidDeviceUUIDByInstant() // 如果瞬间能够得到deviceUUID应提供完整的paramStr
		) {
			return getParamStr();
		}
		// 其它提供非完整的 paramStr
		return getParamStrWithOutDeviceUUID();
	}

	/**
	 * 获取统一的网络请求参数字符串（完整）
	 */
	private static String getParamStr() {
		if (!TextUtils.isEmpty(paramStr)) {
			if (Log.D) {
				Log.d("Temp", "getParamStr() -->> " + paramStr);
			}
			return paramStr;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("&uuid=").append(readDeviceUUID());// 设备唯一号
		sb.append(getParamStrWithOutDeviceUUID());// 平台信息
		paramStr = sb.toString();
		if (Log.D) {
			Log.d("Temp", "getParamStr() create -->> " + paramStr);
		}
		return paramStr;
	}

	/**
	 * 获取统一的网络请求参数字符串（不带DeviceUUID）
	 */
	private static String getParamStrWithOutDeviceUUID() {
		if (!TextUtils.isEmpty(paramStrWithOutDeviceUUID)) {
			if (Log.D) {
				Log.d("Temp", "getParamStrWithOutDeviceUUID() -->> " + paramStrWithOutDeviceUUID);
			}
			return paramStrWithOutDeviceUUID;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("&clientVersion=").append(
				spilitSubString(getSoftwareVersionName(), 12));// 软件版本号
		sb.append("&os=").append("android");// 平台
		sb.append("&client=").append("android");// 平
		//Configuration
		//sb.append("&appId=").append(Configuration.getProperty(Configuration.APP_ID, "1"));
		sb.append("&osVersion=").append(
				spilitSubString(Build.VERSION.RELEASE, 12));// android版本号
		Display display = ((WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		sb.append("&screen=").append(
				display.getWidth() + "*" + display.getHeight());// 设备分辨率
//		sb.append("&uuid=").append(DeviceUtil.getDeviceImei(MyApplication.getInstance()));// uuid
		paramStrWithOutDeviceUUID = sb.toString();
		if (Log.D) {
			Log.d("Temp", "getParamStrWithOutDeviceUUID() create -->> " + paramStrWithOutDeviceUUID);
		}
		return paramStrWithOutDeviceUUID;
	}

	/**
	 * 按指定长度截取串
	 *
	 * @param value
	 * @param length
	 * @return
	 */
	public static String spilitSubString(String value, int length) {
		if (value != null && value.length() > length) {
			value = value.substring(0, length);
		}
		return value;
	}

	private static MacAddressListener macAddressListener = new MacAddressListener() {
		@Override
		public void setMacAddress(String str) {
			// 线程继续
			synchronized (this) {// 同步
				macAddress = str;
				already = true;
				this.notifyAll();
			}
		}
	};

	private static String macAddress;// 保存得到的Mac地址

	private static boolean already;// 辅助判断是否已经得到MAC使线程无需等待

	/**
	 * 读取deivceUUID，可能需要等待
	 */
	public static String readDeviceUUID() {

		String deivceUUIDCache = getValidDeviceUUIDByInstant();
		if (null != deivceUUIDCache) {// 就算上一版本保存了，如果是无效的，照样重新获取
			if (Log.D) {
				Log.d("Temp", "readDeviceUUID() read deivceUUID -->> " + deivceUUIDCache);
			}
			return deivceUUIDCache;
		}

		if (Log.D) {
			Log.d("Temp", "readDeviceUUID() create -->> ");
		}

		StringBuilder deivceUUID = new StringBuilder();

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		// - - - - - 获取

		// 获取deviceId
		String deviceId = DeviceUtil.getDeviceId();
		if (!TextUtils.isEmpty(deviceId)) {// 修正
			deviceId = deviceId.trim().replaceAll("-", "");// 去掉“-”以保证最终分隔符有效。
		}

		// 获取MAC地址
		// String wifiMAC = CommonUtil.getLocalMacAddress();
		String wifiMAC = macAddress;
		if (null == wifiMAC) {
			DeviceUtil.getLocalMacAddress(macAddressListener);
			// 线程暂停
			synchronized (macAddressListener) {// 同步
				try {
					if (!already) {
						if (Log.D) {
							Log.d("Temp", "mac wait start -->> ");
						}
						macAddressListener.wait();
						if (Log.D) {
							Log.d("Temp", "mac wait end -->> ");
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (null == macAddress) {
				wifiMAC = "";
			} else {
				wifiMAC = macAddress;
			}
		}

		if (!TextUtils.isEmpty(wifiMAC)) {// 修正
			wifiMAC = wifiMAC.trim().replaceAll("-|\\.|:", "");// 去掉“-”以保证最终分隔符有效。去掉WIFI中的“.”和“:”。
		}

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		// - - - - - 拼装

		if (!TextUtils.isEmpty(deviceId)) {
			deivceUUID.append(deviceId);
		}

		// 以“-”作为分隔符
		deivceUUID.append("-");

		if (!TextUtils.isEmpty(wifiMAC)) {
			deivceUUID.append(wifiMAC);
		}

		String deivceUUIDStr = deivceUUID.toString();

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		// - - - - - 缓存

		if (isValidDeviceUUID(deivceUUIDStr)) {// 如果deivceUUID有效才进行缓存
			if (Log.D) {
				Log.d("Temp", "readDeviceUUID() write -->> ");
			}
			SharedPreferences sharedPreferences = CommonUtil.getDefaultSharedPreferences();
			sharedPreferences.edit().putString(DEVICE_INFO_UUID, deivceUUIDStr).commit();
		}

		if (Log.D) {
			Log.d("Temp", "readDeviceUUID() create deivceUUID -->> " + deivceUUIDStr);
		}

		return deivceUUIDStr;
	}

	/**
	 * 通过本地获取并判断deivceUUID是否有效，仅有效才返回非null字符串
	 */
	private static String getValidDeviceUUIDByInstant() {
		if (!TextUtils.isEmpty(deivceUUID)) {
			return deivceUUID;
		}
		SharedPreferences sharedPreferences = CommonUtil.getDefaultSharedPreferences();
		String deivceUUIDCache = sharedPreferences.getString(DEVICE_INFO_UUID, null);
		if (isValidDeviceUUID(deivceUUIDCache)) {// 就算上一版本保存了，如果是无效的也不行
			deivceUUID = deivceUUIDCache;
			return deivceUUID;
		}
		return null;
	}

	/**
	 * 判断deivceUUID是否有效
	 */
	private static boolean isValidDeviceUUID(String deivceUUID) {
		if (TextUtils.isEmpty(deivceUUID)) {// 整个都是空那肯定无效
			return false;
		}
		String[] split = deivceUUID.split("-");
		if (split.length > 1) {// 如果参数量不足2认为无效
			return !TextUtils.isEmpty(split[1]);// 如果没有MAC那么认为无效
		}
		return false;
	}

	/**
	 * 得到当前版本信息
	 */
	public static String getSoftwareVersionName() {
		PackageInfo packageInfo = getPackageInfo();
		if(null == packageInfo){
			return "";
		}
		return packageInfo.versionName;
	}

	/**
	 * 得到当前版本信息
	 */
	public static int getSoftwareVersionCode() {
		PackageInfo packageInfo = getPackageInfo();
		if(null == packageInfo){
			return 0;
		}
		return packageInfo.versionCode;
	}

	private static PackageInfo getPackageInfo(){
		try {
			Context cxt = MyApplication.getInstance();
			PackageInfo packageInfo = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0);
			return packageInfo;
		} catch (Exception e) {
			return null;
		}
	}

}
