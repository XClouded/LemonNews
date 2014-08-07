package com.GreenLemonMobile.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Shortcut {
	
	static final String ACTION_INSTALL = "com.android.launcher.action.INSTALL_SHORTCUT";
	static final String ACTION_UNINSTALL = "com.android.launcher.action.UNINSTALL_SHORTCUT";	

	public static void addShorcutForApp(final Activity activity) {
		final String packageName = activity.getPackageName();
		final String className = activity.getLocalClassName();
		
		addShortcut2Desktop(activity, packageName, className, null, null, false);
	}
	
	/**
	 * ��ӿ�ݷ�ʽ������
	 * @param context
	 * @param pakageName
	 * @param className
	 * @param shortcutName ���ֶ�ָ����ݷ�ʽ�����ƣ�ɾ��ʱҲҪһ�¡�null��ʹ��Ĭ������
	 * @param icon �ֶ�ָ����ݷ�ʽ��ͼ�꣬null��ʹ��Ĭ��ͼ��
	 * @param duplicate
	 *
	 * ͬʱ��Ҫ��manifest����������Ȩ�ޣ�
	 * <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
	 */	
	public static void addShortcut2Desktop(final Context context, final String packageName, final String className, String shortcutName, Drawable icon, boolean duplicate) {
		Intent addShortcutIntent = new Intent(ACTION_INSTALL);
		
		String label = shortcutName;
		BitmapDrawable iconDrawable = (BitmapDrawable)icon;
		
		if (label == null || iconDrawable == null) {
			PackageManager pm = context.getPackageManager();
			
			try {
				ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
				if (ai != null) {
					if (label == null)
						label = pm.getApplicationLabel(ai).toString();
					
					if (iconDrawable == null)
						iconDrawable = (BitmapDrawable) pm.getApplicationIcon(ai);
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
		addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconDrawable.getBitmap());
		addShortcutIntent.putExtra("duplicate", duplicate);
		
		Intent startIntent = new Intent(Intent.ACTION_MAIN);
		startIntent.setComponent(new ComponentName(packageName, className));
		
		addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);
		
		context.sendBroadcast(addShortcutIntent);
	}
	
	/**
	 * ɾ�������ݷ�ʽ
	 * @param context
	 * @param pakageName
	 * @param className
	 * @param shortcutName ��������ƶ��Ŀ�ݷ�ʽ���Ʋ���Ӧ���������ֶ�ָ���������޷�ɾ����null��ʹ��Ĭ������
	 *
	 * ͬʱ��Ҫ��manifest����������Ȩ�ޣ�
	 * <uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT" />
	 */	
	public static void delShortcutFromDesktop(final Context context, final String packageName, final String className, final String shortcutName) {
		Intent delShortcutIntent = new Intent(ACTION_UNINSTALL);
		
		String label = shortcutName;
		
		PackageManager pm = context.getPackageManager();
		
		try {
			ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
			if (ai != null) {
				if (label == null)
					label = pm.getApplicationLabel(ai).toString();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		delShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
		
		Intent startIntent = new Intent(Intent.ACTION_MAIN);
		startIntent.setComponent(new ComponentName(packageName, className));
		
		delShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);
		
		context.sendBroadcast(delShortcutIntent);
	}
	
	/**
	 * ��ӵ�Shortcutѡ���У�Ĭ�������ϳ���������
	 * @param activity
	 * @param pakageName
	 * @param className
	 * @param shortcutName
	 * @param icon
	 * @param duplicate
	 *
	 * ͬʱ��Ҫ��manifest��Ϊactivity�ṩһ������
	 * action="android.intent.action.CREATE_SHORTCUT"��intent-filter
	 */	
	public static void addShortcut2Options(final Activity activity, final String packageName, final String className, String shortcutName, Drawable icon, boolean duplicate) {
		Intent addShortcutIntent = new Intent();
		
		String label = shortcutName;
		BitmapDrawable iconDrawable = (BitmapDrawable)icon;
		
		if (label == null || iconDrawable == null) {
			PackageManager pm = activity.getPackageManager();
			
			try {
				ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
				if (ai != null) {
					if (label == null)
						label = pm.getApplicationLabel(ai).toString();
					
					if (iconDrawable == null)
						iconDrawable = (BitmapDrawable) pm.getApplicationIcon(ai);
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
		addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconDrawable.getBitmap());
		addShortcutIntent.putExtra("duplicate", duplicate);
		
		Intent startIntent = new Intent(Intent.ACTION_MAIN);
		startIntent.setComponent(new ComponentName(packageName, className));
		
		addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, startIntent);
		
		activity.setResult(Activity.RESULT_OK, addShortcutIntent);
	}
}
