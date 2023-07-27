package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.Handler.AgileHandler;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;
import com.agile.api.IAgileSession;
import com.agile.api.UserConstants;

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
		String userid=request.getParameter("userid");
		try 
		{	
			System.out.println("GetAssistText Request Received!");
			/** Check cookies and get cookie session if available **/
			Cookie[] cookies = request.getCookies();
			if (cookies != null)
			{
				session = AgileHandler.getAgileSession();//.getAgileCookieSession(request.getCookies(), ConfigHelper.getProperty(Constants.Config.AgileServerURL));
				System.out.println("Agile Cookies Session Created!");
				/** If user not logged in yet, load nothing **/
				if (session != null) 
				{
					//String userid=session.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
					System.out.println("UserID got from js: "+userid);
					String roles = AgileHandler.getCurrentUserRoles(session,userid);
					json.put("Roles", roles);
					System.out.println("Roles: "+roles.toString());
					session.close();
					
					/** Get admin session for loading class attributes */
					session=AgileHandler.getAgileSession();
					
					ArrayList<String> classes = AgileHandler.getAllClasses(session);
					System.out.println("Classes loaded");
					json.put("classes", classes);
				}
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
			json.put("error", e.getMessage());
		}
		finally
		{
			if(session!=null)
			{
				session.close();
			}
		}
		System.out.println("Sending classes and roles data back to agile console");
		response.getWriter().write(json.toJSONString());
	}
}
