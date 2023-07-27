package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.ClassEntry;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.UIListHandler;
import com.agile.api.APIException;
import com.google.gson.Gson;

/**
 * Servlet implementation class ClassLoader
 */
public class ClassListLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ClassListLoader() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			List<ClassEntry> lstclasses = UIListHandler.getClassList();
			json = new Gson().toJson(new ReturnStatus("info", lstclasses.size() + " Classes listed", lstclasses));
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
		// String json = new Gson().toJson(lstclasses);
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
}
