package com.xavor.plmxl.assist.Servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Handler.XMLHandler;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.Constants;


public class DBUtility extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AssistLogger log=AssistLogger.getInstance();
	@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
		log.debug("Entering DBUtility: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String mode = request.getParameter("mode").toString();
		
		log.debug("mode: "+mode);		
		
		if(mode.equalsIgnoreCase("import"))
		{
		
		try {			


				String type= request.getParameter("type").toString();
				HashMap<String,String> params=new HashMap<String,String>();
				String path= request.getParameter("path").toString();
				log.debug("path: "+path);				

				String abspath=ConfigHelper.getAppHomePath();
				
				File f = new File(path);
				
				params.put("path",abspath+f.getName());
				params.put("type",type);
			
				log.info("Importing Database..");
								
				XMLHandler xml=new XMLHandler();
				HashMap<String, String> statusMap = xml.importXML( params);
				
				String status=statusMap.get("status");
			    String hasError=statusMap.get("hasError");
			    String accessType=statusMap.get("accessType");
				if (status.equals("true")) 

				{
					json = new Gson().toJson(new ReturnStatus("success", "Database Imported Successfully"));
					log.info("Database Imported..");
				} 
				else
				{
					if(hasError.equals("true"))
					{
						if(accessType.equals("okay"))
						{
							json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML"));		
						}
						else if(accessType.equals("roles"))
						{
							json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML as RolesPriority is expected."));
						}
						else if(accessType.equals("usergroups"))
						{
							json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML as UserGroupPriority is expected."));
						}
					}
					else
					{
						json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed"));
					}
					log.info("Importing Database Failed..");
				}
			} 
		catch (Exception e)
			{
				
				log.error("Exception: ", e);
				json = new Gson().toJson(new ReturnStatus("error", "Error while Importing Database: " + e.getMessage()));
			}		
		}
		else if(mode.equalsIgnoreCase("export"))
		{
			try {			
			
				log.info("Exporting Database..");
				XMLHandler xml=new XMLHandler();
				HashMap statusMap = xml.exportXML();
							Boolean status=(Boolean)statusMap.get("status");
				if (status) 
				{
					json = new Gson().toJson(new ReturnStatus("success", "Database Exported Successfully"));
					log.info("Database Exported..");
				} 
				else
				{
					json = new Gson().toJson(new ReturnStatus("error", "Exporting Database Failed"));
					log.info("Exporting Database Failed..");
				}
			} 
			catch (Exception e) 
			{				
				log.error("Exception: ", e);
				json = new Gson().toJson(new ReturnStatus("error", "Error while Exporting Database: " + e.getMessage()));
			}
			
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting DBUtility: doPost..");
		
	}


}

