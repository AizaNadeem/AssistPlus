package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Handler.UIListHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class ClassLoader
 */
public class AssistTextListLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AssistTextListLoader() {
		super();
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AssistTextListLoader: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			String classId = request.getParameter("classId");
			String attrId = request.getParameter("attrId");
			String isRoutable = request.getParameter("isRoutable");
			log.debug("classId: ["+classId+"], attrId: ["+attrId+"]");
			List<AssistTextEntry> lsttexts = new UIListHandler().getAssistTextList(classId, attrId, isRoutable);
			json = new Gson().toJson(new ReturnStatus("info", lsttexts.size() + " Assist Text entries found", lsttexts));
		
		} catch (Exception e) {
			
			log.error("Exception in AssistTextListLoader: doGet: ", e);
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
	
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting AssistTextListLoader: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AssistTextListLoader: doPost..");
		String json = "";
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		String textId = request.getParameter("textId");
		String assistText = request.getParameter("assistText");

		log.debug("AssistText"+assistText);
		try {
			String mode = request.getParameter("mode");
			log.debug("Mode: "+mode);
			if (mode.equalsIgnoreCase("new")) 
			{
				json=modeNew(request);
			} 
			if (mode.equalsIgnoreCase("save")) 
			 {
				if(textId.equalsIgnoreCase("-1"))
				{
					json=addNewRow(request);
				}
				else if (!assistText.equals(""))
				{
					json=updateRow(request);
				} 
				else 
				{
					json = new Gson().toJson(new ReturnStatus("error", "Empty Assist Text Row"));
				}
			}  
			else if (mode.equalsIgnoreCase("remove")) 
			{
				json=deleteRow(request);
			}
		} catch (Exception e) {
			log.error("ServletError: " , e);
			json = new Gson().toJson(new ReturnStatus("error", "ServletError: " + e.getMessage()));
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting AssistTextListLoader: doPost..");
	}
	public String addNewRow(HttpServletRequest request)
	{
		DBHandler dbh = null;
		String fontColor = "", backgroundColor = "",json="";
		String classId = request.getParameter("classId");
		String attrId = request.getParameter("attrId");
		String textId = request.getParameter("textId");
		String assistText = request.getParameter("assistText");
		String isRoutable = request.getParameter("isRoutable");
		

		
		log.debug(assistText);
		
		String[] roles = new String[] {};
		roles = request.getParameterValues("roles[]");
		String workflowID=request.getParameter("workflowname");
		String workflowStatusID=request.getParameter("workflowstatus");
		
		
		//This section has been added to cater to classes that have lifecycles
		//The aim of manipulating data here is that so DB structure is not changed
		//The workflowID is set to Lifecycles. When a request comes in AssistPlus servlet, "Lifecycles" is also set as WorkflowID
		//For statuses, if the input is "All Lifecycles" then that is changed to "All Statuses" as DB handles the All Statuses case 
		//The workflowID becomes workflowStatusID to complete the manipulation of the data. 
		
		if(isRoutable != null && isRoutable.equalsIgnoreCase("false")){
			workflowStatusID = workflowID;
			if(workflowStatusID.equalsIgnoreCase("All Lifecycles")){
				workflowStatusID = "All Statuses";
			}
			workflowID = "Lifecycles";
		}
		
		fontColor = request.getParameter("fontcolor");
		backgroundColor = request.getParameter("backgroundcolor");
		boolean isDiffColor = Boolean.parseBoolean(request.getParameter("isDiffColor"));
		log.debug("Workflow Status: ["+workflowStatusID+"],Roles: ["+roles+"], Workflow Name: ["+workflowID+"],Text Id: ["+textId+"], Assist Text: ["+assistText+"],Class Id: ["+classId+"], Attr Id: ["+attrId+"]");
		try 
		{
			dbh = new DBHandler();
			HashMap params=new HashMap<String,Object>();
			params.put("classID",classId);
			params.put("attrID",attrId);
			params.put("assistText",assistText);
			params.put("fontColor",fontColor);
			params.put("backgroundColor",backgroundColor);
			params.put("isDiffColor",isDiffColor);
			params.put("workflowID",workflowID);
			params.put("workflowStatusID",workflowStatusID);
			params.put("roleList", roles);
			HashMap result = dbh.handleDBRequest("addNewAssistText", params, true);
			int intTextId=(Integer) result.get("textId");
			textId=Integer.toString(intTextId);
			json = new Gson().toJson(new ReturnStatus("success", "Assist Text saved"));
			log.info("Assist Text Saved");
			log.debug(json.toString());
			//dbh.handleDBRequest("closeDB", null, false);
			return json;
		} catch (Exception e) 
		{
			log.error( "DBError: ", e);
			
			json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
			log.debug(json.toString());
			//dbh.handleDBRequest("closeDB", null, false);
			return json;
		}
		
	}
	private String updateRow(HttpServletRequest request)
	{
		DBHandler dbh = null;
		String fontColor = "", backgroundColor = "",json="";
		String classId = request.getParameter("classId");
		String attrId = request.getParameter("attrId");
		String textId = request.getParameter("textId");
		String assistText = request.getParameter("assistText");
		String isRoutable = request.getParameter("isRoutable");
		String[] roles = new String[] {};
		roles = request.getParameterValues("roles[]");
		String workflowID=request.getParameter("workflowname");
		String workflowStatusID=request.getParameter("workflowstatus");
		fontColor = request.getParameter("fontcolor");
		backgroundColor = request.getParameter("backgroundcolor");
		boolean isDiffColor = Boolean.parseBoolean(request.getParameter("isDiffColor"));
		
		if(isRoutable != null && isRoutable.equalsIgnoreCase("false")){
			workflowStatusID = workflowID;
			if(workflowStatusID.equalsIgnoreCase("All Lifecycles")){
				workflowStatusID = "All Statuses";
			}
			workflowID = "Lifecycles";
		}
		
		log.debug("Workflow Status: ["+workflowStatusID+"],Roles: ["+roles+"], Workflow Name: ["+workflowID+"],Text Id: ["+textId+"], Assist Text: ["+assistText+"],Class Id: ["+classId+"], Attr Id: ["+attrId+"]");


		try 
		{
			dbh = new DBHandler();
		} 
		catch (Exception e) 
		{
			json = new Gson().toJson(new ReturnStatus("error", "ServletError: " + e.getMessage()));
		}
		HashMap params=new HashMap<String,Object>();
		params.put("textID",textId);
		params.put("assistText",assistText);
		params.put("fontColor",fontColor);
		params.put("backgroundColor",backgroundColor);
		params.put("isDiffColor",isDiffColor);
		params.put("workflowID",workflowID);
		params.put("workflowStatusID",workflowStatusID);
		params.put("roleList", roles);
		dbh.handleDBRequest("updateAssistText", params, true);
		json = new Gson().toJson(new ReturnStatus("success", "Assist Text saved"));
		log.info("Assist Text Saved");
		//dbh.handleDBRequest("closeDB", null, false);
		return json;
	
	}
	private String deleteRow(HttpServletRequest request) throws Exception
	{
		String textId = request.getParameter("textId");
		String json="";
		DBHandler dbh = new DBHandler();
		try {
			
			HashMap params=new HashMap<String,String>();
			params.put("textID", textId);
			dbh.handleDBRequest("removeAssistText", params, true);
			json = new Gson().toJson(new ReturnStatus("success", "Assist Text removed"));
			
			log.info("Assist Text removed");
			//dbh.handleDBRequest("closeDB", null, false);
			return json;
		} catch (Exception e) {
			log.error("DBError: ", e);
			json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
			//dbh.handleDBRequest("closeDB", null, false);
			return json;
		}
	}
	private String modeNew(HttpServletRequest request)
	{
		String classId = request.getParameter("classId");
		String attrId = request.getParameter("attrId");
		String json="";
		log.debug("classId: ["+classId+"], attrId: ["+attrId+"]");
		
		int textID = -1;
		String[] roles = {};
		HashMap map;
		DBHandler dbh = null;
		try {
			dbh = new DBHandler();
			map = dbh.handleDBRequest("getRoleOptions", null, false);
			roles=(String[]) map.get("strRoles");
			log.debug(roles.toString());
		} catch (Exception e) 
		{
			log.error( "DBError: " , e);
			json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
			return json;
		}
		finally
		{
			if(dbh!=null)
			{
				//dbh.handleDBRequest("closeDB", null, false);
			}
		}
		AssistTextEntry ate = new AssistTextEntry();
		ate.setTextID(String.valueOf(textID));
		ate.setAssistText("");
		ate.setAttrID(attrId);
		ate.setClassID(classId);
		ate.setRoles(roles);
		ate.setLastUpdated("");
		json = new Gson().toJson(new ReturnStatus("success", "New Assist Text added", ate));
		return json;
	}
}
