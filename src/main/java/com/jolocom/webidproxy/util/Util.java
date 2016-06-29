package com.jolocom.webidproxy.util;

public class Util {

	public static boolean isAlphaNumeric(String string) {

		for (int i=0; i<string.length(); i++) {

			if ((! Character.isAlphabetic(string.charAt(i)) && (! Character.isDigit(string.charAt(i))))) return false;
		}

		return true;
	}
}
