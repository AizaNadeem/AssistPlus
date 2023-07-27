package com.xavor.plmxl.test;

import com.xavor.plmxl.assist.Handler.ActHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants.Config;

public class Test {
	static AssistLogger log = AssistLogger.getInstance();

	public static void main(String[] args) {

		try {
			ActHandler handler=new ActHandler();
			handler.processActivation("roles");
			

		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
