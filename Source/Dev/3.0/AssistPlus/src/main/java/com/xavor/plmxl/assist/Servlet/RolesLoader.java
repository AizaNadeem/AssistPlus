package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agile.api.APIException;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Handler.UIListHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.Constants;

public class RolesLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering RolesLoader:doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			List<RoleEntry> lstRoles = new UIListHandler().getRoleList();
			String returnMsg = "roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE) ? "Role Preferences loaded" : "User Group Preferences loaded";

			json = new Gson().toJson(new ReturnStatus("info", returnMsg, lstRoles));
		} catch(Exception e) {
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			if(e instanceof APIException) {
				APIException apie = (APIException) e;
				log.error("System is unable to connect to Agile. Please check configuration.", apie);

				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + errorMsg
						+ ". Please check configuration."));
			} else {
				log.error("Exception in RolesLoader:doGet ", e);

				json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			}
		}

		log.debug(json);
		response.getWriter().write(json);
		log.debug("Exiting RolesLoader:doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering RolesLoader:doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String flag = null;
		String[] roles = request.getParameterValues("roles[]");

		if(request.getParameter("flag1") != null) {
			flag = request.getParameter("flag1");
		}

		ArrayList<RoleEntry> roleEntries = new ArrayList<RoleEntry>();
		RoleEntry role = null;
		for(String roleStr : roles) {
			role = new RoleEntry();
			String[] roleParts = roleStr.split("<ri>");
			role.setPriority(Integer.valueOf(roleParts[0]));
			role.setRole(roleParts[1]);
			role.setRoleID(Integer.parseInt(roleParts[2]));
			role.setFontColor(roleParts[3]);
			role.setBackgroundColor(roleParts[4]);
			roleEntries.add(role);
		}

		log.debug(" RoleEntries: " + roleEntries.toString());
		DBHandler dbh = null;
		try {
			dbh = new DBHandler();
			HashMap<String, ArrayList<RoleEntry>> params = new HashMap<String, ArrayList<RoleEntry>>();
			params.put("roleList", roleEntries);
			if(flag != null) {
				log.info("Removing Role..");
			}
			dbh.handleDBRequest("updateRolePriority", params, true);
			if(flag != null) {
				log.info("Role Removed..");
			}
			String returnMsg = "roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE) ? "Role Preferences saved" : "User Group Preferences saved";

			json = new Gson().toJson(new ReturnStatus("success", returnMsg));
		} catch(Exception e) {
			log.error("DBError: ", e);
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", "DBError: " + errorMsg));
		}
		
		log.debug(json.toString());
		response.getWriter().write(json);
		log.debug("Exiting RolesLoader:doPost..");
	}
}
