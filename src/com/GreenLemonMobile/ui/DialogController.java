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
 * �Ի���ͳһ������
 * 
 * TODO û�кܺõؽ��ͬһ���������п���Ҫ��UI�̣߳��п��ܲ��÷�UI�̡߳�UI�̺߳ͷ�UI�߳�֮���Э����
 */
public class DialogController implements AlertDialog.OnClickListener, DialogInterface.OnKeyListener {

	private Context context;
	protected Builder builder;
	protected AlertDialog alertDialog;

	private boolean canBack = false;// Ĭ�ϲ��������

	private CharSequence initTitle;
	private CharSequence initMessage;
	private CharSequence initPositiveButton;
	private CharSequence initNeutralButton;
	private CharSequence initNegativeButton;
	private View view;

	/**
	 * ��ʼ����Ӧ�ö��ƺ���ã���UI�̣߳�
	 */
	public void init(Context context) {
		this.context = context;
		builder = new Builder(context);
		initContent();
		initButton();
	}

	/**
	 * ��ʼ������
	 */
	protected void initContent() {

		// ����
		if (TextUtils.isEmpty(initTitle)) {
			// builder.setTitle("�����̳�");// Ĭ��ֵ
		} else {
			builder.setTitle(initTitle);
		}

		// ��Ϣ
		if (TextUtils.isEmpty(initMessage)) {
			// Ĭ��ֵ
		} else {
			builder.setMessage(initMessage);
		}

		// VIEW
		if (null != view) {
			builder.setView(view);
		}

		// �����¼�
		builder.setOnKeyListener(this);
	}

	/**
	 * ��ʼ����ť
	 */
	protected void initButton() {

		// ����ߵİ�ť������
		if (!TextUtils.isEmpty(initPositiveButton)) {
			builder.setPositiveButton(initPositiveButton, this);
		}

		// ���м�İ�ť��
		if (!TextUtils.isEmpty(initNeutralButton)) {
			builder.setNeutralButton(initNeutralButton, this);
		}

		// ���ұߵİ�ť��ȡ�����˳�
		if (!TextUtils.isEmpty(initNegativeButton)) {
			builder.setNegativeButton(initNegativeButton, this);
		}

	}

	/**
	 * ��ʾ��UI�̣߳�
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
	 * ����
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
	 * ����
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
	 * ��ť������ַ�����null��""�����أ�
	 */
	public void setPositiveButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// ����
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
	 * �а�ť������ַ�����null��""�����أ�
	 */
	public void setNeutralButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// ����
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
	 * �Ұ�ť������ַ�����null��""�����أ�
	 */
	public void setNegativeButton(CharSequence text) {
		if (null != alertDialog) {
			if (TextUtils.isEmpty(text)) {// ����
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
	 * �Զ���VIEW
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
	 * ���ú��˼��Ƿ���Ч��Ĭ�ϲ��������
	 */
	public void setCanBack(boolean canBack) {
		this.canBack = canBack;
	}

	/**
	 * ��ѡ���б�
	 */
	public static DialogController getSimpleDialogController(Context context, String[] dataSet, int checkedItem, OnClickListener listener) {
		DialogController dialogController = new DialogController();
		dialogController.setCanBack(true);
		dialogController.init(context);
		dialogController.builder.setSingleChoiceItems(dataSet, checkedItem, listener).create();
		return dialogController;
	}

}
