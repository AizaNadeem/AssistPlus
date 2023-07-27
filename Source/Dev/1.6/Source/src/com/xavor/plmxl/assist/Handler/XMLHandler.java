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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.xml.SAXErrorHandler;
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
import com.agile.api.IRoutableDesc;
import com.agile.api.IWorkflow;
import com.thoughtworks.xstream.XStream;
import com.xavor.plmxl.assist.DO.AssistColorEntry;
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
	
	List<String> roleIDList=null;
	List<String> colorIDList=null;
	
	HashMap<String,List<WorkflowRolesList>> xmlAssistTextNew=null;
	HashMap<String, List<WorkflowRolesList>> dbAssistTextNew=null;
	
	HashMap<String,AssistTextEntry> xmlAssistTextMap=null;
	HashMap<String,String> dbAssistTextMap=null;
	
	List<String> xmlColorIds=null;
	List<String> xmlRoleIds=null;
	
	List<AssistColorEntry> AssistColorList=null;
	List<RoleEntry> RolePriorityList=null;
	
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
	public HashMap<String,String> importXML( HashMap<String, String> params) throws Exception
	{
		log.debug("Entering importXML..");
		String status = "false";
		String rolesCheck="okay";
		String hasError="false";
		String accessType="okay";
		String classesCheck="okay";
		File fXmlFile=null;
		HashMap<String, String> statusMap=new HashMap<String,String>();
		try {
			String version=null;
			Properties prop = ConfigHelper.loadPropertyFile();			
			HashMap<String, String> map=validateXML(params.get("path"));
			hasError=map.get("hasError");
			accessType=map.get("accessType");
			

			fXmlFile = new File(params.get("path"));
			if(!hasError.equals("true"))
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); ///parser
				factory.setValidating(false);
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setErrorHandler(new SAXErrorHandler());
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
							overWrite(doc);
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
			fXmlFile.delete();
			log.error("SAXException: ", e);;
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
		log.debug("Exiting importXML..");
		return statusMap;
	}
	
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
		
		mergeAssistText( doc);
		
		mergeAssistColor(doc);
		
		mergeRolePriority(doc);
		

		log.debug("Exiting merge..");
	}
	private void mergeAssistColor(Document doc) throws Exception
	{
		log.debug("Entering mergeAssistColor....");
		HashMap<String,List<String>> colorIds=(HashMap<String, List<String>>)dbh.handleDBRequest("getColorIDs", null,false);
		colorIDList=colorIds.get("colorIDList");
		xmlColorIds=new ArrayList<String>();
		getAssistColor(doc);	
		
		if(AssistColorList.size()!=0)
		{
			HashMap<String,Object> params=new HashMap<String,Object>();
			params.put("color", AssistColorList);
			params.put("colorIDList", colorIDList);
			dbh.handleDBRequest("mergeAssistColor", params, true);
		}
		log.debug("Exiting mergeAssistColor....");

	}
	private void mergeRolePriority(Document doc) throws Exception
	{
		log.debug("Entering getRolePriority....");
		HashMap<String,List<String>> roleIds=(HashMap<String, List<String>>) dbh.handleDBRequest("getRoleIDs", null,false);
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
	private void mergeAssistText(Document doc) throws Exception
	{
		log.debug("Entering mergeAssistText....");
		xmlAssistTextNew=new HashMap<String,List<WorkflowRolesList>>(); 
		xmlAssistTextMap=new HashMap<String,AssistTextEntry>(); 
		
		getAssistText(doc);	
		
		HashMap<String,List<String>> textRoles=(HashMap<String, List<String>>) dbh.handleDBRequest("getTextRoles", null,false);
		HashMap<String,List<AssistTextEntry>> dbAssistTextList=(HashMap<String, List<AssistTextEntry>>) dbh.handleDBRequest("getAssistTextMap", null, false);
		
		List<AssistTextEntry> dbTextList= dbAssistTextList.get("textList");
		
		dbAssistTextNew=new HashMap<String,List<WorkflowRolesList>>();
		dbAssistTextMap=new HashMap<String,String>();
		
		for(int i=0;i<dbTextList.size();i++)
		{
			String key=dbTextList.get(i).getClassID()+":";
			key+=dbTextList.get(i).getAttrID()+":";
			if(dbTextList.get(i).getWorkflowID().toString().trim().equalsIgnoreCase(""))
			{
				key+="All Workflows:";
			}
			else
			{
				key+=dbTextList.get(i).getWorkflowID()+":";
			}
			String key1=key;
			
			key1=key1.substring(0,key1.length()-1);
			
			key+=dbTextList.get(i).getWorkflowStatusId()+":";			
			
			List<String> roles=textRoles.get(dbTextList.get(i).getTextID());			
			Collections.sort(roles);			
			for(int j=0;j<roles.size();j++)
			{
				key+=roles.get(j)+":";
			} 
			key=key.substring(0,key.length()-1);
			log.debug("key DB"+key);
			dbAssistTextMap.put(key,dbTextList.get(i).getTextID() );
			
			WorkflowRolesList wrList=new WorkflowRolesList();
			
			List<String> wsid= new ArrayList<String>();
				
			String[] split=dbTextList.get(i).getWorkflowStatusId().split(";");			
			for(int j=0;j<split.length;j++)				
			{
				wsid.add(split[j]);
			}
			wrList.wsidList=wsid;	
			wrList.rolesList=roles;
					
			AssistTextEntry textValue= new AssistTextEntry();
			textValue.setTextID(dbTextList.get(i).getTextID() );
			wrList.text=textValue;	
		
			if(dbAssistTextNew.containsKey(key1))
			{
				dbAssistTextNew.get(key1).add(wrList);				
			}
			else
			{				
				List<WorkflowRolesList> workflowList=new ArrayList<WorkflowRolesList>();
				workflowList.add(wrList);
				dbAssistTextNew.put(key1, workflowList);				
			}				
			log.debug("key1 DB"+key1);							
		}
		insertUpdateText();
		log.debug("Exiting mergeAssistText....");			
	}
	
	private void insertUpdateText()
	{
		log.debug("Entering insertUpdateText....");
		HashMap<String,AssistTextEntry> insertText= new HashMap<String,AssistTextEntry>();
		HashMap<String,AssistTextEntry> updateText= new HashMap<String,AssistTextEntry>();
		List<String> existingKeys=new ArrayList<String>();
		int insertKey=0;
		
		for( String key:xmlAssistTextMap.keySet() )
		{
			if(dbAssistTextMap.containsKey(key))
			{
				AssistTextEntry text=xmlAssistTextMap.get(key);
				updateText.put(dbAssistTextMap.get(key), text);
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
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
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
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();	
		
		Map<String, Boolean> rolesStatus = (Map<String, Boolean>) dbh.handleDBRequest("isRoles", null, false);
		Boolean isRoles=(Boolean)rolesStatus.get("isRoles");
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
	    RolePriorityList =  (List<RoleEntry>) xstream.fromXML(xml);

	    log.debug("role size : "+RolePriorityList.size());
	    for(int j=0;j<RolePriorityList.size();j++)
	    {
	    	xmlRoleIds.add(RolePriorityList.get(j).getRoleID());
	    }	
		log.debug("Exiting getRolePriority....");

	}

	private void getAssistText(Document doc) throws Exception
	{
		log.debug("Entering getAssistText....");

		Node currentTable =doc.getElementsByTagName(Constants.XML.TableAssistText).item(0);
		StringWriter writer = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(new DOMSource(currentTable), new StreamResult(writer));
		String xml = writer.toString();

		XStream xstream = new XStream();
		xstream.alias("AssistText", AssistTextEntry.class);
		xstream.alias("Text", List.class);
		xstream.alias("RoleID", String.class);
		List<AssistTextEntry> textList =  (List<AssistTextEntry>) xstream.fromXML(xml);
		
		for(int j=0;j<textList.size();j++)
		{		
			String key=null;
			HashMap<String,String> idList=new UIListHandler().getClassAttributeID(textList.get(j).getClassName(),textList.get(j).getAtrrName());
			if(!idList.get("classid").equalsIgnoreCase(""))
			{
				textList.get(j).setClassID(idList.get("classid"));
				if(!idList.get("attid").equalsIgnoreCase(""))
				{
					textList.get(j).setAttrID(idList.get("attid"));
				}
				else
				{
					if( StringUtils.isNumeric(textList.get(j).getAttrID()))
					{
						syncFlag=false;
						continue;
					}
				}
			}
			else
			{
				syncFlag=false;
				continue;
			}
			key=textList.get(j).getClassID()+":";
			key+=textList.get(j).getAttrID()+":";
			if(textList.get(j).getWorkflowID()==null || textList.get(j).getWorkflowID().toString().trim().equalsIgnoreCase("")||textList.get(j).getWorkflowID().equalsIgnoreCase("All Workflows") )
			{
				key+="All Workflows:";
			}
			else
			{
				IAdmin adminInstance=AgileHandler.getAgileSession().getAdminInstance();
				IAgileClass cls = AgileHandler.getAgileClass(adminInstance, textList.get(j).getClassName());
				IWorkflow[] workflows=((IRoutableDesc)cls).getWorkflows();
				boolean wfFlag=false;
				if(workflows.length>0)
				{
					for(IWorkflow wf :workflows)
					{
						if(wf.getName().equalsIgnoreCase(textList.get(j).getWorkflowID()))
						{
							wfFlag=true;
						}
								
								
					}
				}
				else
				{
					IAgileList wfList=cls.getAttribute("Workflow").getAvailableValues();
					ArrayList<String> wf=getValuesFromAgileList(wfList);
					if(wf.contains(textList.get(j).getWorkflowID()))
					{
						wfFlag=true;
					}
					
				}
				if(wfFlag)
					{key+=textList.get(j).getWorkflowID()+":";}
				else{
					syncFlag=false;
					continue;
				}
			}
			String key1=key;
			key+=textList.get(j).getWorkflowStatusId()+":";
			
			List<String> roles=textList.get(j).getRolesList();
			Collections.sort(roles);	
			for(int i=0;i<roles.size();i++)
			{
				key+=roles.get(i)+":";
			}

			key=key.substring(0,key.length()-1);

			log.debug("key XML"+key);
			AssistTextEntry textValue= textList.get(j);
			xmlAssistTextMap.put(key, textValue);
			
			key1=key1.substring(0,key1.length()-1);
			
			WorkflowRolesList wrList=new WorkflowRolesList();			
			List<String> wsid= new ArrayList<String>();
			String[] split=textList.get(j).getWorkflowStatusId().split(";");			
			for(int i=0;i<split.length;i++)				
			{
				wsid.add(split[i]);
			}			
			wrList.wsidList=wsid;
			wrList.rolesList=roles;			
			wrList.text=textValue;			
			if(xmlAssistTextNew.containsKey(key1))
			{
				xmlAssistTextNew.get(key1).add(wrList);				
			}
			else
			{				
				List<WorkflowRolesList> workflowList=new ArrayList<WorkflowRolesList>();
				workflowList.add(wrList);
				xmlAssistTextNew.put(key1, workflowList);				
			}			
			log.debug("key1 XML"+key1);
		}
		
		log.debug("Exiting getAssistText....");
		
	}
	private void overWrite(Document doc) throws Exception
	{
		
		log.debug("Entering overWrite..");
		merge(doc);
		
	for( String k:dbAssistTextMap.keySet() )
		{
			if(!xmlAssistTextMap.containsKey(k))
			{
				HashMap<String,String> params=new HashMap<String,String>();
				params.put("textID",dbAssistTextMap.get(k) );
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

}
