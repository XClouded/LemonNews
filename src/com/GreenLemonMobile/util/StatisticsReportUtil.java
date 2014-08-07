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

	private static String paramStr;// ����������������ַ���
	private static String paramStrWithOutDeviceUUID;// ����������������ַ���������deviceUUID��
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
	  *��һ����CPU���ͺţ��ڶ�����CPU��Ƶ��
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
	 * �豸��Ϣ��,���豸��һ�ΰ�װʱʹ�á�
	 *
	 * @param mContext
	 * @return
	 */

	public static String getDeviceInfoStr() {

		JSONObject obj = new JSONObject();

		try {

			// �ⲿ�����ݽϿ��ܱ����棬����1.0.3��ʼֻ����deviceUUID����Ҫ������device��Ϣ������������Ϊ���а�����deviceUUID��
			obj.put("uuid", readDeviceUUID());// �豸Ψһ��
			obj.put("platform", 100);// ƽ̨
			obj.put("brand", spilitSubString(Build.MANUFACTURER, 12));// ������������
			obj.put("model", spilitSubString((Build.MODEL), 12));// �������ͺ�
			Display display = ((WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			obj.put("screen", display.getWidth() + "*" + display.getHeight());// �豸�ֱ���
			//obj.put("&appId=",Configuration.getProperty(Configuration.APP_ID, "1"));

			// �ⲿ�ֱ���ÿ������
			obj.put("clientVersion", getSoftwareVersionName());// ����汾��
			obj.put("osVersion", Build.VERSION.RELEASE);// android �汾��
			//obj.put("partner", Configuration.getProperty(Configuration.PARTNER, ""));// ������
			obj.put("nettype", getNetworkTypeName(MyApplication.getInstance()));// ��������

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
	 * �û����Ӵ��ύ����Ϣ����ÿһ�����Ӷ����ύ��
	 *
	 * @param mContext
	 *            ������
	 * @return
	 */
	public static String getReportString(boolean mustDeviceUUID) {
		if (//
		mustDeviceUUID || // �������deviceUUID�ģ�����
				null != getValidDeviceUUIDByInstant() // ���˲���ܹ��õ�deviceUUIDӦ�ṩ������paramStr
		) {
			return getParamStr();
		}
		// �����ṩ�������� paramStr
		return getParamStrWithOutDeviceUUID();
	}

	/**
	 * ��ȡͳһ��������������ַ�����������
	 */
	private static String getParamStr() {
		if (!TextUtils.isEmpty(paramStr)) {
			if (Log.D) {
				Log.d("Temp", "getParamStr() -->> " + paramStr);
			}
			return paramStr;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("&uuid=").append(readDeviceUUID());// �豸Ψһ��
		sb.append(getParamStrWithOutDeviceUUID());// ƽ̨��Ϣ
		paramStr = sb.toString();
		if (Log.D) {
			Log.d("Temp", "getParamStr() create -->> " + paramStr);
		}
		return paramStr;
	}

	/**
	 * ��ȡͳһ��������������ַ���������DeviceUUID��
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
				spilitSubString(getSoftwareVersionName(), 12));// ����汾��
		sb.append("&os=").append("android");// ƽ̨
		sb.append("&client=").append("android");// ƽ
		//Configuration
		//sb.append("&appId=").append(Configuration.getProperty(Configuration.APP_ID, "1"));
		sb.append("&osVersion=").append(
				spilitSubString(Build.VERSION.RELEASE, 12));// android�汾��
		Display display = ((WindowManager) MyApplication.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		sb.append("&screen=").append(
				display.getWidth() + "*" + display.getHeight());// �豸�ֱ���
//		sb.append("&uuid=").append(DeviceUtil.getDeviceImei(MyApplication.getInstance()));// uuid
		paramStrWithOutDeviceUUID = sb.toString();
		if (Log.D) {
			Log.d("Temp", "getParamStrWithOutDeviceUUID() create -->> " + paramStrWithOutDeviceUUID);
		}
		return paramStrWithOutDeviceUUID;
	}

	/**
	 * ��ָ�����Ƚ�ȡ��
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
			// �̼߳���
			synchronized (this) {// ͬ��
				macAddress = str;
				already = true;
				this.notifyAll();
			}
		}
	};

	private static String macAddress;// ����õ���Mac��ַ

	private static boolean already;// �����ж��Ƿ��Ѿ��õ�MACʹ�߳�����ȴ�

	/**
	 * ��ȡdeivceUUID��������Ҫ�ȴ�
	 */
	public static String readDeviceUUID() {

		String deivceUUIDCache = getValidDeviceUUIDByInstant();
		if (null != deivceUUIDCache) {// ������һ�汾�����ˣ��������Ч�ģ��������»�ȡ
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
		// - - - - - ��ȡ

		// ��ȡdeviceId
		String deviceId = DeviceUtil.getDeviceId();
		if (!TextUtils.isEmpty(deviceId)) {// ����
			deviceId = deviceId.trim().replaceAll("-", "");// ȥ����-���Ա�֤���շָ�����Ч��
		}

		// ��ȡMAC��ַ
		// String wifiMAC = CommonUtil.getLocalMacAddress();
		String wifiMAC = macAddress;
		if (null == wifiMAC) {
			DeviceUtil.getLocalMacAddress(macAddressListener);
			// �߳���ͣ
			synchronized (macAddressListener) {// ͬ��
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

		if (!TextUtils.isEmpty(wifiMAC)) {// ����
			wifiMAC = wifiMAC.trim().replaceAll("-|\\.|:", "");// ȥ����-���Ա�֤���շָ�����Ч��ȥ��WIFI�еġ�.���͡�:����
		}

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		// - - - - - ƴװ

		if (!TextUtils.isEmpty(deviceId)) {
			deivceUUID.append(deviceId);
		}

		// �ԡ�-����Ϊ�ָ���
		deivceUUID.append("-");

		if (!TextUtils.isEmpty(wifiMAC)) {
			deivceUUID.append(wifiMAC);
		}

		String deivceUUIDStr = deivceUUID.toString();

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		// - - - - - ����

		if (isValidDeviceUUID(deivceUUIDStr)) {// ���deivceUUID��Ч�Ž��л���
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
	 * ͨ�����ػ�ȡ���ж�deivceUUID�Ƿ���Ч������Ч�ŷ��ط�null�ַ���
	 */
	private static String getValidDeviceUUIDByInstant() {
		if (!TextUtils.isEmpty(deivceUUID)) {
			return deivceUUID;
		}
		SharedPreferences sharedPreferences = CommonUtil.getDefaultSharedPreferences();
		String deivceUUIDCache = sharedPreferences.getString(DEVICE_INFO_UUID, null);
		if (isValidDeviceUUID(deivceUUIDCache)) {// ������һ�汾�����ˣ��������Ч��Ҳ����
			deivceUUID = deivceUUIDCache;
			return deivceUUID;
		}
		return null;
	}

	/**
	 * �ж�deivceUUID�Ƿ���Ч
	 */
	private static boolean isValidDeviceUUID(String deivceUUID) {
		if (TextUtils.isEmpty(deivceUUID)) {// �������ǿ��ǿ϶���Ч
			return false;
		}
		String[] split = deivceUUID.split("-");
		if (split.length > 1) {// �������������2��Ϊ��Ч
			return !TextUtils.isEmpty(split[1]);// ���û��MAC��ô��Ϊ��Ч
		}
		return false;
	}

	/**
	 * �õ���ǰ�汾��Ϣ
	 */
	public static String getSoftwareVersionName() {
		PackageInfo packageInfo = getPackageInfo();
		if(null == packageInfo){
			return "";
		}
		return packageInfo.versionName;
	}

	/**
	 * �õ���ǰ�汾��Ϣ
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
