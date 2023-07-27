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
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.XACS.Assist.Util.ConfigHelper;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ActHandler {

	public ActHandler() {

	}

	public static JSONObject processActivation() throws FileNotFoundException, Exception {
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
			url = new URL("http://licensing.plmflex.com/FlexAct/activate");
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

//			String respjsonstr = "";
			if (json.containsKey("responseCode")
					&& !json.get("responseCode").equals("200")) // NOT OK
				throw new Exception("Error while getting Activation information. Code:"
								+ json.get("responseCode") + "-"
								+ json.get("time"));
			else {
				String customerName = (String) json.get("cn");
				String noOfUser = "0";
				//(String) json.get("ulic");
				String moduleStr = "-";
				String validUntil = json.get("vutl").toString();

				boolean isExpired = Boolean.parseBoolean((String) json
						.get("licExp"));

				String LicStr = encrypt(decsid + "|" + customerName + "|"
						+ moduleStr + "|" + validUntil.toString() + "|"
						+ noOfUser);

				DBHandler dbh = new DBHandler();
				dbh.updateLicinfo(LicStr);

				String licenseStatus = "Valid";

				if (isExpired)// Integer.parseInt(noOfUser)<1 ||new
								// Date().after((Utils.convertStrToDate(validUntil))))
					licenseStatus = "Invalid";

				JSONObject responseJson = new JSONObject();
				// responseJson.put("Activation Code", deccid);
				responseJson.put("Activating Server", decsid);
				responseJson.put("Company Name", customerName);
				// responseJson.put("Modules", moduleStr);
				// responseJson.put("No. Of Devices/Users", noOfUser);
				responseJson.put("Valid Until", validUntil);
				responseJson.put("License Status", licenseStatus);
				
				return responseJson;
			}

	}

	public static JSONObject getActInfo() throws Exception {
			DBHandler dbh = new DBHandler();
			String licinfo = dbh.getLicInfo();
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				licinfo = "-|Unregistered|None|1970-01-01|0";

			licinfo = licinfo.replace('|', ';');
			String[] licdetails = licinfo.split(";");

			String isValidLic = "Valid";

			if (new Date().after((convertStrToDate(licdetails[3]))))
				isValidLic = "Invalid";

			JSONObject responseJson = new JSONObject();
			responseJson.put("Activating Server", licdetails[0]);
			responseJson.put("Company Name", licdetails[1]);
			// responseJson.put("Modules", licdetails[2]);
			// responseJson.put("No. Of Devices/Users", noOfUser);
			responseJson.put("Valid Until", licdetails[3]);
			responseJson.put("License Status", isValidLic);
			
			dbh.closeDB();
			
			return responseJson;
	}
	
	public static boolean isLicValid()
	{
		try {
			//String licinfo = ConfigHelper.getProperty("LNFO");
			DBHandler dbh = new DBHandler();
			String licinfo = dbh.getLicInfo();
			if (licinfo != null)
				licinfo = decrypt(licinfo);
			else
				licinfo = "-|Unregistered|None|1970-01-01|0";

			licinfo = licinfo.replace('|', ';');
			String[] licdetails = licinfo.split(";");

			if (new Date().after((convertStrToDate(licdetails[3]))))
				return false;

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static String readLicFileAsString() throws IOException,
			FileNotFoundException, Exception {
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
		return new String(buffer);
	}

	private static final char[] PASSWORD = "ahsanfazalmahwishsajidsobiausmanzubair"
			.toCharArray();
	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10,
			(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };

	private static String encrypt(String property) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT,
					20));
			return base64Encode(pbeCipher.doFinal(property.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String base64Encode(byte[] bytes) {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Encoder().encode(bytes);
	}

	private static String decrypt(String property) {
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT,
					20));
			return new String(pbeCipher.doFinal(base64Decode(property)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] base64Decode(String property) throws IOException {
		// NB: This class is internal, and you probably should use another impl
		return new BASE64Decoder().decodeBuffer(property);
	}

	private static Date convertStrToDate(String dtStr) {
		Date date = new Date(0);
		try {
			DateFormat formatter;
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			date = (Date) formatter.parse(dtStr);
		} catch (ParseException e) {
			e.printStackTrace();
			// System.out.println("Exception :"+e);
		}
		return date;
	}
}
