package com.xavor.plmxl.assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.agile.api.APIException;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.INode;
import com.agile.api.IProperty;
import com.agile.api.IRole;
import com.agile.api.IRoutableDesc;
import com.agile.api.ITable;
import com.agile.api.ITableDesc;
import com.agile.api.IUserGroup;
import com.agile.api.IWorkflow;
import com.agile.api.PropertyConstants;
import com.xavor.plmxl.assist.DO.AssistColorEntry;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.AttributeEntry;
import com.xavor.plmxl.assist.DO.BasicModel;
import com.xavor.plmxl.assist.DO.CListModel;
import com.xavor.plmxl.assist.DO.ClassEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;
import com.xavor.plmxl.assist.Util.GenericComparator;
import com.xavor.plmxl.assist.Util.ListComparator;

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
		} 
		log.debug("Exiting getClassList...");

		return classarr;
	}
	public  List<String> getClassAttributeName(int classId, int attId) throws Exception {
		log.debug("Entering getClassAttributeName...");

		List<String> NamesList = new ArrayList<String>();

		IAgileClass cls = AgileHandler.getAgileClass(adminInstance, classId);
		if(cls!=null)
		{
			log.debug(cls.getName());
			NamesList.add(cls.getName());				
		}
		else
		{
			NamesList.add("");	
			NamesList.add("");	
		}			
		if(cls!=null )
		{
			if(cls.getAttribute(attId)!=null)
			{
				IAttribute att=cls.getAttribute(attId);
				log.debug(att.getName());
				NamesList.add(att.getName());
			}
			else
			{
				NamesList.add("");		

			}
		}
		log.debug("size: "+NamesList.size());

		log.debug("Exiting getClassAttributeName...");
		return NamesList;
	}
	
	public static List<AttributeEntry> getAttributeList(int classId, int level) throws Exception {
		log.debug("Entering getAttributeList...");

		IAgileSession session = null;
		List<AttributeEntry> attributeEntryList = new ArrayList<AttributeEntry>();
		List<AttributeEntry> attributesList = new ArrayList<AttributeEntry>();
		boolean gotClasses = false;
		try {
			session = AgileHandler.getAgileSession();			
			if(level==3)
			{
				log.debug("Level: "+level);
				IAgileClass cls = AgileHandler.getAgileClass(session, classId);	
				IAgileClass cls1 = AgileHandler.getAgileClass(session, Integer.parseInt(cls.getSuperClass().getId().toString()));
				IAgileClass cls2 = AgileHandler.getAgileClass(session, Integer.parseInt(cls1.getSuperClass().getId().toString()));
				
				attributesList=getAttributes(cls2, classId,1);
				//sorting in ascending order
				Collections.sort(attributesList, new GenericComparator("attrName", true));
				if(attributesList!=null)		
					attributeEntryList.addAll(attributesList);				
				
				attributesList=getAttributes(cls1, classId,2);
				Collections.sort(attributesList, new GenericComparator("attrName", true));
				if(attributesList!=null)	
					attributeEntryList.addAll(attributesList);
			
				attributesList=getAttributes(cls, classId,level);
				Collections.sort(attributesList, new GenericComparator("attrName", true));
				if(attributesList!=null)
					attributeEntryList.addAll(attributesList);
				
				gotClasses = true;
			}
			else if(level==2)
			{
				IAgileClass cls = AgileHandler.getAgileClass(session, classId);	
				IAgileClass cls1 = AgileHandler.getAgileClass(session, Integer.parseInt(cls.getSuperClass().getId().toString()));
				
				attributesList=getAttributes(cls1,classId,1);
				Collections.sort(attributesList, new GenericComparator("attrName", true));
				if(attributesList!=null)
					attributeEntryList.addAll(attributesList);
				
				attributesList=getAttributes(cls, classId,level);
				Collections.sort(attributesList, new GenericComparator("attrName", true));
				if(attributesList!=null)
					attributeEntryList.addAll(attributesList);		
								
				gotClasses = true;
			}
			else if(level==1)
			{
				IAgileClass cls = AgileHandler.getAgileClass(session, classId);	
				
				attributesList=getAttributes(cls, classId,level);
				Collections.sort(attributesList, new GenericComparator("attrName", true));
				if(attributesList!=null)
					attributeEntryList.addAll(attributesList);
				
				gotClasses = true;
			}
		} catch (Exception e) {
			
			log.error("Exception in getAttributeList : ", e);
			
			throw e;
		} 
		if (gotClasses) {
			log.debug("Exiting getAttributeList...");

			return attributeEntryList;
		} else {
			throw new Exception("Class does not have a " + ((level == 3) ? "Page Three" : ((level == 2) ? "Page Two" : "Title Block/General Info/Cover Page"))
					+ " defined.");
			
		}
	}

	private static List<AttributeEntry> getAttributes(IAgileClass cls, int classId, int level) throws Exception
	{
		log.debug("Entering getAttributes..");
		List<AttributeEntry> attributesList = new ArrayList<AttributeEntry>();			
		ITableDesc[] tbdesc = cls.getTableDescriptors();
		for (ITableDesc tbdes : tbdesc) {
			if (((level == 3) && tbdes.getAPIName().equalsIgnoreCase("PageThree"))
					|| ((level == 2) && tbdes.getAPIName().equalsIgnoreCase("PageTwo"))
					|| ((level == 1) && (tbdes.getAPIName().equalsIgnoreCase("CoverPage") || tbdes.getAPIName().equalsIgnoreCase("TitleBlock") || tbdes
							.getAPIName().equalsIgnoreCase("GeneralInfo"))))
			{
			
				attributesList = parseToAttributeEntryList(tbdes.getAttributes(), classId);
			
			}
		}
		log.debug("Exiting getAttributes..");
		return attributesList;
	}

	
	private static List<AttributeEntry> parseToAttributeEntryList(IAttribute[] attrArr, int classId) throws Exception 
	{	
		log.debug("Entering parseToAttributeEntryList..");
		ArrayList<AttributeEntry> attrLst = new ArrayList<AttributeEntry>();
		
		AttributeEntry attrentry = null;
		AssistColorEntry colorEntry=null;
		
		DBHandler dbh = new DBHandler();
		HashMap params=new HashMap<String,String>();
		params.put("classID", classId);
		HashMap<String,HashMap<Integer,Integer>> map = (HashMap<String, HashMap<Integer,Integer>>) dbh.handleDBRequest("checkAssistText", params, false);//.checkAssistText(classId);
		HashMap<Integer,Integer> rs=map.get("result");
		HashMap colorsParams=new HashMap<String,Object>();
		colorsParams.put("classId", Integer.toString(classId));
		Map attColors=dbh.handleDBRequest("getAssistColorsForClass", colorsParams, false);//.getAssistColors(classes.toString(),true);
		
		for (IAttribute attr : attrArr) 
		{			
			attrentry = new AttributeEntry();
			if (rs.get(attr.getId())!=null) 
			{
				attrentry.sethasTextFlag(Constants.General.TextFlagSet);
			} else 
			{
				if(attr.isVisible())
					attrentry.sethasTextFlag(Constants.General.TextFlagNotSet);
				else
					continue;
			}
			attrentry.setClassID(String.valueOf(classId));
			attrentry.setAttrID(attr.getId().toString());
			attrentry.setAttrName(attr.getFullName());
			attrentry.setIsVisible((attr.isVisible()?"yes":"no"));
			
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
		log.debug("Exiting parseToAttributeEntryList..");
		return attrLst;
	}
	
	public static List<CListModel> getClassWorkflows(String classId,int level) throws APIException
	{
		log.debug("Entering getClassWorkflows...");

		List<CListModel> workflows=new ArrayList<CListModel>(0);
		List<String> workflowNames=new ArrayList<String>();
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
				IAgileClass cls = AgileHandler.getAgileClass(session, Integer.parseInt(classId));
				wfs =((IRoutableDesc)cls).getWorkflows();
				
				if(wfs!=null)
				{
					for (IWorkflow iWorkflow : wfs) 
					{
						workflows.add(new CListModel(iWorkflow.getName(),iWorkflow.getName(),getWorkflowStatuses((INode[])iWorkflow.getChildren())));
						workflowNames.add(iWorkflow.getName());
					}
				}
				if(level==1)
				{
					IAgileClass[] subclsarr = cls.getSubclasses();
					for (IAgileClass subcls : subclsarr)
					{
						wfs=null;
						IAgileClass cls1 = AgileHandler.getAgileClass(session, Integer.parseInt(subcls.getId().toString()));
						wfs =((IRoutableDesc)cls1).getWorkflows();
						if(wfs!=null)
						{
							for (IWorkflow iWorkflow : wfs) 
							{
								if(!workflowNames.contains(iWorkflow.getName()))
								{
									workflows.add(new CListModel(iWorkflow.getName(),iWorkflow.getName(),getWorkflowStatuses((INode[])iWorkflow.getChildren())));
									workflowNames.add(iWorkflow.getName());
								}
							}
						}
						
						IAgileClass[] concclsarr = subcls.getSubclasses();
						for (IAgileClass conccls : subclsarr)
						{
							wfs=null;
							IAgileClass cls2 = AgileHandler.getAgileClass(session, Integer.parseInt(conccls.getId().toString()));
							wfs =((IRoutableDesc)cls2).getWorkflows();
							if(wfs!=null)
							{
								for (IWorkflow iWorkflow : wfs) 
								{
									if(!workflowNames.contains(iWorkflow.getName()))
									{
										workflows.add(new CListModel(iWorkflow.getName(),iWorkflow.getName(),getWorkflowStatuses((INode[])iWorkflow.getChildren())));
										workflowNames.add(iWorkflow.getName());
									}
								}
							}
						}
					
						
					}
					
						
				}
				
			}
			catch(Exception e)
			{
				log.error("Exception in getClassWorkflows : ", e);
				
				return null;
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
	public List<BasicModel> getWorkflowStatuses(String wfName) throws APIException
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
			
			roleList = new ArrayList<RoleEntry>();
			
			String allRoleKey=ConfigHelper.configureAccessType(dbh);
			
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
			{
				Collection agileRoles = AgileHandler.getAllRoles(session);
				populateInfoFromRole(roleList, assistRoles, agileRoles);
			}
			else
			{
				ITable tbl=AgileHandler.getAllUserGroups(session);
				populateInfoFromUserGroup(roleList, assistRoles, tbl);
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
		} 
		

		return roleList;
	}
	
	public static List<String> getAllRoles() throws Exception {
		
		List<String> roleList = null;
		IAgileSession session = null;
		try {
			DBHandler dbh = new DBHandler();
			session = AgileHandler.getAgileSession();			
			roleList = new ArrayList<String>();			
			
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
			{
				Collection agileRoles = AgileHandler.getAllRoles(session);
				for (Object roleObj : agileRoles) 
				{
					IRole role = (IRole) roleObj;
					String id = role.getId().toString();
					roleList.add(id);
				}
			}
			else
			{
				ITable tbl=AgileHandler.getAllUserGroups(session);
				Iterator itr=tbl.getReferentIterator();				
				while (itr.hasNext())
				{
					IUserGroup userGroup=(IUserGroup)itr.next();	
					String id = userGroup.getObjectId().toString();
					roleList.add(id);
				}
			}
			roleList.add("0");
			
			Collections.sort(roleList);
		} catch (Exception e) 
		{
			log.error("Exception in getAllRoles : ", e);
			
			throw e;
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
}
