package com.GreenLemonMobile.util;

import java.util.ArrayList;
import java.util.Iterator;

import com.GreenLemonMobile.network.HttpGroup;
import com.GreenLemonMobile.network.HttpGroup.HttpGroupSetting;
import com.GreenLemonMobile.network.HttpGroup.HttpSetting;
import com.GreenLemonMobile.network.HttpGroup.OnAllListener;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;

public class MyActivity extends Activity implements ITransKey {
	public interface DestroyListener {
		void onDestroy();
	}

	public interface PauseListener {
		void onPause();
	}

	public interface ResumeListener {
		void onResume();
	}
	
	private ArrayList<DestroyListener> destroyListenerList = null;
	private ArrayList<PauseListener> pauseListenerList = null;
	private ArrayList<ResumeListener> resumeListenerList = null;
	private Handler handler;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// ȫ�ֽ�����̲����ص�����
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

		handler = new Handler();
		
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		if (resumeListenerList != null) {
			for (Iterator<ResumeListener> iterator = resumeListenerList.iterator(); iterator.hasNext();) {
				ResumeListener listener = iterator.next();
				listener.onResume();
			}
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (pauseListenerList != null) {
			for (Iterator<PauseListener> iterator = pauseListenerList.iterator(); iterator.hasNext();) {
				PauseListener listener = iterator.next();
				listener.onPause();
			}
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {	
		super.onDestroy();
		
		if (destroyListenerList != null) {
			for (Iterator<DestroyListener> iterator = destroyListenerList.iterator(); iterator.hasNext();) {
				DestroyListener listener = iterator.next();
				listener.onDestroy();
			}
		}
	}

	public void addPauseListener(PauseListener listener) {
		if (pauseListenerList == null)
			pauseListenerList = new ArrayList<PauseListener>();
		if (null != pauseListenerList) {
			pauseListenerList.add(listener);
		}
	}

	public void addResumeListener(ResumeListener listener) {
		if (resumeListenerList == null)
			resumeListenerList = new ArrayList<ResumeListener>();
		
		if (null != resumeListenerList) {
			resumeListenerList.add(listener);
		}
	}

	public void addDestroyListener(DestroyListener listener) {
		if (destroyListenerList == null)
			destroyListenerList = new ArrayList<DestroyListener>();
		
		if (null != destroyListenerList) {
			destroyListenerList.add(listener);
		}
	}

	public void removePauseListener(PauseListener listener) {
		if (null != pauseListenerList) {
			pauseListenerList.remove(listener);
		}
	}

	public void removeResumeListener(ResumeListener listener) {
		if (null != resumeListenerList) {
			resumeListenerList.remove(listener);
		}
	}

	public void removeDestroyListener(DestroyListener listener) {
		if (null != destroyListenerList) {
			destroyListenerList.remove(listener);
		}
	}
	
	/**
	 * ͳһ post �ӿ�
	 */
	public void post(final Runnable action) {
		Log.i("zhoubo", "handler==="+handler);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (MyActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		});
	}

	/**
	 * ͳһ post �ӿ�
	 */
	public void post(final Runnable action, int delayMillis) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (MyActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		}, delayMillis);
	}
	
	/**
	 * ��ʾ���ֲ�ʱ
	 */
	public void onShowModal() {

	}

	/**
	 * �������ֲ�ʱ
	 */
	public void onHideModal() {

	}
	
	public void requestServerJson(HttpSetting httpSetting, OnAllListener listener) {
		httpSetting.setListener(listener);
		getJsonHttpGroupaAsynPool().add(httpSetting);
	}
	
	public void requestServerStream(HttpSetting httpSetting, OnAllListener listener) {
		httpSetting.setListener(listener);
		getJsonHttpGroupaAsynPool().add(httpSetting);
	}	
	
	public HttpGroup getJsonHttpGroupaAsynPool() {
		return getHttpGroupaAsynPool(HttpGroupSetting.TYPE_JSON);
	}
	
	public HttpGroup getStreamHttpGroupaAsynPool() {
		return getHttpGroupaAsynPool(HttpGroupSetting.TYPE_STREAM);
	}
	
	public HttpGroup getStringHttpGroupAsynPool() {
	    return getHttpGroupaAsynPool(HttpGroupSetting.TYPE_STRING);
	}

	public HttpGroup getHttpGroupaAsynPool(int type) {
		HttpGroupSetting setting = new HttpGroupSetting();
		setting.setMyActivity(this);
		setting.setType(type);
		return getHttpGroupaAsynPool(setting);
	}

	public HttpGroup getHttpGroupaAsynPool(HttpGroupSetting setting) {
		HttpGroup httpGroup = new HttpGroup.HttpGroupaAsynPool(setting);
		addDestroyListener(httpGroup);
		return httpGroup;
	}	
}
