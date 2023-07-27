package com.xavor.plmxl.assist.Handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class ActHandler {
	
	 static AssistLogger log=AssistLogger.getInstance();
	
	public static JSONObject processActivation(String accessType) throws FileNotFoundException, Exception 
	{
		log.debug("Entering processActivation..");
		String url;














					//dbh.handleDBRequest("closeDB", null, false);
				

	}

	public static JSONObject getActInfo() throws Exception {
		log.debug("Entering getActInfo..");
			DBHandler dbh = new DBHandler();
			HashMap licMap=dbh.handleDBRequest("getLicInfo", null, false);
			String licinfo = (String) licMap.get("strLic");
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				licinfo = "-|Unregistered|None|1970-01-01|0";

			licinfo = licinfo.replace('|', ';');
			String[] licdetails = licinfo.split(";");

			String isValidLic = "Valid";

			if (new Date().after((convertStrToDate(licdetails[3]))))
			{
				isValidLic = "Invalid";
				
			}
			JSONObject responseJson = new JSONObject();
			responseJson.put("Activating Server : ", licdetails[0]);
			responseJson.put("Company Name : ", licdetails[1]);
		
			responseJson.put("Valid Until : ", licdetails[3]);
			responseJson.put("License Status : ", isValidLic);
			
			HashMap<String, String> configMap = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
			if(!configMap.containsKey("accessType"))
			{
				// keeping role access type as default, user however can update it
				responseJson.put("accessType", "roles");
				responseJson.put("isFirstTime","true");
			}
			else
			{
				responseJson.put("accessType",configMap.get("accessType"));
				Constants.Config.ACCESSTYPEROLE=configMap.get("accessType");
			}
			
			//dbh.handleDBRequest("closeDB", null, false);
			log.debug("Exiting getActInfo..");

			return responseJson;
	}
	
	public static boolean isLicValid() throws Exception
	{
		log.debug("Entering isLicValid..");
		boolean result=false;
		DBHandler dbh = new DBHandler();
		try {
			HashMap licMap=dbh.handleDBRequest("getLicInfo", null, false);//getLicInfo();
			String licinfo = (String) licMap.get("strLic");
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				result= false;

			licinfo = licinfo.replace('|', ';');
			String[] licdetails = licinfo.split(";");

			if (new Date().after((convertStrToDate(licdetails[3]))))
				result= false;
			log.debug("Exiting isLicValid..");

			result= true;
		} catch (Exception e) {
			log.error("Exception in isLicValid", e);
			log.debug("Exiting isLicValid..");

			result= false;
		}
		finally
		{
			//dbh.handleDBRequest("closeDB", null, false);
		}
		return result;
	}

	private static String readLicFileAsString() throws IOException,
			FileNotFoundException, Exception {
		log.debug("Entering readLicFileAsString..");

		String filePath = ConfigHelper.getAppHomePath()
				+ "PLMFlexAssistPlus.lic";
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		log.debug("Exiting readLicFileAsString..");

		return new String(buffer);
	}

	private static final char[] PASSWORD = "ahsanfazalmahwishsajidsobiausmanzubair"
			.toCharArray();
	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10,
			(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };

	private static String encrypt(String property) {
		try {
			log.debug("Entering encrypt..");

			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT,
					20));
			log.debug("Exiting encrypt..");

			return base64Encode(pbeCipher.doFinal(property.getBytes()));
		} catch (Exception e) {
		
			log.error("Exception in encrypt", e);
		}
		log.debug("Exiting encrypt..");

		return null;
	}

	private static String base64Encode(byte[] bytes) {
		return DatatypeConverter.printBase64Binary(bytes);
	}

	private static String decrypt(String property) {
		try {
			log.debug("Entering decrypt..");

			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT,
					20));
			log.debug("Exiting decrypt..");

			return new String(pbeCipher.doFinal(base64Decode(property)));
		} catch (Exception e) {
			log.error("Exception in decrypt", e);
		}
		log.debug("Exiting decrypt..");

		return null;
	}

	private static byte[] base64Decode(String property) {
		return DatatypeConverter.parseBase64Binary(property);
	}

	private static Date convertStrToDate(String dtStr) {
		log.debug("Entering convertStrToDate..");

		Date date = new Date(0);
		try {
			DateFormat formatter;
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			date = (Date) formatter.parse(dtStr);
		} catch (ParseException e) {
		
			log.error("ParseException in convertStrToDate", e);
		}
		log.debug("Exiting convertStrToDate..");
		return date;
	}
}