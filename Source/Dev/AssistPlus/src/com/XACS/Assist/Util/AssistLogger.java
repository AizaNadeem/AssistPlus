package com.XACS.Assist.Util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class AssistLogger 
{
	public static  AssistLogger log=null;
	private final String fullyQualifiedClassName=AssistLogger.class.getName();
	static Logger logger=null;
	
	private AssistLogger()
	{
	    String path=System.getenv(Constants.Config.HOME_ENVVAR);
		System.setProperty("rootPath",path+Constants.Config.LoggerProperty);
		PropertyConfigurator.configure(path+Constants.Config.PropertyConfig);
		logger=Logger.getLogger("com.XACS.Assist");
	}
	public  static AssistLogger getInstance()
	{
		if(log==null)
		{
			log=new AssistLogger();
		}
		else
		{
			logger=Logger.getLogger("com.XACS.Assist");
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
