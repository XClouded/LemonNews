package com.GreenLemonMobile.network;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.GreenLemonMobile.cipher.Base64;
import com.GreenLemonMobile.cipher.Md5Encrypt;
import com.GreenLemonMobile.cipher.RsaEncoder;
import com.GreenLemonMobile.config.CacheTimeConfig;
import com.GreenLemonMobile.config.Configuration;
import com.GreenLemonMobile.constant.Constant;
import com.GreenLemonMobile.database.CacheFileTable;
import com.GreenLemonMobile.entity.BodyEncodeEntity;
import com.GreenLemonMobile.entity.CacheFile;
import com.GreenLemonMobile.ui.DialogController;
import com.GreenLemonMobile.util.CommonUtil;
import com.GreenLemonMobile.util.DefaultEffectHttpListener;
import com.GreenLemonMobile.util.FileGuider;
import com.GreenLemonMobile.util.FileService;
import com.GreenLemonMobile.util.FileService.Directory;
import com.GreenLemonMobile.util.IOUtil;
import com.GreenLemonMobile.util.ImageUtil;
import com.GreenLemonMobile.util.JSONObjectProxy;
import com.GreenLemonMobile.util.Log;
import com.GreenLemonMobile.util.MyActivity;
import com.GreenLemonMobile.util.MyActivity.DestroyListener;
import com.GreenLemonMobile.util.MyApplication;
import com.GreenLemonMobile.util.NetUtils;
import com.GreenLemonMobile.util.StatisticsReportUtil;
import com.GreenLemonMobile.util.StreamToolBox;


public abstract class HttpGroup implements DestroyListener {
	
	interface GlobalInitializationInterface {

		void exit();

		void registerDevice(boolean isFirst);

	}
	
	public interface StopController {
		void stop();

		boolean isStop();
	}
	
	interface Handler {
		void run();
	}
	
	public interface CompleteListener {
		void onComplete(Bundle bundle);
	}	

	/* ������� - ���� */
	public interface OnGroupStartListener {
		void onStart();
	}

	public interface OnGroupEndListener {
		void onEnd();
	}

	public interface OnGroupErrorListener {
		void onError();
	}

	public interface OnGroupProgressListener {
		void onProgress(long max, long progress);
	}

	public interface OnGroupStepListener {
		void onStep(int max, int step);
	}

	/* HttpTask������ - ���� */
	public interface HttpTaskListener {

	}

	public interface OnStartListener extends HttpTaskListener {

		void onStart();

	}

	public interface OnEndListener extends HttpTaskListener {

		void onEnd(HttpResponse httpResponse);

	}

	public interface OnErrorListener extends HttpTaskListener {

		void onError(HttpError error);

	}

	public interface OnReadyListener extends HttpTaskListener {

		void onReady(HttpSettingParams httpSettingParams);

	}

	public interface OnProgressListener extends HttpTaskListener {

		void onProgress(long max, long progress);

	}

	public interface OnCommonListener extends OnEndListener, OnErrorListener, OnReadyListener {

	}

	public interface OnAllListener extends OnStartListener, OnEndListener, OnErrorListener, OnProgressListener {

	}

	public interface CustomOnAllListener extends OnAllListener {
		void onStart();
		void onEnd(HttpResponse httpResponse);
		void onError(HttpError error);
	}	

	private static int httpIdCounter = 0;
	private static String cookies;
	private static String mMd5Key;// ��Կ
	private static JSONObjectProxy mModules;// ģ��	
	private static final int connectTimeout = Integer.parseInt(Configuration.getProperty(Configuration.CONNECT_TIMEOUT));// ���ӳ�ʱ
	private static final int readTimeout = Integer.parseInt(Configuration.getProperty(Configuration.READ_TIMEOUT));// ��ȡ��ʱ
	private final static String sCharset = Constant.sCharset;// ����

	private static final int attempts = Integer.parseInt(Configuration.getProperty(Configuration.ATTEMPTS));// ���Դ���
	private static final int attemptsTime = Integer.parseInt(Configuration.getProperty(Configuration.ATTEMPTS_TIME));// ���Եļ��ʱ��

	private static final String host = Configuration.getProperty(Configuration.HOST);	

	protected ArrayList<HttpRequest> httpList = new ArrayList<HttpRequest>();// �����������
	private boolean useCaches = false;// ����
	protected HttpGroupSetting httpGroupSetting;
	protected int priority;
	protected int type;
	// �����Ƿ����client�ķ�����Ϣ
	private boolean reportUserInfoFlag = true;

	private final HashMap<MyActivity, ArrayList<HttpRequest>> alertDialogStateMap = new HashMap<MyActivity, ArrayList<HttpRequest>>();
	
	
	public static void setMd5Key(String md5Key) {
		mMd5Key = md5Key;
	}

	public static void setModules(JSONObjectProxy jsonObject) {
		mModules = jsonObject;
	}

	/**
	 * ��ȡ��Կ
	 */
	public static void queryMd5Key(CompleteListener listener) {
		HttpGroupSetting setting = new HttpGroupSetting();
		setting.setPriority(HttpGroupSetting.PRIORITY_JSON);
		setting.setType(HttpGroupSetting.TYPE_JSON);
		HttpGroup httpGroup = new HttpGroup.HttpGroupaAsynPool(setting);
		queryMd5Key(httpGroup, listener);
	}

	/**
	 * ��ȡ��Կ
	 */
	public static void queryMd5Key(HttpGroup httpGroup, final CompleteListener listener) {
		OnAllListener onAllListener = new HttpGroup.OnAllListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onEnd(HttpResponse httpResponse) {
				try {
					String md5KeyCode = httpResponse.getJSONObject().getStringOrNull("key");
					if (null == md5KeyCode) {
						return;
					}
					byte[] md5KeyBytes = Base64.decode(md5KeyCode);

					for (int i = 0, byteLength = md5KeyBytes.length; i < byteLength; i++) {
						md5KeyBytes[i] = (byte) ~md5KeyBytes[i];
					}
					String md5Key = new String(md5KeyBytes);
					if (Log.D) {
						Log.d("HttpGroup", "md5Key -->> " + md5Key);
					}
					HttpGroup.setMd5Key(md5Key);
					// ֪ͨ
					if (null != listener) {
						listener.onComplete(null);
					}
				} catch (Exception e) {
					// ֪ͨ
					if (null != listener) {
						listener.onComplete(null);
					}
				}
			}

			@Override
			public void onError(HttpError error) {
				// ֪ͨ
				if (null != listener) {
					listener.onComplete(null);
				}
			}

			@Override
			public void onProgress(long max, long progress) {
			}
		};
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("key");
		httpSetting.setJsonParams(new JSONObject());
		httpSetting.setListener(onAllListener);
		httpSetting.setPost(true);
		httpGroup.add(httpSetting);
	}
	
	public HttpGroup(HttpGroupSetting setting) {
		this.httpGroupSetting = setting;
		this.priority = setting.getPriority();
		this.type = setting.getType();
	}


	public static String getCookies() {
		return cookies;
	}


	abstract protected void execute(HttpRequest httpRequest);

	public HttpRequest add(String functionId, JSONObject params, OnAllListener listener) {// JSON ��ʽ
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId(functionId);
		httpSetting.setJsonParams(params);
		httpSetting.setListener(listener);
		return add(httpSetting);
	}

	public HttpRequest addWithUrl(String url, JSONObject params, OnAllListener listener) {// JSON ��ʽ
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(params);
		httpSetting.setListener(listener);
		return add(httpSetting);
	}

	public HttpRequest add(String url, Map<String, String> paramMap, OnAllListener listener) {// param ��ʽ

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		// TODO
		// �˴����ܻ����û�о���URL����Ĳ��������Ҫ�����ڴ˴����httpSetting.putMapParams()��������һ�ν��д��?
		httpSetting.setMapParams(paramMap);
		httpSetting.setListener(listener);
		return add(httpSetting);
	}
	
	public HttpRequest constructHttpRequest(final HttpSetting httpSetting) {
		// ��ÿ�����������ɷ�һ������ʱ��ʶ��
		httpIdCounter = httpIdCounter + 1;
		httpSetting.setId(httpIdCounter);

		tryEffect(httpSetting);

		if (Log.I) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- onStart -->> ");
		}
		httpSetting.onStart();// ֪ͨ��ʼ������Ҫ�ڣ�

		final HttpRequest httpRequest = new HttpRequest(httpSetting);
		
		if (Log.I && null != httpSetting.getFunctionId()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- functionId -->> " + httpSetting.getFunctionId());
		}

		if (Log.I && null != httpSetting.getUrl()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- url -->> " + httpSetting.getUrl());
		}

		// host����Ϊ��һ��handler�Ѿ�Ҫʹ��host�ˣ������ڴ����ã�
		if(null == httpSetting.getHost()){
			httpSetting.setHost(host);
		}

		// ������ͣ���Ϊ���ȼ���ǰ������������ҲҪ��ǰ��
		if (httpSetting.getType() == 0) {
			httpSetting.setType(type);
		}

		// ���ȼ���һ��Ҫ��ǰ�����ﴦ�?��ΪҪ���ڼ����̳߳�֮ǰ��
		if (httpSetting.getPriority() == 0) {
			httpSetting.setPriority(priority);
		}

		// Ĭ�����ȼ�
		if (httpSetting.getPriority() == 0) {// �ɼ̳�
			switch (httpSetting.getType()) {
			case HttpGroupSetting.TYPE_JSON:// ����� JSON
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_JSON);
				break;
			case HttpGroupSetting.TYPE_IMAGE:// �����ͼƬ
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_IMAGE);
				break;
			case HttpGroupSetting.TYPE_FILE:// ������ļ�
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_FILE);
				break;
			}
		}		

		httpSetting.setHttpRequest(httpRequest);
		
		return httpRequest;
	}

	/**
	 * ÿ���ṩ��httpSetting��Ӧ�����µģ���Ҫͬһ��httpSetting����ṩ�������
	 */
	public HttpRequest add(final HttpSetting httpSetting) {

		// ��ÿ�����������ɷ�һ������ʱ��ʶ��
		httpIdCounter = httpIdCounter + 1;
		httpSetting.setId(httpIdCounter);

		tryEffect(httpSetting);

		if (Log.I) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- onStart -->> ");
		}
		httpSetting.onStart();// ֪ͨ��ʼ������Ҫ�ڣ�

		final HttpRequest httpRequest = new HttpRequest(httpSetting);

		final OnReadyListener onReadyListener = httpSetting.getOnReadyListener();
		if (null != onReadyListener) {
			new Thread() {
				@Override
				public void run() {
					onReadyListener.onReady(httpSetting);
					add2(httpRequest);// ׼���ò���ż���
				}
			}.start();
		} else {
			add2(httpRequest);// ֱ�Ӽ���
		}
		httpSetting.setHttpRequest(httpRequest);
		return httpRequest;

	}

	public void add2(HttpRequest httpRequest) {
		HttpSetting httpSetting = httpRequest.getHttpSetting();

		if (Log.I && null != httpSetting.getFunctionId()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- functionId -->> " + httpSetting.getFunctionId());
		}

		if (Log.I && null != httpSetting.getUrl()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- url -->> " + httpSetting.getUrl());
		}

		// host����Ϊ��һ��handler�Ѿ�Ҫʹ��host�ˣ������ڴ����ã�
		if(null == httpSetting.getHost()){
			httpSetting.setHost(host);
		}

		// ������ͣ���Ϊ���ȼ���ǰ������������ҲҪ��ǰ��
		if (httpSetting.getType() == 0) {
			httpSetting.setType(type);
		}

		// ���ȼ���һ��Ҫ��ǰ�����ﴦ�?��ΪҪ���ڼ����̳߳�֮ǰ��
		if (httpSetting.getPriority() == 0) {
			httpSetting.setPriority(priority);
		}

		// Ĭ�����ȼ�
		if (httpSetting.getPriority() == 0) {// �ɼ̳�
			switch (httpSetting.getType()) {
			case HttpGroupSetting.TYPE_JSON:// ����� JSON
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_JSON);
				break;
			case HttpGroupSetting.TYPE_IMAGE:// �����ͼƬ
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_IMAGE);
				break;
			case HttpGroupSetting.TYPE_FILE:// ������ļ�
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_FILE);
				break;
			}
		}
		execute(httpRequest);// ���Ͻ����̴߳��?�ڴ�֮ǰ����UI�̡߳�
	}

	/**
	 * ���Ҫ��Ĭ��Ч�����Ч��״̬Ϊδ���?��������� activity �ǿգ��ż���Ч��
	 */
	private void tryEffect(HttpSetting httpSetting) {
		MyActivity myActivity = httpGroupSetting.getMyActivity();
		if (HttpSetting.EFFECT_DEFAULT == httpSetting.getEffect() && // ��ҪĬ��Ч��
				HttpSetting.EFFECT_STATE_NO == httpSetting.getEffectState() && // ����Ч��״̬Ϊδ����
				null != myActivity) {// ��������� activity �ǿ�
			DefaultEffectHttpListener effectListener = new DefaultEffectHttpListener(httpSetting, myActivity);
			httpSetting.setListener(effectListener);
		}
	}

	@Override
	public void onDestroy() {
		onGroupStartListener = null;
		onGroupEndListener = null;
		onGroupErrorListener = null;
		onGroupProgressListener = null;
		onGroupStepListener = null;

		if (httpList != null && httpList.size() > 0) {
			for (int index = 0; index < httpList.size(); ++index)
				if (httpList.get(index).getHttpSetting() != null
						&& httpList.get(index).getHttpSetting().getHttpRequest() != null) {
					HttpRequest request = httpList.get(index).getHttpSetting().getHttpRequest();
					request.stop();
					ThreadPool.getThreadPool().removeTask(request);
				}
			httpList.clear();
		}
	}

	/**
	 * @author lijingzuo ͬ����
	 */
	public static class HttpGroupSync extends HttpGroup {

		public HttpGroupSync(HttpGroupSetting setting) {
			super(setting);
		}

		@Override
		public void execute(final HttpRequest httpRequest) {

		}

	}

	/**
	 * @author lijingzuo �첽����
	 */
	public static class HttpGroupaAsynPool extends HttpGroup {

		public HttpGroupaAsynPool(HttpGroupSetting setting) {
			super(setting);
		}

		@Override
		public void execute(final HttpRequest httpRequest) {

			ThreadPool.getThreadPool().offerTask(
					new HttpRequestRunnable(httpRequest),
					httpRequest.getHttpSetting().getPriority());
		}

		public class HttpRequestRunnable implements Runnable {
			private HttpRequest httpRequest;

			public HttpRequestRunnable(HttpRequest httpRequest) {
				this.httpRequest = httpRequest;
			}

			public HttpRequest getHttpRequest() {
				return httpRequest;
			}

			public void setHttpRequest(HttpRequest httpRequest) {
				this.httpRequest = httpRequest;
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (httpList.size() < 1) {// ֪ͨ�鿪ʼ
					HttpGroupaAsynPool.this.onStart();
				}
				httpList.add(httpRequest);
				httpRequest.nextHandler();
			}

		}

	}

	/**
	 * ����
	 */
	public class HttpRequest implements StopController {

		// ֹͣ������
		private boolean stopFlag;
		
		private boolean blocked = false;

		public boolean isStop() {
			return stopFlag;
		}
		
		public void setBlocked(boolean blocked) {
		    this.blocked = blocked;
		}

		public void stop() {
			stopFlag = true;
//			try{
			try {
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
				httpSetting.setListener(null);

				Log.i("zhoubo", "add  33333333333333333");
				if(inputStream!=null){
					inputStream.close();
				}
				if (httpResponse != null) {
					InputStream inputStream0 = httpResponse.getInputStream();
					if (inputStream0 != null) {
						inputStream0.close();
					}
				}
				//httpResponse = null;
				//httpSetting = null;
				if (blocked)
				    this.notifyAll();
//				httpResponse.get
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			}catch(Exception e){
//
//			}
			Log.i("zhoubo", "stopLoadBook()....stopFlag==" +stopFlag);
		}

		// ֹͣ������

		protected HttpSetting httpSetting;
        

		protected HttpURLConnection conn;
		protected InputStream inputStream;

		protected HttpResponse httpResponse;

		protected ArrayList<HttpError> errorList;

		protected boolean manualRetry;

		/**
		 * ����ű���������ʧ�ܵģ������á�
		 */
		protected boolean connectionRetry;

		private int currentHandlerIndex = 0;

		private String thirdHost;

		private ArrayList<HttpError> getErrorList() {
			if (null == errorList) {
				errorList = new ArrayList<HttpError>();
			}
			return errorList;
		}

		private HttpError getLastError() {
			ArrayList<HttpError> errorList = getErrorList();
			int size = errorList.size();
			if (size > 0) {
				return errorList.get(size - 1);
			}
			return null;
		}


		public void setHttpSetting(HttpSetting httpSetting) {
			this.httpSetting = httpSetting;
		}

		private void clearErrorList() {
			getErrorList().clear();
		}

		public boolean isLastError() {// �ж��Ƿ��γ���ʧ��
			boolean result = null != errorList && !(errorList.size() < attempts);
			if (!result) {
				HttpError lastError = getLastError();
				if (null != lastError && lastError.isNoRetry()) {
					result = true;
				}
			}
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- isLastError() -->> " + result);
			}
			return result;
		}

		public void throwError(HttpError error) {
			if (isStop())
				return;
			ArrayList<HttpError> errorList = getErrorList();
			errorList.add(error);
			error.setTimes(errorList.size());
			if (Log.I) {
				Log.i("HttpGroup", "id:" + httpSetting.getId() + "- HttpError -->> " + error);
			}
			// ����û�����
			checkErrorInteraction();
		}

		/**
		 * ����û�����
		 */
		public void checkErrorInteraction() {
			/*
			 * ����2����Ҫ���û��������쳣
			 */
			HttpError lastError = getLastError();
			if (null != lastError && // ��֤WIFI
					HttpError.EXCEPTION == lastError.getErrorCode() && //
					HttpError.EXCEPTION_MESSAGE_ATTESTATION_WIFI.equals(lastError.getException().getMessage())) {
				alertAttestationWIFIDialog();
			} else if (isLastError()) {// ����Ѿ��ﵽ�Զ����Դ���͵���֪ͨ����
				alertErrorDialog();
			}
		}

		/**
		 * ���̰߳�ȫ
		 */
		class HttpDialogController extends DialogController {

			protected ArrayList<HttpRequest> httpRequestList;
			protected MyActivity myActivity;
			protected HttpSetting httpSetting;

			/**
			 * ��ʼ��
			 */
			public void init(ArrayList<HttpRequest> httpRequestList, MyActivity myActivity, HttpSetting httpSetting) {
				this.myActivity = myActivity;
				this.httpRequestList = httpRequestList;
				this.httpSetting = httpSetting;
				init(myActivity);
			}

			/**
			 * ����
			 */
			protected void actionRetry() {
				actionCommon(true);
			}

			/**
			 * ȡ��
			 */
			protected void actionCancel() {
				actionCommon(false);
			}

			protected void actionCommon(boolean isRetry) {
				alertDialog.dismiss();
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- notifyUser() retry -->> httpRequestList.size() = " + httpRequestList.size());
				}
				synchronized (alertDialogStateMap) {
					for (int i = 0; i < httpRequestList.size(); i++) {
						HttpRequest httpRequest = httpRequestList.get(i);
						if (isRetry) {
							httpRequest.manualRetry = true;
						} else {
							if (httpSetting.failedRunnable != null) {
								httpSetting.failedRunnable.run();
							} else {
								myActivity.post(new Runnable() {
		
									@Override
									public void run() {
										// TODO Auto-generated method stub
										//myActivity.finish();
									}
		
								});
							}
						}
						synchronized (httpRequest) {
							httpRequest.notify();
						}
					}
					alertDialogStateMap.remove(myActivity);
				}
			}

		}

		/**
		 * �����Ի���
		 */
		private void notifyUser(final HttpDialogController httpDialogController) {

			final MyActivity myActivity = httpGroupSetting.getMyActivity();
			if (null == myActivity) {// ������޹ص����Ӳ�����
				return;
			}

			boolean result = false;// ���ڿ��Ʋ�Ҫͬһ��������
			ArrayList<HttpRequest> httpRequestList = null;
			synchronized (alertDialogStateMap) {
				httpRequestList = alertDialogStateMap.get(myActivity);// ��ҳ����������赯�������쳣֪ͨ
				if (null == httpRequestList) {// ���û���κ��赯�������쳣֪ͨ
					httpRequestList = new ArrayList<HttpRequest>();
					alertDialogStateMap.put(myActivity, httpRequestList);
					result = true;
				}
				httpRequestList.add(this);
			}

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- notifyUser() -->> result = " + result);
			}

			if (result) {
				// �����Ի���
				// ��ʼ��
				httpDialogController.init(httpRequestList, myActivity, httpSetting);

				myActivity.post(new Runnable() {
					@Override
					public void run() {
						httpDialogController.show();
					}
				});

			}

			// ���̹߳�����ͣ���ȴ�UI�߳̽����û�ѡ��
			synchronized (HttpRequest.this) {
				try {
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- dialog wait start -->> ");
					}
					HttpRequest.this.setBlocked(true);
					HttpRequest.this.wait();
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- dialog wait end -->> ");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * һ���쳣�Ի���
		 */
		private void alertErrorDialog() {

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- alertErrorDialog() -->> ");
			}

			// �Ƿ��ֹ֪ͨ�û�����
			if (!httpSetting.isNotifyUser()) {
				return;
			}

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- alertErrorDialog() -->> true");
			}

			// ����֪ͨ�û�
			HttpDialogController httpDialogController = new HttpDialogController() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case AlertDialog.BUTTON_POSITIVE:// ����ߵİ�ť������
						actionRetry();
						break;
					case AlertDialog.BUTTON_NEGATIVE:// ���ұߵİ�ť��ȡ����˳�
						actionCancel();
						break;
					}
				}
			};
			HttpError lastError = getLastError();
			if (null != lastError && HttpError.JSON_CODE == lastError.getErrorCode()) {
//				httpDialogController.setTitle(MyApplication.getInstance().getText(R.string.alert_title_poor_network2));
//				httpDialogController.setMessage(MyApplication.getInstance().getText(R.string.alert_message_poor_network2));
			} else {
				httpDialogController.setTitle("���緱æ");
				httpDialogController.setMessage("���緱æ�������ԣ�");			
			}
			// ���԰�ť
			httpDialogController.setPositiveButton("����");
			// �˳���ȡ��ť
			httpDialogController.setNegativeButton(httpSetting.isNotifyUserWithExit() ? "�˳�" : "ȡ��");
			notifyUser(httpDialogController);

		}

		/**
		 * ��֤ WIFI �Ի���
		 */
		private void alertAttestationWIFIDialog() {
			// ��������
			HttpDialogController httpDialogController = new HttpDialogController() {

				private int state;

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case AlertDialog.BUTTON_POSITIVE:// ����ߵİ�ť��ȷ��
						switch (state) {
						case 0:// ��һ��
							if (Log.D) {
								Log.d("HttpGroup", "http dialog BUTTON_POSITIVE -->> " + 1);
							}
							// �ı����͹���
							state = 1;
							myActivity.post(new Runnable() {// �ô��ڹرպ�������ʾ
										public void run() {
											if (Log.D) {
												Log.d("HttpGroup", "http dialog change -->> ");
											}
											setMessage("�����Ƿ����ԣ�");
											setPositiveButton("����");
											if (!alertDialog.isShowing()) {
												alertDialog.show();
											}
											// �������������Ҫȷ��������UI����֮��ִ�У�
											Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://app.360buy.com/"));
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											MyApplication.getInstance().startActivity(intent);
										}
									});
							break;
						case 1:// �ڶ���
							if (Log.D) {
								Log.d("HttpGroup", "http dialog BUTTON_POSITIVE -->> " + 2);
							}
							actionRetry();
							break;
						}
						break;
					case AlertDialog.BUTTON_NEGATIVE:// ���ұߵİ�ť��ȡ��
						if (Log.D) {
							Log.d("HttpGroup", "http dialog BUTTON_NEGATIVE -->> " + 1);
						}
						actionCancel();
						break;
					}
				}
			};
			httpDialogController.setTitle("WIFI��֤");
			httpDialogController.setMessage("�������ӵ����������Ҫ��֤�����ڴ������������֤��");
			// ����ߵİ�ť������
			httpDialogController.setPositiveButton("ȷ��");
			// ���ұߵİ�ť��ȡ����˳�
			httpDialogController.setNegativeButton("ȡ��");
			notifyUser(httpDialogController);
		}

		private ArrayList<Handler> handlers = new ArrayList<Handler>();

		public HttpRequest(HttpSetting httpSetting) {
			this.httpSetting = httpSetting;

			handlers.add(proxyHandler);
			handlers.add(paramHandler);
			handlers.add(firstHandler);
			handlers.add(testHandler);
			handlers.add(cacheHandler);
			handlers.add(connectionHandler);
			handlers.add(contentHandler);
		}

		public HttpSetting getHttpSetting() {
			return httpSetting;
		}

		public void nextHandler() {
			if (isStop())
				return;
			int i = currentHandlerIndex;
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- nextHandler() i -->> " + currentHandlerIndex);
			}
			currentHandlerIndex++;
			if (i < handlers.size()) {
				handlers.get(i).run();
				currentHandlerIndex = i;// �ָ����ָ�뵽����
			}
			
			stopFlag = true;
		}

		private File findCachesFileByMd5() {
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- findCachesFileByMd5() -->> ");
			}

			Directory directory = null;

			// 1. ȷ������
			switch (httpSetting.getType()) {

			case HttpGroupSetting.TYPE_JSON: {// JSON
				directory = FileService.getDirectory(FileService.JSON_DIR);
				break;
			}

			case HttpGroupSetting.TYPE_IMAGE: {// IMAGE
				directory = FileService.getDirectory(FileService.IMAGE_DIR);
				break;
			}

			}

			// 2. �����ļ�
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- findCachesFileByMd5() directory -->> " + directory);
			}
			if (null == directory) {
				return null;
			}
			File dir = directory.getDir();
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- findCachesFileByMd5() dir.exists() -->> " + dir.exists());
			}
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- findCachesFileByMd5() dir.isDirectory() -->> " + dir.isDirectory());
			}
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- findCachesFileByMd5() dir -->> " + dir);
			}
			File[] fileList = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					String md5 = httpSetting.getMd5();
					if (null == md5) {
						return false;
					}
					return filename.startsWith(md5);
				}
			});
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- findCachesFileByMd5() fileList -->> " + fileList);
			}
			if (fileList != null) {
				if (fileList.length > 0) {
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- can find caches file by md5 -->> ");
					}
					return fileList[0];
				}
			}

			return null;

		}

		/**
		 * ����
		 */
		private Handler paramHandler = new Handler() {
			@Override
			public void run() {
				if (null != httpSetting.getFunctionId()&&httpSetting.isSuffix) {
					httpSetting.putMapParams("functionId", httpSetting.getFunctionId());
					}
				if(httpSetting.getJsonParams()!=null&&httpSetting.isSuffix){
					String body = httpSetting.getJsonParams().toString();
					if (Log.I) {
						Log.i("HttpGroup", "id:" + httpSetting.getId() + "- body -->> " + body);
					}
					if(httpSetting.getIsEncoder()){
						String bodyEncoder = RsaEncoder.stringBodyEncoder(body, httpSetting.encodeEntity);
						httpSetting.putMapParams("body", bodyEncoder);
					}else{
					    httpSetting.putMapParams("body", body);
					}
				}
				nextHandler();
			}
		};
		/**
		 * WAP
		 */
		private Handler proxyHandler = new Handler() {

			private String hostAndPort;

			public String getHostAndPortByUrl(String url) {

				if (null != hostAndPort) {
					return hostAndPort;
				}

				if (null != url) {
					int start = url.indexOf("://") + 3;
					int end = url.indexOf("/", start);
					if (start == -1) {
						return null;
					}
					if (end == -1) {
						return null;
					}
					hostAndPort = url.substring(start, end);
					return hostAndPort;
				}
				return null;

			}

			@Override
			public void run() {

				String proxyHost = NetUtils.getProxyHost();

				// �Ƿ��ߴ��� Start
				if (null != proxyHost) {
					String url = httpSetting.getUrl();
					if (null != url) {
						thirdHost = getHostAndPortByUrl(url);
						if (null != thirdHost) {
							httpSetting.setUrl(url.replace(thirdHost, proxyHost));
						}
					}
				}
				// �Ƿ��ߴ��� End

				if (null != httpSetting.getFunctionId()&&httpSetting.getUrl()==null) {

					// �Ƿ��ߴ��� Start
					if (null != proxyHost) {
						httpSetting.setUrl("http://" + proxyHost + "/client.action");
					} else {
						httpSetting.setUrl("http://" + httpSetting.getHost() + "/client.action");
					}
					// �Ƿ��ߴ��� End
				}
				nextHandler();
			}

		};

		/**
		 * ���þ���
		 */
		private Handler firstHandler = new Handler() {
			@Override
			public void run() {

				// �̳������ã�

				// ���ӵȴ�ʱ��
				if (httpSetting.getConnectTimeout() == 0) {
					httpSetting.setConnectTimeout(connectTimeout);
				}
				// ��ȡ�ȴ�ʱ��
				if (httpSetting.getReadTimeout() == 0) {
					httpSetting.setReadTimeout(readTimeout);
				}

				// ʹ�� GET �ķ�ʽ��������
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE// �����ͼƬ
						|| httpSetting.getType() == HttpGroupSetting.TYPE_FILE) {// ������ļ�
					httpSetting.setPost(false);
				}

				// Ĭ������ʱ��

				// Ĭ�϶�ȡʱ��
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {// ������ļ�
					httpSetting.setReadTimeout(1000 * 60 * 60);// ��ȡ��ʱ��1��Сʱ��
				}

				// Ĭ�ϻ���
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {// �����ͼƬ
					httpSetting.setLocalFileCache(true);
					httpSetting.setLocalFileCacheTime(CacheTimeConfig.IMAGE);// ͼƬĬ�ϻ���һ��
				}

				// ȫ�ֳ�ʼ��
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {// �����ͼƬ
					httpSetting.setNeedGlobalInitialization(false);
				}

				if (httpSetting.isNeedGlobalInitialization()) {
					GlobalInitialization.initNetwork(true);
					// ȫ�ֳ�ʼ�����豸ע�ᡢ�汾����ȵȣ�ʧ�ܺ�ȡ�����е�httprequest
					if (!GlobalInitialization.getInstance().isGlobalInitialized()) {
						HttpError error = new HttpError();
						error.setErrorCode(HttpError.RESPONSE_CODE);
						error.setResponseCode(200);// Ŀǰ�͵���404����
						for (int i = 0; i < attempts; ++i)
							throwError(error);
						httpSetting.onError(getLastError());// ֪ͨʧ��
						return;
					}
				}

				// ������������ۼ���
				addMaxStep(1);

				urlParam();

				if (checkModule(MODULE_STATE_DISABLE)) {// ���ӿ��Ƿ����
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- functionId close -->> ");
					}
					return;
				}

				if ((TextUtils.isEmpty(httpSetting.getUrl()) && TextUtils.isEmpty(httpSetting.getFunctionId())) || //
						httpSetting.getUrl().endsWith(".gif") || httpSetting.getUrl().endsWith(".bmp")) {
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.RESPONSE_CODE);
					error.setResponseCode(404);// Ŀǰ�͵���404����
					throwError(error);
					httpSetting.onError(getLastError());// ֪ͨʧ��
					return;// ��ûִ�к����ģ�
				} else {
					nextHandler();
					if (isLastError()) {
						if (Log.I) {
							Log.i("HttpGroup", "id:" + httpSetting.getId() + "- onError -->> ");
						}
						httpSetting.onError(getLastError());// ֪ͨʧ��
					} else {
						if (Log.I) {
							Log.i("HttpGroup", "id:" + httpSetting.getId() + "- onEnd -->> ");
						}
						addCompletesCount();
						addStep(1);
						httpSetting.onEnd(httpResponse);// ֪ͨ�ɹ�
						if (httpSetting.successRunnable != null)
							httpSetting.successRunnable.run();
					}

					return;
				}
			}
		};

		// interface HttpTestMappers {
		//
		// boolean
		//
		// }

		/**
		 * ����
		 */
		private Handler testHandler = new Handler() {
			@Override
			public void run() {
				nextHandler();
			}
		};

		/**
		 * ����
		 */
		private Handler cacheHandler = new Handler() {
			@Override
			public void run() {

				File cachesFile = null;
				// �ڴ滺��
				// JSONObjectProxy cachesJsonObject = null;
				// if (httpSetting.isLocalMemoryCache() && null !=
				// (cachesJsonObject = JsonCache.get(httpSetting.getMd5()))) {//
				// �ڴ滺��
				// httpResponse = new HttpResponse();
				// httpResponse.setJsonObject(cachesJsonObject);
				// } else
				if (httpSetting.getCacheMode() != HttpSetting.CACHE_MODE_ONLY_NET && httpSetting.isLocalFileCache() && null != (cachesFile = findCachesFileByMd5())) {// ����л����ļ����߻���

					long localFileCacheTime = httpSetting.getLocalFileCacheTime();

					if (localFileCacheTime != 0 && CacheFileTable.isExpired(cachesFile)) {// ������Ч��
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- local file cache time out -->> ");
						}
						doNetAndCache();
						return;
					}

					httpResponse = new HttpResponse();

					switch (httpSetting.getType()) {

					case HttpGroupSetting.TYPE_JSON: {// JSON

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							inputStream = new FileInputStream(cachesFile);
							httpResponse.setString(IOUtil.readAsString(inputStream, sCharset));
							httpResponse.setJsonObject(new JSONObjectProxy(new JSONObject(httpResponse.getString())));

						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						}finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}

					case HttpGroupSetting.TYPE_XML: {// XML

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							 inputStream = new FileInputStream(cachesFile);
							 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
							 DocumentBuilder builder = factory.newDocumentBuilder();
							 Document dom = builder.parse(inputStream);
						     Element root = dom.getDocumentElement();
							 httpResponse.setRoot(root);

//							httpResponse.setString(IOUtil.readAsString(inputStream, charset));
//							httpResponse.setJsonObject(new JSONObjectProxy(new JSONObject(httpResponse.getString())));

						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						}finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}

					case HttpGroupSetting.TYPE_STREAM: {// STREAM
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							 inputStream = new FileInputStream(cachesFile);
							httpResponse.setByteArrayInputStream(StreamToolBox.flushInputStream(inputStream));
//							httpResponse.setJsonObject(new JSONObjectProxy(new JSONObject(httpResponse.getString())));

						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						}finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}
					case HttpGroupSetting.TYPE_IMAGE: {// IMAGE

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- read image file -->> ");
						}
						try {
							httpResponse.setLength(cachesFile.length());
//							FileInputStream  fileInputStream=new FileInputStream(cachesFile);
//							httpResponse.setInputData(StreamToolBox.getByteArray(fileInputStream));
							//httpResponse.setInputStream(StreamToolBox.loadStreamFromFile(cachesFile));
							Bitmap bitmap = BitmapFactory.decodeFile(cachesFile.getAbsolutePath(), getBitmapOpt());
							bitmap = ImageUtil.CropForExtraWidth(bitmap);
							httpResponse.setBitmap(bitmap);
							httpResponse.setDrawable(new BitmapDrawable(bitmap));
						} catch (Throwable e) {
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						}

						break;
					}
					case HttpGroupSetting.TYPE_STRING: {// STRING

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							inputStream = new FileInputStream(cachesFile);
							httpResponse.setString(IOUtil.readAsString(inputStream, sCharset));
						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						}finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}					
					}

				} else {
					doNetAndCache();
				}
			}
		};

		/**
		 * �����������л���
		 */
		private void doNetAndCache() {

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- doNetAndCache() -->> ");
			}

			// ���Ҫ��ֻ�������棬��ôֱ����onError��
			if (HttpSetting.CACHE_MODE_ONLY_CACHE == httpSetting.getCacheMode()) {
				HttpError httpError = new HttpError(new Exception(HttpError.EXCEPTION_MESSAGE_NO_CACHE));
				httpError.setNoRetry(true);
				throwError(httpError);
				return;
			}

			nextHandler();

			if (isLastError()) {
				return;
			}

			save();
		}

		/**
		 * ����
		 */
		private void save() {
			// �洢
			if (httpSetting.isLocalFileCache()) {
				switch (httpSetting.getType()) {

				case HttpGroupSetting.TYPE_JSON: {// JSON

					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- save json file start -->> ");
					}
					Directory directory = FileService.getDirectory(FileService.JSON_DIR);
					if (null != directory) {
						String fileName = httpSetting.getMd5() + ".json";
						if (null == httpResponse) {
							return;
						}
						String fileContent = httpResponse.getString();
						boolean result = FileService.saveToSDCard(FileService.getDirectory(FileService.JSON_DIR), fileName, fileContent);
						if (result) {
							CacheFile cacheFile = new CacheFile(fileName, httpSetting.getLocalFileCacheTime());
							cacheFile.setDirectory(directory);
							CacheFileTable.insertOrUpdate(cacheFile);
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- save json file -->> " + result);
						}
					}
					break;
				}
				case HttpGroupSetting.TYPE_XML: {// JSON

					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- save json file start -->> ");
					}
					Directory directory = FileService.getDirectory(FileService.XML_DIR);
					if (null != directory) {
						String fileName = httpSetting.getMd5() + ".XML";
						if (null == httpResponse) {
							return;
						}
						String fileContent = httpResponse.getString();
						boolean result = FileService.saveToSDCard(FileService.getDirectory(FileService.XML_DIR), fileName, fileContent);
						if (result) {
							CacheFile cacheFile = new CacheFile(fileName, httpSetting.getLocalFileCacheTime());
							cacheFile.setDirectory(directory);
							CacheFileTable.insertOrUpdate(cacheFile);
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- save json file -->> " + result);
						}
					}
					break;
				}
				case HttpGroupSetting.TYPE_IMAGE: {// IMAGE

					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- save image file start -->> ");
					}
					Directory directory = FileService.getDirectory(FileService.IMAGE_DIR);
					if (null != directory) {
						String fileName = httpSetting.getMd5() + ".image";
						if (null == httpResponse) {
							return;
						}
						byte[] fileContent = httpResponse.getInputData();
						boolean result = FileService.saveToSDCard(directory, fileName, fileContent);
						if (result) {
							CacheFile cacheFile = new CacheFile(fileName, httpSetting.getLocalFileCacheTime());
							cacheFile.setDirectory(directory);
							CacheFileTable.insertOrUpdate(cacheFile);
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- save image file -->> " + result);
						}
					}
					break;
				}

				}
			}
		}

		/**
		 * ����
		 */
		private Handler connectionHandler = new Handler() {

			@Override
			public void run() {
				for (int i = 0; (i < attempts) && !isStop();) {// ����N��

					boolean retry = false;

					try {

						// ���ܹ���
						beforeConnection();
						
						// ����Url����ͨUrl�������ƴ�ӳ�finalUrl
						String urlStr = httpSetting.getFinalUrl();
						URL url = new URL(urlStr);

						Log.d("HttpGroup", "url.getDefaultPort():" + url.getDefaultPort() + "");
						// ��ʼ�������Ӷ���
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- url.openConnection() -->> ");
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- finalUrl -->> " + url);
						}
						conn = (HttpURLConnection) url.openConnection();
						// �Ƿ��ߴ��� Start
						String proxyHost = NetUtils.getProxyHost();
						if (null != proxyHost) {
							conn.setRequestProperty("X-Online-Host", thirdHost != null ? thirdHost : httpSetting.getHost());
						}
						// �Ƿ��ߴ��� End
						conn.setConnectTimeout(httpSetting.getConnectTimeout());
						conn.setReadTimeout(httpSetting.getReadTimeout());
						conn.setUseCaches(useCaches);
						conn.setRequestProperty("Charset", sCharset);
						conn.setRequestProperty("Connection", "Keep-Alive");// ���ֳ�����
						conn.setRequestProperty("Accept-Encoding", "gzip,deflate");// �ͻ���֧��gzip
						if (null != cookies) {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId() + "- cookies set -->> " + cookies);
							}
							conn.setRequestProperty("Cookie", cookies);// Cookie
							// ����
							SharedPreferences sharedPreferences = CommonUtil.getDefaultSharedPreferences();
							sharedPreferences.edit().putString("cookies", cookies).commit();// ˭��ӵ�cookies�־û����к��ã�
						}

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- handleGetOrPost() -->> ");
						}
						handleGetOrPost();
						if (connectionRetry) {// ���Ӳ��ɹ�����
							connectionRetry = false;
							retry = true;
						}

					} catch (Exception e) {
						HttpError httpError = new HttpError(e);
						throwError(httpError);
						retry = true;
					}

					if (retry) {
						if (i < attempts - 1) {
							try {// ��һ��ʱ���ٳ���
								if (Log.D) {
									Log.d("HttpGroup", "id:" + httpSetting.getId() + "- sleep -->> " + attemptsTime);
								}
								Thread.sleep(attemptsTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId() + "- onRetry -->> " + " manualRetry = " + manualRetry);
						}

						if (manualRetry) {
							// �ֶ�����
							manualRetry = false;
							clearErrorList();
							i = 0;
						} else {
							// �Զ����ԣ��������
							i++;
						}
					} else {
						break;
					}
				}
			}
		};

		private void urlParam() {

			if (httpSetting.isPost()) {
				// POST
				if (null != this.httpSetting.getMapParams()) {

					if (reportUserInfoFlag) {
						this.httpSetting.setSemiUrl(this.httpSetting.getUrl() + "?" + "functionId=" + this.httpSetting.getMapParams().get("functionId") + StatisticsReportUtil.getReportString(httpSetting.isNeedGlobalInitialization()));
					} else {
						this.httpSetting.setSemiUrl(this.httpSetting.getUrl() + "?" + "functionId=" + this.httpSetting.getMapParams().get("functionId"));
					}

				} else 
					this.httpSetting.setSemiUrl(this.httpSetting.getUrl());
			} else {
				// GET
				if (null != this.httpSetting.getMapParams()) {

					StringBuilder url = new StringBuilder(this.httpSetting.getUrl());
					url.append("?");

					Map<String, String> mapParams = this.httpSetting.getMapParams();
					Set<String> keySet = mapParams.keySet();
					for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
						String key = (String) iterator.next();
						String value = mapParams.get(key);
						url.append(key).append("=").append(value);
						if (iterator.hasNext()) {
							url.append("&");
						}
					}

					if (reportUserInfoFlag) {
						this.httpSetting.setSemiUrl(url.toString() + StatisticsReportUtil.getReportString(httpSetting.isNeedGlobalInitialization()));
					} else {
						this.httpSetting.setSemiUrl(url.toString());
					}

				} else 
					this.httpSetting.setSemiUrl(this.httpSetting.getUrl());
			}

		}

		private void beforeConnection() {
			// �ж��Ƿ���Ҫָ��
			if (checkModule(MODULE_STATE_ENCRYPT)) {
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- encrypt -->> ");
				}
				if (null == mMd5Key) {
					queryMd5Key(continueListener);

					// ���̹߳�����ͣ���ȴ������̻߳�ȡMd5Key��
					synchronized (HttpRequest.this) {
						try {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId() + "- encrypt wait start -->> ");
							}
							HttpRequest.this.setBlocked(true);
							HttpRequest.this.wait();
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId() + "- encrypt wait end -->> ");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				this.httpSetting.setFinalUrl(httpSetting.getSemiUrl() + "&hash=" + Md5Encrypt.md5(httpSetting.getJsonParams().toString() + mMd5Key));
			} else 
				this.httpSetting.setFinalUrl(httpSetting.getSemiUrl());
		}

		private void handleGetOrPost() throws Exception {
			if (httpSetting.isPost()) {
				post();
			} else {
				get();
			}
			connectionHandler2();
		}

		/**
		 * GET ����
		 */
		private void get() throws Exception {
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- get() -->> ");
			}
			httpResponse = new HttpResponse(conn);
			conn.setRequestMethod("GET");
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- get() -->> ok");
			}
		}

		/**
		 * POST ����
		 */
		private void post() throws Exception {
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- post() -->> ");
			}
			httpResponse = new HttpResponse(conn);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			byte[] data = null;
			if (this.httpSetting.getMapParams() == null) {
				data = ((String) "body=").getBytes();
			} else {
				StringBuilder sb = new StringBuilder();
				Map<String, String> mapParams = this.httpSetting.getMapParams();
				Set<String> keySet = mapParams.keySet();
				for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					if ("functionId".equals(key)) {
						continue;
					}
					String value = mapParams.get(key);
					if (Log.I) {
						Log.i("HttpGroup", "id:" + httpSetting.getId() + "- param key and value -->> " + key + "��" + value);
					}
					sb.append(key).append("=").append(value);
					if (iterator.hasNext()) {
						sb.append("&");
					}
				}

				data = sb.toString().getBytes();
				Log.i("HttpGroup", "finalUrl -->data==="+sb.toString());
			}
			conn.setRequestProperty("Content-Length", String.valueOf(data.length));
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- post() -->> 1");
			}
			DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- post() -->> 2");
			}
			outStream.write(data);
			/*
			 * conn.setRequestProperty("Content-Length", String.valueOf(data.length)); conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); DataOutputStream outStream = new
			 * DataOutputStream(conn.getOutputStream()); outStream.write(data); outStream.flush();
			 */
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- post() -->> ready");
			}
			outStream.flush();
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId() + "- post() -->> ok");
			}
		}

		/**
		 *
		 */
		protected void connectionHandler2() {
			try {
				if(httpSetting.start > 0){
				    Log.i("HttpGroup", "httpSetting.start======"+httpSetting.start);
				    Log.i("HttpGroup", "httpSetting.end======"+httpSetting.end);
					conn.setRequestProperty("Range", "bytes=" + httpSetting.start + "-" +httpSetting.end);
					}
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- connectionHandler2() -->> ");
				}
				conn.connect();
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- ResponseCode() -->> "+conn.getResponseCode());
				}
				// ����ͷ�ֶ�
				httpResponse.setHeaderFields(conn.getHeaderFields());
				// ��ӡ����ͷ�ֶ�
				if (Log.D) {
					Map<String, List<String>> headerFields = conn.getHeaderFields();
					Set<Entry<String, List<String>>> entrySet = headerFields.entrySet();
					JSONObject jsonObject = new JSONObject();
					for (Entry<String, List<String>> entry : entrySet) {
						String name = (null == entry.getKey() ? "<null>" : entry.getKey());
						String value = new JSONArray(entry.getValue()).toString();
						jsonObject.put(name, value);
					}
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- headerFields -->> " + jsonObject.toString());
				}
//				int code = conn.getResponseCode();

				httpResponse.setCode(conn.getResponseCode());

				// ����
				httpResponse.setLength(conn.getContentLength());
				HttpGroup.this.addMaxProgress(httpSetting.start+Long.valueOf(httpResponse.getLength()).longValue());// ������������
				// ����
				httpResponse.setType(conn.getContentType());

				// if (httpSetting.getType() == HttpGroupSetting.TYPE_JSON) {
				// // ͷ�ֶ���ʾ�����������ʱ�����´��?
				// if (null == httpResponse.getType() || !httpResponse.getType().contains("application/json")) {
				// // ��֤WIFI�ж�
				// String customHeaderField = httpResponse.getHeaderField("X_Power_By");
				// if (null == customHeaderField || !customHeaderField.equals("gw.360buy.com")) {
				// Exception e = new Exception(HttpError.EXCEPTION_MESSAGE_ATTESTATION_WIFI);
				// throwError(e);
				// connectionRetry = true;// ����
				// return;
				// }
				// }
				// }

				if (httpResponse.getCode() != HttpURLConnection.HTTP_OK&&httpResponse.getCode() != HttpURLConnection.HTTP_PARTIAL) {
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.RESPONSE_CODE);
					error.setResponseCode(httpResponse.getCode());
					throwError(error);
					connectionRetry = true;// ����
					return;
				}
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- ResponseCode() -->> ok");
				}
				// ��������Cookies
				if (httpSetting.isSaveCookie()) {
					String cookie = conn.getHeaderField("Set-Cookie");
					if (null != cookie) {
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- cookies get -->> " + cookie);
						}
						cookies = cookie;// cookie.substring(0,
											// cookie.indexOf(";"));
					}
				}
				// ������
				InputStream is = null;
				// ֧��gzip
				String encoding = conn.getHeaderField("Content-Encoding");
				if ("gzip".equals(encoding)) {
					is = new GZIPInputStream(conn.getInputStream());
				} else {
					is = conn.getInputStream();
				}
				httpResponse.setInputStream(is);
				// try Ϊ�˱�֤�ͷ� InputStream
				try {
					// ��һ��
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId() + "- ResponseCode() -->> ok nextHandler()");
					}
					nextHandler();
				} finally {
					try {
						if (null != httpResponse.getInputStream()) {
							httpResponse.getInputStream().close();
							httpResponse.setInputStream(null);// ȥ�����Ψһ��
							// InputStream
							// ����
						}
						if (null != conn) {
							conn.disconnect();
							conn = null;
							// HttpResponse ��� conn ��ʱ���ţ����ڷ����º��Ŵ���ѯ�����趨������ TODO
						}
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				if (e instanceof SocketTimeoutException) {// ���ӳ�ʱ
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.TIME_OUT);
					throwError(error);
				} else {// ����
					HttpError httpError = new HttpError(e);
					throwError(httpError);
				}
				connectionRetry = true;// ����
				return;
			}
		}

		/**
		 * �ɷ����ݴ���
		 */
		private Handler contentHandler = new Handler() {
			@Override
			public void run() {
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- contentHandler -->>");
				}
				try {
					if (httpSetting.getType() == HttpGroupSetting.TYPE_JSON) {
						jsonContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {
						imageContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_FILE) {
						fileContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_XML) {
						xmlContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_STREAM) {
						streamContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_STRING) {
						stringContent();
					}
					httpResponse.clean();
				} catch (Exception e) {
					HttpError httpError = new HttpError(e);
					throwError(httpError);
					connectionRetry = true;// ����
					return;
				}
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- contentHandler -->> ok");
				}
			}
		};

		// ��ȡ��ȼ�����
		private IOUtil.ProgressListener ioProgressListener = new IOUtil.ProgressListener() {
			@Override
			public void notify(int incremental, long cumulant) {
				addProgress(incremental);// ����
				httpSetting.onProgress(httpSetting.start+Long.valueOf(httpResponse.getLength()).longValue(), cumulant);// ������
			}
		};

		// ������ϼ��������
		private HttpGroup.CompleteListener continueListener = new HttpGroup.CompleteListener() {
			@Override
			public void onComplete(Bundle bundle) {
				synchronized (HttpRequest.this) {
					HttpRequest.this.notify();
				}
			}
		};




		/**
		 * XML���ݴ���
		 */
		private void xmlContent() throws Exception {
//			// ͷ�ֶ���ʾ�����������ʱ�����´��?
//			if (null == httpResponse.getType() || !httpResponse.getType().contains("application/json")) {
//				HttpError error = new HttpError();
//				error.setErrorCode(HttpError.RESPONSE_CODE);
//				error.setResponseCode(404);
//				throwError(error);
//				connectionRetry = true;// ����
//				return;
//			}

			// ������
			try {
				httpResponse.setInputData(IOUtil.readAsBytes(httpResponse.getInputStream(),  ioProgressListener));
				if (Log.I) {
					Log.i("HttpGroup", "id:" + httpSetting.getId() + "- response string -->> " + httpResponse.getString());
				}
			} catch (Exception e) {// ��ȡ��̳���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
			try {
				try {
					 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					 DocumentBuilder builder = factory.newDocumentBuilder();
					 byte[] bytes = httpResponse.getInputData();
					 Log.i("HttpGroup", "id:" + httpSetting.getId() + "- response string -->> " + StreamToolBox.loadStringFromStream(StreamToolBox.getByteArrayInputStream(bytes), bytes.length));
					 String xml = StreamToolBox.loadStringFromStream(StreamToolBox.getByteArrayInputStream(bytes), bytes.length);
					 httpResponse.setString(xml);
					 Document dom = builder.parse(StreamToolBox.getByteArrayInputStream(bytes),"UTF-8");
				     Element root = dom.getDocumentElement();
					 httpResponse.setRoot(root);
				} catch (Exception e) {
					// SAXParseException
					byte[] bytes = httpResponse.getInputData();
					String xml = StreamToolBox.loadStringFromStream(StreamToolBox.getByteArrayInputStream(bytes), bytes.length);
					jsonContent(xml);
				}
			}catch (Exception e) {// ���� json ��ʽ
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- Can not format json -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}

		}


		/**
		 * Stream���ݴ���
		 */
		private void streamContent() throws Exception {
//			// ͷ�ֶ���ʾ�����������ʱ�����´��?
//			if (null == httpResponse.getType() || !httpResponse.getType().contains("application/json")) {
//				HttpError error = new HttpError();
//				error.setErrorCode(HttpError.RESPONSE_CODE);
//				error.setResponseCode(404);
//				throwError(error);
//				connectionRetry = true;// ����
//				return;
//			}

			// ������
			try {
				httpResponse.setInputData(IOUtil.readAsBytes(httpResponse.getInputStream(),  ioProgressListener));
				if (Log.I) {
					Log.i("HttpGroup", "id:" + httpSetting.getId() + "- response string -->> " + httpResponse.getString());
				}
			} catch (Exception e) {// ��ȡ��̳���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
			try {
				httpResponse.setByteArrayInputStream(new ByteArrayInputStream(httpResponse.getInputData()));
			}catch (Exception e) {// ���� json ��ʽ
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- Can not format json -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}

		}
		
		/**
		 * string ���ݴ���
		 */
		private void stringContent() throws Exception {
			// ͷ�ֶ���ʾ�����������ʱ�����´��?
			if (null == httpResponse.getType() || !(httpResponse.getType().contains("application/xml; charset=UTF-8") || httpResponse.getType().contains("application/atom+xml") || httpResponse.getType().contains("text/xml") || httpResponse.getType().contains("application/json") || httpResponse.getType().contains("text/plain") || httpResponse.getType().contains("text/html"))) {
				HttpError error = new HttpError();
				error.setErrorCode(HttpError.RESPONSE_CODE);
				error.setResponseCode(404);
				throwError(error);
				connectionRetry = true;// ����
				return;
			}
			String charset = sCharset;
			String contentType = httpResponse.getType();
			String charsetTag = "charset=";
			if (contentType != null && (httpResponse.getType().contains("application/atom+xml") || httpResponse.getType().contains("text/xml") || httpResponse.getType().contains("text/html"))) {
			    contentType = contentType.toUpperCase();
			    charsetTag = charsetTag.toUpperCase();
			    if (contentType.contains(charsetTag)) {
			        contentType = contentType.substring(contentType.indexOf(charsetTag));
			        contentType = contentType.substring(charsetTag.length());
			        charset = contentType;
			    } else
			        charset = "GBK";
			}

			// ������
			try {
				httpResponse.setString(IOUtil.readAsString(httpResponse.getInputStream(), charset, ioProgressListener));

				if (Log.I) {
					Log.i("HttpGroup", "id:" + httpSetting.getId() + "- response string -->> " + httpResponse.getString());
				}
			} catch (Exception e) {// ��ȡ��̳���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
			return;
		}


		/**
		 * json ���ݴ���
		 */
		private void jsonContent() throws Exception {
			// ͷ�ֶ���ʾ�����������ʱ�����´��?
			if (null == httpResponse.getType() || !(httpResponse.getType().contains("application/json") || httpResponse.getType().contains("text/plain"))) {
				HttpError error = new HttpError();
				error.setErrorCode(HttpError.RESPONSE_CODE);
				error.setResponseCode(404);
				throwError(error);
				connectionRetry = true;// ����
				return;
			}

			// ������
			try {
				httpResponse.setString(IOUtil.readAsString(httpResponse.getInputStream(), sCharset, ioProgressListener));

				if (Log.I) {
					Log.i("HttpGroup", "id:" + httpSetting.getId() + "- response string -->> " + httpResponse.getString());
				}
			} catch (Exception e) {// ��ȡ��̳���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
			
			jsonContent(httpResponse.getString());
		}
		
		private void jsonContent(String content) throws Exception {
			try {
				httpResponse.setJsonObject(new JSONObjectProxy(new JSONObject(content)));
			} catch (JSONException e) {// ���� json ��ʽ
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- Can not format json -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
			Integer jsonCode = null;
			try {
				jsonCode = Integer.valueOf(httpResponse.getJSONObject().getString("code"));
			} catch (NumberFormatException e) {// jsonCode ���ָ�ʽ����
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- Can not format jsonCode -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;
				return;
			} catch (JSONException e) {// jsonCode �����ڴ���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- not find jsonCode -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
			if (null != jsonCode && jsonCode != 0&&jsonCode != 52&&jsonCode != 1&&jsonCode!=60) {// jsonCode
				// ��֧��ָ��
				if (jsonCode.equals(9)) {
					queryMd5Key(continueListener);

					// ���̹߳�����ͣ���ȴ������̻߳�ȡMd5Key��
					synchronized (HttpRequest.this) {
						try {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId() + "- encrypt wait start -->> " + httpSetting.getUrl());
							}
							HttpRequest.this.wait();
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId() + "- encrypt wait end -->> " + httpSetting.getUrl());
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// ���Կ�ͷ�ͻ����¼�����
					connectionRetry = true;// ����
					return;
				}

				// ��֧��ָ��
				if (jsonCode.equals(10)) {

					// ���Ϊ��Ҫ����
					setModule(MODULE_STATE_ENCRYPT);

					// ���Կ�ͷ�ͻ����¼�����
					connectionRetry = true;// ����
					return;
				}

				if (jsonCode == -1 || // ���ڳ��?�����Ե�jsonCode
						jsonCode == -2) {
					// ������ȷ����
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.JSON_CODE);
					error.setJsonCode(jsonCode);
					error.setHttpResponse(httpResponse);
					throwError(error);
					connectionRetry = true;// ����
					return;
				}

				if (jsonCode == 30 || jsonCode == 1 || jsonCode == 2) {
					final MyActivity myActivity = httpGroupSetting.getMyActivity();
					final String message = httpResponse.getJSONObject().getStringOrNull("message");
					if (null != myActivity) {
					if(httpSetting.isShowToast()){
						myActivity.post(new Runnable() {
							public void run() {
								if (message == null)
									Toast.makeText(myActivity, "���緱æ", Toast.LENGTH_LONG).show();
								else
									Toast.makeText(myActivity, message, Toast.LENGTH_LONG).show();
							}
						});
						 }
					}
					// ������ȷ����
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.JSON_CODE);
					error.setJsonCode(jsonCode);
					error.setHttpResponse(httpResponse);
					error.setNoRetry(true);
					throwError(error);
					// connectionRetry = true;// ������
					return;
				}
			}		
		}

		/**
		 * image ���ݴ���
		 */
		private void imageContent() throws Exception {
			// ͷ�ֶ���ʾ�����������ʱ�����´��?
			if (null == httpResponse.getType() || !httpResponse.getType().contains("image/")) {
				HttpError error = new HttpError();
				error.setErrorCode(HttpError.RESPONSE_CODE);
				error.setResponseCode(404);
				throwError(error);
				connectionRetry = true;// ����
				return;
			}
			// ������
			try {
				httpResponse.setInputData(IOUtil.readAsBytes(httpResponse.getInputStream(), ioProgressListener));
		//	Bitmap bitmap = BitmapFactory.decodeByteArray(httpResponse.getInputData(), 0, httpResponse.getInputData().length, getBitmapOpt());
				Bitmap bitmap = ImageUtil.getImage( new ByteArrayInputStream(httpResponse.getInputData()));
				bitmap = ImageUtil.CropForExtraWidth(bitmap);
				if (bitmap != null) {
					httpResponse.setBitmap(bitmap);
					httpResponse.setDrawable(new BitmapDrawable(bitmap));
				}
			} catch (Throwable e) {// ��ȡ��̳���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- image content connection read error -->> ");
				}
				HttpError httpError = new HttpError(e);
				httpError.setNoRetry(true);
				throwError(httpError);
				return;
			}
			// �߻���
		}

		/**
		 * file ���ݴ���
		 */
		private void fileContent() {
			// ��ʾ�����������ʱ�����´��?
			// ������ʲô����������д���ļ�����ȥ
			try {
				FileGuider savePath = httpSetting.getSavePath();

				if (null != savePath) {
					// ȷ������·��
				}

				// TODO Ӧ���ж���� savePath Ϊ null
				// TODO ���Զ��ṩ���·����Ҳ�����ṩ���·����Ӧ���ж��ַ�ʽ��
				savePath.setAvailableSize(httpSetting.start+httpResponse.getLength());// ����ռ��С
				File file = new File(savePath.getFilePath());
//				IOUtil.readAsFile(httpResponse.getInputStream(), fileOutputStream, ioProgressListener, this);
				Log.i("zhoubo", "11111111111111111111");
				IOUtil.readAsFile(httpResponse.getInputStream(), file, ioProgressListener,httpSetting.start ,this);
				Log.i("zhoubo", "2222222222222222222");
				//				File dir = MyApplication.getInstance().getFilesDir();
				File apkFilePath = new File(savePath.getFilePath());
//				if (Log.D) {
//					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- download() apkFilePath -->> " + apkFilePath);
//				}
//				if (isStop()) {
//					apkFilePath.delete();
//				}
				httpResponse.setSaveFile(apkFilePath);
			} catch (Exception e) {// ��ȡ��̳���
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- file content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// ����
				return;
			}
		}

		/**
		 * ���Ͷ�λ
		 */
		public void typeHandler() {
			nextHandler();
		}

		private Options getBitmapOpt() {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			if (httpResponse.getLength() > 1024 * 64) {
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId() + "- opt.inSampleSize -->> " + 2);
				}
				opt.inSampleSize = 2;
			}
			return opt;
		}

		protected static final int MODULE_STATE_DISABLE = 0;// ����
		protected static final int MODULE_STATE_ENCRYPT = 3;// ����

		/**
		 * ���״̬
		 */
		protected boolean checkModule(int state) {
			if (null != httpSetting.getFunctionId() && //
					null != mModules) {
				Integer state_ = mModules.getIntOrNull(httpSetting.getFunctionId());
				if (null != state_ && state == state_) {
					return true;
				}
			}
			return false;
		}

		/**
		 * ����״̬
		 */
		protected void setModule(int state) {
			if (null != httpSetting.getFunctionId() && //
					null != mModules) {
				try {
					mModules.put(httpSetting.getFunctionId(), state);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * ��Ӧ��װ��
	 */
	public class HttpResponse {

		private InputStream inputStream;
		private byte[] inputData;
		private InputStream byteArrayInputStream;
		private SoftReference<byte[]> softReferenceInputData;
		private Bitmap bitmap;
		private SoftReference<Bitmap> softReferenceBitmap;
		private Drawable drawable;
		private SoftReference<Drawable> softReferenceDrawable;
		private File saveFile;
		private String string;
		private JSONObjectProxy jsonObject;
		private Element root;

		@SuppressWarnings("unused")
		private HttpURLConnection httpURLConnection;
		private Map<String, List<String>> headerFields;
		HttpSetting httpSetting;
//		public HttpSetting getHttpSetting() {
//			return this.httpSetting;
//		}

		private int code;// ��Ӧ��
		private long length;// �����
		private String type;// ý������

		// ����
		private void imageClean() {
			setSoftReferenceInputData(new SoftReference<byte[]>(inputData));
			softReferenceBitmap = new SoftReference<Bitmap>(bitmap);
			softReferenceDrawable = new SoftReference<Drawable>(drawable);
			inputData = null;
			bitmap = null;
			drawable = null;
		}


		public Element getRoot() {
			return root;
		}


		public void setRoot(Element root) {
			this.root = root;
		}



		public InputStream getByteArrayInputStream() {
			return byteArrayInputStream;
		}


		public void setByteArrayInputStream(InputStream byteArrayInputStream) {
			this.byteArrayInputStream = byteArrayInputStream;
		}

		/**
		 * ��ֱ�Ӵӻ�����ȡ����ݶ�������������ʱ�����ܻ�ʹ�ô˹��캯��
		 */
		public HttpResponse() {
		}

		/**
		 * ��ֱ�Ӵӻ�����ȡ����ݶ�������������ʱ�����ܻ�ʹ�ô˹��캯��
		 */
		public HttpResponse(Drawable drawable) {
			this.drawable = drawable;
		}

		public HttpResponse(HttpURLConnection httpConnection) {
			this.httpURLConnection = httpConnection;
		}

		public void clean() {
			this.httpURLConnection = null;
		}

		public void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		public void setJsonObject(JSONObjectProxy jsonObject) {
			this.jsonObject = jsonObject;
		}

		public JSONObjectProxy getJSONObject() {
			return jsonObject;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public long getLength() {
			return length;
		}

		public void setLength(long length) {
			this.length = length;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Drawable getDrawable() {
			if (null != drawable) {
				Drawable drawable = this.drawable;
				imageClean();
				return drawable;
			} else {
				if(softReferenceDrawable == null) return null;//new ExceptionDrawable(MyApplication.getInstance(), MyApplication.getInstance().getString(R.string.no_image));
				return softReferenceDrawable.get();
			}
		}

		public Drawable getThumbDrawable(float targetWidth, float targetHeight) {

			Bitmap sourceBitmap = getBitmap();

			// �������ű���1�����ֱ���
			int sourceWidth = sourceBitmap.getWidth();
			int sourceHeight = sourceBitmap.getHeight();
			float scale;
			if (sourceWidth > sourceHeight) {
				scale = targetWidth / sourceWidth;
			} else {
				scale = targetHeight / sourceHeight;
			}

			// ��Ҫ��С
			if (scale < 1) {
				// �������ű���2�����ֱ���
				int width = Math.round(scale * sourceWidth);
				int height = Math.round(scale * sourceHeight);

				// ��������
				Bitmap bitmap = Bitmap.createScaledBitmap(sourceBitmap, width, height, false);
				setBitmap(bitmap);
				sourceBitmap.recycle();
				setDrawable(new BitmapDrawable(bitmap));
				return getDrawable();// �����������Դ��
			}

			// ����Ҫ��С
			return getDrawable();// �����������Դ��
		}

		public void setDrawable(Drawable drawable) {
			this.drawable = drawable;
		}

		public byte[] getInputData() {
			return inputData;
		}

		public void setInputData(byte[] inputData) {
			this.inputData = inputData;
		}

		public Bitmap getBitmap() {
			if (null != bitmap) {
				Bitmap bitmap = this.bitmap;
				imageClean();
				return bitmap;
			} else {
				if(softReferenceBitmap == null) return null;//BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.drawable.image_logo);
				return softReferenceBitmap.get();
			}
		}

		public void setBitmap(Bitmap bitmap) {
			if (null == bitmap) {
				throw new RuntimeException("bitmap is null");
			}
			this.bitmap = bitmap;
		}

		private String imagePath = null;
		public void setImagePath(String imagePath) {
			this.imagePath = imagePath;
		}
		public String getImagePath() {
			return imagePath;
		}
		public File getSaveFile() {
			return saveFile;
		}

		public void setSaveFile(File saveFile) {
			this.saveFile = saveFile;
		}

		public Map<String, List<String>> getHeaderFields() {
			return headerFields;
		}

		public void setHeaderFields(Map<String, List<String>> headerFields) {
			this.headerFields = headerFields;
		}

		public String getHeaderField(String key) {
			if (null == headerFields) {
				return null;
			}
			List<String> listStr = headerFields.get(key);
			if (null == listStr || listStr.size() < 1) {
				return null;
			}
			return listStr.get(0);
		}


		public SoftReference<byte[]> getSoftReferenceInputData() {
			return softReferenceInputData;
		}


		public void setSoftReferenceInputData(SoftReference<byte[]> softReferenceInputData) {
			this.softReferenceInputData = softReferenceInputData;
		}

	}

	/* ��ı��ӵ����¼� */
	protected void onStart() {
		if (null != onGroupStartListener)
			onGroupStartListener.onStart();
		onGroupStartListener = null;
	}

	// Ϊ�� end -->>
	protected void onEnd() {
		if (null != onGroupEndListener)
			onGroupEndListener.onEnd();
		onGroupEndListener = null;
	}

	private int completesCount = 0;

	protected void addCompletesCount() {
		this.completesCount += 1;
		if (completesCount == httpList.size())
			onEnd();
	}

	// <<-- Ϊ�� end

	protected void onError() {
		if (null != onGroupErrorListener)
			onGroupErrorListener.onError();
		onGroupErrorListener = null;
	}

	// Ϊ�� progress -->>
	private void onProgress(long maxProgress, long progress) {
		if (null != onGroupProgressListener)
			onGroupProgressListener.onProgress(maxProgress, progress);
	}

	private long maxProgress = 0;
	private long progress = 0;

	protected void addMaxProgress(long maxProgress) {// TODO ���̵߳������������������ܻ����߳�����
		this.maxProgress += maxProgress;
		onProgress(this.maxProgress, this.progress);
	}

	protected void addProgress(int progress) {// TODO ���̵߳������������������ܻ����߳�����
		this.progress += progress;
		onProgress(this.maxProgress, this.progress);
	}

	// <<-- Ϊ�� progress

	// Ϊ�� step -->>
	private void onStep(int maxStep, int step) {
		if (null != onGroupStepListener)
			onGroupStepListener.onStep(maxStep, step);
	}

	private int maxStep = 0;
	private int step = 0;

	protected void addMaxStep(int maxStep) {// TODO ���̵߳������������������ܻ����߳�����
		this.maxStep += maxStep;
		onStep(this.maxStep, this.step);
	}

	protected void addStep(int step) {// TODO ���̵߳������������������ܻ����߳�����
		this.step += step;
		onStep(this.maxStep, this.step);
	}

	// <<-- Ϊ�� step

	/* ������� - ��� */
	private OnGroupStartListener onGroupStartListener;
	private OnGroupEndListener onGroupEndListener;
	private OnGroupErrorListener onGroupErrorListener;
	private OnGroupProgressListener onGroupProgressListener;
	private OnGroupStepListener onGroupStepListener;

	public void setOnGroupStartListener(OnGroupStartListener onGroupStartListener) {
		this.onGroupStartListener = onGroupStartListener;
	}

	public void setOnGroupEndListener(OnGroupEndListener onGroupEndListener) {
		this.onGroupEndListener = onGroupEndListener;
	}

	public void setOnGroupErrorListener(OnGroupErrorListener onGroupErrorListener) {
		this.onGroupErrorListener = onGroupErrorListener;
	}

	public void setOnGroupProgressListener(OnGroupProgressListener onGroupProgressListener) {
		this.onGroupProgressListener = onGroupProgressListener;
	}

	public void setOnGroupStepListener(OnGroupStepListener onGroupStepListener) {
		this.onGroupStepListener = onGroupStepListener;
	}

	/**
	 * ������Ϣ��װ
	 */
	public static class HttpError {

		public static final int EXCEPTION = 0;
		public static final int TIME_OUT = 1;
		public static final int RESPONSE_CODE = 2;
		public static final int JSON_CODE = 3;

		public static final String EXCEPTION_MESSAGE_ATTESTATION_WIFI = "attestation WIFI";
		public static final String EXCEPTION_MESSAGE_NO_CACHE = "no cache";

		/**
		 * ����ķ���
		 */
		private int errorCode;

		/**
		 * �������responseCode
		 */
		private int responseCode;

		/**
		 * �������jsonCode
		 */
		private int jsonCode;

		/**
		 * ����
		 */
		private String message;

		/**
		 * ��������쳣
		 */
		private Throwable exception;

		/**
		 * �ڼ��γ���
		 */
		private int times;

		/**
		 * ��������
		 */
		private boolean noRetry;

		private HttpResponse httpResponse;

		public HttpError() {

		}

		public HttpError(Throwable exception) {
			this.errorCode = EXCEPTION;
			this.exception = exception;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getErrorCodeStr() {
			switch (errorCode) {
			case EXCEPTION:
				return "EXCEPTION";
			case TIME_OUT:
				return "TIME_OUT";
			case RESPONSE_CODE:
				return "RESPONSE_CODE";
			case JSON_CODE:
				return "JSON_CODE";
			default:
				return "UNKNOWN";
			}
		}

		public void setErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public int getJsonCode() {
			return jsonCode;
		}

		public void setJsonCode(int jsonCode) {
			this.jsonCode = jsonCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Throwable getException() {
			return exception;
		}

		public void setException(Throwable exception) {
			this.exception = exception;
		}

		public int getTimes() {
			return times;
		}

		public void setTimes(int times) {
			this.times = times;
		}

		public HttpResponse getHttpResponse() {
			return httpResponse;
		}

		public void setHttpResponse(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		@Override
		public String toString() {
			if (null != getException()) {
				if (Log.D) {
					Log.d("HttpGroup", "HttpError Exception -->> ", getException());
				}
			}
			return "HttpError [errorCode=" + getErrorCodeStr() + ", exception=" + exception + ", jsonCode=" + jsonCode + ", message=" + message + ", responseCode=" + responseCode + ", time=" + times + "]";
		}

		/**
		 * �Ƿ���������
		 */
		public boolean isNoRetry() {
			return noRetry;
		}

		/**
		 * �����Ƿ���������
		 */
		public void setNoRetry(boolean noRetry) {
			this.noRetry = noRetry;
		}

	}

	/**
	 * Copyright 2011 Jingdong Android Mobile Application
	 *
	 * @author lijingzuo
	 *
	 *         Time: 2011-1-10 ����12:52:06
	 *
	 *         Name:
	 *
	 *         Description: ���������÷�װ
	 */
	public static class HttpGroupSetting {

		public static final int PRIORITY_FILE = 500;
		public static final int PRIORITY_JSON = 1000;
		public static final int PRIORITY_IMAGE = 5000;
		public static final int PRIORITY_XML = 10000;
		public static final int PRIORITY_STRING = 20000;
		public static final int PRIORITY_STREAM = 30000;

		public static final int TYPE_FILE = 500;
		public static final int TYPE_JSON = 1000;
		public static final int TYPE_IMAGE = 5000;
		public static final int TYPE_XML = 10000;
		public static final int TYPE_STRING = 20000;
		public static final int TYPE_STREAM = 30000;

		private MyActivity myActivity;
		private int priority;
		private int type;

		public MyActivity getMyActivity() {
			return myActivity;
		}

		public void setMyActivity(MyActivity myActivity) {
			this.myActivity = myActivity;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
			if (0 == priority) {
				switch (type) {
				case TYPE_FILE:
					setPriority(PRIORITY_FILE);
					break;
				case TYPE_JSON:
					setPriority(PRIORITY_JSON);
					break;
				case TYPE_IMAGE:
					setPriority(PRIORITY_IMAGE);
					break;
				case TYPE_STRING:
					setPriority(PRIORITY_STRING);
					break;
				case TYPE_STREAM:
					setPriority(PRIORITY_STREAM);
					break;
				}
			}
		}

	}

	public interface HttpSettingParams {

		void putJsonParam(String key, Object value);

		void putMapParams(String key, String value);

	}

	/**
	 * Copyright 2010 Jingdong Android Mobile Application
	 *
	 * @author lijingzuo
	 *
	 *         Time: 2010-12-27 ����05:26:55
	 *
	 *         Name:
	 *
	 *         Description: ������Ϣ��װ
	 */
	public static class HttpSetting implements HttpSettingParams, Cloneable{

		public static final int EFFECT_NO = 0;// ��ҪЧ��
		public static final int EFFECT_DEFAULT = 1;// Ĭ��Ч��

		public static final int EFFECT_STATE_NO = 0;
		public static final int EFFECT_STATE_YES = 1;

		public static final int CACHE_MODE_AUTO = 0;
		public static final int CACHE_MODE_ONLY_CACHE = 1;
		public static final int CACHE_MODE_ONLY_NET = 2;
		private boolean isShowToast = true;
		private int id;
		private String host;
		private String functionId;
		private String url; // firstHandler()�����url����װ����װ��ɺ�ֵ��semiUrl��Ȼ����beforeConnection()�������semi���ó�finalurl
		private String semiUrl;
		private String finalUrl; // ����Url����ͨUrl�������ƴ�ӳ�finalUrl
		private FileGuider savePath;
		private JSONObject jsonParams;
		private Map<String, String> mapParams;
		private OnStartListener onStartListener;
		private OnProgressListener onProgressListener;
		private OnEndListener onEndListener;
		private OnErrorListener onErrorListener;
		private OnReadyListener onReadyListener;
		private int connectTimeout;
		private int readTimeout;
		private String md5;
		private int type;
		private int priority;// 0:�̳�
		private boolean post = "post".equals(Configuration.getProperty(Configuration.REQUEST_METHOD, "post"));
		private boolean notifyUser = false;
		private boolean notifyUserWithExit = false;// ����������һ�����֣��˳����߼��ɼ��������?
		private boolean localMemoryCache = false;
		private boolean localFileCache = false;
		private boolean isURLEncoder = true;
		private long localFileCacheTime = CacheTimeConfig.DEFAULT;// 0:���ñ��棨��������֣���Ϊ���������͵�Σ�գ�
		private boolean needGlobalInitialization = true;
		private int effect = 1;// 0:��ҪЧ��,1:Ĭ��Ч��
		private int effectState = 0;// 0:δ����,1:�Ѵ���
		private int cacheMode = 0;// ����ģʽ��0:�Զ�ģʽ���л����û��棬û���������磩,1:ֻʹ�û���,2:ֻʹ������
		private HttpRequest httpRequest;
		private long  start; //�������
		private long  end; //�������
		private boolean isSuffix = true; //
		private boolean isSaveCookie = true;
		private boolean isEncoder = false; // body �Ƿ����  true Ϊ���� false Ϊ������
		private BodyEncodeEntity encodeEntity;
		private Runnable successRunnable;
		private Runnable failedRunnable;
		
		public Runnable getSuccessRunnable() {
			return successRunnable;
		}

		public void setSuccessRunnable(Runnable successRunnable) {
			this.successRunnable = successRunnable;
		}

		public Runnable getFailedRunnable() {
			return failedRunnable;
		}

		public void setFailedRunnable(Runnable failedRunnable) {
			this.failedRunnable = failedRunnable;
		}

		
		public boolean isShowToast() {
			return isShowToast;
		}

		public void setShowToast(boolean isShowToast) {
			this.isShowToast = isShowToast;
		}

		
		@Override
	    public Object clone() {
			HttpSetting httpSetting = null;
			
			try {
				httpSetting = (HttpSetting) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			if (httpSetting != null) {
				httpSetting.setJsonParams(this.getJsonParams());
			}

	        return httpSetting;
	    }		

		public BodyEncodeEntity getEncodeEntity() {
			return encodeEntity;
		}
		public void setEncodeEntity(BodyEncodeEntity encodeEntity) {
			this.encodeEntity = encodeEntity;
		}
		public void setIsEncoder(boolean isEncoder){
			this.isEncoder = isEncoder;
		}
		public boolean getIsEncoder(){
			return isEncoder;
		}
		public boolean isSaveCookie() {
			return isSaveCookie;
		}

		public void setSaveCookie(boolean isSaveCookie) {
			this.isSaveCookie = isSaveCookie;
		}

		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public boolean isSuffix() {
			return isSuffix;
		}

		public void setSuffix(boolean isSuffix) {
			this.isSuffix = isSuffix;
		}


		public HttpRequest getHttpRequest() {
			return httpRequest;
		}

		public void setHttpRequest(HttpRequest httpRequest) {
			this.httpRequest = httpRequest;
		}

		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public String getFunctionId() {
			return functionId;
		}

		public void setFunctionId(String functionId) {
			this.functionId = functionId;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
		
		public String getSemiUrl() {
			return semiUrl;
		}

		public void setSemiUrl(String semiUrl) {
			this.semiUrl = semiUrl;
		}		

		public String getFinalUrl() {
			return finalUrl;
		}

		public void setFinalUrl(String finalUrl) {
			this.finalUrl = finalUrl;
		}

		public JSONObject getJsonParams() {
			return jsonParams;
		}

		public boolean isURLEncoder() {
			return isURLEncoder;
		}

		public void setURLEncoder(boolean isURLEncoder) {
			this.isURLEncoder = isURLEncoder;
		}

		/**
		 * ������Ӳ���
		 */
		@Deprecated
		public void setJsonParams(JSONObject params) {
			if (null == params) {
				return;
			}
			try {
				this.jsonParams = new JSONObject(params.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void putJsonParam(String key, Object value) {
			if (null == this.jsonParams) {
				this.jsonParams = new JSONObject();
			}
			try {
				this.jsonParams.put(key, value);
			} catch (JSONException e) {
				if (Log.D) {
					Log.d("HttpGroup", "JSONException -->> ", e);
				}
			}
		}

		public Map<String, String> getMapParams() {
			return mapParams;
		}

		/**
		 * ������Ӳ���
		 */
		@Deprecated
		public void setMapParams(Map<String, String> mapParams) {
			if (null == mapParams) {
				return;
			}
			Set<String> keySet = mapParams.keySet();
			for (String key : keySet) {
				putMapParams(key, mapParams.get(key));
			}
		}

		public void putMapParams(String key, String value) {
			if (null == this.mapParams) {
				this.mapParams = new HashMap<String, String>();
			}
			if(isURLEncoder){
			try {
				value = URLEncoder.encode(value, sCharset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
				}
			this.mapParams.put(key, value);
		}

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public OnStartListener getOnStartListener() {
			return onStartListener;
		}

		public OnProgressListener getOnProgressListener() {
			return onProgressListener;
		}

		public OnEndListener getOnEndListener() {
			return onEndListener;
		}

		public OnErrorListener getOnErrorListener() {
			return onErrorListener;
		}

		public OnReadyListener getOnReadyListener() {
			return onReadyListener;
		}

		public void setListener(HttpTaskListener httpTaskListener) {
			if (httpTaskListener == null) {
				this.onErrorListener = null;
				this.onStartListener = null;
				this.onProgressListener = null;
				this.onEndListener = null;
				this.onReadyListener = null;
			}
			if (httpTaskListener instanceof CustomOnAllListener) {
				setEffect(0);// û��Ч��
			}
			if (httpTaskListener instanceof DefaultEffectHttpListener) {
				setEffectState(1);// �Ѵ���
			}
			if (httpTaskListener instanceof OnErrorListener) {
				this.onErrorListener = (OnErrorListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnStartListener) {
				this.onStartListener = (OnStartListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnProgressListener) {
				this.onProgressListener = (OnProgressListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnEndListener) {
				this.onEndListener = (OnEndListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnReadyListener) {
				this.onReadyListener = (OnReadyListener) httpTaskListener;
			}
		}

		public void onStart() {
			if (null != onStartListener) {
				onStartListener.onStart();
			}
		}

		public void onEnd(HttpResponse httpResponse) {
			if (null != onEndListener) {
				onEndListener.onEnd(httpResponse);
			}
		}

		public void onError(HttpError httpError) {
			if (null != onErrorListener) {
				onErrorListener.onError(httpError);
			}
		}

		public void onProgress(long max, long progress) {
			if (null != onProgressListener) {
				onProgressListener.onProgress(max, progress);
			}
		}

		public String getMd5() {
			if (null == md5) {
				String urlTempStr = getSemiUrl();
				if (null == urlTempStr) {
					return null;
				}
				int start = 0;
				for (int i = 0; i < 3; i++) {
					start = urlTempStr.indexOf("/", start + 1);
				}
				if (start == -1) {
					return null;
				}
				String urlPath = getSemiUrl().substring(start);
				if (isPost()) {
					md5 = Md5Encrypt.md5(urlPath + getJsonParams());
				} else {
					md5 = Md5Encrypt.md5(urlPath);
				}
				if (Log.D) {
					Log.d("HttpGroup", "urlPath -->> " + urlPath + " md5 -->> " + md5);
				}
			}
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public boolean isPost() {
			return post;
		}

		public void setPost(boolean post) {
			this.post = post;
		}

		public int getReadTimeout() {
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
		}

		public boolean isNotifyUser() {
			return notifyUser;
		}

		public void setNotifyUser(boolean notifyUser) {
			this.notifyUser = notifyUser;
		}

		public boolean isLocalMemoryCache() {
			return localMemoryCache;
		}

		public void setLocalMemoryCache(boolean localMemoryCache) {
			this.localMemoryCache = localMemoryCache;
		}

		public boolean isLocalFileCache() {
			return localFileCache;
		}

		public void setLocalFileCache(boolean localFileCache) {
			this.localFileCache = localFileCache;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public long getLocalFileCacheTime() {
			return localFileCacheTime;
		}

		public void setLocalFileCacheTime(long localFileCacheTime) {
			this.localFileCacheTime = localFileCacheTime;
		}

		public FileGuider getSavePath() {
			return savePath;
		}

		/**
		 * ע�ⲻҪ��ͬһ�����������������
		 */
		public void setSavePath(FileGuider savePath) {
			this.savePath = savePath;
		}

		public boolean isNotifyUserWithExit() {
			return notifyUserWithExit;
		}

		public void setNotifyUserWithExit(boolean notifyUserOrExit) {
			this.notifyUserWithExit = notifyUserOrExit;
		}

		public boolean isNeedGlobalInitialization() {
			return needGlobalInitialization;
		}

		public void setNeedGlobalInitialization(boolean needGlobalInitialization) {
			this.needGlobalInitialization = needGlobalInitialization;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getEffect() {
			return effect;
		}

		public void setEffect(int effect) {
			this.effect = effect;
		}

		public int getEffectState() {
			return effectState;
		}

		public void setEffectState(int effectState) {
			this.effectState = effectState;
		}

		public int getCacheMode() {
			return cacheMode;
		}

		/**
		 * ����ģʽ��0:�Զ�ģʽ���л����û��棬û���������磩,1:ֻʹ�û���,2:ֻʹ������ CACHE_MODE_AUTO��CACHE_MODE_ONLY_CACHE��CACHE_MODE_ONLY_NET
		 */
		public void setCacheMode(int cacheMode) {
			this.cacheMode = cacheMode;
		}

	}

	public static String mergerUrlAndParams(String urlStr, Map<String, String> params) {

		if (null == params) {
			return urlStr;
		}

		Set<String> keySet = params.keySet();
		if (null == keySet || keySet.isEmpty()) {
			return urlStr;
		}

		StringBuilder url = new StringBuilder(urlStr);
		int i = urlStr.indexOf("?");
		if (i == -1) {
			url.append("?");
		} else {
			String queryString = urlStr.substring(i + 1);
			if (!TextUtils.isEmpty(queryString) && !queryString.endsWith("&")) {
				url.append("&");
			}
		}

		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String value = params.get(key);
			url.append(key).append("=").append(value);
			if (iterator.hasNext()) {
				url.append("&");
			}
		}

		return url.toString();
	}

	public static void cleanCookies() {
		cookies = null;
	}

	public static void setCookies(String str) {
		cookies = str;
	}

}
