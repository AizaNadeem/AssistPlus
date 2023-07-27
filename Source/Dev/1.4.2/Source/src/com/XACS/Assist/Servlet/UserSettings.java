package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.Handler.AgileHandler;
import com.XACS.Assist.Util.Constants;
import com.agile.api.IAgileSession;
import com.agile.api.UserConstants;
import com.xavor.ACS.AgileUtils;

public class UserSettings extends HttpServlet 
{
	private static final long	serialVersionUID	= 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		JSONObject json = new JSONObject();
		IAgileSession session=null;
		try 
		{
			//Properties props = Utils.loadPropertyFile(Utils.getAgilePath("/opt/Agile/BRCMPX.properties", "/projects/agiledev1/Agile/BRCMPX.properties"));
			String AgileServerURL = "http://plmflexdev.xavor.com:7001/Agile";//Utils.getValueByKey(props, "AgileURL");
			session = AgileUtils.getRequestSession(request, AgileServerURL);
			
			if (session == null)
			{
				session = AgileUtils.getAgileCookieSession(request.getCookies(), AgileServerURL);
			}
			if (session != null) 
			{
				session.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
				String roles = AgileHandler.getCurrentUserRoles(session);
				json.put("Roles", roles);
				ArrayList<String> classes = AgileHandler.getAllClasses(session);
				json.put("classes", classes);
			}
		} catch (Exception e) 
		{
			json.put("error", e.getMessage());
		}
		finally
		{
			if(session!=null)
			{
				session.close();
			}
		}
		response.getWriter().write(json.toJSONString());
	}
}
