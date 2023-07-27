package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IItem;
import com.agile.api.IRoutable;
import com.agile.api.ItemConstants;
import com.agile.api.UserConstants;
import com.xavor.ACS.AgileUtils;
import com.xavor.plmxl.assist.Handler.ActHandler;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class AssistPlus extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected String completeURL(String mURL) {
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

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Entering AssistPlus: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		JSONObject jobj = new JSONObject();
		HashMap<String, ArrayList<String>> helpMap = null;

		try {
			String classId = request.getParameter("classid").toString().trim();
			String userid = request.getParameter("userid");
			if (userid == null || userid.equalsIgnoreCase("")) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					log.debug("got cookies");
					for (Cookie cookie : cookies) {
						log.debug(cookie.getName() + " : " + cookie.getValue());
					}
					String agile_url = ConfigHelper.getProperty(Constants.Config.AgileServerURL);
					if (agile_url != null)
						log.debug("agile url: " + agile_url);
					else
						log.debug("agile url is null");
					IAgileSession cookiesSession = AgileUtils.getAgileCookieSession(cookies, agile_url);
					/** If user not logged in yet, load nothing **/
					if (cookiesSession != null) {
						log.debug("session created from cookies");
						userid = cookiesSession.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID)
								.toString();
						log.debug("got userid from the session: " + userid);
					} else {
						log.debug("unable to create the agile session from the cookies");
						return;
					}
				}
			} else {
				userid = userid.trim();
			}
			if (ActHandler.isLicValid()) {
				if (!classId.equals("undefined")) {
					String murl = ConfigHelper.getProperty(Constants.Config.PrimaryInstURL);
					/**
					 * If there is a Master URL configured, this is Slave
					 * instance so get assist text data from Master instance
					 **/
					if (murl != null && !murl.equals("")) {
						String body = "";
						Enumeration lstParam = request.getParameterNames();
						log.debug(lstParam.toString());
						while (lstParam.hasMoreElements()) {
							String param = lstParam.nextElement().toString();
							body += body.equals("") ? "" : "&";
							body += param + "=" + URLEncoder.encode(request.getParameter(param), "UTF-8");
						}
						murl = completeURL(murl);

						log.debug("Master Node Url=[" + murl + "]");

						String responseStr = ConfigHelper.getDataFromServer(murl, body);
						jobj = (JSONObject) new JSONParser().parse(responseStr);
						log.debug(responseStr);
					} else // This is Master Instance
					{
						DBHandler dbh = new DBHandler();
						HashMap params = new HashMap<String, String>();
						params.put("userid", userid);
						HashMap result = dbh.handleDBRequest("getOptOutUser", params, false);
						Boolean isOptedOut = (Boolean) result.get("isOptedOut");
						log.debug("ClassID::" + classId);
						

						if (!isOptedOut) {
							String isRequestForAffectedItems = request.getParameter("isRequestForAffectedItems").toString().trim();
							String workflowID = request.getParameter("workflowID").toString().trim();
							String workflowStatusID = request.getParameter("workflowStatusID").toString().trim();
							String lifecycleID = request.getParameter("lifecycleID").toString().trim();
							String objID = request.getParameter("objID").toString().trim();
							
							IAgileSession session = AgileHandler.getAgileSession();
							String rolesList = AgileHandler.getCurrentUserRoles(session, userid);
							IAgileClass actualClass = AgileHandler.getAgileClass(session, Integer.parseInt(classId));
							IAgileClass levelOneParent = null;
							IAgileClass levelTwoParent = null;
							String roles = rolesList, classes = "";
							
							

						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`	
							if(isRequestForAffectedItems.equalsIgnoreCase("true")){
								log.debug("Getting Assist Text for Affected Items");
								IItem item = (IItem)session.getObject(IItem.OBJECT_TYPE, objID);
								actualClass = item.getAgileClass();
								classId = actualClass.getId().toString();
								if(lifecycleID == null || lifecycleID.isEmpty()){
									lifecycleID = item.getCell(ItemConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE).toString();
								}
							} 
						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`	

							if (actualClass != null) {
								classes += (classId) + " ,";
								levelOneParent = actualClass.getSuperClass();
							}
							if (levelOneParent != null) {
								classes += (levelOneParent.getId().toString()) + " ,";
								levelTwoParent = levelOneParent.getSuperClass();
							}
							if (levelTwoParent != null) {
								classes += (levelTwoParent.getId().toString());

							}


							
//							if (!(actualClass instanceof IRoutableDesc)) {
//								if (lifecycleID == null || lifecycleID.equalsIgnoreCase("")) {
//									String objID = request.getParameter("objID").toString().trim();
//									try {
//										IDataObject obj =  (IDataObject) session.getObject(Integer.parseInt(classId), objID);
//										if(obj instanceof IItem){
//											lifecycleID = obj.getCell(ItemConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof IManufacturerPart){
//											lifecycleID = obj.getCell(ManufacturerPartConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof IManufacturer){
//											lifecycleID = obj.getCell(ManufacturerConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof IPrice){
//											lifecycleID = obj.getCell(PriceConstants.ATT_GENERAL_INFORMATION_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof ISupplier){
//											lifecycleID = obj.getCell(SupplierConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof ICustomer){
//											lifecycleID = obj.getCell(CustomerConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof IFileFolder){
//											lifecycleID = obj.getCell(FileFolderConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof IPartGroup){
//											lifecycleID = obj.getCell(PartGroupConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof IRequestForQuote){
//											lifecycleID = obj.getCell(RequestForQuoteConstants.ATT_COVERPAGE_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof ISpecification){
//											lifecycleID = obj.getCell(SpecificationConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										} else if(obj instanceof ISubstance){
//											lifecycleID = obj.getCell(SubstanceConstants.ATT_GENERAL_INFO_LIFECYCLE_PHASE)
//													.getValue().toString();
//										}
//
//									} catch (Throwable e) {
//										log.info("Ignoring throwable " + e.getMessage());
//									}
//								}
							
							if(actualClass.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS)){
								if (lifecycleID == null || lifecycleID.equalsIgnoreCase("")) {
									
									try{
										IItem item = (IItem) session.getObject(Integer.parseInt(classId), objID);
										lifecycleID = item.getCell(ItemConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE).toString();
									} catch(Throwable e){
										log.info("Ignoring throwable " + e.getMessage());
									}
								}
								//Manipulating the existing DB Structure. The Lifecycle of the object becomes the workflowStatusID and workflowID becomes "Lifecycles"
								workflowID = "Lifecycles";
								workflowStatusID = lifecycleID;
							} else{
								if (workflowID == null || workflowID.equalsIgnoreCase("")) {
									
									try {
										IRoutable obj = (IRoutable) session.getObject(Integer.parseInt(classId), objID);
										workflowID = obj.getWorkflow().getName().toString();
										workflowStatusID = obj.getStatus().getName().toString();
									} catch (Throwable e) {
										log.info("Ignoring throwable " + e.getMessage());
									}

								}
							}

							log.debug("Classes" + classes + "Roles List=[" + rolesList + "],Workflow Id=[" + workflowID
									+ "], Status Id=[" + workflowStatusID + "]");

							if (classes != null && !classes.isEmpty()) {

								String allRoleKey = ConfigHelper.configureAccessType(dbh);
								log.debug("allRoleKey: " + allRoleKey);
								params = new HashMap<String, String>();
								params.put("classes", classes);
								params.put("roles", roles);
								params.put("allRoleKey", allRoleKey);
								params.put("workflowID", workflowID);
								params.put("workflowStatusID", workflowStatusID);

								helpMap = (HashMap<String, ArrayList<String>>) dbh.handleDBRequest("getAssistInfoMap",
										params, false);

								jobj.put("helpText", helpMap);
								HashMap colorsParams = new HashMap<String, Object>();
								colorsParams.put("classId", classes.toString());
								Map attColors = dbh.handleDBRequest("getAssistColorsForClasses", colorsParams, false);

								log.debug("Attribute Colors=[" + attColors + "]");

								jobj.put("attColors", attColors);
							} else {
								log.debug("No classes found.");
							}
						} else {
							jobj.put("isOptedOut", "true");
						}

						// dbh.handleDBRequest("closeDB", null, false);
					}
				}
			}

			log.debug(jobj.toString());
			response.getWriter().write(jobj.toString());
		} catch (Exception e) {
			response.getWriter().write(e.getMessage());
			log.error("Exception in AssistPlus: doPost :", e);

		} catch (Throwable th) {
			log.info("Exception in AssistPlus: doPost :" + th.getMessage());

		}
		log.debug("Exiting AssistPlus: doPost..");
	}
}
