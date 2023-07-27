package com.XACS.Assist.Util;

public final class Constants {
	public static class Config {
		public static String	HOME_ENVVAR		 = "PLMFLEX_HOME";
		public static String	USERHOME_PROP	 = "user.home";
		public static String	AgileServerURL	 = "AgileURL";
		public static String	AgileUserName	 = "AgileUser";
		public static String	AgilePassword	 = "AgilePassword";
		public static String	PrimaryInstURL 	 = "PrimaryInstanceURL";
		public static String	ConnectionString = "ConnString";
		public static String	LoggerProperty   = "/AssistPlus";
		public static String	PropertyConfig   = "/AssistPlus/PropertyConfig";
		public static String 	ACCESSTYPEROLE	 =null;
		public static String    ASSISTPLUSPATH	 ="/AssistPlus/";
		public static String    XSD_ROLE		 =	"/AssistPlus/XSDrole.xsd";		
		public static String    XSD_GROUP		 = "/AssistPlus/XSDgroup.xsd";
	}

	public static class General {
		public static String	TextFlagSet		= "yes";
		public static String	TextFlagNotSet	= "no";
		public static String 	PropertyFileName="AssistPlus.properties";
	}
	
	public static class XML {
		public static String XmlVersion="<?xml version='1.0' encoding='ISO-8859-1'?>";
		public static String StartingTagRole="<AssistPlus xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://assistplus.com  XSDrole.xsd' xmlns='http://assistplus.com' version='";
		public static String StartingTagGroup="<AssistPlus xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://assistplus.com  XSDrole.xsd' xmlns='http://assistplus.com' version='";
		public static String EndAttrTag="'>";
		public static String EndingTag="</AssistPlus>";
		public static String TableAssistText="Text";
		public static String TableAssitColor="LabelColors";
		public static String TableRolePriority="Roles";
		public static String XMLFileName="export.xml";
				
	}

	public static class DB {
		public static String		DB_NAME						= "xacs.assist.db";
		public static String		TBL_ASSISTTEXT				= "AssistText";
		public static String		TBL_ROLEPRIORITY			= "RolePriority";
		public static String		COL_CLASSID					= "ClassID";
		public static String		COL_TEXTID					= "TextID";
		public static String		COL_COLORID					= "ColorID";
		public static String		COL_ATTRID					= "AttrID";
		public static String		COL_ROLEID					= "RoleID";
		public static String		COL_PRIORITY				= "Priority";
		public static String		COL_ROLE					= "Role";
		public static String		COL_TEXT					= "AssistText";
		public static String		COL_FONT					= "fontcolor";
		public static String		COL_BACKGROUND			    = "background";
		public static String		COL_DIFFCOLOR				= "isDiffColor";
		public static String		COL_ASSISTCOLOR				= "AssistColor";
		public static String		COL_WORKID					= "workflowID";
		public static String		COL_WORKSTATID				= "workflowStatusID";
		public static String		COL_DATACREATED				= "DateCreated";
		public static String		IDX_CLASSID					= "IDX_ClassID";
		public static final String	IDX_ROLEPRIORITY_ROLE		= "IDX_RPR_ROLE";
		public static String		CREATE_TABLE_QUERY			= "CREATE TABLE " + TBL_ASSISTTEXT + " (" + COL_CLASSID + " INTEGER NOT NULL, " + COL_ATTRID
																		+ " INTEGER NOT NULL, " + COL_ROLEID + " INTEGER NOT NULL, " + COL_PRIORITY
																		+ " INTEGER NOT NULL, " + COL_TEXT + " TEXT NOT NULL, " + "PRIMARY KEY (" + COL_CLASSID
																		+ "," + COL_ATTRID + "," + COL_ROLEID + "," + COL_PRIORITY + ")" + ")";
		public static String		CREATE_CLASSID_INDEX_QUERY	= "CREATE INDEX " + IDX_CLASSID + " ON " + TBL_ASSISTTEXT + "(" + COL_CLASSID + ","
																		+ COL_ATTRID + ")";
		public static String		MSG_DB_LOCKED				="Database is Locked";
		public static String		MSG_DB_ERROR				="SQL logic error";
		public static String		MSG_DB_BUSY					="Database Busy";
		public static String 		MSG_DRIVER_NOT_INSTALLED	="SQLite JDBC driver not installed";
	}
}
// public final 