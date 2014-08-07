package com.GreenLemonMobile.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtils {

	public static Date parseDate(String dateStr) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd aahh:mm:ss").parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
