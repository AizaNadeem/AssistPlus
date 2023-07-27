package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.ActHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class Authenticate
 */
public class Authenticate extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Authenticate() {
		super();
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering Authenticate: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
	
		String username = request.getParameter("une");
		String pwd = request.getParameter("pwd");
		int uid = -1;
		DBHandler dbh = null;
		try {
			dbh = new DBHandler();
			HashMap<String,String> params=new HashMap<String,String>();
			params.put("userName", username);
			params.put("password", pwd);
			uid = (Integer) (dbh.handleDBRequest("authenticate", params, false)).get("userID");
		} catch (Exception e) {
			log.error("Error while authenticating user: " , e);
			json = new Gson().toJson(new ReturnStatus("error", "Error while authenticating user: " + e.getMessage()));
			
			response.getWriter().write(json);
			log.debug("Exiting Authenticate: doPost..");
			return;
		}
		finally
		{
			if(dbh!=null)
			{
				//dbh.handleDBRequest("closeDB", null, false);
			}
		}
		if (uid == -1) {
			json = new Gson().toJson(new ReturnStatus("error", "Invalid User Name / Password. Please try again"));
		} else {
			JSONObject responseJson = new JSONObject();
			responseJson.put("uid", String.valueOf(uid));
			try {
				responseJson.put("licinfo", ActHandler.getActInfo());
			} 
			catch (Exception e) {
				
				log.error("Exception in Authenticate: doPost :Error while getting License Information: ", e);
				
				json = new Gson().toJson(new ReturnStatus("error","Error while getting License Information: " + e.getMessage()));
				response.getWriter().write(json);
				
				log.debug("Exiting Authenticate: doPost..");
				return;
			}
			json = new Gson().toJson(new ReturnStatus("success", "Welcome Admin", responseJson));
			
		}
		
		
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting Authenticate: doPost..");

	}
}
