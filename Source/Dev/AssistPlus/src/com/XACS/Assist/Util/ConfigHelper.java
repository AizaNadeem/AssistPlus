package com.XACS.Assist.Util;

import java.util.HashMap;

import com.XACS.Assist.Handler.DBHandler;

public class ConfigHelper {
	
	static HashMap<String, String> prop = null; 
	 static AssistLogger log=AssistLogger.getInstance();
		
	public static String getAppHomePath() throws Exception
	{
		log.debug("Entering getAppHomePath..");
		String homePath = System.getenv(Constants.Config.HOME_ENVVAR);// "PLMFLEX_HOME"
		if ((homePath == null) || homePath.equals("")) {
			log.info("Home variable: "+Constants.Config.HOME_ENVVAR+" not found. Using User's Home instead.");
			homePath = System.getProperty(Constants.Config.USERHOME_PROP);// "user.home"
			if ((homePath == null) || homePath.equals("")) {
				throw new Exception("PLMFLEX_HOME: environment not configured");
			}
		}
		String fileSep = System.getProperty("file.separator");
		if (!homePath.endsWith(fileSep)) {
			homePath = homePath + fileSep;
		}
		log.info("Using home path: "+ homePath);
		log.debug("Exiting getAppHomePath..");
		return homePath + "AssistPlus" + fileSep;
	}
	
	
	public static void loadConfigurations() throws Exception
	{
		log.debug("Entering loadConfigurations..");
		DBHandler dbh = new DBHandler();
        prop = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
        log.debug("Exiting loadConfigurations..");
	}
	
	public static void loadConfigurations( DBHandler db) throws Exception
	{
		log.debug("Entering loadConfigurations..");
        prop = (HashMap<String, String>) db.handleDBRequest("readConfigurations", null, false);
        log.debug("Exiting loadConfigurations..");
	}
	
	public static String getProperty(String key, DBHandler db) throws Exception
	{
		if (prop == null)
			loadConfigurations(db);
		return prop.get(key);
	}
	
	public static String getProperty(String key) throws Exception
	{
		if (prop == null)
			loadConfigurations();
		return prop.get(key);
	}
}
