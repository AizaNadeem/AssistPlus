package com.xavor.plmxl.assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.agile.api.ITreeNode;
import com.agile.api.IUserGroup;
import com.agile.api.IWorkflow;
import com.agile.api.ItemConstants;
import com.agile.api.NodeConstants;
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

	static AssistLogger log = AssistLogger.getInstance();
	IAgileSession session = null;
	IAdmin adminInstance = null;
	Boolean isRoutable = null;

	public UIListHandler(DBHandler db) throws Exception {
		AgileHandler.disconnect();
		session = AgileHandler.getAgileSession(db);
		adminInstance = AgileHandler.getAdminInstance(session);
	}

	public UIListHandler() throws Exception {
		AgileHandler.disconnect();
		session = AgileHandler.getAgileSession();
		adminInstance = AgileHandler.getAdminInstance(session);
	}
	
	public Boolean isRoutable(){
		return isRoutable;
	}

	public List<ClassEntry> getClassList() throws Exception {
		log.debug("Entering getClassList...");

		ArrayList<ClassEntry> classarr = new ArrayList<ClassEntry>();
		DBHandler dbh = new DBHandler();
		ArrayList<Integer> rs = new ArrayList<Integer>();
		HashMap result = dbh.handleDBRequest("checkClassesAssistText", null, false);// .checkClassesAssistText();
		rs = (ArrayList<Integer>) result.get("result");
		// IAgileSession session = null;
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

	public List<String> getClassAttributeName(int classId, int attId) throws Exception {
		log.debug("Entering getClassAttributeName...");

		List<String> NamesList = new ArrayList<String>();

		IAgileClass cls = AgileHandler.getAgileClass(adminInstance, classId);
		if (cls != null) {
			log.debug(cls.getAPIName());
			NamesList.add(cls.getAPIName());
		} else {
			NamesList.add("");
			NamesList.add("");
		}
		if (cls != null) {
			if (attId != -1 && cls.getAttribute(attId) != null) {
				IAttribute att = cls.getAttribute(attId);
				log.debug(att.getFullyQualifiedAPIName());
				NamesList.add(att.getFullyQualifiedAPIName());
			} else {
				NamesList.add("");

			}
		}
		log.debug("size: " + NamesList.size());

		log.debug("Exiting getClassAttributeName...");
		return NamesList;
	}

	public HashMap<String, String> getClassAttributeID(String classAPIName, String attAPIName) throws Exception {
		log.debug("Entering getClassAttributeName...");

		HashMap<String, String> idList = new HashMap<String, String>();

		IAgileClass cls = null;
		try {
			cls = AgileHandler.getAgileClass(adminInstance, classAPIName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			idList.put("classid", "");
			idList.put("attid", "");
			return idList;
		}
		if (cls != null) {
			log.debug(cls.getAPIName());
			idList.put("classid", cls.getId().toString());
		} else {
			idList.put("classid", "");
		}
		if (cls != null) {
			IAttribute att = null;
			try {
				if (attAPIName != null && !attAPIName.equalsIgnoreCase("")) {
					att = cls.getAttribute(attAPIName);
				} else {
					idList.put("attid", "");
					return idList;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// idList.put("classid","");
				idList.put("attid", "");
				return idList;
			}
			if (att != null) {

				log.debug(att.getId().toString());
				idList.put("attid", att.getId().toString());
			} else {
				idList.put("attid", "");

			}
		}
		log.debug("size: " + idList.size());

		log.debug("Exiting getClassAttributeName...");
		return idList;
	}

	public List<AttributeEntry> getAttributeList(int classId, int level) throws Exception {
		log.debug("Entering getAttributeList...");

		// IAgileSession session = null;
		List<AttributeEntry> attributeEntryList = new ArrayList<AttributeEntry>();
		List<AttributeEntry> attributesList = new ArrayList<AttributeEntry>();
		boolean gotClasses = false;
		try {
			session = AgileHandler.getAgileSession();
			// if(level==3)
			// {
			log.debug("Level: " + level);
			IAgileClass cls = AgileHandler.getAgileClass(session, classId);
			// IAgileClass cls1 = AgileHandler.getAgileClass(session,
			// Integer.parseInt(cls.getSuperClass().getId().toString()));
			// IAgileClass cls2 = AgileHandler.getAgileClass(session,
			// Integer.parseInt(cls1.getSuperClass().getId().toString()));

			// attributesList=getAttributes(cls2, classId,1);
			// //sorting in ascending order
			// Collections.sort(attributesList, new
			// GenericComparator("attrName", true));
			// if(attributesList!=null)
			// attributeEntryList.addAll(attributesList);
			//
			// attributesList=getAttributes(cls1, classId,2);
			// Collections.sort(attributesList, new
			// GenericComparator("attrName", true));
			// if(attributesList!=null)
			// attributeEntryList.addAll(attributesList);

			attributesList = getAttributes(cls, classId, level);
			Collections.sort(attributesList, new GenericComparator("attrName", true));
			if (attributesList != null)
				attributeEntryList.addAll(attributesList);

			gotClasses = true;
			// }

		} catch (Exception e) {

			log.error("Exception in getAttributeList : ", e);

			throw e;
		}
		if (gotClasses) {
			log.debug("Exiting getAttributeList...");

			return attributeEntryList;
		} else {
			throw new Exception(
					"Class does not have a "
							+ ((level == 3) ? "Page Three"
									: ((level == 2) ? "Page Two" : "Title Block/General Info/Cover Page"))
							+ " defined.");

		}
	}

	private List<AttributeEntry> getAttributes(IAgileClass cls, int classId, int level) throws Exception {
		log.debug("Entering getAttributes..");
		List<AttributeEntry> attributesList = new ArrayList<AttributeEntry>();
		ITableDesc[] tbdesc = cls.getTableDescriptors();
		for (ITableDesc tbdes : tbdesc) {
			if (((level == 3)
					&& (tbdes.getAPIName().equalsIgnoreCase("Attachments") || tbdes.getAPIName().equalsIgnoreCase("BOM")
							|| tbdes.getAPIName().equalsIgnoreCase("AffectedItems")
							|| tbdes.getAPIName().equalsIgnoreCase("PageThree")
							|| tbdes.getAPIName().equalsIgnoreCase("PageTwo")
							|| tbdes.getAPIName().equalsIgnoreCase("CoverPage")
							|| tbdes.getAPIName().equalsIgnoreCase("TitleBlock")
							|| tbdes.getAPIName().equalsIgnoreCase("GeneralInfo")))
					|| ((level == 2) && (tbdes.getAPIName().equalsIgnoreCase("Attachments")
							|| tbdes.getAPIName().equalsIgnoreCase("BOM")
							|| tbdes.getAPIName().equalsIgnoreCase("AffectedItems")
							|| tbdes.getAPIName().equalsIgnoreCase("PageTwo")
							|| tbdes.getAPIName().equalsIgnoreCase("CoverPage")
							|| tbdes.getAPIName().equalsIgnoreCase("TitleBlock")
							|| tbdes.getAPIName().equalsIgnoreCase("GeneralInfo")))
					|| ((level == 1) && (tbdes.getAPIName().equalsIgnoreCase("Attachments")
							|| tbdes.getAPIName().equalsIgnoreCase("BOM")
							|| tbdes.getAPIName().equalsIgnoreCase("AffectedItems")
							|| tbdes.getAPIName().equalsIgnoreCase("CoverPage")
							|| tbdes.getAPIName().equalsIgnoreCase("TitleBlock")
							|| tbdes.getAPIName().equalsIgnoreCase("GeneralInfo")))) {

				attributesList.addAll(parseToAttributeEntryList(tbdes.getAttributes(), classId));

			}
		}
		try {
			IRoutableDesc class1 = (IRoutableDesc) cls;
			attributesList.addAll(parseToAttributeEntryList(class1.getWorkflows(), classId));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("Exiting getAttributes..");
		return attributesList;
	}

	private List<AttributeEntry> parseToAttributeEntryList(IAttribute[] attrArr, int classId) throws Exception {
		log.debug("Entering parseToAttributeEntryList..");
		ArrayList<AttributeEntry> attrLst = new ArrayList<AttributeEntry>();

		AttributeEntry attrentry = null;
		AssistColorEntry colorEntry = null;

		DBHandler dbh = new DBHandler();
		HashMap params = new HashMap<String, String>();
		params.put("classID", classId);
		HashMap<String, HashMap<Integer, Integer>> map = (HashMap<String, HashMap<Integer, Integer>>) dbh
				.handleDBRequest("checkAssistText", params, false);// .checkAssistText(classId);
		HashMap<Integer, Integer> rs = map.get("result");
		HashMap colorsParams = new HashMap<String, Object>();
		colorsParams.put("classId", Integer.toString(classId));
		Map attColors = dbh.handleDBRequest("getAssistColorsForClass", colorsParams, false);// .getAssistColors(classes.toString(),true);

		for (IAttribute attr : attrArr) {
			attrentry = new AttributeEntry();
			if (rs.get(attr.getId().toString()) != null) {
				attrentry.sethasTextFlag(Constants.General.TextFlagSet);
			} else {
				if (attr.isVisible())
					attrentry.sethasTextFlag(Constants.General.TextFlagNotSet);
				else
					continue;
			}
			attrentry.setClassID(String.valueOf(classId));
			attrentry.setAttrID(attr.getId().toString());
			if (!attr.getFullName().toString().contains("(Image)")) {
				attrentry.setAttrName(attr.getFullName());
			} else {
				continue;
			}
			attrentry.setIsVisible((attr.isVisible() ? "yes" : "no"));

			if (attColors.containsKey(attrentry.getAttrID())) {
				colorEntry = (AssistColorEntry) attColors.get(attrentry.getAttrID());
				attrentry.setAssistColorId(colorEntry.getColorId());
				attrentry.setAssistColor(colorEntry.getAssistColor());
			}

			try {
				IProperty descProp = attr.getProperty(PropertyConstants.PROP_DESCRIPTION);
				if ((descProp != null) && (descProp.getValue() != null)) {
					attrentry.setAttrDescription(
							(descProp.getValue().toString().equals("") ? " " : descProp.getValue().toString()));
				} else {
					attrentry.setAttrDescription("");
				}
			} catch (Exception e) {
				attrentry.setAttrDescription("");
				log.error("Exception in parseToAttributeEntryList : ", e);

			}
			attrentry.setIsWorkflow("false");
			attrLst.add(attrentry);
		}
		log.debug("Exiting parseToAttributeEntryList..");
		return attrLst;
	}

	private List<AttributeEntry> parseToAttributeEntryList(IWorkflow[] workflows, int classId) throws Exception {
		log.debug("Entering parseToAttributeEntryList..");
		ArrayList<AttributeEntry> attrLst = new ArrayList<AttributeEntry>();

		AttributeEntry attrentry = null;
		AssistColorEntry colorEntry = null;

		DBHandler dbh = new DBHandler();
		HashMap params = new HashMap<String, String>();
		params.put("classID", classId);
		HashMap<String, HashMap<String, String>> map = (HashMap<String, HashMap<String, String>>) dbh
				.handleDBRequest("checkAssistText", params, false);// .checkAssistText(classId);
		HashMap<String, String> rs = map.get("result");
		HashMap colorsParams = new HashMap<String, Object>();
		colorsParams.put("classId", Integer.toString(classId));
		Map attColors = dbh.handleDBRequest("getAssistColorsForClass", colorsParams, false);// .getAssistColors(classes.toString(),true);

		for (IWorkflow wf : workflows) {

			List<CListModel> list = getWorkflowStatuses((INode[]) wf.getChildren());
			for (CListModel l : list) {
				attrentry = new AttributeEntry();
				if (rs.get(wf.getName().toString().replaceAll("\\s", "") + l.getId().replaceAll("\\s", "")) != null) {
					attrentry.sethasTextFlag(Constants.General.TextFlagSet);
				} else {
					attrentry.sethasTextFlag(Constants.General.TextFlagNotSet);
				}
				attrentry.setClassID(String.valueOf(classId));
				attrentry.setAttrID(wf.getName().toString().replaceAll("\\s", "") + l.getId().replaceAll("\\s", ""));
				attrentry.setAttrName("Workflow." + wf.getName() + "." + l.getId());
				attrentry.setIsVisible(("yes"));

				if (attColors.containsKey(attrentry.getAttrID())) {
					colorEntry = (AssistColorEntry) attColors.get(attrentry.getAttrID());
					attrentry.setAssistColorId(colorEntry.getColorId());
					attrentry.setAssistColor(colorEntry.getAssistColor());
				}

				try {
					IProperty descProp = wf.getProperty(PropertyConstants.PROP_DESCRIPTION);
					if ((descProp != null) && (descProp.getValue() != null)) {
						attrentry.setAttrDescription(
								(descProp.getValue().toString().equals("") ? " " : descProp.getValue().toString()));
					} else {
						attrentry.setAttrDescription("");
					}
				} catch (Exception e) {
					attrentry.setAttrDescription("");
					log.error("Exception in parseToAttributeEntryList : ", e);

				}
				attrentry.setIsWorkflow("true");
				attrLst.add(attrentry);
			}
		}
		log.debug("Exiting parseToAttributeEntryList..");
		return attrLst;
	}

	public List<CListModel> getClassWorkflows(String classId, int level) throws APIException {
		log.debug("Entering getClassWorkflows...XXXXXX");

		List<CListModel> workflows = new ArrayList<CListModel>(0);
		List<String> workflowNames = new ArrayList<String>();
		// IAgileSession session=null;
		try {
			session = AgileHandler.getAgileSession();
		} catch (Exception ex) {

			log.error("Exception in getClassWorkflows : ", ex);

		}

		if (session != null) {

			IWorkflow[] wfs = null;
			try {
				
				IAgileClass cls = AgileHandler.getAgileClass(session, Integer.parseInt(classId));

				if (cls.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS) /*!(cls instanceof IRoutableDesc)*/) {
					isRoutable = false;
					ArrayList<CListModel> allLifecyclesOfClass = new ArrayList<CListModel>();
					List<String> listOfLifecycles = getLifeCyclePhases(cls, level);
					if(listOfLifecycles.size() != 0){
						for (String lifecycle : listOfLifecycles) {
							workflows.add(new CListModel(lifecycle, lifecycle));
						}
			//			workflows.add(new CListModel("Lifecycles", "Lifecycles", allLifecyclesOfClass));
					}
				} else if(cls instanceof IRoutableDesc) {
					isRoutable = true;

					IRoutableDesc class1 = (IRoutableDesc) cls;
					wfs = class1.getWorkflows();

					if (wfs != null) {
						for (IWorkflow iWorkflow : wfs) {
							workflows.add(new CListModel(iWorkflow.getName(), iWorkflow.getName(),
									getWorkflowStatuses((INode[]) iWorkflow.getChildren())));
							workflowNames.add(iWorkflow.getName());
							log.debug("workflow: " + iWorkflow.getName());

						}
					}
					if (level == 1) {
						IAgileClass[] subclsarr = cls.getSubclasses();
						for (IAgileClass subcls : subclsarr) {
							wfs = null;
							IAgileClass cls1 = AgileHandler.getAgileClass(session,
									Integer.parseInt(subcls.getId().toString()));
							IRoutableDesc class2 = (IRoutableDesc) cls1;
							wfs = class2.getWorkflows();
							if (wfs != null) {
								for (IWorkflow iWorkflow : wfs) {
									if (!workflowNames.contains(iWorkflow.getName())) {
										workflows.add(new CListModel(iWorkflow.getName(), iWorkflow.getName(),
												getWorkflowStatuses((INode[]) iWorkflow.getChildren())));
										workflowNames.add(iWorkflow.getName());
										log.debug("workflow: " + iWorkflow.getName());
									}
								}
							}

							IAgileClass[] concclsarr = subcls.getSubclasses();
							for (IAgileClass conccls : subclsarr) {
								wfs = null;
								IAgileClass cls2 = AgileHandler.getAgileClass(session,
										Integer.parseInt(conccls.getId().toString()));
								IRoutableDesc class3 = (IRoutableDesc) cls2;
								wfs = class3.getWorkflows();
								if (wfs != null) {
									for (IWorkflow iWorkflow : wfs) {
										if (!workflowNames.contains(iWorkflow.getName())) {
											workflows.add(new CListModel(iWorkflow.getName(), iWorkflow.getName(),
													getWorkflowStatuses((INode[]) iWorkflow.getChildren())));
											workflowNames.add(iWorkflow.getName());
										}
									}
								}
							}

						}

					}
				}

			} catch (Exception e) {
				log.error("Exception in getClassWorkflows : ", e);

				return null;
			}

			Collections.sort(workflows, new ListComparator());
			
			if(isRoutable == null){
				
			} else if(isRoutable){
				workflows.add(0, new CListModel("All Workflows", "All Workflows"));
			} 
			else{
				workflows.add(0, new CListModel("All Lifecycles", "All Lifecycles"));
			}
			

			
			

		}

		log.debug("Workflow: " + workflows.toString());
		log.debug("Exiting getClassWorkflows...");

		return workflows;
	}

	public List<CListModel> getWorkflowStatuses(INode[] nodes) {
		log.debug("Entering getWorkflowStatuses...");

		List<CListModel> statuses = new ArrayList<CListModel>(0);
		log.debug("nodes length : " + nodes.length);
		try {

			for (INode iNode : nodes) {
				if (iNode.getAPIName().equalsIgnoreCase("StatusList")) {

					log.debug(iNode.getAPIName());
					if (iNode.getChildren() != null) {
						INode sNodes[] = (INode[]) iNode.getChildren();

						for (INode sNode : sNodes) {
							statuses.add(new CListModel(sNode.getName(), sNode.getName()));
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception in getWorkflowStatuses : ", e);
		}
		log.debug("WorkflowStatuses: " + statuses.toString());
		log.debug("Exiting getWorkflowStatuses...");

		return statuses;
	}

	public List<BasicModel> getWorkflowStatuses(String wfName) throws APIException {
		log.debug("Entering getWorkflowStatuses...");

		List<BasicModel> statuses = new ArrayList<BasicModel>(0);
		// IAgileSession session=null;
		try {
			session = AgileHandler.getAgileSession();
		} catch (Exception ex) {

			log.error("Exception in getWorkflowStatuses : ", ex);

		}
		if (session != null) {
			IAdmin admin = session.getAdminInstance();

			INode node = admin.getNode(wfName);

			INode nodes[] = (INode[]) node.getChildren();

			for (INode iNode : nodes) {
				if (iNode.getAPIName().equalsIgnoreCase("StatusList")) {
					INode sNodes[] = (INode[]) iNode.getChildren();

					for (INode sNode : sNodes) {
						statuses.add(new BasicModel(sNode.getAPIName(), sNode.getName()));
					}
				}
			}
		}
		log.debug("WorkflowStatuses: " + statuses.toString());
		log.debug("Exiting getWorkflowStatuses...");

		return statuses;
	}

	public List<AssistTextEntry> getAssistTextList(String classId, String attrId, String isRoutable) throws Exception {
		try {
			log.debug("Entering getAssistTextList...");

			DBHandler dbh = new DBHandler();
			HashMap params = new HashMap<String, String>();
			params.put("classID", classId);
			params.put("attrId", attrId);
			params.put("isRoutable", isRoutable);
			HashMap textMap = new HashMap<String, ArrayList<AssistTextEntry>>();
			textMap = dbh.handleDBRequest("getAssistTexts", params, false);
			log.debug("Exiting getAssistTextList...");

			return (List<AssistTextEntry>) textMap.get("ateArr");
		} catch (Exception e) {

			log.error("Exception in getAssistTextList : ", e);

			throw e;
		}

	}

	public List<RoleEntry> getRoleList() throws Exception {

		ArrayList<RoleEntry> roleList = null;
		// IAgileSession session = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, ArrayList<String>> assistRoles = (HashMap<String, ArrayList<String>>) dbh
					.handleDBRequest("getRolePriorities", null, false);// .getRolePriorities();
			session = AgileHandler.getAgileSession();

			roleList = new ArrayList<RoleEntry>();

			String allRoleKey = ConfigHelper.configureAccessType(dbh);

			if ("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE)) {
				Collection agileRoles = AgileHandler.getAllRoles(session);
				populateInfoFromRole(roleList, assistRoles, agileRoles);
			} else {
				ITable tbl = AgileHandler.getAllUserGroups(session);
				populateInfoFromUserGroup(roleList, assistRoles, tbl);
			}

			log.debug("allRoleKey: " + allRoleKey);
			RoleEntry roleEntry = new RoleEntry();
			roleEntry.setRoleID("0");
			roleEntry.setRole(allRoleKey);

			if (assistRoles.containsKey(allRoleKey)) {
				roleEntry.setPriority(Integer.parseInt(assistRoles.get(allRoleKey).get(0)));
				roleEntry.setFontColor(assistRoles.get(allRoleKey).get(1));
				roleEntry.setBackgroundColor(assistRoles.get(allRoleKey).get(2));
			} else {
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
			log.error("Exception in getRoleList : ", e);

			throw e;
		}

		return roleList;
	}

	public List<String> getAllRoles() throws Exception {

		List<String> roleList = null;
		// IAgileSession session = null;
		try {
			DBHandler dbh = new DBHandler();
			session = AgileHandler.getAgileSession();
			roleList = new ArrayList<String>();

			if ("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE)) {
				Collection agileRoles = AgileHandler.getAllRoles(session);
				for (Object roleObj : agileRoles) {
					IRole role = (IRole) roleObj;
					String id = role.getId().toString();
					roleList.add(id);
				}
			} else {
				ITable tbl = AgileHandler.getAllUserGroups(session);
				Iterator itr = tbl.getReferentIterator();
				while (itr.hasNext()) {
					IUserGroup userGroup = (IUserGroup) itr.next();
					String id = userGroup.getObjectId().toString();
					roleList.add(id);
				}
			}
			roleList.add("0");

			Collections.sort(roleList);
		} catch (Exception e) {
			log.error("Exception in getAllRoles : ", e);

			throw e;
		}

		return roleList;
	}

	private void populateInfoFromRole(List<RoleEntry> roleList, HashMap<String, ArrayList<String>> assistRoles,
			Collection agileRoles) throws APIException {

		for (Object roleObj : agileRoles) {
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

	private void populateInfoFromUserGroup(List<RoleEntry> roleList, HashMap<String, ArrayList<String>> assistRoles,
			ITable agileUserGroups) throws APIException {

		Iterator itr = agileUserGroups.getReferentIterator();

		while (itr.hasNext()) {
			IUserGroup userGroup = (IUserGroup) itr.next();

			String name = userGroup.getName();
			String id = userGroup.getObjectId().toString();
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

	private List<IAgileClass> getAllSubclasses(IAgileClass baseClass) throws APIException {
		log.debug("Getting all subclasses of " + baseClass.getName());
		List<IAgileClass> allSubclasses = new ArrayList<IAgileClass>();

		IAgileClass[] subclasses = baseClass.getSubclasses();

		if (subclasses.length != 0) {
			for (IAgileClass subclass : subclasses) {
				IAgileClass[] sub_subclasses = subclass.getSubclasses();
				if (sub_subclasses.length != 0) {
					for (IAgileClass sub_subclass : sub_subclasses) {
						allSubclasses.add(sub_subclass);
					}
				} else {
					allSubclasses.add(subclass);
				}

			}
		} else {
			allSubclasses.add(baseClass);
		}
		log.debug("Got all subclasses");
		return allSubclasses;
	}

	public List<String> getLifeCyclePhases(IAgileClass cls, int level) throws Exception {
		IAdmin admin = session.getAdminInstance();
		String className = cls.getName();
		INode[] lifecyclephase = null;
		List<INode> lfp = null, lfp2 = null;
		if (level == 1)
			lifecyclephase = (INode[]) ((INode) admin.getNode(NodeConstants.NODE_LIFECYCLE_PHASES)
					.getChildNode(className)).getChildren();
		else if (level == 2) {
			String parentClassName = cls.getSuperClass().getName();
			lifecyclephase = (INode[]) ((INode) admin.getNode(NodeConstants.NODE_LIFECYCLE_PHASES)
					.getChildNode(parentClassName)).getChildren();
			lfp = (List<INode>) ((INode) admin.getNode(NodeConstants.NODE_AGILE_CLASSES).getChild(className))
					.getChildNode("LifeCycle Phases").getChildNodes();
		} else if (level == 3) {

			String parentClassName = cls.getSuperClass().getName();
			String superClassName = cls.getSuperClass().getSuperClass().getName();
			lifecyclephase = (INode[]) ((INode) admin.getNode(NodeConstants.NODE_LIFECYCLE_PHASES)
					.getChildNode(superClassName)).getChildren();
			lfp = (List<INode>) ((INode) admin.getNode(NodeConstants.NODE_AGILE_CLASSES).getChild(parentClassName))
					.getChildNode("LifeCycle Phases").getChildNodes();

			ITreeNode temp = (ITreeNode) ((INode) admin.getNode(NodeConstants.NODE_AGILE_CLASSES)
					.getChild(parentClassName)).getChildNode("User-Defined Subclasses");
			ITreeNode temp2 = temp.getChildNode(cls.getId());

			ITreeNode temp3 = temp2.getChildNode("LifeCycle Phases");
			lfp2 = (List<INode>) temp3.getChildNodes();

			// lfp2 = (List<INode>) ((INode)
			// admin.getNode(NodeConstants.NODE_AGILE_CLASSES).getChild(parentClassName))
			// .getChildNode("User-Defined
			// Subclasses").getChildNode(className).getChildNode("LifeCycle
			// Phases")
			// .getChildNodes();
		}
		List<String> listValues = new ArrayList<String>();

		for (int i = 0; i < lifecyclephase.length; i++) {
			String name = lifecyclephase[i].getName();
			listValues.add(name);

		}

		if (lfp != null)
			for (int i = 0; i < lfp.size(); i++) {
				String name = lfp.get(i).getName();
				listValues.add(name);
			}

		if (lfp2 != null)
			for (int i = 0; i < lfp2.size(); i++) {
				String name = lfp2.get(i).getName();
				listValues.add(name);
			}

		return listValues;
	}

	private List<String> union(List<String> list1, List<String> list2) {
		HashSet<String> set = new HashSet<String>();

		set.addAll(list1);
		set.addAll(list2);

		return new ArrayList<String>(set);
	}
	


}
