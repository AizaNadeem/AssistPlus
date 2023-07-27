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
import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;
import com.agile.api.IAgileSession;
import com.agile.api.UserConstants;

public class UserSettings extends HttpServlet 
{
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering UserSettings:doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		JSONObject json = new JSONObject();
		IAgileSession session=null;
		try 
		{
			/** Check cookies and get cookie session if available **/
			Cookie[] cookies = request.getCookies();
			if (cookies != null)
			{
				session = AgileHandler.getAgileCookieSession(request.getCookies(), ConfigHelper.getProperty(Constants.Config.AgileServerURL));
				/** If user not logged in yet, load nothing **/
				if (session != null) 
				{
					session.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
					log.debug("Current User"+session.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString());
					String roles = AgileHandler.getCurrentUserRoles(session);
					json.put("Roles", roles);
					log.debug("Roles: "+ roles);
					session.close();
					
					/** Get admin session for loading class attributes */
					session=AgileHandler.getAgileSession();
					
					ArrayList<String> classes = AgileHandler.getAllClasses(session);
					json.put("classes", classes);
				}
			}
		} catch (Exception e) 
		{
			log.error("Exception in UserSettings:doGet " , e);
			
			json.put("error", e.getMessage());
		}
		finally
		{
			if(session!=null)
			{
				session.close();
			}
		}
		log.debug(json.toString());
		response.getWriter().write(json.toJSONString());
		log.debug("Exiting UserSettings:doGet..");

	}
}
