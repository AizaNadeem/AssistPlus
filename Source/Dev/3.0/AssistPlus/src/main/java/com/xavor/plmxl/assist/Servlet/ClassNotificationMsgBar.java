package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.agile.api.IAgileSession;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.CommonUtils;
import com.xavor.plmxl.assist.Util.ConfigHelper;

/**
 * Servlet implementation class ClassNotificationMsgBar
 */
public class ClassNotificationMsgBar extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ClassNotificationMsgBar() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ClassNotificationMsgBar: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		DBHandler dbh;
		try {
			JSONObject jobj = new JSONObject();
			dbh = new DBHandler();
			String classId = request.getParameter("classId");
			log.debug("classId: [" + classId + "]");
			getClassNotificationMsgFromDB(dbh, jobj, classId);
			response.getWriter().write(jobj.toJSONString());
		} catch(Exception e) {
			String errorMsg = (e.getMessage() != null)? "" + e.getMessage() : "Null";
			log.error("Exception in ClassNotificationMsgBar: doGet", e);
			json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			response.getWriter().write(json);
		}
		log.debug(json);
		log.debug("Exiting ClassNotificationMsgBar: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = "";
		String classId = request.getParameter("classId");
		String assistText = request.getParameter("assistText");
		String notifEnable = request.getParameter("notifEnable");
		String overrideEnable = request.getParameter("overrideEnable");
		String fontColor = request.getParameter("fontColor");
		String backgroundColor = request.getParameter("backgroundColor");
		String[] roles = request.getParameterValues("roles[]");
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, Object> params = new HashMap<>();
			params.put("classID", classId);
			params.put("assistText", assistText);
			params.put("notifEnable", notifEnable);
			params.put("overrideEnable", overrideEnable);
			params.put("fontColor", fontColor);
			params.put("backgroundColor", backgroundColor);
			params.put("roles", roles);
			dbh.handleDBRequest("toggleClassNotificationMsg", params, true);
			json = new Gson().toJson(new ReturnStatus("success", "Assist Class message settings saved"));
			response.getWriter().write(json);
		} catch(Exception e) {
			log.error("Exception in ClassNotificationMsgBar: doPost: ", e);
			String errorMsg = (e.getMessage() != null) ? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			response.getWriter().write(json);
		}
		log.debug("Exiting ClassNotificationMsgBar: doPost..");
	}
	@SuppressWarnings("unchecked")
	public void getClassNotificationMsgFromDB(DBHandler dbh, JSONObject jobj, String classId) {
		HashMap<String, String> params = new HashMap<>();
		params.put("classId", classId);
		Map<?, ?> helpMap = dbh.handleDBRequest("getClassNotificationMsg", params, false);
		jobj.put("isClassNotifEnabled",helpMap.get("isNotifEnabled") +"");
		jobj.put("isNotifOverrideEnabled",helpMap.get("isOverrideEnabled") +"");
		jobj.put("classFontColor",helpMap.get("fontColor")+"" );
		jobj.put("classBackgroundColor",helpMap.get("backgroundColor") +"");
		jobj.put("classNotificationId",helpMap.get("notificationId") +"");
		jobj.put("classNotificationMsg",helpMap.get("notificationMsg")+"" );
		jobj.put("classRoles",helpMap.get("roles")+"" );
	}
	
	
	public void getClassNotificationMsgFromDB(DBHandler dbh, JSONObject jobj, String classId, Set<String> rolesList, String allRoleKey) {
		if(getClassRoles(dbh, rolesList, allRoleKey, classId).equals("true")) {
			HashMap<String, String> params = new HashMap<>();
			params.put("classId", classId);
			Map<?, ?> helpMap = dbh.handleDBRequest("getClassNotificationMsg", params, false);
			jobj.put("isClassNotifEnabled",helpMap.get("isNotifEnabled") +"");
			jobj.put("isNotifOverrideEnabled",helpMap.get("isOverrideEnabled") +"");
			jobj.put("classFontColor",helpMap.get("fontColor")+"" );
			jobj.put("classBackgroundColor",helpMap.get("backgroundColor") +"");
			jobj.put("classNotificationId",helpMap.get("notificationId") +"");
			jobj.put("classNotificationMsg",helpMap.get("notificationMsg")+"" );
		}
	}
	
	public String getClassRoles(DBHandler dbh, Set<String> rolesList, String allRoleKey, String classId) {
		HashMap<String, Object> params = new HashMap<>();
		params.put("rolesList", rolesList);
		params.put("allRoleKey", allRoleKey);
		params.put("classId", classId);
		Map roleMap = dbh.handleDBRequest("getClassRoles", params, true);
		return roleMap.containsKey("isRole")?  (String) roleMap.get("isRole"): "";
	}
	
}
