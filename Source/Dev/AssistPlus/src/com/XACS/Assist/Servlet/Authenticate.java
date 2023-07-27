package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.ActHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Util.AssistLogger;
import com.google.gson.Gson;

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
		String mode = request.getParameter("mode");
		
		log.debug("mode: "+mode);
		
		if (mode.equalsIgnoreCase("login")) {
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
					dbh.handleDBRequest("closeDB", null, false);
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
				json = new Gson().toJson(new ReturnStatus("success", "User authentication successful", responseJson));
				
			}
			
		} else if (mode.equalsIgnoreCase("changepwd")) {
			String uid = request.getParameter("uid");
			String cpwd = request.getParameter("cpwd");
			String npwd = request.getParameter("npwd");
			DBHandler dbh=null;
			try 
			{
				dbh = new DBHandler();
				HashMap params=new HashMap<String,String>();
				params.put("uid",uid);
				params.put("cpwd",cpwd);
				params.put("npwd",npwd);
				log.info("Changing Password..");
				HashMap statusMap = (HashMap)dbh.handleDBRequest("ChangePassword", params, true);
				Boolean status=(Boolean)statusMap.get("status");
				if (status) {
					json = new Gson().toJson(new ReturnStatus("success", "Password changed successfully"));
					log.info("Password Changed..");
				} else {
					json = new Gson().toJson(new ReturnStatus("error", "Invalid Password"));
					log.info("Changing Password Failed..");
				}
			} catch (Exception e) {
				log.info("Changing Password Failed..");
				log.error("Error while changing password: ", e);

				json = new Gson().toJson(new ReturnStatus("error", "Error while changing password: " + e.getMessage()));
			}
			finally
			{
				if(dbh!=null)
				{
					dbh.handleDBRequest("closeDB", null, false);
				}
			}
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting Authenticate: doPost..");

	}
}
