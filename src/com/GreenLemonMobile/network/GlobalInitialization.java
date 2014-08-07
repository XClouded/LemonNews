package com.GreenLemonMobile.network;

import java.util.ArrayList;

import android.content.SharedPreferences;

import com.GreenLemonMobile.network.HttpGroup.GlobalInitializationInterface;
import com.GreenLemonMobile.util.Log;

/**
 * ȫ�ֳ�ʼ���ࣨ����ģʽ��
 */
public class GlobalInitialization implements GlobalInitializationInterface {
	
	abstract class Handler {
		abstract void run();
	}

	private static GlobalInitialization globalInitialization;	

	/**
	 * Ϊ�˷���༭��ѡ�����
	 */
	private SharedPreferences sharedPreferences;

	/**
	 * ��ʶ�Ƿ�serverConfig��updateInfoChecked���Ѿ���ɹ���һ�εĳ�ʼ��
	 */
	private boolean allAlready = false;
	
	private boolean updateInfoChecked = true;
	
	//private boolean alreadyLogin = false;

	private boolean blockWaiting = false;
	
	private ArrayList<Handler> handlers = new ArrayList<Handler>();
	
	private int currentHandlerIndex = 0;
	
	private int globalInitializationState = 0;// 0:δ����ʼ��,1:���ڳ�ʼ��,2:�Ѿ���ʼ��

	public int getGlobalInitializationState() {
		return globalInitializationState;
	}

	public synchronized void setGlobalInitializationState(int globalInitializationState) {
		this.globalInitializationState = globalInitializationState;
	}

	/**
	 * �����Ҫ���������̵߳�����
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
	 * ˽�л����캯��
	 */
	private GlobalInitialization() {

	}

	public synchronized void globalInitialization(boolean wait) {

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization networkInitialization() -->> ");
		}
		
		// ��ʼ�� cpaProcessor
		//cpaProcessor = new CPAUtils.Processor(myActivity.getHandler(), httpGroup, this);

		if (Log.D) {
			Log.d("Temp", "GlobalInitialization globalInitializationState -->> " + globalInitializationState);
		}
		switch (globalInitializationState) {
		case 0:// δ����ʼ��
			globalInitializationStart();
			if (allAlready) {
				globalInitializationState = 2;
				break;
			} else {
				globalInitializationState = 1;
			}
		case 1:// ���ڳ�ʼ��
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
		case 2:// �Ѿ���ʼ��
			break;
		}

	}
	
	public boolean isGlobalInitialized() {
		return globalInitializationState == 2;
	}

	/**
	 * ��ʼ����������ȫ�����ʱ
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
	 * ��ʼ����������ʱ
	 */
	// TODO: whether to send the server Configuration or not? Ŀǰ�ǰ�serverConfigȥ����
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
//			// �豸ע��
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
//			// �������
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
//			serverConfig(true);// �·�����
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
	 * �豸ע��
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
	 * checksofteWareUpdated Ӧ�ø���
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
	 * �˷������ڶ���ע�ᣬ�������������ͻ��˻����ߣ��˷���������initNetwork()֮ǰ���ã�����ͺܶණ��û�г�ʼ���ˡ�
	 */
	public static void regDevice() {
		GlobalInitialization.getInstance().registerDevice(false);
	}	

	/**
	 * �·�����
	 */
	private void serverConfig(final boolean isFirst) {
	}
	
	/**
	 * �豸ע��
	 */
	public void registerDevice(final boolean isFirst) {
	}
	
	/**
	 * checksofteWareUpdated Ӧ�ø���
	 * @param bManual trueΪ�û��ֶ�������汾���£�false��ʾ�����̨�Զ����汾����
	 */
	public static void checksofteWareUpdated(final boolean bManual) {
	}

	/**
	 * �״ε��ñ�����ȷ�������ͨѶ�������˳�Ӧ��
	 */
	public void exit() {
	}


}
