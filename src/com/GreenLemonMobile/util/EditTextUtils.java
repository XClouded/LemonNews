package com.GreenLemonMobile.util;

import android.graphics.Color;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;

public class EditTextUtils {

	public static void setTextWithSelection(EditText editText, String str) {
		editText.setText(str);
		editText.setSelection(str.length());
	}
	public static SpannableStringBuilder ShowErrMsg(CharSequence charSequence) {
		ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#6a645a"));
		SpannableStringBuilder ssbuilder = new SpannableStringBuilder(charSequence);
		ssbuilder.setSpan(foregroundColorSpan, 0, charSequence.length(), 0);
		return ssbuilder;
		
	}

}
