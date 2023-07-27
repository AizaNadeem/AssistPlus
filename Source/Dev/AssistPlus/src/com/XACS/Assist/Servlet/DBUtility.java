package com.XACS.Assist.Servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.XMLHandler;
import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.Constants;
import com.google.gson.Gson;

public class DBUtility extends HttpServlet {

	AssistLogger log=AssistLogger.getInstance();
	@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		
		log.debug("Entering DBUtility: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String mode = request.getParameter("mode").toString();
		
		log.debug("mode: "+mode);
	
		DBHandler dbh=null;
		
		if(mode.equalsIgnoreCase("import"))
		{
		
		try {
				dbh = new DBHandler();
				String type= request.getParameter("type").toString();
				HashMap<String,String> params=new HashMap<String,String>();
				String path= request.getParameter("path").toString();
				log.debug("path: "+path);
				
				String abspath=System.getenv(Constants.Config.HOME_ENVVAR);
				
				File f = new File(path);
				
				params.put("path",abspath+Constants.Config.ASSISTPLUSPATH+f.getName());
				params.put("type",type);
			
				log.info("Importing Database..");
								
				XMLHandler xml=new XMLHandler();
				HashMap statusMap = xml.importXML( params);
				
				Boolean status=(Boolean)statusMap.get("status");
			    Boolean hasError=(Boolean)statusMap.get("hasError");
				if (status) 
				{
					json = new Gson().toJson(new ReturnStatus("success", "Database Imported Successfully"));
					log.info("Database Imported..");
				} 
				else
				{
					if(hasError)
					{
						json = new Gson().toJson(new ReturnStatus("error", "Importing Database Failed. Invalid XML"));
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

		finally
			{
				if(dbh!=null)
				{
					dbh.handleDBRequest("closeDB", null, false);
				}
			}
		}
		else if(mode.equalsIgnoreCase("export"))
		{
			try {
				
				dbh = new DBHandler();
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

			finally
			{
				if(dbh!=null)
				{
					dbh.handleDBRequest("closeDB", null, false);
				}
			}
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting DBUtility: doPost..");
		
	}


}

