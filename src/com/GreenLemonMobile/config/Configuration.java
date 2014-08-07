package com.GreenLemonMobile.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	
	private static Properties properties;
	private static Map<String, String> localProperties;
	
	// 日志开关
	public static final boolean sPrintLog = false;
	public static final boolean sPrintClassLog = false;
	
	public static final String HOST = "host";// 服务端
	
	public static final String PRINT_LOG = "printLog";
	public static final String DEBUG_LOG = "debugLog";
	public static final String VIEW_LOG = "viewLog";
	public static final String ERROR_LOG = "errorLog";
	public static final String INFO_LOG = "infoLog";
	public static final String WARN_LOG = "warnLog";
	
	public static final String CONNECT_TIMEOUT = "connectTimeout";// 连接超时
	public static final String READ_TIMEOUT = "readTimeout";// 读取超时
	public static final String ATTEMPTS = "attempts";// 尝试次数
	public static final String ATTEMPTS_TIME = "attemptsTime";// 尝试的间隔时间
	public static final String REQUEST_METHOD = "requestMethod";// 网络请求方式
	
	public static final String ROUTINE_CHECK_DELAY_TIME = "routineCheckDelayTime";// 例行检查延迟时间

	public static final String LEAVE_TIME_GAP = "leaveTimeGap";// 例行检查延迟时间	

	public static final String LOCAL_MEMORY_CACHE = "localMemoryCache";// 本地文件缓存
	public static final String LOCAL_FILE_CACHE = "localFileCache";// 本地内存缓存

	public static final String INIT_POOL_SIZE = "initPoolSize";// 最少网络线程池
	public static final String MAX_POOL_SIZE = "maxPoolSize";// 最大网络线程池
	
	public static final String DISCUSSUPLOADIMAGE_WIDTH = "discussUploadImageWidth";// 晒单上传图片宽度上限
	public static final String DISCUSSUPLOADIMAGE_HEIGHT = "discussUploadImageHeight";// 晒单上传图片高度上限
	public static final String DISCUSSUPLOADIMAGE_QUALITY = "discussUploadImageQuality";// 晒单上传图片质量
	
	public static final String APPLICATION_UPGRADE = "applicationUpgrade";// 升级检查开关

	public static final String APPLICATION_SHORTCUT = "applicationShortcut";// 快捷方式开关	
	
	static {
		localProperties = new HashMap<String, String>();

		// 内置配置
		localProperties.put(HOST, "gw.ebook.360buy.com");// 服务端
		localProperties.put(CONNECT_TIMEOUT, "" + 20 * 1000);// 连接超时
		localProperties.put(READ_TIMEOUT, "" + 20 * 1000);// 读取超时
		localProperties.put(ATTEMPTS, "" + 2);// 尝试次数
		localProperties.put(ATTEMPTS_TIME, "" + 0);// 尝试的间隔时间
		localProperties.put(REQUEST_METHOD, "get");// 网络请求方式

		localProperties.put(LOCAL_MEMORY_CACHE, "false");// 本地文件缓存
		localProperties.put(LOCAL_FILE_CACHE, "false");// 本地内存缓存

		localProperties.put(INIT_POOL_SIZE, "" + 5);// 最少网络线程池
		localProperties.put(MAX_POOL_SIZE, "" + 5);// 最大网络线程池

		localProperties.put(DISCUSSUPLOADIMAGE_WIDTH, "" + 500);// 晒单上传图片宽度上限
		localProperties.put(DISCUSSUPLOADIMAGE_HEIGHT, "" + 500);// 晒单上传图片高度上限
		localProperties.put(DISCUSSUPLOADIMAGE_QUALITY, "" + 80);// 晒单上传图片质量

		localProperties.put(ROUTINE_CHECK_DELAY_TIME, "" + (1000 * 2));// 例行检查延迟时间

		localProperties.put(LEAVE_TIME_GAP, "" + (1000 * 60 * 60));// 用户离开时间阀值

		localProperties.put(PRINT_LOG, "false");// 日志开关
		localProperties.put(DEBUG_LOG, "true");// 日志开关
		localProperties.put(VIEW_LOG, "true");// 日志开关
		localProperties.put(ERROR_LOG, "true");// 日志开关
		localProperties.put(INFO_LOG, "true");// 日志开关
		localProperties.put(WARN_LOG, "true");// 日志开关

		localProperties.put(APPLICATION_UPGRADE, "true");// 升级检查开关
		localProperties.put(APPLICATION_SHORTCUT, "false");// 快捷方式开关
		try {
			InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("config.properties");
			if (null != inputStream) {
				properties = new Properties();
				properties.load(inputStream);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static String getProperty(String key) {
		return getProperty(key, null);
	}

	public static String getProperty(String key, String defaultValue) {
		String result = null;
		if (null != properties) {
			result = properties.getProperty(key);
		}
		if (null == result) {
			result = localProperties.get(key);
		}
		if (null == result) {
			result = defaultValue;
		}
		return result;
	}

	public static Integer getIntegerProperty(String key) {
		return getIntegerProperty(key, null);
	}

	public static Integer getIntegerProperty(String key, Integer defaultValue) {
		String value = getProperty(key);
		if (null == value) {
			return defaultValue;
		}
		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return defaultValue;
		}

	}

	public static Boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, null);
	}

	public static Boolean getBooleanProperty(String key, Boolean defaultValue) {
		String value = getProperty(key);
		if (null == value) {
			return defaultValue;
		}
		try {
			return Boolean.valueOf(value);
		} catch (Exception e) {
			return defaultValue;
		}

	}	
}
