package com.XACS.Assist.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.PPM.CompleteTask;
import com.XACS.Assist.Util.AssistLogger;

public class CompleteProgram extends HttpServlet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		log.debug("Entering CompleteProgram: doGet..");
		JSONObject jObj = new JSONObject();
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			String programNumber = request.getParameter("prgName");
			log.debug("Program Number: "+programNumber);
			CompleteTask cTask = new CompleteTask();
			jObj = cTask.CompleteProgram(programNumber, request);
		} catch (Exception e) {
			log.error("Exception in CompleteProgram: ", e);
			
			jObj.put("Error", e.getMessage());
		}
		response.getWriter().write(jObj.toJSONString());
		log.debug(jObj.toJSONString());
		log.debug("Exiting CompleteProgram: doGet..");

	}
}
