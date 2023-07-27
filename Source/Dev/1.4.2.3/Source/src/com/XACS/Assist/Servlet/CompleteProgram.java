package com.XACS.Assist.Servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.XACS.Assist.PPM.CompleteTask;

public class CompleteProgram extends HttpServlet {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject jObj = new JSONObject();
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			String programNumber = request.getParameter("prgName");
			CompleteTask cTask = new CompleteTask();
			jObj = cTask.CompleteProgram(programNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			jObj.put("Error", e.getMessage());
		}
		response.getWriter().write(jObj.toJSONString());
	}
}
