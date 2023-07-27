package com.XACS.Assist.Handler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;

public class ActHandler {
	
	 static AssistLogger log=AssistLogger.getInstance();
	
	public static JSONObject processActivation(String accessType) throws FileNotFoundException, Exception {
		log.debug("Entering processActivation..");
		URL url;
		URLConnection urlConnection;
		DataOutputStream outStream;
		DataInputStream inStream;

			String cid = encrypt(readLicFileAsString());

			String decsid = InetAddress.getLocalHost().getHostName();
			String sid = encrypt(decsid);

			String body = "cid=" + URLEncoder.encode(cid, "UTF-8") + "&sid="
					+ URLEncoder.encode(sid, "UTF-8") + "&rnd="
					+ URLEncoder.encode(String.valueOf(Math.random()), "UTF-8");
			url = new URL("http://licensing.plmxl.com/FlexAct/activate");
			urlConnection = url.openConnection();
			((HttpURLConnection) urlConnection).setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setDefaultUseCaches(false);
			urlConnection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			urlConnection.setRequestProperty("Content-Length",
					"" + body.length());
			outStream = new DataOutputStream(urlConnection.getOutputStream());

			// Send request
			outStream.writeBytes(body);
			outStream.flush();

			inStream = new DataInputStream(urlConnection.getInputStream());
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					inStream));
			String responseStr = "";
			String line = null;
			while ((line = rd.readLine()) != null) {
				responseStr = responseStr + line;
			}

			// Close I/O streams
			inStream.close();
			outStream.close();

			JSONObject json = (JSONObject) new JSONParser()
					.parse(decrypt(responseStr));


			if (json.containsKey("responseCode")
					&& !json.get("responseCode").equals("200")) // NOT OK
				throw new Exception("Error while getting Activation information. Code:"
								+ json.get("responseCode") + "-"
								+ json.get("time"));
			else {
				String customerName = (String) json.get("cn");
				
				String noOfUser = "0";
				String moduleStr = "-";
				String validUntil = json.get("vutl").toString();

				boolean isExpired = Boolean.parseBoolean((String) json
						.get("licExp"));

				String LicStr = encrypt(decsid + "|" + customerName + "|"
						+ moduleStr + "|" + validUntil.toString() + "|"
						+ noOfUser);

				DBHandler dbh = new DBHandler();
				HashMap params=new HashMap<String,String>();
				params.put("licinfo", LicStr);
				dbh.handleDBRequest("updateLicinfo", params, true);

				String licenseStatus = "Valid";

				if (isExpired)
				{
					licenseStatus = "Invalid";
					log.info("Liscence Expired..");
				}

				JSONObject responseJson = new JSONObject();
	
				responseJson.put("Activating Server : ", decsid);
				responseJson.put("Company Name : ", customerName);
				responseJson.put("Valid Until : ", validUntil);
				if(!isExpired)
				{
					log.info("Liscence is "+licenseStatus +"..");
				}
				responseJson.put("License Status : ", licenseStatus);

					HashMap<String, String> configMap = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
					if(!configMap.containsKey("accessType"))
					{
						// keeping role access type as default, user however can update it
						HashMap configParams=new HashMap<String,String>();
						configParams.put("key","accessType");
						configParams.put("value",accessType);
						dbh.handleDBRequest("insertConfig", configParams, true);//.insertConfig(, accessType);
						
						responseJson.put("accessType", accessType);
						Constants.Config.ACCESSTYPEROLE=accessType;
						HashMap roleParams=new HashMap<String,String>();
						if("roles".equalsIgnoreCase(accessType))
						{
							roleParams.put("newRole","All Roles");
							dbh.handleDBRequest("updateDefaultRolePriority", roleParams, true);
							}
						else
						{
							roleParams.put("newRole","All User Groups");
							dbh.handleDBRequest("updateDefaultRolePriority", roleParams, true);
						}
					}
					else
					{
						responseJson.put("accessType",configMap.get("accessType"));
					}

				log.debug("Exiting processActivation..");
				return responseJson;
			}

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
			
			dbh.handleDBRequest("closeDB", null, false);
			log.debug("Exiting getActInfo..");

			return responseJson;
	}
	
	public static boolean isLicValid()
	{
		log.debug("Entering isLicValid..");
		try {
	
			DBHandler dbh = new DBHandler();
			HashMap licMap=dbh.handleDBRequest("getLicInfo", null, false);//getLicInfo();
			String licinfo = (String) licMap.get("strLic");
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				licinfo = "-|Unregistered|None|1970-01-01|0";

			licinfo = licinfo.replace('|', ';');
			String[] licdetails = licinfo.split(";");

			if (new Date().after((convertStrToDate(licdetails[3]))))
				return false;
			log.debug("Exiting isLicValid..");

			return true;
		} catch (Exception e) {
			log.error("Exception in isLicValid", e);
			log.debug("Exiting isLicValid..");

			return false;
		}
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
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Encoder().encode(bytes);
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

	private static byte[] base64Decode(String property) throws IOException {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Decoder().decodeBuffer(property);
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
