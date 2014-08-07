package com.GreenLemonMobile.util;

public class DefaultEffectHandle {

	private DefaultEffectHttpListener defaultEffectHttpListener;
	
	private int counter;

	public DefaultEffectHandle(MyActivity myActivity) {
		defaultEffectHttpListener = new DefaultEffectHttpListener(null, myActivity);
	}

	/**
	 * 添加遮罩
	 */
	public void addModal() {
		defaultEffectHttpListener.onStart();
		counter++;
	}

	/**
	 * 移除遮罩
	 */
	public void removeModal() {
		while(counter > 0){
			defaultEffectHttpListener.onEnd(null);
			counter--;
		}
	}

}
