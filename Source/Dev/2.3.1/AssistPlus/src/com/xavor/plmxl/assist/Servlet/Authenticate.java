package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();
	
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
	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBHandler dbh;
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		Map<String,String> result = null;
		try {
			dbh = new DBHandler();
			result = (HashMap<String,String>) dbh.handleDBRequest("getUserName", null, false);
		} catch (Exception e) {
			log.error("Exception while handling DB Request 'getUserName': ", e);
		}
		
		String json =  new Gson().toJson(result);
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering Authenticate: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
	
		String username = request.getParameter("une");
		String pwd = request.getParameter("pwd");
		int uid = -1;
		boolean isAdminUser = false;
		DBHandler dbh = null;
		try {
			dbh = new DBHandler();
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("userName", username);
			params.put("password", pwd);
			Map<String, Object> result = (Map<String, Object>) dbh.handleDBRequest("authenticate", params, false);
			uid = (Integer) result.get("userID");
			isAdminUser = (Boolean) result.get("isAdminUser");
		} catch (Exception e) {
			log.error("Error while authenticating user: " , e);
			String errorMsg = (e != null)? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", "Error while authenticating user: " + errorMsg));
			
			response.getWriter().write(json);
			log.debug("Exiting Authenticate: doPost..");
			return;
		} finally {
			/*if(dbh != null) {
				dbh.handleDBRequest("closeDB", null, false);
			}*/
		}
		
		if (uid == -1) {
			json = new Gson().toJson(new ReturnStatus("error", "Invalid User Name / Password. Please try again"));
		} else {
			JSONObject responseJson = new JSONObject();
			responseJson.put("uid", String.valueOf(uid));
			responseJson.put("isAdminUser", isAdminUser);
			try {
				responseJson.put("licinfo", ActHandler.getActInfo());
			} catch (Exception e) {
				log.error("Exception in Authenticate: doPost :Error while getting License Information: ", e);
				String errorMsg = (e != null)? "" + e.getMessage() : "Null";
				json = new Gson().toJson(new ReturnStatus("error","Error while getting License Information: " + errorMsg));
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
