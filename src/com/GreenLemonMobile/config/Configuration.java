package com.GreenLemonMobile.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	
	private static Properties properties;
	private static Map<String, String> localProperties;
	
	// ��־����
	public static final boolean sPrintLog = false;
	public static final boolean sPrintClassLog = false;
	
	public static final String HOST = "host";// �����
	
	public static final String PRINT_LOG = "printLog";
	public static final String DEBUG_LOG = "debugLog";
	public static final String VIEW_LOG = "viewLog";
	public static final String ERROR_LOG = "errorLog";
	public static final String INFO_LOG = "infoLog";
	public static final String WARN_LOG = "warnLog";
	
	public static final String CONNECT_TIMEOUT = "connectTimeout";// ���ӳ�ʱ
	public static final String READ_TIMEOUT = "readTimeout";// ��ȡ��ʱ
	public static final String ATTEMPTS = "attempts";// ���Դ���
	public static final String ATTEMPTS_TIME = "attemptsTime";// ���Եļ��ʱ��
	public static final String REQUEST_METHOD = "requestMethod";// ��������ʽ
	
	public static final String ROUTINE_CHECK_DELAY_TIME = "routineCheckDelayTime";// ���м���ӳ�ʱ��

	public static final String LEAVE_TIME_GAP = "leaveTimeGap";// ���м���ӳ�ʱ��	

	public static final String LOCAL_MEMORY_CACHE = "localMemoryCache";// �����ļ�����
	public static final String LOCAL_FILE_CACHE = "localFileCache";// �����ڴ滺��

	public static final String INIT_POOL_SIZE = "initPoolSize";// ���������̳߳�
	public static final String MAX_POOL_SIZE = "maxPoolSize";// ��������̳߳�
	
	public static final String DISCUSSUPLOADIMAGE_WIDTH = "discussUploadImageWidth";// ɹ���ϴ�ͼƬ�������
	public static final String DISCUSSUPLOADIMAGE_HEIGHT = "discussUploadImageHeight";// ɹ���ϴ�ͼƬ�߶�����
	public static final String DISCUSSUPLOADIMAGE_QUALITY = "discussUploadImageQuality";// ɹ���ϴ�ͼƬ����
	
	public static final String APPLICATION_UPGRADE = "applicationUpgrade";// ������鿪��

	public static final String APPLICATION_SHORTCUT = "applicationShortcut";// ��ݷ�ʽ����	
	
	static {
		localProperties = new HashMap<String, String>();

		// ��������
		localProperties.put(HOST, "gw.ebook.360buy.com");// �����
		localProperties.put(CONNECT_TIMEOUT, "" + 20 * 1000);// ���ӳ�ʱ
		localProperties.put(READ_TIMEOUT, "" + 20 * 1000);// ��ȡ��ʱ
		localProperties.put(ATTEMPTS, "" + 2);// ���Դ���
		localProperties.put(ATTEMPTS_TIME, "" + 0);// ���Եļ��ʱ��
		localProperties.put(REQUEST_METHOD, "get");// ��������ʽ

		localProperties.put(LOCAL_MEMORY_CACHE, "false");// �����ļ�����
		localProperties.put(LOCAL_FILE_CACHE, "false");// �����ڴ滺��

		localProperties.put(INIT_POOL_SIZE, "" + 5);// ���������̳߳�
		localProperties.put(MAX_POOL_SIZE, "" + 5);// ��������̳߳�

		localProperties.put(DISCUSSUPLOADIMAGE_WIDTH, "" + 500);// ɹ���ϴ�ͼƬ�������
		localProperties.put(DISCUSSUPLOADIMAGE_HEIGHT, "" + 500);// ɹ���ϴ�ͼƬ�߶�����
		localProperties.put(DISCUSSUPLOADIMAGE_QUALITY, "" + 80);// ɹ���ϴ�ͼƬ����

		localProperties.put(ROUTINE_CHECK_DELAY_TIME, "" + (1000 * 2));// ���м���ӳ�ʱ��

		localProperties.put(LEAVE_TIME_GAP, "" + (1000 * 60 * 60));// �û��뿪ʱ�䷧ֵ

		localProperties.put(PRINT_LOG, "false");// ��־����
		localProperties.put(DEBUG_LOG, "true");// ��־����
		localProperties.put(VIEW_LOG, "true");// ��־����
		localProperties.put(ERROR_LOG, "true");// ��־����
		localProperties.put(INFO_LOG, "true");// ��־����
		localProperties.put(WARN_LOG, "true");// ��־����

		localProperties.put(APPLICATION_UPGRADE, "true");// ������鿪��
		localProperties.put(APPLICATION_SHORTCUT, "false");// ��ݷ�ʽ����
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
