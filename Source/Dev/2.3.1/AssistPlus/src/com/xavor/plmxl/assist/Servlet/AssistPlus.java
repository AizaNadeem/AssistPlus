package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IDataObject;
import com.agile.api.IItem;
import com.agile.api.IRoutable;
import com.agile.api.IStatus;
import com.agile.api.IWorkflow;
import com.agile.api.ItemConstants;
import com.agile.api.UserConstants;
import com.xavor.ACS.AgileUtils;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class AssistPlus extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering AssistPlus: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		JSONObject jobj = new JSONObject();

		try {			
			String classId = request.getParameter("classid").trim();
			String userid = request.getParameter("userid");
			if (userid == null || userid.trim().isEmpty()) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					log.debug("got cookies");
					for (Cookie cookie : cookies) {
						log.debug(cookie.getName() + " : " + cookie.getValue());
					}
					String agile_url = ConfigHelper.getProperty(Constants.Config.AgileServerURL);
					if (agile_url != null) {
						log.debug("Agile URL: " + agile_url);
						IAgileSession cookiesSession = AgileUtils.getAgileCookieSession(cookies, agile_url);
						if (cookiesSession != null) {
							log.debug("session created from cookies");
							userid = cookiesSession.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
							log.debug("got userid from the session: " + userid);
							cookiesSession.close(true);//v 2.3.0.4 fix
						} else {
							log.info("Unable to create the Agile session from cookies");
							return;
						}
					} else {
						log.info("Agile url is not configured in Assist+ Admin Panel");
						return;
					}
				}
			} else {
				userid = userid.trim();
			}
			
			jobj.put("userid", userid);
			
			if (!classId.equals("undefined")) {
				String murl = ConfigHelper.getProperty(Constants.Config.PrimaryInstURL);
				/**
				 * If there is a Master URL configured, this is Slave
				 * instance so get assist text data from Master instance
				 **/
				if (murl != null && !murl.equals("") && !murl.equals("NULL_VALUE")) {
					String body = "";
					Enumeration<String> lstParam = request.getParameterNames();
					log.debug(lstParam.toString());
					while (lstParam.hasMoreElements()) {
						String param = lstParam.nextElement();
						body += body.equals("") ? "" : "&";
						body += param + "=" + URLEncoder.encode(request.getParameter(param), "UTF-8");
					}
					murl = completeURL(murl);

					log.debug("Master Node Url=[" + murl + "]");

					String responseStr = ConfigHelper.getDataFromServer(murl, body);
					jobj = (JSONObject) new JSONParser().parse(responseStr);
					log.debug(responseStr);
				} else {
					/**
					 * This is Master Instance
					 */
					DBHandler dbh = new DBHandler();
					HashMap<String, String> params = new HashMap<String, String>();
					params.put("userid", userid);
					Map result = dbh.handleDBRequest("getOptOutUser", params, false);
					
					Boolean isOptedOut = false;
					Boolean enableOptOut = (Boolean) result.get("enableOptOut");
					jobj.put("enableOptOut", enableOptOut);
					if(enableOptOut) {
						isOptedOut = (Boolean) result.get("isOptedOut");
					}
					log.debug("ClassID::" + classId);

					if (!isOptedOut) {
						String loadAssistForAffectedItem = request.getParameter("loadAssistForAffectedItem").trim();
						String loadAssistForMainObject = request.getParameter("loadAssistForMainObject").trim();
						String workflowID = request.getParameter("workflowID").trim();
						String workflowStatusID = request.getParameter("workflowStatusID").trim();
						String lifecycleID = request.getParameter("lifecycleID").trim();
						String objID = request.getParameter("objID").trim();
						String affectedItem = request.getParameter("affectedItem").trim();

						IAgileSession session = AgileHandler.getAgileSession();
						Set<String> rolesList = AgileHandler.getCurrentUserRoles(session, userid);

						if (loadAssistForAffectedItem.equalsIgnoreCase("true")) {
							JSONObject affectedItemAssist = loadAssistMapping(session, dbh, rolesList, true, classId, affectedItem, lifecycleID, workflowID, workflowStatusID);
							jobj.put("affectedItemAssist", affectedItemAssist);
						}
						if (loadAssistForMainObject.equalsIgnoreCase("true")) {
							JSONObject affectedItemAssist = loadAssistMapping(session, dbh, rolesList, false, classId, objID, lifecycleID, workflowID, workflowStatusID);
							jobj.put("mainObjectAssist", affectedItemAssist);
						}
					} else {
						jobj.put("isOptedOut", "true");
					}
					
					try {
						String paramString = request.getParameter("statistics");
						if (paramString != null) {
							JSONObject stats = (JSONObject) JSONValue.parse(paramString);
							updateStatisticsInDB(dbh, stats);
						}
					} catch(Exception ex) {
						log.error("Error while logging Usage Statistics: ", ex);
					}
				}
			}

			log.debug(jobj.toString());
			try {
				response.getWriter().write(jobj.toString());
			} catch(Throwable th) {}
		} catch (Exception e) {
			try {
				response.getWriter().write("" + e);
			} catch(Throwable th) {}
			log.error("Exception in AssistPlus: doPost: ", e);
		} catch (Throwable th) {
			log.error("Exception in AssistPlus: doPost: ", th);
		}
		log.debug("Exiting AssistPlus: doPost..");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private JSONObject loadAssistMapping(IAgileSession session, DBHandler dbh, Set<String> rolesList, boolean isAffectedItem, String classId, String objID, String lifecycleID, String workflowID, String workflowStatusID) throws Exception {
		JSONObject jobj = new JSONObject();
		HashMap<String, ArrayList<String>> helpMap = null;

		IAgileClass actualClass = null;
		IAgileClass levelOneParent = null;
		IAgileClass levelTwoParent = null;
		String classes = "";

		if (isAffectedItem) {
			log.debug("Getting Assist Text for Affected Items");
			IItem item = (IItem) session.getObject(IItem.OBJECT_TYPE, objID);
			actualClass = item.getAgileClass();
			classId = actualClass.getId().toString();
			jobj.put("classid", classId);
			if (lifecycleID.isEmpty()) {
				lifecycleID = item.getCell(ItemConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE).toString();
			}
		} else {
			actualClass = AgileHandler.getAgileClass(session, Integer.parseInt(classId));
		}

		if (actualClass != null) {
			classes += classId;
			levelOneParent = actualClass.getSuperClass();
		}
		if (levelOneParent != null) {
			classes += "," + levelOneParent.getId();
			levelTwoParent = levelOneParent.getSuperClass();
		}
		if (levelTwoParent != null) {
			classes += "," + levelTwoParent.getId();
		}

		if (!classes.isEmpty()) {
			if (actualClass.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS)) {
				if (lifecycleID == null || lifecycleID.equalsIgnoreCase("")) {
					try {
						IItem item = (IItem) session.getObject(Integer.parseInt(classId), objID);
						lifecycleID = item.getCell(ItemConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE).toString();
					} catch (Throwable e) {
						log.info("Ignoring throwable " + e);
					}
				}
				/* Manipulating the existing DB Structure. The Lifecycle of the
				   object becomes the workflowStatusID and workflowID becomes "Lifecycles" */
				workflowID = "Lifecycles";
				workflowStatusID = lifecycleID;
			} else {
				if (workflowID == null || workflowID.equalsIgnoreCase("")) {
					try {
						IDataObject obj = (IDataObject) session.getObject(Integer.parseInt(classId), objID);
						if (obj instanceof IRoutable) {
							log.debug("Object is an IRoutable");
							IRoutable robj = (IRoutable) obj;
							IWorkflow wflow = robj.getWorkflow();
							if (wflow != null) {
								workflowID = wflow.getName();
								IStatus status = robj.getStatus();
								if (status != null) {
									workflowStatusID = status.getName();
								}
							}
						}
					} catch (Throwable e) {
						log.info("Ignoring throwable: " + e);
					}
				}
			}
			log.debug("Classes=[" + classes + "], Workflow=[" + workflowID + "], Status=[" + workflowStatusID + "]");

			String allRoleKey = ConfigHelper.configureAccessType(dbh);
			log.debug("allRoleKey: " + allRoleKey);
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("classes", classes);
			params.put("roles", rolesList);
			params.put("allRoleKey", allRoleKey);
			params.put("workflowID", workflowID);
			params.put("workflowStatusID", workflowStatusID);

			helpMap = (HashMap<String, ArrayList<String>>) dbh.handleDBRequest("getAssistInfoMap", params, false);

			jobj.put("helpText", helpMap);
			HashMap<String, Object> colorsParams = new HashMap<String, Object>();
			colorsParams.put("classId", classes);
			Map attColors = dbh.handleDBRequest("getAssistColorsForClasses", colorsParams, false);

			log.debug("Attribute Colors=[" + attColors + "]");

			jobj.put("attColors", attColors);
		} else {
			log.info("No classes found: " + classId);
		}

		return jobj;
	}

	private String completeURL(String mURL) {
		String murl = mURL;
		if (murl.endsWith("/")) {
			murl = murl.substring(0, murl.length() - 1);
		}
		if (!murl.endsWith("/" + Constants.Config.ProjectName)) {
			murl += "/" + Constants.Config.ProjectName;
		}
		murl += "/" + Constants.Config.URL_GetAssistText;
		return murl;
	}
	
	private void updateStatisticsInDB(DBHandler dbh, JSONObject stats) {
		Map<String, Object> params = new HashMap<String, Object>();
		
		if(stats == null || stats.get("userid") == null) {
			return;
		}
		
		String userid = stats.get("userid").toString();
		params.put("userid", userid);
		
		JSONObject affectedItem = (JSONObject) stats.get("affectedItem");
		if(affectedItem != null) {
			params.put("affectedItemClassid", affectedItem.get("classid").toString());
			
			JSONObject attrs = (JSONObject) affectedItem.get("attrs");
			params.put("affectedItemAttrs", attrs);
		}
		
		JSONObject main = (JSONObject) stats.get("main");
		params.put("classid", main.get("classid").toString());
		JSONObject attrs = (JSONObject) main.get("attrs");
		params.put("attrs", attrs);
		
		dbh.handleDBRequest("updateStatistics", params, false);
	}
}
