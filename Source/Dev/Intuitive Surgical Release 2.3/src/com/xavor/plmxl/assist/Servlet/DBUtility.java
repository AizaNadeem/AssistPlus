package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.XMLHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;

public class DBUtility extends HttpServlet {

	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering DBUtility: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String mode = request.getParameter("mode").toString();

		log.debug("mode: " + mode);

		if(mode.equalsIgnoreCase("import")) {
			try {
				String type = request.getParameter("type").toString();
				HashMap<String, String> params = new HashMap<String, String>();
				String path = request.getParameter("path").toString();
				log.debug("path: " + path);

				String abspath = ConfigHelper.getAppHomePath();

				if(path.contains("\\")) {
					String[] splitName = path.split("\\\\");
					if(splitName.length > 0) {
						path = splitName[splitName.length - 1];
					}
				}

				params.put("path", abspath + path);
				params.put("type", type);

				log.info("Importing Database..");

				XMLHandler xml = new XMLHandler();
				HashMap<String, String> statusMap = xml.importXML(params);

				String status = statusMap.get("status");
				String hasError = statusMap.get("hasError");
				String accessType = statusMap.get("accessType");
				String rolesCheck = statusMap.get("rolesCheck");
				String classesCheck = statusMap.get("classesCheck");
				if(status.equals("true")) {
					if(rolesCheck.equals("okay") && classesCheck.equals("okay")) {
						json = new Gson().toJson(new ReturnStatus("success", "Database Imported Successfully"));
					} else if(!rolesCheck.equals("okay") && !classesCheck.equals("okay")) {
						json = new Gson().toJson(new ReturnStatus("attention", "Assist Text entries after successful import contain roles/usergroups/classes not defined on the destination server, kindly edit the entries"));
					} else if(!rolesCheck.equals("okay")) {
						json = new Gson().toJson(new ReturnStatus("attention", "Assist Text entries after successful import contain roles/usergroups not defined on the destination server, kindly edit the entries"));
					} else if(!classesCheck.equals("okay")) {
						json = new Gson().toJson(new ReturnStatus("attention", "Assist Text entries after successful import contain classes/attributes not defined on the destination server, some entries may not have not been imported"));
					}

					log.info("Database Imported..");
				} else {
					if(hasError.equals("true")) {
						if(accessType.equals("okay")) {
							json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML"));
						} else if(accessType.equals("roles")) {
							json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML as RolesPriority is expected."));
						} else if(accessType.equals("usergroups")) {
							json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML as UserGroupPriority is expected."));
						}
					} else {
						json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed"));
					}
					log.info("Importing Database Failed..");
				}
			} catch(Exception e) {
				log.error("Exception: ", e);
				String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
				json = new Gson().toJson(new ReturnStatus("error", "Error while Importing Database: " + errorMsg));
			}
		} else if(mode.equalsIgnoreCase("export")) {
			try {
				log.info("Exporting Database..");
				XMLHandler xml = new XMLHandler();
				HashMap<String, Boolean> statusMap = xml.exportXML();
				Boolean status = (Boolean) statusMap.get("status");
				if(status) {
					json = new Gson().toJson(new ReturnStatus("success", "Database Exported Successfully"));
					log.info("Database Exported..");
				} else {
					json = new Gson().toJson(new ReturnStatus("error", "Exporting Database Failed"));
					log.info("Exporting Database Failed..");
				}
			} catch(Exception e) {
				log.error("Exception: ", e);
				String errorMsg = (e != null) ? "" + e.getMessage() : "Null";
				json = new Gson().toJson(new ReturnStatus("error", "Error while Exporting Database: " + errorMsg));
			}
		}
		
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting DBUtility: doPost..");
	}

}
