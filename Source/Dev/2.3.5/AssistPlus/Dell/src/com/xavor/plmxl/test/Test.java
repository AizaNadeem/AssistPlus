package com.xavor.plmxl.test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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
			
			
			AssistLogger logger = AssistLogger.getInstance();
//			Properties prop=new Properties();
//	    	String path = "C:\\Users\\mabbasi\\OneDrive - Xavor Corporation\\AssistPlus\\Assist+ v2.3.0.3\\";
//	    	System.setProperty("rootPath", path);
//	    	FileInputStream fis = new FileInputStream(path + Constants.Config.PropertyConfig);
//	    	prop.load(fis);
//	    	fis.close();
//	    	PropertyConfigurator.configure(prop);
//	    	Logger logger = Logger.getLogger(Constants.Config.PACKAGE_NAME);
//	    	logger.setLevel(Level.DEBUG);
//			logger.log(AssistLogger.class.getName(), Level.DEBUG, "helloworld", null);
			logger.debug( "hellowokjkjrld");
			logger.error( "hERRORellowokjkjrld");
			logger.info( "heINFOllowokjkjrld");


//			
//			HashMap<String, String> params=new HashMap<String, String>();
//			params.put("path", "C:\\Users\\mabbasi\\OneDrive - Xavor Corporation\\AssistPlus\\Tickets\\onsemi\\sbx.xml");
//			XMLHandler xml=new XMLHandler();
//			HashMap statusMap = xml.importXML(params);
//			AgileSessionFactory factoary= AgileSessionFactory.getInstance("http://ivnagile03.ad.skynet:7001/Agile");
//
//				HashMap<Integer, String> paramss = new HashMap<Integer, String>();
//				paramss.put(AgileSessionFactory.USERNAME, "admin");
//				paramss.put(AgileSessionFactory.PASSWORD, "qaagile123");
//				IAgileSession sessi1on = factoary.createSession(paramss);
//				System.out.println("hello");

			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
