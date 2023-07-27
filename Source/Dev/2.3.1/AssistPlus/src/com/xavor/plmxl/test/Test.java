package com.xavor.plmxl.test;

import com.xavor.plmxl.assist.Util.AssistLogger;

public class Test {
	static AssistLogger log = AssistLogger.getInstance();

	public static void main(String[] args) {

		try {
			log.debug("hello my dear");

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
