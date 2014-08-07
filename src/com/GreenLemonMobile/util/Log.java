package com.GreenLemonMobile.util;

import com.GreenLemonMobile.config.Configuration;


public class Log {

	private static boolean printLog = Configuration.sPrintLog;
	private static boolean printClassLog = Configuration.sPrintClassLog;

	public static boolean D = printLog ? Boolean.parseBoolean(Configuration.getProperty(Configuration.DEBUG_LOG, "false")) : false;
	public static boolean V = printLog ? Boolean.parseBoolean(Configuration.getProperty(Configuration.VIEW_LOG, "false")) : false;
	public static boolean I = printLog ? Boolean.parseBoolean(Configuration.getProperty(Configuration.INFO_LOG, "false")) : false;
	public static boolean W = printLog ? Boolean.parseBoolean(Configuration.getProperty(Configuration.WARN_LOG, "false")) : false;
	public static boolean E = printLog ? Boolean.parseBoolean(Configuration.getProperty(Configuration.ERROR_LOG, "false")) : false;
	
	public static void d(String tag, String msg) {
		if (!D) {
			return;
		}
		android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable tr) {
		if (!D) {
			return;
		}
		android.util.Log.d(tag, msg, tr);
	}

	public static void v(String tag, String msg) {
		if (!V) {
			return;
		}
		android.util.Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable tr) {
		if (!V) {
			return;
		}
		android.util.Log.v(tag, msg, tr);
	}

	public static void i(String tag, String msg) {
		if (!I) {
			return;
		}
		android.util.Log.i(tag, msg);
	}

	
	public static void i(String msg) {
		if (!I) {
			return;
		}
		android.util.Log.i(getStackTraceName(), msg);
	}
	
	public static void i(String tag, String msg, Throwable tr) {
		if (!I) {
			return;
		}
		android.util.Log.i(tag, msg, tr);
	}

	public static void w(String tag, String msg) {
		if (!W) {
			return;
		}
		android.util.Log.w(tag, msg);
	}

	public static void w(String tag, Throwable tr) {
		if (!W) {
			return;
		}
		android.util.Log.w(tag, tr);
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (!W) {
			return;
		}
		android.util.Log.w(tag, msg, tr);
	}

	public static void e(String tag, String msg) {
		if (!E) {
			return;
		}
		android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (!E) {
			return;
		}
		android.util.Log.e(tag, msg, tr);
	}
	
	public static String getStackTraceName(){
		if(!printClassLog){
			return "";
		}
		 StackTraceElement[]  stackTrace = Thread.currentThread().getStackTrace();	
		 String  stackTraceName = null;
		if(stackTrace.length>4){
			stackTraceName = stackTrace[4].getClassName()+ stackTrace[4].getMethodName();
		  }
		return stackTraceName;
	}

}
