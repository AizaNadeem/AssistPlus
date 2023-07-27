package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		DBHandler dbh;
		try {
			JSONObject jobj = new JSONObject();
			HashMap<String, String> params=new HashMap<String, String>();
			dbh = new DBHandler();
			getNotificationBarMsgFromDB(dbh,jobj);
//			Map<?, ?> helpMap = dbh.handleDBRequest("getNotificationMsg", params, false);
////			log.debug("notificationMsg: "+helpMap.get("notificationMsg"));
//			jobj.put("notificationMsg",helpMap.get("notificationMsg") );
//			jobj.put("isNotifEnabled",helpMap.get("isNotifEnabled") );


			response.getWriter().write(jobj.toJSONString());
		} catch(Exception e) {
			JSONObject jobj = new JSONObject();
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			String json = "Error while getting assist message Information: " + errorMsg;
			jobj.put("error", json);
			response.getWriter().write(jobj.toJSONString());
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String json = "";
		String[] configs = new String[] {};
		configs = request.getParameterValues("configs[]");
		log.debug("Configs: " + configs);
		
		try {
			DBHandler dbh = new DBHandler();
			HashMap<String, Object> params = new HashMap<String, Object>();
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
	public void getNotificationBarMsgFromDB(DBHandler dbh, JSONObject jobj ) {
		HashMap<String, String> params=new HashMap<String, String>();
		Map<?, ?> helpMap = dbh.handleDBRequest("getNotificationMsg", params, false);
		jobj.put("notificationMsg",helpMap.get("notificationMsg")+"" );
		jobj.put("isNotifEnabled",helpMap.get("isNotifEnabled") +"");

	}


}
