package com.xavor.plmxl.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.agile.api.AgileSessionFactory;
import com.agile.api.IAgileSession;
import com.xavor.plmxl.assist.Handler.*;

import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class Test {
//	static AssistLogger log = AssistLogger.getInstance();

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		try {
			
			
			HashMap<String, String> params=new HashMap<String, String>();
			params.put("path", "C:\\Users\\mabbasi\\OneDrive - Xavor Corporation\\AssistPlus\\Tickets\\onsemi\\sbx.xml");
			XMLHandler xml=new XMLHandler();
			HashMap statusMap = xml.importXML(params);
			AgileSessionFactory factoary= AgileSessionFactory.getInstance("http://ivnagile03.ad.skynet:7001/Agile");

				HashMap<Integer, String> paramss = new HashMap<Integer, String>();
				paramss.put(AgileSessionFactory.USERNAME, "admin");
				paramss.put(AgileSessionFactory.PASSWORD, "qaagile123");
				IAgileSession sessi1on = factoary.createSession(paramss);
				System.out.println("hello");

			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
