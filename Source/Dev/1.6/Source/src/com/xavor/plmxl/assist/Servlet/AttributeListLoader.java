

package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.AttributeEntry;
import com.xavor.plmxl.assist.DO.CListModel;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.UIListHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

public class AttributeListLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 AssistLogger log=AssistLogger.getInstance();
    public AttributeListLoader() 
    {
        super();
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering AttributeListLoader: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = "";
		try 
		{	
			String classId = request.getParameter("classId");
			String level = request.getParameter("level");			

			log.debug("Class Id=["+classId+"]");
			
			UIListHandler objUIListHandler=new UIListHandler();
			List<AttributeEntry> lstattrs = objUIListHandler.getAttributeList(Integer.parseInt(classId), Integer.parseInt(level));
			
			List<CListModel> workflows=objUIListHandler.getClassWorkflows(classId,Integer.parseInt(level));
			
			Boolean isRoutable = objUIListHandler.isRoutable();
			
			JSONObject data=new JSONObject();
			
			data.put("attributes", lstattrs);
			data.put("workflows", workflows);
			data.put("isRoutable", isRoutable);
			log.debug("Data: "+data.toString());
			
			json = new Gson().toJson(new ReturnStatus("info",lstattrs.size() + " Attributes found",data));
			
		} catch (Exception e)
		{
			log.error("Exception in AttributeListLoader: doGet..", e);
			
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting AttributeListLoader: doGet..");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	}

}