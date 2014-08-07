package com.GreenLemonMobile.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

/**
 * 对话框统一控制类
 * 
 * TODO 没有很好地解决同一个操作，有可能要放UI线程，有可能不用放UI线程。UI线程和非UI线程之间的协调。
 */
public class DialogController implements AlertDialog.OnClickListener, DialogInterface.OnKeyListener {

	private Context context;
	protected Builder builder;
	protected AlertDialog alertDialog;

	private boolean canBack = false;// 默认不允许后退

	private CharSequence initTitle;
	private CharSequence initMessage;
	private CharSequence initPositiveButton;
	private CharSequence initNeutralButton;
	private CharSequence initNegativeButton;
	private View view;

	/**
	 * 初始化，应该定制后调用（非UI线程）
	 */
	public void init(Context context) {
		this.context = context;
		builder = new Builder(context);
		initContent();
		initButton();
	}

	/**
	 * 初始化内容
	 */
	protected void initContent() {

		// 标题
		if (TextUtils.isEmpty(initTitle)) {
			// builder.setTitle("京东商城");// 默认值
		} else {
			builder.setTitle(initTitle);
		}

		// 信息
		if (TextUtils.isEmpty(initMessage)) {
			// 默认值
		} else {
			builder.setMessage(initMessage);
		}

		// VIEW
		if (null != view) {
			builder.setView(view);
		}

		// 按键事件
		builder.setOnKeyListener(this);
	}

	/**
	 * 初始化按钮
	 */
	protected void initButton() {

		// （左边的按钮）重试
		if (!TextUtils.isEmpty(initPositiveButton)) {
			builder.setPositiveButton(initPositiveButton, this);
		}

		// （中间的按钮）
		if (!TextUtils.isEmpty(initNeutralButton)) {
			builder.setNeutralButton(initNeutralButton, this);
		}

		// （右边的按钮）取消或退出
		if (!TextUtils.isEmpty(initNegativeButton)) {
			builder.setNegativeButton(initNegativeButton, this);
		}

	}

	/**
	 * 显示（UI线程）
	 */
	public void show() {
		if (null != alertDialog) {
			alertDialog.show();
		} else if (null != builder) {
			alertDialog = builder.show();
		} else {
			throw new RuntimeException("builder is null, need init this controller");
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (!isCanBack() && KeyEvent.KEYCODE_BACK == keyCode) {
			return true;
		}
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
	}

	/**
	 * 标题
	 */
	public void setTitle(CharSequence title) {
		if (null != alertDialog) {
			alertDialog.setTitle(title);
		} else if (null != builder) {
			builder.setTitle(title);
		} else {
			initTitle = title;
		}
	}

	/**
	 * 内容
	 */
	public void setMessage(CharSequence message) {
		if (null != alertDialog) {
			alertDialog.setMessage(message);
		} else if (null != builder) {
			builder.setMessage(message);
		} else {
			initMessage = message;
		}
	}

	/**
	 * 左按钮（如果字符串给null或""就隐藏）
	 */
	public void setPositiveButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// 隐藏
				alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
			} else {
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, text, this);
			}
		} else if (null != builder) {
			builder.setPositiveButton(text, this);
		} else {
			initPositiveButton = text;
		}
	}

	/**
	 * 中按钮（如果字符串给null或""就隐藏）
	 */
	public void setNeutralButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// 隐藏
				alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
			} else {
				alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, text, this);
			}
		} else if (null != builder) {
			builder.setNeutralButton(initNeutralButton, this);
		} else {
			initNeutralButton = text;
		}
	}

	/**
	 * 右按钮（如果字符串给null或""就隐藏）
	 */
	public void setNegativeButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// 隐藏
				alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
			} else {
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, text, this);
			}
		} else if (null != builder) {
			builder.setNegativeButton(initNegativeButton, this);
		} else {
			initNegativeButton = text;
		}
	}

	/**
	 * 自定义VIEW
	 */
	public void setView(View view) {
		if (null != alertDialog) {
			alertDialog.setView(view);
		} else if (null != builder) {
			builder.setView(view);
		} else {
			this.view = view;
		}
	}

	public boolean isCanBack() {
		return canBack;
	}

	/**
	 * 设置后退键是否有效，默认不允许后退
	 */
	public void setCanBack(boolean canBack) {
		this.canBack = canBack;
	}

	/**
	 * 单选项列表
	 */
	public static DialogController getSimpleDialogController(Context context, String[] dataSet, int checkedItem, OnClickListener listener) {
		DialogController dialogController = new DialogController();
		dialogController.setCanBack(true);
		dialogController.init(context);
		dialogController.builder.setSingleChoiceItems(dataSet, checkedItem, listener).create();
		return dialogController;
	}

}
