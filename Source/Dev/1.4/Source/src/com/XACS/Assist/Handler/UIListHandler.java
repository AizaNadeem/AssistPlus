package com.XACS.Assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.XACS.Assist.DO.AssistTextEntry;
import com.XACS.Assist.DO.AttributeEntry;
import com.XACS.Assist.DO.ClassEntry;
import com.XACS.Assist.DO.ConfigEntry;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Util.Constants;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IProperty;
import com.agile.api.IRole;
import com.agile.api.ITableDesc;
import com.agile.api.PropertyConstants;

public class UIListHandler {
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

	private static List<AttributeEntry> parseToAttributeEntryList(IAttribute[] attrArr, int classId) throws Exception {
		ArrayList<AttributeEntry> attrLst = new ArrayList<AttributeEntry>();
		AttributeEntry attrentry = null;
		DBHandler dbh = new DBHandler();
		ArrayList<Integer> rs = new ArrayList<Integer>();
		rs = dbh.checkAssistText(classId);
		for (IAttribute attr : attrArr) {
			if (attr.isVisible()) {
				attrentry = new AttributeEntry();
				// String attrId = attr.getId().toString();
				attrentry.setClassID(String.valueOf(classId));
				attrentry.setAttrID(attr.getId().toString());
				attrentry.setAttrName(attr.getFullName());
				if (rs.contains(attr.getId())) {
					/*
					 * System.out.println(String.valueOf(classId));
					 * System.out.println(attr.getId().toString());
					 * System.out.println(attr.getFullName());
					 */
					attrentry.sethasTextFlag(Constants.General.TextFlagSet);
				} else {
					attrentry.sethasTextFlag(Constants.General.TextFlagNotSet);
				}
				try {
					IProperty descProp = attr.getProperty(PropertyConstants.PROP_DESCRIPTION);
					if ((descProp != null) && (descProp.getValue() != null)) {
						attrentry.setAttrDescription((descProp.getValue().toString().equals("") ? " " : descProp.getValue().toString()));
					} else {
						attrentry.setAttrDescription("");
					}
				} catch (Exception e) {
					// TODO: handle exception
					attrentry.setAttrDescription("");
				}
				attrLst.add(attrentry);
			}
		}
		return attrLst;
	}

	public static List<AssistTextEntry> getAssistTextList(String classId, String attrId) throws Exception {
		try {
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
			Collection agileRoles = AgileHandler.getAllRoles(session);
			roleList = new ArrayList<RoleEntry>();
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
			RoleEntry roleEntry = new RoleEntry();
			roleEntry.setRoleID("0");
			roleEntry.setRole("All Roles");
			if (assistRoles.containsKey("All Roles")) {
				roleEntry.setPriority(Integer.parseInt(assistRoles.get("All Roles").get(0)));
				roleEntry.setFontColor(assistRoles.get("All Roles").get(1));
				roleEntry.setBackgroundColor(assistRoles.get("All Roles").get(2));
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
			throw e;
		} finally {
			AgileHandler.disconnect(session);
		}
		return roleList;
	}

	public static HashMap<String, ConfigEntry> getConfigEntries() throws Exception {
		HashMap<String, ConfigEntry> configArr = null;
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, String> configMap = dbh.readConfigurations();
			ConfigEntry ce = null;
			configArr = new HashMap<String, ConfigEntry>();
			for (String key : configMap.keySet()) {
				if (!key.equalsIgnoreCase("LNFO")) {
					ce = new ConfigEntry();
					ce.setKey(key);
					ce.setValue(configMap.get(key));
					ce.setId(key.replaceAll(" ", ""));
					if (key.equalsIgnoreCase("AgilePassword")) {
						ce.setType("password");
					} else {
						ce.setType("text");
					}
					configArr.put(ce.getKey(), ce);
				}
			}
			return configArr;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		// return configArr;
	}
}
