package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.Handler.ActHandler;
import com.XACS.Assist.Handler.AgileHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.agile.api.IAgileSession;

public class AssistPlus extends HttpServlet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/*
	 * private static String serverName = ""; private static String UserName =
	 * ""; private static String Password = "";
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject jobj = new JSONObject();
		HashMap<String, ArrayList<String>> helpMap = null;
		IAgileSession session = null;
		try {
			String classId = request.getParameter("classid").toString().trim();
			String userId = request.getParameter("userid").toString().trim();
			
			if(ActHandler.isLicValid())
			{
				ArrayList<String> roles, classes = null;
				session = AgileHandler.getAgileSession();
				roles = AgileHandler.getUserRoles(userId, session);
				classes = AgileHandler.getRelatedClasses(session, classId);
				DBHandler dbh = new DBHandler();
				helpMap = dbh.getAssistInfoMap(classes, roles);
				jobj.putAll(helpMap);
			}
			response.getWriter().write(jobj.toJSONString());
		} catch (Exception e) {
			// TODO: handle exception
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} finally {
			AgileHandler.disconnect(session);
		}
	}
}
