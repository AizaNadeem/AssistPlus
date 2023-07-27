package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.Handler.ActHandler;
import com.XACS.Assist.Handler.DBHandler;

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
		try {
			String classId = request.getParameter("classid").toString().trim();
			System.out.println("ClassID::" + classId);
			if (ActHandler.isLicValid()) {
				if (!classId.equals("undefined")) {
					String rolesList = request.getParameter("roles").toString().trim();
					System.out.println("Role List::" + rolesList);
					String classesList = request.getParameter("classes").toString().trim();
					System.out.println("Classes List::" + classesList);
					System.out.println("Current Class List::" + classesList);
					ArrayList<String> roles = new ArrayList<String>(), classes = new ArrayList<String>();
					Collections.addAll(roles, rolesList.split(";"));
					Collections.addAll(classes, classesList.split(";"));
					DBHandler dbh = new DBHandler();
					helpMap = dbh.getAssistInfoMap(classes, roles);
					jobj.putAll(helpMap);
				}
			}
			response.getWriter().write(jobj.toJSONString());
		} catch (Exception e) {
			// TODO: handle exception
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} finally {
			// AgileHandler.disconnect(session);
			System.gc();
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}
}
