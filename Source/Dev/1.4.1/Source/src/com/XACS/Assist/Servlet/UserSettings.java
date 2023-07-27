package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.Handler.AgileHandler;
import com.agile.api.IAgileSession;
import com.agile.api.UserConstants;
import com.xavor.ACS.AgileUtils;
import com.xavor.ACS.Utils;

public class UserSettings extends HttpServlet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject json = new JSONObject();
		try {
			Properties props = Utils.loadPropertyFile(Utils.getAgilePath("/opt/Agile/BRCMPX.properties", "/projects/agiledev1/Agile/BRCMPX.properties"));
			String AgileServerURL = Utils.getValueByKey(props, "AgileURL");
			IAgileSession session = AgileUtils.getRequestSession(request, AgileServerURL);
			if (session == null) {
				session = AgileUtils.getAgileCookieSession(request.getCookies(), AgileServerURL);
			}
			if (session != null) {
				session.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
				String roles = AgileHandler.getCurrentUserRoles(session);
				json.put("Roles", roles);
				ArrayList<String> classes = AgileHandler.getAllClasses(session);
				json.put("classes", classes);
			}
		} catch (Exception e) {
			json.put("error", e.getMessage());
			// TODO: handle exception
		}
		response.getWriter().write(json.toJSONString());
	}
}
