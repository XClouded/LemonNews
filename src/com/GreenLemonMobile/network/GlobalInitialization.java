package com.GreenLemonMobile.network;

import java.util.ArrayList;

import android.content.SharedPreferences;

import com.GreenLemonMobile.network.HttpGroup.GlobalInitializationInterface;
import com.GreenLemonMobile.util.Log;

/**
 * 全局初始化类（单例模式）
 */
public class GlobalInitialization implements GlobalInitializationInterface {
	
	abstract class Handler {
		abstract void run();
	}

	private static GlobalInitialization globalInitialization;	

	/**
	 * 为了方便编辑首选项而已
	 */
	private SharedPreferences sharedPreferences;

	/**
	 * 标识是否serverConfig和updateInfoChecked都已经完成过第一次的初始化
	 */
	private boolean allAlready = false;
	
	private boolean updateInfoChecked = true;
	
	//private boolean alreadyLogin = false;

	private boolean blockWaiting = false;
	
	private ArrayList<Handler> handlers = new ArrayList<Handler>();
	
	private int currentHandlerIndex = 0;
	
	private int globalInitializationState = 0;// 0:未曾初始化,1:正在初始化,2:已经初始化

	public int getGlobalInitializationState() {
		return globalInitializationState;
	}

	public synchronized void setGlobalInitializationState(int globalInitializationState) {
		this.globalInitializationState = globalInitializationState;
	}

	/**
	 * 添加需要阻塞其它线程的任务
	 */
	private void addInitializationTaskList(Handler handler){
		handlers.add(handler);
	}
	
	private void nextInitializationTask() {
		int i = currentHandlerIndex;
		currentHandlerIndex++;
		if (i < handlers.size()) {
			handlers.get(i).run();
		} else 
			globalInitializationEnd();
	}	
	
	public static GlobalInitialization getInstance() {
		if (null == globalInitialization)
			globalInitialization = new GlobalInitialization();
		return globalInitialization;
	}

	public static void initNetwork(boolean wait) {
		Log.d("Temp", "GlobalInitialization initNetwork() -->> ");
		
		GlobalInitialization.getInstance().globalInitialization(wait);
	}

	/**
	 * 私有化构造函数
	 */
	private GlobalInitialization() {

	}

	public synchronized void globalInitialization(boolean wait) {

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization networkInitialization() -->> ");
		}
		
		// 初始化 cpaProcessor
		//cpaProcessor = new CPAUtils.Processor(myActivity.getHandler(), httpGroup, this);

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization globalInitializationState -->> " + globalInitializationState);
		}
		switch (globalInitializationState) {
		case 0:// 未曾初始化
			globalInitializationStart();
			if (allAlready) {
				globalInitializationState = 2;
				break;
			} else {
				globalInitializationState = 1;
			}
		case 1:// 正在初始化
			if (wait) {
				try {
					if (Log.D) {
						Log.d("Temp", "GlobalInitialization wait start -->> ");
					}
					blockWaiting = true;
					wait();
					if (Log.D) {
						Log.d("Temp", "GlobalInitialization wait end -->> ");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			break;
		case 2:// 已经初始化
			break;
		}

	}
	
	public boolean isGlobalInitialized() {
		return globalInitializationState == 2;
	}

	/**
	 * 初始化网络连接全都完成时
	 */
	private synchronized void globalInitializationEnd() {

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization networkInitializationEnd() -->> ");
		}

		globalInitializationState = 2;

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization notifyAll -->> ");
		}
		if (blockWaiting) {
			try {
				notifyAll();
			} catch (IllegalMonitorStateException e) {
			}
		}
		blockWaiting = false;
		
//		GlobalInitialization.getInstance().registerDevice();
//		boolean autoLogin = CommonUtil.getJdSharedPreferences().getBoolean(Contants.AUTO_LOGIN, false);
//		if (autoLogin) {
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					GlobalInitialization.getInstance().AutoLogin();
//				}
//				
//			}).start();
//		}
	}

	/**
	 * 初始化网络连接时
	 */
	// TODO: whether to send the server Configuration or not? 目前是把serverConfig去掉了
	public void globalInitializationStart() {

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization networkInitializationStart() -->> ");
		}

//		sharedPreferences = MyApplication.getInstance().getSharedPreferences(Constant.JD_SHARE_PREFERENCE, Context.MODE_PRIVATE);
//
//		final boolean alreadyDevice =  sharedPreferences.getBoolean("registerDevice", false);
//		final boolean alreadyConfig =  sharedPreferences.getBoolean("serverConfig", false);
		//
		if (/*alreadyDevice &&*/ updateInfoChecked /*&& alreadyLogin*/) {
			allAlready = true;
		}
//		if (!alreadyLogin) {
//			boolean autoLogin = CommonUtil.getJdSharedPreferences().getBoolean(Contants.AUTO_LOGIN, false);
//			if (!autoLogin)
//				allAlready = true;
//		}	

//		if (!alreadyDevice) {
//			if (Log.D) {
//				Log.d("Temp", "not already device -->> ");
//			}
//			// 设备注册
//			addInitializationTaskList(new Handler() {
//				void run() {
//					GlobalInitialization.getInstance().registerDevice();
//				}
//			});	
//		}
		
//		if (!alreadyLogin) {
//			boolean autoLogin = CommonUtil.getJdSharedPreferences().getBoolean(Contants.AUTO_LOGIN, false);
//			if (autoLogin)
//				addInitializationTaskList(new Handler() {
//					void run() {
//						GlobalInitialization.getInstance().AutoLogin();
//					}
//				});
//		}
		
//		if (!updateInfoChecked) {
//			// 升级检测
//			addInitializationTaskList(new Handler() {
//				void run() {
//					GlobalInitialization.getInstance().checksofteWareUpdated();
//				}
//			});
//			//setUpdateInfoChecked(true);
//		}
//		if (!alreadyConfig) {
//			if (Log.D) {
//				Log.d("Temp", "not already device -->> ");
//			}
//			serverConfig(true);// 下发配置
//		}

		if (handlers.size() > 0) {
			new Thread(new Runnable() {
				public void run() {
					currentHandlerIndex = 0;
					nextInitializationTask();
				}
			}).start();
		}

	}
	
	/**
	 * 设备注册
	 * 
	 */
	public void registerDevice() {
	}
	
	public boolean isUpdateInfoChecked() {
		return updateInfoChecked;
	}

	public synchronized void setUpdateInfoChecked(boolean updateInfoChecked) {
		this.updateInfoChecked = updateInfoChecked;
	}
	
	/**
	 * checksofteWareUpdated 应用更新
	 * 
	 */
	private void checksofteWareUpdated() {
	}
	
//	public boolean isAutoLogin() {
//		return alreadyLogin;
//	}
//
//	public void setAutoLogin(boolean autoLogin) {
//		this.alreadyLogin = autoLogin;
//	}
	
	public void AutoLogin() {
	}
	
	public synchronized void unBlockTask() {
		globalInitializationState = 0;
		if (blockWaiting) {
			try {
				notifyAll();
			} catch (IllegalMonitorStateException e) {
			}
		}
		blockWaiting = false;
	}	
	
	/**
	 * 此方法用于定期注册，给服务器反馈客户端还在线，此方法不能在initNetwork()之前调用，否则就很多东西没有初始化了。
	 */
	public static void regDevice() {
		GlobalInitialization.getInstance().registerDevice(false);
	}	

	/**
	 * 下发配置
	 */
	private void serverConfig(final boolean isFirst) {
	}
	
	/**
	 * 设备注册
	 */
	public void registerDevice(final boolean isFirst) {
	}
	
	/**
	 * checksofteWareUpdated 应用更新
	 * @param bManual true为用户手动点击检测版本更新，false表示程序后台自动检测版本更新
	 */
	public static void checksofteWareUpdated(final boolean bManual) {
	}

	/**
	 * 首次调用必须正确与服务器通讯，否则退出应用
	 */
	public void exit() {
	}


}
