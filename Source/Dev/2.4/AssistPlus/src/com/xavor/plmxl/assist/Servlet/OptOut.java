package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

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

public class OptOut extends HttpServlet {

	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.addHeader("Access-Control-Allow-Origin","*"); 
//		response.addHeader( "Access-Control-Allow-Headers", "Content-Type");
		String useCase="getOptOut";
		try {
			if(request.getParameter("useCase").equals("updateOptOut")){
				doPost(request,response);
			}
			else if(request.getParameter("useCase").equals(useCase)){
				doGet(response);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
		
	}

	@SuppressWarnings("unchecked")
	private void doGet(HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		DBHandler dbh;
		try {
			dbh = new DBHandler();
			HashMap<String, String> data = (HashMap<String, String>) dbh.handleDBRequest("getOptOutUsers", null, false);
			JSONObject jobj = new JSONObject();
			jobj.put("data", data);
			response.getWriter().write(jobj.toJSONString());
		} catch(Exception e) {
			JSONObject jobj = new JSONObject();
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			String json = "Error while getting opt out Information: " + errorMsg;
			jobj.put("error", json);
			response.getWriter().write(jobj.toJSONString());
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering OptOut: doPost..");
		response.setContentType("text/html");
		String callback = request.getParameter("callback");
		response.setCharacterEncoding("UTF-8");

		String json = "";
		try {
			DBHandler dbh = new DBHandler();
			String mode = request.getParameter("mode").toString().trim();
			String userid = request.getParameter("userid").toString().trim();
			userid = CommonUtils.getUserIdOfLoggedInUser(request, userid,log);
			if(userid.isEmpty())
				return;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("userid", userid);
			if(mode.equalsIgnoreCase("on")) {
				try {
					dbh.handleDBRequest("addOptOutUser", params, true);
				} catch(Exception ex) {
					json = new Gson().toJson(new ReturnStatus("error", "user already opted out"));
				}
			} else {
				dbh.handleDBRequest("removeOptOutUser", params, true);
			}

			json = new Gson().toJson(new ReturnStatus("success", "user previliges changed"));
//			response.getWriter().write(json);
		} catch(Exception e) {
			log.error("Exception in OptOut: doPost: ", e);
			String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
			json = new Gson().toJson(new ReturnStatus("error", errorMsg));
//			response.getWriter().write(json);
		}
		if(callback != null) {
			response.getWriter().write(callback + "(" + json + ");");
        }
        else {
        	response.getWriter().write(json);
        }
		log.debug("Exiting OptOut: doPost..");
	}

	

}
