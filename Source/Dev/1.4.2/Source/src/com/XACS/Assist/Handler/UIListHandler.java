package com.XACS.Assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.XACS.Assist.DO.AssistColorEntry;
import com.XACS.Assist.DO.AssistTextEntry;
import com.XACS.Assist.DO.AttributeEntry;
import com.XACS.Assist.DO.ClassEntry;
import com.XACS.Assist.DO.ConfigEntry;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Util.Constants;
import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IProperty;
import com.agile.api.IQuery;
import com.agile.api.IRole;
import com.agile.api.ITable;
import com.agile.api.ITableDesc;
import com.agile.api.IUserGroup;
import com.agile.api.PropertyConstants;
import com.agile.api.UserGroupConstants;

public class UIListHandler {
	public static void main(String[] args) 
	{
		IAgileSession session=null;
		
		Map<Object, String> data=new HashMap<Object,String>(2);
		
		data.put(AgileSessionFactory.USERNAME, "admin");
		data.put(AgileSessionFactory.PASSWORD, "agile");
		
		try 
		{
			AgileSessionFactory factory=AgileSessionFactory.getInstance("http://plmflexdev.xavor.com:7001/Agile");
			System.out.println("Creating Session...");
			session=factory.createSession(data);
			System.out.println("Session created.");
			IQuery q =
					(IQuery)session.createObject(IQuery.OBJECT_TYPE, "select * from [User Groups]");
					ArrayList groups = new ArrayList();
					Iterator itr =
					q.execute().getReferentIterator();
					while (itr.hasNext()) {
					groups.add(itr.next());
					}
					for (int i = 0; i < groups.size(); i++) {
					IUserGroup ug =
					(IUserGroup)groups.get(i);
					System.out.println(
					ug.getId() + ", " +
					ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_NAME) + ", " +
					ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_DESCRIPTION) + ", " +
					ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_MAX_NUM_OF_NAMED_USERS) + ", " +
					ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_STATUS)
					);
					}
			
		} catch (APIException e) 
		{
			e.printStackTrace();
		}finally
		{
			if(session!=null)
			{
				session.close();
			}
		}
	}
	public static List<ClassEntry> getClassList() throws Exception {
		ArrayList<ClassEntry> classarr = new ArrayList<ClassEntry>();
		DBHandler dbh = new DBHandler();
		ArrayList<Integer> rs = new ArrayList<Integer>();
		rs = dbh.checkClassesAssistText();
		IAgileSession session = null;
		try {
			session = AgileHandler.getAgileSession();
			IAgileClass[] baseclsarr = AgileHandler.getBaseAgileClasses(session);
			ClassEntry clsentry = null;
			for (IAgileClass basecls : baseclsarr) {
				clsentry = new ClassEntry();
				String baseClassId = basecls.getId().toString();
				clsentry.setIdVal(baseClassId);
				clsentry.setClassVal(baseClassId);
				clsentry.setName(basecls.getName());
				clsentry.setLevel("1");
				if (rs.contains(Integer.parseInt(baseClassId))) {
					clsentry.sethasTextFlag(Constants.General.TextFlagSet);
				} else {
					clsentry.sethasTextFlag(Constants.General.TextFlagNotSet);
				}
				/*
				 * ArrayList<Integer> rs1=new ArrayList<Integer>();
				 * rs1=dbh.checkAssistText(Integer.parseInt(baseClassId));
				 * if(!rs1.isEmpty()) {
				 * clsentry.sethasTextFlag(Constants.General.TextFlagSet); }
				 * else{
				 * clsentry.sethasTextFlag(Constants.General.TextFlagNotSet); }
				 */
				classarr.add(clsentry);
				IAgileClass[] subclsarr = basecls.getSubclasses();
				for (IAgileClass subcls : subclsarr) {
					clsentry = new ClassEntry();
					String subClassId = subcls.getId().toString();
					clsentry.setIdVal(subClassId);
					clsentry.setClassVal("child-of-" + baseClassId);
					clsentry.setName(subcls.getName());
					clsentry.setLevel("2");
					if (rs.contains(Integer.parseInt(subClassId))) {
						clsentry.sethasTextFlag(Constants.General.TextFlagSet);
					} else {
						clsentry.sethasTextFlag(Constants.General.TextFlagNotSet);
					}
					/*
					 * ArrayList<Integer> rs2=new ArrayList<Integer>();
					 * rs2=dbh.checkAssistText(Integer.parseInt(subClassId));
					 * if(!rs2.isEmpty()) {
					 * clsentry.sethasTextFlag(Constants.General.TextFlagSet); }
					 * else{
					 * clsentry.sethasTextFlag(Constants.General.TextFlagNotSet
					 * ); }
					 */
					classarr.add(clsentry);
					IAgileClass[] concclsarr = subcls.getSubclasses();
					for (IAgileClass conccls : concclsarr) {
						clsentry = new ClassEntry();
						clsentry.setIdVal(conccls.getId().toString());
						clsentry.setClassVal("child-of-" + subClassId);
						clsentry.setName(conccls.getName());
						clsentry.setLevel("3");
						if (rs.contains(Integer.parseInt(conccls.getId().toString()))) {
							clsentry.sethasTextFlag(Constants.General.TextFlagSet);
						} else {
							clsentry.sethasTextFlag(Constants.General.TextFlagNotSet);
						}
						/*
						 * ArrayList<Integer> rs3=new ArrayList<Integer>();
						 * rs3=dbh
						 * .checkAssistText(Integer.parseInt(conccls.getId
						 * ().toString())); if(!rs3.isEmpty()) {
						 * clsentry.sethasTextFlag
						 * (Constants.General.TextFlagSet); } else{
						 * clsentry.sethasTextFlag
						 * (Constants.General.TextFlagNotSet); }
						 */
						classarr.add(clsentry);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			AgileHandler.disconnect(session);
		}
		return classarr;
	}

	public static List<AttributeEntry> getAttributeList(int classId, int level) throws Exception {
		IAgileSession session = null;
		List<AttributeEntry> attributeEntryList = new ArrayList<AttributeEntry>();
		boolean gotClasses = false;
		try {
			session = AgileHandler.getAgileSession();
			IAgileClass cls = AgileHandler.getAgileClass(session, classId);
			ITableDesc[] tbdesc = cls.getTableDescriptors();
			for (ITableDesc tbdes : tbdesc) {
				if (((level == 3) && tbdes.getAPIName().equalsIgnoreCase("PageThree"))
						|| ((level == 2) && tbdes.getAPIName().equalsIgnoreCase("PageTwo"))
						|| ((level == 1) && (tbdes.getAPIName().equalsIgnoreCase("CoverPage") || tbdes.getAPIName().equalsIgnoreCase("TitleBlock") || tbdes
								.getAPIName().equalsIgnoreCase("GeneralInfo")))) {
					// attrArr=tbdes.getAttributes();
					attributeEntryList = parseToAttributeEntryList(tbdes.getAttributes(), classId);
					gotClasses = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			AgileHandler.disconnect(session);
		}
		if (gotClasses) {
			return attributeEntryList;
		} else {
			throw new Exception("Class does not have a " + ((level == 3) ? "Page Three" : ((level == 2) ? "Page Two" : "Title Block/General Info/Cover Page"))
					+ " defined.");
			
		}
	}

	private static List<AttributeEntry> parseToAttributeEntryList(IAttribute[] attrArr, int classId) throws Exception 
	{
		ArrayList<AttributeEntry> attrLst = new ArrayList<AttributeEntry>();
		
		AttributeEntry attrentry = null;
		AssistColorEntry colorEntry=null;
		
		DBHandler dbh = new DBHandler();
		
		ArrayList<Integer> rs = dbh.checkAssistText(classId);
		
		for (IAttribute attr : attrArr) 
		{
			if (attr.isVisible()) 
			{
				attrentry = new AttributeEntry();
				
				attrentry.setClassID(String.valueOf(classId));
				attrentry.setAttrID(attr.getId().toString());
				attrentry.setAttrName(attr.getFullName());
				
				if (rs.contains(attr.getId())) 
				{
					attrentry.sethasTextFlag(Constants.General.TextFlagSet);
				} else 
				{
					attrentry.sethasTextFlag(Constants.General.TextFlagNotSet);
				}
				
				try 
				{
					IProperty descProp = attr.getProperty(PropertyConstants.PROP_DESCRIPTION);
					if ((descProp != null) && (descProp.getValue() != null)) 
					{
						attrentry.setAttrDescription((descProp.getValue().toString().equals("") ? " " : descProp.getValue().toString()));
					} else 
					{
						attrentry.setAttrDescription("");
					}
				} catch (Exception e) 
				{
					attrentry.setAttrDescription("");
				}
				attrLst.add(attrentry);
			}
		}
		return attrLst;
	}

	public static List<AssistTextEntry> getAssistTextList(String classId, String attrId) throws Exception {
		try 
		{
			DBHandler dbh = new DBHandler();
			return dbh.getAssistTexts(classId, attrId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		// return null;
	}

	public static List<RoleEntry> getRoleList() throws Exception {
		ArrayList<RoleEntry> roleList = null;
		IAgileSession session = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, ArrayList<String>> assistRoles = dbh.getRolePriorities();
			session = AgileHandler.getAgileSession();
			
			if(Constants.Config.ACCESSTYPEROLE==null)
			{
				Constants.Config.ACCESSTYPEROLE=dbh.getConfigByKey("accessType");
			}
			
			
			roleList = new ArrayList<RoleEntry>();
			
			String allRoleKey="";
			
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
			{
				Collection agileRoles = AgileHandler.getAllRoles(session);
				populateInfoFromRole(roleList, assistRoles, agileRoles);
				allRoleKey="All Roles";
			}
			else
			{
				ITable tbl=AgileHandler.getAllUserGroups(session);
				populateInfoFromUserGroup(roleList, assistRoles, tbl);
				allRoleKey="All User Groups";
			}
			
			RoleEntry roleEntry = new RoleEntry();
			roleEntry.setRoleID("0");
			roleEntry.setRole(allRoleKey);
			
			if (assistRoles.containsKey(allRoleKey)) 
			{
				roleEntry.setPriority(Integer.parseInt(assistRoles.get(allRoleKey).get(0)));
				roleEntry.setFontColor(assistRoles.get(allRoleKey).get(1));
				roleEntry.setBackgroundColor(assistRoles.get(allRoleKey).get(2));
			} else 
			{
				roleEntry.setPriority(-1);
			}
			roleList.add(roleEntry);
			Collections.sort(roleList, new Comparator<RoleEntry>() {
				public int compare(RoleEntry r1, RoleEntry r2) {
					return (r1.getRole().compareToIgnoreCase(r2.getRole()));
				}
			});
			Collections.sort(roleList, new Comparator<RoleEntry>() {
				public int compare(RoleEntry r1, RoleEntry r2) {
					return (new Integer(r2.getPriority())).compareTo(new Integer(r1.getPriority()));
				}
			});
		} catch (Exception e) {
			throw e;
		} finally {
			AgileHandler.disconnect(session);
		}
		return roleList;
	}
	private static void populateInfoFromRole(List<RoleEntry> roleList,HashMap<String, ArrayList<String>> assistRoles,Collection agileRoles) throws APIException
	{
		for (Object roleObj : agileRoles) 
		{
			IRole role = (IRole) roleObj;
			String name = role.getName();
			String id = role.getId().toString();
			RoleEntry roleEntry = new RoleEntry();
			roleEntry.setRoleID(id);
			roleEntry.setRole(name);
			if (assistRoles.containsKey(name)) {
				roleEntry.setPriority(Integer.parseInt(assistRoles.get(name).get(0)));
				roleEntry.setFontColor(assistRoles.get(name).get(1));
				roleEntry.setBackgroundColor(assistRoles.get(name).get(2));
			} else {
				roleEntry.setPriority(-1);
			}
			roleList.add(roleEntry);
		}
	}
	private static void populateInfoFromUserGroup(List<RoleEntry> roleList,HashMap<String, ArrayList<String>> assistRoles,ITable agileUserGroups) throws APIException
	{
		
		Iterator itr=agileUserGroups.getReferentIterator();
		
		while (itr.hasNext())
		{
			IUserGroup userGroup=(IUserGroup)itr.next();
			
			String name = userGroup.getName();
			String id = userGroup.getObjectId().toString();
			RoleEntry roleEntry = new RoleEntry();
			roleEntry.setRoleID(id);
			roleEntry.setRole(name);
			if (assistRoles.containsKey(name)) 
			{
				roleEntry.setPriority(Integer.parseInt(assistRoles.get(name).get(0)));
				roleEntry.setFontColor(assistRoles.get(name).get(1));
				roleEntry.setBackgroundColor(assistRoles.get(name).get(2));
			} else 
			{
				roleEntry.setPriority(-1);
			}
			roleList.add(roleEntry);
		}
		
		
	}
	public static HashMap<String, ConfigEntry> getConfigEntries() throws Exception {
		HashMap<String, ConfigEntry> configArr = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, String> configMap = dbh.readConfigurations();
			ConfigEntry ce = null;
			configArr = new HashMap<String, ConfigEntry>();
			
			for (String key : configMap.keySet()) 
			{
				if (!key.equalsIgnoreCase("LNFO")) 
				{
					ce = new ConfigEntry();
					ce.setKey(key);
					ce.setValue(configMap.get(key));
					ce.setId(key.replaceAll(" ", ""));
					if (key.equalsIgnoreCase("AgilePassword")) 
					{
						ce.setType("password");
					} else 
					{
						ce.setType("text");
					}
					configArr.put(ce.getKey(), ce);
				}
			}
			if(!configMap.containsKey("accessType"))
			{
				// keeping role access type as default, user however can update it
				
				dbh.insertConfig("accessType", "roles");
			}
			else
			{
				Constants.Config.ACCESSTYPEROLE=configMap.get("accessType");
			}
			
			return configArr;
		} catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
		// return configArr;
	}
}
