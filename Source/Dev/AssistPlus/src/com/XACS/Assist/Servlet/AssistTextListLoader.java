package com.XACS.Assist.Servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.DO.AssistTextEntry;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.UIListHandler;
import com.XACS.Assist.Util.AssistLogger;
import com.google.gson.Gson;

/**
 * Servlet implementation class ClassLoader
 */
public class AssistTextListLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AssistTextListLoader() {
		super();
		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AssistTextListLoader: doGet..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		try {
			String classId = request.getParameter("classId");
			String attrId = request.getParameter("attrId");
			log.debug("classId: ["+classId+"], attrId: ["+attrId+"]");
			List<AssistTextEntry> lsttexts = UIListHandler.getAssistTextList(classId, attrId);
			json = new Gson().toJson(new ReturnStatus("info", lsttexts.size() + " Assist Text entries found", lsttexts));
		
		} catch (Exception e) {
			
			log.error("Exception in AssistTextListLoader: doGet: ", e);
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
	
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting AssistTextListLoader: doGet..");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AssistTextListLoader: doPost..");
		String json = "";
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String fontColor = "", backgroundColor = "";
		fontColor = request.getParameter("fontcolor");
		backgroundColor = request.getParameter("backgroundcolor");
		boolean isDiffColor = Boolean.parseBoolean(request.getParameter("isDiffColor"));
		try {
			String mode = request.getParameter("mode");
			log.debug("Mode: "+mode);
			if (mode.equalsIgnoreCase("new")) {
				String classId = request.getParameter("classId");
				String attrId = request.getParameter("attrId");
				log.debug("classId: ["+classId+"], attrId: ["+attrId+"]");
				
				int textID = -1;
				String[] roles = {};
				HashMap map;
				DBHandler dbh = null;
				try {
					dbh = new DBHandler();
					map = dbh.handleDBRequest("getRoleOptions", null, false);
					roles=(String[]) map.get("strRoles");
					log.debug(roles.toString());
				} catch (Exception e) 
				{
					log.error( "DBError: " , e);
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
					response.getWriter().write(json);
					return;
				}
				finally
				{
					if(dbh!=null)
					{
						dbh.handleDBRequest("closeDB", null, false);
					}
				}
				AssistTextEntry ate = new AssistTextEntry();
				ate.setTextID(String.valueOf(textID));
				ate.setAssistText("");
				ate.setAttrID(attrId);
				ate.setClassID(classId);
				ate.setRoles(roles);
				json = new Gson().toJson(new ReturnStatus("success", "New Assist Text added", ate));
			} 
			
			 if (mode.equalsIgnoreCase("save")) {
				 DBHandler dbh = null;
				String classId = request.getParameter("classId");
				String attrId = request.getParameter("attrId");
				String textId = request.getParameter("textId");
				String assistText = request.getParameter("assistText");
				String[] roles = new String[] {};
				roles = request.getParameterValues("roles[]");
				String workflowID=request.getParameter("workflowname");
				String workflowStatusID=request.getParameter("workflowstatus");
				log.debug("Workflow Status: ["+workflowStatusID+"],Roles: ["+roles+"], Workflow Name: ["+workflowID+"],Text Id: ["+textId+"], Assist Text: ["+assistText+"],Class Id: ["+classId+"], Attr Id: ["+attrId+"]");
				try {
					/////////////////////////////////new row handling////////////////////////////
					
					if(textId.equalsIgnoreCase("-1"))
					{					
						//int textID = -1;
						
						try {
							dbh = new DBHandler();
							HashMap params=new HashMap<String,Object>();
							params.put("classID",classId);
							params.put("attrID",attrId);
							params.put("assistText",assistText);
							params.put("fontColor",fontColor);
							params.put("backgroundColor",backgroundColor);
							params.put("isDiffColor",isDiffColor);
							params.put("workflowID",workflowID);
							params.put("workflowStatusID",workflowStatusID);
							HashMap result = dbh.handleDBRequest("addNewAssistText", params, true);
							int intTextId=(Integer) result.get("textId");
							textId=Integer.toString(intTextId);
							HashMap roleParams=new HashMap<String,Integer>();
							roleParams.put("textID", textId);
							roleParams.put("roleList", roles);
							dbh.handleDBRequest("updateTextRoleList", roleParams, true);
							json = new Gson().toJson(new ReturnStatus("success", "Assist Text saved"));
							log.info("Assist Text Saved");
							log.debug(json.toString());
						} catch (Exception e) 
						{
							log.error( "DBError: ", e);
							
							json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
							response.getWriter().write(json);
							log.debug(json.toString());
							return;
						}

					}
					
					
					
					////////////////////////////////new row handling end///////////////////////
										else if (!assistText.equals("")) {
						dbh = new DBHandler();
						HashMap params=new HashMap<String,Object>();
						params.put("textID",textId);
						params.put("assistText",assistText);
						params.put("fontColor",fontColor);
						params.put("backgroundColor",backgroundColor);
						params.put("isDiffColor",isDiffColor);
						params.put("workflowID",workflowID);
						params.put("workflowStatusID",workflowStatusID);
						dbh.handleDBRequest("updateAssistText", params, true);
						HashMap roleParams=new HashMap<String,Integer>();
						roleParams.put("textID", textId);
						roleParams.put("roleList", roles);
						dbh.handleDBRequest("updateTextRoleList", roleParams, true);
						json = new Gson().toJson(new ReturnStatus("success", "Assist Text saved"));
						log.info("Assist Text Saved");
					} else {
						
						json = new Gson().toJson(new ReturnStatus("error", "Empty Assist Text Row"));
					}
				} catch (Exception e) {
					log.error( "DBError: ", e);
					
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
									duplicate.add(rows.get(y).getTextID());
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
				
				try {
					if (duplicate.size() == 0/* isEmpty() */) {
						json = new Gson().toJson(new ReturnStatus("success", "All Assist Text Already saved"));
						DBHandler dbh = new DBHandler();
						HashMap params=new HashMap<String,ArrayList<AssistTextEntry>>();
						params.put("rows",rows);
						log.debug("rows: "+rows.toString());
						dbh.handleDBRequest("updateAllAssistText", params, true);
						json = new Gson().toJson(new ReturnStatus("success", "All Assist Text saved"));
					} else {
						json = new Gson().toJson(new ReturnStatus("error", "Multiple Roles in Rows ", duplicate));
					}
				} catch (Exception e) {
					log.error( "DBError: " , e);
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
				
				}
			} else if (mode.equalsIgnoreCase("remove")) {
				String textId = request.getParameter("textId");
				try {
					DBHandler dbh = new DBHandler();
					HashMap params=new HashMap<String,String>();
					params.put("textID", textId);
					dbh.handleDBRequest("removeAssistText", params, true);
					json = new Gson().toJson(new ReturnStatus("success", "Assist Text removed"));
					
					log.info("Assist Text removed");
				} catch (Exception e) {
					log.error("DBError: ", e);
					json = new Gson().toJson(new ReturnStatus("error", "DBError: " + e.getMessage()));
				}
			}
		} catch (Exception e) {
			log.error("ServletError: " , e);
			json = new Gson().toJson(new ReturnStatus("error", "ServletError: " + e.getMessage()));
		}
		response.getWriter().write(json);
		log.debug(json.toString());
		log.debug("Exiting AssistTextListLoader: doPost..");
	}
}
