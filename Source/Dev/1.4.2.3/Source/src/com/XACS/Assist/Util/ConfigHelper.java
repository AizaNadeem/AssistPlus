package com.XACS.Assist.Util;

import java.util.HashMap;
import com.XACS.Assist.Handler.DBHandler;

public class ConfigHelper {
	
	static HashMap<String, String> prop = null; 
	
	/*public static Properties loadProperties(ServletContext context) {
		Properties prop = new Properties();
		try {
			String Path = context.getRealPath("/WEB-INF/classes/prop.properties");
			InputStream inputStream = new FileInputStream(Path);
			prop.load(inputStream);
		} catch (IOException ex) {
		}
		return prop;
	}*/
	
	public static String getAppHomePath() throws Exception
	{
		String homePath = System.getenv(Constants.Config.HOME_ENVVAR);// "PLMFLEX_HOME"
		if ((homePath == null) || homePath.equals("")) {
			System.out.println("Home variable: "+Constants.Config.HOME_ENVVAR+" not found. Using User's Home instead.");
			homePath = System.getProperty(Constants.Config.USERHOME_PROP);// "user.home"
			if ((homePath == null) || homePath.equals("")) {
				throw new Exception("PLMFLEX_HOME: environment not configured");
			}
		}
		String fileSep = System.getProperty("file.separator");
		if (!homePath.endsWith(fileSep)) {
			homePath = homePath + fileSep;
		}
		System.out.println("Using home path: "+ homePath);
		return homePath + "AssistPlus" + fileSep;
	}
	
	
	public static void loadConfigurations() throws Exception
	{
		DBHandler dbh = new DBHandler();
        prop = dbh.readConfigurations();
	}
	
	public static String getProperty(String key) throws Exception
	{
		if (prop == null)
			loadConfigurations();
		return prop.get(key);
	}
}
