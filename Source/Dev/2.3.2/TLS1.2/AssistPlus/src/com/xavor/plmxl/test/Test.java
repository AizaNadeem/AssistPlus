package com.xavor.plmxl.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

import com.xavor.plmxl.assist.Handler.ActHandler;

public class Test {
	//	static AssistLogger log = AssistLogger.getInstance();
	private static byte[] base64Decode(String property) {
		return DatatypeConverter.parseBase64Binary(property);
	}
	private static final char[] PASSWORD = "ahsanfazalmahwishsajidsobiausmanzubair"
			.toCharArray();
	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10,
			(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };
	private static String base64Encode(byte[] bytes) {
		return DatatypeConverter.printBase64Binary(bytes);
	}
	public static void main(String[] args) throws ClassNotFoundException, SQLException {

		try {
//			SecretKeyFactory keyFactory = SecretKeyFactory
//					.getInstance("PBEWithMD5AndDES");
//			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
//			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
//			pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT,
//					20));
//
//			System.out.println(new String(pbeCipher.doFinal(base64Decode("8C5SXQrs+T+M/cBFDJodNhShP6ew+Oq5"))));
			String property="zadmin";
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT,
					20));

			System.out.println(base64Encode(pbeCipher.doFinal(property.getBytes())));
			
			String dbUser = "agile";
			String dbPass = "tartan";
			String driver = "oracle.jdbc.driver.OracleDriver";
//			String connstring = "jdbc:oracle:thin:@";
			String dbServer = "jdbc:oracle:thin:@(DESCRIPTION= (ADDRESS=(PROTOCOL=TCPS)(PORT=1523)(HOST=agile936.xavor.com))(CONNECT_DATA=(SERVICE_NAME=agile9))"
//					+")";
					+ "(SECURITY=(ssl_server_cert_dn=\"CN=agile936.xavor.com, OU=xavor,O=xavor\")))";
			Class.forName(driver);
			String url="jdbc:oracle:thin:@agile936.xavor.com:1523:agile9";
//			String url = connstring;
//			url += dbServer;
			Connection conn = DriverManager.getConnection(dbServer, dbUser, dbPass);
			System.out.println("conn created");
			
			//			AgileSessionFactory factoary= AgileSessionFactory.getInstance("http://ivnagile03.ad.skynet:7001/Agile");
			//
			//				HashMap<Integer, String> paramss = new HashMap<Integer, String>();
			//				paramss.put(AgileSessionFactory.USERNAME, "admin");
			//				paramss.put(AgileSessionFactory.PASSWORD, "qaagile123");
			//				IAgileSession sessi1on = factoary.createSession(paramss);
			//				System.out.println("hello");


		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
