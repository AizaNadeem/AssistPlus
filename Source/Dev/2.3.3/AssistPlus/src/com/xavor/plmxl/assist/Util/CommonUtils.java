package com.xavor.plmxl.assist.Util;

import javax.servlet.http.HttpServletRequest;

import com.xavor.plmxl.assist.Handler.AgileHandler;

public class CommonUtils {

	public static boolean isNull(String s) {
		if ((s == null) || (s.equals("")) || (s.trim().length() == 0) || s.trim().equalsIgnoreCase("null")) {
			return true;
		}
		return false;
	}
	public static String getUserIdOfLoggedInUser(HttpServletRequest request, String userid, AssistLogger log) throws Exception {
		String userIdFromRequest = request.getSession().getAttribute("userId")+"";
		log.debug("userIdFromRequest: "+userIdFromRequest+" userid:"+userid);
		if(!userIdFromRequest.equalsIgnoreCase("null") && !userIdFromRequest.isEmpty() ) {
			userid=userIdFromRequest;
		}
		else if(userid != null && !userid.trim().isEmpty() && !userid.equalsIgnoreCase("null")) {
			userid = userid.trim();
			request.getSession().setAttribute("userId", userid);
		}
		else {
			if(CommonUtils.isNull(ConfigHelper.getProperty(Constants.Config.AgileServerURL))){
				log.info("Agile url is not configured in Assist+ Admin Panel");
				return "";
			}
			String reqSessionEnabled=ConfigHelper.loadPropertyFile().getProperty("enable.request.session.creation", "Yes");
			if(reqSessionEnabled.equals("No")) {
				return "";
			}
			else
			{
				if(CommonUtils.isNull(ConfigHelper.getProperty(Constants.Config.AgileServerURL))){
					log.info("Agile url is not configured in Assist+ Admin Panel");
					return "";
				}
				AgileHandler agileHandler=new AgileHandler(request);
				userid=agileHandler.getUserIdFromSession();
				if(CommonUtils.isNull(userid)){
					log.info("user id not obtained from request session.");
					return "";
				}
			}


		}
		return userid;
	}

}
