package com.xavor.plmxl.assist.Util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Properties;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ConfigEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;

public class ConfigHelper {
	
	static HashMap<String, String> prop = null; 
	 static AssistLogger log=AssistLogger.getInstance();
		
	public static String getAppHomePath() throws Exception
	{

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
	
		return homePath;
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
	public static HashMap<String, ConfigEntry> getConfigEntries() throws Exception {
		log.debug("Entering getConfigEntries...");

		HashMap<String, ConfigEntry> configArr = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, String> configMap = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
			ConfigEntry ce = null;
			configArr = new HashMap<String, ConfigEntry>();

			for (String key : configMap.keySet()) 
			{
				if (!key.equalsIgnoreCase("LNFO")) 
				{
					ce = new ConfigEntry();
					ce.setKey(key);
					ce.setValue(configMap.get(key));
					ce.setId(key.replaceAll(" ", ""));
					if (key.equalsIgnoreCase("AgilePassword")) 
					{
						ce.setType("password");
					} else 
					{
						ce.setType("text");
					}
					configArr.put(ce.getKey(), ce);
				}
			}

			log.debug("Exiting getConfigEntries...");

			return configArr;
		} catch (Exception e) 
		{

			log.error("Exception in getConfigEntries", e);

			throw e;
		}
	}
	public static String configureAccessType(DBHandler dbh)
	{
		if(Constants.Config.ACCESSTYPEROLE==null)
		{
			HashMap params=new HashMap<String,String>();
			params.put("key", "accessType");
			HashMap map=dbh.handleDBRequest("getConfigByKey", params, false);//.getConfigByKey("accessType");
			Constants.Config.ACCESSTYPEROLE=(String) map.get("value");
		}
		String allRoleKey=Constants.General.ALLROLEKEY;
		if(!"roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
		{
			allRoleKey=Constants.General.ALLUSERGROUPSKEY;
		}
		return allRoleKey;
		
	}
	public static Properties loadPropertyFile()
	{
		String homePath;
		Properties prop = new Properties();
		try {
			homePath = getAppHomePath();

			prop.load(new FileInputStream(homePath+Constants.General.PropertyFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return prop;
	}
	public static HashMap<String, String> getDbProperties()
	{
		log.debug("Entering getDbProperties..");
		HashMap<String, String> properties=new HashMap<String, String>();
		Properties property=loadPropertyFile();
		properties.put("driver", property.getProperty("driver"));
		properties.put("connstring", property.getProperty("connstring") );
		log.debug("Exiting getDbProperties..");
		return properties;
	}
	
	public static String getDataFromServer(String serverURL,String body)
	{
		log.debug("Entering getDataFromServer..");
		String responseStr = "";		
		try 
		{
			URL url = new URL(serverURL);
			DataOutputStream outStream;
			URLConnection conn = url.openConnection();
			((HttpURLConnection) conn).setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			if(body!=null)
			{	
				
				conn.setRequestProperty("Content-Length",
						"" + body.length());
				outStream = new DataOutputStream(conn.getOutputStream());
				log.debug(body.toString());
				outStream.writeBytes(body);
				outStream.flush();
			}

			// Send request
			
			
			BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
			
			String line = null;
			while ((line = br.readLine()) != null) {
				responseStr = responseStr + line;
			}
			
			
		} catch (MalformedURLException e) 
		{
			log.error("Could not connect to "+serverURL+", please verify the URL." , e);
			
			responseStr = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+serverURL+", please verify the URL."));
		} catch (IOException e) {
			log.error("Could not connect to "+serverURL+", please verify the URL." , e);
			
			responseStr = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+serverURL+", please verify the URL."));
		}
		catch (Exception e) {
			log.error("Could not connect to "+serverURL+", please verify the URL." , e);
			
			responseStr = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+serverURL+", please verify the URL."));
		}
		log.debug("Exiting getDataFromServer..");

		return responseStr;
	}

}
