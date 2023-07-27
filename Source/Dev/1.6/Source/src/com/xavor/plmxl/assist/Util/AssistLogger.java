package com.xavor.plmxl.assist.Util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class AssistLogger 
{
	private static  AssistLogger log=null;
	private final String fullyQualifiedClassName=AssistLogger.class.getName();
	private Logger logger=null;
	
	private AssistLogger()
	{
	     try
	    {
	    	String path = ConfigHelper.getAppHomePath();
	    	System.setProperty("rootPath",path);
	    	PropertyConfigurator.configure(path+Constants.Config.PropertyConfig);
	    	logger=Logger.getLogger(Constants.Config.PACKAGE_NAME);
	    }
	    catch (Exception e)
	    {
	    	System.out.println("Error while instantiating AssistLogger: " + e.getMessage());
	    }
	}
	public  static AssistLogger getInstance()
	{
		if(log==null)
		{
			log=new AssistLogger();
		}
		return log;
	}
	public void debug(String message)
	{
		logger.setLevel(Level.DEBUG);
		logger.log(fullyQualifiedClassName, Level.DEBUG, message, null);
	}

	public void info(String message)
	{
		logger.setLevel(Level.INFO);
		logger.log(fullyQualifiedClassName, Level.INFO, message, null);
	}

	public void error(String message,Exception e)
	{
		logger.setLevel(Level.ERROR);
		logger.log(fullyQualifiedClassName, Level.ERROR, message+e.getMessage(), e);
		
		logger.setLevel(Level.INFO);
		logger.log(fullyQualifiedClassName, Level.INFO, message+e.getMessage(), null);
	}
}
