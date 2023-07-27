package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.AssistTextEntry;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.UIListHandler;
import com.google.gson.Gson;

/**
 * Servlet implementation class ClassLoader
 */
public class AssistTextListLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AssistTextListLoader() {
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
			String classId = request.getParameter("classId");
			String attrId = request.getParameter("attrId");
			List<AssistTextEntry> lsttexts = UIListHandler.getAssistTextList(classId, attrId);
			json = new Gson().toJson(new ReturnStatus("info", lsttexts.size() + " Assist Text entries found", lsttexts));
		} catch (Exception e) {
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
		// String json = new Gson().toJson(lsttexts);
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String json = "";
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String fontColor = "", backgroundColor = "";
		fontColor = request.getParameter("fontcolor");
		backgroundColor = request.getParameter("backgroundcolor");
		boolean isDiffColor = Boolean.parseBoolean(request.getParameter("isDiffColor"));
		try {
			String mode = request.getParameter("mode");
			// String classId = "";
			// String attrId = "";
			if (mode.equalsIgnoreCase("new")) {
				String classId = request.getParameter("classId");
				String attrId = request.getParameter("attrId");
				int textID = -1;
				String[] roles = {};
				DBHandler dbh = null;
				try 
				{
					dbh = new DBHandler();
					textID = dbh.addNewAssistText(classId, attrId);
					roles = dbh.getRoleOptions();
				} catch (Exception e) 
				{
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
					response.getWriter().write(json);
					return;
				}
				finally
				{
					if(dbh!=null)
					{
						dbh.closeDB();
					}
				}
				AssistTextEntry ate = new AssistTextEntry();
				ate.setTextID(String.valueOf(textID));
				ate.setAssistText("");
				ate.setAttrID(attrId);
				ate.setClassID(classId);
				ate.setRoles(roles);
				json = new Gson().toJson(new ReturnStatus("success", "New Assist Text added", ate));
			} else if (mode.equalsIgnoreCase("save")) {
				String textId = request.getParameter("textId");
				String assistText = request.getParameter("assistText");
				String[] roles = new String[] {};
				roles = request.getParameterValues("roles[]");
				try 
				{
					if (!assistText.equals("")) 
					{
						DBHandler dbh = new DBHandler();
						dbh.updateAssistText(textId, assistText, fontColor, backgroundColor, isDiffColor);
						dbh.updateTextRoleList(textId, roles);
						json = new Gson().toJson(new ReturnStatus("success", "Assist Text saved"));
					} else {
						json = new Gson().toJson(new ReturnStatus("error", "Empty Assist Text Row"));
					}
				} catch (Exception e) {
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
				}
			} else if (mode.equalsIgnoreCase("saveAll")) {
				String[] saveAllText = new String[] {};
				String[] roles;
				String[] Troles = null;
				String text = "";
				String txtid = "";
				saveAllText = request.getParameterValues("saveAllText[]");
				String classid = request.getParameter("classid");
				String attid = request.getParameter("attid");
				ArrayList<AssistTextEntry> rows = new ArrayList<AssistTextEntry>();
				ArrayList<String> totalRoles = new ArrayList<String>();
				// getting roles from DB
				/*
				 * DBHandler dbh2 = new DBHandler(); ArrayList <Integer>
				 * getroles= new ArrayList<Integer>();
				 * getroles=dbh2.checkDuplicateRoles
				 * (Integer.parseInt(classid),Integer.parseInt(attid));
				 */
				// ***********
				ArrayList<String> duplicate = new ArrayList<String>();
				for (int i = 0; i < saveAllText.length; i++) {
					AssistTextEntry temp = new AssistTextEntry();
					saveAllText[i] = saveAllText[i].substring(2, saveAllText[i].length() - 2);
					roles = saveAllText[i].split("],");
					for (int j = 0; j < roles.length; j++) {
						if (roles[j].startsWith("[")) {
							roles[j] = roles[j].substring(1, roles[j].length());
						}
					}
					Troles = roles[0].split(",");
					for (int k = 0; k < Troles.length; k++) {
						for (int y = 0; y < rows.size(); y++) {
							for (int z = 0; z < rows.get(y).getRoles().length; z++) {
								if (rows.get(y).getRoles()[z].equals(Troles[k])) {
									duplicate.add(roles[2]);
									// System.out.println(roles[2]);
									duplicate.add(rows.get(y).getTextID());
									// System.out.println(rows.get(y).getTextID());
								}
							}
						}
						totalRoles.add(Troles[k]);
					}
					text = roles[1];
					txtid = roles[2];
					isDiffColor = false;
					fontColor = "";
					backgroundColor = "";
					isDiffColor = Boolean.parseBoolean(roles[3]);
					fontColor = roles[4];
					backgroundColor = roles[5];
					temp.setRoles(Troles);
					temp.setAssistText(text);
					temp.setTextID(txtid);
					temp.setClassID(classid);
					temp.setAttrID(attid);
					temp.setIsDifferentColor(isDiffColor);
					temp.setFontColor(fontColor);
					temp.setBackgroundColor(backgroundColor);
					rows.add(temp);
				}
				// System.out.println(duplicate);
				try {
					if (duplicate.size() == 0/* isEmpty() */) {
						json = new Gson().toJson(new ReturnStatus("success", "All Assist Text Already saved"));
						DBHandler dbh = new DBHandler();
						dbh.updateAllAssistText(rows);
						json = new Gson().toJson(new ReturnStatus("success", "All Assist Text saved"));
					} else {
						json = new Gson().toJson(new ReturnStatus("error", "Multiple Roles in Rows ", duplicate));
					}
				} catch (Exception e) {
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
					// System.out.println(e.getMessage());
				}
			} else if (mode.equalsIgnoreCase("remove")) {
				String textId = request.getParameter("textId");
				try {
					DBHandler dbh = new DBHandler();
					dbh.removeAssistText(textId);
					json = new Gson().toJson(new ReturnStatus("success", "Assist Text removed"));
				} catch (Exception e) {
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
				}
			}
		} catch (Exception e) {
			json = new Gson().toJson(new ReturnStatus("error", "ServletError: " + e.getMessage()));
			// System.out.println(e.getMessage());
		}
		response.getWriter().write(json);
	}
}
