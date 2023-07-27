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
import com.XACS.Assist.DO.BasicModel;
import com.XACS.Assist.DO.CListModel;
import com.XACS.Assist.DO.ClassEntry;
import com.XACS.Assist.DO.ConfigEntry;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.Constants;
import com.XACS.Assist.Util.ListComparator;
import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.INode;
import com.agile.api.IProperty;
import com.agile.api.IQuery;
import com.agile.api.IRole;
import com.agile.api.IRoutableDesc;
import com.agile.api.ITable;
import com.agile.api.ITableDesc;
import com.agile.api.IUserGroup;
import com.agile.api.IWorkflow;
import com.agile.api.PropertyConstants;
import com.agile.api.UserGroupConstants;

public class UIListHandler {
	
	static AssistLogger log=AssistLogger.getInstance();
	IAgileSession session = null;
	IAdmin adminInstance = null;
	public UIListHandler(DBHandler db) throws Exception
	{
		session = AgileHandler.getAgileSession(db);
		adminInstance =AgileHandler.getAdminInstance(session);
	}
	public UIListHandler() throws Exception
	{
		session = AgileHandler.getAgileSession();
		adminInstance =AgileHandler.getAdminInstance(session);
	}
	public static void main(String[] args) 
	{
		IAgileSession session=null;
		
		
		Map<Object, String> data=new HashMap<Object,String>(2);
		
		data.put(AgileSessionFactory.USERNAME, "admin");
		data.put(AgileSessionFactory.PASSWORD, "agile");
		
		try 
		{
			AgileSessionFactory factory=AgileSessionFactory.getInstance("http://plmflexdev.xavor.com:7001/Agile");
		
			log.info("Creating Session...");
			
			session=factory.createSession(data);
			log.info("Session created.");
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
					
					log.debug(" GetUserGroup : "+ug.getId() + ", " +
							ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_NAME) + ", " +
							ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_DESCRIPTION) + ", " +
							ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_MAX_NUM_OF_NAMED_USERS) + ", " +
							ug.getValue(UserGroupConstants.ATT_GENERAL_INFO_STATUS));
					}
			
		} catch (APIException e) 
		{
			
			log.error("APIException in UIListHandler Main : ", e);
		
		}finally
		{
			if(session!=null)
			{
				session.close();
			}
		}
	}
	public static List<ClassEntry> getClassList() throws Exception {
		log.debug("Entering getClassList...");

		ArrayList<ClassEntry> classarr = new ArrayList<ClassEntry>();
		DBHandler dbh = new DBHandler();
		ArrayList<Integer> rs = new ArrayList<Integer>();
		HashMap result = dbh.handleDBRequest("checkClassesAssistText", null, false);//.checkClassesAssistText();
		rs=(ArrayList<Integer>) result.get("result");
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
						
						classarr.add(clsentry);
					}
				}
			}
		} catch (Exception e) {
			
			log.error("Exception in getClassList : ", e);
			
			throw e;
		} finally {
			AgileHandler.disconnect(session);
		}
		log.debug("Exiting getClassList...");

		return classarr;
	}
	public static void createSession()
	{
		
	}
	public  List<String> getClassAttributeName(int classId, int attId) throws Exception {
		log.debug("Entering getClassAttributeName...");

		
		List<String> NamesList = new ArrayList<String>();
		try {
			
			
			IAgileClass cls = AgileHandler.getAgileClass(adminInstance, classId);
			NamesList.add(cls.getName());
			IAttribute att=cls.getAttribute(attId);
			NamesList.add(att.getName());
			
		} catch (Exception e) {
			
			log.error("Exception in getClassAttributeName : ", e);
			throw e;
		} 
		
		log.debug("Exiting getClassAttributeName...");
		return NamesList;
	}
	public void disconnectSession()
	{

			AgileHandler.disconnect(session);
	}
	
	public static List<AttributeEntry> getAttributeList(int classId, int level) throws Exception {
		log.debug("Entering getAttributeList...");

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
				
					attributeEntryList = parseToAttributeEntryList(tbdes.getAttributes(), classId);
					gotClasses = true;
				}
			}
		} catch (Exception e) {
			
			log.error("Exception in getAttributeList : ", e);
			
			throw e;
		} finally {
			AgileHandler.disconnect(session);
		}
		if (gotClasses) {
			log.debug("Exiting getAttributeList...");

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
		HashMap params=new HashMap<String,String>();
		params.put("classID", classId);
		HashMap<String,ArrayList<Integer>> map = (HashMap<String, ArrayList<Integer>>) dbh.handleDBRequest("checkAssistText", params, false);//.checkAssistText(classId);
		ArrayList<Integer> rs=map.get("result");
		HashMap colorsParams=new HashMap<String,Object>();
		colorsParams.put("classId", Integer.toString(classId));
		colorsParams.put("mClasses",false);
		Map attColors=dbh.handleDBRequest("getAssistColors", colorsParams, false);//.getAssistColors(classes.toString(),true);
		
		for (IAttribute attr : attrArr) 
		{
			
			attrentry = new AttributeEntry();

			attrentry.setClassID(String.valueOf(classId));
			attrentry.setAttrID(attr.getId().toString());
			attrentry.setAttrName(attr.getFullName());
			attrentry.setIsVisible((attr.isVisible()?"yes":"no"));

			if (rs.contains(attr.getId())) 
			{
				attrentry.sethasTextFlag(Constants.General.TextFlagSet);
			} else 
			{
				attrentry.sethasTextFlag(Constants.General.TextFlagNotSet);
			}

			if(attColors.containsKey(attrentry.getAttrID()))
			{
				colorEntry=(AssistColorEntry)attColors.get(attrentry.getAttrID());

				attrentry.setAssistColorId(colorEntry.getColorId());
				attrentry.setAssistColor(colorEntry.getAssistColor());

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
				log.error("Exception in parseToAttributeEntryList : ", e);

			}
			attrLst.add(attrentry);
			
		}
		return attrLst;
	}
	
	public static List<CListModel> getClassWorkflows(String classId) throws APIException
	{
		log.debug("Entering getClassWorkflows...");

		List<CListModel> workflows=new ArrayList<CListModel>(0);
		IAgileSession session=null;
		try
		{
			session = AgileHandler.getAgileSession();
		}
		catch(Exception ex)
		{
			
			log.error("Exception in getClassWorkflows : ", ex);
			
		}
		
		if(session!=null)
		{
			IAdmin admin=session.getAdminInstance();
			int classid=Integer.parseInt(classId);
			IWorkflow[] wfs=null;
			try
			{
				wfs =((IRoutableDesc)admin.getAgileClass(classid)).getWorkflows();
			}
			catch(Exception e)
			{
				log.error("Exception in getClassWorkflows : ", e);
				
				return null;
			}
			
			if(wfs!=null)
			{
				for (IWorkflow iWorkflow : wfs) 
				{
					workflows.add(new CListModel(iWorkflow.getName(),iWorkflow.getName(),getWorkflowStatuses((INode[])iWorkflow.getChildren())));
				}
			}
			
			Collections.sort(workflows,new ListComparator());
		}
		
		log.debug("Workflow: "+workflows.toString());
		log.debug("Exiting getClassWorkflows...");

		return workflows;
	}
	
	public static List<CListModel> getWorkflowStatuses(INode[] nodes) throws APIException
	{
		log.debug("Entering getWorkflowStatuses...");

		List<CListModel> statuses=new ArrayList<CListModel>(0);
		
		for (INode iNode : nodes) 
		{
			if(iNode.getAPIName().equalsIgnoreCase("StatusList"))
			{
				INode sNodes[]=(INode[])iNode.getChildren();
				
				for (INode sNode: sNodes) 
				{
					statuses.add(new CListModel(sNode.getName(), sNode.getName()));
				}
			}
		}
		log.debug("WorkflowStatuses: "+statuses.toString());
		log.debug("Exiting getWorkflowStatuses...");

		return statuses; 
	}
	public static List<BasicModel> getWorkflowStatuses(String wfName) throws APIException
	{
		log.debug("Entering getWorkflowStatuses...");

		List<BasicModel> statuses=new ArrayList<BasicModel>(0);
		IAgileSession session=null;
		try
		{
			session = AgileHandler.getAgileSession();
		}
		catch(Exception ex)
		{
			
			log.error("Exception in getWorkflowStatuses : ", ex);
			
		}
		if(session!=null)
		{
			IAdmin admin=session.getAdminInstance();
			
			INode node=admin.getNode(wfName);
			
			INode nodes[]=(INode[])node.getChildren();
			
			for (INode iNode : nodes) 
			{
				if(iNode.getAPIName().equalsIgnoreCase("StatusList"))
				{
					INode sNodes[]=(INode[])iNode.getChildren();
					
					for (INode sNode: sNodes) 
					{
						statuses.add(new BasicModel(sNode.getAPIName(), sNode.getName()));
					}
				}
			}
		}
		log.debug("WorkflowStatuses: "+statuses.toString());
		log.debug("Exiting getWorkflowStatuses...");

		return statuses; 
	}



	public static List<AssistTextEntry> getAssistTextList(String classId, String attrId) throws Exception {
		try 
		{
			log.debug("Entering getAssistTextList...");

			DBHandler dbh = new DBHandler();
			HashMap params=new HashMap<String,String>();
			params.put("classID", classId);
			params.put("attrId", attrId);
			HashMap textMap=new HashMap<String,ArrayList<AssistTextEntry>>();
			textMap=dbh.handleDBRequest("getAssistTexts", params,false);
			log.debug("Exiting getAssistTextList...");

			return (List<AssistTextEntry>) textMap.get("ateArr");
		} catch (Exception e) {
		
			log.error("Exception in getAssistTextList : ", e);
			
			throw e;
		}

	}

	public static List<RoleEntry> getRoleList() throws Exception {
		

		ArrayList<RoleEntry> roleList = null;
		IAgileSession session = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, ArrayList<String>> assistRoles = (HashMap<String, ArrayList<String>>) dbh.handleDBRequest("getRolePriorities", null, false);//.getRolePriorities();
			session = AgileHandler.getAgileSession();
			
			if(Constants.Config.ACCESSTYPEROLE==null)
			{
				HashMap params=new HashMap<String,String>();
				params.put("key", "accessType");
				HashMap map=dbh.handleDBRequest("getConfigByKey", params, false);//.getConfigByKey("accessType");
				Constants.Config.ACCESSTYPEROLE=(String) map.get("value");
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
			
			log.debug("allRoleKey: "+allRoleKey);
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
			Collections.sort(roleList, new Comparator<RoleEntry>() 
			{
				public int compare(RoleEntry r1, RoleEntry r2) 
				{
					

					return (r1.getRole().compareToIgnoreCase(r2.getRole()));
				}
			});
			Collections.sort(roleList, new Comparator<RoleEntry>() 
			{
				public int compare(RoleEntry r1, RoleEntry r2) 
				{
					

					return (new Integer(r2.getPriority())).compareTo(new Integer(r1.getPriority()));
				}
			});
		} catch (Exception e) 
		{
			log.error("Exception in getRoleList : ", e);
			
			throw e;
		} finally 
		{
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
		log.debug("Entering getConfigEntries...");

		HashMap<String, ConfigEntry> configArr = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, String> configMap = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
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

			log.debug("Exiting getConfigEntries...");

			return configArr;
		} catch (Exception e) 
		{

			log.error("Exception in getConfigEntries", e);

			throw e;
		}
	}
}
