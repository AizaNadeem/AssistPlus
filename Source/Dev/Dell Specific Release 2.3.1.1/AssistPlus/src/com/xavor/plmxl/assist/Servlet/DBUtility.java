package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAttribute;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Handler.XMLHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;

public class DBUtility extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String ArrayList = null;
	AssistLogger log = AssistLogger.getInstance();

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering DBUtility: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		HashMap<String, Object> gsonObj=new HashMap<String, Object>();
		String json = "";
		String mode = request.getParameter("mode").toString();
		Map<String, List<JSONObject>> stats = new HashMap<String, List<JSONObject>>();

		log.debug("mode: " + mode);

		if(mode.equalsIgnoreCase("import")) {
			try {
				String type = request.getParameter("type").toString();
				HashMap<String, String> params = new HashMap<String, String>();
				String path = request.getParameter("path").toString();
				log.debug("path: " + path);

				String abspath = ConfigHelper.getAppHomePath();

				if(path.contains("\\")) {
					String[] splitName = path.split("\\\\");
					if(splitName.length > 0) {
						path = splitName[splitName.length - 1];
					}
				}

				params.put("path", abspath + path);
				params.put("type", type);

				log.info("Importing Database..");

				XMLHandler xml = new XMLHandler();
				HashMap<String, Object> statusMap = xml.importXML(params);

				String status = (String) statusMap.get("status");
				String hasError = (String) statusMap.get("hasError");
				String accessType = (String) statusMap.get("accessType");
				String rolesCheck = (String) statusMap.get("rolesCheck");
				String classesCheck = (String) statusMap.get("classesCheck");
				java.util.ArrayList<HashMap> insertedEntries=(java.util.ArrayList<HashMap> ) statusMap.get("insertedEntries");
				java.util.ArrayList<HashMap> updatedEntries=(java.util.ArrayList<HashMap> ) statusMap.get("updatedEntries");
				java.util.ArrayList<HashMap> deletedEntries=(java.util.ArrayList<HashMap> ) statusMap.get("deletedEntries");
				java.util.ArrayList<HashMap> failedEntries=(java.util.ArrayList<HashMap> ) statusMap.get("failedEntries");
				
				
				List<JSONObject> rows = new ArrayList<JSONObject>();
				IAdmin admin = AgileHandler.getAgileSession().getAdminInstance();
				Map<String, IAgileClass> aclass_cache = new HashMap<String, IAgileClass>();
				Map<String, String> attr_cache = new HashMap<String, String>();
				int insertCount=0, updateCount=0, deleteCount=0, failedCount=0;
				for(int i=0; i<insertedEntries.size(); i++)
				{
					HashMap<String, Object> tempEntry=insertedEntries.get(i);
					JSONObject row= new JSONObject();
					String classId = (String) tempEntry.get("classId");
					String attrId = (String) tempEntry.get("attrId");
					IAgileClass aclass = null;
					if(aclass_cache.containsKey(classId)) {
						aclass = aclass_cache.get(classId);
					} else {
						aclass = admin.getAgileClass(Integer.valueOf(classId));
						aclass_cache.put(classId, aclass);
					}
					if(aclass == null) {
						log.info("Class not found: " + classId);
						continue;
					}
					String attrName = null;
					String key = classId + "." + attrId;
					if(attr_cache.containsKey(key)) {
						attrName = attr_cache.get(key);
					} else {
						IAttribute iattr = null;
						try {
							iattr = aclass.getAttribute(Integer.valueOf(attrId));
						} catch(Exception ex) {
							iattr = null;
						}
						if(iattr != null) {
							attrName = iattr.getFullName();
						}
						attr_cache.put(key, attrName);
					}
					if(attrName == null) {
						log.info("Attribute not found: " + key);
						continue;
					}
					row.put("classId", aclass.getName());
					row.put("attrId", attrName);
					String roleLabel="";
					try {
					List<String> rolesIds=(List<String>) tempEntry.get("roleList");
					DBHandler dbh = new DBHandler();
					Map<String, HashMap> rolesFromDB=(Map<String, HashMap>) dbh.handleDBRequest("getRoleLable", null, false);
					HashMap<String, Object> roleMap=rolesFromDB.get("roleLabel");
					List<String> roleLables=new ArrayList<String>();
 					for(int j=0; j<rolesIds.size(); j++)
					{
						if(roleMap.containsKey(rolesIds.get(j)))
						{
							roleLables.add((String) roleMap.get(rolesIds.get(j)));
						}
					}
 					
 					roleLabel=roleLables.get(0);
					for(int k1=1; k1<roleLables.size(); k1++)
						roleLabel=roleLabel+"; "+roleLables.get(k1);
					
 					}
					catch(Exception e)
					{
						log.debug("Error while getting roles: "+ e.getMessage());
						log.debug("Entry added in failed Entries");
						failedEntries.add(tempEntry);
						roleLabel="";
						continue;
					}
					row.put("roles",roleLabel );
					String statuses=(String) tempEntry.get("statuses");
					String workflow_lifecycle=(String) tempEntry.get("workflow_lifecycle");
					log.info("Before workflow_lifecycle: "+workflow_lifecycle);
					log.info("Before statuses: "+statuses);
					try {
						
						if(workflow_lifecycle.equals("Lifecycles"))
						{
							workflow_lifecycle="";
							
							if(statuses.equals("All Statuses"))
							{
								statuses="All Lifecycles";
							}
						}
					}
					catch (Exception e)
					{
						log.debug("Workflow/lifecycle is empty");
					}
					log.info("After workflow_lifecycle: "+workflow_lifecycle);
					log.info("After statuses: "+statuses);
					row.put("statuses",statuses);
					row.put("workflow_lifecycle", workflow_lifecycle);
					row.put("action", "inserted");
					insertCount++;
					rows.add(row);
				}
				
				for(int i=0; i<updatedEntries.size(); i++)
				{
					HashMap<String, Object> tempEntry=updatedEntries.get(i);
					JSONObject row= new JSONObject();
					String classId = (String) tempEntry.get("classId");
					String attrId = (String) tempEntry.get("attrId");
					IAgileClass aclass = null;
					if(aclass_cache.containsKey(classId)) {
						aclass = aclass_cache.get(classId);
					} else {
						aclass = admin.getAgileClass(Integer.valueOf(classId));
						aclass_cache.put(classId, aclass);
					}
					if(aclass == null) {
						log.info("Class not found: " + classId);
						continue;
					}
					String attrName = null;
					String key = classId + "." + attrId;
					if(attr_cache.containsKey(key)) {
						attrName = attr_cache.get(key);
					} else {
						IAttribute iattr = null;
						try {
							iattr = aclass.getAttribute(Integer.valueOf(attrId));
						} catch(Exception ex) {
							iattr = null;
						}
						if(iattr != null) {
							attrName = iattr.getFullName();
						}
						attr_cache.put(key, attrName);
					}
					if(attrName == null) {
						log.info("Attribute not found: " + key);
						continue;
					}
					row.put("classId", aclass.getName());
					row.put("attrId", attrName);
					String roleLabel="";
					try {
					List<String> rolesIds=(List<String>) tempEntry.get("roleList");
					DBHandler dbh = new DBHandler();
					Map<String, HashMap> rolesFromDB=(Map<String, HashMap>) dbh.handleDBRequest("getRoleLable", null, false);
					HashMap<String, Object> roleMap=rolesFromDB.get("roleLabel");
					List<String> roleLables=new ArrayList<String>();
 					for(int j=0; j<rolesIds.size(); j++)
					{
						if(roleMap.containsKey(rolesIds.get(j)))
						{
							roleLables.add((String) roleMap.get(rolesIds.get(j)));
						}
					}
 					
 					roleLabel=roleLables.get(0);
					for(int k1=1; k1<roleLables.size(); k1++)
						roleLabel=roleLabel+"; "+roleLables.get(k1);
					
 					}
					catch(Exception e)
					{
						log.debug("Error while getting roles: "+ e.getMessage());
						log.debug("Entry added in failed Entries");
						failedEntries.add(tempEntry);
						roleLabel="";
					}
					row.put("roles", roleLabel);
					String statuses=(String) tempEntry.get("statuses");
					String workflow_lifecycle=(String) tempEntry.get("workflow_lifecycle");
					log.info("Before workflow_lifecycle: "+workflow_lifecycle);
					log.info("Before statuses: "+statuses);
					try {
						
						if(workflow_lifecycle.equals("Lifecycles"))
						{
							workflow_lifecycle="";
							
							if(statuses.equals("All Statuses"))
							{
								statuses="All Lifecycles";
							}
						}
					}
					catch (Exception e)
					{
						log.debug("Workflow/lifecycle is empty");
					}
					log.info("After workflow_lifecycle: "+workflow_lifecycle);
					log.info("After statuses: "+statuses);
					row.put("statuses",statuses);
					row.put("workflow_lifecycle", workflow_lifecycle);
					row.put("action", "updated");
					updateCount++;
					rows.add(row);
				}
				
				for(int i=0; i<deletedEntries.size(); i++)
				{
					HashMap<String, Object> tempEntry=deletedEntries.get(i);
					JSONObject row= new JSONObject();
					String classId = (String) tempEntry.get("classId");
					String attrId = (String) tempEntry.get("attrId");
					IAgileClass aclass = null;
					if(aclass_cache.containsKey(classId)) {
						aclass = aclass_cache.get(classId);
					} else {
						aclass = admin.getAgileClass(Integer.valueOf(classId));
						aclass_cache.put(classId, aclass);
					}
					if(aclass == null) {
						log.info("Class not found: " + classId);
						continue;
					}
					String attrName = null;
					String key = classId + "." + attrId;
					if(attr_cache.containsKey(key)) {
						attrName = attr_cache.get(key);
					} else {
						IAttribute iattr = null;
						try {
							iattr = aclass.getAttribute(Integer.valueOf(attrId));
						} catch(Exception ex) {
							iattr = null;
						}
						if(iattr != null) {
							attrName = iattr.getFullName();
						}
						attr_cache.put(key, attrName);
					}
					if(attrName == null) {
						log.info("Attribute not found: " + key);
						continue;
					}
					row.put("classId", aclass.getName());
					row.put("attrId", attrName);
					String roleLabel="";
					try {
					List<String> rolesIds=(List<String>) tempEntry.get("roleList");
					DBHandler dbh = new DBHandler();
					Map<String, HashMap> rolesFromDB=(Map<String, HashMap>) dbh.handleDBRequest("getRoleLable", null, false);
					HashMap<String, Object> roleMap=rolesFromDB.get("roleLabel");
					List<String> roleLables=new ArrayList<String>();
 					for(int j=0; j<rolesIds.size(); j++)
					{
						if(roleMap.containsKey(rolesIds.get(j)))
						{
							roleLables.add((String) roleMap.get(rolesIds.get(j)));
						}
					}
 					
 					roleLabel=roleLables.get(0);
					for(int k1=1; k1<roleLables.size(); k1++)
						roleLabel=roleLabel+"; "+roleLables.get(k1);
					
 					}
					catch(Exception e)
					{
						log.debug("Error while getting roles: "+ e.getMessage());
						roleLabel="";
					}
					row.put("roles", roleLabel);
					String statuses=(String) tempEntry.get("statuses");
					String workflow_lifecycle=(String) tempEntry.get("workflow_lifecycle");
					log.info("Before workflow_lifecycle: "+workflow_lifecycle);
					log.info("Before statuses: "+statuses);
					try {
						
						if(workflow_lifecycle.equals("Lifecycles"))
						{
							workflow_lifecycle="";
							
							if(statuses.equals("All Statuses"))
							{
								statuses="All Lifecycles";
							}
						}
					}
					catch (Exception e)
					{
						log.debug("Workflow/lifecycle is empty");
					}
					log.info("After workflow_lifecycle: "+workflow_lifecycle);
					log.info("After statuses: "+statuses);
					row.put("statuses",statuses);
					row.put("workflow_lifecycle", workflow_lifecycle);
					row.put("action", "deleted");
					deleteCount++;
					rows.add(row);
				}
				for(int i=0; i<failedEntries.size(); i++)
				{
					HashMap<String, Object> tempEntry=failedEntries.get(i);
					JSONObject row= new JSONObject();
					String classId = (String) tempEntry.get("classId");
					String attrId = (String) tempEntry.get("attrId");
					IAgileClass aclass = null;
					if(aclass_cache.containsKey(classId)) {
						aclass = aclass_cache.get(classId);
					} else {
						aclass = admin.getAgileClass(Integer.valueOf(classId));
						aclass_cache.put(classId, aclass);
					}
					if(aclass == null) {
						log.info("Class not found: " + classId);
						continue;
					}
					String attrName = null;
					String key = classId + "." + attrId;
					if(attr_cache.containsKey(key)) {
						attrName = attr_cache.get(key);
					} else {
						IAttribute iattr = null;
						try {
							iattr = aclass.getAttribute(Integer.valueOf(attrId));
						} catch(Exception ex) {
							iattr = null;
						}
						if(iattr != null) {
							attrName = iattr.getFullName();
						}
						attr_cache.put(key, attrName);
					}
					if(attrName == null) {
						log.info("Attribute not found: " + key);
						continue;
					}
					row.put("classId", aclass.getName());
					row.put("attrId", attrName);
					String roleLabel="";
					try {
					List<String> rolesIds=(List<String>) tempEntry.get("roleList");
					DBHandler dbh = new DBHandler();
					Map<String, HashMap> rolesFromDB=(Map<String, HashMap>) dbh.handleDBRequest("getRoleLable", null, false);
					HashMap<String, Object> roleMap=rolesFromDB.get("roleLabel");
					List<String> roleLables=new ArrayList<String>();
 					for(int j=0; j<rolesIds.size(); j++)
					{
						if(roleMap.containsKey(rolesIds.get(j)))
						{
							roleLables.add((String) roleMap.get(rolesIds.get(j)));
						}
					}
 					
 					roleLabel=roleLables.get(0);
					for(int k1=1; k1<roleLables.size(); k1++)
						roleLabel=roleLabel+"; "+roleLables.get(k1);
					
 					}
					catch(Exception e)
					{
						log.debug("Error while getting roles: "+ e.getMessage());
						roleLabel="(Role is invalid or not configured in the Assist+ admin panel)";
						//continue;
					}
					row.put("roles",roleLabel );
					String statuses=(String) tempEntry.get("statuses");
					String workflow_lifecycle=(String) tempEntry.get("workflow_lifecycle");
					log.info("Before workflow_lifecycle: "+workflow_lifecycle);
					log.info("Before statuses: "+statuses);
					try {
						
						if(workflow_lifecycle.equals("Lifecycles"))
						{
							workflow_lifecycle="";
							
							if(statuses.equals("All Statuses"))
							{
								statuses="All Lifecycles";
							}
						}
					}
					catch (Exception e)
					{
						log.debug("Workflow/lifecycle is empty");
					}
					log.info("After workflow_lifecycle: "+workflow_lifecycle);
					log.info("After statuses: "+statuses);
					row.put("statuses",statuses);
					row.put("workflow_lifecycle", workflow_lifecycle);
					row.put("action", "failed");
					failedCount++;
					rows.add(row);
				}
				stats.put("stats", rows);
				
				
				
				ReturnStatus retStatus=null;
				if(status.equals("true")) {
					gsonObj.put("stats",stats);
					gsonObj.put("insertCount", insertCount);
					gsonObj.put("updateCount", updateCount);
					gsonObj.put("deleteCount", deleteCount);
					gsonObj.put("failedCount", failedCount);
					
					if(rolesCheck.equals("okay") && classesCheck.equals("okay")) {
						retStatus=new ReturnStatus("success", "Database Imported Successfully");
						gsonObj.put("retStatus",retStatus);
					} else if(!rolesCheck.equals("okay") && !classesCheck.equals("okay")) {
						retStatus=new ReturnStatus("attention", "Assist Text entries after successful import contain roles/usergroups/classes not defined on the destination server, kindly edit the entries");
						gsonObj.put("retStatus",retStatus);
						} else if(!rolesCheck.equals("okay")) {
						retStatus=new ReturnStatus("attention", "Assist Text entries after successful import contain roles/usergroups not defined on the destination server, kindly edit the entries");
						gsonObj.put("retStatus",retStatus);
					} else if(!classesCheck.equals("okay")) {
						retStatus=new ReturnStatus("attention", "Assist Text entries after successful import contain classes/attributes not defined on the destination server, some entries may not have not been imported");
						gsonObj.put("retStatus",retStatus);
						
					}

					log.info("Database Imported..");
				} else {
					if(hasError.equals("true")) {
						if(accessType.equals("okay")) {
							retStatus=new ReturnStatus("error", "Importing Database Failed. Invalid XML");
							gsonObj.put("retStatus",retStatus);
						} else if(accessType.equals("roles")) {
							retStatus=new ReturnStatus("error", "Importing Database Failed. Invalid XML as RolesPriority is expected.");
							gsonObj.put("retStatus",retStatus);
						} else if(accessType.equals("usergroups")) {
							retStatus=new ReturnStatus("error", "Importing Database Failed. Invalid XML as UserGroupPriority is expected.");
							gsonObj.put("retStatus",retStatus);
						}
					} else {
						retStatus=new ReturnStatus("error", "Importing Database Failed");
						gsonObj.put("retStatus",retStatus);
					}
					log.info("Importing Database Failed..");
				}
			} catch(Exception e) {
				log.error("Exception: ", e);
				String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
				json = new Gson().toJson(new ReturnStatus("error", "Error while Importing Database: " + errorMsg));
			}
		} else if(mode.equalsIgnoreCase("export")) {
			try {
				log.info("Exporting Database..");
				XMLHandler xml = new XMLHandler();
				HashMap<String, Boolean> statusMap = xml.exportXML();
				Boolean status = (Boolean) statusMap.get("status");
				if(status) {
					json = new Gson().toJson(new ReturnStatus("success", "Database Exported Successfully"));
					log.info("Database Exported..");
				} else {
					json = new Gson().toJson(new ReturnStatus("error", "Exporting Database Failed"));
					log.info("Exporting Database Failed..");
				}
			} catch(Exception e) {
				log.error("Exception: ", e);
				String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
				json = new Gson().toJson(new ReturnStatus("error", "Error while Exporting Database: " + errorMsg));
			}
		}
		
		if(json.equals(""))
			json=new Gson().toJson(gsonObj);
		
		
		
	
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting DBUtility: doPost..");
	}

}
