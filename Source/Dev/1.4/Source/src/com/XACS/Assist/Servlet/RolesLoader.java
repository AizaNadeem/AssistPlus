package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.DO.RoleEntry;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.UIListHandler;
import com.agile.api.APIException;
import com.google.gson.Gson;

/**
 * Servlet implementation class RolesLoader
 */
public class RolesLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RolesLoader() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			List<RoleEntry> lstRoles = UIListHandler.getRoleList();
			json = new Gson().toJson(new ReturnStatus("info", "Role Preferences loaded", lstRoles));
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
		// String json = new Gson().toJson(lstRoles);
		response.getWriter().write(json);
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
		try {
			DBHandler dbh = new DBHandler();
			dbh.updateRolePriority(roleEntries);
			json = new Gson().toJson(new ReturnStatus("success", "Role Preferences saved"));
		} catch (Exception e) {
			json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
		}
		response.getWriter().write(json);
	}
}
