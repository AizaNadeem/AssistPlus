package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.agile.api.APIException;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAttribute;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.AssistText;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class SearchAssistText
 */
public class SearchAssistText extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AssistLogger log = AssistLogger.getInstance();
	DBHandler dbh =null;
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering SearchAssistText: doPost...");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String jsonResponse = "";
		HashMap<String, Object> gsonObj=new HashMap<String, Object>();
		//implement search logic
		
		try {
			List<JSONObject> rows = new ArrayList<JSONObject>();
			IAdmin admin = AgileHandler.getAgileSession().getAdminInstance();
			Map<String, IAgileClass> aclass_cache = new HashMap<String, IAgileClass>();
			Map<String, String> attr_cache = new HashMap<String, String>();
			int searchCount=0;
			dbh=new DBHandler();
			String searchText=request.getParameter("text").toString();
			HashMap<String, Object> params=new HashMap<String, Object>();
			params.put("searchText", searchText);
			Map<String, ArrayList<AssistTextEntry>> exEntries=(Map<String, ArrayList<AssistTextEntry>>) dbh.handleDBRequest("getAllSearchedTexts", params,false);
			ArrayList<AssistTextEntry> searchedEntries=(ArrayList<AssistTextEntry>) exEntries.get("existingEntries");
			searchCount=populateLogsData(searchedEntries, rows, admin, aclass_cache, attr_cache, searchCount);
			gsonObj.put("existingEntries", rows);
			gsonObj.put("searchCount", searchCount);
			ReturnStatus retStatus=new ReturnStatus("success","Search is completed");
			gsonObj.put("retStatus",retStatus);
		} catch(Exception ex) {
			log.error("Error occurred while searching:: ", ex);
			String errorMsg = (ex != null) ? ex.getMessage() : "Null";
			ReturnStatus retStatus=new ReturnStatus("error","Error occurred while searching: " + errorMsg);
			gsonObj.put("retStatus",retStatus);
			//jsonResponse = new Gson().toJson(new ReturnStatus("error", "No entries found: " + errorMsg));
		}
		
		
		jsonResponse=new Gson().toJson(gsonObj);
		response.getWriter().write(jsonResponse);
		
		
		log.debug("Exiting SearchAssistText: doPost...");
	}
	private int populateLogsData(java.util.ArrayList<AssistTextEntry> insertedEntries, List<JSONObject> rows,
			IAdmin admin, Map<String, IAgileClass> aclass_cache, Map<String, String> attr_cache, int insertCount)
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
			row.put("textid", tempEntry.getTextID());
			row.put("assistText", tempEntry.getAssistText());
			insertCount++;
			rows.add(row);
		}
		return insertCount;
	
	}

}
