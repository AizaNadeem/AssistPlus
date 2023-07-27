package com.XACS.Assist.Handler;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.XACS.Assist.DO.AssistColorEntry;
import com.XACS.Assist.DO.AssistTextEntry;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;

public class DBHandler {
	Connection			conn	= null;
	Statement			stat	= null;
	Statement 			stat1 	= null;

	Statement 			stat2 	= null;
	PreparedStatement	prep	= null;
	AssistLogger log=AssistLogger.getInstance();

	public DBHandler() throws Exception {
		init();
	}


	private void init() throws Exception,SQLException 
	{
		try 
		{
			String homePath = ConfigHelper.getAppHomePath();
			Properties prop = new Properties();
			prop.load(new FileInputStream(homePath+Constants.General.PropertyFileName));
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("connstring") + homePath + Constants.DB.DB_NAME);// "xacs.assist.db"
			
			log.info("DB Connection Created..");
		} catch (ClassNotFoundException e) 
		{
			log.error( "ClassNotFoundException in init() : ", e);
			throw new Exception(Constants.DB.MSG_DRIVER_NOT_INSTALLED);
		}
	}

	private Map<String, String> readConfigurations() throws Exception,SQLException 
	{
		log.debug("Entering readConfigurations...");

		HashMap<String, String> configMap = null;
		ResultSet rs = stat.executeQuery("SELECT ConfigKey, ConfigVal " + "FROM Configurations WHERE ConfigKey <> 'LNFO' ORDER BY rowid DESC");
		configMap = new HashMap<String, String>();
		while ((rs != null) && rs.next()) 
		{
			configMap.put(rs.getString("ConfigKey"), rs.getString("ConfigVal"));
		}
		
		log.debug("Configurations: "+configMap.toString());
		log.debug("Exiting readConfigurations...");

		return configMap;
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
	 * @param workflowSTatusID 
	 * @param workflowID 
	 ***/
	
	private Map<String, ArrayList<String>> getAssistInfoMap(HashMap<String,Object> params) throws Exception,SQLException 
	{
		log.debug("Entering getAssistInfoMap...");

		HashMap<String, ArrayList<String>> asInfo = null;
		ArrayList<String> classes=(ArrayList<String>) params.get("classes");
		ArrayList<String> roles=(ArrayList<String>) params.get("roles");
		String allRoleKey=(String) params.get("allRoleKey");
		String workflowID=(String) params.get("workflowID");
		String workflowSTatusID=(String) params.get("workflowSTatusID");
		
		String rolestr = roles.toString().replace("'", "''").replace('[', ' ').replace(']', ' ').replace(", ", "\',\'").trim();
		String clsstr = classes.toString().replace('[', ' ').replace(']', ' ').trim();
		
		String quer = "SELECT AttrP.AttrID, AssistText.AssistText,"
				+ " case when AssistText.isDiffColor then AssistText.fontcolor else RolePriority.fontcolor end as fontcolor,"
				+ " case when AssistText.isDiffColor then AssistText.background else RolePriority.background end as background "
				+ "FROM ( SELECT ClassID, AttrID, MAX(Priority) Priority " 
				+ "FROM RoleTextMap " 
				+ "INNER JOIN RolePriority "
				+ "ON RoleTextMap.RoleID = RolePriority.RoleID " 
				+ "INNER JOIN AssistText " 
				+ "ON RoleTextMap.TextID = AssistText.TextID " 
				+ "WHERE Role IN ('" + rolestr + "','"+allRoleKey+"') " 
				+ "AND ClassID IN (" + clsstr + ") "
				+ "AND (workflowId IN ('" + workflowID + "','All Workflows','') OR workflowId IS NULL) "
				+ "AND (workflowStatusID IN ('" + workflowSTatusID + "','All Statuses','') OR workflowStatusID IS NULL) "
				+ "GROUP BY ClassID, AttrID ORDER BY RoleTextMap.TextID) AttrP "
				+ "INNER JOIN AssistText " 
				+ "ON AttrP.AttrID = AssistText.AttrID " 
				+ "AND AttrP.ClassID=AssistText.ClassID "
				+ "INNER JOIN RolePriority " 
				+ "ON AttrP.Priority = RolePriority.Priority "
				+ "INNER JOIN RoleTextMap " 
				+ "ON RoleTextMap.RoleID = RolePriority.RoleID " 
				+ "AND RoleTextMap.TextID = AssistText.TextID "
				+ "WHERE (workflowId IN ('" + workflowID + "','All Workflows','') OR workflowId IS NULL) "
				+ "AND (workflowStatusID IN ('" + workflowSTatusID + "','All Statuses','') OR workflowStatusID IS NULL) ";
		
		log.debug(" getAssistInfoMap query=["+quer+"]");
		ResultSet rs = stat.executeQuery(quer);
		ArrayList<String> roleAttributes;
		String attrId;
		String assistText;
		String fontcolor;
		String background;
		asInfo = new HashMap<String, ArrayList<String>>();
		
		while ((rs != null) && rs.next()) 
		{
			attrId = rs.getString("AttrID");
			assistText = rs.getString("AssistText");
			fontcolor = rs.getString("fontcolor");
			background = rs.getString("background");
			HashMap params2=new HashMap<String,String>();
			params2.put("key", "hoverColor");
			HashMap map=this.handleDBRequest("getConfigByKey", params2, false);
			roleAttributes = new ArrayList<String>();
			
			roleAttributes.add(assistText);
			roleAttributes.add(fontcolor);
			roleAttributes.add(background);
			roleAttributes.add((String) map.get("value"));
			asInfo.put(attrId, roleAttributes);
		}
		
		log.debug("AssistInfo: "+asInfo.toString());
		log.debug("Exiting getAssistInfoMap...");

		return asInfo;
	}

	private Map<String,ArrayList<AssistTextEntry>> getAssistTexts(HashMap<String,String> params) throws Exception,SQLException {
		ArrayList<AssistTextEntry> ateArr = null;
		ArrayList<String> strRoleArr;
		String classId=(String) params.get("classID");
		String attrId=(String) params.get("attrId");
		ResultSet rs = stat.executeQuery("SELECT TextID, AssistText, isDiffColor, fontcolor, background,workflowId,workflowStatusID,DateCreated,LastUpdated " + "FROM AssistText " + "WHERE ClassID=" + classId
				+ " AND AttrID=" + attrId);
		ateArr = new ArrayList<AssistTextEntry>();
		AssistTextEntry ate = null;
		while ((rs != null) && rs.next()) {
			ate = new AssistTextEntry();
			ate.setTextID(rs.getString("TextID"));
			ate.setAssistText(rs.getString("AssistText"));
			ate.setFontColor(rs.getString("fontcolor"));
			ate.setBackgroundColor(rs.getString("background"));
			ate.setIsDifferentColor(rs.getBoolean("isDiffColor"));
			ate.setWorkflowID(rs.getString("workflowId"));
			ate.setWorkflowStatusID(rs.getString("workflowStatusID"));
			ate.setDateCreated(rs.getString("DateCreated"));
			ate.setLastUpdated(rs.getString("LastUpdated"));
			initSecondStatement();
			ResultSet rs1 = stat1.executeQuery("SELECT RP.RoleID value, Role label,  CASE WHEN (ifnull(TM.RoleID,-1) == -1) THEN 0 ELSE 1 END AS selected "
					+ "FROM RolePriority RP " + "LEFT OUTER JOIN (SELECT RoleID from RoleTextMap WHERE TextID=" + ate.getTextID() + ") TM "
					+ "ON RP.RoleID = TM.RoleID ");
			strRoleArr = new ArrayList<String>();
			while ((rs1 != null) && rs1.next()) {
				String str = "<option value=\"" + rs1.getString("value") + "\""
						+ ((rs1.getString("selected").equalsIgnoreCase("0")) ? "" : " selected=\"selected\"") + " >" + rs1.getString("label") + "</option>";
				strRoleArr.add(str);
			}
			String[] strarr = new String[strRoleArr.size()];
			ate.setRoles(strRoleArr.toArray(strarr));
			ate.setClassID(classId);
			ate.setAttrID(attrId);
			ateArr.add(ate);
		}

		HashMap <String,ArrayList<AssistTextEntry>> retHash=new HashMap<String,ArrayList<AssistTextEntry>>();
		retHash.put("ateArr", ateArr);
		return retHash;
	}

	private Map<String,String[]> getRoleOptions() throws Exception,SQLException 
	{
		log.debug("Entering getRoleOptions...");

		String[] strRoles = null;
		String str;
		HashMap<String,String[]> retMap=new HashMap<String,String[]>();
		ResultSet rs = stat.executeQuery("SELECT RoleID value, Role label " + "FROM RolePriority RP;");
		ArrayList<String> strRoleArr = new ArrayList<String>();
		while ((rs != null) && rs.next()) {
			str = "<option value=\"" + rs.getString("value") + "\"" + ">" + rs.getString("label") + "</option>";
			strRoleArr.add(str);
		}
		strRoles = new String[strRoleArr.size()];
		strRoles = strRoleArr.toArray(strRoles);
		
		log.debug("RoleOptions"+strRoles.toString());
		retMap.put("strRoles", strRoles);
		log.debug("Exiting getRoleOptions...");

		return retMap;
	}

	private Map<String, ArrayList<String>> getRolePriorities() throws SQLException,Exception {
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
		log.debug("Role Priorities: "+roleMap.toString());
		log.debug("Exiting getRolePriorities...");

		return roleMap;
	}

	private Map <String,ArrayList<Integer>> checkClassesAssistText() throws SQLException ,Exception{
		log.debug("Entering checkClassesAssistText...");

		ArrayList<Integer> i = new ArrayList<Integer>();
		HashMap<String, ArrayList<Integer>> ret=new HashMap<String,ArrayList<Integer>>();
		ResultSet rs = stat.executeQuery("SELECT DISTINCT ClassID FROM AssistText WHERE ClassID NOT NULL");
		while ((rs != null) && rs.next()) {
			i.add(rs.getInt("ClassID"));
		}
		ret.put("result", i);
		log.debug("checkClassesAssistText: "+i.toString());
		log.debug("Exiting checkClassesAssistText...");

		return ret;
	}

	private Map<String, Object> getAssistColors(HashMap<?, ?> params) throws SQLException,Exception
	{
		log.debug("Entering getAssistColors...");

		String classId=(String) params.get("classId");
		String selectQry="";
		String clsStr="";
		boolean mClasses=(Boolean) params.get("mClasses");
		Map<String, Object> attColors=new HashMap<String, Object>(0);
		if(conn!=null)
		{		
			if(mClasses)
			{
				clsStr = classId.replace('[', ' ').replace(']', ' ').trim();
				selectQry="SELECT ColorID, AttrID, AssistColor FROM AssistColor WHERE ClassID IN ("+clsStr+")";
			}
			else
			{
				selectQry="SELECT ColorID, AttrID, AssistColor FROM AssistColor WHERE ClassID="+classId;
			}
			
			log.debug("getAssistColors select query: "+selectQry);
			
			ResultSet rs=null;
			AssistColorEntry colorEntry=null;
			rs=stat.executeQuery(selectQry);
				
				if(rs!=null)
				{
					while(rs.next())
					{
						if(mClasses)
						{
							attColors.put(rs.getString("AttrID"), rs.getString("AssistColor")+"");
						}
						else
						{
							colorEntry=new AssistColorEntry();
							
							colorEntry.setColorId(rs.getString("ColorID"));
							colorEntry.setAttributeId(rs.getString("AttrID"));
							colorEntry.setAssistColor(rs.getString("AssistColor"));
							
							attColors.put(colorEntry.getAttributeId(), colorEntry);
						}
					}
				}
		}
		
		log.debug("Attribute Colors: "+attColors.toString());
		
		log.debug("Exiting getAssistColors...");

		return attColors;
	}
	private Map <String,ArrayList<Integer>> checkAssistText(HashMap params) throws SQLException,Exception {
		log.debug("Entering checkAssistText...");

		int classID=(Integer) params.get("classID");
		ArrayList<Integer> i = new ArrayList<Integer>();
		HashMap<String, ArrayList<Integer>> result=new HashMap<String,ArrayList<Integer>>();
		ResultSet rs = stat.executeQuery("SELECT DISTINCT AttrID FROM AssistText WHERE ClassID='" + classID + "'");
		
		log.debug("checkAssistText Selecct Query: "+"SELECT DISTINCT AttrID FROM AssistText WHERE ClassID='" + classID + "'");
		while ((rs != null) && rs.next()) {
			i.add(rs.getInt("AttrID"));
		}
		result.put("result", i);
		
		log.debug("Check Assist Text: "+result.toString());
		log.debug("Exiting checkAssistText...");

		return result;
	}
	private Map<String, Integer> addNewAssistColor(HashMap params) throws Exception,SQLException 
	{
		log.debug("Entering addNewAssistColor...");

		int res=-1;
		String classID=(String) params.get("classID");
		JSONArray jsonAttColors=(JSONArray) params.get("jsonAttColors");
		JSONObject attColor=null; 
		
		String colorId="";
		String attId="";
		String assistColor="";
		
		String insertQry="INSERT INTO AssistColor (ClassID,AttrID,AssistColor) VALUES (?,?,?)";
		
		if(conn!=null && jsonAttColors!=null && jsonAttColors.size()>0)
		{
			
			deleteColor(classID);
			for(int attColorIndex=0;attColorIndex<jsonAttColors.size();attColorIndex++)
			{
				attColor=(JSONObject)jsonAttColors.get(attColorIndex);
				
				colorId=attColor.get("colorId")+"";
				attId=attColor.get("attId")+"";
				assistColor=attColor.get("assistColor")+"";
				if(!assistColor.equalsIgnoreCase("#666666"))
				{
					log.debug("addNewAssistColor Insert/Update ::: Color Id=["+colorId+"], Attribute Id=["+attId+"], Assist Color=["+assistColor+"]");
					
					if(prep==null)
					{
						prep=conn.prepareStatement(insertQry);
					}
					
					prep.setString(1, classID);
					prep.setString(2, attId);
					prep.setString(3, assistColor);
					
					res=prep.executeUpdate();
					log.debug("Insert/Update AssistColor result=["+res+"]");
				}
			}
		}
		HashMap<String, Integer> result=new HashMap<String,Integer>();
		result.put("result", res);
		
		
		log.debug("Exiting addNewAssistColor...");

		return result;
	}
	private boolean deleteColor(String classID) throws SQLException,Exception
	{
		try {
			log.debug("Entering deleteColor...");

			String strStatament="DELETE FROM AssistColor WHERE ClassID=" +classID+";";
			log.debug("deleteColor delete Query: "+"DELETE FROM AssistColor WHERE ClassID=" +classID+";");
			initFirstStatement();
			ResultSet rs = stat.executeQuery("SELECT ColorID " + "FROM AssistColor " + "WHERE ClassID=" +classID+";");
			if(rs != null && rs.next())
			{				
				stat.addBatch(strStatament);
				conn.setAutoCommit(false);
				stat.executeBatch();
				conn.setAutoCommit(true);
				stat.close();
			}
			rs.close();
			log.debug("Exiting deleteColor...");

			return true;
		} catch (Exception e) {
			
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			log.debug("Exiting deleteColor...");

			return false;
		}
	}
	private Map<String,Integer> addNewAssistText(HashMap params) throws Exception,SQLException 
	{
		Integer textId = -1;
		HashMap<String, Integer> result=new HashMap<String,Integer>();
		String classID=(String) params.get("classID");
		String attrID=(String) params.get("attrID");
		String assistText=(String) params.get("assistText");
		String fontColor=(String) params.get("fontColor");
		String backgroundColor=(String) params.get("backgroundColor");
		boolean isDiffColor=(Boolean) params.get("isDiffColor");
		String workflowID=(String) params.get("workflowID");
		String workflowStatusID=(String) params.get("workflowStatusID");
	
		String query = "SELECT TextID " +
				"FROM AssistText " +
				"WHERE ClassID=" + classID + 
						" AND AttrID=" + attrID +
						" AND AssistText='" +assistText.replace("'","''")+ "'" +
						" AND workflowID='" + workflowID.replace("'","''") +"'" +
						" AND workflowStatusID='" + workflowStatusID.replace("'","''") +"'" +
				" ORDER BY TextID DESC";
		
		prep = conn.prepareStatement("INSERT INTO AssistText (ClassID, AttrID, AssistText,fontcolor,background,isDiffColor,workflowID,workflowStatusID,DateCreated,LastUpdated) Values (?,?,?,?,?,?,?,?,?,?);");
		String timeStamp = new SimpleDateFormat("d MMM yyyy hh:mm aaa").format(Calendar.getInstance().getTime());
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
		conn.setAutoCommit(true);
		ResultSet rs = stat.executeQuery(query);
		if ((rs != null) && rs.next()) 
		{
			textId = rs.getInt("TextID");
		}
		result.put("textId",textId);
		return result;
	}

	private void removeAssistText(HashMap params) throws Exception,SQLException {
		log.debug("Entering removeAssistText...");

			String textID=(String) params.get("textID");
			stat.addBatch("DELETE FROM AssistText WHERE TextID=" + textID + ";");
			log.debug("removeAssistText: DELETE FROM AssistText WHERE TextID=" + textID + ";");
			
			stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
			log.debug("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
			stat.executeBatch();
			log.debug("Exiting removeAssistText...");
	}

	private void updateAssistText(HashMap params)throws Exception,SQLException {
			String textID=(String) params.get("textID");
			String assistText=(String) params.get("assistText");
			String fontColor=(String) params.get("fontColor");
			String backgroundColor=(String) params.get("backgroundColor");
			boolean isDiffColor=(Boolean) params.get("isDiffColor");
			String workflowID=(String) params.get("workflowID");
			String workflowStatusID=(String) params.get("workflowStatusID");
			prep = conn.prepareStatement("Update AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?,workflowID=?,workflowStatusID=?, LastUpdated=?   WHERE TextID=?;");
			String timeStamp = new SimpleDateFormat("d MMM yyyy hh:mm aaa").format(Calendar.getInstance().getTime());
			prep.setString(1, assistText);
			prep.setString(2, fontColor);
			prep.setString(3, backgroundColor);
			prep.setBoolean(4, isDiffColor);
			
			if(workflowID==null || workflowID.equals("") || workflowID.equals("null"))
			{
				prep.setString(5, "All Workflows");
			}
			else
			{
				prep.setString(5, workflowID);
			}
			prep.setString(6, workflowStatusID);
			prep.setString(7,timeStamp);
			prep.setString(8, textID);
			prep.executeUpdate();
	}

	private void updateAllAssistText(HashMap params) throws Exception,SQLException {
		log.debug("Entering updateAllAssistText...");

			ArrayList<AssistTextEntry> rows=(ArrayList<AssistTextEntry>) params.get("rows");
			String text;
			String textid;
			String fontcolor;
			String background;
			String rolestr = "";
			boolean isDiffColor;
			String[] roles;
			ResultSet rs;
			for (int t = 0; t < rows.size(); t++) {
				text = rows.get(t).getAssistText();
				textid = rows.get(t).getTextID();
				fontcolor = rows.get(t).getFontColor();
				background = rows.get(t).getBackgroundColor();
				isDiffColor = rows.get(t).getIsDifferentColor();
				textid = textid.replace("row", "");
				roles = rows.get(t).getRoles();
				rs = stat.executeQuery("SELECT TextID FROM AssistText WHERE TextID=" + textid);
				log.debug("updateAllAssistText: SELECT TextID FROM AssistText WHERE TextID=" + textid);
				stat.close();
				if (rs != null) {
					prep = conn.prepareStatement("Update AssistText SET AssistText=?, isDiffColor=?,fontcolor=?,background=? WHERE TextID=?;");
					prep.setString(1, text);
					prep.setBoolean(2, isDiffColor);
					prep.setString(3, fontcolor);
					prep.setString(4, background);
					prep.setString(5, textid);
					prep.addBatch();
					initFirstStatement();
					stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textid + ";");
					log.debug(" updateAllAssistText: DELETE FROM RoleTextMap WHERE TextID=" + textid + ";");
					if (roles != null) {
						rolestr = "";
						for (String str : roles) {
							rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
						}
						stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textid + ", RoleID " + "FROM RolePriority "
								+ "WHERE RoleID IN (" + rolestr + ");");
						log.debug("updateAllAssistText: INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textid + ", RoleID " + "FROM RolePriority "
								+ "WHERE RoleID IN (" + rolestr + ");");
						
					}
					prep.executeBatch();
					stat.executeBatch();
				}
			}
			log.debug("Exiting updateAllAssistText...");
	}

	private void updateTextRoleList(HashMap params) throws Exception,SQLException {
		log.debug("Entering updateTextRoleList...");

		String textID=(String) params.get("textID");
		String rolestr;
		String[] roleList=(String[]) params.get("roleList");
		stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
		
		log.debug("updateTextRoleList: DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
		if (roleList != null) {
			rolestr = "";
			for (String str : roleList) {
				rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
			}
			stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN ("
					+ rolestr + ");");
			log.debug("updateTextRoleList: INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN ("
					+ rolestr + ");");
			
		}
		stat.executeBatch();
		log.debug("Exiting updateTextRoleList...");

	}

	private void updateRolePriority(HashMap <String,ArrayList<RoleEntry>> params) throws Exception,SQLException {
		log.debug("Entering updateRolePriority...");

			ArrayList<RoleEntry> roleList=params.get("roleList");
			String roleIds = "";
			for (RoleEntry role : roleList) {
				roleIds = roleIds + (roleIds.equals("") ? "" : ",") + role.getRoleID();
			}
			ResultSet rs = stat.executeQuery("SELECT RoleID " + "FROM RolePriority " + "WHERE RoleID NOT IN (" + roleIds + ");");
			String delRoleIds = "";
			while ((rs != null) && rs.next()) {
				delRoleIds = delRoleIds + (delRoleIds.equals("") ? "" : ",") + rs.getString("RoleID");
			}
			stat.close();
			initFirstStatement();
			if (!delRoleIds.equals("")) {
				stat.addBatch("DELETE FROM RoleTextMap " + "WHERE RoleID IN(" + delRoleIds + ");");
				stat.addBatch("DELETE FROM AssistText " + "WHERE TextID NOT IN ( " + "SELECT TextID " + "FROM RoleTextMap " +
				");");
			}
			stat.addBatch("DELETE FROM RolePriority;");
			
			for (RoleEntry role : roleList) {
				log.debug("updateRolePriority Role: "+role.getRole());
				
				stat.addBatch("INSERT INTO RolePriority (RoleID, Role, Priority,fontcolor,background) " + "VALUES (" + role.getRoleID() + ",'"
						+ role.getRole().replace("'", "''") + "'," + role.getPriority() + ",'" + role.getFontColor() + "','" + role.getBackgroundColor()
						+ "' );");
				
				log.debug("updateRolePriority: INSERT INTO RolePriority (RoleID, Role, Priority,fontcolor,background) " + "VALUES (" + role.getRoleID() + ",'"
						+ role.getRole().replace("'", "''") + "'," + role.getPriority() + ",'" + role.getFontColor() + "','" + role.getBackgroundColor()
						+ "' );");
			}
			stat.executeBatch();
			log.debug("Exiting updateRolePriority...");

		}
	private void insertConfig(HashMap params) throws SQLException,Exception
	{
		log.debug("Entering insertConfig.. ");
		String key=(String) params.get("key");
		String value=(String) params.get("value");
		String configVal=null;
			if(key.equals("accessType"))
			{
				ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'accessType'");
				while ((rs != null) && rs.next())
				{
					configVal= rs.getString("ConfigVal");
				}	
			}
		
			if(configVal==null)
			{
				log.info("Changing Access Type..");
			}
		String insertConfigQry="";
		if(conn!=null)
		{
				// for single SQL operation, preferring Statement over Prepared Statement
				insertConfigQry="INSERT INTO Configurations (ConfigKey,ConfigVal) VALUES ('"+key+"','"+value+"')";
				log.debug("insertConfig: INSERT INTO Configurations (ConfigKey,ConfigVal) VALUES ('"+key+"','"+value+"')");
				stat.executeUpdate(insertConfigQry);	
		}
		if(configVal==null)
		{
			log.info("Access Type Changed..");
		}
		log.debug("Exiting insertConfig.. ");
	}
	private Map<String, String> getConfigByKey(HashMap params) throws SQLException,Exception
	{
		log.debug("Entering getConfigByKey.. ");

		String value="";
		String key=(String) params.get("key");
		String selectConfigQry="";
		ResultSet rs;
		HashMap<String, String> result=new HashMap<String,String>();
		if(conn!=null)
		{
				// for single SQL operation, preferring Statement over Prepared Statement
				selectConfigQry="SELECT ConfigVal FROM Configurations WHERE ConfigKey='"+key+"'";
				log.debug("getConfigByKey: SELECT ConfigVal FROM Configurations WHERE ConfigKey='"+key+"'");
				rs=stat.executeQuery(selectConfigQry);
				if(rs!=null && rs.next())
				{
					value=rs.getString("ConfigVal");
				}
				
		}
		result.put("value", value);
		
		log.debug("Configuration: "+result.toString());
		
		log.debug("Exiting getConfigByKey.. ");

		return result;
	}
	private void updateConfigurations(HashMap<?, ?> prams) throws Exception,SQLException {
		log.debug("Entering updateConfigurations.. ");

			String[] configs=(String[]) prams.get("configs");
			String[] params;
			
			String configVal=null;
			
			for (String config : configs) 
			{
				params = config.split("=",2);
				if(params[0].equals("accessType"))
				{
					ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'accessType'");
					while ((rs != null) && rs.next())
					{
						configVal= rs.getString("ConfigVal");
					}	
				}
			}
			
			if(configVal==null)
			{
				log.info("Changing Access Type..");
			}
			prep = conn.prepareStatement("UPDATE Configurations SET ConfigVal=? WHERE ConfigKey=?");
			for (String config : configs) 
			{
				params = config.split("=",2);
							
				log.debug("Update Configuration Parameters: "+"["+params[1]+"] , ["+params[0]+"]");
				
				prep.setString(1, params[1]);
				prep.setString(2, params[0]);
				prep.addBatch();
			}
			prep.executeBatch();
			
			if(configVal==null)
			{
				log.info("Access Type Changed..");
			}
			log.debug("Exiting updateConfigurations.. ");

	}
	void updateDefaultRolePriority(HashMap params) throws SQLException,Exception
	{
		log.debug("Entering updateDefaultRolePriority.. ");

		String newRole=(String) params.get("newRole");
		String query="";
		int res;
		if(conn!=null)
		{
			query="UPDATE RolePriority set Role = '"+newRole.replace("'","''")+"' WHERE RoleID =0";
			log.debug("updateDefaultRolePriority: UPDATE RolePriority set Role = '"+newRole.replace("'","''")+"' WHERE RoleID =0");
			res=stat.executeUpdate(query);
			
			log.info("Role Priority updated=["+res+"]");
		}
		log.debug("Exiting updateDefaultRolePriority.. ");

	}
	private void updateLicinfo(HashMap params) throws Exception,SQLException {
		log.debug("Entering updateLicinfo.. ");

		String licinfo=(String) params.get("licinfo");
		stat.addBatch("DELETE FROM Configurations WHERE ConfigKey='LNFO';");
		if (licinfo != null) {
			stat.addBatch("INSERT INTO Configurations (ConfigKey, ConfigVal) " + "VALUES('LNFO','" + licinfo.replace("'", "''") + "')");
			log.debug("updateLicinfo: INSERT INTO Configurations (ConfigKey, ConfigVal) " + "VALUES('LNFO','" + licinfo.replace("'", "''") + "')");
		}
		stat.executeBatch();
		log.debug("Exiting updateLicinfo.. ");

	}

	private Map<String,String> getLicInfo() throws Exception,SQLException {
		log.debug("Entering getLicInfo.. ");

		String strLic = null;
		HashMap<String, String> retMap=new HashMap<String,String>();
		ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'LNFO'");
		while ((rs != null) && rs.next()) {
			strLic = rs.getString("ConfigVal");
		}
		retMap.put("strLic", strLic);
		log.debug("Exiting getLicInfo.. ");

		return retMap;
	}

	private void deleteAssistColor(HashMap params) throws SQLException,Exception
	{
		log.debug("Entering deleteAssistColor...");

		String colorID=(String) params.get("colorID");
		stat.addBatch("DELETE FROM AssistColor WHERE ColorID=" + colorID + ";");
		log.debug("deleteAssistColor: DELETE FROM AssistColor WHERE ColorID=" + colorID + ";");
		stat.executeBatch();
		log.debug("Exiting deleteAssistColor...");
	}

	private void deleteRolePriority(HashMap params) throws SQLException,Exception
	{
		log.debug("Entering deleteRolePriority...");

		String roleID=(String) params.get("roleID");
		stat.addBatch("DELETE FROM RolePriority WHERE RoleID=" + roleID + ";");
		log.debug("deleteRolePriority: DELETE FROM RolePriority WHERE RoleID=" + roleID + ";");
		stat.executeBatch();
		log.debug("Exiting deleteRolePriority...");
	}
	private void mergeRolePriority(HashMap<String,Object> params) throws SQLException,Exception
	{
		log.debug("Entering mergeRolePriority...");
		
		List<RoleEntry> roleList=(List<RoleEntry>)params.get("role");
		
		List <String> roleIDList=(List<String>) params.get("roleIDList");
				
		for(int i=0;i<roleList.size();i++)
		{
			if(roleIDList.contains(roleList.get(i).getRoleID()))
			{
				String updateQuery="UPDATE RolePriority SET  Role=?, Priority=?, fontcolor=?, background=? WHERE RoleID=?";
				prep = conn.prepareStatement(updateQuery);
				prep.setString(1, roleList.get(i).getRole());
				prep.setString(2, String.valueOf(roleList.get(i).getPriority()));
				prep.setString(3, roleList.get(i).getFontColor());
				prep.setString(4, roleList.get(i).getBackgroundColor());
				prep.setString(5, roleList.get(i).getRoleID());

				log.debug("updatetQuery: "+updateQuery);
				prep.executeUpdate();
			}
			else
			{
				String insertQuery="INSERT into RolePriority (RoleID, Role, Priority, fontcolor, background ) VALUES ( ";

				insertQuery+=" '"+roleList.get(i).getRoleID()+"',";			
				insertQuery+=" '"+roleList.get(i).getRole()+"',";					
				insertQuery+=" '"+roleList.get(i).getPriority()+"',";			
				insertQuery+=" '"+roleList.get(i).getFontColor()+"',";					
				insertQuery+=" '"+roleList.get(i).getBackgroundColor()+"',";					
				insertQuery=insertQuery.substring(0,insertQuery.length()-1);
				insertQuery+=" )";

				log.debug("insertQuery: "+insertQuery);
				initFirstStatement();
				stat.addBatch(insertQuery);
				stat.executeBatch();
			}
		}
		
		
		log.debug("Exiting mergeRolePriority...");

			
	}
	private Map<String,List<AssistTextEntry>> getAssistTextMap() throws SQLException,Exception
	{
		HashMap<String,List<AssistTextEntry>> dbAssistTextList=new HashMap<String,List<AssistTextEntry>>();
		
		List<AssistTextEntry> textList= new ArrayList<AssistTextEntry>();
		
		ResultSet rset = stat.executeQuery("SELECT AssistText.TextID,ClassID,AttrID,workflowID,workflowStatusID FROM AssistText");
		while(rset.next())  
		{
			AssistTextEntry text=new AssistTextEntry();
			text.setTextID(rset.getObject("TextID").toString());
			text.setAttrID(rset.getObject("AttrID").toString());
			text.setClassID(rset.getObject("ClassID").toString());
			text.setWorkflowID(rset.getObject("workflowID").toString());
			text.setWorkflowStatusID(rset.getObject("workflowStatusID").toString());
			textList.add(text);
		}
		dbAssistTextList.put("textList", textList);	
		return dbAssistTextList;
				
	}

	
	private Map<String,List<String>>getRoleIDs() throws SQLException,Exception
	{
		HashMap<String,List<String>> roleIds=new HashMap<String,List<String>>();
		ResultSet rset = stat.executeQuery("SELECT RoleID FROM RolePriority" );
		
		List <String> roleIDList = new ArrayList<String>();
		while ((rset != null) && rset.next()) 
		{
			roleIDList.add(rset.getString("RoleID"));
		}
		roleIds.put("roleIDList", roleIDList);
		return roleIds;
	}
	private Map<String,List<String>>getColorIDs() throws SQLException,Exception
	{
		ResultSet rset = stat.executeQuery("SELECT ColorID FROM AssistColor" );
		HashMap<String,List<String>> colorIds=new HashMap<String,List<String>>();
		List <String> colorIDList = new ArrayList<String>();
		while ((rset != null) && rset.next()) 
		{
			colorIDList.add(rset.getString("ColorID"));
		}
		colorIds.put("colorIDList", colorIDList);
		return colorIds;
	}
	private void mergeAssistColor(HashMap<String,Object> params) throws SQLException,Exception
	{
		log.debug("Entering mergeAssistColor...");
	
		List<AssistColorEntry> colorList=(List<AssistColorEntry>)params.get("color");
		List <String> colorIDList=(List<String>) params.get("colorIDList");
			
		for(int i=0;i<colorList.size();i++)
		{
			if(colorIDList.contains(colorList.get(i).getColorId()))
			{
				String updateQuery="UPDATE AssistColor SET  ClassID=?, AttrID=?, AssistColor=? WHERE ColorID=?";
				prep = conn.prepareStatement(updateQuery);
				prep.setString(1,colorList.get(i).getClassId());
				prep.setString(2, colorList.get(i).getAttributeId());
				prep.setString(3, colorList.get(i).getAssistColor());
				prep.setString(4, colorList.get(i).getColorId());

				log.debug("updatetQuery: "+updateQuery);
				prep.executeUpdate();
			}
			else
			{
				String insertQuery="INSERT into AssistColor (ColorID, ClassID, AttrID, AssistColor ) VALUES ( ";

				insertQuery+=" '"+colorList.get(i).getColorId()+"',";
				insertQuery+=" '"+colorList.get(i).getClassId()+"',";			
				insertQuery+=" '"+colorList.get(i).getAttributeId()+"',";			
				insertQuery+=" '"+colorList.get(i).getAssistColor()+"',";			
				insertQuery=insertQuery.substring(0,insertQuery.length()-1);
				insertQuery+=" )";

				log.debug("insertQuery: "+insertQuery);
				initFirstStatement();
				stat.addBatch(insertQuery);
				stat.executeBatch();
			}

		}
		log.debug("Entering mergeAssistColor...");
			
	}
	
	private Map<String,List<String>> getTextRoles() throws SQLException,Exception
	{
		log.debug("Entering getTextRoles...");

		ResultSet rset = stat.executeQuery("SELECT TextID,RoleID FROM RoleTextMap" );
		HashMap<String,List<String>> textRoles= new HashMap<String,List<String>>();
		
		while(rset.next())
		{
			if(textRoles.containsKey(rset.getObject("TextID").toString()))
			{
				textRoles.get(rset.getObject("TextID").toString()).add(rset.getObject("RoleID").toString());
			}
			else
			{
				List<String> roles=new ArrayList<String>();
				roles.add(rset.getObject("RoleID").toString());
				textRoles.put(rset.getObject("TextID").toString(),roles);
			}
		}
		log.debug("Exiting getTextRoles...");

		
		return textRoles;
	}
	
	private void mergeUpdateText(HashMap<String,Object> params) throws SQLException,Exception
	{
		log.debug("Entering mergeUpdateText...");
		HashMap<String,AssistTextEntry> updateText=(HashMap<String, AssistTextEntry>) params.get("updateText");
		String updateQuery="UPDATE AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?, DateCreated=?, LastUpdated=? WHERE TextID=? "; 

		prep = conn.prepareStatement(updateQuery);
		String timeStamp = new SimpleDateFormat("d MMM yyyy hh:mm aaa").format(Calendar.getInstance().getTime());
		for(String Id:updateText.keySet())
		{
			
			prep.setString(1,StringEscapeUtils.unescapeXml( updateText.get(Id).getAssistText()));
			prep.setString(2, updateText.get(Id).getFontColor());
			prep.setString(3, updateText.get(Id).getBackgroundColor());
			prep.setBoolean(4, updateText.get(Id).getIsDifferentColor());		
			prep.setString(5, updateText.get(Id).getDateCreated());
			prep.setString(6,timeStamp);
			prep.setString(7, Id);
			prep.addBatch();
			log.debug("updatetQuery: "+Id);
		}
		prep.executeBatch();
		log.debug("Exiting mergeUpdateText...");

	}
	private void mergeInsertText(HashMap<String,Object> params) throws SQLException,Exception
	{
		log.debug("Entering mergeInsertText...");
		HashMap<String,AssistTextEntry> insertText=(HashMap<String,AssistTextEntry>) params.get("insertText");
		String timeStamp = new SimpleDateFormat("d MMM yyyy hh:mm aaa").format(Calendar.getInstance().getTime());
		for(String i:insertText.keySet())
		{
			String InsertNew="INSERT into AssistText(ClassID, AttrID, AssistText, fontcolor, background, isDiffColor, workflowID, workflowStatusID, DateCreated,LastUpdated) VALUES ( ";
			InsertNew+=" '"+insertText.get(i).getClassID()+"',";
			InsertNew+=" '"+insertText.get(i).getAttrID()+"',";
			InsertNew+=" '"+StringEscapeUtils.unescapeXml(insertText.get(i).getAssistText())+"',";
			InsertNew+=" '"+insertText.get(i).getFontColor()+"',";
			InsertNew+=" '"+insertText.get(i).getBackgroundColor()+"',";
			if(insertText.get(i).getIsDifferentColor())
				InsertNew+=" '1',";
			else
				InsertNew+=" '0',";	
			InsertNew+=" '"+insertText.get(i).getWorkflowID()+"',";
			InsertNew+=" '"+insertText.get(i).getWorkflowStatusId()+"',";
			InsertNew+=" '"+insertText.get(i).getDateCreated()+"',";
			InsertNew+=" '"+timeStamp+"',";
			InsertNew=InsertNew.substring(0,InsertNew.length()-1);
			InsertNew+=" )";
			log.debug("InsertNew "+InsertNew);
			stat.addBatch(InsertNew);
		}
		
		stat.executeBatch();
		
		String textId =null;
		initSecondStatement();
		for(String i:insertText.keySet())
		{
			String query = "SELECT TextID FROM AssistText WHERE ClassID='"+insertText.get(i).getClassID()+"' AND AttrID='" +insertText.get(i).getAttrID()+	"' AND workflowID='"+insertText.get(i).getWorkflowID()+"' AND workflowStatusID='"+insertText.get(i).getWorkflowStatusId()+"' AND AssistText='"+StringEscapeUtils.unescapeXml(insertText.get(i).getAssistText())+"'";
			log.debug(query);
			ResultSet rs = stat.executeQuery(query);
			if ((rs != null) && rs.next()) 
			{
				textId = rs.getObject("TextID").toString();
			}
			
			for(int j=0;j< insertText.get(i).getRolesList().size();j++)
			{
				
				log.debug("INSERT INTO RoleTextMap (TextID,RoleID) VALUES ('"+textId+"','"+insertText.get(i).getRolesList().get(j)+"')");
				if(textId!=null)
					stat1.addBatch("INSERT INTO RoleTextMap (TextID,RoleID) VALUES ('"+textId+"','"+insertText.get(i).getRolesList().get(j)+"')");
			}
			
		}
		stat1.executeBatch();
		log.debug("Exiting mergeInsertText...");

	}

	private Map<String,Boolean> isRoles() throws SQLException,Exception
	{
		log.debug("Entering isRoles...");

		HashMap<String, Boolean> rolesStatus=new HashMap<String,Boolean>();
		boolean isRoles=true;
		
		prep = conn.prepareStatement("SELECT ConfigVal FROM Configurations WHERE ConfigKey=?");
		prep.setString(1, "accessType");
		ResultSet rset2 = prep.executeQuery();
		while (rset2.next()) 
		{
			if( rset2.getObject("ConfigVal").toString().equals("roles"))
			{
				isRoles=true;
			}
			else
			{
				isRoles=false;
			}
		}
		
		rolesStatus.put("isRoles", isRoles);
		log.debug("Exiting isRoles...");
		return rolesStatus;
	}
	private HashMap<String,List<AssistColorEntry>> getAssistColor() throws SQLException,Exception
	{
		HashMap<String,List<AssistColorEntry>> colorList= new HashMap<String,List<AssistColorEntry>>();
		List<AssistColorEntry> AssistColorList=new ArrayList<AssistColorEntry>();	
		ResultSet rset = stat.executeQuery("SELECT * FROM AssistColor" );
		while(rset.next())  
		{
			AssistColorEntry color=new AssistColorEntry();
			color.setColorId(rset.getObject("ColorID").toString());
			color.setClassId(rset.getObject("ClassID").toString());
			color.setAttributeId(rset.getObject("AttrID").toString());
			color.setAssistColor(rset.getObject("AssistColor").toString());
			AssistColorList.add(color);
		
		}
		
		colorList.put("assistColor",AssistColorList );
		return colorList;
		
	}
	private HashMap<String,List<RoleEntry>> getRolePriority() throws SQLException,Exception
	{
		HashMap<String,List<RoleEntry>> roleList= new HashMap<String,List<RoleEntry>>();
		List<RoleEntry> RolePriorityList=new ArrayList<RoleEntry>();	
		ResultSet rset = stat.executeQuery("SELECT * FROM RolePriority" );
		while(rset.next())  
		{
			RoleEntry role=new RoleEntry();
			role.setRoleID(rset.getObject("RoleID").toString());
			role.setRole(rset.getObject("Role").toString());
			role.setPriority(Integer.parseInt(rset.getObject("Priority").toString()));
			role.setFontColor(rset.getObject("fontcolor").toString());
			role.setBackgroundColor(rset.getObject("background").toString());
			RolePriorityList.add(role);
		
		}
		roleList.put("rolePriority",RolePriorityList );
		return roleList;
		
	}
	
	private HashMap<String,List<AssistTextEntry>> getAssistText() throws Exception,SQLException
	{
		HashMap<String,List<String>> textRoles=(HashMap<String, List<String>>) handleDBRequest("getTextRoles", null,false);
		
		HashMap<String,List<AssistTextEntry>> textList= new HashMap<String,List<AssistTextEntry>>();
		List<AssistTextEntry> AssistTextList=new ArrayList<AssistTextEntry>();
		
		HashMap<String,String> className=new HashMap<String,String>();
		HashMap<String,String> attrName=new HashMap<String,String>();	
		initThirdStatement();
		UIListHandler objUIListHandler=new UIListHandler(this);
		
		ResultSet rset = stat2.executeQuery("SELECT * FROM AssistText" );
		while(rset.next())  
		{
			AssistTextEntry text=new AssistTextEntry();
			String textID=rset.getObject("TextID").toString();
			text.setTextID(textID);
			String assisttext=rset.getObject("AssistText").toString();
			text.setAssistText(StringEscapeUtils.escapeXml(assisttext));
			text.setAttrID(rset.getObject("AttrID").toString());
			text.setBackgroundColor(rset.getObject("background").toString());
			text.setClassID(rset.getObject("ClassID").toString());
			text.setDateCreated(rset.getObject("DateCreated").toString());
			text.setFontColor(rset.getObject("fontcolor").toString());
			text.setIsDifferentColor(rset.getBoolean("isDiffColor"));
			text.setWorkflowID(rset.getObject("workflowID").toString());
			text.setWorkflowStatusID(rset.getObject("workflowStatusID").toString());
			String classID=rset.getObject("ClassID").toString();
			String attrID=rset.getObject("AttrID").toString();
			
			if(!className.containsKey(classID) || !attrName.containsKey(attrID) )
			{
				List<String> names=objUIListHandler.getClassAttributeName(Integer.parseInt(rset.getObject("ClassID").toString()),Integer.parseInt(rset.getObject("AttrID").toString()));
				text.setClassName(names.get(0));
				text.setAtrrName(names.get(1));
			if(!className.containsKey(classID))
				{
					className.put(classID,names.get(0) );
				}
				if(!attrName.containsKey(attrID))
				{
					attrName.put(attrID, names.get(1));
				}
			}
			else
			{
				text.setClassName(className.get(classID));
				text.setAtrrName(attrName.get(attrID));
			}
			List<String> rolesList = new ArrayList<String>();
			if(textRoles.containsKey(textID))
			{
				for(int k=0;k<textRoles.get(textID).size();k++)
				{
					rolesList.add(textRoles.get(textID).get(k));
				}
			}
			text.setRolesList(rolesList);
			AssistTextList.add(text);
		}
		objUIListHandler.disconnectSession();
		textList.put("assistText", AssistTextList);
		return textList;
		
		
	}
	

	private Map<String,Integer> authenticate(HashMap<String,String> params) throws Exception,SQLException {
		log.debug("Entering authenticate.. ");

		int uid = -1;
		String userName=params.get("userName");
		String password=params.get("password");
		HashMap result=new HashMap<String,Integer>();
		prep = conn.prepareStatement("SELECT UserId " + "FROM User " + "WHERE UserName=? AND Pwd=?");
		prep.setString(1, userName);
		prep.setString(2, password);
		ResultSet rs = prep.executeQuery();
		if ((rs != null) && rs.next()) {
			uid = rs.getInt("UserId");
			log.debug("UserId: "+uid);
		}
		result.put("userID", uid);
		log.debug("Exiting authenticate.. ");
				
		return result;
	}

	private Map<String,Boolean> ChangePassword(HashMap<String,String> map) throws SQLException,Exception {
		log.debug("Entering ChangePassword.. ");

		boolean status = false;
		HashMap<String, Boolean> statusMap=new HashMap<String,Boolean>();
		String uid=map.get("uid");
		String cpwd=map.get("cpwd");
		String npwd=map.get("npwd");
	
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
	private void closeDB() throws Exception,SQLException
	{
		log.debug("Entering closeDB.. ");

		if(conn!=null)
		{
			conn.close();
			log.info("DB Connection closed..");
		}
		log.debug("Exiting closeDB.. ");

	}
	private void initFirstStatement() throws SQLException,Exception
	{
		stat=conn.createStatement();
	}
	
	private void initSecondStatement() throws SQLException,Exception
	{
		stat1=conn.createStatement();
	}
	private void initThirdStatement() throws SQLException,Exception
	{
		stat2=conn.createStatement();
	}
	private void releaseResources(boolean autoCommit) throws SQLException,Exception
	{
		if(stat!=null)
		{
			stat.close();
		}
		if(stat1!=null)
		{
			stat1.close();
		}
		if(prep!=null)
		{
			prep.close();
		}
		if (autoCommit&&conn!=null&&!conn.getAutoCommit()) 
		{
			conn.rollback();
			conn.setAutoCommit(true);
		}
	}
	public HashMap<?, ?> handleDBRequest(String funcName,HashMap <?,?> params,boolean autoCommit)
	{
		log.debug("Entering handleDBRequest.. ");

		HashMap<?, ?> ret=null;
		try 
		{
			initFirstStatement();
			if(autoCommit==true)
			{
				if(conn.getAutoCommit())
				{
					conn.setAutoCommit(false);
				}
			}
			Class<?> c = Class.forName("com.XACS.Assist.Handler.DBHandler");
			Method method=null;
			Object result=null;
			if(params!=null)
			{
				method=c.getDeclaredMethod(funcName, HashMap.class);
				Object[] parameters=new Object[1];
				parameters[0]=params;
				result = method.invoke(this,parameters);
				if(autoCommit==true)
				{
					if(!conn.getAutoCommit())
					{
						conn.setAutoCommit(true);
					}
				}
				
			}
			else
			{
				method=c.getDeclaredMethod(funcName, null);
				result = method.invoke(this,(Object[])null);
				if(autoCommit==true)
				{
					if(!conn.getAutoCommit())
					{
						conn.setAutoCommit(true);
					}
				}
			}
			
			if(result!=null)
			{
				ret=(HashMap<?, ?>) result;
			}
			
			
		} 
		catch (SQLException e) 
		{
			

			if(e.getMessage().toLowerCase().contains("lock"))
			{
				log.error(Constants.DB.MSG_DB_LOCKED, e);
			}
			if(e.getMessage().contains("SQL logic error"))
			{
				log.error(Constants.DB.MSG_DB_ERROR, e);
			}
			if(e.getMessage().toLowerCase().contains("busy"))
			{
				log.error(Constants.DB.MSG_DB_BUSY, e);
			}

			
		} 
		catch (Exception e) 
		{		
			log.error( "Exception: " , e);
		}
		finally
		{
			try
			{
				releaseResources(autoCommit);
			}
			 catch (SQLException e1) 
			{
				
				log.error( "SQLException: " , e1);
			} catch (Exception e) {
				
				log.error( "Exception: " , e);
			}
		}
		log.debug("Exiting handleDBRequest.. ");
		return ret;
	}
	
}
