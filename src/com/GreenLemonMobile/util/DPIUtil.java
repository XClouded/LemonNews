package com.GreenLemonMobile.util;

import android.content.Context;
import android.view.Display;

public class DPIUtil {

	private static float mDensity;
	private static Display defaultDisplay;

	public static void setDensity(float density) {
		mDensity = density;
	}

	public static void setDefaultDisplay(Display defaultDisplay) {
		DPIUtil.defaultDisplay = defaultDisplay;
	}

	public static int percentWidth(float percent) {
		return (int) (defaultDisplay.getWidth() * percent);
	}

	public static int percentHeight(float percent) {
		return (int) (defaultDisplay.getHeight() * percent);
	}

	public static int dip2px(float dipValue) {
		return (int) (dipValue * mDensity + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		return (int) (pxValue / mDensity + 0.5f);
	}
	
	public static int getWidth(){
		return defaultDisplay.getWidth();
	}
	
	public static int getHeight(){
		return defaultDisplay.getHeight();
	}
	
}
