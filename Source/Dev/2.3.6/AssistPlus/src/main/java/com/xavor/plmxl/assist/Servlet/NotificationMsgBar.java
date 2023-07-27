package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
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
		String json = "";
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
				getNotificationDetails(dbh, jobj, userid, noOfChars);
			} else {
				getNotificationBarMsgFromDB(dbh,jobj);
			}
			
			response.getWriter().write(jobj.toJSONString());
		} catch(Exception e) {
			String errorMsg = (e.getMessage() != null)? "" + e.getMessage() : "Null";
			log.error("Exception in NotificationMsgBar: doGet", e);
			json = new Gson().toJson(new ReturnStatus("error", errorMsg));
			response.getWriter().write(json);
		}
		log.debug(json);
		log.debug("Exiting NotificationMsgBar: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = "";
		String[] configs = request.getParameterValues("configs[]");
		log.debug("Configs: " + configs);
		
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, Object> params = new HashMap<>();
			params.put("configs", configs);
			dbh.handleDBRequest("toggleNotificationMsg", params, true);
			json = new Gson().toJson(new ReturnStatus("success", "Assist message settings saved"));
			response.getWriter().write(json);
		} catch(Exception e) {
			log.error("Exception in NotificationMsgBar: doPost: ", e);
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
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
		jobj.put("notificationId",helpMap.get("notificationId") +"");
		jobj.put("notificationMsg",helpMap.get("notificationMsg")+"" );
	}
	
	public void getNotificationDetails(DBHandler dbh, JSONObject jobj, String userid, String noOfChars) {
		Map<?, ?> helpMap = dbh.handleDBRequest("getNotificationMsg", new HashMap<>(), false);
		jobj.put("noOfChars",noOfChars);
		jobj.put("isNotifEnabled",helpMap.get("isNotifEnabled") +"");
		jobj.put("isAckEnabled",helpMap.get("isAckEnabled") +"");
		jobj.put("notificationId",helpMap.get("notificationId") +"");
		jobj.put("notificationMsg",helpMap.get("notificationMsg")+"" );
		String isAckUser = getUserAcknowledgment(dbh, userid, helpMap.get("notificationId") +"");
		jobj.put("isAckUser",isAckUser);
	}
	
	public String getUserAcknowledgment(DBHandler dbh, String userid, String notificationId) {
		if(userid.isEmpty()) {
			return "";
		}
		HashMap<String, String> params = new HashMap<>();
		params.put("userid", userid);
		params.put("notificationId", notificationId);
		Map ackMap = dbh.handleDBRequest("checkAcknowledgment", params, true);
		return (String) ackMap.get("acknowledgment");
	}
}
