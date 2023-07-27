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
			Properties property = ConfigHelper.loadPropertyFile();
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

		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, Object> params = new HashMap<>();
			params.put("classID", classId);
			params.put("assistText", assistText);
			params.put("notifEnable", notifEnable);
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
		jobj.put("classNotificationId",helpMap.get("notificationId") +"");
		jobj.put("classNotificationMsg",helpMap.get("notificationMsg")+"" );
	}
	
}
