package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class Authenticate
 */
public class ChangePassword extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ChangePassword() {
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
		log.debug("Entering ChangePasword: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
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
			String errorMsg = (e != null)? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", "Error while changing password: " + errorMsg));
		}
		finally
		{
			if(dbh!=null)
			{
				//dbh.handleDBRequest("closeDB", null, false);
			}
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting ChangePasword: doPost..");

	}
}
