package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.DO.AssistColorEntry;
import com.XACS.Assist.Handler.ActHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Util.Constants;

public class AssistPlus extends HttpServlet 
{
	private static final long	serialVersionUID	= 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		JSONObject jobj = new JSONObject();
		HashMap<String, ArrayList<String>> helpMap = null;
		
		try 
		{
			String classId = request.getParameter("classid").toString().trim();
			System.out.println("ClassID::" + classId);
			if (ActHandler.isLicValid()) 
			{
				if (!classId.equals("undefined")) 
				{
					String rolesList = request.getParameter("roles").toString().trim();
					//System.out.println("Role List::" + rolesList);
					
					String classesList = request.getParameter("classes").toString().trim();
					//System.out.println("Classes List::" + classesList);
					
					ArrayList<String> roles = new ArrayList<String>(), classes = new ArrayList<String>();
					
					Collections.addAll(roles, rolesList.split(";"));
					Collections.addAll(classes, classesList.split(";"));
					
					DBHandler dbh = new DBHandler();
					
					if(Constants.Config.ACCESSTYPEROLE==null)
					{
						Constants.Config.ACCESSTYPEROLE=dbh.getConfigByKey("accessType");
					}
					String allRoleKey="All Roles";
					if(!"roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
					{
						allRoleKey="All User Groups";
					}
					helpMap = dbh.getAssistInfoMap(classes, roles,allRoleKey);
					//jobj.putAll(helpMap);
					
					jobj.put("helpText", helpMap);
					
					dbh.closeDB();
				}
			}
			System.out.println(jobj.toString());
			response.getWriter().write(jobj.toString());
		} catch (Exception e) 
		{
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		}
	}
}
