package org.earthling.quran.threads.freeminds;

import java.util.Locale;
import java.util.ResourceBundle;

public class ApplicationProperties {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(
			"application", Locale.ENGLISH);

	public static String get(String key) {
		String value = resourceBundle.getString(key);
		if (value == null) {
			return "";
		}
		return value.trim();
	}
}
