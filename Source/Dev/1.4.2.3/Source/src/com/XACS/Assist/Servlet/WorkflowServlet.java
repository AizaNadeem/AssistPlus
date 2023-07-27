package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import com.XACS.Assist.DO.BasicModel;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.UIListHandler;
import com.agile.api.APIException;
import com.google.gson.Gson;

public class WorkflowServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
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
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		JSONObject jobj = new JSONObject();
		String action=request.getParameter("action");
		String json="";
		if("loadworkflow".equalsIgnoreCase(action))
		{
			
		try {
			String classId = request.getParameter("classid").toString().trim();
			List workflowList = new UIListHandler().getClassWorkflows(classId);
			json = new Gson().toJson(new ReturnStatus("info", workflowList.size() + " Workflows listed", workflowList));
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
		// String json = new Gson().toJson(lstclasses);
		response.getWriter().write(json);
		}
		if("loadworkflowstatus".equalsIgnoreCase(action))
		{
			try {
				String workflowName = request.getParameter("workflowname").toString().trim();
				List<BasicModel> workflowStatusList = new UIListHandler().getWorkflowStatuses(workflowName);
				JSONObject data=new JSONObject();
				data.put("workflowstatus", workflowStatusList);	
				//json = new Gson().toJson(new ReturnStatus("info",lstattrs.size() + " Attributes found",data));
				//json = new Gson().toJson(new ReturnStatus("info", workflowList.size() + " Workflows listed", data));
				json = new Gson().toJson(new ReturnStatus("info", workflowStatusList.size() + " Workflow Statuses listed", data));
			} catch (Exception e) {
				if (e instanceof APIException) {
					APIException apie = (APIException) e;
					json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
							+ ". Please check configuration."));
				} else {
					json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
				}
			}
			// String json = new Gson().toJson(lstclasses);
			response.getWriter().write(json);

		}

	}
}
