package com.XACS.Assist.Handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.XACS.Assist.DO.AssistColorEntry;
import com.XACS.Assist.DO.AssistTextEntry;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;

public class DBHandler {
	Connection			conn	= null;
	Statement			stat	= null;
	PreparedStatement	prep	= null;

	public DBHandler() throws Exception {
		init();
	}

	@Override
	protected void finalize() throws Exception {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) 
			{
				e.printStackTrace();
				throw e;
			}
		}
	}

	private void init() throws Exception 
	{
		try 
		{
			String homePath = ConfigHelper.getAppHomePath();
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + homePath + Constants.DB.DB_NAME);// "xacs.assist.db"
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
			throw new Exception("SQLite JDBC driver not installed");
		} catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
	}

	public HashMap<String, String> readConfigurations() throws Exception 
	{
		HashMap<String, String> configMap = null;
		try 
		{
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT ConfigKey, ConfigVal " + "FROM Configurations WHERE ConfigKey <> 'LNFO' ORDER BY rowid DESC");
			configMap = new HashMap<String, String>();
			while ((rs != null) && rs.next()) 
			{
				configMap.put(rs.getString("ConfigKey"), rs.getString("ConfigVal"));
			}
			stat.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
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
	 ***/
	
	public HashMap<String, ArrayList<String>> getAssistInfoMap(ArrayList<String> classes, ArrayList<String> roles,String allRoleKey) throws Exception 
	{
		HashMap<String, ArrayList<String>> asInfo = null;
		
		String rolestr = roles.toString().replace("'", "''").replace('[', ' ').replace(']', ' ').replace(", ", "\',\'").trim();
		String clsstr = classes.toString().replace('[', ' ').replace(']', ' ').trim();
		
		String quer = "SELECT AttrP.AttrID, AssistText.AssistText,"
				+ " case when AssistText.isDiffColor then AssistText.fontcolor else RolePriority.fontcolor end as fontcolor,"
				+ " case when AssistText.isDiffColor then AssistText.background else RolePriority.background end as background "
				+ "FROM ( SELECT AttrID, MAX(Priority) Priority " + "FROM RoleTextMap " + "INNER JOIN RolePriority "
				+ "ON RoleTextMap.RoleID = RolePriority.RoleID " + "INNER JOIN AssistText " + "ON RoleTextMap.TextID = AssistText.TextID " + "WHERE Role IN ('"
				+ rolestr + "','"+allRoleKey+"') " + "AND ClassID IN (" + clsstr + ") " + "GROUP BY AttrID " + "ORDER BY RoleTextMap.TextID) AttrP "
				+ "INNER JOIN AssistText " + "ON AttrP.AttrID = AssistText.AttrID " + "INNER JOIN RolePriority " + "ON AttrP.Priority = RolePriority.Priority "
				+ "INNER JOIN RoleTextMap " + "ON RoleTextMap.RoleID = RolePriority.RoleID " + "AND RoleTextMap.TextID = AssistText.TextID ";
		
		stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(quer);
		asInfo = new HashMap<String, ArrayList<String>>();
		
		while ((rs != null) && rs.next()) 
		{
			String attrId = rs.getString("AttrID");
			String assistText = rs.getString("AssistText");
			String fontcolor = rs.getString("fontcolor");
			String background = rs.getString("background");
			
			ArrayList<String> roleAttributes = new ArrayList<String>();
			
			roleAttributes.add(assistText);
			roleAttributes.add(fontcolor);
			roleAttributes.add(background);
			
			asInfo.put(attrId, roleAttributes);
		}
		stat.close();
		return asInfo;
		/*
		 * } catch (Exception e) { e.printStackTrace(); }
		 */
	}

	public List<AssistTextEntry> getAssistTexts(String classId, String attrId) throws Exception {
		ArrayList<AssistTextEntry> ateArr = null;
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT TextID, AssistText, isDiffColor, fontcolor, background " + "FROM AssistText " + "WHERE ClassID=" + classId
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
				Statement stat1 = conn.createStatement();
				ResultSet rs1 = stat1.executeQuery("SELECT RP.RoleID value, Role label,  CASE WHEN (ifnull(TM.RoleID,-1) == -1) THEN 0 ELSE 1 END AS selected "
						+ "FROM RolePriority RP " + "LEFT OUTER JOIN (SELECT RoleID from RoleTextMap WHERE TextID=" + ate.getTextID() + ") TM "
						+ "ON RP.RoleID = TM.RoleID ");
				ArrayList<String> strRoleArr = new ArrayList<String>();
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
				stat1.close();
			}
			stat.close();
		} catch (SQLException e) 
		{
			e.printStackTrace();
			throw e;
		}
		return ateArr;
	}

	public String[] getRoleOptions() throws Exception 
	{
		String[] strRoles = null;
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT RoleID value, Role label " + "FROM RolePriority RP;");
			ArrayList<String> strRoleArr = new ArrayList<String>();
			while ((rs != null) && rs.next()) {
				String str = "<option value=\"" + rs.getString("value") + "\"" + ">" + rs.getString("label") + "</option>";
				strRoleArr.add(str);
			}
			strRoles = new String[strRoleArr.size()];
			strRoles = strRoleArr.toArray(strRoles);
			stat.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return strRoles;
	}

	public HashMap<String, ArrayList<String>> getRolePriorities() throws SQLException {
		HashMap<String, ArrayList<String>> roleMap = null;
		try 
		{
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT Role,Priority,fontcolor,background FROM RolePriority RP ORDER BY Priority ASC ");
			roleMap = new HashMap<String, ArrayList<String>>();
			while ((rs != null) && rs.next()) {
				ArrayList<String> roleAttributes = new ArrayList<String>();
				roleAttributes.add(rs.getString("Priority"));
				roleAttributes.add(rs.getString("fontcolor"));
				roleAttributes.add(rs.getString("background"));
				roleMap.put(rs.getString("Role"), roleAttributes);
			}
			stat.close();
			return roleMap;
		} catch (SQLException e) 
		{
			e.printStackTrace();
			throw e;
		}
	}

	public ArrayList<Integer> checkClassesAssistText() {
		ArrayList<Integer> i = new ArrayList<Integer>();
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT DISTINCT ClassID FROM AssistText WHERE ClassID NOT NULL");
			while ((rs != null) && rs.next()) {
				i.add(rs.getInt("ClassID"));
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}

	public ArrayList<Integer> checkDuplicateRoles(int classid, int attid) {
		ArrayList<Integer> i = new ArrayList<Integer>();
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT RoleID FROM AssistText,RoleTextMap  where AssistText.TextID=RoleTextMap.TextID AND AssistText.ClassID='"
					+ classid + "' AND AssistText.AttrID='" + attid + "'");
			// ResultSet rs =
			// stat.executeQuery("SELECT RoleTextMap.TextID FROM AssistText,RoleTextMap where AssistText.TextID=RoleTextMap.TextID AND AssistText.ClassID='"+classid+"' AND AssistText.AttrID='"+attid+"' GROUP BY RoleID HAVING count(*) > 1");
			while ((rs != null) && rs.next()) // SELECT TextID FROM RoleTextMap
			// GROUP BY RoleID HAVING
			// count(*) > 1
			{
				i.add(rs.getInt("RoleID"));
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	public Map<String, Object> getAssistColors(String classId,boolean mClasses)
	{
		Map<String, Object> attColors=new HashMap<String, Object>(0);
		if(conn!=null)
		{
			String selectQry="";
			
			if(mClasses)
			{
				String clsStr = classId.replace('[', ' ').replace(']', ' ').trim();
				selectQry="SELECT ColorID, AttrID, AssistColor FROM AssistColor WHERE ClassID IN ("+clsStr+")";
			}
			else
			{
				selectQry="SELECT ColorID, AttrID, AssistColor FROM AssistColor WHERE ClassID="+classId;
			}
			
			
			Statement stmt=null;
			ResultSet rs=null;
			AssistColorEntry colorEntry=null;
			
			try 
			{
				stmt=conn.createStatement();
				rs=stmt.executeQuery(selectQry);
				
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
				
			} catch (SQLException e) 
			{
				e.printStackTrace();
			}finally
			{
				if(stmt!=null)
				{
					try 
					{
						stmt.close();
					} catch (SQLException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
		return attColors;
	}
	public ArrayList<Integer> checkAssistText(int ClassID) {
		ArrayList<Integer> i = new ArrayList<Integer>();
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT DISTINCT AttrID FROM AssistText WHERE ClassID='" + ClassID + "'");
			while ((rs != null) && rs.next()) {
				i.add(rs.getInt("AttrID"));
			}
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	public int addNewAssistColor(String classID, Map attColors) throws Exception 
	{
		int res=-1;
		
		if(conn!=null && attColors!=null && attColors.size()>0)
		{
			PreparedStatement stmt=conn.prepareStatement("INSERT INTO AssistColor(ClassID,AttrID,AssistColor) Values(?,?,?)");
			
			Set attributeSet=attColors.keySet();
			Iterator attIt=attributeSet.iterator();
			String attId="";
			String attColor="";
			
			while(attIt.hasNext())
			{
				attId=attIt.next()+"";
				attColor=attColors.get(attId)+"";
				
				stmt.setString(1, classID);
				stmt.setString(2, attId);
				stmt.setString(3, attColor);
				
				res=stmt.executeUpdate();
				
				System.out.println("Attribute ["+attId+"], color ["+attColor+"], set=["+res+"]");
			}
			
		}
		
		return res;
	}
	public int addNewAssistColor(String classID, JSONArray jsonAttColors) throws Exception 
	{
		int res=-1;
		
		if(conn!=null && jsonAttColors!=null && jsonAttColors.size()>0)
		{
			JSONObject attColor=null;
			
			String colorId="";
			String attId="";
			String assistColor="";
			
			String insertQry="INSERT INTO AssistColor (ClassID,AttrID,AssistColor) VALUES (?,?,?)";
			String updateQry="UPDATE AssistColor SET ClassID=?,AttrID=?,AssistColor=? where ColorID=?";
			
			PreparedStatement insertStmt=null;
			PreparedStatement updateStmt=null;
			
			try 
			{
				for(int attColorIndex=0;attColorIndex<jsonAttColors.size();attColorIndex++)
				{
					attColor=(JSONObject)jsonAttColors.get(attColorIndex);
					
					colorId=attColor.get("colorId")+"";
					attId=attColor.get("attId")+"";
					assistColor=attColor.get("assistColor")+"";
					
					System.out.println("Insert/Update ::: Color Id=["+colorId+"], Attribute Id=["+attId+"], Assist Color=["+assistColor+"]");
					
					
					if(colorId!=null && !colorId.equals(""))
					{
						if(updateStmt==null)
						{
							updateStmt=conn.prepareStatement(updateQry);
						}
						
						updateStmt.setString(1, classID);
						updateStmt.setString(2, attId);
						updateStmt.setString(3, assistColor);
						updateStmt.setString(4, colorId);
						
						res=updateStmt.executeUpdate();
						
					}
					else
					{
						if(insertStmt==null)
						{
							insertStmt=conn.prepareStatement(insertQry);
						}
						
						insertStmt.setString(1, classID);
						insertStmt.setString(2, attId);
						insertStmt.setString(3, assistColor);
						
						res=insertStmt.executeUpdate();
					}
					System.out.println("Insert/Update result=["+res+"]");
				}
			} catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				if(insertStmt!=null)
				{
					insertStmt.close();
				}
				if(updateStmt!=null)
				{
					updateStmt.close();
				}
			}
		}
		
		return res;
	}
	public int addNewAssistText(String classID, String attrID) throws Exception 
	{
		int textId = -1;
		try 
		{
			prep = conn.prepareStatement("INSERT INTO AssistText (ClassID, AttrID, AssistText) Values (?,?,?);");
			prep.setString(1, classID);
			prep.setString(2, attrID);
			prep.setString(3, "");
			// prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeUpdate();
			conn.setAutoCommit(true);
			prep.close();
			// stat.executeUpdate("INSERT INTO AssistText (ClassID, AttrID, AssistText) Values ("+classID+", "+attrID+", '')");
			// System.out.println(i);
			stat = conn.createStatement();
			// conn.setAutoCommit(false);
			ResultSet rs = stat.executeQuery("SELECT TextID FROM AssistText WHERE ClassID=" + classID + " AND AttrID=" + attrID
					+ " AND AssistText='' ORDER BY TextID DESC");
			// conn.setAutoCommit(true);
			if ((rs != null) && rs.next()) {
				textId = rs.getInt("TextID");
			}
			// conn.setAutoCommit(true);
			stat.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
		return textId;
	}

	public void removeAssistText(String textID) throws Exception {
		try {
			stat = conn.createStatement();
			stat.addBatch("DELETE FROM AssistText WHERE TextID=" + textID + ";");
			stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
			conn.setAutoCommit(false);
			stat.executeBatch();
			conn.setAutoCommit(true);
			stat.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
	}

	public void updateAssistText(String textID, String assistText, String fontColor, String backgroundColor, boolean isDiffColor) throws Exception {
		try {
			prep = conn.prepareStatement("Update AssistText SET AssistText=?, fontcolor=?, background=?, isDiffColor=?   WHERE TextID=?;");
			prep.setString(1, assistText);
			prep.setString(2, fontColor);
			prep.setString(3, backgroundColor);
			prep.setBoolean(4, isDiffColor);
			prep.setString(5, textID);
			conn.setAutoCommit(false);
			// int i =
			// assistText = assistText.replace("'", "''");
			prep.executeUpdate();
			conn.setAutoCommit(true);
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
	}

	public void updateAllAssistText(ArrayList<AssistTextEntry> rows) throws Exception {
		try {
			stat = conn.createStatement();
			for (int t = 0; t < rows.size(); t++) {
				String text = rows.get(t).getAssistText();
				String textid = rows.get(t).getTextID();
				String fontcolor = rows.get(t).getFontColor();
				String background = rows.get(t).getBackgroundColor();
				boolean isDiffColor = rows.get(t).getIsDifferentColor();
				textid = textid.replace("row", "");
				String[] roles = rows.get(t).getRoles();
				// +" "+rows.get(t).getAttrID();
				// conn.setAutoCommit(false);
				// System.out.println("Select Query Execute");
				ResultSet rs = stat.executeQuery("SELECT TextID FROM AssistText WHERE TextID=" + textid);
				// conn.setAutoCommit(true);
				stat.close();
				// conn.setAutoCommit(true);
				if (rs != null) {
					// System.out.println("Update Query Execute");
					// update assist text
					/*
					 * prep =conn.prepareStatement(
					 * "Update AssistText SET AssistText=? WHERE TextID=?;");
					 * conn.setAutoCommit(false); prep.setString(1, text);
					 * prep.setString(2, textid); prep.executeUpdate();
					 * prep.close();
					 */
					// **updateTextRoleList(textid, roles);
					prep = conn.prepareStatement("Update AssistText SET AssistText=?, isDiffColor=?,fontcolor=?,background=? WHERE TextID=?;");
					prep.setString(1, text);
					prep.setBoolean(2, isDiffColor);
					prep.setString(3, fontcolor);
					prep.setString(4, background);
					prep.setString(5, textid);
					conn.setAutoCommit(false);
					prep.addBatch();
					// prep.executeUpdate();
					stat = conn.createStatement();
					stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textid + ";");
					if (roles != null) {
						String rolestr = "";
						for (String str : roles) {
							rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
						}
						stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textid + ", RoleID " + "FROM RolePriority "
								+ "WHERE RoleID IN (" + rolestr + ");");
					}
					prep.executeBatch();
					stat.executeBatch();
					conn.setAutoCommit(true);
					prep.close();
					stat.close();
					// System.out.println("Update Query ends");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
	}

	public void updateTextRoleList(String textID, String[] roleList) throws Exception {
		try {
			stat = conn.createStatement();
			stat.addBatch("DELETE FROM RoleTextMap WHERE TextID=" + textID + ";");
			if (roleList != null) {
				String rolestr = "";
				for (String str : roleList) {
					rolestr = rolestr + (rolestr.equals("") ? "" + str + "" : "," + str + "");
				}
				stat.addBatch("INSERT INTO RoleTextMap (TextID, RoleID) " + "SELECT " + textID + ", RoleID " + "FROM RolePriority " + "WHERE RoleID IN ("
						+ rolestr + ");");
			}
			conn.setAutoCommit(false);
			stat.executeBatch();
			conn.setAutoCommit(true);
			stat.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
	}

	public void updateRolePriority(ArrayList<RoleEntry> roleList) throws Exception {
		try 
		{
			stat = conn.createStatement();
			String roleIds = "";
			for (RoleEntry role : roleList) 
			{
				roleIds = roleIds + (roleIds.equals("") ? "" : ",") + role.getRoleID();
			}
			// conn.setAutoCommit(false);
			ResultSet rs = stat.executeQuery("SELECT RoleID " + "FROM RolePriority " + "WHERE RoleID NOT IN (" + roleIds + ");");
			String delRoleIds = "";
			while ((rs != null) && rs.next()) 
			{
				delRoleIds = delRoleIds + (delRoleIds.equals("") ? "" : ",") + rs.getString("RoleID");
			}
			stat.close();
			stat = conn.createStatement();
			if (!delRoleIds.equals("")) 
			{
				stat.addBatch("DELETE FROM RoleTextMap " + "WHERE RoleID IN(" + delRoleIds + ");");
				/*
				 * stat.executeUpdate("DELETE FROM RoleTextMap " +
				 * "WHERE RoleID IN(" + delRoleIds+")");
				 */
				stat.addBatch("DELETE FROM AssistText " + "WHERE TextID NOT IN ( " + "SELECT TextID " + "FROM RoleTextMap " +
				/*
				 * "WHERE RoleID IN (" + delRoleIds+")" +
				 */
				");");
			}
			/*
			 * conn.setAutoCommit(true); / Delete all and reload
			 * conn.setAutoCommit(false);
			 */
			stat.addBatch("DELETE FROM RolePriority;");
			for (RoleEntry role : roleList) 
			{
				stat.addBatch("INSERT INTO RolePriority (RoleID, Role, Priority,fontcolor,background) " + "VALUES (" + role.getRoleID() + ",'"
						+ role.getRole().replace("'", "''") + "'," + role.getPriority() + ",'" + role.getFontColor() + "','" + role.getBackgroundColor()
						+ "' );");
			}
			conn.setAutoCommit(false);
			stat.executeBatch();
			conn.setAutoCommit(true);
			stat.close();
		} catch (Exception e) 
		{
			e.printStackTrace();
			if (!conn.getAutoCommit()) 
			{
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
	}
	public void insertConfig(String key,String value)
	{
		Statement stmt=null;
		
		if(conn!=null)
		{
			try 
			{
				// for single SQL operation, preferring Statement over Prepared Statement
				stmt=conn.createStatement();
				String insertConfigQry="INSERT INTO Configurations (ConfigKey,ConfigVal) VALUES ('"+key+"','"+value+"')";
				stmt.executeUpdate(insertConfigQry);
				
			} catch (SQLException e) 
			{
				e.printStackTrace();
			}catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
	public String getConfigByKey(String key)
	{
		Statement stmt=null;
		String value="";
		if(conn!=null)
		{
			try 
			{
				// for single SQL operation, preferring Statement over Prepared Statement
				stmt=conn.createStatement();
				String selectConfigQry="SELECT ConfigVal FROM Configurations WHERE ConfigKey='"+key+"'";
				ResultSet rs=stmt.executeQuery(selectConfigQry);
				if(rs!=null && rs.next())
				{
					value=rs.getString("ConfigVal");
				}
				
			} catch (SQLException e) 
			{
				e.printStackTrace();
			}catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		return value;
	}
	public void updateConfigurations(String[] configs) throws Exception {
		PreparedStatement prep = null;
		try {
			prep = conn.prepareStatement("UPDATE Configurations SET ConfigVal=? WHERE ConfigKey=?");
			for (String config : configs) {
				String[] params = config.split("=", 2);
				prep.setString(1, params[1]);
				prep.setString(2, params[0]);
				prep.addBatch();
				if("accessType".equalsIgnoreCase(params[0]))
				{
					Constants.Config.ACCESSTYPEROLE=params[1];
					
					if("roles".equalsIgnoreCase(params[1]))
					{
						updateDefaultRolePriority("All Roles");
					}
					else
					{
						updateDefaultRolePriority("All User Groups");
					}
				}
			}
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			prep.close();
			throw e;
		}
	}
	private void updateDefaultRolePriority(String newRole) throws SQLException
	{
		Statement stmt=null;
		
		if(conn!=null)
		{
			String query="UPDATE RolePriority set Role = '"+newRole+"' WHERE RoleID =0";
			stmt=conn.createStatement();
			int res=stmt.executeUpdate(query);
			
			System.out.println("Role Priority updated=["+res+"]");
			if(stmt!=null)
			{
				stmt.close();
			}
		}
		
		
		
	}
	public void updateLicinfo(String licinfo) throws Exception {
		PreparedStatement prep = null;
		try {
			stat = conn.createStatement();
			stat.addBatch("DELETE FROM Configurations WHERE ConfigKey='LNFO';");
			if (licinfo != null) {
				stat.addBatch("INSERT INTO Configurations (ConfigKey, ConfigVal) " + "VALUES('LNFO','" + licinfo + "')");
			}
			conn.setAutoCommit(false);
			stat.executeBatch();
			conn.setAutoCommit(true);
			// stat.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			// stat.close();
			throw e;
		} finally {
			stat.close();
		}
	}

	public String getLicInfo() throws Exception {
		String strLic = null;
		try {
			stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT ConfigVal FROM Configurations WHERE ConfigKey = 'LNFO'");
			// ArrayList<String> strRoleArr = new ArrayList<String>();
			while ((rs != null) && rs.next()) {
				strLic = rs.getString("ConfigVal");
			}
			// stat.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			stat.close();
		}
		return strLic;
	}

	public int authenticate(String un, String pwd) throws SQLException {
		int uid = -1;
		try {
			prep = conn.prepareStatement("SELECT UserId " + "FROM User " + "WHERE UserName=? AND Pwd=?");
			prep.setString(1, un);
			prep.setString(2, pwd);
			// prep.addBatch();
			// conn.setAutoCommit(false);
			ResultSet rs = prep.executeQuery();
			/*
			 * "SELECT UserId "+ "FROM User "+
			 * "WHERE UserName='"+un+"' AND Pwd='"+pwd+"'");
			 */
			if ((rs != null) && rs.next()) {
				uid = rs.getInt("UserId");
			}
			// conn.setAutoCommit(true);
			prep.close();
			/*
			 * stat = conn.createStatement(); un = un.replace("'", "''"); pwd =
			 * pwd.replace("'", "''"); ResultSet rs = prep.executeQuery();
			 * /"SELECT UserId "+ "FROM User "+
			 * "WHERE UserName='"+un+"' AND Pwd='"+pwd+"'"); if (rs != null &&
			 * rs.next()) uid = rs.getInt("UserId"); prep.close();
			 */
		} catch (SQLException e) 
		{
			e.printStackTrace();
			throw e;
		}
		return uid;
	}

	public boolean ChangePassword(String uid, String cpwd, String npwd) throws SQLException {
		boolean status = false;
		try {
			prep = conn.prepareStatement("UPDATE User " + "SET Pwd=? " + "WHERE UserId=? AND Pwd=?");
			prep.setString(1, npwd);
			prep.setString(2, uid);
			prep.setString(3, cpwd);
			// prep.addBatch();
			conn.setAutoCommit(false);
			int cnt = prep.executeUpdate();
			conn.setAutoCommit(true);
			/*
			 * stat = conn.createStatement(); uid = uid.replace("'", "''"); cpwd
			 * = cpwd.replace("'", "''"); npwd = npwd.replace("'", "''");
			 * stat.executeUpdate( "UPDATE User " + "SET Pwd = '"+npwd+"' " +
			 * "WHERE UserId='"+uid+"' AND Pwd='"+cpwd+"'");
			 */
			if (cnt == 1) {
				status = true;
			}
			prep.close();
			// stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
			if (!conn.getAutoCommit()) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw e;
		}
		return status;
	}
	public void closeDB()
	{
		if(conn!=null)
		{
			try 
			{
				conn.close();
			} catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
