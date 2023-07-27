package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;

import com.agile.api.APIException;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAttribute;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Handler.ExcelHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class ImportExcel
 */
public class ImportExcel extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AssistLogger log = AssistLogger.getInstance();

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ImportExcel: doPost...");
		String jsonResponse = "";
		HashMap<String, Object> gsonObj=new HashMap<String, Object>();
		try {
			String type = null;
			InputStream fileContent = null;
			String fileName = null;
			
			if(ServletFileUpload.isMultipartContent(request)) {
				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				for (FileItem item : items) {
					if (!item.isFormField()) {
						// Process form file field (input type="file")
						fileName = FilenameUtils.getName(item.getName());
						if(fileName.endsWith(".xlsx")) {
							log.info("Importing xlsx file: " + fileName);
							fileContent = item.getInputStream();
						} else {
							throw new Exception("Excel file is invalid [" + fileName + "]");
						}
					} else {
						String attr = item.getFieldName();
						InputStream stream = item.getInputStream();
						if(attr.equals("type")) {
							type = Streams.asString(stream);
						}
					}
				}
			}
			ExcelHandler excelHandler = new ExcelHandler(fileContent);
			setAssistNotification(excelHandler, type, fileContent, gsonObj);
			setAssistClassNotification(excelHandler, type, fileContent, gsonObj);
			setAssistText(excelHandler, type, fileContent, gsonObj);
		} catch(Exception ex) {
			log.error("Error while importing xlsx file: ", ex);
			String errorMsg = (ex != null) ? ex.getMessage() : "Null";
			ReturnStatus retStatus=new ReturnStatus("error", "Error while importing xlsx file: " + errorMsg);
			gsonObj.put("retStatus",retStatus);
			//jsonResponse = new Gson().toJson(new ReturnStatus("error", "Error while importing xlsx file: " + errorMsg));
		}
		
		response.setContentType("application/json");
		String json=new Gson().toJson(gsonObj);
		response.getWriter().write(json);
		log.debug(json);
		log.debug("Exiting ImportExcel: doPost...");
	}

	private void setAssistText(ExcelHandler excelHandler, String type, InputStream fileContent, HashMap<String, Object> gsonObj) throws Exception {
		if(type != null && fileContent != null) {
			log.info("Import Type: " + type);
			HashMap<String, Object> statusMap = excelHandler.importExcelFile(type);
			
			//transforming lists into json rows
			java.util.ArrayList<AssistTextEntry> insertedEntries=(java.util.ArrayList<AssistTextEntry> ) statusMap.get("insertedEntries");
			java.util.ArrayList<AssistTextEntry> updatedEntries=(java.util.ArrayList<AssistTextEntry> ) statusMap.get("updatedEntries");
			java.util.ArrayList<AssistTextEntry> deletedEntries=(java.util.ArrayList<AssistTextEntry> ) statusMap.get("deletedEntries");
			java.util.ArrayList<AssistTextEntry> failedEntries=(java.util.ArrayList<AssistTextEntry> ) statusMap.get("failedEntries");
			Map<String, List<JSONObject>> stats = new HashMap<String, List<JSONObject>>();
			
			List<JSONObject> rows = new ArrayList<JSONObject>();
			IAdmin admin = AgileHandler.getAgileSession().getAdminInstance();
			Map<String, IAgileClass> aclass_cache = new HashMap<String, IAgileClass>();
			Map<String, String> attr_cache = new HashMap<String, String>();
			int insertCount=0, updateCount=0, deleteCount=0, failedCount=0;
			insertCount = populateLogsData(insertedEntries, rows, admin, aclass_cache, attr_cache, insertCount,"inserted" );
			updateCount = populateLogsData(updatedEntries, rows, admin, aclass_cache, attr_cache, updateCount,"updated" );
			deleteCount = populateLogsData(deletedEntries, rows, admin, aclass_cache, attr_cache, deleteCount,"deleted" );
		
			for(int i=0; i<failedEntries.size(); i++)
			{
				AssistTextEntry tempEntry=failedEntries.get(i);
				JSONObject row= new JSONObject();
				
				row.put("classId", tempEntry.getClassName());
				row.put("attrId", tempEntry.getAtrrName());
//				String statuses="";
//				try {
//				List<String> allStatuses=tempEntry.getWorkflowStatuses();
//				statuses=allStatuses.get(0);
//				for(int k1=1; k1<allStatuses.size(); k1++)
//					statuses=statuses+";"+allStatuses.get(k1);
//				}
//				catch(Exception e)
//				{
//					statuses="";
//				}
				row.put("statuses",tempEntry.getWorkflowStatusId());
				row.put("workflow_lifecycle", tempEntry.getWorkflowID());
				
				String roles="";
				try {
				List<String> allRoles=tempEntry.getRolesList();
				roles=allRoles.get(0);
				for(int k1=1; k1<allRoles.size(); k1++)
					roles=roles+";"+allRoles.get(k1);
				}
				catch(Exception e)
				{
					roles="";
				}
				row.put("roles", roles);
				
				row.put("action", "failed");
				failedCount++;
				rows.add(row);
			}
			
			stats.put("stats", rows);
			gsonObj.put("stats",stats);
			gsonObj.put("insertCount", insertCount);
			gsonObj.put("updateCount", updateCount);
			gsonObj.put("deleteCount", deleteCount);
			gsonObj.put("failedCount", failedCount);
			
			
			String result="Import completed. Successful Rows: " + statusMap.get("success") + ", Errors: " + statusMap.get("errors");
			
			ReturnStatus retStatus=new ReturnStatus("success", result);
			gsonObj.put("retStatus", retStatus);
			//jsonResponse = new Gson().toJson(new ReturnStatus("success", result));
		} else {
			log.error("Invalid file or import type. Type: " + type + ", Content: " + fileContent);
			ReturnStatus retStatus=new ReturnStatus("error", "Invalid data. Please try again.");
			gsonObj.put("retStatus",retStatus);
		}
	
	}
	
	private void setAssistNotification(ExcelHandler excelHandler, String type, InputStream fileContent, HashMap<String, Object> gsonObj) throws Exception {
		if(type != null && fileContent != null) {
			log.info("Import Type: " + type);
			excelHandler.importNotificationExcelFile(type);
		} else {
			log.error("Invalid file or import type. Type: " + type + ", Content: " + fileContent);
			ReturnStatus retStatus=new ReturnStatus("error", "Invalid data. Please try again.");
			gsonObj.put("retStatus",retStatus);
		}
	}
	
	private void setAssistClassNotification(ExcelHandler excelHandler, String type, InputStream fileContent, HashMap<String, Object> gsonObj) throws Exception {
		if(type != null && fileContent != null) {
			log.info("Import Type: " + type);
			excelHandler.importClassExcelFile(type);
		} else {
			log.error("Invalid file or import type. Type: " + type + ", Content: " + fileContent);
			ReturnStatus retStatus=new ReturnStatus("error", "Invalid data. Please try again.");
			gsonObj.put("retStatus",retStatus);
		}
	}

	
	private int populateLogsData(java.util.ArrayList<AssistTextEntry> insertedEntries, List<JSONObject> rows,
			IAdmin admin, Map<String, IAgileClass> aclass_cache, Map<String, String> attr_cache, int insertCount, String action)
			throws APIException {
		for(int i=0; i<insertedEntries.size(); i++)
		{
			AssistTextEntry tempEntry=insertedEntries.get(i);
			JSONObject row= new JSONObject();
			String classId = (String) tempEntry.getClassID();
			String attrId = (String) tempEntry.getAttrID();
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
			List<String> rolesIds=(List<String>) tempEntry.getRolesList();
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
				continue;
			}
			row.put("roles",roleLabel );
			List<String> statuseList=(List<String>) tempEntry.getWorkflowStatuses();
			String statuses=tempEntry.getWorkflowStatusId();
			String workflow_lifecycle=(String) tempEntry.getWorkflowID();
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
			row.put("action", action);
			insertCount++;
			rows.add(row);
		}
		return insertCount;
	}
}
