package com.xavor.plmxl.assist.Handler;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.agile.api.APIException;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileList;
import com.agile.api.IAttribute;
import com.agile.api.IRoutableDesc;
import com.agile.api.IStatus;
import com.agile.api.IWorkflow;
import com.agile.api.ItemConstants;
import com.thoughtworks.xstream.XStream;
import com.xavor.plmxl.assist.DO.AssistAttributeEntry;
import com.xavor.plmxl.assist.DO.AssistClassEntry;
import com.xavor.plmxl.assist.DO.AssistClassNotificationEntry;
import com.xavor.plmxl.assist.DO.AssistColorEntry;
import com.xavor.plmxl.assist.DO.AssistNotifConfigEntry;
import com.xavor.plmxl.assist.DO.AssistNotificationEntry;
import com.xavor.plmxl.assist.DO.AssistText;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.DO.WorkflowRolesList;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class XMLHandler {
	
	Writer  fw=null;
	AssistLogger log=AssistLogger.getInstance();
	DBHandler dbh=null;
	
	private final String ITEMS_WFVALUE = "Lifecycles";
	private final String ALL_STATUS = "All Statuses";
	private final String ALL_WORKFLOWS = "All Workflows";
	
	ArrayList<HashMap<String, Object>> insertedEntries=new ArrayList<HashMap<String,Object>>();
	ArrayList<HashMap<String, Object>> updatedEntries=new ArrayList<HashMap<String,Object>>();
	ArrayList<HashMap<String, Object>> deletedEntries=new ArrayList<HashMap<String,Object>>();
	ArrayList<HashMap<String, Object>> failedEntries=new ArrayList<HashMap<String,Object>>();
	
	List<String> roleIDList=null;
	List<String> colorIDList=null;
	
	HashMap<String,List<WorkflowRolesList>> xmlAssistTextNew=null;
	
	HashMap<String, List<WorkflowRolesList>> dbAssistTextNew=null;
	HashMap<String, AssistTextEntry> dbAssistTextMap=null;
	
	HashMap<String, List<WorkflowRolesList>> newDBAssistTextMapNew=null;
	
	HashMap<String,AssistTextEntry> xmlAssistTextMap=null;
		
	HashMap<String,String> dbAssistTextMapTexttID=null;
	
	HashMap<String, AssistTextEntry> oldDbAssistTextMap=null;
	List <AssistTextEntry> NewdbTextList=null;
	
	HashMap<String,AssistTextEntry> insertText= null;
	HashMap<String,AssistTextEntry> updateText= null;
	
	List<String> xmlColorIds=null;
	List<String> xmlRoleIds=null;
	
	List<AssistColorEntry> AssistColorList=null;
	List<RoleEntry> RolePriorityList=null;
	AssistNotificationEntry assistNotification = null;
	List<AssistClassNotificationEntry> assistClassNotificationList = null;

	AssistNotifConfigEntry assistConfig = null;
	
	boolean syncFlag=true;
	private void initExport( boolean isRoles) throws Exception
	{
		log.debug("Entering init....");

		Properties prop = ConfigHelper.loadPropertyFile();
	
		String abspath = ConfigHelper.getAppHomePath();
		fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(abspath+Constants.XML.XMLFileName), "UTF-8"));
	//	fw = new FileWriter(abspath+Constants.XML.XMLFileName);
		fw.append(Constants.XML.XmlVersion);
		fw.append(System.getProperty("line.separator"));
		
		if(isRoles)
			fw.append(Constants.XML.StartingTagRole+prop.getProperty("version")+Constants.XML.EndAttrTag);
		else
			fw.append(Constants.XML.StartingTagGroup+prop.getProperty("version")+Constants.XML.EndAttrTag);
		
		fw.append(System.getProperty("line.separator"));
		log.debug("Exiting init....");

	}
	
	private void setAssistColor() throws Exception 
	{
		log.debug("Entering setAssistColor....");

		HashMap<String,List<AssistColorEntry>> color= (HashMap<String, List<AssistColorEntry>>) dbh.handleDBRequest("getAssistColor", null,false);
		List<AssistColorEntry> assistColor=color.get("assistColor");
		
		XStream xstream = new XStream();
		xstream.alias("LabelColor", AssistColorEntry.class);
		xstream.alias("LabelColors", List.class);
		String xml = xstream.toXML(assistColor);
		fw.append(xml);
		
		log.debug("Exiting setAssistColor....");

	}

	
	private void setRolePriority( boolean isRoles) throws Exception
	{
		log.debug("Entering setRolePriority....");

		HashMap<String,List<RoleEntry>> roles= (HashMap<String, List<RoleEntry>>) dbh.handleDBRequest("getRolePriority", null,false);
		List<RoleEntry> rolePriority=roles.get("rolePriority");
		
		XStream xstream = new XStream();
		if(isRoles)
		{
			xstream.alias("RolePriority", RoleEntry.class);
		}
		else
		{
			xstream.alias("UserGroupPriority", RoleEntry.class);
		}
	    xstream.alias("Roles", List.class);
		String xml = xstream.toXML(rolePriority);
		fw.append(xml);		
		
		log.debug("Exiting setRolePriority....");
		
	}
	
	private HashMap<String, String> validateXML(String path) throws Exception
	{
		log.debug("Entering validateXML..");
		
	   	String abspath = ConfigHelper.getAppHomePath();
	   	System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
		SAXParserFactory fac = SAXParserFactory.newInstance(); 
		fac.setNamespaceAware(true);
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		
		dbh=new DBHandler();
		Map<String, Boolean> rolesStatus=(Map<String, Boolean>) dbh.handleDBRequest("isRoles", null, false);
		Boolean isRoles=(Boolean)rolesStatus.get("isRoles");
		if(isRoles)
		{
			log.debug("ROLES");
			log.debug("abs path: "+abspath+"role file: "+Constants.Config.XSD_ROLE);
			fac.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource(abspath+Constants.Config.XSD_ROLE)}));
		}
		else
		{
			log.debug("abs path: "+abspath+"group file: "+Constants.Config.XSD_GROUP);
			fac.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource(abspath+Constants.Config.XSD_GROUP)}));	
		}
		
		SAXParser parser = fac.newSAXParser();
		Handler h=new Handler();
		SAXReader reader = new SAXReader(parser.getXMLReader());
		reader.setValidation(false);
		reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		reader.setErrorHandler(h);
		reader.setFeature("http://xml.org/sax/features/validation", true);
		reader.setFeature("http://apache.org/xml/features/validation/schema", true);		
		h.setHasErrors(false);
		
		log.debug("Path:"+path);
		reader.read(new File(path));
		log.debug("Exiting validateXML..");
		String accessType="okay";
		boolean hasError=h.getHasErrors();

		log.debug(isRoles.toString());
		
		HashMap<String, String> statusMap=new HashMap<String,String>();
		if( hasError && !h.isAccessTypeOkay() )
		{
			if(isRoles && h.isRolePriority())
			{
				accessType="roles";
			}
			else
			{
				accessType="usergroups";
			}			
		}
		
		if(hasError)
		{
			statusMap.put("hasError","true" );
		}
		else
		{
			statusMap.put("hasError","false" );
		}
		
		statusMap.put("accessType", accessType);		


		
		return statusMap;		
	}
//	public HashMap<String,String> importXML( HashMap<String, String> params) throws Exception
//	{
//		log.debug("Entering importXML..");
//		String status = "false";
//		String rolesCheck="okay";
//		String hasError="false";
//		String accessType="okay";
//		String classesCheck="okay";
//		File fXmlFile=null;
//		HashMap<String, String> statusMap=new HashMap<String,String>();
//		try {
//			String version=null;
//			Properties prop = ConfigHelper.loadPropertyFile();			
//			HashMap<String, String> map=validateXML(params.get("path"));
//			hasError=map.get("hasError");
//			accessType=map.get("accessType");
//			
//
//			fXmlFile = new File(params.get("path"));
//			if(!hasError.equals("true"))
//			{
//				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); ///parser
//				factory.setValidating(false);
//				factory.setNamespaceAware(true);
//				DocumentBuilder builder = factory.newDocumentBuilder();
//				builder.setErrorHandler(new SAXErrorHandler());
//				Document doc = builder.parse(fXmlFile);
//				
//				log.debug("XML is Valid!");
//				Node currentNode = doc.getDocumentElement();
//				if (currentNode.getNodeType() == Node.ELEMENT_NODE) 
//				{
//					Element rootElement = (Element) currentNode;
//					version=prop.getProperty("version");
//					if(rootElement.getAttribute("version").equals(version))
//					{
//						if(params.get("type").equals("overWrite"))
//						{
//							overWrite(doc);
//							if(!syncFlag)
//							{
//								classesCheck="notokay";
//							}
//						}
//						else if(params.get("type").equals("merge"))
//						{
//							merge(doc);
//							if(!syncFlag)
//							{
//								classesCheck="notokay";
//							}
//						}
//						rolesCheck=checkRoles();
//						status="true";
//					}
//					else
//					{
//						status="false";
//					}
//				}						
//				fXmlFile.delete();
//			}
//			else
//			{
//				log.debug("Invlaid XML!");
//				fXmlFile.delete();
//				status="false";
//			}			
//
//			
//
//		} 
//		 catch (ParserConfigurationException e) {
//			status="false";
//			fXmlFile.delete();
//			log.error("ParserConfigurationException: ", e);
//		}
//		catch (SAXException e) {
//			status="false";
//			fXmlFile.delete();
//			log.error("SAXException: ", e);;
//		} catch (IOException e) {
//			status="false";
//			if(fXmlFile!=null)
//			{
//				fXmlFile.delete();
//			}
//			log.error("IOException: ", e);
//		}
//		catch (DocumentException e)
//		{
//			if(fXmlFile!=null)
//			{
//				fXmlFile.delete();
//			}
//			status="false";
//			hasError="true";
//			log.error("DocumentException: ", e);
//		}
//		catch (Exception e)
//		{
//			if(fXmlFile!=null)
//			{
//				fXmlFile.delete();
//			}
//			status="false";
//			log.error("Exception: ", e);			
//
//
//		}
//		statusMap.put("status", status);
//		statusMap.put("hasError",hasError);
//		statusMap.put("accessType", accessType);
//		statusMap.put("rolesCheck", rolesCheck);
//		statusMap.put("classesCheck", classesCheck); 
//		log.debug("Exiting importXML..");
//		return statusMap;
//	}
	
	private String checkRoles() throws Exception{
		
		List<String> agileRoles = new UIListHandler().getAllRoles();
		log.debug(agileRoles.toString());
		log.debug(" "+agileRoles.size());
		Map<String, List<String>> roles=(Map<String, List<String>>) dbh.handleDBRequest("getRoleIds", null, false);
		List<String> assistRoles=roles.get("assistRoles");
		log.debug(""+assistRoles.size());
		Collections.sort(assistRoles);
		for( String role:assistRoles)
		{
			log.debug(role);
			if(!agileRoles.contains(role))
			{
				return "false";
			}
		}		
		return "okay";		
	}
	private void merge(Document doc) throws Exception
	{
		log.debug("Entering merge..");		
		dbh=new DBHandler();
		
		mergeAssistNotification(doc);
		
		mergeAssistClassNotification(doc);
		
		mergeAssistText(doc);
		
		mergeAssistColor(doc);
		
		mergeRolePriority(doc);
		
		dbh.handleDBRequest("deleteUncheckedRoles", null,true);
		
		

		log.debug("Exiting merge..");
	}
	private void mergeAssistColor(Document doc) throws Exception
	{
		log.debug("Entering mergeAssistColor....");
		Map<String,List<String>> colorIds = (Map<String, List<String>>)dbh.handleDBRequest("getColorIDs", null,false);
		colorIDList=colorIds.get("colorIDList");
		
		Map<String,List<String>> colorAttributes = (Map<String, List<String>>)dbh.handleDBRequest("getColorAttributes", null,false);
		List<String> colorAttributesList = colorAttributes.get("colorAttributesList");
		
		xmlColorIds=new ArrayList<String>();
		getAssistColor(doc);	
		
		if(AssistColorList.size()!=0)
		{
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("color", AssistColorList);
			params.put("colorAttributesList", colorAttributesList);
			dbh.handleDBRequest("mergeAssistColor", params, true);
		}
		log.debug("Exiting mergeAssistColor....");

	}
	private void mergeRolePriority(Document doc) throws Exception
	{
		log.debug("Entering getRolePriority....");
		HashMap<String,List<String>> roleIds=(HashMap<String, List<String>>) dbh.handleDBRequest("getRoleIdList", null,false);
		roleIDList=roleIds.get("roleIDList");
		xmlRoleIds=new ArrayList<String>();
		
		getRolePriority(doc);
		
		if(RolePriorityList.size()!=0)
		{
			HashMap<String,Object> params=new HashMap<String,Object>();
			params.put("role", RolePriorityList);
			params.put("roleIDList", roleIDList);
			dbh.handleDBRequest("mergeRolePriority", params, true);
		}		
		log.debug("Exiting getRolePriority....");

	}
	
	private void mergeAssistNotification(Document doc) throws Exception {
		log.debug("Entering mergeAssistNotification....");	
		HashMap<String, Object> params = getAssistNotification(doc);
		if(!params.isEmpty()) {
			dbh.handleDBRequest("toggleNotificationMsg", params, true);
		}
		log.debug("Exiting mergeAssistNotification....");
	}

	private void mergeAssistClassNotification(Document doc) throws Exception {
		log.debug("Entering mergeAssistClassNotification....");	
		HashMap<String, List<AssistClassNotificationEntry>> params = getAssistClassNotification(doc);
		HashMap<String, Object> classParams = new HashMap<>();
		IAdmin admin = AgileHandler.getAgileSession().getAdminInstance();
		Map<String, AgileClassDO> agileClassesCache = new HashMap<>();
		for(AssistClassNotificationEntry assistClassNotification: params.get("assistClassNotificationList")) {
			String classId = assistClassNotification.getClassID();
			log.debug("Class ID: "+ classId );
		    String assistText = assistClassNotification.getAssistText();
		    String enableNotification = assistClassNotification.getNotifEnable();
		    String enableOverride =  assistClassNotification.getOverrideEnable();
		    String fontColor =  assistClassNotification.getFontColor();
		    String backgroundColor =  assistClassNotification.getBackgroundColor();
	
		    if(classId == null || assistText == null || enableNotification == null ||
		    		fontColor == null || backgroundColor == null || enableOverride == null || 
					classId.isEmpty() || assistText.isEmpty() || enableNotification.isEmpty() || 
					enableOverride.isEmpty() || fontColor.isEmpty() || backgroundColor.isEmpty() || assistClassNotification.getRoleList().isEmpty()) {
				
				log.error("Class Notification entry is missing required field values");
				continue;
			}
		    
			AgileClassDO aclassDO = null;
			if(agileClassesCache.containsKey(classId)) {
				aclassDO = agileClassesCache.get(classId);
			} else {
				IAgileClass agileClass = admin.getAgileClass(Integer.valueOf(classId));
				if(agileClass != null) {
					aclassDO = new AgileClassDO();
					aclassDO.agileClass = agileClass;
					aclassDO.classId = (Integer) agileClass.getId();			
					agileClassesCache.put(classId, aclassDO);
				} else {
					log.error("Agile Class not found: " + classId);
					continue;
				}
			}
			classParams.put("classID", classId);
			assistText = assistText.trim();
			if(!assistText.startsWith("<") || !assistText.endsWith(">")) {
				assistText = "<p>" + StringEscapeUtils.escapeHtml4(assistText).replaceAll("\\n", "</p><p>").replaceAll("<p>\\s*<\\/p>", "<p>&nbsp;</p>") + "</p>";
			}
			
			try {
				final byte[] utf8Bytes = assistText.getBytes("UTF-8");
				if(utf8Bytes.length > 2000) {
					continue;
				}
			} catch(Exception ex) {
				log.error("Error while getting Byte size of Assist Text: " + ex);
				if(assistText.length() > 1000) {
					continue;
				}
			}
			classParams.put("assistText", assistText);
			classParams.put("notifEnable", enableNotification);
			classParams.put("overrideEnable", enableOverride);
			classParams.put("fontColor",fontColor);
			classParams.put("backgroundColor", backgroundColor);
			classParams.put("roles", assistClassNotification.getRoleList()!=null? assistClassNotification.getRoleList().toArray(new String[0]): null);
			dbh.handleDBRequest("toggleClassNotificationMsg", classParams, true);
		}
		log.debug("Exiting mergeAssistClassNotification....");
	}

	
	@SuppressWarnings("unchecked")
	private void mergeAssistText(Document doc) throws Exception {
		log.debug("Entering mergeAssistText....");
		xmlAssistTextNew=new HashMap<String,List<WorkflowRolesList>>(); 
		xmlAssistTextMap=new HashMap<String,AssistTextEntry>(); 
		Map<String, AgileClassDO> agileClassesCache = new HashMap<String, AgileClassDO>();
		IAdmin admin = AgileHandler.getAgileSession().getAdminInstance();
		int errors = 0;
		//-------------New Import Logic
		List<AssistClassEntry> classes=parseXML(doc);
		int assistCount=0;
		for(int i=0; i<classes.size(); i++)
		{
			AssistClassEntry classEntry=classes.get(i);
			String classId=classEntry.getClassID();
			List<AssistAttributeEntry> attributes=classEntry.getAttributes();			
			for(int j=0; j<attributes.size(); j++)
			{
				AssistAttributeEntry attributeEntry=attributes.get(j);
				String attrId=attributeEntry.getAttrID();	
				List<AssistText> texts=attributeEntry.getTexts();
				for(int k=0; k<texts.size(); k++)
				{
					AssistText textEntry=texts.get(k);
					assistCount++;
					String workflow_lifecycle=textEntry.getWorkflow_lifecycle();
					List<String> textEntryStatuses=textEntry.getWorkflowStatuses();
					boolean emptyWorkFlowLifecycle=false;
					if(workflow_lifecycle==""|| workflow_lifecycle==null || workflow_lifecycle.isEmpty())
						emptyWorkFlowLifecycle=true;
					boolean keyExists=false;
					HashMap<String, String> keyParams=new HashMap<String, String>();
					keyParams.put("classId", classId);
					keyParams.put("attrId", attrId);
					keyParams.put("workflow_lifecycle", workflow_lifecycle);
					Map<?, ?> tempMap=(Map<?, ?>) dbh.handleDBRequest("isKeyExists", keyParams,false);
					keyExists=(Boolean) tempMap.get("keyExists");
					log.debug("Assist Text Entry Number: "+ assistCount);
					log.debug("Class ID: "+ classId );
					log.debug("Attribute ID: "+ attrId );
					
					//validating class, attribute, workflow id from agile
					 admin = AgileHandler.getAgileSession().getAdminInstance();
					AgileClassDO aclassDO = null;
					if(agileClassesCache.containsKey(classId)) {
						aclassDO = agileClassesCache.get(classId);
					} else {
						IAgileClass agileClass = admin.getAgileClass(Integer.valueOf(classId));
						if(agileClass != null) {
							aclassDO = new AgileClassDO();
							aclassDO.agileClass = agileClass;
							aclassDO.classId = (Integer) agileClass.getId();
							
							aclassDO.isItem = agileClass.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS);				
							if(aclassDO.isItem) {
								
							} else {
								aclassDO.isRoutable = (agileClass instanceof IRoutableDesc);
								if(aclassDO.isRoutable) {
									IWorkflow[] workflows = ((IRoutableDesc) agileClass).getWorkflows();
									for(IWorkflow wf : workflows) {
										String wfName = wf.getName();
										aclassDO.workflows.add(wfName);
										
										Set<String> statusNames = new HashSet<String>();
										IStatus[] statuses = wf.getStates();
										for(IStatus st : statuses) {
											statusNames.add(st.getName());
										}
										statusNames.add(ALL_STATUS);
										
										aclassDO.statuses.put(wfName, statusNames);
									}
									
									aclassDO.workflows.add(ALL_WORKFLOWS);
									Set<String> statusNames = new HashSet<String>();
									statusNames.add(ALL_STATUS);
									aclassDO.statuses.put(ALL_WORKFLOWS, statusNames);			
								}
							}
							
							agileClassesCache.put(classId, aclassDO);
						} else {
							log.error("Agile Class not found: " + classId);
							errors++;
							populateFailedEntries(textEntry, classId, attrId);
							continue;
						}
					}
				
					
					//Validating Workflow and Status
					if(aclassDO.isRoutable) {
						if(!aclassDO.workflows.contains(workflow_lifecycle)) {
							log.error("Workflow not found: " + workflow_lifecycle);
							errors++;
							populateFailedEntries(textEntry, classId, attrId);
							break;
						} else {
							Set<String> wfstatuses = aclassDO.statuses.get(workflow_lifecycle);
							if(workflow_lifecycle.equalsIgnoreCase("All workflows") && textEntryStatuses.get(0).isEmpty())
							{
								
							}
							else
							{
								String[] split = (String[]) textEntryStatuses.toArray();
								for(String st : split) {
									if(!wfstatuses.contains(st)) {
										log.error("Status not found: " + st);
										errors++;
										populateFailedEntries(textEntry, classId, attrId);
										break;
									}
							}
							}
						}
					}
					
					//Validate Attribute
					if(aclassDO.attributes.containsKey(attrId)) {
						attrId = aclassDO.attributes.get(attrId).toString();
					} else {
						IAttribute attr = aclassDO.agileClass.getAttribute(Integer.valueOf(attrId));
						if(attr != null) {
							aclassDO.attributes.put(attrId, Integer.valueOf(attrId));
						} else {
							log.error("Attribute not found: " + attrId);
							errors++;
							populateFailedEntries(textEntry, classId, attrId);
							continue;
						}
					}
					//validating assistText length
					String assistText = textEntry.getText();
					try {
						final byte[] utf8Bytes = assistText.getBytes("UTF-8");
						if(utf8Bytes.length > 4000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of size " + utf8Bytes.length + "Bytes. Maximum size is 4000 Bytes");
							errors++;
							populateFailedEntries(textEntry, classId, attrId);
							continue;
						}
					} catch(Exception ex) {
						log.error("Error while getting Byte size of Assist Text: " + ex);
						if(assistText.length() > 2000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of length exceeding the maximum length of 2000.");
							errors++;
							populateFailedEntries(textEntry, classId, attrId);
							continue;
						}
					}
					
					//
					Map<String, List<AssistText>> exEntries=(Map<String, List<AssistText>>) dbh.handleDBRequest("getExistingMatchingEntries", keyParams,false);
					List<AssistText> existingEntries=exEntries.get("existingEntries");
					
					boolean disjoint=false;
					boolean isStatusUpdated=false;
					boolean isRoleUpdated=false;
					List<String> RolesN$=new ArrayList<String>();
					List<String> statusN$=new ArrayList<String>();
					boolean hasNext=false;
					Iterator itr=existingEntries.iterator();
					while(itr.hasNext())
					{
						hasNext=true;
						disjoint=false;
						AssistText dbEntry=(AssistText) itr.next();
						List<String> RolesN=new ArrayList<String>();
						if(isRoleUpdated==false)
						{
							RolesN=textEntry.getRoles();
						}
						else
						{
							RolesN=RolesN$;
							
							
						}
						
						
						List<String> role1=dbEntry.getRoles();
						log.debug("Key: " + classId +";" + attrId+ ";"+workflow_lifecycle+ ";"+ RolesN +"; dbRoles: "+ role1);
						List<String> intersectionRoles=ListUtils.intersection(RolesN, role1);
						boolean isRoleSubset=false;
						try {
							isRoleSubset= RolesN.containsAll(role1);
						}
						catch (Exception e)
						{
							isRoleSubset=true;
						}
						//disjoint roles
						if(Collections.disjoint(RolesN, role1))
						{
							
							if((RolesN.isEmpty() || RolesN==null)&& (role1.isEmpty() || role1==null))
							{
								
							}
							else
							disjoint=true;
							hasNext=false;
							
						}
						//dbroles is a subset of new roles or dbroles are same as new roles
						else if(isRoleSubset)
						{
							List<String> statusN=new ArrayList<String>();
							if(isStatusUpdated==false) {
								statusN=textEntry.getWorkflowStatuses();
							}
							else
							{
								statusN=statusN$;
								if(statusN.isEmpty() || statusN==null)
								{
									break;
								}
							
							}
							
							List<String> status1=dbEntry.getWorkflowStatuses();
							List<String> intersectionStatuses=ListUtils.intersection(statusN, status1);
							boolean isStatusSubset=false;
							try {
								isStatusSubset= statusN.containsAll(status1);
							}
							catch (Exception e)
							{
								isStatusSubset=true;
							}
							if(Collections.disjoint(statusN, status1))
							{
								
								if((statusN.isEmpty() ||statusN==null) && (status1.isEmpty()||status1==null))
								{
									
								}
								else
								disjoint=false;
							}
							else if(isStatusSubset)
							{
								//update text
								HashMap<String, Object> updateEntry=new HashMap<String, Object>();
								updateEntry.put("text", textEntry);
								updateEntry.put("oldText", dbEntry);
								updateEntry.put("classId", classId);
								updateEntry.put("attrId", attrId);
								updateEntry.put("textId", textEntry.getTextID());
								updateEntry.put("workflow_lifecycle", workflow_lifecycle);
								String roles="";
								try {
								List<String> allRoles=textEntry.getRoles();
								roles=allRoles.get(0);
								for(int k1=1; k1<allRoles.size(); k1++)
									roles=roles+";"+allRoles.get(k1);
								}
								catch(Exception e)
								{
									roles="";
								}
								updateEntry.put("roles", roles);
								updateEntry.put("roleList", textEntry.getRoles());
								
								String statuses="";
								try {
									List<String> allStatuses=textEntry.getWorkflowStatuses();
									statuses=allStatuses.get(0);
									for(int k1=1; k1<allStatuses.size(); k1++)
										statuses=statuses+"; "+allStatuses.get(k1);
									}
									catch(Exception e)
									{
										statuses="";
									}
								
								updateEntry.put("statuses", statuses);
								
								Map<?, ?> isErrorExistsMap=(Map<?, ?>) dbh.handleDBRequest("updateEntry", updateEntry,true);
								boolean isErrorExists=(Boolean) isErrorExistsMap.get("isErrorExists");
								if(isErrorExists==false)
								{	
									updatedEntries.add(updateEntry);
									log.info("-------Entry updated in Database with following new Details-------");
									log.info("Attribute ID: " +attrId);
									log.info("Class ID: " + classId);
									log.info("Associated new Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
									log.info("Associated OLD Assist Text: " + StringEscapeUtils.unescapeXml(dbEntry.getText()));
									log.info("New Font Color: " + textEntry.getFontColor());
									log.info("OLD Font Color: " + dbEntry.getFontColor());
									log.info("New Background Color: " + textEntry.getBackgroundColor());
									log.info("OLD Background Color: " + dbEntry.getBackgroundColor());
									log.info("New Workflow: " + textEntry.getWorkflow_lifecycle());
									log.info("OLD Workflow: " + dbEntry.getWorkflow_lifecycle());
									log.info("New Workflow Status: " + textEntry.getWorkflowStatuses().toString());
									log.info("OLD Workflow Status: " + dbEntry.getWorkflowStatuses().toString());
									List<String> roles1=textEntry.getRoles();
									log.info("New Roles are as follows");
									for(int k1=0; k1<roles1.size(); k1++)
										log.info("--"+roles1.get(k1));
									List<String> oldRoles=dbEntry.getRoles();
									log.info("OLD Roles are as follows");
									for(int k1=0; k1<oldRoles.size(); k1++)
										log.info("--"+oldRoles.get(k1));
									}
									
								
								else if(isErrorExists==true)
									
								{	
									log.info("Update failed for entry having following details");
									log.info("Attribute ID: " +attrId);
									log.info("Class ID: " + classId);
									log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(dbEntry.getText()));
									log.info("Font Color: " + dbEntry.getFontColor());
									log.info("Background Color: " + dbEntry.getBackgroundColor());
									log.info("Workflow: " + dbEntry.getWorkflow_lifecycle());
									log.info("Workflow Status: " + dbEntry.getWorkflowStatuses().toString());
									List<String> roles1=dbEntry.getRoles();
									log.info("Roles are as follows");
									for(int k1=0; k1<roles1.size(); k1++)
										log.info("--"+roles1.get(k1));
								}
								
								//loop break
								break;
							}
							else if(statusN.size()>intersectionStatuses.size() || status1.size()>intersectionStatuses.size()) {
								//status1.removeall(intersectionStatuses) from db
								HashMap<String, Object> deleteEntry=new HashMap<String, Object>();
								deleteEntry.put("text", dbEntry);
								deleteEntry.put("classId", classId);
								deleteEntry.put("attrId", attrId);
								deleteEntry.put("statuses",intersectionStatuses);
								
								
								
								Iterator<String> it = status1.iterator();
								List<String> status1A=new ArrayList<String>();
								while (it.hasNext()) {
									String status = it.next();
									if(!intersectionStatuses.contains(status))
									status1A.add(status);
								   
								    }
								status1=new ArrayList<String>();
								status1.addAll(status1A);
								
								
								
								deleteEntry.put("newStatuses",status1);
								deleteEntry.put("workflow_lifecycle", workflow_lifecycle);
								dbh.handleDBRequest("deleteStatuses", deleteEntry, true);
								
								//new entry using intersection statuses
								HashMap<String, Object> insertEntry=new HashMap<String, Object>();
								AssistText tempTextEntry=new AssistText();
								tempTextEntry=textEntry;
								tempTextEntry.setWorkflowStatuses(intersectionStatuses);
								insertEntry.put("text", tempTextEntry);
								insertEntry.put("classId", classId);
								insertEntry.put("attrId", attrId);
								insertEntry.put("workflow_lifecycle", workflow_lifecycle);
								
								String roles="";
								try {
									List<String> allRoles=tempTextEntry.getRoles();
									roles=allRoles.get(0);
									for(int k1=1; k1<allRoles.size(); k1++)
										roles=roles+";"+allRoles.get(k1);
									}
								catch(Exception e)
									{
										roles="";
									}
								insertEntry.put("roles", roles);
								insertEntry.put("roleList", tempTextEntry.getRoles());
								
								String statuses="";
								try {
									List<String> allStatuses=tempTextEntry.getWorkflowStatuses();
									statuses=allStatuses.get(0);
									for(int k1=1; k1<allStatuses.size(); k1++)
										statuses=statuses+"; "+allStatuses.get(k1);
									}
									catch(Exception e)
									{
										statuses="";
									}
								insertEntry.put("statuses", statuses);	
								
								Map<?, ?> isErrorExistsMap=(Map<?, ?>)	dbh.handleDBRequest("insertNewEntry", insertEntry,true);
								boolean isErrorExists=(Boolean) isErrorExistsMap.get("isErrorExists");
								if(isErrorExists==false)
								{
									insertedEntries.add(insertEntry);
									log.info("-------New Entry added in Database with following Details-------");
									log.info("Attribute ID: " +attrId);
									log.info("Class ID: " + classId);
									log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
									log.info("Font Color: " + textEntry.getFontColor());
									log.info("Background Color: " + textEntry.getBackgroundColor());
									log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
									log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
									List<String> roles1=textEntry.getRoles();
									log.info("Roles are as follows");
									for(int k1=0; k1<roles1.size(); k1++)
										log.info("--"+roles1.get(k1));
									}
									else if(isErrorExists==true)
									{	
										log.info("New Entry failed having following details");
										log.info("Attribute ID: " +attrId);
										log.info("Class ID: " + classId);
										log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
										log.info("Font Color: " + textEntry.getFontColor());
										log.info("Background Color: " + textEntry.getBackgroundColor());
										log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
										log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
										List<String> roles1=textEntry.getRoles();
										log.info("Roles are as follows");
										for(int k1=0; k1<roles1.size(); k1++)
											log.info("--"+roles1.get(k1));
										}
								
								
								//statusn=statusn-intersection
								Iterator<String> it2 = statusN.iterator();
								ArrayList<String> statusNA=new ArrayList<String>();
								while (it2.hasNext()) {
									String status = it2.next();
								    if (!intersectionStatuses.contains(status)) {
								    	statusNA.add(status);
								    }
								}
								statusN=new ArrayList<String>();
								statusN.addAll(statusNA);
								statusN$=statusN;
								isStatusUpdated=true;
								if(statusN.isEmpty() || statusN==null) {
									disjoint=false;
									break;
								}
								else
									textEntry.setWorkflowStatuses(statusN$);
								
								hasNext=false;
					
								
								
								
								
							}
							hasNext=false;
						}
						//db roles and new roles have some elements common
						else if(RolesN.size()>intersectionRoles.size() || role1.size()>intersectionRoles.size())
						{
							List<String> statusN = new ArrayList<>();
							if(!isStatusUpdated) {
								statusN=textEntry.getWorkflowStatuses();
							}
							else
							{
								statusN=statusN$;
								if(statusN.isEmpty() || statusN==null)
								{
									break;
								}
							}
							
							List<String> status1=dbEntry.getWorkflowStatuses();
							List<String> intersectionStatuses=ListUtils.intersection(statusN, status1);
							boolean isStatusSubset=false;
							try {
								isStatusSubset= statusN.containsAll(status1);
							}
							catch (Exception e)
							{
								isStatusSubset=true;
							}
							if(Collections.disjoint(statusN, status1))
							{
								if((statusN.isEmpty() ||statusN==null) && (status1.isEmpty()||status1==null))
								{
									
								}
								else
								
								disjoint=true;
							}
							else if(isStatusSubset)
							{
								//delete intersectionroles from db
								HashMap<String, Object> deleteEntry=new HashMap<String, Object>();
								deleteEntry.put("text", dbEntry);
								deleteEntry.put("classId", classId);
								deleteEntry.put("attrId", attrId);
								deleteEntry.put("roles",intersectionRoles);
								deleteEntry.put("workflow_lifecycle", workflow_lifecycle);
								dbh.handleDBRequest("deleteRoles", deleteEntry, true);
								
								//create new entry using intersection roles
								HashMap<String, Object> insertEntry=new HashMap<String, Object>();
								AssistText tempTextEntry=new AssistText();
								tempTextEntry=textEntry;
								tempTextEntry.setRoles(intersectionRoles);
								insertEntry.put("text", tempTextEntry);
								insertEntry.put("classId", classId);
								insertEntry.put("attrId", attrId);
								insertEntry.put("workflow_lifecycle", workflow_lifecycle);
								
								String roles="";
								try {
									List<String> allRoles=tempTextEntry.getRoles();
									roles=allRoles.get(0);
									for(int k1=1; k1<allRoles.size(); k1++)
										roles=roles+";"+allRoles.get(k1);
									}
								catch(Exception e)
									{
										roles="";
									}
								insertEntry.put("roles", roles);
								insertEntry.put("roleList", tempTextEntry.getRoles());
								
								
								String statuses="";
								try {
									List<String> allStatuses=tempTextEntry.getWorkflowStatuses();
									statuses=allStatuses.get(0);
									for(int k1=1; k1<allStatuses.size(); k1++)
										statuses=statuses+"; "+allStatuses.get(k1);
									}
									catch(Exception e)
									{
										statuses="";
									}
								insertEntry.put("statuses", statuses);	
								
								
								Map<?, ?> isErrorExistsMap=(Map<?, ?>)	dbh.handleDBRequest("insertNewEntry", insertEntry,true);
								boolean isErrorExists=(Boolean) isErrorExistsMap.get("isErrorExists");
								if(isErrorExists==false)
								{
									insertedEntries.add(insertEntry);
									log.info("-------New Entry added in Database with following Details-------");
									log.info("Attribute ID: " +attrId);
									log.info("Class ID: " + classId);
									log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
									log.info("Font Color: " + textEntry.getFontColor());
									log.info("Background Color: " + textEntry.getBackgroundColor());
									log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
									log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
									List<String> roles1=textEntry.getRoles();
									log.info("Roles are as follows");
									for(int k1=0; k1<roles1.size(); k1++)
										log.info("--"+roles1.get(k1));
									}
									
								if(isErrorExists==true)
									{
										
										log.info("New Entry failed having following details");
										log.info("Attribute ID: " +attrId);
										log.info("Class ID: " + classId);
										log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
										log.info("Font Color: " + textEntry.getFontColor());
										log.info("Background Color: " + textEntry.getBackgroundColor());
										log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
										log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
										List<String> roles1=textEntry.getRoles();
										log.info("Roles are as follows");
										for(int k1=0; k1<roles1.size(); k1++)
											log.info("--"+roles1.get(k1));
										}
								
								
								//rolen=rolen-intersectionroles
								for(int x=0; x<intersectionRoles.size(); x++)
								{
									RolesN.remove(intersectionRoles.get(x));
								}
								isRoleUpdated=true;
								RolesN$=RolesN;
								if(RolesN.isEmpty() || RolesN==null) {
									disjoint=false;
									break;
								}
								else
									textEntry.setRoles(RolesN$);
								
								
							}
							else if(statusN.size()>intersectionStatuses.size()) {
								//status1.removeall(intersectionStatuses) from db
								HashMap<String, Object> deleteEntry=new HashMap<String, Object>();
								deleteEntry.put("text", dbEntry);
								deleteEntry.put("classId", classId);
								deleteEntry.put("attrId", attrId);
								deleteEntry.put("statuses",intersectionStatuses);
								
								
								
								Iterator<String> it = status1.iterator();
								List<String> status1A=new ArrayList<>();
								while (it.hasNext()) {
									String status = it.next();
									if(!intersectionStatuses.contains(status))
									status1A.add(status);
								   
								    }
								status1=new ArrayList<>();
								status1.addAll(status1A);
								
								
								
								deleteEntry.put("newStatuses",status1);
								deleteEntry.put("workflow_lifecycle", workflow_lifecycle);
								dbh.handleDBRequest("deleteStatuses", deleteEntry, true);
								
								//new entry using intersection statuses
								HashMap<String, Object> insertEntry=new HashMap<String, Object>();
								AssistText tempTextEntry=new AssistText();
								tempTextEntry=textEntry;
								tempTextEntry.setWorkflowStatuses(intersectionStatuses);
								insertEntry.put("text", tempTextEntry);
								insertEntry.put("classId", classId);
								insertEntry.put("attrId", attrId);
								insertEntry.put("workflow_lifecycle", workflow_lifecycle);
								
								String roles="";
								try {
									List<String> allRoles=tempTextEntry.getRoles();
									roles=allRoles.get(0);
									for(int k1=1; k1<allRoles.size(); k1++)
										roles=roles+";"+allRoles.get(k1);
									}
								catch(Exception e)
									{
										roles="";
									}
								insertEntry.put("roles", roles);
								insertEntry.put("roleList", tempTextEntry.getRoles());
								
								String statuses="";
								try {
									List<String> allStatuses=tempTextEntry.getWorkflowStatuses();
									statuses=allStatuses.get(0);
									for(int k1=1; k1<allStatuses.size(); k1++)
										statuses=statuses+"; "+allStatuses.get(k1);
									}
									catch(Exception e)
									{
										statuses="";
									}
								insertEntry.put("statuses", statuses);	
								
								
								Map<?, ?> isErrorExistsMap=(Map<?, ?>)	dbh.handleDBRequest("insertNewEntry", insertEntry,true);
								boolean isErrorExists=(Boolean) isErrorExistsMap.get("isErrorExists");
								if(isErrorExists==false)
								{
									insertedEntries.add(insertEntry);
									log.info("-------New Entry added in Database with following Details-------");
									log.info("Attribute ID: " +attrId);
									log.info("Class ID: " + classId);
									log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
									log.info("Font Color: " + textEntry.getFontColor());
									log.info("Background Color: " + textEntry.getBackgroundColor());
									log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
									log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
									List<String> roles1=textEntry.getRoles();
									log.info("Roles are as follows");
									for(int k1=0; k1<roles1.size(); k1++)
										log.info("--"+roles1.get(k1));
									}
									
								if(isErrorExists==true){
										
										log.info("New Entry failed having following details");
										log.info("Attribute ID: " +attrId);
										log.info("Class ID: " + classId);
										log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
										log.info("Font Color: " + textEntry.getFontColor());
										log.info("Background Color: " + textEntry.getBackgroundColor());
										log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
										log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
										List<String> roles1=textEntry.getRoles();
										log.info("Roles are as follows");
										for(int k1=0; k1<roles1.size(); k1++)
											log.info("--"+roles1.get(k1));
										}
								
								
								//statusn=statusn-intersection
								Iterator<String> it2 = statusN.iterator();
								ArrayList<String> statusNA=new ArrayList<String>();
								while (it2.hasNext()) {
									String status = it2.next();
								    if (!intersectionStatuses.contains(status)) {
								    	statusNA.add(status);
								    }
								}
								statusN=new ArrayList<String>();
								statusN.addAll(statusNA);
								statusN$=statusN;
								isStatusUpdated=true;
								if(statusN.isEmpty() || statusN==null) {
									disjoint=false;
									break;
								}
								else
									textEntry.setWorkflowStatuses(statusN$);
							}
							hasNext=false;
						}
						
					}
					
					
					if(disjoint==true || hasNext==false) {
					
					HashMap<String, Object> insertEntry=new HashMap<String, Object>();
					insertEntry.put("text", textEntry);
					insertEntry.put("classId", classId);
					insertEntry.put("attrId", attrId);
					insertEntry.put("workflow_lifecycle", workflow_lifecycle);
					
					String roles="";
					try {
						List<String> allRoles=textEntry.getRoles();
						roles=allRoles.get(0);
						for(int k1=1; k1<allRoles.size(); k1++)
							roles=roles+";"+allRoles.get(k1);
						}
					catch(Exception e)
						{
							roles="";
						}
					insertEntry.put("roles", roles);
					insertEntry.put("roleList", textEntry.getRoles());
					
					String statuses="";
					try {
						List<String> allStatuses=textEntry.getWorkflowStatuses();
						statuses=allStatuses.get(0);
						for(int k1=1; k1<allStatuses.size(); k1++)
							statuses=statuses+"; "+allStatuses.get(k1);
						}
						catch(Exception e)
						{
							statuses="";
						}
					insertEntry.put("statuses", statuses);	
					
					
					Map<?, ?> isErrorExistsMap=(Map<?, ?>)	dbh.handleDBRequest("insertNewEntry", insertEntry,true);
					boolean isErrorExists=(Boolean) isErrorExistsMap.get("isErrorExists");
					if(isErrorExists==false)
					{
						insertedEntries.add(insertEntry);
						log.info("-------New Entry added in Database with following Details-------");
						log.info("Attribute ID: " +attrId);
						log.info("Class ID: " + classId);
						log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
						log.info("Font Color: " + textEntry.getFontColor());
						log.info("Background Color: " + textEntry.getBackgroundColor());
						log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
						log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
						List<String> roles1=textEntry.getRoles();
						log.info("Roles are as follows");
						for(int k1=0; k1<roles1.size(); k1++)
							log.info("--"+roles1.get(k1));
						}
						
					if(isErrorExists==true){
							
							log.info("New Entry failed having following details");
							log.info("Attribute ID: " +attrId);
							log.info("Class ID: " + classId);
							log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(textEntry.getText()));
							log.info("Font Color: " + textEntry.getFontColor());
							log.info("Background Color: " + textEntry.getBackgroundColor());
							log.info("Workflow: " + textEntry.getWorkflow_lifecycle());
							log.info("Workflow Status: " + textEntry.getWorkflowStatuses().toString());
							List<String> roles1=textEntry.getRoles();
							log.info("Roles are as follows");
							for(int k1=0; k1<roles1.size(); k1++)
								log.info("--"+roles1.get(k1));
							}
					
					
					log.info("entry added for classID: " + classId +"attr ID: "+attrId );
					}
					
				}
			}
		}
		//-------------//
		
		//getAssistText(doc);	
		
		
		HashMap<String,List<String>> textRoles = (HashMap<String, List<String>>) dbh.handleDBRequest("getTextRoles", null,false);
		HashMap<String,List<AssistTextEntry>> dbAssistTextList = (HashMap<String, List<AssistTextEntry>>) dbh.handleDBRequest("getAssistTextMap", null, false);
		
		List<AssistTextEntry> dbTextList= dbAssistTextList.get("textList");
		
		dbAssistTextMap=new HashMap<String, AssistTextEntry>();
		dbAssistTextNew=new HashMap<String,List<WorkflowRolesList>>();
		

		
		
		
		dbAssistTextMapTexttID=new HashMap<String,String>();
		oldDbAssistTextMap=new HashMap<String, AssistTextEntry>();
		
		for(int i=0;i<dbTextList.size();i++) {
			AssistTextEntry assistEntry = dbTextList.get(i);
			List<String> roles = textRoles.get(assistEntry.getTextID());
			Collections.sort(roles);
			dbTextList.get(i).setRolesList(roles);
			
		}
		
		
		
		
		
		for(int i=0;i<dbTextList.size();i++) {
			AssistTextEntry assistEntry = dbTextList.get(i);
			String key = assistEntry.getClassID() + ":" + assistEntry.getAttrID() + ":";
			if(assistEntry.getWorkflowID().trim().length() == 0) {
				key += "All Workflows";
			} else {
				key += assistEntry.getWorkflowID();
			}
			
			String key1 = key;
			
			key += ":" + assistEntry.getWorkflowStatusId() + ":";
			
			List<String> roles = textRoles.get(assistEntry.getTextID());
			Collections.sort(roles);			
			for(int j=0; j<roles.size(); j++) {
				key += roles.get(j) + ":";
			} 
			key = key.substring(0, key.length()-1);
			
			log.debug("key DB"+key);
			dbAssistTextMap.put(key, assistEntry);
			dbAssistTextMapTexttID.put(key, assistEntry.getTextID());
			
			List<String> wsid = new ArrayList<String>();			
			String[] split = assistEntry.getWorkflowStatusId().split(";");			
			for(int j=0; j<split.length; j++) {
				wsid.add(split[j]);
			}
			
			AssistTextEntry textValue = new AssistTextEntry();
			textValue.setTextID(assistEntry.getTextID());
			
			WorkflowRolesList wrList = new WorkflowRolesList();			
			wrList.wsidList = wsid;	
			wrList.rolesList = roles;				
			wrList.text = textValue;
		
			if(dbAssistTextNew.containsKey(key1)) {
				dbAssistTextNew.get(key1).add(wrList);
			} else {				
				List<WorkflowRolesList> workflowList = new ArrayList<WorkflowRolesList>();
				workflowList.add(wrList);
				dbAssistTextNew.put(key1, workflowList);
			}
		}	
		log.debug("Exiting mergeAssistText....");			
	}

	private void populateFailedEntries(AssistText textEntry, String classId, String attrId)
	{
		HashMap<String, Object> failedEntry=new HashMap<String, Object>();
		failedEntry.put("text", textEntry);
		failedEntry.put("classId", classId);
		failedEntry.put("attrId", attrId);
		failedEntry.put("workflow_lifecycle", textEntry.getWorkflow_lifecycle());
		
		String roles="";
		try {
			List<String> allRoles=textEntry.getRoles();
			roles=allRoles.get(0);
			for(int k1=1; k1<allRoles.size(); k1++)
				roles=roles+";"+allRoles.get(k1);
			}
		catch(Exception e)
			{
				roles="";
			}
		failedEntry.put("roles", roles);
		failedEntry.put("roleList", textEntry.getRoles());
		
		String statuses="";
		try {
			List<String> allStatuses=textEntry.getWorkflowStatuses();
			statuses=allStatuses.get(0);
			for(int k1=1; k1<allStatuses.size(); k1++)
				statuses=statuses+"; "+allStatuses.get(k1);
			}
			catch(Exception e)
			{
				statuses="";
			}
		failedEntry.put("statuses", statuses);	
		failedEntries.add(failedEntry);
	}
	
	
	
	private void insertUpdateText()
	{
		log.debug("Entering insertUpdateText....");
		insertText= new HashMap<String,AssistTextEntry>();
		updateText= new HashMap<String,AssistTextEntry>();
		List<String> existingKeys=new ArrayList<String>();
		int insertKey=0;
		
		for( String key:xmlAssistTextMap.keySet() )
		{
			if(dbAssistTextMapTexttID.containsKey(key))
			{
				AssistTextEntry text=xmlAssistTextMap.get(key);
				updateText.put(dbAssistTextMapTexttID.get(key), text);
				log.debug("true");
			}
			else
			{
				log.debug("Key : "+key);
				String split[]=key.split(":");
				String newKey="";
				
				if(split.length>3)
				{
					for(int count=0;count<3;count++)
					{
						newKey+=split[count]+":";
					}
					newKey=newKey.substring(0,newKey.length()-1);
				}
				if(!existingKeys.contains(newKey))
				{
					existingKeys.add(newKey);
					log.debug("newKey : "+newKey);
					if(dbAssistTextNew.containsKey(newKey))
					{
						List<String> allWsids=new ArrayList<String>();
						List<String> allRoles=new ArrayList<String>();
						
						for(int i=0;i<dbAssistTextNew.get(newKey).size();i++)
						{
							allRoles=ListUtils.union(allRoles, dbAssistTextNew.get(newKey).get(i).rolesList);
							allWsids=ListUtils.union(allWsids, dbAssistTextNew.get(newKey).get(i).wsidList);
						}
						log.debug(allRoles.toString());
						log.debug(allWsids.toString());
						boolean partialMatchWorkflow=false;
						boolean partialMatchRoles=false;
						boolean matchRoles=false;
						boolean sameWsid=false;
						for(int i=0;i<xmlAssistTextNew.get(newKey).size();i++)
						{
							partialMatchRoles=false;
							partialMatchWorkflow=false;
							sameWsid=false;
							matchRoles=false;
							for(int j=0;j<dbAssistTextNew.get(newKey).size() ;j++)
							{
								List<String> intersectionWorkflow=ListUtils.intersection(xmlAssistTextNew.get(newKey).get(i).wsidList, dbAssistTextNew.get(newKey).get(j).wsidList);
								log.debug(" w size"+xmlAssistTextNew.get(newKey).get(i).wsidList.size());log.debug("r size"+dbAssistTextNew.get(newKey).get(j).wsidList.size());log.debug("i size"+intersectionWorkflow.size());
								if(intersectionWorkflow.size()==xmlAssistTextNew.get(newKey).get(i).wsidList.size() && intersectionWorkflow.size()==dbAssistTextNew.get(newKey).get(j).wsidList.size())
								{
									//same
									sameWsid=true;
									List<String> intersectionRoles=ListUtils.intersection(xmlAssistTextNew.get(newKey).get(i).rolesList, dbAssistTextNew.get(newKey).get(j).rolesList);
									if(intersectionRoles.size()!=0)
									{
										matchRoles=true;
									}
								}							
								else if(intersectionWorkflow.size()!=0)
								{									//partial match
									partialMatchWorkflow=true;	
									List<String> intersectionRoles=ListUtils.intersection(xmlAssistTextNew.get(newKey).get(i).rolesList, dbAssistTextNew.get(newKey).get(j).rolesList);
									if(intersectionRoles.size()!=0) //partial match
									{
										partialMatchRoles=true;
									}
								}									
							}
							log.debug("Boolean: "+sameWsid+","+matchRoles+","+partialMatchWorkflow+","+partialMatchRoles);
							if(sameWsid && !matchRoles)
							{
								List<String> intersectionAllRoles=ListUtils.intersection(xmlAssistTextNew.get(newKey).get(i).rolesList,allRoles);
								if(intersectionAllRoles.size()==0) //distinct roles
								{
									// NEW ENTRY
									AssistTextEntry text=xmlAssistTextNew.get(newKey).get(i).text;
									log.debug(xmlAssistTextNew.get(newKey).get(i).rolesList.toString());
								
									insertText.put(String.valueOf(insertKey),text );
									insertKey++;
									log.debug("New Entry");	
								}
								
							}
							else if(!partialMatchWorkflow)
							{
								//distinct WorkflowStatuses
								List<String> intersectionAllWorkflows=ListUtils.intersection(xmlAssistTextNew.get(newKey).get(i).wsidList,allWsids);							
								if(intersectionAllWorkflows.size()==0) //distinct workflowStatuses
								{
									// NEW ENTRY
									AssistTextEntry text=xmlAssistTextNew.get(newKey).get(i).text;
									log.debug(xmlAssistTextNew.get(newKey).get(i).wsidList.toString());
								
									insertText.put(String.valueOf(insertKey),text );
									insertKey++;
									log.debug("New Entry");		
							
								}
							}
							else if(partialMatchWorkflow && !partialMatchRoles)
							{ 
								List<String> intersectionAllRoles=ListUtils.intersection(xmlAssistTextNew.get(newKey).get(i).rolesList,allRoles);
								if(intersectionAllRoles.size()==0) //distinct roles
								{
									// NEW ENTRY
									AssistTextEntry text=xmlAssistTextNew.get(newKey).get(i).text;
									log.debug(xmlAssistTextNew.get(newKey).get(i).rolesList.toString());
								
									insertText.put(String.valueOf(insertKey),text );
									insertKey++;
									log.debug("New Entry");	
								}
								
							}
							
						}
					}
					else
					{
						// NEW ENTRY
						for(int i=0; i<xmlAssistTextNew.get(newKey).size();i++)
						{
							
							
							AssistTextEntry text=xmlAssistTextNew.get(newKey).get(i).text;
							insertText.put(String.valueOf(insertKey),text );
							insertKey++;
							log.debug("New Entry");
						}
						
					}					
					
				}
			}
		}
	
		if(updateText.size()!=0)
		{
			HashMap<String,Object> params=new HashMap<String,Object>();
			params.put("updateText", updateText);
			dbh.handleDBRequest("mergeUpdateText", params,true);
		}
		if(insertText.size()!=0)
		{
			HashMap<String,Object> params=new HashMap<String,Object>();
			params.put("insertText", insertText);
			dbh.handleDBRequest("mergeInsertText", params, true);
		}
		
		
		log.debug("Exiting insertUpdateText....");
	}
	
	
	
	
	private void getAssistColor(Document doc) throws Exception
	{
		log.debug("Entering getAssistColor....");
		Node currentTable =doc.getElementsByTagName(Constants.XML.TableAssitColor).item(0);
		StringWriter writer = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();	
		
		XStream xstream = new XStream();
		xstream.alias("LabelColor", AssistColorEntry.class);
		xstream.alias("LabelColors", List.class);
		
		AssistColorList =  (List<AssistColorEntry>) xstream.fromXML(xml);
	    log.debug("colorList size : "+AssistColorList.size());

		for(int j=0;j<AssistColorList.size();j++)
		{
			xmlColorIds.add(AssistColorList.get(j).getColorId());
		}		
		log.debug("Exiting getAssistColor....");

	}
	
	private void getRolePriority(Document doc) throws Exception
	{
		log.debug("Entering getRolePriority....");
		Node currentTable =doc.getElementsByTagName(Constants.XML.TableRolePriority).item(0);
		
		StringWriter writer = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();	
		
		Map<String, Boolean> rolesStatus = (Map<String, Boolean>) dbh.handleDBRequest("isRoles", null, false);
		Boolean isRoles = rolesStatus.get("isRoles");
		XStream xstream = new XStream();
		if(Boolean.TRUE.equals(isRoles)){
			xstream.alias("RolePriority", RoleEntry.class);
		}
		else {
			xstream.alias("UserGroupPriority", RoleEntry.class);
		}
	    xstream.alias("Roles", List.class);
	    RolePriorityList =  (List<RoleEntry>) xstream.fromXML(xml);

	    log.debug("role size : "+RolePriorityList.size());
	    for(int j=0;j<RolePriorityList.size();j++)
	    {
	    	xmlRoleIds.add(RolePriorityList.get(j).getRoleID().toString());
	    }	
		log.debug("Exiting getRolePriority....");

	}

	private AssistNotifConfigEntry getAssistConfiguration(Document doc) throws Exception
	{
		log.debug("Entering getAssistConfiguration....");
		Node currentTable =doc.getElementsByTagName(Constants.XML.TableAssistConfiguration).item(0);
		
		StringWriter writer = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();	
		
		XStream xstream = new XStream();
		xstream.alias("AssistConfiguration", AssistNotifConfigEntry.class);	
		assistConfig = (AssistNotifConfigEntry) xstream.fromXML(xml);	  
		log.debug("Exiting getAssistConfiguration....");
		return assistConfig;
	}

	
	private HashMap<String, Object> getAssistNotification(Document doc) throws Exception
	{
		log.debug("Entering getAssistNotification....");
		Node currentTable =doc.getElementsByTagName(Constants.XML.TableAssistNotification).item(0);
		HashMap<String, Object> params = new HashMap<>();
		StringWriter writer = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();	
		
		XStream xstream = new XStream();
		xstream.alias("AssistNotification", AssistNotificationEntry.class);	
	    xstream.alias("RoleID", String.class);	    
	    assistNotification = (AssistNotificationEntry) xstream.fromXML(xml);
	    
	    List<String> configs = new ArrayList<>();
	    List<String> roles = new ArrayList<>();
	    AssistNotifConfigEntry notifConfig = getAssistConfiguration(doc);
	    
	    String enableOptOut = notifConfig.getOptOutEnable();
	    String enableNotification = notifConfig.getNotifEnable();
	    String assistText = assistNotification.getAssistMessage();
	    String enableDuration = assistNotification.getDurationEnable();
	    String durationLimit =  assistNotification.getDurationLimit();
	    String fontColor =  assistNotification.getFontColor();
	    String backgroundColor =  assistNotification.getBackgroundColor();
	    
	    if(enableOptOut == null || assistText == null || enableNotification == null || enableDuration == null ||
				assistNotification.getFontColor() == null || assistNotification.getBackgroundColor() == null || durationLimit == null || 
				enableOptOut.isEmpty() || assistText.isEmpty() || enableNotification.isEmpty() || enableDuration.isEmpty() || 
				durationLimit.isEmpty() || fontColor.isEmpty() || backgroundColor.isEmpty() || roles.isEmpty()) {
			
			log.error("Assist Notification entry is missing required field values");
			return new HashMap<>();
		}
	    
		configs.add("isNotifEnabled" + "=" + enableNotification);
		configs.add("isAckEnabled" + "=" + enableOptOut);
		configs.add("isDurationEnabled"+"="+enableDuration);
		configs.add("durationLimit"+"="+durationLimit);
		configs.add("fontNotifColor"+"="+fontColor);
		configs.add("backgroundNotifColor"+"="+backgroundColor);
		
		assistText = assistText.trim();
		if(!assistText.startsWith("<") || !assistText.endsWith(">")) {
			assistText = "<p>" + StringEscapeUtils.escapeHtml4(assistText).replaceAll("\\n", "</p><p>").replaceAll("<p>\\s*<\\/p>", "<p>&nbsp;</p>") + "</p>";
		}
		try {
			final byte[] utf8Bytes = assistText.getBytes("UTF-8");
			if(utf8Bytes.length > 2000 && assistText.length() > 1000) {
				return new HashMap<>();
			}
		} catch(Exception ex) {
			log.error("Error while getting Byte size of Assist Text: " + ex);
		}
		
		Map msgMap = dbh.handleDBRequest("getNotificationMsgOnly", null, true);
		String assistMessage = msgMap.containsKey("assistMessage")?  (String) msgMap.get("assistMessage"): "";
		if(!assistMessage.equals(assistNotification.getAssistMessage())) {
			configs.add("notificationMsg"+ "="+assistText);
		}
		for(String role: assistNotification.getRoleList()) {
			roles.add(role);
		}	
		params.put("configs", configs.toArray(new String[0]));
		params.put("roles", roles.toArray(new String[0]));
		log.debug("Exiting getAssistNotification....");
		return params;
	}

	private HashMap<String, List<AssistClassNotificationEntry>> getAssistClassNotification(Document doc) throws Exception
	{
		log.debug("Entering getAssistClassNotification....");
		Node currentTable =doc.getElementsByTagName(Constants.XML.TableAssistClassNotification).item(0);
		
		StringWriter writer = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();	
		
		XStream xstream = new XStream();
		xstream.alias("AssistClassNotification", AssistClassNotificationEntry.class);
		xstream.alias("ClassNotification", List.class);
	    xstream.alias("RoleID", String.class);  
	    assistClassNotificationList = (List<AssistClassNotificationEntry>) xstream.fromXML(xml);  
	    HashMap<String, List<AssistClassNotificationEntry>> params = new HashMap<>();
	    params.put("assistClassNotificationList", assistClassNotificationList);	
		log.debug("Exiting getAssistClassNotification....");
		return params;
	}

	
	@SuppressWarnings("unchecked")
	private void getAssistText(Document doc) throws Exception {
		log.debug("Entering getAssistText....");
		
		Node currentTable = doc.getElementsByTagName(Constants.XML.TableAssistText).item(0);
		StringWriter writer = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();
		
		XStream xstream = new XStream();
		xstream.alias("AssistText", AssistTextEntry.class);
		xstream.alias("Text", List.class);
		xstream.alias("RoleID", String.class);
		List<AssistTextEntry> textList =  (List<AssistTextEntry>) xstream.fromXML(xml);
		
		for(int j=0; j<textList.size(); j++) {
			AssistTextEntry atextEntry = textList.get(j);
			
			HashMap<String,String> idList = new UIListHandler().getClassAttributeID(atextEntry.getClassName(), atextEntry.getAtrrName());
			if(!idList.get("classid").equalsIgnoreCase(""))	{
				atextEntry.setClassID(idList.get("classid"));
				if(!idList.get("attid").equalsIgnoreCase("")) {
					atextEntry.setAttrID(idList.get("attid"));
				} else if(StringUtils.isNumeric(atextEntry.getAttrID())) {
					syncFlag = false;
					continue;
				}
			} else {
				syncFlag = false;
				continue;
			}
			
			String key1 = null;
			String key = atextEntry.getClassID() + ":" + atextEntry.getAttrID() + ":";
			
			IAdmin adminInstance=AgileHandler.getAgileSession().getAdminInstance();
			IAgileClass cls = AgileHandler.getAgileClass(adminInstance, atextEntry.getClassName());
			String wfID = atextEntry.getWorkflowID();
			String wfStatusID = atextEntry.getWorkflowStatusId();
			if(cls.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS)) {
				if(!"Lifecycles".equalsIgnoreCase(wfID)) {
					wfID = "Lifecycles";
					atextEntry.setWorkflowID(wfID);
				}
				if(wfStatusID == null || wfStatusID.trim().length() == 0) {
					wfStatusID = "All Statuses";
					atextEntry.setWorkflowStatusId(wfStatusID);
				}
				key += wfID;
				key1 = key;
				key += ":" + wfStatusID + ":";
			} else {
				if(wfID == null || wfID.trim().length() == 0 || wfID.equalsIgnoreCase("All Workflows")) {
					key += "All Workflows";
					key1 = key;
					key += ":" + wfStatusID + ":";
				} else {
					IWorkflow[] workflows = ((IRoutableDesc) cls).getWorkflows();
					boolean wfFlag = false;
					if(workflows.length > 0) {
						for(IWorkflow wf : workflows) {
							if(wf.getName().equalsIgnoreCase(wfID)) {
								wfFlag = true;
								break;
							}
						}
					} else {
						IAgileList wfList = cls.getAttribute("Workflow").getAvailableValues();
						ArrayList<String> wf = getValuesFromAgileList(wfList);
						if(wf.contains(wfID)) {
							wfFlag = true;
						}
					}
					
					if(wfFlag) {
						key += wfID;
						key1 = key;
						key += ":" + wfStatusID + ":";
					} else {
						syncFlag = false;
						continue;
					}
				}
			}
			
			List<String> roles = atextEntry.getRolesList();
			Collections.sort(roles);	
			for(int i=0; i<roles.size(); i++) {
				key += roles.get(i) + ":";
			}

			key = key.substring(0, key.length()-1);

			log.debug("key XML " + key);
			xmlAssistTextMap.put(key, atextEntry); 
			
			WorkflowRolesList wrList = new WorkflowRolesList();			
			List<String> wsid = new ArrayList<String>();
			String[] split = wfStatusID.split(";");			
			for(int i=0; i<split.length; i++) {
				wsid.add(split[i]);
			}			
			wrList.wsidList = wsid;
			wrList.rolesList = roles;
			wrList.text = atextEntry;
			log.debug("key1 XML"+key1);
			if(xmlAssistTextNew.containsKey(key1)) {
				xmlAssistTextNew.get(key1).add(wrList);
			} else {
				List<WorkflowRolesList> workflowList = new ArrayList<WorkflowRolesList>();
				workflowList.add(wrList);
				xmlAssistTextNew.put(key1, workflowList);
			}
		}
		
		log.debug("Exiting getAssistText....");
		
	}
	
	@SuppressWarnings("unchecked")
	private void overWrite(Document doc) throws Exception
	{
		
		log.debug("Entering overWrite..");
		merge(doc);
		
	for( String k:dbAssistTextMapTexttID.keySet() )
		{
			if(!xmlAssistTextMap.containsKey(k))
			{
				HashMap<String,Object> params=new HashMap<String,Object>();
				params.put("textID",dbAssistTextMapTexttID.get(k) );
				Map<String, Object> text= (Map<String, Object>) dbh.handleDBRequest("getAssistTextUsingTextId", params, true);
				AssistText aText=(AssistText) text.get("AssistTextUsingTextId");
				String classId=(String) text.get("classId");
				String attrId=(String) text.get("attrId");
				params.put("classId",classId);
				params.put("attrId",attrId);
				
				params.put("workflow_lifecycle", aText.getWorkflow_lifecycle());
				
				String roles="";
				try {
				List<String> allRoles=aText.getRoles();
				roles=allRoles.get(0);
				for(int k1=1; k1<allRoles.size(); k1++)
					roles=roles+";"+allRoles.get(k1);
				}
				catch(Exception e)
				{
					roles="";
				}
				params.put("roles", roles);
				params.put("roleList", aText.getRoles());
				
				String statuses="";
				try {
					List<String> allStatuses=aText.getWorkflowStatuses();
					statuses=allStatuses.get(0);
					for(int k1=1; k1<allStatuses.size(); k1++)
						statuses=statuses+";"+allStatuses.get(k1);
					}
					catch(Exception e)
					{
						statuses="";
					}
				
				params.put("statuses", statuses);
				
				
				deletedEntries.add(params);
				log.info("-------Entry deleted in Database with following new Details-------");
				log.info("Attribute ID: " +attrId);
				log.info("Class ID: " + classId);
				log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(aText.getText()));
				log.info(" Font Color: " + aText.getFontColor());
				log.info(" Background Color: " + aText.getBackgroundColor());
				log.info(" Workflow: " + aText.getWorkflow_lifecycle());
				log.info(" Workflow Status: " + aText.getWorkflowStatuses().toString());
				List<String> roles1=aText.getRoles();
				log.info(" Roles were as follows");
				for(int k1=0; k1<roles1.size(); k1++)
					log.info("--"+roles1.get(k1));
				
				dbh.handleDBRequest("removeAssistText", params, true);
				
			}
		}
		
		for(int i=0;i<roleIDList.size();i++)
		{
			
			if(!xmlRoleIds.contains(roleIDList.get(i)))
			{
				HashMap<String,String> params=new HashMap<String,String>();
				params.put("roleID", roleIDList.get(i));
				dbh.handleDBRequest("deleteRolePriority", params, true);
			}
		}
		for(int i=0;i<colorIDList.size();i++)
		{			
			if(!xmlColorIds.contains(colorIDList.get(i)))
			{
				HashMap<String,String> params=new HashMap<String,String>();
				params.put("colorID", colorIDList.get(i));
				dbh.handleDBRequest("deleteAssistColor", params, true);
			}
		
		}
		merge(doc);
		
		log.debug("Exiting overWrite..");
	}	
	@SuppressWarnings("unchecked")
	private void newOverWrite(Document doc) throws Exception
	{
		log.debug("Entering newOverWrite..");
		Map<String, Object> existingAT= (Map<String, Object>) dbh.handleDBRequest("getAllAssistTexts", null, true);
		List<AssistTextEntry> existingAssistTexts=(List<AssistTextEntry>) existingAT.get("existingEntries");
		
		for(int i=0; i<existingAssistTexts.size(); i++) {
			AssistTextEntry entry=existingAssistTexts.get(i);
			HashMap<String,Object> params=new HashMap<String,Object>();
			params.put("textID",entry.getTextID());
			String classId= entry.getClassID();
			String attrId= entry.getAttrID();
			params.put("classId", classId);
			params.put("attrId",attrId);
			
			params.put("workflow_lifecycle", entry.getWorkflowID());
			
			String roles="";
			try {
			
			List<String> allRoles=entry.getRolesList();
			roles=allRoles.get(0);
			for(int k1=1; k1<allRoles.size(); k1++)
				roles=roles+";"+allRoles.get(k1);
			}
			catch(Exception e)
			{
				roles="";
			}
			params.put("roles", roles);
			params.put("roleList", entry.getRolesList());
			
			String statuses="";
			try {
				List<String> allStatuses=entry.getWorkflowStatuses();
				statuses=allStatuses.get(0);
				for(int k1=1; k1<allStatuses.size(); k1++)
					statuses=statuses+"; "+allStatuses.get(k1);
			}
			catch(Exception e) {
				statuses="";
			}
			
			params.put("statuses", statuses);
			deletedEntries.add(params);
		}
		Map<?, ?> isErrorExistsMap=(Map<?, ?>)	dbh.handleDBRequest("deleteTables", null, false);
		Map<?, ?> isErrorExistsClassNotifMap=(Map<?, ?>) dbh.handleDBRequest("deleteClassNotificationTables", null, false);
		//dbh.handleDBRequest("deleteNotificationTables", null, false);
		boolean isErrorExists=(Boolean) isErrorExistsMap.get("isErrorExists");
		boolean isClassNotifErrorExists=(Boolean) isErrorExistsClassNotifMap.get("isErrorExists");
		if(!isErrorExists && !isClassNotifErrorExists) {
			merge(doc);
			log.info("tables deleted successfully");
		}
		else
			log.error("Error deleting tables");
		
//	for( String k:dbAssistTextMapTexttID.keySet() )
//		{
//			if(!xmlAssistTextMap.containsKey(k))
//			{
//				HashMap<String,Object> params=new HashMap<String,Object>();
//				params.put("textID",dbAssistTextMapTexttID.get(k) );
//				Map<String, Object> text= (Map<String, Object>) dbh.handleDBRequest("getAssistTextUsingTextId", params, true);
//				AssistText aText=(AssistText) text.get("AssistTextUsingTextId");
//				String classId=(String) text.get("classId");
//				String attrId=(String) text.get("attrId");
//				params.put("classId",classId);
//				params.put("attrId",attrId);
//				
//				params.put("workflow_lifecycle", aText.getWorkflow_lifecycle());
//				
//				String roles="";
//				try {
//				List<String> allRoles=aText.getRoles();
//				roles=allRoles.get(0);
//				for(int k1=1; k1<allRoles.size(); k1++)
//					roles=roles+";"+allRoles.get(k1);
//				}
//				catch(Exception e)
//				{
//					roles="";
//				}
//				params.put("roles", roles);
//				params.put("roleList", aText.getRoles());
//				
//				String statuses="";
//				try {
//					List<String> allStatuses=aText.getWorkflowStatuses();
//					statuses=allStatuses.get(0);
//					for(int k1=1; k1<allStatuses.size(); k1++)
//						statuses=statuses+";"+allStatuses.get(k1);
//					}
//					catch(Exception e)
//					{
//						statuses="";
//					}
//				
//				params.put("statuses", statuses);
//				
//				
//				deletedEntries.add(params);
//				log.info("-------Entry deleted in Database with following new Details-------");
//				log.info("Attribute ID: " +attrId);
//				log.info("Class ID: " + classId);
//				log.info("Associated Assist Text: " + StringEscapeUtils.unescapeXml(aText.getText()));
//				log.info(" Font Color: " + aText.getFontColor());
//				log.info(" Background Color: " + aText.getBackgroundColor());
//				log.info(" Workflow: " + aText.getWorkflow_lifecycle());
//				log.info(" Workflow Status: " + aText.getWorkflowStatuses().toString());
//				List<String> roles1=aText.getRoles();
//				log.info(" Roles were as follows");
//				for(int k1=0; k1<roles1.size(); k1++)
//					log.info("--"+roles1.get(k1));
//				
//				dbh.handleDBRequest("removeAssistText", params, true);
//				
//			}
//		}
		
		for(int i=0;i<roleIDList.size();i++)
		{
			
			if(!xmlRoleIds.contains(roleIDList.get(i)))
			{
				HashMap<String,String> params=new HashMap<String,String>();
				params.put("roleID", roleIDList.get(i));
				dbh.handleDBRequest("deleteRolePriority", params, true);
			}
		}
		dbh.handleDBRequest("deleteUncheckedRoles", null,true);
		for(int i=0;i<colorIDList.size();i++)
		{			
			if(!xmlColorIds.contains(colorIDList.get(i)))
			{
				HashMap<String,String> params=new HashMap<String,String>();
				params.put("colorID", colorIDList.get(i));
				dbh.handleDBRequest("deleteAssistColor", params, true);
			}
		
		}		
		log.debug("Exiting overWrite..");
	}	
	
	
	private void setAssistText() throws Exception 
	{
		log.debug("Entering setAssistText....");
		HashMap<String,List<AssistTextEntry>> textList=(HashMap<String, List<AssistTextEntry>>) dbh.handleDBRequest("getAssistText",null,false);

		List<AssistTextEntry> assistText=textList.get("assistText");
		XStream xstream = new XStream();
		xstream.alias("AssistText", AssistTextEntry.class);
	    xstream.alias("Text", List.class);
		xstream.alias("RoleID", String.class);
	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer  writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
		xstream.toXML(assistText, writer);
		String xml = stream.toString("UTF-8");
	
		fw.append(xml);
		
		
		log.debug("Exittering setAssistText....");
	}
	
	private void setAssistClassNotification() throws Exception 
	{
		log.debug("Entering setAssistClassNotification....");
		HashMap<String,List<AssistClassNotificationEntry>> assistClassNotifList = (HashMap<String, List<AssistClassNotificationEntry>>) dbh.handleDBRequest("getClassNotification",null,false);
		List<AssistClassNotificationEntry> classNotifList = assistClassNotifList.get("classNotification");
		
		XStream xstream = new XStream();
		xstream.alias("AssistClassNotification", AssistClassNotificationEntry.class);
		xstream.alias("ClassNotification", List.class);
		xstream.alias("RoleID", String.class);
	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer  writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
		xstream.toXML(classNotifList, writer);
		String xml = stream.toString("UTF-8");
	
		fw.append(xml);
		log.debug("Exiting setAssistClassNotification....");
	}

	
	private void setAssistNotification() throws Exception 
	{
		log.debug("Entering setAssistText....");
		HashMap<String, AssistNotificationEntry> assistNotification=(HashMap<String, AssistNotificationEntry>) dbh.handleDBRequest("getAssistNotification",null,false);

		AssistNotificationEntry notification = assistNotification.get("assistNotification");
		XStream xstream = new XStream();
		xstream.alias("AssistNotification", AssistNotificationEntry.class);
		xstream.alias("RoleID", String.class);
	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer  writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
		xstream.toXML(notification, writer);
		String xml = stream.toString("UTF-8");
	
		fw.append(xml);		
		log.debug("Exiting setAssistText....");
	}

	private void setAssistConfiguration() throws Exception 
	{
		log.debug("Entering setAssistConfiguration....");
		HashMap<String, String> assistConfiguration = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations",null,false);
		
		AssistNotifConfigEntry config = new AssistNotifConfigEntry();
		config.setNotifEnable(assistConfiguration.get("isNotifEnabled"));
		config.setOptOutEnable(assistConfiguration.get("isAckEnabled"));

		XStream xstream = new XStream();
		xstream.alias("AssistConfiguration", AssistNotifConfigEntry.class);
	
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Writer  writer = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
		xstream.toXML(config, writer);
		String xml = stream.toString("UTF-8");
	
		fw.append(xml);		
		log.debug("Exiting setAssistConfiguration....");
	}

	
	private void endXML() throws IOException
	{
		log.debug("Entering endXML....");
		fw.append(Constants.XML.EndingTag); 
		fw.append(System.getProperty("line.separator"));
		fw.flush();
		fw.close();
		log.debug("Exiting endXML....");
	}
	
	public HashMap<String,Boolean> exportXML() 
	{
		log.debug("Entering exportXML....");
		boolean status = false;
		HashMap<String, Boolean> statusMap=new HashMap<String,Boolean>();
		try {
			dbh=new DBHandler();
			Map<String, Boolean> rolesStatus;
			rolesStatus = (Map<String, Boolean>) dbh.handleDBRequest("isRoles", null, false);
			Boolean isRoles=(Boolean)rolesStatus.get("isRoles");
			initExport(isRoles);
			setAssistText();
			setAssistConfiguration();
			setAssistNotification();
			setAssistClassNotification();
			setRolePriority(isRoles);
			setAssistColor();
			endXML();

			status=true;
		} catch (Exception e) {
			log.error("Exception: ", e);
			status=false;
		}
		statusMap.put("status", status);
		log.debug("Exiting exportXML....");

		return statusMap;
		

	}

	private ArrayList<String> getValuesFromAgileList(IAgileList list) throws APIException 
	{ 
		ArrayList<String> values=new ArrayList<String>();
		if (list != null ) { 
			Object[] children = list.getChildren(); 
			if (children != null)
			{  
				for (int i = 0; i < children.length; ++i) 
				{ 
					list=(IAgileList)children[i];
					String val=list.getValue().toString().toLowerCase();
					if(!val.contains("obsolete") && !val.contains("end of life") && !val.contains("eol") )
					{
						values.add(list.getValue().toString());
					}
				}
			} 
		} 
		return values;
	} 	
	public HashMap<String,Object> importXML( HashMap<String, String> params) throws Exception
	{
		log.debug("Entering importXMLIntoDB..");
		String status = "false";
		String rolesCheck="okay";
		String hasError="false";
		String accessType="okay";
		String classesCheck="okay";
		
		File fXmlFile=null;
		HashMap<String, Object> statusMap=new HashMap<String,Object>();
		try {
			String version=null;
			Properties prop = ConfigHelper.loadPropertyFile();			
			HashMap<String, String> map=validateXML(params.get("path"));
			hasError=map.get("hasError");
			accessType=map.get("accessType");
			

			fXmlFile = new File(params.get("path"));
			if(!hasError.equals("true"))
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				factory.setXIncludeAware(false);
				factory.setExpandEntityReferences(false);
				factory.setValidating(false);
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setErrorHandler(null);
				Document doc = builder.parse(fXmlFile);
				
				log.debug("XML is Valid!");
				Node currentNode = doc.getDocumentElement();
				if (currentNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element rootElement = (Element) currentNode;
					version=prop.getProperty("version");
					if(rootElement.getAttribute("version").equals(version))
					{
						if(params.get("type").equals("overWrite"))
						{
							newOverWrite(doc);
							if(!syncFlag)
							{
								classesCheck="notokay";
							}
						}
						else if(params.get("type").equals("merge"))
						{
							merge(doc);
							if(!syncFlag)
							{
								classesCheck="notokay";
							}
						}
						rolesCheck=checkRoles();
						status="true";
					}
					else
					{
						status="false";
					}
				}						
				fXmlFile.delete();
			}
			else
			{
				log.debug("Invlaid XML!");
				fXmlFile.delete();
				status="false";
			}			

			

		} 
		 catch (ParserConfigurationException e) {
			status="false";
			fXmlFile.delete();
			log.error("ParserConfigurationException: ", e);
		}
		catch (SAXException e) {
			status="false";
			log.error("SAXException: ", e);
			fXmlFile.delete();
		} catch (IOException e) {
			status="false";
			if(fXmlFile!=null)
			{
				fXmlFile.delete();
			}
			log.error("IOException: ", e);
		}
		catch (DocumentException e)
		{
			if(fXmlFile!=null)
			{
				fXmlFile.delete();
			}
			status="false";
			hasError="true";
			log.error("DocumentException: ", e);
		}
		catch (Exception e)
		{
			if(fXmlFile!=null)
			{
				fXmlFile.delete();
			}
			status="false";
			log.error("Exception: ", e);			


		}
		statusMap.put("status", status);
		statusMap.put("hasError",hasError);
		statusMap.put("accessType", accessType);
		statusMap.put("rolesCheck", rolesCheck);
		statusMap.put("classesCheck", classesCheck); 
		statusMap.put("insertedEntries",insertedEntries);
		statusMap.put("updatedEntries",updatedEntries);
		statusMap.put("deletedEntries",deletedEntries);
		statusMap.put("failedEntries",failedEntries);
		
		log.debug("Exiting importXML..");
		return statusMap;
		
	}
	
	@SuppressWarnings("unchecked")
	private List<AssistClassEntry> parseXML(Document doc) throws Exception {
		log.debug("Entering parseXML....");
		
		Node currentTable = doc.getElementsByTagName(Constants.XML.TableAssistText).item(0);
		StringWriter writer = new StringWriter();
		
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer = tf.newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();
		
		XStream xstream = new XStream();
		xstream.alias("AssistText", AssistTextEntry.class);
		xstream.alias("Text", List.class);
		xstream.alias("RoleID", String.class);
		List<AssistTextEntry> textList =  (List<AssistTextEntry>) xstream.fromXML(xml);
		
		//populating class entries
		Map<String, AssistClassEntry> classMap=new HashMap<String, AssistClassEntry>();
		AssistAttributeEntry attribute=null;
		for(int i=0; i<textList.size(); i++)
		{
			AssistTextEntry classTextEntry = textList.get(i);
			String clsId=classTextEntry.getClassID();
			Map<String, AssistAttributeEntry> attributeMap=new HashMap<String, AssistAttributeEntry>();
			AssistClassEntry classEntry=null;
			if(classMap.containsKey(clsId))
			{
				classEntry=classMap.get(clsId);
				attributeMap=classEntry.getAttrMap();
			}
			else
			{
				classEntry=new AssistClassEntry();
				classEntry.setClassID(clsId);
				classEntry.setAttrMap(new HashMap<String, AssistAttributeEntry>());
			}
			
			String attrId=classTextEntry.getAttrID();
			AssistAttributeEntry attributeEntry=null;
			if(attributeMap.containsKey(attrId))
			{
				attributeEntry=attributeMap.get(attrId);
			}
			else
			{
				attributeEntry=new AssistAttributeEntry();
				attributeEntry.setAttrID(attrId);
				attributeEntry.setTexts(new ArrayList<AssistText>());
			}
			
			List<AssistText> atList=attributeEntry.getTexts();
			AssistText text=new AssistText();	
			text.setText(classTextEntry.getAssistText());
			text.setBackgroundColor(classTextEntry.getBackgroundColor());
			text.setFontColor(classTextEntry.getFontColor());
			text.setWorkflow_lifecycle(classTextEntry.getWorkflowID());
			text.setRoles(classTextEntry.getRolesList());
			text.setDateCreated(classTextEntry.getDateCreated());
			text.setLastUpdated(classTextEntry.getLastUpdated());
			text.setTextID(classTextEntry.getTextID());
			text.setIsDiffColor(classTextEntry.getIsDifferentColor());
			
			String tempStatuses=classTextEntry.getWorkflowStatusId();
			List<String> statusList=Arrays.asList(tempStatuses.split(";"));
			text.setWorkflowStatuses(statusList);
			
			atList.add(text);
			attributeEntry.setTexts(atList);
			
			attributeMap.put(attrId, attributeEntry);
			
			classEntry.setAttrMap(attributeMap);
			classMap.put(clsId, classEntry);
		}
		//
		
		//converting them into list form
		for(String key: classMap.keySet())
		{
			AssistClassEntry clsEntry=classMap.get(key);
			Map<String, AssistAttributeEntry> attrMap=clsEntry.getAttrMap();
			List<AssistAttributeEntry> allAttributes=new ArrayList<AssistAttributeEntry>();
			for(String key1: attrMap.keySet())
			{
				AssistAttributeEntry attrEntry=attrMap.get(key1);
				allAttributes.add(attrEntry);
			}
			clsEntry.setAttributes(allAttributes);
		}
		
		
		List<AssistClassEntry> classes=new ArrayList<AssistClassEntry>();
		for(String key: classMap.keySet())
		{
			AssistClassEntry clsEntry=classMap.get(key);
			classes.add(clsEntry);
			
		}
		//
		
		
		//find all label colors from xml file
		Node currentTable1 =doc.getElementsByTagName(Constants.XML.TableAssitColor).item(0);
		StringWriter writer1 = new StringWriter();
		TransformerFactory tf1 = TransformerFactory.newInstance();
		tf1.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf1.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		tf1.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer transformer1 = tf1.newTransformer();
		transformer1.transform(new DOMSource(currentTable1), new StreamResult(writer1));
		String xml1 = writer1.toString();	
		XStream xstream1 = new XStream();
		xstream1.alias("LabelColor", AssistColorEntry.class);
		xstream1.alias("LabelColors", List.class);
		List<AssistColorEntry> assistColorList =  (List<AssistColorEntry>) xstream1.fromXML(xml1);
		
		
		for(int i=0; i<classes.size(); i++) {
			AssistClassEntry tempClass=classes.get(i);
			String classId=tempClass.getClassID();
			List<AssistAttributeEntry> tempAttributeList=tempClass.getAttributes();
			for(int j=0; j<tempAttributeList.size(); j++)
			{
				AssistAttributeEntry tempAtr=tempAttributeList.get(j);
				String tempAttrId=tempAtr.getAttrID();
				for(int k=0; k<assistColorList.size(); k++)
				{
					AssistColorEntry colorEntry=assistColorList.get(k);
					if(classId.equals(colorEntry.getClassId()) && tempAttrId.equals(colorEntry.getAttributeId()))
						tempAtr.setLabelColor(colorEntry.getAssistColor());
						
				}
			}
		}
			
		
		
		
		for(int j=0; j<textList.size(); j++) {
			AssistTextEntry atextEntry = textList.get(j);
			HashMap<String,String> idList = new UIListHandler().getClassAttributeID(atextEntry.getClassName(), atextEntry.getAtrrName());
			if(!idList.get("classid").equalsIgnoreCase(""))	{
				atextEntry.setClassID(idList.get("classid"));
				if(!idList.get("attid").equalsIgnoreCase("")) {
					atextEntry.setAttrID(idList.get("attid"));
				} else if(StringUtils.isNumeric(atextEntry.getAttrID())) {
					
					syncFlag = false;
					continue;
				}
			} else {
				syncFlag = false;
				continue;
			}
			
			String key1 = null;
			String key = atextEntry.getClassID() + ":" + atextEntry.getAttrID() + ":";
			
			IAdmin adminInstance=AgileHandler.getAgileSession().getAdminInstance();
			IAgileClass cls = AgileHandler.getAgileClass(adminInstance, atextEntry.getClassName());
			String wfID = atextEntry.getWorkflowID();
			String wfStatusID = atextEntry.getWorkflowStatusId();
			
			
			
			
			
			
			
			if(cls.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS)) {
				if(!"Lifecycles".equalsIgnoreCase(wfID)) {
					wfID = "Lifecycles";
					atextEntry.setWorkflowID(wfID);
				}
				if(wfStatusID == null || wfStatusID.trim().length() == 0) {
					wfStatusID = "All Statuses";
					atextEntry.setWorkflowStatusId(wfStatusID);
				}
				key += wfID;
				key1 = key;
				key += ":" + wfStatusID + ":";
			} else {
				if(wfID == null || wfID.trim().length() == 0 || wfID.equalsIgnoreCase("All Workflows")) {
					key += "All Workflows";
					key1 = key;
					key += ":" + wfStatusID + ":";
				} else {
					IWorkflow[] workflows = ((IRoutableDesc) cls).getWorkflows();
					boolean wfFlag = false;
					if(workflows.length > 0) {
						for(IWorkflow wf : workflows) {
							if(wf.getName().equalsIgnoreCase(wfID)) {
								wfFlag = true;
								break;
							}
						}
					} else {
						IAgileList wfList = cls.getAttribute("Workflow").getAvailableValues();
						ArrayList<String> wf = getValuesFromAgileList(wfList);
						if(wf.contains(wfID)) {
							wfFlag = true;
						}
					}
					
					if(wfFlag) {
						key += wfID;
						key1 = key;
						key += ":" + wfStatusID + ":";
					} else {
						syncFlag = false;
						continue;
					}
				}
			}
			
			List<String> roles = atextEntry.getRolesList();
			Collections.sort(roles);	
			for(int i=0; i<roles.size(); i++) {
				key += roles.get(i) + ":";
			}

			key = key.substring(0, key.length()-1);

			log.debug("key XML " + key);
			xmlAssistTextMap.put(key, atextEntry); 
			
			WorkflowRolesList wrList = new WorkflowRolesList();			
			List<String> wsid = new ArrayList<String>();
			String[] split = wfStatusID.split(";");			
			for(int i=0; i<split.length; i++) {
				wsid.add(split[i]);
			}			
			wrList.wsidList = wsid;
			wrList.rolesList = roles;
			wrList.text = atextEntry;
			log.debug("key1 XML"+key1);
			if(xmlAssistTextNew.containsKey(key1)) {
				xmlAssistTextNew.get(key1).add(wrList);
			} else {
				List<WorkflowRolesList> workflowList = new ArrayList<WorkflowRolesList>();
				workflowList.add(wrList);
				xmlAssistTextNew.put(key1, workflowList);
			}
		}
		
	
		
		log.debug("Exiting getAssistText....");
		return classes;
		
	}	


	private class AgileClassDO {
		private IAgileClass agileClass = null;
		private Integer classId = null;
		private boolean isRoutable = false;
		private boolean isItem = false;
		private Set<String> workflows = new HashSet<String>();
		private Map<String, Set<String>> statuses = new HashMap<String, Set<String>>();
		private Map<String, Integer> attributes = new HashMap<String, Integer>(); 
	}

}
