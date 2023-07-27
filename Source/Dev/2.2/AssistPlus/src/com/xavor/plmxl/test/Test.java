package com.xavor.plmxl.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IItem;
import com.agile.api.IRoutable;
import com.agile.api.ItemConstants;
import com.xavor.plmxl.assist.Handler.ActHandler;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;

public class Test {
	 static AssistLogger log=AssistLogger.getInstance();
	public static void main(String[] args) {
		
		try{
			
//			IAgileSession session =	getAgileSession("http://agile934.xavor.com:7001/Agile", "admin", "agile123");
//			IAgileClass cls = session.getAdminInstance().getAgileClass("Documents");
//			String id = cls.getId().toString();
//			
//			UIListHandler handler = new UIListHandler();
//			
//		List<CListModel> list = handler.getClassWorkflows(id, 2);
			

			
			assistPlusTestMethod("881", "admin","Inactive",null,null, "P00134","true");
			
		} catch(Exception e){
			System.err.println(e);
		}
		

	}
	
	
	public static void assistPlusTestMethod(String classId, String userid, String workflowID, String workflowStatusID, String lifecycleID, String objID, String isRequestForAffectedItems){
		log.debug("Entering AssistPlus: doPost..");

		
		JSONObject jobj = new JSONObject();
		HashMap<String, ArrayList<String>> helpMap = null;
		
		try 
		{
			if(userid==null||userid.equalsIgnoreCase(""))
			{
				
			} else {
				userid = userid.trim();
			}
			if (ActHandler.isLicValid()) 
			{
				if (!classId.equals("undefined")) 
				{
					
						DBHandler dbh = new DBHandler();
						HashMap params=new HashMap<String,String>();
						params.put("userid", userid);
						Map result=dbh.handleDBRequest("getOptOutUser", params, false);
						Boolean isOptedOut=(Boolean)result.get("isOptedOut");
						log.debug("ClassID::" + classId);;
							
								
							
						if(isOptedOut!=true)
						{

							
							IAgileSession session = AgileHandler.getAgileSession();
							String rolesList = AgileHandler.getCurrentUserRoles(session, userid);
							IAgileClass actualClass = null;
							IAgileClass levelOneParent = null;
							IAgileClass levelTwoParent = null;
							String roles = rolesList, classes = "";
							
							

						//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`	
							if(isRequestForAffectedItems.equalsIgnoreCase("true")){
								IItem item = (IItem)session.getObject(IItem.OBJECT_TYPE, objID);
								actualClass = item.getAgileClass();
								classId = actualClass.getId().toString();
								if(lifecycleID == null || lifecycleID.isEmpty()){
									lifecycleID = item.getCell(ItemConstants.ATT_TITLE_BLOCK_LIFECYCLE_PHASE).toString();
								}
							} else{
								actualClass = AgileHandler.getAgileClass(session, Integer.parseInt(classId));
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
					}
						else
						{
							jobj.put("isOptedOut", "true");
						}

					//dbh.handleDBRequest("closeDB", null, false);
					}
				}
			
			
			
			
			log.debug(jobj.toString());
		
		} catch (Exception e) 
		{
		
			log.error("Exception in AssistPlus: doPost :", e);
			
		} catch (Throwable th) 
		{
			log.info("Exception in AssistPlus: doPost :" + th.getMessage());
			
		}
		log.debug("Exiting AssistPlus: doPost..");
	}
	
    public static IAgileSession getAgileSession(String url, String name, String password) throws APIException {
        AgileSessionFactory factory = AgileSessionFactory.getInstance(url);
        Map<Integer, String> params = new HashMap<Integer, String>();
        params.put(AgileSessionFactory.USERNAME, name);
        params.put(AgileSessionFactory.PASSWORD, password);
       IAgileSession session = factory.createSession(params);
        return session;
         
    }
	
	}


