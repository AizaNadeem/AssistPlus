package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.agile.api.IAgileSession;
import com.agile.api.UserConstants;
import com.google.gson.Gson;
import com.xavor.ACS.AgileUtils;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class OptOut extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AssistLogger log=AssistLogger.getInstance();
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		DBHandler dbh;
		try {
			dbh = new DBHandler();
			HashMap<String,String> data=(HashMap<String, String>) dbh.handleDBRequest("getOptOutUsers", null, false);
			JSONObject jobj = new JSONObject();
			jobj.put("data", data);
			response.getWriter().write(jobj.toJSONString());
		} catch (Exception e) {
			JSONObject jobj = new JSONObject();
			String json = "Error while getting opt out Information: " + e.getMessage();
			jobj.put("error", json);
			response.getWriter().write(jobj.toJSONString());
		}
		
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering OptOut: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json="";
		try
		{
			DBHandler dbh=new DBHandler();
			String mode =request.getParameter("mode").toString().trim();
			String userid =request.getParameter("userid").toString().trim();
			if(userid==null||userid.equalsIgnoreCase(""))
			{
				Cookie[] cookies = request.getCookies();
				if (cookies != null)
				{
					IAgileSession cookiesSession = AgileUtils.getAgileCookieSession(cookies,ConfigHelper.getProperty(Constants.Config.AgileServerURL));
					/** If user not logged in yet, load nothing **/
					if (cookiesSession != null) 
					{
						userid=cookiesSession.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
					
					}
					else
					{
						return;
					}
				}
			}
			HashMap params=new HashMap<String,String>();
			params.put("userid",userid);
			if(mode.equalsIgnoreCase("on"))
			{
				try{
				dbh.handleDBRequest("addOptOutUser", params, true);
				}
				catch(Exception ex)
				{
					json = new Gson().toJson(new ReturnStatus("error", "user already opted out"));
				}
			}
			else
			{
				dbh.handleDBRequest("removeOptOutUser", params, true);
			}
			json = new Gson().toJson(new ReturnStatus("success", "user previliges changed"));
			response.getWriter().write(json);
		}
		 catch (Exception e) 
			{
			 	json=new Gson().toJson(new ReturnStatus("error", e.getMessage()));
				response.getWriter().write(json);
				log.error("Exception in OptOut: doPost :", e);
				
			}
			log.debug("Exiting OptOut: doPost..");
	}

}
