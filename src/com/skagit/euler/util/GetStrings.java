package com.skagit.euler.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GetStrings {
	final static DateTimeFormatter _TimeFormatter = DateTimeFormatter.ofPattern("MMM-dd hh:mm:ss");

	public static String getString(final long[] arr) {
		String s = "[";
		final int n = arr.length;
		for (int k = 0; k < n; ++k) {
			s += (k == 0 ? "" : ",") + arr[k];
		}
		s += "]";
		return s;
	}

	public static String getStringFromMillis(final long millis) {
		final Instant instant = Instant.ofEpochMilli(millis);
		final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		return zonedDateTime.format(_TimeFormatter);
	}

	public static String getCurrentTimeString() {
		return getStringFromMillis(System.currentTimeMillis());
	}

}
