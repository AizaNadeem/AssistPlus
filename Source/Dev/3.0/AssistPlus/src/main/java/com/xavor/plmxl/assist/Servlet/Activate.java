package com.xavor.plmxl.assist.Servlet;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.ActHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

public class Activate extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	AssistLogger log=AssistLogger.getInstance();
	public Activate() 
	{
		super();
		

	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering Activate: doGet ..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		ReturnStatus rs = null;
		try{
			rs = new ReturnStatus("info", "License information loaded.", ActHandler.getActInfo());
		} 
		catch (Exception e) {
			
			log.error("Error while getting License Information: " , e);
			
			rs = new ReturnStatus("error","Error while getting License Information: " + e);
		}
		
		String respjsonstr = new Gson().toJson(rs);
		log.debug(respjsonstr);
		response.getWriter().write(respjsonstr);
		log.debug("Exiting Activate: doPost ..");
	}

	@Override
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering Activate: doPost ..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ReturnStatus rs = null;
		try
		{
			if(request.getParameter("accessType")!=null){
				rs = new ReturnStatus("success","Activation information updated successfully.",ActHandler.processActivation(request.getParameter("accessType").trim()));
			}
			else {
				rs = new ReturnStatus("success","Activation information updated successfully.",ActHandler.processActivation(null));
			}
		} catch (FileNotFoundException e) {
			log.error("License file not found.", e);	
			rs = new ReturnStatus("error","License file not found. Please place the provided license file under <PLMFLEX_HOME>\\AssistPlus.");
		} catch (Exception e) {
			log.error("Error during Activation: ", e);
			rs = new ReturnStatus("error", "Error during Activation: "+ e.getMessage());
		}
		String respjsonstr = new Gson().toJson(rs);
		response.getWriter().write(respjsonstr);
		
		log.debug(respjsonstr);
		log.debug("Exiting Activate: doPost ..");
	}
}
