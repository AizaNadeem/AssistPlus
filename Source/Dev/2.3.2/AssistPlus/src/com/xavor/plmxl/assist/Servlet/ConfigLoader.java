package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.agile.api.APIException;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ConfigEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;

/**
 * Servlet implementation class ConfigLoader
 */
public class ConfigLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConfigLoader() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering configLoader: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String masterServerUrl = "";

		try {
			masterServerUrl = request.getParameter("masterServerUrl");

			log.debug("masterServerUrl=[" + masterServerUrl + "]");
			if(masterServerUrl != null && !masterServerUrl.equals("")) {
				json = ConfigHelper.getDataFromServer(masterServerUrl + "/loadConfig", null);
				JSONObject jobj = (JSONObject) new JSONParser().parse(json);
				json = jobj.toString();
				log.debug(json);
			} else {
				HashMap<String, ConfigEntry> lstConfig = ConfigHelper.getConfigEntries();
				json = new Gson().toJson(lstConfig);
			}
		} catch(Exception e) {
			log.error("Exception in ConfigLoader:doGet ", e);
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", errorMsg));
		}
		response.getWriter().write(json);

		log.debug(json.toString());
		log.debug("Exiting configLoader: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ConfigLoader: doPost..");

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String[] configs = new String[] {};
		configs = request.getParameterValues("configs[]");
		log.debug("Configs: " + configs);
		int chk = 1;
		String[] split;
		for(int i = 0; i < configs.length; i++) {
			split = configs[i].split("=");
			if(split.length > 1 || split[0].equalsIgnoreCase("PrimaryInstanceURL")) {
				continue;
			}
			
			chk = 0;
		}
		
		DBHandler dbh = null;
		try {
			if(chk == 1) {
				dbh = new DBHandler();
				HashMap params = new HashMap<String, String[]>();
				params.put("configs", configs);
				dbh.handleDBRequest("updateConfigurations", params, true);
				ConfigHelper.loadConfigurations();
				AgileHandler.refreshConnectionAfterConfigChange();
				json = new Gson().toJson(new ReturnStatus("success", "Configuration saved"));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", "Mandatory Fields are not filled"));
			}
		} catch(Exception e) {
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			if(e instanceof APIException) {
				APIException apie = (APIException) e;
				log.error("System is unable to connect to Agile. Please check configuration.", apie);

				json = new Gson().toJson(new ReturnStatus("attention", "System is unable to connect to Agile due to " + errorMsg
						+ ". Please check configuration."));
			} else {
				log.error("Exception in ConfigLoader: doPost", e);
				json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			}
		} finally {
			if(dbh != null) {
				// dbh.handleDBRequest("closeDB", null, false);
			}
		}
		
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting ConfigLoader: doPost..");
	}
}
