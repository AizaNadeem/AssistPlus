package com.xavor.plmxl.assist.Handler;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.xavor.plmxl.assist.DO.AssistColorEntry;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class DBHandler {
	static Connection conn = null;
	Statement stat = null;
	Statement stat1 = null;

	Statement stat2 = null;
	PreparedStatement prep = null;
	PreparedStatement prep1 = null;

	AssistLogger log = AssistLogger.getInstance();
	public static boolean executeUpdate = false;

	public DBHandler() throws Exception {
		if (conn == null || conn.isClosed()) {
			conn = null;
			init();
		}
	}

	private void init() throws Exception, SQLException {
		log.debug("Entering init..");
		try {
			String homePath = ConfigHelper.getAppHomePath();
			HashMap<String, String> prop = ConfigHelper.getDbProperties();
			Class.forName(prop.get("driver"));
			conn = DriverManager.getConnection(prop.get("connstring") + homePath + Constants.DB.DB_NAME);// "xacs.assist.db"
			log.info("DB Connection Created..");
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getColumns(null, null, "OptOutUsers", "optoutdate");
			if (rs.next()) {
				log.info("DB is already Updated");
				rs.close();
			} else {
				handleDBRequest("updateDataBase", null, true);
				executeUpdate = true;
				rs.close();
			}
		} catch (ClassNotFoundException e) {
			log.error("ClassNotFoundException in init() : ", e);
			throw new Exception(Constants.DB.MSG_DRIVER_NOT_INSTALLED);
		}
		log.debug("Exiting init..");
	}

	private void updateDataBase() throws Exception, SQLException {
		stat.addBatch("CREATE TABLE IF NOT EXISTS [TEMP_TABLE] (" + "[TextID] INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE," + "[ClassID] INTEGER  NULL," + "[AttrID] TEXT NULL," + "[AssistText] TEXT  NULL" + ", fontcolor text, background text, isDiffColor boolean default false, 'workflowID' CHAR, 'workflowStatusID' CHAR, DateCreated TEXT, LastUpdated TEXT);");
		stat.addBatch("INSERT INTO TEMP_TABLE SELECT * FROM AssistText;");

		stat.addBatch("DROP TABLE AssistText;");

		stat.addBatch("ALTER TABLE TEMP_TABLE RENAME TO AssistText;");

		stat.addBatch("CREATE TABLE 'AssistColorTEMP' ('ColorID' INTEGER PRIMARY KEY  NOT NULL ,'ClassID' INTEGER,'AttrID' TEXT,'AssistColor' text);");

		stat.addBatch("INSERT INTO AssistColorTEMP SELECT * FROM AssistColor;");

		stat.addBatch("DROP TABLE AssistColor;");

		stat.addBatch("ALTER TABLE AssistColorTEMP RENAME TO AssistColor;");

		stat.addBatch("ALTER TABLE OptOutUsers ADD COLUMN optoutdate CHAR;");
		stat.executeBatch();

	}

	private Map<String, String> readConfigurations() throws Exception, SQLException {
		log.debug("Entering readConfigurations...");

		HashMap<String, String> configMap = null;
		ResultSet rs = stat.executeQuery("SELECT ConfigKey, ConfigVal " + "FROM Configurations WHERE ConfigKey <> 'LNFO' ORDER BY rowid DESC");
		configMap = new HashMap<String, String>();
		while ((rs != null) && rs.next()) {
			configMap.put(rs.getString("ConfigKey"), rs.getString("ConfigVal"));
		}

		log.debug("Configurations: " + configMap.toString());
		log.debug("Exiting readConfigurations...");

		return configMap;
	}

	private String generateQsForIn(int numQs) {
		StringBuilder items = new StringBuilder();
		for (int i = 0; i < numQs; i++) {
			if (i != 0)
				items.append(", ");
			items.append("?");
		}
		return items.toString();
	}

	public class infoMapBean implements java.io.Serializable {
		private String attrId;
		private String assistText;
		private String fontcolor;
		private String background;
		private String lastUpdated;
		private String classId;

		public infoMapBean() {

		}

		public String getAttrId() {
			return attrId;
		}

		public void setAttrId(String attrId) {
			this.attrId = attrId;
		}

		public String getAssistText() {
			return assistText;
		}

		public void setAssistText(String assistText) {
			this.assistText = assistText;
		}

		public String getFontcolor() {
			return fontcolor;
		}

		public void setFontcolor(String fontcolor) {
			this.fontcolor = fontcolor;
		}

		public String getBackground() {
			return background;
		}

		public void setBackground(String background) {
			this.background = background;
		}

		public String getLastUpdated() {
			return lastUpdated;
		}

		public void setLastUpdated(String lastUpdated) {
			this.lastUpdated = lastUpdated;
		}

		public String getClassId() {
			return classId;
		}

		public void setClassId(String classId) {
			this.classId = classId;
		}
	}

	/**
	 * Query used for extracting assist texts ** SELECT AttrP.AttrID,
	 * AssistText.AssistText FROM ( SELECT AttrID, MAX(Priority) Priority FROM
	 * RoleTextMap INNER JOIN RolePriority ON RoleTextMap.RoleID =
	 * RolePriority.RoleID INNER JOIN AssistText ON RoleTextMap.TextID =
	 * AssistText.TextID WHERE Role IN ('Administrator','Change Analyst','Change
	 * Agent','All Roles') AND ClassID IN (931) GROUP BY AttrID ORDER BY
	 * RoleTextMap.TextID ) AttrP INNER JOIN AssistText ON AttrP.AttrID =
	 * AssistText.AttrID INNER JOIN RolePriority ON AttrP.Priority =
	 * RolePriority.Priority INNER JOIN RoleTextMap ON RoleTextMap.RoleID =
	 * RolePriority.RoleID AND RoleTextMap.TextID = AssistText.TextID
	 * 
	 * @param workflowSTatusID
	 * @param workflowID
	 ***/
	private Map<String, ArrayList<String>> getAssistInfoMap(HashMap<String, Object> params) throws Exception, SQLException {
		log.debug("Entering getAssistInfoMap...");

		HashMap<String, ArrayList<String>> asInfo = null;
		String classes = (String) params.get("classes");
		String roles = (String) params.get("roles");
		String allRoleKey = (String) params.get("allRoleKey");
		String workflowID = (String) params.get("workflowID");
		String workflowSTatusID = (String) params.get("workflowStatusID");

		log.debug("Classes: " + classes);
		String subClass = "";
		String levelOneParent = "";
		String levelTwoParent = "";
		String splitClasses[] = classes.split(",");
		if (splitClasses.length > 1 && splitClasses.length <= 3) {
			subClass = splitClasses[0];
			levelOneParent = splitClasses[1];
			levelTwoParent = splitClasses[2];
			log.debug(subClass + "," + levelOneParent + "," + levelTwoParent);
		}
		subClass = subClass.substring(0, subClass.length() - 1);
		levelOneParent = levelOneParent.substring(0, levelOneParent.length() - 1);
		log.debug("After: " + subClass + "," + levelOneParent + "," + levelTwoParent);
		String rolestr = roles;
		String clsstr = classes;

		String rolesArray[] = (rolestr + allRoleKey).split(",");
		String prepMultiRolesParam = "";
		if (rolesArray != null) {
			prepMultiRolesParam = generateQsForIn(rolesArray.length);
		}

		
		String classesArray[] = clsstr.split(",");
		String prepMultiClassIDParam = "";
		if (classesArray != null) {
			prepMultiClassIDParam = generateQsForIn(classesArray.length);
		}

		String quer = "SELECT AttrP.ClassID,AttrP.AttrID, AssistText.AssistText," + " case when AssistText.isDiffColor then AssistText.fontcolor else RolePriority.fontcolor end as fontcolor," + " case when AssistText.isDiffColor then AssistText.background else RolePriority.background end as background, AssistText.LastUpdated " + "FROM ( SELECT ClassID, AttrID, MAX(Priority) Priority "
				+ "FROM RoleTextMap " + "INNER JOIN RolePriority " + "ON RoleTextMap.RoleID = RolePriority.RoleID " + "INNER JOIN AssistText " + "ON RoleTextMap.TextID = AssistText.TextID "
				+ "WHERE Role IN ("	+ prepMultiRolesParam + ") " //
				+ "AND ClassID IN (" + prepMultiClassIDParam + ") " // 1
				+ "AND (workflowId IN (?,'All Workflows','') OR workflowId IS NULL) " // 2
				+ "AND (workflowStatusID like ? " // 3
				+ "OR workflowStatusID IS NULL"
				+ " OR workflowStatusID = '' "
				+ " OR workflowStatusID = 'All Statuses')"
				+ "GROUP BY ClassID, AttrID) AttrP "
				+ "INNER JOIN AssistText "
				+ "ON AttrP.AttrID = AssistText.AttrID "
				+ "AND AttrP.ClassID=AssistText.ClassID "
				+ "INNER JOIN RolePriority "
				+ "ON AttrP.Priority = RolePriority.Priority "
				+ "INNER JOIN RoleTextMap "
				+ "ON RoleTextMap.RoleID = RolePriority.RoleID " + "AND RoleTextMap.TextID = AssistText.TextID "
				+ "WHERE (workflowId IN (?,'All Workflows','') OR workflowId IS NULL) " // 4
				+ "AND (workflowStatusID like ? " // 5
				+ "OR workflowStatusID IS NULL" + " OR workflowStatusID = '' " + " OR workflowStatusID = 'All Statuses')";

		log.debug(" getAssistInfoMap query=[" + quer + "]");

		prep = conn.prepareStatement(quer);
		int i = 1;
		for (int x = 0; x < rolesArray.length; x++) {
			String temp = rolesArray[x].trim();
			if (temp.startsWith("'")) {
				temp = temp.replaceFirst("'", "");
			}
			if (temp.endsWith("'")) {
				temp = temp.substring(0, temp.length() - 1);
			}
			prep.setString(i++, temp);
		}
		for (int y = 0; y < classesArray.length; y++) {
			prep.setInt(i++, Integer.parseInt(classesArray[y].trim()));
		}

		prep.setString(i++, workflowID);
		prep.setString(i++, "%" + workflowSTatusID + "%");
		prep.setString(i++, workflowID);
		prep.setString(i++, "%" + workflowSTatusID + "%");

		ResultSet rs = prep.executeQuery();

		List<infoMapBean> infomapRecords = new ArrayList<DBHandler.infoMapBean>();
		while (rs.next()) {
			infoMapBean bean = new infoMapBean();
			bean.setAttrId(rs.getString("AttrID"));
			bean.setAssistText(rs.getString("AssistText"));
			bean.setFontcolor(rs.getString("fontcolor"));
			bean.setBackground(rs.getString("background"));
			bean.setLastUpdated(rs.getString("LastUpdated"));
			bean.setClassId(rs.getString("ClassID"));
			infomapRecords.add(bean);
		}

		log.debug("Prepared Startement : " + prep);

		ArrayList<String> roleAttributes;
		String attrId;
		String assistText;
		String fontcolor;
		String background, lastUpdated;
		asInfo = new HashMap<String, ArrayList<String>>();
		HashMap<String, String> classIdInfo = new HashMap<String, String>();
		String classId;
		

		for (infoMapBean bean : infomapRecords) {

			attrId = bean.getAttrId();
			assistText = bean.getAssistText();
			fontcolor = bean.getFontcolor();
			background = bean.getBackground();
			lastUpdated = bean.getLastUpdated();
			classId = bean.getClassId();
			log.debug("Attreibute id: " + attrId);
			log.debug("Class id: " + classId);
			log.debug("subclass: " + subClass);
			log.debug("level one parent: " + levelOneParent);
			log.debug("level two parent: " + levelTwoParent);

			roleAttributes = new ArrayList<String>();
			roleAttributes.add(assistText);

			if (fontcolor == null && background == null || (fontcolor.equals(" ") && background.equals(" "))) {
				HashMap params2 = new HashMap<String, String>();
				params2.put("key", "fontColor");
				HashMap map = this.handleDBRequest("getConfigByKey", params2, false);
				fontcolor = (String) map.get("value");

				params2 = new HashMap<String, String>();
				params2.put("key", "backgroundColor");
				map = this.handleDBRequest("getConfigByKey", params2, false);
				background = (String) map.get("value");

			}
			roleAttributes.add(fontcolor);
			roleAttributes.add(background);
			HashMap params2 = new HashMap<String, String>();
			params2.put("key", "hoverColor");
			HashMap map = this.handleDBRequest("getConfigByKey", params2, false);

			roleAttributes.add((String) map.get("value"));
			roleAttributes.add(lastUpdated);

			params2 = new HashMap<String, String>();
			params2.put("key", "indicateNewText");
			map = this.handleDBRequest("getConfigByKey", params2, false);
			String indicateNew = (String) map.get("value");

			if (indicateNew.equalsIgnoreCase("yes")) {
				params2 = new HashMap<String, String>();
				params2.put("key", "textDuration");
				map = this.handleDBRequest("getConfigByKey", params2, false);

				roleAttributes.add((String) map.get("value"));
			} else {
				roleAttributes.add("-1");
			}
			// /////////////////////////////////////////////////////////////
			// Check for Level1 ! and Level3
			if (Integer.parseInt(classId) == Integer.parseInt(subClass)) {
				asInfo.put(attrId, roleAttributes);
				log.debug("subclass: " + subClass);
				classIdInfo.put(attrId, subClass);
			} else if (Integer.parseInt(classId) == Integer.parseInt(levelOneParent)) {
				if (asInfo.containsKey(attrId)) {
					String currentClass = classIdInfo.get(attrId);
					if (Integer.parseInt(currentClass) != Integer.parseInt(subClass)) {
						asInfo.put(attrId, roleAttributes);
						log.debug("level one parent: " + levelOneParent);
						classIdInfo.put(attrId, levelOneParent);
					}
				} else {
					asInfo.put(attrId, roleAttributes);
					log.debug("level one parent: " + levelOneParent);
					classIdInfo.put(attrId, levelOneParent);
				}
			} else// if(classId.equalsIgnoreCase(levelTwoParent))
			{
				if (!asInfo.containsKey(attrId)) {
					asInfo.put(attrId, roleAttributes);
					log.debug("level two parent: " + levelTwoParent);
					classIdInfo.put(attrId, levelTwoParent);
				}
			}
			// asInfo.put(attrId, roleAttributes);
		}
		
		log.debug("AssistInfo: " + asInfo.toString());

		log.debug("Exiting getAssistInfoMap...");

		return asInfo;
	}

	

	private Map<String, ArrayList<AssistTextEntry>> getAssistTexts(HashMap<String, String> params) throws Exception, SQLException {
		log.debug("Entering getAssistTexts..");
		ArrayList<AssistTextEntry> ateArr = null;
		ArrayList<String> strRoleArr;
		String classId = (String) params.get("classID");
		String attrId = (String) params.get("attrId");
		String isRoutable = (String) params.get("isRoutable");
		ResultSet rs = stat.executeQuery("SELECT TextID, AssistText, isDiffColor, fontcolor, background,workflowId,workflowStatusID,DateCreated,LastUpdated " + "FROM AssistText " + "WHERE ClassID=" + classId + " AND AttrID='" + attrId + "'");
		ateArr = new ArrayList<AssistTextEntry>();
		AssistTextEntry ate = null;
		while ((rs != null) && rs.next()) {
			ate = new AssistTextEntry();
			ate.setTextID(rs.getString("TextID"));
			ate.setAssistText(rs.getString("AssistText"));
			ate.setFontColor(rs.getString("fontcolor"));
			ate.setBackgroundColor(rs.getString("background"));
			ate.setIsDifferentColor(rs.getBoolean("isDiffColor"));
			
			if(isRoutable != null && isRoutable.equalsIgnoreCase("false")){
				String workflowStatusID = rs.getString("workflowStatusID");
				if(workflowStatusID.equalsIgnoreCase("All Statuses")){
					workflowStatusID = "All Lifecycles";
				}
				ate.setWorkflowID(workflowStatusID);
			} else{
				
				ate.setWorkflowID(rs.getString("workflowId"));
				ate.setWorkflowStatusID(rs.getString("workflowStatusID"));
			}
			
			ate.setDateCreated(rs.getString("DateCreated"));
			if (rs.getString("LastUpdated") != null) {
				ate.setLastUpdated(rs.getString("LastUpdated"));
			} else {
				ate.setLastUpdated("");
			}
			initSecondStatement();
			ResultSet rs1 = stat1.executeQuery("SELECT RP.RoleID value, Role label,  CASE WHEN (ifnull(TM.RoleID,-1) == -1) THEN 0 ELSE 1 END AS selected " + "FROM RolePriority RP " + "LEFT OUTER JOIN (SELECT RoleID from RoleTextMap WHERE TextID=" + ate.getTextID() + ") TM " + "ON RP.RoleID = TM.RoleID ");
			strRoleArr = new ArrayList<String>();

			while ((rs1 != null) && rs1.next()) {
				log.debug("getAssistTexts: " + rs1.getString("label"));
				strRoleArr.add(rs1.getString("value"));
				strRoleArr.add(rs1.getString("label"));
				strRoleArr.add((rs1.getString("selected").equalsIgnoreCase("0")) ? "*" : "selected");
			}
			String[] strarr = new String[strRoleArr.size()];
			ate.setRoles(strRoleArr.toArray(strarr));
			ate.setClassID(classId);
			ate.setAttrID(attrId);
			ateArr.add(ate);
		}

		HashMap<String, ArrayList<AssistTextEntry>> retHash = new HashMap<String, ArrayList<AssistTextEntry>>();
		retHash.put("ateArr", ateArr);
		log.debug("Exiting getAssistTexts..");
		return retHash;
	}

	private Map<String, String[]> getRoleOptions() throws Exception, SQLException {
		log.debug("Entering getRoleOptions...");

		String[] strRoles = null;
		String str;
		HashMap<String, String[]> retMap = new HashMap<String, String[]>();
		ResultSet rs = stat.executeQuery("SELECT RoleID value, Role label " + "FROM RolePriority RP;");
		ArrayList<String> strRoleArr = new ArrayList<String>();

		while ((rs != null) && rs.next()) {
			log.debug("getroleoptions: " + rs.getString("label"));
			strRoleArr.add(rs.getString("value"));
			strRoleArr.add(rs.getString("label"));
		}
		strRoles = new String[strRoleArr.size()];
		strRoles = strRoleArr.toArray(strRoles);

		log.debug("RoleOptions" + strRoles.toString());
		retMap.put("strRoles", strRoles);
		log.debug("Exiting getRoleOptions...");

		return retMap;
	}

	private Map<String, List<String>> getRoleIds() throws Exception, SQLException {
		log.debug("Entering getRoleIds...");

		List<String> roleIds = new ArrayList<String>();

		HashMap<String, List<String>> retMap = new HashMap<String, List<String>>();
		ResultSet rs = stat.executeQuery("SELECT RoleID FROM RolePriority;");

		while ((rs != null) && rs.next()) {
			roleIds.add(rs.getString("RoleID"));
		}

		log.debug("RoleOptions" + roleIds.toString());
		retMap.put("assistRoles", roleIds);
		log.debug("Exiting getRoleIds...");

		return retMap;
	}

	private Map<String, ArrayList<String>> getRolePriorities() throws SQLException, Exception {
		log.debug("Entering getRolePriorities...");

		HashMap<String, ArrayList<String>> roleMap = null;
		ArrayList<String> roleAttributes;
		ResultSet rs = stat.executeQuery("SELECT Role,Priority,fontcolor,background FROM RolePriority RP ORDER BY Priority ASC ");
		roleMap = new HashMap<String, ArrayList<String>>();
		while ((rs != null) && rs.next()) {
			roleAttributes = new ArrayList<String>();
			roleAttributes.add(rs.getString("Priority"));
			roleAttributes.add(rs.getString("fontcolor"));
			roleAttributes.add(rs.getString("background"));
			roleMap.put(rs.getString("Role"), roleAttributes);
		}
		log.debug("Role Priorities: " + roleMap.toString());
		log.debug("Exiting getRolePriorities...");

		return roleMap;
	}

	private Map<String, ArrayList<Integer>> checkClassesAssistText() throws SQLException, Exception {
		log.debug("Entering checkClassesAssistText...");

		ArrayList<Integer> i = new ArrayList<Integer>();
		HashMap<String, ArrayList<Integer>> ret = new HashMap<String, ArrayList<Integer>>();
		ResultSet rs = stat.executeQuery("SELECT DISTINCT ClassID FROM AssistText WHERE ClassID NOT NULL");
		while ((rs != null) && rs.next()) {
			i.add(rs.getInt("ClassID"));
		}
		ret.put("result", i);
		log.debug("checkClassesAssistText: " + i.toString());
		log.debug("Exiting checkClassesAssistText...");

		return ret;
	}

	private Map<String, Object> getAssistColorsForClasses(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering getAssistColorsForClasses...");

		String classIds = (String) params.get("classId");
		String selectQry = "";
		Map<String, Object> attColors = new HashMap<String, Object>(0);

		String subClass = "";
		String levelOneParent = "";
		String levelTwoParent = "";
		String splitClasses[] = classIds.split(",");

		if (splitClasses.length > 1 && splitClasses.length <= 3) {
			subClass = splitClasses[0];
			levelOneParent = splitClasses[1];
			levelTwoParent = splitClasses[2];
			log.debug(subClass + "," + levelOneParent + "," + levelTwoParent);
		}
		subClass = subClass.substring(0, subClass.length() - 1);
		levelOneParent = levelOneParent.substring(0, levelOneParent.length() - 1);

		selectQry = "SELECT ColorID, AttrID, AssistColor,ClassID FROM AssistColor WHERE ClassID IN (" + classIds + ")";
		log.debug("getAssistColors select query: " + selectQry);

		ResultSet rs = null;
		AssistColorEntry colorEntry = null;
		rs = stat.executeQuery(selectQry);

		HashMap<String, String> classInfo = new HashMap<String, String>();

		if (rs != null) {
			while (rs.next()) {
				if (Integer.parseInt(rs.getString("ClassID")) == Integer.parseInt((subClass))) {
					attColors.put(rs.getString("AttrID"), rs.getString("AssistColor") + "");
					classInfo.put(rs.getString("AttrID"), subClass);
				} else if (Integer.parseInt(rs.getString("ClassID")) == Integer.parseInt(levelOneParent)) {
					if (attColors.containsKey(rs.getString("AttrID"))) {
						String currentClass = classInfo.get(rs.getString("AttrID"));
						if (Integer.parseInt(currentClass) != Integer.parseInt(subClass)) {
							attColors.put(rs.getString("AttrID"), rs.getString("AssistColor") + "");
							log.debug("level one parent: " + levelOneParent);
							classInfo.put(rs.getString("AttrID"), levelOneParent);
						}
					} else {
						attColors.put(rs.getString("AttrID"), rs.getString("AssistColor") + "");
						log.debug("level one parent: " + levelOneParent);
						classInfo.put(rs.getString("AttrID"), levelOneParent);
					}
				} else {
					if (!attColors.containsKey(rs.getString("AttrID"))) {
						attColors.put(rs.getString("AttrID"), rs.getString("AssistColor") + "");
						log.debug("level two parent: " + levelTwoParent);
						classInfo.put(rs.getString("AttrID"), levelTwoParent);
					}
				}
			}
		}
		log.debug("Attribute Colors: " + attColors.toString());
		log.debug("Exiting getAssistColorsForClasses...");
		return attColors;
	}

	private Map<String, Object> getAssistColorsForClass(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering getAssistColorsForClass...");

		String classId = (String) params.get("classId");
		String selectQry = "";
		Map<String, Object> attColors = new HashMap<String, Object>(0);

		selectQry = "SELECT ColorID, AttrID, AssistColor FROM AssistColor WHERE ClassID=" + classId;
		log.debug("getAssistColors select query: " + selectQry);

		ResultSet rs = null;
		AssistColorEntry colorEntry = null;
		rs = stat.executeQuery(selectQry);
		if (rs != null) {
			while (rs.next()) {
				colorEntry = new AssistColorEntry();
				colorEntry.setColorId(rs.getString("ColorID"));
				colorEntry.setAttributeId(rs.getString("AttrID"));
				log.debug(rs.getString("AttrID"));
				colorEntry.setAssistColor(rs.getString("AssistColor"));
				attColors.put(colorEntry.getAttributeId(), colorEntry);
			}
		}
		log.debug("Attribute Colors: " + attColors.toString());

		log.debug("Exiting getAssistColorsForClass...");

		return attColors;
	}

	private Map<String, HashMap<String, String>> checkAssistText(HashMap params) throws SQLException, Exception {
		log.debug("Entering checkAssistText...");

		int classID = (Integer) params.get("classID");
		HashMap<String, String> i = new HashMap<String, String>();
		HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
		ResultSet rs = stat.executeQuery("SELECT AttrID FROM AssistText WHERE ClassID='" + classID + "'");

		log.debug("checkAssistText Selecct Query: " + "SELECT AttrID FROM AssistText WHERE ClassID='" + classID + "'");
		while ((rs != null) && rs.next()) {
			i.put(rs.getString("AttrID"), rs.getString("AttrID"));
		}
		result.put("result", i);

		log.debug("Check Assist Text: " + result.toString());
		log.debug("Exiting checkAssistText...");

		return result;
	}

	private Map<String, Integer> addNewAssistColor(HashMap params) throws Exception, SQLException {
		log.debug("Entering addNewAssistColor...");

		int res = -1;
		String classID = (String) params.get("classID");
		JSONArray jsonAttColors = (JSONArray) params.get("jsonAttColors");
		JSONObject attColor = null;

		String colorId = "";
		String attId = "";
		String assistColor = "";

		String insertQry = "INSERT INTO AssistColor (ClassID,AttrID,AssistColor) VALUES (?,?,?)";

		if (jsonAttColors != null && jsonAttColors.size() > 0) {
			HashMap<String, String> params1 = new HashMap<String, String>();
			params1.put("classID", classID);
			handleDBRequest("deleteColor", params1, true);

			for (int attColorIndex = 0; attColorIndex < jsonAttColors.size(); attColorIndex++) {
				attColor = (JSONObject) jsonAttColors.get(attColorIndex);

				colorId = attColor.get("colorId") + "";
				attId = attColor.get("attId") + "";
				assistColor = attColor.get("assistColor") + "";
				if (!assistColor.equalsIgnoreCase(Constants.Config.DEFAULT_LABEL_COLOR)) {
					log.debug("addNewAssistColor Insert/Update ::: Color Id=[" + colorId + "], Attribute Id=[" + attId + "], Assist Color=[" + assistColor + "]");

					if (prep == null) {
						prep = conn.prepareStatement(insertQry);
					}

					prep.setString(1, classID);
					prep.setString(2, attId);
					prep.setString(3, assistColor);

					res = prep.executeUpdate();
					log.debug("Insert/Update AssistColor result=[" + res + "]");
				}
			}
		}
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		result.put("result", res);

		log.debug("Exiting addNewAssistColor...");

		return result;
	}

	private void deleteColor(HashMap params) throws SQLException, Exception {
		String classID = (String) params.get("classID");

		log.debug("Entering deleteColor...");

		String strStatament = "DELETE FROM AssistColor WHERE ClassID=" + classID + ";";
		log.debug("deleteColor delete Query: " + "DELETE FROM AssistColor WHERE ClassID=" + classID + ";");

		ResultSet rs = stat.executeQuery("SELECT ColorID " + "FROM AssistColor " + "WHERE ClassID=" + classID + ";");
		if (rs != null && rs.next()) {
			stat.addBatch(strStatament);
		}
		stat.executeBatch();

		log.debug("Exiting deleteColor...");

	}

	private Map<String, Integer> addNewAssistText(HashMap params) throws Exception, SQLException {
		log.debug("Entering addNewAssistText. ");
		Integer textId = -1;
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		String classID = (String) params.get("classID");
		String attrID = (String) params.get("attrID");
		String assistText = (String) params.get("assistText");
		String fontColor = (String) params.get("fontColor");
		String backgroundColor = (String) params.get("backgroundColor");
		boolean isDiffColor = (Boolean) params.get("isDiffColor");
		String workflowID = (String) params.get("workflowID");
		String workflowStatusID = (String) params.get("workflowStatusID");

		String query = "SELECT TextID " + "FROM AssistText " + "WHERE ClassID=" + classID + " AND AttrID='" + attrID + "'" + " AND AssistText='" + assistText.replace("'", "''") + "'" + " AND workflowID='" + workflowID.replace("'", "''") + "'" + " AND workflowStatusID='" + workflowStatusID.replace("'", "''") + "'" + " ORDER BY TextID DESC";

		prep = conn.prepareStatement("INSERT INTO AssistText (ClassID, AttrID, AssistText,fontcolor,background,isDiffColor,workflowID,workflowStatusID,DateCreated,LastUpdated) Values (?,?,?,?,?,?,?,?,?,?);");

		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();

		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());

		String timeStamp = simpleDateFormatter.format(today);
		log.debug("TimeStamp: " + timeStamp);

		prep.setString(1, classID);
		prep.setString(2, attrID);
		prep.setString(3, assistText);
		prep.setString(4, fontColor);
		prep.setString(5, backgroundColor);
		prep.setBoolean(6, isDiffColor);
		prep.setString(7, workflowID);
		prep.setString(8, workflowStatusID);
		prep.setString(9, timeStamp);
		prep.setString(10, timeStamp);
		/** Start transaction to Add the AssistText and retrieve the new TextID **/

		prep.executeUpdate();

		try {
			ResultSet rs = stat.executeQuery(query);
			if ((rs != null) && rs.next()) {
				textId = rs.getInt("TextID");
			}
			result.put("textId", textId);
		} catch (Exception e) {
			log.error("", e);
		}

		String rolestr;
		String[] roleList = (String[]) params.get("roleList");
		if (roleList != null) {
			rolestr = "";
			for (String str : roleList) {
				rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
			}
			stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textId + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");
			log.debug("updateTextRoleList: INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textId + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");

		}
		stat.executeBatch();
		log.debug("Exiting addNewAssistText. ");
		return result;
	}

	private void removeAssistText(HashMap params) throws Exception, SQLException {
		log.debug("Entering removeAssistText...");

		String textID = (String) params.get("textID");
		stat.addBatch("DELETE FROM AssistText WHERE TextID=" + textID + ";");
		log.debug("removeAssistText: DELETE FROM AssistText WHERE TextID=" + textID + ";");

		stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
		log.debug("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
		stat.executeBatch();
		log.debug("Exiting removeAssistText...");
	}

	private void updateAssistText(HashMap params) throws Exception, SQLException {
		log.debug("Entering updateAssistText..");
		String textID = (String) params.get("textID");
		String assistText = (String) params.get("assistText");
		String fontColor = (String) params.get("fontColor");
		String backgroundColor = (String) params.get("backgroundColor");
		boolean isDiffColor = (Boolean) params.get("isDiffColor");
		String workflowID = (String) params.get("workflowID");
		String workflowStatusID = (String) params.get("workflowStatusID");
		prep = conn.prepareStatement("Update AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?,workflowID=?,workflowStatusID=?, LastUpdated=?   WHERE TextID=?;");

		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();
		System.out.println(java.util.TimeZone.getDefault());
		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());

		String timeStamp = simpleDateFormatter.format(today);
		log.debug("TimeStamp: " + timeStamp);
		prep.setString(1, assistText);
		prep.setString(2, fontColor);
		prep.setString(3, backgroundColor);
		prep.setBoolean(4, isDiffColor);

		if (workflowID == null || workflowID.equals("") || workflowID.equals("null")) {
			prep.setString(5, "All Workflows");
		} else {
			prep.setString(5, workflowID);
		}
		prep.setString(6, workflowStatusID);
		prep.setString(7, timeStamp);
		prep.setString(8, textID);
		prep.executeUpdate();

		String rolestr;
		String[] roleList = (String[]) params.get("roleList");
		if (roleList != null) {
			rolestr = "";
			for (String str : roleList) {
				rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
			}
			stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
			log.debug(" updateAllAssistText: DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
			stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");
			log.debug("updateTextRoleList: INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");

		}
		stat.executeBatch();
		log.debug("Exiting updateAssistText..");

	}

	private void updateAllAssistText(HashMap params) throws Exception, SQLException {
		log.debug("Entering updateAllAssistText...");

		ArrayList<AssistTextEntry> rows = (ArrayList<AssistTextEntry>) params.get("rows");
		String text;
		String textid;
		String fontcolor;
		String background;
		String rolestr = "";
		boolean isDiffColor;
		String[] roles;
		ResultSet rs;
		initSecondStatement();
		for (int t = 0; t < rows.size(); t++) {
			text = rows.get(t).getAssistText();
			textid = rows.get(t).getTextID();
			fontcolor = rows.get(t).getFontColor();
			background = rows.get(t).getBackgroundColor();
			isDiffColor = rows.get(t).getIsDifferentColor();
			textid = textid.replace("row", "");
			roles = rows.get(t).getRoles();
			rs = stat1.executeQuery("SELECT TextID FROM AssistText WHERE TextID=" + textid);
			log.debug("updateAllAssistText: SELECT TextID FROM AssistText WHERE TextID=" + textid);

			if (rs != null) {
				stat.addBatch("Update AssistText SET AssistText='" + text + "',isDiffColor='" + isDiffColor + "',fontcolor='" + fontcolor + "',background='" + background + "' WHERE TextID='" + textid + "';");

				log.debug("Update AssistText SET AssistText='" + text + "',isDiffColor='" + isDiffColor + "',fontcolor='" + fontcolor + "',background='" + background + "' WHERE TextID='" + textid + "';");

				stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textid + ";");
				log.debug(" updateAllAssistText: DELETE FROM RoleTextMap WHERE TextID=" + textid + ";");
				if (roles != null) {
					rolestr = "";
					for (String str : roles) {
						rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
					}
					stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textid + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");
					log.debug("updateAllAssistText: INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textid + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");

				}
			}
		}
		stat.executeBatch();
		log.debug("Exiting updateAllAssistText...");
	}

	private void updateTextRoleList(HashMap params) throws Exception, SQLException {
		log.debug("Entering updateTextRoleList...");

		String textID = (String) params.get("textID");
		String rolestr;
		String[] roleList = (String[]) params.get("roleList");
		stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");

		log.debug("updateTextRoleList: DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
		if (roleList != null) {
			rolestr = "";
			for (String str : roleList) {
				rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
			}
			stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");
			log.debug("updateTextRoleList: INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN (" + rolestr + ");");

		}
		stat.executeBatch();
		log.debug("Exiting updateTextRoleList...");

	}

	private void updateRolePriority(HashMap<String, ArrayList<RoleEntry>> params) throws Exception, SQLException {
		log.debug("Entering updateRolePriority...");

		ArrayList<RoleEntry> roleList = params.get("roleList");

		List<String> RoleIDS = new ArrayList<String>();
		for (RoleEntry role : roleList) {
			RoleIDS.add(role.getRoleID());
		}
		String roleIds = StringUtils.join(RoleIDS, ",");
		log.debug("RoleIDs : " + roleIds);

		ResultSet rs = stat.executeQuery("SELECT RoleID " + "FROM RolePriority " + "WHERE RoleID NOT IN (" + roleIds + ");");
		String delRoleIds = "";
		while ((rs != null) && rs.next()) {
			delRoleIds = delRoleIds + (delRoleIds.equals("") ? "" : ",") + rs.getString("RoleID");
		}
		initFirstStatement();
		if (!delRoleIds.equals("")) {
			stat.addBatch("DELETE FROM RoleTextMap " + "WHERE RoleID IN(" + delRoleIds + ");");
			stat.addBatch("DELETE FROM AssistText " + "WHERE TextID NOT IN ( " + "SELECT TextID " + "FROM RoleTextMap " + ");");
		}
		stat.addBatch("DELETE FROM RolePriority;");

		for (RoleEntry role : roleList) {
			log.debug("updateRolePriority Role: " + role.getRole());

			stat.addBatch("INSERT INTO RolePriority (RoleID, Role, Priority,fontcolor,background) " + "VALUES (" + role.getRoleID() + ",'" + role.getRole().replace("'", "''") + "'," + role.getPriority() + ",'" + role.getFontColor() + "','" + role.getBackgroundColor() + "' );");

			log.debug("updateRolePriority: INSERT INTO RolePriority (RoleID, Role, Priority,fontcolor,background) " + "VALUES (" + role.getRoleID() + ",'" + role.getRole().replace("'", "''") + "'," + role.getPriority() + ",'" + role.getFontColor() + "','" + role.getBackgroundColor() + "' );");
		}
		stat.executeBatch();
		log.debug("Exiting updateRolePriority...");

	}

	private void insertConfig(HashMap params) throws SQLException, Exception {
		log.debug("Entering insertConfig.. ");
		String key = (String) params.get("key");
		String value = (String) params.get("value");
		String configVal = null;
		if (key.equals("accessType")) {
			ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'accessType'");
			while ((rs != null) && rs.next()) {
				configVal = rs.getString("ConfigVal");
			}
		}

		if (configVal == null) {
			log.info("Changing Access Type..");
		}
		String insertConfigQry = "";
		if (conn != null) {
			// for single SQL operation, preferring Statement over Prepared
			// Statement
			insertConfigQry = "INSERT INTO Configurations (ConfigKey,ConfigVal) VALUES ('" + key + "','" + value + "')";
			log.debug("insertConfig: INSERT INTO Configurations (ConfigKey,ConfigVal) VALUES ('" + key + "','" + value + "')");
			stat.executeUpdate(insertConfigQry);
		}
		if (configVal == null) {
			log.info("Access Type Changed..");
		}
		log.debug("Exiting insertConfig.. ");
	}

	private Map<String, String> getConfigByKey(HashMap params) throws SQLException, Exception {
		log.debug("Entering getConfigByKey.. ");

		String value = "";
		String key = (String) params.get("key");
		String selectConfigQry = "";
		ResultSet rs;
		HashMap<String, String> result = new HashMap<String, String>();
		if (conn != null) {
			// for single SQL operation, preferring Statement over Prepared
			// Statement
			selectConfigQry = "SELECT ConfigVal FROM Configurations WHERE ConfigKey='" + key + "'";
			log.debug("getConfigByKey: SELECT ConfigVal FROM Configurations WHERE ConfigKey='" + key + "'");
			rs = stat.executeQuery(selectConfigQry);
			if (rs != null && rs.next()) {
				value = rs.getString("ConfigVal");
			}

		}
		result.put("value", value);

		log.debug("Configuration: " + result.toString());

		log.debug("Exiting getConfigByKey.. ");

		return result;
	}

	private void updateConfigurations(HashMap<?, ?> prams) throws Exception, SQLException {
		log.debug("Entering updateConfigurations.. ");

		String[] configs = (String[]) prams.get("configs");
		String[] params;
		String configVal = null;
		for (int i = 0; i < configs.length && configVal == null; i++) {
			params = configs[i].split("=", 2);
			if (params[0].equals("accessType")) {
				ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'accessType'");
				while ((rs != null) && rs.next()) {
					configVal = rs.getString("ConfigVal");
				}
			}
		}
		if (configVal == null) {
			log.info("Changing Access Type..");
		}
		prep = conn.prepareStatement("UPDATE Configurations SET ConfigVal=? WHERE ConfigKey=?");
		for (String config : configs) {
			params = config.split("=", 2);
			log.debug("Update Configuration Parameters: " + "[" + params[1] + "] , [" + params[0] + "]");
			prep.setString(1, params[1]);
			prep.setString(2, params[0]);
			prep.addBatch();
		}
		prep.executeBatch();

		if (configVal == null) {
			log.info("Access Type Changed..");
		}
		log.debug("Exiting updateConfigurations.. ");
		AgileHandler.disconnect();

	}

	void updateDefaultRolePriority(HashMap params) throws SQLException, Exception {
		log.debug("Entering updateDefaultRolePriority.. ");

		String newRole = (String) params.get("newRole");
		String query = "";
		int res;

		query = "UPDATE RolePriority set Role = '" + newRole.replace("'", "''") + "' WHERE RoleID =0";
		log.debug("updateDefaultRolePriority: UPDATE RolePriority set Role = '" + newRole.replace("'", "''") + "' WHERE RoleID =0");
		res = stat.executeUpdate(query);

		log.info("Role Priority updated=[" + res + "]");

		log.debug("Exiting updateDefaultRolePriority.. ");

	}

	private void updateLicinfo(HashMap params) throws Exception, SQLException {
		log.debug("Entering updateLicinfo.. ");

		String licinfo = (String) params.get("licinfo");
		stat.addBatch("DELETE FROM Configurations WHERE ConfigKey='LNFO';");
		if (licinfo != null) {
			stat.addBatch("INSERT INTO Configurations (ConfigKey, ConfigVal) " + "VALUES('LNFO','" + licinfo.replace("'", "''") + "')");
			log.debug("updateLicinfo: INSERT INTO Configurations (ConfigKey, ConfigVal) " + "VALUES('LNFO','" + licinfo.replace("'", "''") + "')");
		}
		stat.executeBatch();
		log.debug("Exiting updateLicinfo.. ");

	}

	private Map<String, String> getLicInfo() throws Exception, SQLException {
		log.debug("Entering getLicInfo.. ");

		String strLic = null;
		HashMap<String, String> retMap = new HashMap<String, String>();
		ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'LNFO'");
		while ((rs != null) && rs.next()) {
			strLic = rs.getString("ConfigVal");
		}
		retMap.put("strLic", strLic);
		log.debug("Exiting getLicInfo.. ");

		return retMap;
	}

	private void deleteAssistColor(HashMap params) throws SQLException, Exception {
		log.debug("Entering deleteAssistColor...");

		String colorID = (String) params.get("colorID");
		stat.addBatch("DELETE FROM AssistColor WHERE ColorID=" + colorID + ";");
		log.debug("deleteAssistColor: DELETE FROM AssistColor WHERE ColorID=" + colorID + ";");
		stat.executeBatch();
		log.debug("Exiting deleteAssistColor...");
	}

	private void deleteRolePriority(HashMap params) throws SQLException, Exception {
		log.debug("Entering deleteRolePriority...");

		String roleID = (String) params.get("roleID");
		stat.addBatch("DELETE FROM RolePriority WHERE RoleID=" + roleID + ";");
		log.debug("deleteRolePriority: DELETE FROM RolePriority WHERE RoleID=" + roleID + ";");
		stat.executeBatch();
		log.debug("Exiting deleteRolePriority...");
	}

	private void mergeRolePriority(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering mergeRolePriority...");

		List<RoleEntry> roleList = (List<RoleEntry>) params.get("role");

		List<String> roleIDList = (List<String>) params.get("roleIDList");

		String updateQuery = "UPDATE RolePriority SET  Role=?, Priority=?, fontcolor=?, background=? WHERE RoleID=?";
		prep = conn.prepareStatement(updateQuery);
		String insertQuery = "INSERT into RolePriority (RoleID, Role, Priority, fontcolor, background ) VALUES (?,?,?,?,?)";
		prep1 = conn.prepareStatement(insertQuery);
		for (int i = 0; i < roleList.size(); i++) {
			if (roleIDList.contains(roleList.get(i).getRoleID())) {
				prep.setString(1, roleList.get(i).getRole());
				prep.setString(2, String.valueOf(roleList.get(i).getPriority()));
				prep.setString(3, roleList.get(i).getFontColor());
				prep.setString(4, roleList.get(i).getBackgroundColor());
				prep.setString(5, roleList.get(i).getRoleID());
				log.debug("updatetQuery: " + updateQuery);
				prep.addBatch();
			} else {
				prep1.setString(1, roleList.get(i).getRoleID());
				prep1.setString(2, roleList.get(i).getRole());
				prep1.setString(3, String.valueOf(roleList.get(i).getPriority()));
				prep1.setString(4, roleList.get(i).getFontColor());
				prep1.setString(5, roleList.get(i).getBackgroundColor());

				log.debug("insertQuery: " + insertQuery);
				prep1.addBatch();
			}
		}
		prep.executeBatch();
		prep1.executeBatch();

		log.debug("Exiting mergeRolePriority...");

	}

	private Map<String, List<AssistTextEntry>> getAssistTextMap() throws SQLException, Exception {
		log.debug("Entering getAssistTextMap.. ");
		HashMap<String, List<AssistTextEntry>> dbAssistTextList = new HashMap<String, List<AssistTextEntry>>();

		List<AssistTextEntry> textList = new ArrayList<AssistTextEntry>();

		ResultSet rset = stat.executeQuery("SELECT AssistText.TextID,ClassID,AttrID,workflowID,workflowStatusID FROM AssistText");
		while (rset.next()) {
			AssistTextEntry text = new AssistTextEntry();
			text.setTextID(rset.getObject("TextID").toString());
			text.setAttrID(rset.getObject("AttrID").toString());
			text.setClassID(rset.getObject("ClassID").toString());
			text.setWorkflowID(rset.getObject("workflowID").toString());
			text.setWorkflowStatusID(rset.getObject("workflowStatusID").toString());
			textList.add(text);
		}
		dbAssistTextList.put("textList", textList);

		log.debug("Exiting getAssistTextMap.. ");
		return dbAssistTextList;

	}

	private Map<String, List<String>> getRoleIDs() throws SQLException, Exception {
		log.debug("Entering getRoleIDs.. ");
		HashMap<String, List<String>> roleIds = new HashMap<String, List<String>>();
		ResultSet rset = stat.executeQuery("SELECT RoleID FROM RolePriority");

		List<String> roleIDList = new ArrayList<String>();
		while ((rset != null) && rset.next()) {
			roleIDList.add(rset.getString("RoleID"));
		}
		roleIds.put("roleIDList", roleIDList);
		log.debug("Exiting getRoleIDs.. ");
		return roleIds;
	}

	private Map<String, List<String>> getColorIDs() throws SQLException, Exception {
		log.debug("Entering getColorIDs.. ");

		ResultSet rset = stat.executeQuery("SELECT ColorID FROM AssistColor");
		HashMap<String, List<String>> colorIds = new HashMap<String, List<String>>();
		List<String> colorIDList = new ArrayList<String>();
		while ((rset != null) && rset.next()) {
			colorIDList.add(rset.getString("ColorID"));
		}
		colorIds.put("colorIDList", colorIDList);
		log.debug("Exiting getColorIDs.. ");

		return colorIds;
	}

	private void mergeAssistColor(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering mergeAssistColor...");

		List<AssistColorEntry> colorList = (List<AssistColorEntry>) params.get("color");
		List<String> colorIDList = (List<String>) params.get("colorIDList");

		String updateQuery = "UPDATE AssistColor SET  ClassID=?, AttrID=?, AssistColor=? WHERE ColorID=?";
		prep = conn.prepareStatement(updateQuery);
		String insertQuery = "INSERT into AssistColor (ColorID, ClassID, AttrID, AssistColor ) VALUES (?,?,?,?)";
		prep1 = conn.prepareStatement(insertQuery);
		for (int i = 0; i < colorList.size(); i++) {
			if (colorIDList.contains(colorList.get(i).getColorId())) {
				prep.setString(1, colorList.get(i).getClassId());
				prep.setString(2, colorList.get(i).getAttributeId());
				prep.setString(3, colorList.get(i).getAssistColor());
				prep.setString(4, colorList.get(i).getColorId());
				log.debug("updatetQuery: " + updateQuery);
				prep.addBatch();
			} else {
				prep1.setString(1, colorList.get(i).getColorId());
				prep1.setString(2, colorList.get(i).getClassId());
				prep1.setString(3, colorList.get(i).getAttributeId());
				prep1.setString(4, colorList.get(i).getAssistColor());

				log.debug("insertQuery: " + insertQuery);
				prep1.addBatch();
			}
		}
		prep.executeBatch();
		prep1.executeBatch();
		log.debug("Exiting mergeAssistColor...");

	}

	private Map<String, List<String>> getTextRoles() throws SQLException, Exception {
		log.debug("Entering getTextRoles...");

		ResultSet rset = stat.executeQuery("SELECT TextID,RoleID FROM RoleTextMap");
		HashMap<String, List<String>> textRoles = new HashMap<String, List<String>>();

		while (rset.next()) {
			if (textRoles.containsKey(rset.getObject("TextID").toString())) {
				textRoles.get(rset.getObject("TextID").toString()).add(rset.getObject("RoleID").toString());
			} else {
				List<String> roles = new ArrayList<String>();
				roles.add(rset.getObject("RoleID").toString());
				textRoles.put(rset.getObject("TextID").toString(), roles);
			}
		}
		log.debug("Exiting getTextRoles...");

		return textRoles;
	}

	private void mergeUpdateText(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering mergeUpdateText...");
		HashMap<String, AssistTextEntry> updateText = (HashMap<String, AssistTextEntry>) params.get("updateText");
		String updateQuery = "UPDATE AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?, DateCreated=?, LastUpdated=? WHERE TextID=? ";

		prep = conn.prepareStatement(updateQuery);
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();

		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());

		String timeStamp = simpleDateFormatter.format(today);
		log.debug("TimeStamp: " + timeStamp);

		for (String Id : updateText.keySet()) {
			prep.setString(1, StringEscapeUtils.unescapeXml(updateText.get(Id).getAssistText()));
			prep.setString(2, updateText.get(Id).getFontColor());
			prep.setString(3, updateText.get(Id).getBackgroundColor());
			prep.setBoolean(4, updateText.get(Id).getIsDifferentColor());
			prep.setString(5, updateText.get(Id).getDateCreated());
			prep.setString(6, timeStamp);
			prep.setString(7, Id);
			prep.addBatch();
			log.debug("updatetQuery: " + Id);
		}
		prep.executeBatch();
		log.debug("Exiting mergeUpdateText...");

	}

	private void mergeInsertText(HashMap<String, Object> params) throws SQLException, Exception {

		log.debug("Entering mergeInsertText...");
		HashMap<String, AssistTextEntry> insertText = (HashMap<String, AssistTextEntry>) params.get("insertText");
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();

		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());

		String timeStamp = simpleDateFormatter.format(today);
		log.debug("TimeStamp: " + timeStamp);

		String[] keys = new String[insertText.size()];
		int index = 0;
		for (Map.Entry<String, AssistTextEntry> mapEntry : insertText.entrySet()) {
			keys[index] = mapEntry.getKey();
			log.debug(keys[index]);
			index++;
		}
		int maxId = 0;
		String InsertNew = null;

		String insertQuery = "INSERT into AssistText(ClassID, AttrID, AssistText, fontcolor, background, isDiffColor, workflowID, workflowStatusID, DateCreated,LastUpdated) VALUES (?,?,?,?,?,?,?,?,?,?)";

		prep = conn.prepareStatement(insertQuery);
		if (insertText.size() != 0) {
			prep.setString(1, insertText.get(keys[0]).getClassID());
			prep.setString(2, insertText.get(keys[0]).getAttrID());
			prep.setString(3, StringEscapeUtils.unescapeXml(insertText.get(keys[0]).getAssistText()));
			prep.setString(4, insertText.get(keys[0]).getFontColor());
			prep.setString(5, insertText.get(keys[0]).getBackgroundColor());
			prep.setBoolean(6, insertText.get(keys[0]).getIsDifferentColor());
			prep.setString(7, insertText.get(keys[0]).getWorkflowID());
			prep.setString(8, insertText.get(keys[0]).getWorkflowStatusId());
			prep.setString(9, insertText.get(keys[0]).getDateCreated());
			prep.setString(10, timeStamp);
			prep.executeUpdate();

			String maxQuery = "Select MAX(TextID) As maxId FROM AssistText";
			ResultSet rs = stat.executeQuery(maxQuery);
			if ((rs != null) && rs.next()) {
				log.debug(rs.getObject("maxId").toString());
				maxId = Integer.parseInt(rs.getObject("maxId").toString());
			}
		}

		prep = conn.prepareStatement(insertQuery);
		for (int i = 1; i < keys.length; i++) {
			prep.setString(1, insertText.get(keys[i]).getClassID());
			prep.setString(2, insertText.get(keys[i]).getAttrID());
			prep.setString(3, StringEscapeUtils.unescapeXml(insertText.get(keys[i]).getAssistText()));
			prep.setString(4, insertText.get(keys[i]).getFontColor());
			prep.setString(5, insertText.get(keys[i]).getBackgroundColor());
			prep.setBoolean(6, insertText.get(keys[i]).getIsDifferentColor());
			prep.setString(7, insertText.get(keys[i]).getWorkflowID());
			prep.setString(8, insertText.get(keys[i]).getWorkflowStatusId());
			prep.setString(9, insertText.get(keys[i]).getDateCreated());
			prep.setString(10, timeStamp);
			prep.addBatch();
		}
		prep.executeBatch();

		for (int i = 0; i < keys.length; i++) {
			for (int j = 0; j < insertText.get(keys[i]).getRolesList().size(); j++) {
				log.debug("INSERT INTO RoleTextMap (TextID,RoleID) VALUES ('" + maxId + "','" + insertText.get(keys[i]).getRolesList().get(j) + "')");
				stat.addBatch("INSERT INTO RoleTextMap (TextID,RoleID) VALUES ('" + maxId + "','" + insertText.get(keys[i]).getRolesList().get(j) + "')");
			}
			maxId++;
		}
		stat.executeBatch();
		log.debug("Exiting mergeInsertText...");

	}

	private Map<String, Boolean> isRoles() throws SQLException, Exception {
		log.debug("Entering isRoles...");

		HashMap<String, Boolean> rolesStatus = new HashMap<String, Boolean>();
		boolean isRoles = true;

		prep = conn.prepareStatement("SELECT ConfigVal FROM Configurations WHERE ConfigKey=?");
		prep.setString(1, "accessType");
		ResultSet rset2 = prep.executeQuery();
		while (rset2.next()) {
			if (rset2.getObject("ConfigVal").toString().equals("roles")) {
				isRoles = true;
			} else {
				isRoles = false;
			}
		}

		rolesStatus.put("isRoles", isRoles);
		log.debug("Exiting isRoles...");
		return rolesStatus;
	}

	private HashMap<String, List<AssistColorEntry>> getAssistColor() throws SQLException, Exception {
		log.debug("Entering getAssistColor.. ");

		HashMap<String, List<AssistColorEntry>> colorList = new HashMap<String, List<AssistColorEntry>>();
		List<AssistColorEntry> AssistColorList = new ArrayList<AssistColorEntry>();
		ResultSet rset = stat.executeQuery("SELECT * FROM AssistColor");
		while (rset.next()) {
			AssistColorEntry color = new AssistColorEntry();
			color.setColorId(rset.getObject("ColorID").toString());
			color.setClassId(rset.getObject("ClassID").toString());
			color.setAttributeId(rset.getObject("AttrID").toString());
			color.setAssistColor(rset.getString("AssistColor"));
			AssistColorList.add(color);

		}

		colorList.put("assistColor", AssistColorList);
		log.debug("Exiting getAssistColor.. ");

		return colorList;

	}

	private HashMap<String, List<RoleEntry>> getRolePriority() throws SQLException, Exception {
		log.debug("Entering getRolePriority.. ");

		HashMap<String, List<RoleEntry>> roleList = new HashMap<String, List<RoleEntry>>();
		List<RoleEntry> RolePriorityList = new ArrayList<RoleEntry>();
		ResultSet rset = stat.executeQuery("SELECT * FROM RolePriority");
		while (rset.next()) {
			RoleEntry role = new RoleEntry();
			role.setRoleID(rset.getObject("RoleID").toString());
			role.setRole(rset.getString("Role"));
			role.setPriority(Integer.parseInt(rset.getObject("Priority").toString()));
			role.setFontColor(rset.getString("fontcolor"));
			role.setBackgroundColor(rset.getString("background"));
			RolePriorityList.add(role);

		}
		roleList.put("rolePriority", RolePriorityList);
		log.debug("Exiting getRolePriority.. ");

		return roleList;

	}

	private HashMap<String, String> getOptOutUsers() throws SQLException, Exception {
		log.debug("Entering getOptOutUsers.. ");

		HashMap<String, String> usersList = new HashMap<String, String>();
		ResultSet rset = stat.executeQuery("SELECT * FROM OptOutUsers");
		while (rset.next()) {
			String userid = "";
			try {
				userid = rset.getObject("userid").toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return usersList;
			}
			String optoutdate = "";
			try {
				optoutdate = rset.getObject("optoutdate").toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				optoutdate = "Not Available";
			}
			usersList.put(userid, optoutdate);

		}
		log.debug("Exiting getOptOutUsers.. ");

		return usersList;

	}

	private HashMap<String, List<AssistTextEntry>> getAssistText() throws Exception, SQLException {
		log.debug("Entering getAssistText.. ");

		HashMap<String, List<String>> textRoles = (HashMap<String, List<String>>) handleDBRequest("getTextRoles", null, false);

		HashMap<String, List<AssistTextEntry>> textList = new HashMap<String, List<AssistTextEntry>>();
		List<AssistTextEntry> AssistTextList = new ArrayList<AssistTextEntry>();

		HashMap<String, String> className = new HashMap<String, String>();
		HashMap<String, String> attrName = new HashMap<String, String>();
		initThirdStatement();
		UIListHandler objUIListHandler = new UIListHandler(this);
		ResultSet rset = stat2.executeQuery("SELECT * FROM AssistText");
		while (rset.next()) {
			AssistTextEntry text = new AssistTextEntry();
			String textID = rset.getObject("TextID").toString();
			text.setTextID(textID);
			String assisttext = rset.getString("AssistText");
			text.setAssistText(StringEscapeUtils.escapeXml(assisttext));
			text.setAttrID(rset.getObject("AttrID").toString());
			text.setBackgroundColor(rset.getString("background"));
			text.setClassID(rset.getObject("ClassID").toString());
			if (rset.getString("DateCreated") != null) {
				text.setDateCreated(rset.getString("DateCreated"));
			} else {
				text.setDateCreated("");
			}
			text.setFontColor(rset.getString("fontcolor"));
			text.setIsDifferentColor(rset.getBoolean("isDiffColor"));
			text.setWorkflowID(rset.getString("workflowID"));
			text.setWorkflowStatusID(rset.getString("workflowStatusID"));
			String classID = rset.getObject("ClassID").toString();
			String attrID = rset.getObject("AttrID").toString();

			if (!className.containsKey(classID) || !attrName.containsKey(attrID)) {
				int attid = -1;
				try {
					attid = Integer.parseInt(rset.getObject("AttrID").toString());
				} catch (Exception ex) {
					log.info("a workflow");
					attid = -1;
				}
				List<String> names = objUIListHandler.getClassAttributeName(Integer.parseInt(rset.getObject("ClassID").toString()), attid);
				text.setClassName(names.get(0));
				text.setAtrrName(names.get(1));
				if (!className.containsKey(classID)) {
					className.put(classID, names.get(0));
				}
				if (!attrName.containsKey(attrID)) {
					attrName.put(attrID, names.get(1));
				}
			} else {
				text.setClassName(className.get(classID));
				text.setAtrrName(attrName.get(attrID));
			}
			List<String> rolesList = new ArrayList<String>();
			if (textRoles.containsKey(textID)) {
				for (int k = 0; k < textRoles.get(textID).size(); k++) {
					rolesList.add(textRoles.get(textID).get(k));
				}
			}
			text.setRolesList(rolesList);
			AssistTextList.add(text);
		}

		textList.put("assistText", AssistTextList);
		log.debug("Exiting getAssistText.. ");

		return textList;

	}

	private Map<String, Integer> authenticate(HashMap<String, String> params) throws Exception, SQLException {
		log.debug("Entering authenticate.. ");

		int uid = -1;
		String userName = params.get("userName");
		String password = params.get("password");
		HashMap result = new HashMap<String, Integer>();
		prep = conn.prepareStatement("SELECT UserId " + "FROM User " + "WHERE UserName=? AND Pwd=?");
		prep.setString(1, userName);
		prep.setString(2, password);
		ResultSet rs = prep.executeQuery();
		if ((rs != null) && rs.next()) {
			uid = rs.getInt("UserId");
			log.debug("UserId: " + uid);
		}
		result.put("userID", uid);
		log.debug("Exiting authenticate.. ");

		return result;
	}

	private Map<String, Boolean> ChangePassword(HashMap<String, String> map) throws SQLException, Exception {
		log.debug("Entering ChangePassword.. ");

		boolean status = false;
		HashMap<String, Boolean> statusMap = new HashMap<String, Boolean>();
		String uid = map.get("uid");
		String cpwd = map.get("cpwd");
		String npwd = map.get("npwd");

		prep = conn.prepareStatement("UPDATE User " + "SET Pwd=? " + "WHERE UserId=? AND Pwd=?");
		prep.setString(1, npwd);
		prep.setString(2, uid);
		prep.setString(3, cpwd);
		int cnt = prep.executeUpdate();
		if (cnt == 1) {
			status = true;
		}
		statusMap.put("status", status);
		log.debug("Exiting ChangePassword.. ");

		return statusMap;
	}

	private void initFirstStatement() throws SQLException, Exception {
		stat = conn.createStatement();
	}

	private void initSecondStatement() throws SQLException, Exception {
		stat1 = conn.createStatement();
	}

	private void initThirdStatement() throws SQLException, Exception {
		stat2 = conn.createStatement();
	}

	private void releaseResources(boolean autoCommit) throws SQLException, Exception {
		log.debug("Entering releaseResources..");
		if (stat != null) {
			stat.close();
		}
		if (stat1 != null) {
			stat1.close();
		}
		if (prep != null) {
			prep.close();
		}
		if (prep1 != null) {
			prep1.close();
		}
		if (autoCommit && conn != null && !conn.getAutoCommit()) {
			conn.rollback();
			conn.setAutoCommit(true);
		}
		log.debug("Exiting releaseResources..");

	}

	private Map<String, String> getUserName() throws Exception, SQLException {
		log.debug("Entering getUserName.. ");
		String userName = null;
		HashMap result = new HashMap<String, String>();
		prep = conn.prepareStatement("SELECT UserName FROM User;");
		ResultSet rs = prep.executeQuery();
		if ((rs != null) && rs.next()) {
			userName = rs.getString("UserName");
			log.debug("UserName: " + userName);
		}
		result.put("userName", userName);
		log.debug("Exiting getUserName.. ");

		return result;
	}

	private void addOptOutUser(HashMap<String, String> params) throws Exception, SQLException {
		String userid = params.get("userid");
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();

		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());

		String timeStamp = simpleDateFormatter.format(today);
		prep = conn.prepareStatement("INSERT INTO OptOutUsers (userid,optoutdate) Values (?,?);");

		prep.setString(1, userid);
		prep.setString(2, timeStamp);
		prep.executeUpdate();
	}

	private void removeOptOutUser(HashMap<String, String> params) throws Exception, SQLException {
		String userid = params.get("userid");
		prep = conn.prepareStatement("DELETE FROM OptOutUsers " + "WHERE userid=?");
		prep.setString(1, userid);
		prep.executeUpdate();

	}

	public static void main(String[] args) throws Exception {
		DBHandler dbh = new DBHandler();
		HashMap params = new HashMap<String, String>();
		params.put("userid", "admin");
		dbh.handleDBRequest("getOptOutUser", params, false);
	}

	private HashMap<String, Boolean> getOptOutUser(HashMap<String, String> params) throws Exception, SQLException {
		String userid = params.get("userid");
		HashMap result = new HashMap<String, Boolean>();
		prep = conn.prepareStatement("SELECT userid " + "FROM OptOutUsers " + "WHERE userid=?");
		prep.setString(1, userid);
		ResultSet rs = prep.executeQuery();
		if (rs != null && rs.next()) {
			result.put("isOptedOut", true);
		} else {
			result.put("isOptedOut", false);
		}
		return result;

	}

	public HashMap<?, ?> handleDBRequest(String funcName, HashMap<?, ?> params, boolean autoCommit) {
		log.debug("Entering handleDBRequest.. ");

		HashMap<?, ?> ret = null;
		try {
			initFirstStatement();
			if (autoCommit) {
				if (conn.getAutoCommit()) {
					conn.setAutoCommit(false);
				}
			}
			Class<?> c = Class.forName("com.xavor.plmxl.assist.Handler.DBHandler");
			Method method = null;
			Object result = null;
			if (params != null) {
				method = c.getDeclaredMethod(funcName, HashMap.class);
				Object[] parameters = new Object[1];
				parameters[0] = params;
				result = method.invoke(this, parameters);
				if (autoCommit) {
					if (!conn.getAutoCommit()) {
						conn.setAutoCommit(true);
					}
				}

			} else {
				method = c.getDeclaredMethod(funcName, null);
				result = method.invoke(this, (Object[]) null);
				if (autoCommit) {
					if (!conn.getAutoCommit()) {
						conn.setAutoCommit(true);
					}
				}
			}

			if (result != null) {
				ret = (HashMap<?, ?>) result;
			}

		} catch (SQLException e) {

			try {
				releaseResources(true);
			} catch (SQLException e2) {
				log.error("SQL Exception: ", e2);
			} catch (Exception e2) {
				log.error("Exception: ", e2);
			}
			if (e.getMessage().toLowerCase().contains("lock")) {
				log.error(Constants.DB.MSG_DB_LOCKED, e);
			}
			if (e.getMessage().contains("SQL logic error")) {
				log.error(Constants.DB.MSG_DB_ERROR, e);
			}
			if (e.getMessage().toLowerCase().contains("busy")) {
				log.error(Constants.DB.MSG_DB_BUSY, e);
			}
			if (e.getMessage().toLowerCase().contains("nested")) {
				log.error(Constants.DB.MSG_DB_BUSY, e);
				try {
					releaseResources(true);
				} catch (SQLException e1) {
					log.error("SQL Exception: ", e1);
				} catch (Exception e1) {
					log.error("Exception: ", e1);
				}
			}

		} catch (Exception e) {
			try {
				releaseResources(true);
			} catch (SQLException e2) {
				log.error("SQL Exception: ", e2);
			} catch (Exception e2) {
				log.error("Exception: ", e2);
			}
			log.error("Exception: ", e);
			if (e.getMessage().toLowerCase().contains("nested")) {

				log.debug("Nested");
				log.error(Constants.DB.MSG_DB_BUSY, e);
				try {
					releaseResources(true);
				} catch (SQLException e1) {
					log.error("SQL Exception: ", e1);
				} catch (Exception e1) {
					log.error("Exception: ", e1);
				}
			}
		} finally {
			try {
				releaseResources(autoCommit);
			} catch (SQLException e1) {

				log.error("SQLException: ", e1);
			} catch (Exception e) {

				log.error("Exception: ", e);
			}
		}
		log.debug("Exiting handleDBRequest.. ");
		return ret;
	}

}
