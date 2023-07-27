package com.xavor.plmxl.assist.config;

import java.io.Console;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

public class AssistPlusConfigurations
{
    private static final char[] PASSWORD;
    private static final byte[] SALT;
    
    static {
        PASSWORD = "cf7821a22d3ad0cf30666ffd52fa83aef3d3f0801fb72c212a9636b0".toCharArray();
        SALT = new byte[] { -34, 51, 16, 18, -34, 51, 16, 18 };
    }
    
    public static void main(final String[] args) {
        Console console = null;
        try {
            console = System.console();
            int tries = 0;
            String db;
            for (db = console.readLine("Select Assist+ DB, Enter 1 for Oracle DB, 2 for TLS 1.2 enabled Oracle DB: ", new Object[0]); !"1".equals(db) && !"2".equals(db); db = console.readLine("Select Assist+ DB. Enter 1 for Oracle DB, 2 for TLS 1.2 enabled Oracle DB: ", new Object[0])) {
                if (++tries > 2) {
                    System.out.println("Invalid option selected. Exiting the application.");
                    return;
                }
                System.out.println("Invalid option selected. Try again.");
            }
            tries = 0;
            final Properties props = new Properties();
            props.setProperty("version", "3.0");
            if ("2".equals(db)) {
                final String dbServer = console.readLine("Enter DB Server TNS Alias: ", new Object[0]);
                final String username = console.readLine("Enter DB User Name: ", new Object[0]);
                char[] pass_array = console.readPassword("Enter DB Password: ", new Object[0]);
                final String trustStore = console.readLine("Enter trustore.jks location: ", new Object[0]);
                char[] trustStorePassword_arr = console.readPassword("Enter trustore password: ", new Object[0]);
                final String tnsAdminLocation = console.readLine("Enter tnsnames.ora location: ", new Object[0]);
                String password = new String(pass_array);
                String trustStorePassword = new String(trustStorePassword_arr);
                while (password == null || password.trim().isEmpty()) {
                    if (++tries > 2) {
                        System.out.println("Invalid Password. Exiting the application.");
                        return;
                    }
                    System.out.println("Invalid Password. Try again.");
                    pass_array = console.readPassword("Enter DB Password: ", new Object[0]);
                    password = new String(pass_array);
                }
                while (trustStorePassword == null || trustStorePassword.trim().isEmpty()) {
                    if (++tries > 3) {
                        System.out.println("Invalid Password. Exiting the application.");
                        return;
                    }
                    System.out.println("Invalid Password. Try again.");
                    trustStorePassword_arr = console.readPassword("Enter trustore password:  ", new Object[0]);
                    trustStorePassword = new String(trustStorePassword_arr);
                }
                props.setProperty("trustStore", trustStore);
                props.setProperty("trustStorePassword", encrypt(trustStorePassword));
                props.setProperty("tnsAdmin", tnsAdminLocation);
                props.setProperty("trustStoreType", "jks");
                props.setProperty("is.TLS.1.2.connection", "true");
                props.setProperty("ssl_server_dn_match", "true");
                props.setProperty("driver", "oracle.jdbc.driver.OracleDriver");
                props.setProperty("connstring", "jdbc:oracle:thin:@");
                props.setProperty("dbServer", dbServer);
                props.setProperty("dbUser", username);
                props.setProperty("dbPwd", encrypt(password));
                final FileOutputStream output = new FileOutputStream("AssistPlus.properties");
                props.store(output, null);
                output.close();
                System.out.println("Oracle DB has been configured successfully.");
            }
            else {
                final String dbServer = console.readLine("Enter DB Server <HOST>:<PORT>:<SID> : ", new Object[0]);
                final String username = console.readLine("Enter DB User Name: ", new Object[0]);
                char[] pass_array;
                String password2;
                for (pass_array = console.readPassword("Enter DB Password: ", new Object[0]), password2 = new String(pass_array); password2 == null || password2.trim().isEmpty(); password2 = new String(pass_array)) {
                    if (++tries > 2) {
                        System.out.println("Invalid Password. Exiting the application.");
                        return;
                    }
                    System.out.println("Invalid Password. Try again.");
                    pass_array = console.readPassword("Enter DB Password: ", new Object[0]);
                }
                props.setProperty("driver", "oracle.jdbc.driver.OracleDriver");
                props.setProperty("connstring", "jdbc:oracle:thin:@");
                props.setProperty("is.TLS.1.2.connection", "false");
                props.setProperty("dbServer", dbServer);
                props.setProperty("dbUser", username);
                props.setProperty("dbPwd", encrypt(password2));
                final FileOutputStream output2 = new FileOutputStream("AssistPlus.properties");
                props.store(output2, null);
                output2.close();
                System.out.println("Oracle DB has been configured successfully.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static String encrypt(final String property) throws Exception {
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        final SecretKey key = keyFactory.generateSecret(new PBEKeySpec(AssistPlusConfigurations.PASSWORD));
        final Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(1, key, new PBEParameterSpec(AssistPlusConfigurations.SALT, 20));
        return DatatypeConverter.printBase64Binary(pbeCipher.doFinal(property.getBytes()));
    }
}