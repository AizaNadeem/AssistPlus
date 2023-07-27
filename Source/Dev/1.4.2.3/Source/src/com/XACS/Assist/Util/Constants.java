package com.XACS.Assist.Util;

public final class Constants {
	public static class Config {
		public static String	HOME_ENVVAR		= "PLMFLEX_HOME";
		public static String	USERHOME_PROP	= "user.home";
		public static String	AgileServerURL	= "AgileURL";
		public static String	AgileUserName	= "AgileUser";
		public static String	AgilePassword	= "AgilePassword";
		public static String	PrimaryInstURL 	= "PrimaryInstanceURL";
		public static String ACCESSTYPEROLE=null;
	}

	public static class General {
		public static String	TextFlagSet		= "yes";
		public static String	TextFlagNotSet	= "no";
		 
	}

	public static class DB {
		public static String		DB_NAME						= "xacs.assist.db";
		public static String		TBL_ASSISTTEXT				= "AssistText";
		public static String		TBL_ROLEPRIORITY			= "RolePriority";
		public static String		COL_CLASSID					= "ClassID";
		public static String		COL_ATTRID					= "AttrID";
		public static String		COL_ROLEID					= "RoleID";
		public static String		COL_PRIORITY				= "Priority";
		public static String		COL_ROLE					= "Role";
		public static String		COL_TEXT					= "AssistText";
		public static String		IDX_CLASSID					= "IDX_ClassID";
		public static final String	IDX_ROLEPRIORITY_ROLE		= "IDX_RPR_ROLE";
		public static String		CREATE_TABLE_QUERY			= "CREATE TABLE " + TBL_ASSISTTEXT + " (" + COL_CLASSID + " INTEGER NOT NULL, " + COL_ATTRID
																		+ " INTEGER NOT NULL, " + COL_ROLEID + " INTEGER NOT NULL, " + COL_PRIORITY
																		+ " INTEGER NOT NULL, " + COL_TEXT + " TEXT NOT NULL, " + "PRIMARY KEY (" + COL_CLASSID
																		+ "," + COL_ATTRID + "," + COL_ROLEID + "," + COL_PRIORITY + ")" + ")";
		public static String		CREATE_CLASSID_INDEX_QUERY	= "CREATE INDEX " + IDX_CLASSID + " ON " + TBL_ASSISTTEXT + "(" + COL_CLASSID + ","
																		+ COL_ATTRID + ")";
	}
}
// public final 