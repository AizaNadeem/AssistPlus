package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.List;
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

public class NotifUsageReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AssistLogger log = AssistLogger.getInstance();

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering NotifUsageReport: doPost...");
		String jsonResponse = "";
		
		try {
			DBHandler dbh = new DBHandler();
			Map<String, List<JSONObject>> report = (Map<String, List<JSONObject>>) dbh.handleDBRequest("generateNotifUsageReport", null, false);
			jsonResponse = new Gson().toJson(report);
		} catch(Exception ex) {
			log.error("Error while generating user acknowledgment report: ", ex);
			String errorMsg = (ex.getMessage() != null) ? ex.getMessage() : "Null";
			jsonResponse = new Gson().toJson(new ReturnStatus("error", "Error while generating report: " + errorMsg));
		}
		
		response.setContentType("application/json");
		response.getWriter().write(jsonResponse);
		log.debug("Exiting NotifUsageReport: doPost...");
	}

}
