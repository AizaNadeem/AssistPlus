package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Util.AssistLogger;

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
			dbh.handleDBRequest("closeDB", null, false);
			
		} catch (Exception e) 
		{
			log.error("Exception in AssistColor: doPost ", e);
			
		}
		log.debug("Exiting AssistColor: doPost ..");
	}
	private Map getAttributeColors(String attColorStr)
	{
		log.debug("Entering getAttributeColors..");
		Map result=new HashMap();
		
		String attColorArray[]=null;
		
		if(attColorStr.contains(";"))
		{
			attColorArray=attColorStr.split(";");
		}
		else
		{
			attColorArray=new String[1];
			attColorArray[0]=attColorStr;
		}
		
		String attColors[]=null;
		for(int attColIndex=0;attColIndex<attColorArray.length;attColIndex++)
		{
			attColors=attColorArray[attColIndex].split(":");
			if(attColors!=null && attColors.length>0)
			{
				result.put(attColors[0], attColors[1]);
			}
		}
		
		log.debug(result.toString());
		log.debug("Exiting getAttributeColors..");
		return result;
	}

}
