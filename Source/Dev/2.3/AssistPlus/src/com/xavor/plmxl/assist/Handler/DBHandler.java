package com.xavor.plmxl.assist.Handler;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAttribute;
import com.xavor.plmxl.assist.DO.AssistColorEntry;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class DBHandler {
	private static Connection conn = null;
	
	private PreparedStatement prep = null;
	private PreparedStatement prep1 = null;
	private PreparedStatement prep2 = null;
	private PreparedStatement prep3 = null;
	private PreparedStatement prep4 = null;

	private AssistLogger log = AssistLogger.getInstance();
	
	private static boolean isOracleDb = false;

	public DBHandler() throws SQLException, Exception {
		initializeConnection();
	}
	
	public Map<?, ?> handleDBRequest(String funcName, Map<?, ?> params, boolean autoCommit) {
		log.debug("Entering handleDBRequest.. ");

		Map<?, ?> ret = null;
		try {
			if (autoCommit) {
				if (!conn.getAutoCommit()) {
					conn.setAutoCommit(true);
				}
			} else {
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
			} else {
				method = c.getDeclaredMethod(funcName);
				result = method.invoke(this, (Object[]) null);
			}

			if (result != null) {
				ret = (Map<?, ?>) result;
			}
		} catch (Exception ex) {
			autoCommit = true;
			log.error("Exception: ", ex);
		} finally {
			try {
				releaseResources(autoCommit);
			} catch (Exception e) {
				log.error("Exception while releasing Resources: ", e);
			}
		}
		
		log.debug("Exiting handleDBRequest...");
		return ret;
	}

	private void init() throws Exception, SQLException {
		log.debug("Entering init...");
		try {
			String homePath = ConfigHelper.getAppHomePath();
			Map<String, String> prop = ConfigHelper.getDbProperties();
			String dbUser = prop.get("dbUser");
			String dbPass = prop.get("dbPwd");
			String driver = prop.get("driver");
			String connstring = prop.get("connstring");
			String dbServer = prop.get("dbServer");
			
			Class.forName(driver);
			String url = connstring;
			if(connstring.contains("sqlite")) {
				url += homePath + Constants.DB.DB_NAME;
				conn = DriverManager.getConnection(url);
			} else if(connstring.contains("oracle")) {
				url += dbServer;
				conn = DriverManager.getConnection(url, dbUser, dbPass);
				isOracleDb = true;
			} else {
				throw new Exception(Constants.DB.MSG_DB_CONNSTRING_INVALID);
			}
			
			log.info("DB connection created");
		} catch (ClassNotFoundException e) {
			log.error("ClassNotFoundException in init(): ", e);
			throw new Exception(Constants.DB.MSG_DRIVER_NOT_INSTALLED);
		}
		log.debug("Exiting init...");
	}
	
	private void releaseResources(boolean autoCommit) throws SQLException {
		log.debug("Entering releaseResources...");
		if (prep != null) {
			try {
				prep.close();
			} catch(Throwable th) {}
		}
		if (prep1 != null) {
			try {
				prep1.close();
			} catch(Throwable th) {}
		}
		if (prep2 != null) {
			try {
				prep2.close();
			} catch(Throwable th) {}
		}
		if (prep3 != null) {
			try {
				prep3.close();
			} catch(Throwable th) {}
		}
		if (prep4 != null) {
			try {
				prep4.close();
			} catch(Throwable th) {}
		}
		
		if (autoCommit && conn != null && !conn.getAutoCommit()) {
			conn.rollback();
			conn.setAutoCommit(true);
		}
		log.debug("Exiting releaseResources...");
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
	@SuppressWarnings({"unused", "unchecked"})
	private Map<String, ArrayList<String>> getAssistInfoMap(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering getAssistInfoMap...");
		
		String classes = (String) params.get("classes");
		Set<String> roles = (Set<String>) params.get("roles");
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
		String clsstr = classes;

		roles.add(allRoleKey);
		String prepMultiRolesParam = generateQsForIn(roles.size());
		
		String classesArray[] = clsstr.split(",");
		String prepMultiClassIDParam = "";
		if (classesArray != null) {
			prepMultiClassIDParam = generateQsForIn(classesArray.length);
		}

		String quer = "SELECT AttrP.ClassID,AttrP.AttrID,AssistText.AssistText,case when AssistText.isDiffColor <> 0 then AssistText.fontcolor else RolePriority.fontcolor end as fontcolor,case when AssistText.isDiffColor <> 0 then AssistText.background else RolePriority.background end as background,AssistText.LastUpdated "
				+ "FROM (SELECT ClassID,AttrID,MAX(Priority) Priority "
				+ "FROM RoleTextMap INNER JOIN RolePriority ON RoleTextMap.RoleID = RolePriority.RoleID INNER JOIN AssistText ON RoleTextMap.TextID = AssistText.TextID "
				+ "WHERE Role IN ("	+ prepMultiRolesParam + ") "
				+ "AND ClassID IN (" + prepMultiClassIDParam + ") "
				+ "AND (workflowId IN (?,?,?) OR workflowId IS NULL) "
				+ "AND (workflowStatusID like ? OR workflowStatusID IS NULL OR workflowStatusID=? OR workflowStatusID=?) "
				+ "GROUP BY ClassID, AttrID) AttrP "
				+ "INNER JOIN AssistText "
				+ "ON AttrP.AttrID = AssistText.AttrID "
				+ "AND AttrP.ClassID = AssistText.ClassID "
				+ "INNER JOIN RolePriority "
				+ "ON AttrP.Priority = RolePriority.Priority "
				+ "INNER JOIN RoleTextMap "
				+ "ON RoleTextMap.RoleID = RolePriority.RoleID AND RoleTextMap.TextID = AssistText.TextID "
				+ "WHERE (workflowId IN (?,?,?) OR workflowId IS NULL) "
				+ "AND (workflowStatusID like ? OR workflowStatusID IS NULL OR workflowStatusID=? OR workflowStatusID=?)";

		log.debug(" getAssistInfoMap query=[" + quer + "]");

		prep = prepareStatement(quer);
		int i = 1;
		
		for (String role : roles) {
			prep.setString(i++, role);
		}
		
		for (int y = 0; y < classesArray.length; y++) {
			prep.setInt(i++, Integer.parseInt(classesArray[y].trim()));
		}

		prep.setString(i++, workflowID);
		prep.setString(i++, "All Workflows");
		prep.setString(i++, "");
		
		String wfStatusLikeParam = "%" + workflowSTatusID + "%";
		prep.setString(i++, wfStatusLikeParam);
		prep.setString(i++, "All Statuses");
		prep.setString(i++, "");
		
		prep.setString(i++, workflowID);
		prep.setString(i++, "All Workflows");
		prep.setString(i++, "");
		
		prep.setString(i++, wfStatusLikeParam);
		prep.setString(i++, "All Statuses");
		prep.setString(i++, "");

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
		
		//Get default Configurations
		Map<String, String> configs = readConfigurations();
		final String FONT_COLOR = configs.get("fontColor");
		final String BACKGROUND_COLOR = configs.get("backgroundColor");
		final String HOVER_COLOR = configs.get("hoverColor");
		final String INDICATE_NEW_TEXT = configs.get("indicateNewText");
		final String TEXT_DURATION = configs.get("textDuration");
		
		
		HashMap<String, ArrayList<String>> asInfo = new HashMap<String, ArrayList<String>>();
		List<String> attrIdsForSubclass = new ArrayList<String>();
		for (infoMapBean bean : infomapRecords) {
			String attrId = bean.getAttrId();
			String assistText = bean.getAssistText();
			String fontcolor = bean.getFontcolor();
			String background = bean.getBackground();
			String lastUpdated = bean.getLastUpdated();
			String classId = bean.getClassId();
			log.debug("Class Id: " + classId + ", Attribute Id: " + attrId);

			ArrayList<String> roleAttributes = new ArrayList<String>();
			roleAttributes.add(assistText);

			if (fontcolor == null || fontcolor.trim().isEmpty()) {
				fontcolor = FONT_COLOR;
			}
			roleAttributes.add(fontcolor);
			
			if (background == null || background.trim().isEmpty()) {
				background = BACKGROUND_COLOR;
			}
			roleAttributes.add(background);
			
			roleAttributes.add(HOVER_COLOR);
			roleAttributes.add(lastUpdated);

			if (INDICATE_NEW_TEXT.equalsIgnoreCase("yes")) {
				roleAttributes.add(TEXT_DURATION);
			} else {
				roleAttributes.add("-1");
			}
			
			if(classId.equalsIgnoreCase(subClass)) {
				attrIdsForSubclass.add(attrId);
				asInfo.put(attrId, roleAttributes);
			} else if(classId.equalsIgnoreCase(levelOneParent)) {
				if(!attrIdsForSubclass.contains(attrId)) {
					asInfo.put(attrId, roleAttributes);
				}
			} else if(!asInfo.containsKey(attrId)) {
				asInfo.put(attrId, roleAttributes);
			}
		}
		
		log.debug("AssistInfo: " + asInfo);
		log.debug("Exiting getAssistInfoMap...");

		return asInfo;
	}

	private String generateQsForIn(int numQs) {
		StringBuilder items = new StringBuilder();
		for (int i = 0; i < numQs; i++) {
			if (i != 0)
				items.append(",");
			items.append("?");
		}
		return items.toString();
	}
	
	private Map<String, String> readConfigurations() throws SQLException, Exception {
		log.debug("Entering readConfigurations...");

		Map<String, String> configMap = null;
		prep1 = prepareStatement("SELECT CONFIGKEY,CONFIGVAL FROM CONFIGURATIONS WHERE CONFIGKEY <> 'LNFO' ORDER BY rowid DESC");
		ResultSet rs = prep1.executeQuery();
		configMap = new HashMap<String, String>();
		while ((rs != null) && rs.next()) {
			configMap.put(rs.getString("CONFIGKEY"), rs.getString("CONFIGVAL"));
		}

		log.debug("Configurations: " + configMap.toString());
		log.debug("Exiting readConfigurations...");

		return configMap;
	}

	@SuppressWarnings("unused")
	private Map<String, ArrayList<AssistTextEntry>> getAssistTexts(HashMap<String, String> params) throws Exception, SQLException {
		log.debug("Entering getAssistTexts..");
		ArrayList<AssistTextEntry> ateArr = null;
		ArrayList<String> strRoleArr;
		String classId = (String) params.get("classID");
		String attrId = (String) params.get("attrId");
		String isRoutable = (String) params.get("isRoutable");
		prep = prepareStatement("SELECT TEXTID,ASSISTTEXT,ISDIFFCOLOR,FONTCOLOR,BACKGROUND,WORKFLOWID,WORKFLOWSTATUSID,DATECREATED,LASTUPDATED FROM ASSISTTEXT WHERE CLASSID=? AND ATTRID=?");
		prep.setInt(1, Integer.parseInt(classId));
		prep.setString(2, attrId);
		
		ResultSet rs = prep.executeQuery();
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
			
			String IFNULL = isOracleDb? "NVL" : "ifnull";
			prep1 = prepareStatement("SELECT RP.RoleID value, Role label, CASE WHEN (" + IFNULL + "(TM.RoleID,-1) = -1) THEN 0 ELSE 1 END AS selected FROM RolePriority RP LEFT OUTER JOIN (SELECT RoleID from RoleTextMap WHERE TextID=?) TM ON RP.RoleID = TM.RoleID");
			prep1.setInt(1, Integer.parseInt(ate.getTextID()));
			
			ResultSet rs1 = prep1.executeQuery();
			strRoleArr = new ArrayList<String>();

			while ((rs1 != null) && rs1.next()) {
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

		Map<String, ArrayList<AssistTextEntry>> retHash = new HashMap<String, ArrayList<AssistTextEntry>>();
		retHash.put("ateArr", ateArr);
		log.debug("Exiting getAssistTexts..");
		return retHash;
	}

	@SuppressWarnings("unused")
	private Map<String, String[]> getRoleOptions() throws Exception, SQLException {
		log.debug("Entering getRoleOptions...");

		String[] strRoles = null;
		HashMap<String, String[]> retMap = new HashMap<String, String[]>();
		prep = prepareStatement("SELECT RoleID value, Role label FROM RolePriority RP");
		ResultSet rs = prep.executeQuery();
		ArrayList<String> strRoleArr = new ArrayList<String>();

		while ((rs != null) && rs.next()) {
			strRoleArr.add(rs.getString("value"));
			strRoleArr.add(rs.getString("label"));
		}
		strRoles = new String[strRoleArr.size()];
		strRoles = strRoleArr.toArray(strRoles);
		
		retMap.put("strRoles", strRoles);
		log.debug("Exiting getRoleOptions...");

		return retMap;
	}

	@SuppressWarnings("unused")
	private Map<String, List<String>> getRoleIds() throws Exception, SQLException {
		log.debug("Entering getRoleIds...");

		List<String> roleIds = new ArrayList<String>();

		HashMap<String, List<String>> retMap = new HashMap<String, List<String>>();
		prep = prepareStatement("SELECT RoleID FROM RolePriority");
		ResultSet rs = prep.executeQuery();

		while ((rs != null) && rs.next()) {
			roleIds.add(rs.getString("RoleID"));
		}

		log.debug("RoleOptions" + roleIds.toString());
		retMap.put("assistRoles", roleIds);
		log.debug("Exiting getRoleIds...");

		return retMap;
	}

	@SuppressWarnings("unused")
	private Map<String, ArrayList<String>> getRolePriorities() throws SQLException, Exception {
		log.debug("Entering getRolePriorities...");

		HashMap<String, ArrayList<String>> roleMap = null;
		ArrayList<String> roleAttributes;
		prep = prepareStatement("SELECT Role,Priority,fontcolor,background FROM RolePriority RP ORDER BY Priority ASC");
		ResultSet rs = prep.executeQuery();
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

	@SuppressWarnings("unused")
	private Map<String, ArrayList<Integer>> checkClassesAssistText() throws SQLException, Exception {
		log.debug("Entering checkClassesAssistText...");

		ArrayList<Integer> i = new ArrayList<Integer>();
		HashMap<String, ArrayList<Integer>> ret = new HashMap<String, ArrayList<Integer>>();
		
		prep = prepareStatement("SELECT DISTINCT ClassID FROM AssistText WHERE ClassID IS NOT NULL");
		ResultSet rs = prep.executeQuery();
		while ((rs != null) && rs.next()) {
			i.add(rs.getInt("ClassID"));
		}
		ret.put("result", i);
		log.debug("checkClassesAssistText: " + i.toString());
		log.debug("Exiting checkClassesAssistText...");

		return ret;
	}

	@SuppressWarnings("unused")
	private Map<String, Object> getAssistColorsForClasses(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering getAssistColorsForClasses...");
				
		int subClass = -1;
		int levelOneParent = -1;
		int levelTwoParent = -1;
		
		String classIds = (String) params.get("classId");
		String splitClasses[] = classIds.trim().split("\\s*,\\s*");
		int length = splitClasses.length;
		
		if(length == 1) {
			prep = prepareStatement("SELECT ATTRID,ASSISTCOLOR,CLASSID FROM ASSISTCOLOR WHERE CLASSID IN (?)");
			subClass = Integer.parseInt(splitClasses[0]);
			prep.setInt(1, subClass);
		} else if(length == 2) {
			prep = prepareStatement("SELECT ATTRID,ASSISTCOLOR,CLASSID FROM ASSISTCOLOR WHERE CLASSID IN (?,?)");
			subClass = Integer.parseInt(splitClasses[0]);
			prep.setInt(1, subClass);
			levelOneParent = Integer.parseInt(splitClasses[1]);
			prep.setInt(2, levelOneParent);
		} else if(length == 3) {
			prep = prepareStatement("SELECT ATTRID,ASSISTCOLOR,CLASSID FROM ASSISTCOLOR WHERE CLASSID IN (?,?,?)");
			subClass = Integer.parseInt(splitClasses[0]);
			prep.setInt(1, subClass);
			levelOneParent = Integer.parseInt(splitClasses[1]);
			prep.setInt(2, levelOneParent);
			levelTwoParent = Integer.parseInt(splitClasses[2]);
			prep.setInt(3, levelTwoParent);
		}
		
		log.debug(subClass + "," + levelOneParent + "," + levelTwoParent);
		
		Map<String, Object> attColors = new HashMap<String, Object>();
		List<String> attrIdsForSubclass = new ArrayList<String>();
		ResultSet rs = prep.executeQuery();
		if (rs != null) {
			while (rs.next()) {
				int classId = rs.getInt("CLASSID");
				String attrId = rs.getString("ATTRID");
				
				if(classId == subClass) {
					attrIdsForSubclass.add(attrId);
					attColors.put(attrId, rs.getString("ASSISTCOLOR"));
				} else if(classId == levelOneParent) {
					if(!attrIdsForSubclass.contains(attrId)) {
						attColors.put(attrId, rs.getString("ASSISTCOLOR"));
					}
				} else if(!attColors.containsKey(attrId)) {
					attColors.put(attrId, rs.getString("ASSISTCOLOR"));
				}
			}
		}
		
		log.debug("Attribute Colors: " + attColors.toString());
		log.debug("Exiting getAssistColorsForClasses...");
		return attColors;
	}

	@SuppressWarnings("unused")
	private Map<String, AssistColorEntry> getAssistColorsForClass(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering getAssistColorsForClass...");

		String classId = (String) params.get("classId");
		Map<String, AssistColorEntry> attColors = new HashMap<String, AssistColorEntry>();

		ResultSet rs = null;
		prep = prepareStatement("SELECT ColorID, AttrID, AssistColor FROM AssistColor WHERE ClassID=?");
		prep.setInt(1, Integer.parseInt(classId));
		rs = prep.executeQuery();
		if (rs != null) {
			while (rs.next()) {
				AssistColorEntry colorEntry = new AssistColorEntry();
				colorEntry.setColorId(rs.getString("ColorID"));
				colorEntry.setAttributeId(rs.getString("AttrID"));
				log.debug(rs.getString("AttrID"));
				colorEntry.setAssistColor(rs.getString("AssistColor"));
				attColors.put(colorEntry.getAttributeId(), colorEntry);
			}
		}
		log.debug("Attribute Colors: " + attColors);
		log.debug("Exiting getAssistColorsForClass...");

		return attColors;
	}

	@SuppressWarnings("unused")
	private Map<String, List<String>> checkAssistText(HashMap<String, String> params) throws SQLException, Exception {
		log.debug("Entering checkAssistText..."); //TODO Multiple calls

		String classID = (String) params.get("classId");
		List<String> i = new ArrayList<String>();
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		prep = prepareStatement("SELECT DISTINCT AttrID FROM AssistText WHERE ClassID=?");
		prep.setInt(1, Integer.parseInt(classID));
		ResultSet rs = prep.executeQuery();

		log.debug("checkAssistText Class: " + classID);
		while ((rs != null) && rs.next()) {
			i.add(rs.getString("AttrID"));
		}
		result.put("result", i);

		log.debug("Check Assist Text: " + result.toString());
		log.debug("Exiting checkAssistText...");

		return result;
	}

	@SuppressWarnings("unused")
	private void addNewAssistColor(HashMap<?, ?> params) throws Exception, SQLException {
		log.debug("Entering addNewAssistColor...");
		
		String classID = (String) params.get("classID");
		JSONArray jsonAttColors = (JSONArray) params.get("jsonAttColors");
		JSONObject attColor = null;

		String colorId = "";
		String attId = "";
		String assistColor = "";

		String insertQry = isOracleDb? 
				"INSERT INTO AssistColor (ColorID,ClassID,AttrID,AssistColor) VALUES (SEQ_ASSISTCOLOR.nextVal,?,?,?)" : 
				"INSERT INTO AssistColor (ClassID,AttrID,AssistColor) VALUES (?,?,?)";
		prep = prepareStatement(insertQry);
		boolean executeBatch = false;

		if (jsonAttColors != null && jsonAttColors.size() > 0) {
			HashMap<String, String> params1 = new HashMap<String, String>();
			params1.put("classID", classID);
			deleteColor(params1);

			for (int attColorIndex = 0; attColorIndex < jsonAttColors.size(); attColorIndex++) {
				attColor = (JSONObject) jsonAttColors.get(attColorIndex);
				colorId = attColor.get("colorId") + "";
				attId = attColor.get("attId") + "";
				assistColor = attColor.get("assistColor") + "";
				
				if(assistColor.length() == 4) {
					assistColor = assistColor.replaceAll(".", "$0$0").substring(1);
				}
				
				if (!assistColor.equals(Constants.Config.DEFAULT_LABEL_COLOR)) {
					log.debug("addNewAssistColor Insert/Update ::: Color Id=[" + colorId + "], Attribute Id=[" + attId + "], Assist Color=[" + assistColor + "]");
					
					executeBatch = true;
					prep.setString(1, classID);
					prep.setString(2, attId);
					prep.setString(3, assistColor);
					prep.addBatch();
				}
			}
			
			if(executeBatch) {
				prep.executeBatch();
			}
		}

		log.debug("Exiting addNewAssistColor...");
	}

	private void deleteColor(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering deleteColor...");
		
		String classID = (String) params.get("classID");
		prep1 = prepareStatement("DELETE FROM AssistColor WHERE ClassID=?");
		prep1.setInt(1, Integer.parseInt(classID));
		prep1.executeUpdate();

		log.debug("Exiting deleteColor...");
	}

	@SuppressWarnings("unused")
	private Map<String, Integer> addNewAssistText(HashMap<?, ?> params) throws Exception, SQLException {
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

		/*String wfIDExpression = "='" + workflowID.replace("'", "''") + "'";
		String wfsIDExpression = "='" + workflowStatusID.replace("'", "''") + "'";*/

		if(isOracleDb) {
			prep = prepareStatement("INSERT INTO AssistText (TextID, ClassID, AttrID, AssistText,fontcolor,background,isDiffColor,workflowID,workflowStatusID,DateCreated,LastUpdated) Values (SEQ_ASSISTTEXT.nextVal,?,?,?,?,?,?,?,?,?,?)");
			
			/*if(workflowID.length() == 0) wfIDExpression = " IS NULL";
			if(workflowStatusID.length() == 0) wfsIDExpression = " IS NULL";*/
		} else {
			prep = prepareStatement("INSERT INTO AssistText (ClassID, AttrID, AssistText,fontcolor,background,isDiffColor,workflowID,workflowStatusID,DateCreated,LastUpdated) Values (?,?,?,?,?,?,?,?,?,?)");
		}
		
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
			prep1 = prepareStatement("SELECT TextID FROM AssistText WHERE ClassID=? AND AttrID=? AND AssistText=? AND (workflowID=? OR (workflowID IS NULL AND ? IS NULL)) AND (workflowStatusID=? OR (workflowStatusID IS NULL AND ? IS NULL)) ORDER BY TextID DESC");
			prep1.setInt(1, Integer.parseInt(classID));
			prep1.setString(2, attrID);
			prep1.setString(3, assistText); //assistText.replace("'", "''")
			prep1.setString(4, workflowID);
			if(workflowID.length() > 0) {
				prep1.setString(5, workflowID);
			} else {
				prep1.setString(5, null);
			}
			prep1.setString(6, workflowStatusID);
			if(workflowStatusID.length() > 0) {
				prep1.setString(7, workflowStatusID);
			} else {
				prep1.setString(7, null);
			}
			ResultSet rs = prep1.executeQuery();
			if ((rs != null) && rs.next()) {
				textId = rs.getInt("TextID");
			}
			result.put("textId", textId);
		} catch (Exception e) {
			log.error("", e);
		}

		String[] roleList = (String[]) params.get("roleList");
		if (roleList != null && roleList.length > 0) {
			String rolestr = generateQsForIn(roleList.length);
			
			prep2 = prepareStatement("INSERT INTO RoleTextMap (TextID, RoleID) SELECT ?, RoleID FROM RolePriority WHERE RoleID IN (" + rolestr + ")");
			prep2.setInt(1, textId);
			for(int i=0; i<roleList.length; i++) {
				prep2.setString(i+2, roleList[i]);
			}
			
			prep2.executeUpdate();
		}
		
		log.debug("Exiting addNewAssistText. ");
		return result;
	}

	@SuppressWarnings("unused")
	private void removeAssistText(HashMap<?, ?> params) throws Exception, SQLException {
		log.debug("Entering removeAssistText...");
		String textID = (String) params.get("textID");
		
		log.debug("DELETE FROM AssistText WHERE TextID=" + textID);
		prep = prepareStatement("DELETE FROM AssistText WHERE TextID=?");
		prep.setInt(1, Integer.parseInt(textID));
		prep.executeUpdate();
		
		log.debug("DELETE FROM RoleTextMap WHERE TextID=" + textID);
		prep1 = prepareStatement("DELETE FROM RoleTextMap WHERE TextID=?");
		prep1.setInt(1, Integer.parseInt(textID));
		prep1.executeUpdate();
		
		log.debug("Exiting removeAssistText...");
	}

	@SuppressWarnings("unused")
	private void updateAssistText(HashMap<?, ?> params) throws Exception, SQLException {
		log.debug("Entering updateAssistText..");
		String textID = (String) params.get("textID");
		String assistText = (String) params.get("assistText");
		String fontColor = (String) params.get("fontColor");
		String backgroundColor = (String) params.get("backgroundColor");
		boolean isDiffColor = (Boolean) params.get("isDiffColor");
		String workflowID = (String) params.get("workflowID");
		String workflowStatusID = (String) params.get("workflowStatusID");
		prep = prepareStatement("Update AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?,workflowID=?,workflowStatusID=?, LastUpdated=?   WHERE TextID=?");

		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();
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
		
		String[] roleList = (String[]) params.get("roleList");	
		if (roleList != null && roleList.length > 0) {
			prep1 = prepareStatement("DELETE FROM RoleTextMap WHERE TextID=?");
			prep1.setInt(1, Integer.parseInt(textID));
			prep1.executeUpdate();
			
			String rolestr = generateQsForIn(roleList.length);
			
			prep2 = prepareStatement("INSERT INTO RoleTextMap (TextID, RoleID) SELECT ?, RoleID FROM RolePriority WHERE RoleID IN (" + rolestr + ")");
			prep2.setInt(1, Integer.parseInt(textID));
			for(int i=0; i<roleList.length; i++) {
				prep2.setString(i+2, roleList[i]);
			}
			
			prep2.executeUpdate();
		}
		
		log.debug("Exiting updateAssistText..");
	}

	@SuppressWarnings("unused")
	private void updateRolePriority(HashMap<String, ArrayList<RoleEntry>> params) throws Exception, SQLException {
		log.debug("Entering updateRolePriority...");
		ArrayList<RoleEntry> roleList = params.get("roleList");

		String roleIds = generateQsForIn(roleList.size());
		prep = prepareStatement("SELECT RoleID FROM RolePriority WHERE RoleID NOT IN (" + roleIds + ")");
		for(int i=0; i<roleList.size(); i++) {
			prep.setInt(i+1, roleList.get(i).getRoleID());
		}	
		ResultSet rs = prep.executeQuery();
		
		prep1 = prepareStatement("DELETE FROM RoleTextMap WHERE RoleID=?");
		while ((rs != null) && rs.next()) {
			prep1.setInt(1, rs.getInt("RoleID"));
			prep1.addBatch();
		}
		prep1.executeBatch();
		
		prep2 = prepareStatement("DELETE FROM AssistText WHERE TextID NOT IN ( SELECT TextID FROM RoleTextMap )");
		prep2.executeUpdate();
		
		prep3 = prepareStatement("DELETE FROM RolePriority");
		prep3.executeUpdate();

		prep4 = prepareStatement("INSERT INTO RolePriority (RoleID, Role, Priority,fontcolor,background) VALUES (?,?,?,?,?)");
		for (RoleEntry role : roleList) {
			log.debug("updateRolePriority Role: " + role.getRole());
			prep4.setInt(1, role.getRoleID());
			prep4.setString(2, role.getRole());
			prep4.setInt(3, role.getPriority());
			prep4.setString(4, role.getFontColor());
			prep4.setString(5, role.getBackgroundColor());
			prep4.addBatch();
		}
		prep4.executeBatch();
		
		log.debug("Exiting updateRolePriority...");
	}

	@SuppressWarnings("unused")
	private void insertConfig(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering insertConfig.. ");
		String key = (String) params.get("key");
		String value = (String) params.get("value");
		
		/*String configVal = null;
		if (key.equals("accessType")) {
			prep = prepareStatement("SELECT ConfigVal FROM Configurations WHERE ConfigKey=?");
			prep.setString(1, "accessType");
			ResultSet rs = prep.executeQuery();
			if((rs != null) && rs.next()) {
				configVal = rs.getString("ConfigVal");
			}
		}*/
		
		prep1 = prepareStatement("INSERT INTO Configurations (ConfigKey,ConfigVal) VALUES (?,?)");
		prep1.setString(1, key);
		prep1.setString(2, value);
		prep1.executeUpdate();
		
		log.debug("Exiting insertConfig.. ");
	}

	@SuppressWarnings("unused")
	private Map<String, String> getConfigByKey(HashMap<String, String> params) throws SQLException, Exception {
		log.debug("Entering getConfigByKey...");
		
		String value = "";	
		String key = params.get("key");
		log.debug("getConfigByKey: SELECT CONFIGVAL FROM CONFIGURATIONS WHERE CONFIGKEY:" + key);
		prep = prepareStatement("SELECT CONFIGVAL FROM CONFIGURATIONS WHERE CONFIGKEY=?");
		prep.setString(1, key);
		
		ResultSet rs = prep.executeQuery();
		if (rs != null && rs.next()) {
			value = rs.getString("CONFIGVAL");
		}
		
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("value", value);

		log.debug("Configuration: " + result);
		log.debug("Exiting getConfigByKey...");

		return result;
	}

	@SuppressWarnings("unused")
	private void addConfigurations(HashMap<?, ?> params) throws Exception {
		log.debug("Entering addConfigurations...");
		prep = prepareStatement("INSERT INTO Configurations VALUES (?,?)");
		for(Entry<?,?> entry : params.entrySet()) {
			prep.setString(1, entry.getKey().toString());
			prep.setString(2, entry.getValue().toString());
			prep.addBatch();
		}
		prep.executeBatch();
		log.debug("Exiting addConfigurations...");
	}
	
	@SuppressWarnings({"unused"})
	private void updateConfigurations(HashMap<?, ?> prams) throws Exception, SQLException {
		log.debug("Entering updateConfigurations.. ");

		String[] configs = (String[]) prams.get("configs");	
		boolean enableOptOut = true;
		prep = prepareStatement("UPDATE Configurations SET ConfigVal=? WHERE ConfigKey=?");
		
		for (String config : configs) {
			String[] params = config.split("=", 2);
			log.debug("Update Configuration Parameters: [" + params[1] + "] , [" + params[0] + "]");
			String val = params[1];
			if(val.trim().length() == 0) {
				val = "NULL_VALUE";
			}
			prep.setString(1, val);
			prep.setString(2, params[0]);
			prep.addBatch();
			
			if("enableOptOut".equals(params[0])) {
				enableOptOut = "Yes".equalsIgnoreCase(val);
			}
		}
		prep.executeBatch();
		
		if(!enableOptOut) {
			removeAllOptOutUsers();
		}
		log.debug("Exiting updateConfigurations.. ");
		AgileHandler.disconnect();
	}

	@SuppressWarnings("unused")
	private void updateDefaultRolePriority(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering updateDefaultRolePriority.. ");

		String newRole = (String) params.get("newRole");

		prep = prepareStatement("UPDATE RolePriority set Role=? WHERE RoleID = 0");
		prep.setString(1, newRole);
		log.debug("updateDefaultRolePriority: UPDATE RolePriority set Role = '" + newRole.replace("'", "''") + "' WHERE RoleID =0");
		int res = prep.executeUpdate();
		log.info("Role Priority updated=[" + res + "]");
		
		log.debug("Exiting updateDefaultRolePriority.. ");
	}

	@SuppressWarnings("unused")
	private void updateLicinfo(HashMap<?, ?> params) throws Exception, SQLException {
		log.debug("Entering updateLicinfo.. ");
		
		String licinfo = (String) params.get("licinfo");
		if (licinfo != null) {
			log.debug("LNFO: " + licinfo);
			
			prep = prepareStatement("DELETE FROM Configurations WHERE ConfigKey=?");
			prep.setString(1, "LNFO");
			prep.executeUpdate();
			
			prep1 = prepareStatement("INSERT INTO Configurations (ConfigKey, ConfigVal) VALUES (?,?)");
			prep1.setString(1, "LNFO");
			prep1.setString(2, licinfo);
			prep1.executeUpdate();
		}
		
		log.debug("Exiting updateLicinfo.. ");
	}

	@SuppressWarnings("unused")
	private Map<String, String> getLicInfo() throws Exception, SQLException {
		log.debug("Entering getLicInfo.. ");

		String strLic = null;
		HashMap<String, String> retMap = new HashMap<String, String>();
		
		prep = prepareStatement("SELECT ConfigVal FROM Configurations WHERE ConfigKey=?");
		prep.setString(1, "LNFO");		
		ResultSet rs = prep.executeQuery();
		while ((rs != null) && rs.next()) {
			strLic = rs.getString("ConfigVal");
		}
		
		retMap.put("strLic", strLic);
		log.debug("Exiting getLicInfo.. ");

		return retMap;
	}

	@SuppressWarnings("unused")
	private void deleteAssistColor(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering deleteAssistColor...");

		String colorID = (String) params.get("colorID");
		
		prep = prepareStatement("DELETE FROM AssistColor WHERE ColorID=?");
		prep.setInt(1, Integer.parseInt(colorID));
		log.debug("deleteAssistColor: DELETE FROM AssistColor WHERE ColorID=" + colorID);
		prep.executeUpdate();
		
		log.debug("Exiting deleteAssistColor...");
	}

	@SuppressWarnings("unused")
	private void deleteRolePriority(HashMap<?, ?> params) throws SQLException, Exception {
		log.debug("Entering deleteRolePriority...");

		String roleID = (String) params.get("roleID");
		
		prep = prepareStatement("DELETE FROM RolePriority WHERE RoleID=?");
		prep.setInt(1, Integer.parseInt(roleID));
		log.debug("deleteRolePriority: DELETE FROM RolePriority WHERE RoleID=" + roleID);
		prep.executeUpdate();
		
		log.debug("Exiting deleteRolePriority...");
	}

	@SuppressWarnings({"unused", "unchecked"})
	private void mergeRolePriority(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering mergeRolePriority...");

		List<RoleEntry> roleList = (List<RoleEntry>) params.get("role");

		List<String> roleIDList = (List<String>) params.get("roleIDList");

		String updateQuery = "UPDATE RolePriority SET  Role=?, Priority=?, fontcolor=?, background=? WHERE RoleID=?";
		prep = prepareStatement(updateQuery);
		String insertQuery = "INSERT into RolePriority (RoleID, Role, Priority, fontcolor, background ) VALUES (?,?,?,?,?)";
		prep1 = prepareStatement(insertQuery);
		for (int i = 0; i < roleList.size(); i++) {
			if (roleIDList.contains(roleList.get(i).getRoleID())) {
				prep.setString(1, roleList.get(i).getRole());
				prep.setString(2, String.valueOf(roleList.get(i).getPriority()));
				prep.setString(3, roleList.get(i).getFontColor());
				prep.setString(4, roleList.get(i).getBackgroundColor());
				prep.setInt(5, roleList.get(i).getRoleID());
				log.debug("updatetQuery: " + updateQuery);
				prep.addBatch();
			} else {
				prep1.setInt(1, roleList.get(i).getRoleID());
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

	@SuppressWarnings("unused")
	private Map<String, List<AssistTextEntry>> getAssistTextMap() throws SQLException, Exception {
		log.debug("Entering getAssistTextMap.. ");
		HashMap<String, List<AssistTextEntry>> dbAssistTextList = new HashMap<String, List<AssistTextEntry>>();
		List<AssistTextEntry> textList = new ArrayList<AssistTextEntry>();

		prep = prepareStatement("SELECT AssistText.TextID,ClassID,AttrID,workflowID,workflowStatusID,DateCreated FROM AssistText");
		ResultSet rset = prep.executeQuery();
		while (rset.next()) {
			AssistTextEntry text = new AssistTextEntry();
			String workflowID = rset.getObject("workflowID") == null? "" : rset.getObject("workflowID").toString();
			String workflowStatusID = rset.getObject("workflowStatusID") == null? "" : rset.getObject("workflowStatusID").toString();
			text.setTextID(rset.getObject("TextID").toString());
			text.setAttrID(rset.getObject("AttrID").toString());
			text.setClassID(rset.getObject("ClassID").toString());
			text.setWorkflowID(workflowID);
			text.setWorkflowStatusID(workflowStatusID);
			text.setDateCreated(rset.getObject("DateCreated").toString());
			textList.add(text);
		}
		dbAssistTextList.put("textList", textList);

		log.debug("Exiting getAssistTextMap.. ");
		return dbAssistTextList;
	}

	@SuppressWarnings("unused")
	private Map<String, List<String>> getRoleIDs() throws SQLException, Exception {
		log.debug("Entering getRoleIDs.. ");
		HashMap<String, List<String>> roleIds = new HashMap<String, List<String>>();
		
		prep = prepareStatement("SELECT RoleID FROM RolePriority");
		ResultSet rset = prep.executeQuery();

		List<String> roleIDList = new ArrayList<String>();
		while ((rset != null) && rset.next()) {
			roleIDList.add(rset.getString("RoleID"));
		}
		roleIds.put("roleIDList", roleIDList);
		log.debug("Exiting getRoleIDs.. ");
		return roleIds;
	}

	@SuppressWarnings("unused")
	private Map<String, List<String>> getColorIDs() throws SQLException, Exception {
		log.debug("Entering getColorIDs.. ");

		prep = prepareStatement("SELECT ColorID FROM AssistColor");
		ResultSet rset = prep.executeQuery();
		HashMap<String, List<String>> colorIds = new HashMap<String, List<String>>();
		List<String> colorIDList = new ArrayList<String>();
		while ((rset != null) && rset.next()) {
			colorIDList.add(rset.getString("ColorID"));
		}
		colorIds.put("colorIDList", colorIDList);
		log.debug("Exiting getColorIDs.. ");

		return colorIds;
	}
	
	@SuppressWarnings("unused")
	private Map<String, List<String>> getColorAttributes() throws SQLException, Exception {
		log.debug("Entering getColorIDs.. ");

		prep = prepareStatement("SELECT ClassID, AttrID FROM AssistColor");
		ResultSet rset = prep.executeQuery();
		HashMap<String, List<String>> colorIds = new HashMap<String, List<String>>();
		List<String> colorAttributesList = new ArrayList<String>();
		while ((rset != null) && rset.next()) {
			String entry = rset.getString("ClassID") + ":" + rset.getString("AttrID");
			colorAttributesList.add(entry);
		}
		colorIds.put("colorAttributesList", colorAttributesList);
		log.debug("Exiting getColorIDs.. ");

		return colorIds;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void mergeAssistColor(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering mergeAssistColor...");

		List<AssistColorEntry> colorList = (List<AssistColorEntry>) params.get("color");
		List<String> colorAttributesList = (List<String>) params.get("colorAttributesList");

		String updateQuery = "UPDATE AssistColor SET AssistColor=? WHERE ClassID=? AND AttrID=?";
		prep = prepareStatement(updateQuery);
		String insertQuery = isOracleDb?
				"INSERT INTO AssistColor (ColorID,ClassID,AttrID,AssistColor) VALUES (SEQ_ASSISTCOLOR.nextVal,?,?,?)" : 
				"INSERT INTO AssistColor (ClassID,AttrID,AssistColor) VALUES (?,?,?)";
		prep1 = prepareStatement(insertQuery);
		
		for (int i = 0; i < colorList.size(); i++) {
			AssistColorEntry assistEntry = colorList.get(i);
			String entry = assistEntry.getClassId() + ":" + assistEntry.getAttributeId();
			if (colorAttributesList.contains(entry)) {
				prep.setString(1, assistEntry.getAssistColor());
				prep.setString(2, assistEntry.getClassId());
				prep.setString(3, assistEntry.getAttributeId());
				
				log.debug("updateQuery: " + updateQuery);
				prep.addBatch();
			} else {
				prep1.setString(1, assistEntry.getClassId());
				prep1.setString(2, assistEntry.getAttributeId());
				prep1.setString(3, assistEntry.getAssistColor());

				log.debug("insertQuery: " + insertQuery);
				prep1.addBatch();
			}
		}
		prep.executeBatch();
		prep1.executeBatch();
		log.debug("Exiting mergeAssistColor...");
	}

	@SuppressWarnings("unused")
	private Map<String, List<String>> getTextRoles() throws SQLException, Exception {
		log.debug("Entering getTextRoles...");

		prep = prepareStatement("SELECT TextID,RoleID FROM RoleTextMap");
		ResultSet rset = prep.executeQuery();
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

	@SuppressWarnings({"unused", "unchecked"})
	private void mergeUpdateText(HashMap<String, Object> params) throws SQLException, Exception {
		log.debug("Entering mergeUpdateText...");
		HashMap<String, AssistTextEntry> updateText = (HashMap<String, AssistTextEntry>) params.get("updateText");
		String updateQuery = "UPDATE AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?, DateCreated=?, LastUpdated=? WHERE TextID=? ";

		prep = prepareStatement(updateQuery);
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

	@SuppressWarnings({"unused", "unchecked"})
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

		String insertQuery = isOracleDb?
					"INSERT INTO AssistText (TextID, ClassID, AttrID, AssistText, fontcolor, background, isDiffColor, workflowID, workflowStatusID, DateCreated, LastUpdated) VALUES (SEQ_ASSISTTEXT.nextVal,?,?,?,?,?,?,?,?,?,?)" :
					"INSERT INTO AssistText (ClassID, AttrID, AssistText, fontcolor, background, isDiffColor, workflowID, workflowStatusID, DateCreated, LastUpdated) VALUES (?,?,?,?,?,?,?,?,?,?)";

		prep = prepareStatement(insertQuery);
		if (insertText.size() != 0) {
			String dateCreated = insertText.get(keys[0]).getDateCreated();
			if(dateCreated == null || !dateCreated.isEmpty()) {
				dateCreated = timeStamp;
			}
			
			prep.setString(1, insertText.get(keys[0]).getClassID());
			prep.setString(2, insertText.get(keys[0]).getAttrID());
			prep.setString(3, StringEscapeUtils.unescapeXml(insertText.get(keys[0]).getAssistText()));
			prep.setString(4, insertText.get(keys[0]).getFontColor());
			prep.setString(5, insertText.get(keys[0]).getBackgroundColor());
			prep.setBoolean(6, insertText.get(keys[0]).getIsDifferentColor());
			prep.setString(7, insertText.get(keys[0]).getWorkflowID());
			prep.setString(8, insertText.get(keys[0]).getWorkflowStatusId());
			prep.setString(9, dateCreated);
			prep.setString(10, timeStamp);
			prep.executeUpdate();

			prep1 = prepareStatement("Select MAX(TextID) As maxId FROM AssistText");
			ResultSet rs = prep1.executeQuery();
			if ((rs != null) && rs.next()) {
				log.debug(rs.getObject("maxId").toString());
				maxId = Integer.parseInt(rs.getObject("maxId").toString());
			}
		}

		prep = prepareStatement(insertQuery);
		for (int i = 1; i < keys.length; i++) {
			String dateCreated = insertText.get(keys[i]).getDateCreated();
			if(dateCreated == null || !dateCreated.isEmpty()) {
				dateCreated = timeStamp;
			}
			
			prep.setString(1, insertText.get(keys[i]).getClassID());
			prep.setString(2, insertText.get(keys[i]).getAttrID());
			prep.setString(3, StringEscapeUtils.unescapeXml(insertText.get(keys[i]).getAssistText()));
			prep.setString(4, insertText.get(keys[i]).getFontColor());
			prep.setString(5, insertText.get(keys[i]).getBackgroundColor());
			prep.setBoolean(6, insertText.get(keys[i]).getIsDifferentColor());
			prep.setString(7, insertText.get(keys[i]).getWorkflowID());
			prep.setString(8, insertText.get(keys[i]).getWorkflowStatusId());
			prep.setString(9, dateCreated);
			prep.setString(10, timeStamp);
			prep.addBatch();
		}
		prep.executeBatch();

		prep2 = prepareStatement("INSERT INTO RoleTextMap (TextID,RoleID) VALUES (?,?)");
		for (int i = 0; i < keys.length; i++) {
			for (int j = 0; j < insertText.get(keys[i]).getRolesList().size(); j++) {
				prep2.setInt(1, maxId);
				prep2.setInt(2, Integer.parseInt(insertText.get(keys[i]).getRolesList().get(j)));
				prep2.addBatch();
			}
			maxId++;
		}
		prep2.executeBatch();
		
		log.debug("Exiting mergeInsertText...");
	}

	@SuppressWarnings("unused")
	private Map<String, Boolean> isRoles() throws Exception {
		log.debug("Entering isRoles...");

		HashMap<String, Boolean> rolesStatus = new HashMap<String, Boolean>();
		boolean isRoles = true;

		prep = prepareStatement("SELECT ConfigVal FROM Configurations WHERE ConfigKey=?");
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

	@SuppressWarnings("unused")
	private HashMap<String, List<AssistColorEntry>> getAssistColor() throws SQLException, Exception {
		log.debug("Entering getAssistColor.. ");

		HashMap<String, List<AssistColorEntry>> colorList = new HashMap<String, List<AssistColorEntry>>();
		List<AssistColorEntry> AssistColorList = new ArrayList<AssistColorEntry>();
		
		prep = prepareStatement("SELECT * FROM AssistColor");
		ResultSet rset = prep.executeQuery();
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

	@SuppressWarnings("unused")
	private HashMap<String, List<RoleEntry>> getRolePriority() throws SQLException, Exception {
		log.debug("Entering getRolePriority.. ");

		HashMap<String, List<RoleEntry>> roleList = new HashMap<String, List<RoleEntry>>();
		List<RoleEntry> RolePriorityList = new ArrayList<RoleEntry>();
		
		prep = prepareStatement("SELECT * FROM RolePriority");
		ResultSet rset = prep.executeQuery();
		while (rset.next()) {
			RoleEntry role = new RoleEntry();
			role.setRoleID(rset.getInt("RoleID"));
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

	@SuppressWarnings("unused")
	private HashMap<String, String> getOptOutUsers() throws SQLException, Exception {
		log.debug("Entering getOptOutUsers.. ");

		HashMap<String, String> usersList = new HashMap<String, String>();		
		prep = prepareStatement("SELECT * FROM OptOutUsers");
		ResultSet rset = prep.executeQuery();
		
		while (rset.next()) {
			String userid = "";
			try {
				userid = rset.getObject("userid").toString();
			} catch (Exception e) {
				return usersList;
			}
			String optoutdate = "";
			try {
				optoutdate = rset.getObject("optoutdate").toString();
			} catch (Exception e) {
				optoutdate = "Not Available";
			}
			usersList.put(userid, optoutdate);
		}
		
		log.debug("Exiting getOptOutUsers.. ");
		return usersList;
	}

	@SuppressWarnings({"unused", "unchecked"})
	private HashMap<String, List<AssistTextEntry>> getAssistText() throws Exception, SQLException {
		log.debug("Entering getAssistText.. ");

		HashMap<String, List<String>> textRoles = (HashMap<String, List<String>>) handleDBRequest("getTextRoles", null, false);

		HashMap<String, List<AssistTextEntry>> textList = new HashMap<String, List<AssistTextEntry>>();
		List<AssistTextEntry> AssistTextList = new ArrayList<AssistTextEntry>();

		Set<String> notFoundClasses = new HashSet<String>();
		Map<String, String> classAPINames = new HashMap<String, String>();
		Map<String, Map<String, String>> attrFullAPINames = new HashMap<String, Map<String, String>>();
		
		UIListHandler objUIListHandler = new UIListHandler(this);
		
		prep = prepareStatement("SELECT * FROM AssistText");
		ResultSet rset = prep.executeQuery();
		while (rset.next()) {
			String classID = rset.getObject("ClassID").toString();
			String attrID = rset.getObject("AttrID").toString();

			if(notFoundClasses.contains(classID)) {
				continue;
			}
			
			AssistTextEntry text = new AssistTextEntry();
			String textID = rset.getObject("TextID").toString();
			text.setTextID(textID);
			String assisttext = rset.getString("AssistText");
			text.setAssistText(StringEscapeUtils.escapeXml(assisttext));
			text.setAttrID(attrID);
			text.setBackgroundColor(rset.getString("background"));
			text.setClassID(classID);
			if (rset.getString("DateCreated") != null) {
				text.setDateCreated(rset.getString("DateCreated"));
			} else {
				text.setDateCreated("");
			}
			text.setFontColor(rset.getString("fontcolor"));
			text.setIsDifferentColor(rset.getBoolean("isDiffColor"));
			
			String wfID = rset.getString("workflowID");
			if(wfID == null) wfID = "";
			text.setWorkflowID(wfID);
			
			String wfstatusID = rset.getString("workflowStatusID");
			if(wfstatusID == null) wfstatusID = "";
			text.setWorkflowStatusID(wfstatusID);
			
			if(classAPINames.containsKey(classID)) {
				String classAPIName = classAPINames.get(classID);
				log.debug("Agile Class API Name: " + classAPIName);
				text.setClassName(classAPIName);
				
				Map<String, String> classAttrFullAPINames = null;
				if(attrFullAPINames.containsKey(classID)) {
					classAttrFullAPINames = attrFullAPINames.get(classID);
				} else {
					classAttrFullAPINames = new HashMap<String, String>();
					attrFullAPINames.put(classID, classAttrFullAPINames);
				}
				
				if(classAttrFullAPINames.containsKey(attrID)) {
					String attrFullAPIName = classAttrFullAPINames.get(attrID);
					log.debug("Attribute API Name: " + attrFullAPIName);
					text.setAtrrName(attrFullAPIName);
				} else {
					String attrFullAPIName = objUIListHandler.getAttrFullAPIName(classID, attrID);
					classAttrFullAPINames.put(attrID, attrFullAPIName);
					text.setAtrrName(attrFullAPIName);
				}
			} else {
				String classAPIName = objUIListHandler.getClassAPIName(classID);
				if(classAPIName != null) {
					classAPINames.put(classID, classAPIName);
					
					Map<String, String> classAttrFullAPINames = new HashMap<String, String>();
					attrFullAPINames.put(classID, classAttrFullAPINames);
					
					String attrFullAPIName = objUIListHandler.getAttrFullAPIName(classID, attrID);
					classAttrFullAPINames.put(attrID, attrFullAPIName);
					
					text.setClassName(classAPIName);
					text.setAtrrName(attrFullAPIName);
				} else {
					notFoundClasses.add(classID);
					continue;
				}
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

	@SuppressWarnings("unused")
	private Map<String, Object> authenticate(HashMap<String, String> params) throws Exception {
		log.debug("Entering authenticate.. ");

		int uid = -1;
		boolean isAdminUser = false;
		
		String userName = params.get("userName");
		String password = params.get("password");
		Map<String, Object> result = new HashMap<String, Object>();
		String userTable = isOracleDb? "LoginUser" : "User";
		prep = prepareStatement("SELECT USERID, ADMINUSER FROM " + userTable + " WHERE UserName=? AND Pwd=?");
		prep.setString(1, userName);
		prep.setString(2, password);
		ResultSet rs = prep.executeQuery();
		if ((rs != null) && rs.next()) {
			uid = rs.getInt("UserId");
			isAdminUser = "Yes".equalsIgnoreCase(rs.getString("ADMINUSER"));
			log.debug("UserId: " + uid + ", Admin: " + isAdminUser);
		}
		result.put("userID", uid);
		result.put("isAdminUser", isAdminUser);
		log.debug("Exiting authenticate.. ");

		return result;
	}

	@SuppressWarnings("unused")
	private Map<String, Boolean> ChangePassword(HashMap<String, String> map) throws Exception {
		log.debug("Entering ChangePassword.. ");

		boolean status = false;
		HashMap<String, Boolean> statusMap = new HashMap<String, Boolean>();
		String uid = map.get("uid");
		String cpwd = map.get("cpwd");
		String npwd = map.get("npwd");
		String userTable = isOracleDb? "LoginUser" : "User";
		prep = prepareStatement("UPDATE " + userTable + " SET Pwd=? WHERE UserId=? AND Pwd=?");
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

	@SuppressWarnings("unused")
	private Map<String, String> getUserName() throws Exception {
		log.debug("Entering getUserName.. ");
		String userName = null;
		Map<String, String> result = new HashMap<String, String>();
		String userTable = isOracleDb? "LoginUser" : "User";
		prep = prepareStatement("SELECT UserName FROM " + userTable);
		ResultSet rs = prep.executeQuery();
		if ((rs != null) && rs.next()) {
			userName = rs.getString("UserName");
			log.debug("UserName: " + userName);
		}
		result.put("userName", userName);
		log.debug("Exiting getUserName.. ");

		return result;
	}

	@SuppressWarnings("unused")
	private void addOptOutUser(HashMap<String, String> params) throws Exception {
		Map<String, String> configs = readConfigurations();
		if("No".equalsIgnoreCase(configs.get("enableOptOut"))) {
			return;
		}
		
		String userid = params.get("userid");
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();

		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());

		String timeStamp = simpleDateFormatter.format(today);
		prep = prepareStatement("INSERT INTO OptOutUsers (userid,optoutdate) Values (?,?)");

		prep.setString(1, userid);
		prep.setString(2, timeStamp);
		prep.executeUpdate();
	}

	private void removeAllOptOutUsers() throws Exception {
		prep = prepareStatement("DELETE FROM OptOutUsers");
		prep.executeUpdate();
	}
	
	@SuppressWarnings("unused")
	private void removeOptOutUser(HashMap<String, String> params) throws Exception {
		String userid = params.get("userid");
		prep = prepareStatement("DELETE FROM OPTOUTUSERS WHERE trim(USERID)=?");
		prep.setString(1, userid);
		prep.executeUpdate();
	}

	@SuppressWarnings("unused")
	private Map<String, Boolean> getOptOutUser(HashMap<String, String> params) throws Exception {	
		Map<String, Boolean> result = new HashMap<String, Boolean>();
		
		boolean enableOptOut = true;	
		prep = prepareStatement("SELECT CONFIGVAL FROM CONFIGURATIONS WHERE CONFIGKEY=?");
		prep.setString(1, "enableOptOut");
		ResultSet rs = prep.executeQuery();
		while ((rs != null) && rs.next()) {
			enableOptOut = "Yes".equalsIgnoreCase(rs.getString("CONFIGVAL"));
		}
		result.put("enableOptOut", enableOptOut);
				
		if(enableOptOut) {
			String userid = params.get("userid");
			prep1 = prepareStatement("SELECT USERID,OPTOUTDATE FROM OPTOUTUSERS WHERE trim(USERID)=?");
			prep1.setString(1, userid);
			ResultSet rs1 = prep1.executeQuery();
			
			if (rs1 != null && rs1.next()) {
				result.put("isOptedOut", true);
			} else {
				result.put("isOptedOut", false);
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("unused")
	private void updateStatistics(HashMap<String, Object> params) throws Exception {
		String userid = (String) params.get("userid");
		String classid = (String) params.get("classid");
		JSONObject attrs = (JSONObject) params.get("attrs");
		
		updateStatistics(userid, classid, attrs);
		
		if(params.containsKey("affectedItemClassid")) {
			classid = (String) params.get("affectedItemClassid");
			attrs = (JSONObject) params.get("affectedItemAttrs");
			
			releaseResources(false);
			
			updateStatistics(userid, classid, attrs);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void updateStatistics(String userid, String classid, JSONObject attrs) throws Exception {
		if(attrs != null && !attrs.isEmpty()) {
			log.debug("Getting existing Assist texts usage stats...");
			prep2 = prepareStatement("SELECT ATTRID, USAGECOUNT FROM USAGESTATS WHERE USERID=? AND CLASSID=?");
			prep2.setString(1, userid);
			prep2.setString(2, classid);
			
			ResultSet rs = prep2.executeQuery();
			Map<String, Integer> currentStats = new HashMap<String, Integer>();
			if (rs != null) {
				while(rs.next()) {
					String attrid = rs.getString("ATTRID");
					Integer count = rs.getInt("USAGECOUNT");
					currentStats.put(attrid, count);
				}
			}
			
			prep = prepareStatement("INSERT INTO USAGESTATS (USERID, CLASSID, ATTRID, USAGECOUNT, LASTACCESSDATE) VALUES (?,?,?,?,?)");
			prep1 = prepareStatement("UPDATE USAGESTATS SET USAGECOUNT=?, LASTACCESSDATE=? WHERE USERID=? AND CLASSID=? AND ATTRID=?");
			
			boolean runInserts = false;
			boolean runUpdates = false;
			
			for(Object obj : attrs.entrySet()) {
				Entry entry = (Entry) obj;
				String attrid = entry.getKey().toString();
				Integer count = Integer.parseInt(entry.getValue().toString());
				
				if(currentStats.containsKey(attrid)) {
					count += currentStats.get(attrid);
					prep1.setInt(1, count);
					prep1.setDate(2, new java.sql.Date(new Date().getTime()));
					prep1.setString(3, userid);
					prep1.setString(4, classid);
					prep1.setString(5, attrid);
					prep1.addBatch();
					
					runUpdates = true;
				} else {
					prep.setString(1, userid);
					prep.setString(2, classid);
					prep.setString(3, attrid);
					prep.setInt(4, count);
					prep.setDate(5, new java.sql.Date(new Date().getTime()));
					prep.addBatch();
					
					runInserts = true;
				}
			}
			
			if(runInserts)
				prep.executeBatch();
			if(runUpdates)
				prep1.executeBatch();
						
			conn.commit();
		} else {
			log.debug("No need to update Assist texts usage stats");
		}
	}
	
	@SuppressWarnings({"unused", "unchecked"})
	private Map<String, List<JSONObject>> generateUsageReport() throws Exception {
		log.debug("Generating Usage Report...");
		Map<String, List<JSONObject>> report = new HashMap<String, List<JSONObject>>();
		List<JSONObject> rows = new ArrayList<JSONObject>();
		
		prep = prepareStatement("SELECT USERID, CLASSID, ATTRID, USAGECOUNT, LASTACCESSDATE FROM USAGESTATS ORDER BY USERID ASC");		
		ResultSet rs = prep.executeQuery();
		
		IAdmin admin = AgileHandler.getAgileSession().getAdminInstance();
		DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		Map<String, IAgileClass> aclass_cache = new HashMap<String, IAgileClass>();
		Map<String, String> attr_cache = new HashMap<String, String>();
		
		if (rs != null) {
			while(rs.next()) {
				JSONObject row = new JSONObject();
				
				String userId = rs.getString("USERID");
				String classId = rs.getString("CLASSID");
				String attrId = rs.getString("ATTRID");
				int count = rs.getInt("USAGECOUNT");
				java.sql.Date lastAccessDate = rs.getDate("LASTACCESSDATE");
				
				IAgileClass aclass = null;
				if(aclass_cache.containsKey(classId)) {
					aclass = aclass_cache.get(classId);
				} else {
					aclass = admin.getAgileClass(Integer.parseInt(classId));
					aclass_cache.put(classId, aclass);
				}
				
				if(aclass == null) {
					log.info("Class not found: " + classId);
					continue;
				}
				
				String attrName = null;
				String key = classId + "." + attrId;
				if(attr_cache.containsKey(key)) {
					attrName = attr_cache.get(key);
				} else {
					IAttribute iattr = null;
					try {
						iattr = aclass.getAttribute(Integer.parseInt(attrId));
					} catch(Exception ex) {
						iattr = null;
					}
					if(iattr != null) {
						attrName = iattr.getFullName();
					}
					attr_cache.put(key, attrName);
				}
				
				if(attrName == null) {
					log.info("Attribute not found: " + key);
					continue;
				}
				
				row.put("userId", userId);
				row.put("classId", aclass.getName());
				row.put("attrId", attrName);
				row.put("count", count);
				row.put("lastAccessDate", format.format(new Date(lastAccessDate.getTime())));
				
				rows.add(row);
			}
		}
		
		report.put("report", rows);
		log.debug("Usage Report generated: " + rows.size());
		return report;
	}
	
	private void initializeConnection() throws SQLException, Exception {
		if (conn == null || conn.isClosed()) {
			conn = null;
			init();
		}
	}	
	
	private PreparedStatement prepareStatement(String statement) throws SQLException, Exception {
		initializeConnection();
		return conn.prepareStatement(statement);
	}
	
	private class infoMapBean implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		
		private String attrId;
		private String assistText;
		private String fontcolor;
		private String background;
		private String lastUpdated;
		private String classId;

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

	public static void main(String args[]) {
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("userid", "admin");
			dbh.handleDBRequest("getOptOutUser", params, false);
		} catch(Exception ex) {
			
		}
	}
}
