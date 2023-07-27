package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.CommonUtils;

/**
 * Servlet implementation class AcknowledgeAssistMessage
 */
public class AcknowledgeAssistMessage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();
	static final String ERROR = "error";
	static final String NOTIFICATION_ID = "notificationId";
	static final String USER_ID = "userid";
	static final String USER_NAME = "userName";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AcknowledgeAssistMessage() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AcknowledgeAssistMessage: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		
		DBHandler dbh = null;
		try {
			dbh = new DBHandler();
			String notificationId = request.getParameter(NOTIFICATION_ID);
			String userid = request.getParameter(USER_ID).trim();
			userid = CommonUtils.getUserIdOfLoggedInUser(request, userid,log);
			if(userid.isEmpty())
				return;
			HashMap<String, String> params = new HashMap<>();
			params.put(USER_ID, userid);
			params.put(NOTIFICATION_ID, notificationId);
			Map<?, ?> ackMap = dbh.handleDBRequest("checkAcknowledgment", params, true);
			String ackInfo = (String) ackMap.get("acknowledgment");
			if(ackInfo.equals("true")) {
				json = new Gson().toJson(new ReturnStatus(ERROR, "Already Acknowledged"));
			} else {
				json = new Gson().toJson(new ReturnStatus("success", "Not Acknowledged"));
			}
			response.getWriter().write(json);
		} catch(Exception e) {
			String errorMsg = (e.getMessage() != null)? "" + e.getMessage() : "Null";
			log.error("Exception in AcknowledgeAssistMessage: doGet", e);
			json = new Gson().toJson(new ReturnStatus(ERROR, errorMsg));
		}
		log.debug(json);
		log.debug("Exiting AcknowledgeAssistMessage: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AcknowledgeAssistMessage: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		
		DBHandler dbh = null;
		try {
			dbh = new DBHandler();
			String notificationId = request.getParameter(NOTIFICATION_ID);
			String userid = request.getParameter(USER_ID).trim();
			String userName = request.getParameter(USER_NAME).trim();
			userid = CommonUtils.getUserIdOfLoggedInUser(request, userid,log);
			if(userid.isEmpty())
				return;
			HashMap<String, String> params = new HashMap<>();
			params.put(USER_ID, userid);
			params.put(USER_NAME, userName);
			params.put(NOTIFICATION_ID, notificationId);
			dbh.handleDBRequest("acknowledgeUser", params, true);
			json = new Gson().toJson(new ReturnStatus("success", "Acknowledged"));
			response.getWriter().write(json);
		} catch(Exception e) {
			String errorMsg = (e.getMessage() != null)? "" + e.getMessage() : "Null";
			log.error("Exception in AcknowledgeAssistMessage: doPost", e);
			json = new Gson().toJson(new ReturnStatus(ERROR, errorMsg));
		}
		log.debug(json);
		log.debug("Exiting AcknowledgeAssistMessage: doPost..");
	}
}
