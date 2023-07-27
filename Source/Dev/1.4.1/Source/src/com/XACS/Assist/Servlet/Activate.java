package com.XACS.Assist.Servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.ActHandler;
import com.google.gson.Gson;

/**
 * Servlet implementation class Activate
 */
public class Activate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Activate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//doPost(request, response);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		ReturnStatus rs = null;
		try{
			rs = new ReturnStatus("info", "License information loaded.", ActHandler.getActInfo());
		} 
		catch (Exception e) {
			e.printStackTrace();
			rs = new ReturnStatus("error","Error while getting License Information: " + e.getMessage());
		}
		
		String respjsonstr = new Gson().toJson(rs);
		response.getWriter().write(respjsonstr);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		ReturnStatus rs = null;
		try{
			rs = new ReturnStatus("success",
					"Activation information updated successfully.",
					ActHandler.processActivation());
		} catch (FileNotFoundException e) {
		rs = new ReturnStatus(
				"error",
				"License file not found. Please place the provided license file under <PLMFLEX_HOME>\\AssistPlus.");
		} catch (Exception e) {
		e.printStackTrace();
		rs = new ReturnStatus("error", "Error during Activation: "
				+ e.getMessage());
		}
		String respjsonstr = new Gson().toJson(rs);
		response.getWriter().write(respjsonstr);
	}
}
