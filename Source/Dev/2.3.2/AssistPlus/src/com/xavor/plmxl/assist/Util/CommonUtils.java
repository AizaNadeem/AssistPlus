package com.xavor.plmxl.assist.Util;

public class CommonUtils {
	
	public static boolean isNull(String s) {
		if ((s == null) || (s.equals("")) || (s.trim().length() == 0) || s.trim().equalsIgnoreCase("null")) {
			return true;
		}
		return false;
	}

}
