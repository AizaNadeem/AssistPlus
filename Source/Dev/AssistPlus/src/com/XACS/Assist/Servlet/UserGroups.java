
package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.UIListHandler;
import com.XACS.Assist.Util.AssistLogger;
import com.agile.api.APIException;
import com.google.gson.Gson;

public class UserGroups extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserGroups() 
	{
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering UserGroups:doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			List<RoleEntry> lstRoles = UIListHandler.getRoleList();
			json = new Gson().toJson(new ReturnStatus("info", "Role Preferences loaded", lstRoles));
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				log.error("System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration.", apie);
				
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				log.error("Exception in UserGroups:doGet " , e);
				
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
	
		log.debug(json.toString());
		response.getWriter().write(json);
		log.debug("Exiting UserGroups:doGet..");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering UserGroups:doPost..");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String[] roles = request.getParameterValues("roles[]");
		ArrayList<RoleEntry> roleEntries = new ArrayList<RoleEntry>();
		RoleEntry role = null;
		for (String roleStr : roles) {
			role = new RoleEntry();
			String[] roleParts = roleStr.split(":");
			role.setPriority(Integer.valueOf(roleParts[0]));
			role.setRole(roleParts[1]);
			role.setRoleID(roleParts[2]);
			role.setFontColor(roleParts[3]);
			role.setBackgroundColor(roleParts[4]);
			roleEntries.add(role);
		}
		log.debug("Role Entries: "+ roleEntries.toString());
		DBHandler dbh =null;
		try 
		{
			dbh = new DBHandler();
			HashMap<String, ArrayList<RoleEntry>> params=new HashMap<String,ArrayList<RoleEntry>>();
			params.put("roleList", roleEntries);
			dbh.handleDBRequest("updateRolePriority", params, true);
			json = new Gson().toJson(new ReturnStatus("success", "Role Preferences saved"));
		} catch (Exception e) {
			log.error("DBError: " , e);
		
			json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
		}
		finally
		{
			if(dbh!=null)
			{
				dbh.handleDBRequest("closeDB", null, false);
			}
		}
		log.debug(json.toString());
		response.getWriter().write(json);
		log.debug("Exiting UserGroups:doPost..");

	}
}
