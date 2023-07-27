package com.xavor.plmxl.assist.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AssistLogger {
	private static AssistLogger log = null;
	private final String fullyQualifiedClassName = AssistLogger.class.getName();
	private Logger logger = null;

	private AssistLogger() {
		FileInputStream fis = null;
		try {
			Properties prop=new Properties();
			String homePath=ConfigHelper.getAppHomePath();
	    	System.setProperty("rootPath", homePath);
	    	fis = new FileInputStream(homePath + Constants.Config.PropertyConfig);
	    	prop.load(fis);
	    	fis.close();
	    	PropertyConfigurator.configure(prop);
	    	logger = Logger.getLogger(Constants.Config.PACKAGE_NAME);
	    } catch (Exception e) {
	    	System.out.println("Error while instantiating AssistLogger: " + e);
	    } finally {
	    	try {
	    		if(fis != null) {
	    			fis.close();
	    		}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}

	public static AssistLogger getInstance() {
		if(log == null) {
			log = new AssistLogger();
		}
		return log;
	}

	public void debug(String message) {
		if(logger != null) {
			logger.setLevel(Level.DEBUG);
			logger.log(fullyQualifiedClassName, Level.DEBUG, message, null);
		}

	}

	public void info(String message) {
		if(logger != null) {
			logger.setLevel(Level.INFO);
			logger.log(fullyQualifiedClassName, Level.INFO, message, null);

		}
	}

	public void error(String message) {
		if(logger != null) {
			logger.setLevel(Level.ERROR);
			logger.log(fullyQualifiedClassName, Level.ERROR, message, null);
		}
	}

	public void error(String message, Throwable e) {
		if(logger != null) {
			String errorMsg = (e != null)? "" + e.getMessage() : "Please check logs for futher details.";

			logger.setLevel(Level.ERROR);
			logger.log(fullyQualifiedClassName, Level.ERROR, message+errorMsg, e);

			logger.setLevel(Level.INFO);
			logger.log(fullyQualifiedClassName, Level.INFO, message+errorMsg, null);
		}
	}
}
