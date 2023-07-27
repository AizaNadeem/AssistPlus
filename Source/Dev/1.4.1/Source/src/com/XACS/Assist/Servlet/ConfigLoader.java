package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.ConfigEntry;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.AgileHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.UIListHandler;
import com.XACS.Assist.Util.ConfigHelper;
import com.agile.api.APIException;
import com.google.gson.Gson;

/**
 * Servlet implementation class ConfigLoader
 */
public class ConfigLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConfigLoader() {
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
			HashMap<String, ConfigEntry> lstConfig = UIListHandler.getConfigEntries();
			json = new Gson().toJson(lstConfig);
		} catch (Exception e) {
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
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
		String[] configs = new String[] {};
		configs = request.getParameterValues("configs[]");
		int chk = 0;
		String[] split;
		for (int i = 0; i < configs.length; i++) {
			chk = 0;
			split = configs[i].split("=");
			if (split.length > 1) {
				chk = 1;
			}
		}
		try {
			if (chk == 1) {
				DBHandler dbh = new DBHandler();
				dbh.updateConfigurations(configs);
				ConfigHelper.loadConfigurations();
				AgileHandler.refreshConnection();
				json = new Gson().toJson(new ReturnStatus("success", "Configuration saved"));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", "Mandatory Fields are not filled"));
			}
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
		response.getWriter().write(json);
	}
}
