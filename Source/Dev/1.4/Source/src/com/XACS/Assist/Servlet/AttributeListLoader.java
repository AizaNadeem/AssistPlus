package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.AttributeEntry;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.UIListHandler;
import com.google.gson.Gson;

/**
 * Servlet implementation class ClassLoader
 */
public class AttributeListLoader extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AttributeListLoader() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {	
			String classId = request.getParameter("classId");
			String level = request.getParameter("level");
			
			List<AttributeEntry> lstattrs = UIListHandler.getAttributeList(Integer.parseInt(classId), Integer.parseInt(level));
			json = new Gson().toJson(new ReturnStatus("info",lstattrs.size() + " Attributes found",lstattrs));
		} catch (Exception e)
		{
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
	//	String json = new Gson().toJson(lstattrs);
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}