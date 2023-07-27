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
import java.util.Map;

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
	
	private static AssistLogger log = AssistLogger.getInstance();
	private static String url = "http://licensing.xavor.com/FlexAct/activate";
	
	public static JSONObject processActivation(String accessType) throws FileNotFoundException, Exception 
	{
		log.debug("Entering processActivation..");
			String cid = encrypt(readLicFileAsString());
			String decsid = InetAddress.getLocalHost().getHostName();			String sid = encrypt(decsid);
			String body = "cid=" + URLEncoder.encode(cid, "UTF-8") + "&sid="
					+ URLEncoder.encode(sid, "UTF-8") + "&rnd="
					+ URLEncoder.encode(String.valueOf(Math.random()), "UTF-8");
			String responseStr=ConfigHelper.getDataFromServer(url, body);
			if(responseStr.contains("error")) {
				JSONObject json = (JSONObject) new JSONParser().parse(responseStr);
				throw new Exception(json.get("message")+"");
			}			JSONObject json = (JSONObject) new JSONParser().parse(decrypt(responseStr));
			if (json.containsKey("responseCode") && !json.get("responseCode").equals("200")) // NOT OK
			{
//				"Unable to activate License. Please contact Xavor."
				throw new Exception("Error while getting Activation information. Please contact Xavor for futher assistance");
			}
			else {
								String customerName = (String) json.get("cn");								String noOfUser = "0";				String moduleStr = "-";				String validUntil = json.get("vutl").toString();
				boolean isExpired = Boolean.parseBoolean((String) json						.get("licExp"));
				String LicStr = encrypt(decsid + "|" + customerName + "|"						+ moduleStr + "|" + validUntil + "|"						+ noOfUser);
				DBHandler dbh = new DBHandler();				HashMap<String,String> params=new HashMap<>();				params.put("licinfo", LicStr);				dbh.handleDBRequest("updateLicinfo", params, true);
				String licenseStatus = "Valid";
				if (isExpired)				{					licenseStatus = "Invalid";					log.info("Liscence Expired..");				}
				JSONObject responseJson = new JSONObject();					responseJson.put("Activating Server : ", decsid);				responseJson.put("Company Name : ", customerName);				responseJson.put("Valid Until : ", validUntil);				if(!isExpired)				{					log.info("Liscence is "+licenseStatus +"..");				}				responseJson.put("License Status : ", licenseStatus);
					HashMap<String, String> configMap = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);					if(!configMap.containsKey("accessType"))
					{						// keeping role access type as default, user however can update it						HashMap<String,String> configParams=new HashMap<>();						configParams.put("key","accessType");
						configParams.put("value",accessType);						dbh.handleDBRequest("insertConfig", configParams, true);//.insertConfig(, accessType);												responseJson.put("accessType", accessType);
						Constants.Config.ACCESSTYPEROLE=accessType;						HashMap roleParams=new HashMap<String,String>();
						if("roles".equalsIgnoreCase(accessType))						{							roleParams.put("newRole",Constants.General.ALLROLEKEY);							dbh.handleDBRequest("updateDefaultRolePriority", roleParams, true);							}						else						{							roleParams.put("newRole",Constants.General.ALLUSERGROUPSKEY);							dbh.handleDBRequest("updateDefaultRolePriority", roleParams, true);						}					}					else					{						responseJson.put("accessType",configMap.get("accessType"));
					}

					//dbh.handleDBRequest("closeDB", null, false);					log.debug("Exiting processActivation..");
									return responseJson;			}

	}

	public static JSONObject getActInfo() throws Exception {
		log.debug("Entering getActInfo..");
			DBHandler dbh = new DBHandler();
			Map<?, ?> licMap = dbh.handleDBRequest("getLicInfo", null, false);
			String licinfo = (String) licMap.get("strLic");
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				licinfo = "-|Unregistered|None|1970-01-01|0";

			String[] licdetails = {};
			if(licinfo != null) {
				licinfo = licinfo.replace('|', ';');
				licdetails = licinfo.split(";");
			}

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
			Map<?, ?> licMap = dbh.handleDBRequest("getLicInfo", null, false);//getLicInfo();
			String licinfo = (String) licMap.get("strLic");
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				result= false;

			licinfo = (licinfo+"").replace('|', ';');
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
					log.error("Exception in readLicFileAsString:", ignored);
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
			date = formatter.parse(dtStr);
		} catch (ParseException e) {
		
			log.error("ParseException in convertStrToDate", e);
		}
		log.debug("Exiting convertStrToDate..");
		return date;
	}
}
