package com.XACS.Assist.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.ActHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.google.gson.Gson;

/**
 * Servlet implementation class Authenticate
 */
public class Authenticate extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Authenticate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String mode = request.getParameter("mode");
		if (mode.equalsIgnoreCase("login")) {
			String username = request.getParameter("une");
			String pwd = request.getParameter("pwd");
			int uid = -1;
			DBHandler dbh = null;
			try {
				dbh = new DBHandler();
				uid = dbh.authenticate(username, pwd);
			} catch (Exception e) {
				json = new Gson().toJson(new ReturnStatus("error", "Error while authenticating user: " + e.getMessage()));
				response.getWriter().write(json);
				return;
			}
			finally
			{
				if(dbh!=null)
				{
					dbh.closeDB();
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
					e.printStackTrace();
					json = new Gson().toJson(new ReturnStatus("error","Error while getting License Information: " + e.getMessage()));
					response.getWriter().write(json);
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
				boolean status = dbh.ChangePassword(uid, cpwd, npwd);
				if (status) {
					json = new Gson().toJson(new ReturnStatus("success", "Password changed successfully"));
				} else {
					json = new Gson().toJson(new ReturnStatus("error", "Invalid Password"));
				}
			} catch (Exception e) {
				json = new Gson().toJson(new ReturnStatus("error", "Error while changing password: " + e.getMessage()));
			}
			finally
			{
				if(dbh!=null)
				{
					dbh.closeDB();
				}
			}
		}
		response.getWriter().write(json);
	}
}
