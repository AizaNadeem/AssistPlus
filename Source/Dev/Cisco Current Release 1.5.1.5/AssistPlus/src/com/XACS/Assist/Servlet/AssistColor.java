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

public class AssistColor extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public AssistColor() 
    {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String classId = request.getParameter("classId");
		String attColors=request.getParameter("attColors");
		
		System.out.println("Class Id=["+classId+"], Attribute Colors=["+attColors+"]");
		
		try 
		{
			DBHandler dbh = new DBHandler();
			
			JSONParser parser=new JSONParser();
			JSONArray jsonAttColors=(JSONArray)parser.parse(attColors);
			
			dbh.addNewAssistColor(classId, jsonAttColors);
			
			dbh.closeDB();
			
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	private Map getAttributeColors(String attColorStr)
	{
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
		
		
		return result;
	}

}
