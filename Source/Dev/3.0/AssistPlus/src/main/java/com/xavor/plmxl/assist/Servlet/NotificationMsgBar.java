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
 * Servlet implementation class NotificationMsgBar
 */
public class NotificationMsgBar extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NotificationMsgBar() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering NotificationMsgBar: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		DBHandler dbh;
		try {
			JSONObject jobj = new JSONObject();
			dbh = new DBHandler();
			Properties property = ConfigHelper.loadPropertyFile();
			String noOfChars=property.getProperty("character.limit.for.assist.msg", "140");
			String isAdmin = request.getParameter("isAdmin");
			if(!isAdmin.equals("true")) {
				String userid = request.getParameter("userid");
				userid = CommonUtils.getUserIdOfLoggedInUser(request, userid,log);
				IAgileSession session = AgileHandler.getAgileSession();
				Set<String> rolesList = AgileHandler.getCurrentUserRoles(session, userid);
				String allRoleKey = ConfigHelper.configureAccessType(dbh);
				getNotificationDetails(dbh, jobj, rolesList, allRoleKey, userid, noOfChars);
			} else {
				getNotificationBarMsgFromDB(dbh,jobj);
			}
			
			response.getWriter().write(jobj.toJSONString());
		} catch(Exception e) {
			String errorMsg = (e.getMessage() != null)? "" + e.getMessage() : "Null";
			log.error("Exception in NotificationMsgBar: doGet", e);
			String json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			response.getWriter().write(json);
		}
		log.debug("Exiting NotificationMsgBar: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering NotificationMsgBar: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = "";
		String[] configs = request.getParameterValues("configs[]");
		String[] roles = request.getParameterValues("roles[]");
		log.debug("Configs: " + configs);
		
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, Object> params = new HashMap<>();
			params.put("configs", configs);
			params.put("roles", roles);
			dbh.handleDBRequest("toggleNotificationMsg", params, true);
			json = new Gson().toJson(new ReturnStatus("success", "Assist message settings saved"));
			response.getWriter().write(json);
		} catch(Exception e) {
			log.error("Exception in NotificationMsgBar: doPost: ", e);
			String errorMsg = (e.getMessage() != null) ? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			response.getWriter().write(json);
		}
		log.debug("Exiting NotificationMsgBar: doPost..");
	}
	@SuppressWarnings("unchecked")
	public void getNotificationBarMsgFromDB(DBHandler dbh, JSONObject jobj) {
		Map<?, ?> helpMap = dbh.handleDBRequest("getNotificationMsg", new HashMap<>(), false);
		jobj.put("isNotifEnabled",helpMap.get("isNotifEnabled") +"");
		jobj.put("isAckEnabled",helpMap.get("isAckEnabled") +"");
		jobj.put("fontColor",helpMap.get("fontColor")+"" );
		jobj.put("backgroundColor",helpMap.get("backgroundColor") +"");
		jobj.put("notificationId",helpMap.get("notificationId") +"");
		jobj.put("notificationMsg",helpMap.get("notificationMsg")+"" );
		jobj.put("isDurationEnabled",helpMap.get("isDurationEnabled") +"");
		jobj.put("durationLimit",helpMap.get("durationLimit")+"" );
		jobj.put("roles",helpMap.get("roles")+"" );
	}
	
	public void getNotificationDetails(DBHandler dbh, JSONObject jobj, Set<String> rolesList, String allRoleKey, String userid, String noOfChars) {
		if(getRoles(dbh, rolesList, allRoleKey).equals("true")) {
			Map<?, ?> helpMap = dbh.handleDBRequest("getNotificationMsg", new HashMap<>(), false);
			String notificationId = helpMap.get("notificationId") +"";
			jobj.put("noOfChars",noOfChars);
			jobj.put("isNotifEnabled",helpMap.get("isNotifEnabled") +"");
			jobj.put("isAckEnabled",helpMap.get("isAckEnabled") +"");
			jobj.put("fontColor",helpMap.get("fontColor") +"");
			jobj.put("backgroundColor",helpMap.get("backgroundColor")+"");
			jobj.put("notificationId",notificationId);
			jobj.put("notificationMsg",helpMap.get("notificationMsg")+"");
			jobj.put("isDurationEnabled",helpMap.get("isDurationEnabled") +"");
			jobj.put("durationLimit",helpMap.get("durationLimit")+"" );
			jobj.put("lastUpdated",helpMap.get("lastUpdated")+"" );
			String isAckUser = getUserAcknowledgment(dbh, userid, notificationId);
			jobj.put("isAckUser",isAckUser);
			if((helpMap.get("isNotifEnabled")+"").equals("Yes") && !(helpMap.get("notificationMsg")+"").equals("null") && getViewStatistics(dbh, userid, notificationId).equals("false")) {
				updateNotificationStatistics(dbh, notificationId);
			}
		}
	}
	public String getRoles(DBHandler dbh, Set<String> rolesList, String allRoleKey) {
		HashMap<String, Object> params = new HashMap<>();
		params.put("rolesList", rolesList);
		params.put("allRoleKey", allRoleKey);
		Map roleMap = dbh.handleDBRequest("getRoles", params, true);
		return roleMap.containsKey("isRole")?  (String) roleMap.get("isRole"): "";
	}
	public String getUserAcknowledgment(DBHandler dbh, String userid, String notificationId) {
		if(userid.isEmpty()) {
			return "";
		}
		HashMap<String, String> params = new HashMap<>();
		params.put("userid", userid);
		params.put("notificationId", notificationId);
		Map ackMap = dbh.handleDBRequest("checkAcknowledgment", params, true);
		return ackMap.containsKey("acknowledgment")?  (String) ackMap.get("acknowledgment"): "";
	}
	
	public String getViewStatistics(DBHandler dbh, String userid, String notificationId) {
		if(userid.isEmpty()) {
			return "";
		}
		HashMap<String, String> params = new HashMap<>();
		params.put("userid", userid);
		params.put("notificationId", notificationId);
		Map existMap = dbh.handleDBRequest("getNotificationStatistics", params, true);
		return existMap.containsKey("rowExists")? (String) existMap.get("rowExists"): "";
	}
	
	
	public void updateNotificationStatistics(DBHandler dbh, String notificationId) {
		HashMap<String, String> params = new HashMap<>();
		params.put("notificationId", notificationId);
		dbh.handleDBRequest("updateNotificationStatistics", params, true);
	}
}
