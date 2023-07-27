package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

public class AssistColor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 AssistLogger log=AssistLogger.getInstance();

    public AssistColor() 
    {
        super();
		

    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering AssistColor: doPost ..");
		String classId = request.getParameter("classId");
		String attColors=request.getParameter("attColors");
		String json;
		log.debug("Class Id=["+classId+"], Attribute Colors=["+attColors+"]");
		
		try 
		{
			DBHandler dbh = new DBHandler();
			JSONParser parser=new JSONParser();
			JSONArray jsonAttColors=(JSONArray)parser.parse(attColors);
			HashMap params=new HashMap<String,Object>();
			params.put("classID",classId);
			params.put("jsonAttColors", jsonAttColors);
			dbh.handleDBRequest("addNewAssistColor", params, true);
			//dbh.handleDBRequest("closeDB", null, false);
			json = new Gson().toJson(new ReturnStatus("success", "Assist Text saved"));
			
		} catch (Exception e) 
		{
			log.error("Exception in AssistColor: doPost ", e);
			json = new Gson().toJson(new ReturnStatus("error", "ServletError: " + e.getMessage()));
			
		}
		response.getWriter().write(json);
		log.debug("Exiting AssistColor: doPost ..");
	}
}
