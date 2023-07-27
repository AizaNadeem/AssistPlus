package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.agile.api.APIException;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.BasicModel;
import com.xavor.plmxl.assist.DO.CListModel;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.UIListHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

public class WorkflowServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 AssistLogger log=AssistLogger.getInstance();
       
    public WorkflowServlet() 
    {
        super();
       
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering WorkflowServlet:doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String action=request.getParameter("action");
		log.debug("Action: "+action);
		String json="";
		if("loadworkflow".equalsIgnoreCase(action))
		{
			
		try {
			String classId = request.getParameter("classid").toString().trim();
			log.debug("classId: "+classId);
			List<CListModel> workflowList = new UIListHandler().getClassWorkflows(classId);
			log.debug(" Workflow List: "+workflowList);
			json = new Gson().toJson(new ReturnStatus("info", workflowList.size() + " Workflows listed", workflowList));
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				log.error("System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration.", apie);
				
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				log.error("Exception in WorkflowServlet:doPost ", e);
				
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
		
		response.getWriter().write(json);
		}
		if("loadworkflowstatus".equalsIgnoreCase(action))
		{
			try {
				String workflowName = request.getParameter("workflowname").toString().trim();
				log.debug("Workflow Name: "+workflowName);
				List<BasicModel> workflowStatusList = new UIListHandler().getWorkflowStatuses(workflowName);
				JSONObject data=new JSONObject();
				data.put("workflowstatus", workflowStatusList);	
				json = new Gson().toJson(new ReturnStatus("info", workflowStatusList.size() + " Workflow Statuses listed", data));
			} catch (Exception e) {
				if (e instanceof APIException) {
					APIException apie = (APIException) e;
					log.error("System is unable to connect to Agile due to " + apie.getMessage()
							+ ". Please check configuration.", apie);
					
					json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
							+ ". Please check configuration."));
				} else {
					log.error("Exception in WorkflowServlet:doPost ", e);
					
					json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
				}
			}
			response.getWriter().write(json);
		}
		
		log.debug(json.toString());
		log.debug("Exiting WorkflowServlet:doPost..");

	}
}
