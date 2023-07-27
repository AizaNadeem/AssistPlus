package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agile.api.APIException;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ClassEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.UIListHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class ClassLoader
 */
public class ClassListLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();  

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ClassListLoader() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ClassListLoader: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			List<ClassEntry> lstclasses = new UIListHandler().getClassList();
			json = new Gson().toJson(new ReturnStatus("info", lstclasses.size() + " Classes listed", lstclasses));
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				log.error("System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration.", apie);
				
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				log.error("Exception  ", e);
				
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}
		
		log.debug(json.toString());
		response.getWriter().write(json);
		log.debug("Exiting ClassListLoader: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
}
