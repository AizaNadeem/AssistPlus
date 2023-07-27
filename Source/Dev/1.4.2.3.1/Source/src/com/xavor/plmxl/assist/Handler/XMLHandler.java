package com.xavor.plmxl.assist.Handler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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

import org.apache.log4j.xml.SAXErrorHandler;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.xavor.plmxl.assist.DO.AssistColorEntry;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class XMLHandler {
	
	FileWriter  fw=null;
	AssistLogger log=AssistLogger.getInstance();
	DBHandler dbh=null;
	
	List<String> roleIDList=null;
	List<String> colorIDList=null;
	
	HashMap<String,AssistTextEntry> xmlAssistTextMap=null;
	HashMap<String,String> dbAssistTextMap=null;
	
	List<String> xmlColorIds=null;
	List<String> xmlRoleIds=null;
	
	List<AssistColorEntry> AssistColorList=null;
	List<RoleEntry> RolePriorityList=null;
	
	private void initExport( boolean isRoles) throws Exception
	{
		log.debug("Entering init....");

		Properties prop = ConfigHelper.loadPropertyFile();
	
		String abspath = ConfigHelper.getAppHomePath();
		
		fw = new FileWriter(abspath+Constants.XML.XMLFileName);
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
		
		SAXParserFactory fac = SAXParserFactory.newInstance();  
		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		
		dbh=new DBHandler();
		Map<String, Boolean> rolesStatus=(Map<String, Boolean>) dbh.handleDBRequest("isRoles", null, false);
		Boolean isRoles=(Boolean)rolesStatus.get("isRoles");
		if(isRoles)
		{
			log.debug("ROLES");
			fac.setSchema(schemaFactory.newSchema(new Source[] {new StreamSource(abspath+Constants.Config.XSD_ROLE)}));
		}
		else
		{
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
		String hasError="false";
		String accessType="okay";
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
						}
						else if(params.get("type").equals("merge"))
						{
							merge(doc);
						}
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
		
		log.debug("Exiting importXML..");
		return statusMap;
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
		HashMap<String,Object> params=new HashMap<String,Object>();
		params.put("color", AssistColorList);
		params.put("colorIDList", colorIDList);
		dbh.handleDBRequest("mergeAssistColor", params, true);
		
		log.debug("Exiting mergeAssistColor....");

	}
	private void mergeRolePriority(Document doc) throws Exception
	{
		log.debug("Entering getRolePriority....");
		HashMap<String,List<String>> roleIds=(HashMap<String, List<String>>) dbh.handleDBRequest("getRoleIDs", null,false);
		roleIDList=roleIds.get("roleIDList");
		xmlRoleIds=new ArrayList<String>();
		
		getRolePriority(doc);	
		HashMap<String,Object> params=new HashMap<String,Object>();
		params.put("role", RolePriorityList);
		params.put("roleIDList", roleIDList);
		dbh.handleDBRequest("mergeRolePriority", params, true);
				
		log.debug("Exiting getRolePriority....");

	}
	private void mergeAssistText(Document doc) throws Exception
	{
		xmlAssistTextMap=new HashMap<String,AssistTextEntry>(); 
		getAssistText(doc);	
		
		HashMap<String,List<String>> textRoles=(HashMap<String, List<String>>) dbh.handleDBRequest("getTextRoles", null,false);
		HashMap<String,List<AssistTextEntry>> dbAssistTextList=(HashMap<String, List<AssistTextEntry>>) dbh.handleDBRequest("getAssistTextMap", null, false);
		
		List<AssistTextEntry> dbTextList= dbAssistTextList.get("textList");
		
		dbAssistTextMap=new HashMap<String,String>();
		
		for(int i=0;i<dbTextList.size();i++)
		{
			String key=dbTextList.get(i).getClassID()+":";
			key+=dbTextList.get(i).getAttrID()+":";
			key+=dbTextList.get(i).getWorkflowID()+":";
			key+=dbTextList.get(i).getWorkflowStatusId()+":";
			
			log.debug(key);
			List<String> roles=textRoles.get(dbTextList.get(i).getTextID());
			
			Collections.sort(roles);
			
			for(int j=0;j<roles.size();j++)
			{
				key+=roles.get(j)+":";
			}
			key=key.substring(0,key.length()-1);
			
			dbAssistTextMap.put(key,dbTextList.get(i).getTextID() );
						
		}
		HashMap<String,AssistTextEntry> updateText= new HashMap<String,AssistTextEntry>();
		HashMap<String,AssistTextEntry> insertText= new HashMap<String,AssistTextEntry>();
		
		for( String k:xmlAssistTextMap.keySet() )
		{
			if(dbAssistTextMap.containsKey(k))
			{
				AssistTextEntry text=xmlAssistTextMap.get(k);
				updateText.put(dbAssistTextMap.get(k), text);
				log.debug("true");
			}
			else
			{
				AssistTextEntry text=xmlAssistTextMap.get(k);
				insertText.put(k,text );
				log.debug("false");
			}
		}
		
		HashMap<String,Object> params=new HashMap<String,Object>();
		params.put("updateText", updateText);
		dbh.handleDBRequest("mergeUpdateText", params,true);
		
		params=new HashMap<String,Object>();
		params.put("insertText", insertText);
		dbh.handleDBRequest("mergeInsertText", params, true);
		
		
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
			key=textList.get(j).getClassID()+":";
			key+=textList.get(j).getAttrID()+":";
			key+=textList.get(j).getWorkflowID()+":";
			key+=textList.get(j).getWorkflowStatusId()+":";

			List<String> roles=textList.get(j).getRolesList();
			Collections.sort(roles);

			for(int i=0;i<roles.size();i++)
			{
				key+=roles.get(i)+":";
			}

			key=key.substring(0,key.length()-1);

			AssistTextEntry textValue= textList.get(j);
			xmlAssistTextMap.put(key, textValue);

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
		String xml = xstream.toXML(assistText);
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

	

}
