package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.DO.AttributeEntry;
import com.XACS.Assist.DO.CListModel;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.UIListHandler;
import com.google.gson.Gson;

public class AttributeListLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public AttributeListLoader() 
    {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try 
		{	
			String classId = request.getParameter("classId");
			String level = request.getParameter("level");
			
			System.out.println("Class Id=["+classId+"]");
			
			List<AttributeEntry> lstattrs = UIListHandler.getAttributeList(Integer.parseInt(classId), Integer.parseInt(level));
			
			List<CListModel> workflows=UIListHandler.getClassWorkflows(classId);
			
			JSONObject data=new JSONObject();
			
			data.put("attributes", lstattrs);
			data.put("workflows", workflows);
			
			json = new Gson().toJson(new ReturnStatus("info",lstattrs.size() + " Attributes found",data));
		} catch (Exception e)
		{
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	}

}