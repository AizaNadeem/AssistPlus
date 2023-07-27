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
import java.util.Map;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ConfigEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;

public class ConfigHelper {
	
	static HashMap<String, String> prop = null; 
	static AssistLogger log = AssistLogger.getInstance();
		
	public static String getAppHomePath() throws Exception {
		String homePath = System.getenv(Constants.Config.HOME_ENVVAR);
		if ((homePath == null) || homePath.isEmpty()) {
//			System.out.println("Home variable: " + Constants.Config.HOME_ENVVAR + " not found. Using User's Home instead.");
//			homePath = System.getProperty(Constants.Config.USERHOME_PROP); //"user.home"
			if ((homePath == null) || homePath.isEmpty()) {
				throw new Exception("PLMFLEX_HOME: environment variable not configured prperly. Please configure and restart server for changes to take effect.");
			}
		}
		String fileSep = System.getProperty("file.separator");
		if (!homePath.endsWith(fileSep)) {
			homePath = homePath + fileSep;			
		}		
	
		return homePath;
	}
		
	public static void loadConfigurations() throws Exception {
		DBHandler dbh = new DBHandler();
		loadConfigurations(dbh);
	}
	
	@SuppressWarnings("unchecked")
	public static void loadConfigurations( DBHandler db) throws Exception {
		log.debug("Entering loadConfigurations..");
        prop = (HashMap<String, String>) db.handleDBRequest("readConfigurations", null, false);
        log.debug("Exiting loadConfigurations..");
	}
	
	public static String getProperty(String key, DBHandler db) throws Exception	{
		if (prop == null) {
			loadConfigurations(db);
		}
		return prop.get(key);
	}
	
	public static String getProperty(String key) throws Exception {
		if (prop == null) {
			loadConfigurations();
		}
		return prop.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, ConfigEntry> getConfigEntries() throws Exception {
		log.debug("Entering getConfigEntries...");
		HashMap<String, ConfigEntry> configArr = null;
		DBHandler dbh = new DBHandler();
		HashMap<String, String> configMap = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
		ConfigEntry ce = null;
		configArr = new HashMap<String, ConfigEntry>();

		for (String key : configMap.keySet()) {
			if (!key.equalsIgnoreCase("LNFO")) {
				String val = configMap.get(key);
				if(val.equals("NULL_VALUE")) {
					val = "";
				}
				ce = new ConfigEntry();
				ce.setKey(key);
				ce.setValue(val);
				ce.setId(key.replaceAll(" ", ""));
				if (key.equalsIgnoreCase("AgilePassword")) {
					ce.setType("password");
				} else {
					ce.setType("text");
				}
				configArr.put(ce.getKey(), ce);
			}
		}

		/**
		 * If updated from Assist+ version < 2.2 then add
		 * new key value pair in CONFIGURATIONS table for
		 * enableOptOut
		 * */  
		if(!configArr.containsKey("enableOptOut")) {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("enableOptOut", "No");
			dbh.handleDBRequest("addConfigurations", params, true);
			loadConfigurations();
			
			ce = new ConfigEntry();
			ce.setKey("enableOptOut");
			ce.setValue("No");
			ce.setId("enableOptOut");
			ce.setType("text");
			configArr.put(ce.getKey(), ce);
		}
		
		log.debug("Exiting getConfigEntries...");
		return configArr;
	}
	
	@SuppressWarnings("unchecked")
	public static String configureAccessType(DBHandler dbh) {
		if(Constants.Config.ACCESSTYPEROLE == null) {
			HashMap<String, String> params = new HashMap<String,String>();
			params.put("key", "accessType");
			HashMap<String, String> map = (HashMap<String, String>) dbh.handleDBRequest("getConfigByKey", params, false);
			Constants.Config.ACCESSTYPEROLE = (String) map.get("value");
		}
		
		String allRoleKey=Constants.General.ALLROLEKEY;
		if(!"roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE)) {
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
			e.printStackTrace();
		}
		return prop;
	}
	
	public static Map<String, String> getDbProperties() throws Exception	{
		log.debug("Entering getDbProperties...");
		Properties property = loadPropertyFile();
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("driver", property.getProperty("driver"));
		properties.put("connstring", property.getProperty("connstring") );
		properties.put("dbUser", property.getProperty("dbUser"));
		properties.put("dbPwd", decrypt(property.getProperty("dbPwd")));
		properties.put("dbServer", property.getProperty("dbServer"));
		
		log.debug("Exiting getDbProperties..");
		return properties;
	}
	
	private static final char[] PASSWORD = "cf7821a22d3ad0cf30666ffd52fa83aef3d3f0801fb72c212a9636b0".toCharArray();
	private static final byte[] SALT = {(byte) 0xde, (byte) 0x33, (byte) 0x10,	(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };
	
	public static String decrypt(String property) throws Exception {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(DatatypeConverter.parseBase64Binary(property)));
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
			
			
		}catch (Exception e) {
			log.error("Could not connect to "+serverURL+", please verify the URL." , e);
			
			responseStr = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+serverURL+". Please verify URL is accessible on internet."));
		}
		log.debug("Exiting getDataFromServer..");

		return responseStr;
	}

}
