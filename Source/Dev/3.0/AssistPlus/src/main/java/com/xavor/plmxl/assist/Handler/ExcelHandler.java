package com.xavor.plmxl.assist.Handler;

import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAttribute;
import com.agile.api.IRoutableDesc;
import com.agile.api.IStatus;
import com.agile.api.IWorkflow;
import com.agile.api.ItemConstants;
import com.xavor.plmxl.assist.DO.AssistText;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Util.AssistLogger;

public class ExcelHandler {

	private AssistLogger log = AssistLogger.getInstance();
	private DBHandler dbh = null;
	Writer  fw=null;
	
	
	private List<XSSFSheet> sheet = new ArrayList<>();
	private XSSFWorkbook book = null;
	
	private IAdmin admin = null;
	
	private Map<String, AgileClassDO> agileClassesCache = null;
	
	private final String ITEMS_WFVALUE = "Lifecycles";
	private final String ALL_STATUS = "All Statuses";
	private final String ALL_WORKFLOWS = "All Workflows";
	private HashMap<String, Object> statusMap=null;
	
	public ExcelHandler(InputStream inputStream) throws Exception {
		dbh = new DBHandler();
		book = new XSSFWorkbook(inputStream);
		for(int i=0; i<book.getNumberOfSheets(); i++) {
			sheet.add(book.getSheetAt(i));
		}
		admin = AgileHandler.getAgileSession().getAdminInstance();
		agileClassesCache = new HashMap<String, AgileClassDO>();
		statusMap= new HashMap<String, Object>();


	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> importExcelFile(String importType) throws Exception {
		boolean overwrite = "overwrite".equalsIgnoreCase(importType);
		
		//Get all Roles configured in the Admin panel
		Map<String, String> allRoles = new HashMap<String, String>();
		List<RoleEntry> allRoleEntries = (List<RoleEntry>) dbh.handleDBRequest("getRolePriority", null, false).get("rolePriority");
		for(RoleEntry role : allRoleEntries) {
			allRoles.put(role.getRole(), role.getRoleID().toString());
		}
		
		//Get default Font and Background colors
		Map<String, String> configs = (Map<String, String>) dbh.handleDBRequest("readConfigurations", null, false);
		final String FONT_COLOR = configs.get("fontColor");
		final String BACKGROUND_COLOR = configs.get("backgroundColor");
				
		//Get existing Assist+ text mappings
		Map<String, List<String>> currentTextRoles = (Map<String, List<String>>) dbh.handleDBRequest("getTextRoles", null, false);
		List<AssistTextEntry> currentTextsList = (List<AssistTextEntry>) dbh.handleDBRequest("getAssistTextMap", null, false).get("textList");
		for(AssistTextEntry entry : currentTextsList) {
			List<String> roles = currentTextRoles.get(entry.getTextID());
			entry.setRolesList(roles);
			
			entry.setFontColor(FONT_COLOR);
			entry.setBackgroundColor(BACKGROUND_COLOR);
		}
		
		int lastRow = sheet.get(0).getLastRowNum();
		log.debug("Rows in the excel sheet: " + lastRow);

		Map<String, AssistTextEntry> updateText = new HashMap<String, AssistTextEntry>();
		Map<String, AssistTextEntry> insertText = new HashMap<String, AssistTextEntry>();
		int insertKey = 1;
		List<Integer> updatedTextIndexes = new ArrayList<Integer>();
		List<AssistTextEntry> insertedTexts = new ArrayList<AssistTextEntry>();
		List<AssistTextEntry> updatedText = new ArrayList<AssistTextEntry>();
		List<AssistTextEntry> failedTexts = new ArrayList<AssistTextEntry>();
		List<AssistTextEntry> deletedTexts = new ArrayList<AssistTextEntry>();
		
		int success = 0;
		int errors = 0;
		
		// Skip first Row (i=0) for the Header
		rowsLoop: for(int i = 1; i <= lastRow; i++) {
			log.debug("Processing excel row: " + (i + 1));
			XSSFRow row = sheet.get(0).getRow(i);
			
			try {
				if(row != null) {
					String className = getStringValue(row.getCell(0));
					String attrName = getStringValue(row.getCell(1));
					String assistText = getStringValue(row.getCell(2));
					String workflow = getStringValue(row.getCell(3));
					String status = getStringValue(row.getCell(4));
					String roles = getStringValue(row.getCell(5));
					
					//temp id to find failed entries
					AssistTextEntry TempEntry = new AssistTextEntry();
					TempEntry.setClassName(className);
					TempEntry.setAtrrName(attrName);
					TempEntry.setWorkflowID(workflow);
					TempEntry.setWorkflowStatusId(status);
					List<String> roless=Arrays.asList(roles.split(";"));
					TempEntry.setRolesList(roless);
					
					
					//Validating Required fields
					if(className == null || attrName == null || assistText == null || roles == null || 
							className.isEmpty() || attrName.isEmpty() || assistText.isEmpty() || roles.isEmpty()) {
						
						log.error("Row [" + (i + 1) + "] is missing required field values");
						errors++;
						failedTexts.add(TempEntry);
						continue;
					}
					
					//Validate Roles
					List<String> roleIds = new ArrayList<String>();
					String[] rolesArray = roles.trim().split("\\s*;\\s*");
					if(rolesArray.length > 0) {
						for(String role : rolesArray) {
							String roleId = allRoles.get(role);
							if(roleId != null) {
								roleIds.add(roleId);
							} else {
								log.error("Role is invalid or not configured in the Assist+ admin panel: " + role);
								errors++;
								List<String> unconfiguredRole=new ArrayList<String>();
								unconfiguredRole.add("(Role is invalid or not configured in the Assist+ admin panel)");
								TempEntry.setRolesList(unconfiguredRole);
								failedTexts.add(TempEntry);
								continue rowsLoop;
							}
						}
					} else {
						log.error("Row [" + (i + 1) + "] is missing required field values");
						errors++;
						failedTexts.add(TempEntry);
						continue;
					}
					
					AgileClassDO aclassDO = null;
					if(agileClassesCache.containsKey(className)) {
						aclassDO = agileClassesCache.get(className);
					} else {
						IAgileClass agileClass = admin.getAgileClass(className);
						if(agileClass != null) {
							aclassDO = new AgileClassDO();
							aclassDO.agileClass = agileClass;
							aclassDO.classId = (Integer) agileClass.getId();
							
							aclassDO.isItem = agileClass.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS);				
							if(aclassDO.isItem) {
								
							} else {
								aclassDO.isRoutable = (agileClass instanceof IRoutableDesc);
								if(aclassDO.isRoutable) {
									IWorkflow[] workflows = ((IRoutableDesc) agileClass).getWorkflows();
									for(IWorkflow wf : workflows) {
										String wfName = wf.getName();
										aclassDO.workflows.add(wfName);
										
										Set<String> statusNames = new HashSet<String>();
										IStatus[] statuses = wf.getStates();
										for(IStatus st : statuses) {
											statusNames.add(st.getName());
										}
										statusNames.add(ALL_STATUS);
										
										aclassDO.statuses.put(wfName, statusNames);
									}
									
									aclassDO.workflows.add(ALL_WORKFLOWS);
									Set<String> statusNames = new HashSet<String>();
									statusNames.add(ALL_STATUS);
									aclassDO.statuses.put(ALL_WORKFLOWS, statusNames);			
								}
							}
							
							agileClassesCache.put(className, aclassDO);
						} else {
							log.error("Agile Class not found: " + className);
							errors++;
							failedTexts.add(TempEntry);
							continue;
						}
					}
					
					Integer classId = aclassDO.classId;
					
					if(aclassDO.isItem) {
						workflow = ITEMS_WFVALUE;
					}
					
					if(workflow == null || workflow.isEmpty()) {
						if(aclassDO.isRoutable) {
							workflow = ALL_WORKFLOWS;
						}
					}
					
					if(status == null || status.isEmpty() || (workflow+"").equalsIgnoreCase(ALL_WORKFLOWS)) {
						status = ALL_STATUS;
					}
					
					//Validating Workflow and Status
					if(aclassDO.isRoutable) {
						if(!aclassDO.workflows.contains(workflow)) {
							log.error("Workflow not found: " + workflow);
							errors++;
							failedTexts.add(TempEntry);
							continue;
						} else {
							Set<String> wfstatuses = aclassDO.statuses.get(workflow);
							String[] split = status.trim().split("\\s*;\\s*");
							for(String st : split) {
								if(!wfstatuses.contains(st)) {
									log.error("Status not found: " + st);
									errors++;
									failedTexts.add(TempEntry);
									continue rowsLoop;
								}
							}
						}
					}
					
					//Validate Attribute
					Integer attrId = null;
					if(aclassDO.attributes.containsKey(attrName)) {
						attrId = aclassDO.attributes.get(attrName);
					} else {
						IAttribute attr = aclassDO.agileClass.getAttribute(attrName);
						if(attr != null) {
							attrId = (Integer) attr.getId();
							aclassDO.attributes.put(attrName, attrId);
						} else {
							log.error("Attribute not found: " + attrName);
							errors++;
							failedTexts.add(TempEntry);
							continue;
						}
					}
					
					AssistTextEntry entry = new AssistTextEntry();
					entry.setClassID(classId.toString());
					entry.setAttrID(attrId.toString());
					entry.setWorkflowID(workflow);
					entry.setWorkflowStatusId(status);
					entry.setRolesList(roleIds);
					
					
					assistText = assistText.trim();
					if(!assistText.startsWith("<") || !assistText.endsWith(">")) {
						assistText = "<p>" + StringEscapeUtils.escapeHtml4(assistText).replaceAll("\\n", "</p><p>").replaceAll("<p>\\s*<\\/p>", "<p>&nbsp;</p>") + "</p>";
					}
					
					try {
						final byte[] utf8Bytes = assistText.getBytes("UTF-8");
						if(utf8Bytes.length > 4000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of size " + utf8Bytes.length + "Bytes. Maximum size is 4000 Bytes");
							errors++;
							failedTexts.add(TempEntry);
							continue;
						}
					} catch(Exception ex) {
						log.error("Error while getting Byte size of Assist Text: " + ex);
						if(assistText.length() > 2000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of length exceeding the maximum length of 2000.");
							errors++;
							failedTexts.add(TempEntry);
							continue;
						}
					}
					
					if(insertedTexts.indexOf(entry) == -1) {
						int index = currentTextsList.indexOf(entry);
						if(index >= 0) {
							//Update existing Entry
							updatedTextIndexes.add(index);
							AssistTextEntry currentEntry = currentTextsList.get(index);
							currentEntry.setAssistText(assistText);
							currentEntry.setWorkflowStatusId(status);
							//TODO roles
							updateText.put(currentEntry.getTextID(), currentEntry);
							updatedText.add(entry);
							log.info("Row [" + (i + 1) + "] added to the UPDATE list");
						} else {
							//Create new Entry
							entry.setAssistText(assistText);
							entry.setFontColor(FONT_COLOR);
							entry.setBackgroundColor(BACKGROUND_COLOR);
							entry.setIsDifferentColor(false);
							
							insertedTexts.add(entry);
							insertText.put(String.valueOf(insertKey), entry);
							insertKey++;
							
							log.info("Row [" + (i + 1) + "] added to the INSERT list");
						}
						
						success++;
					} else {
						log.error("Cannot import the Row [" + (i + 1) + "]. There is a conflict with another row in the excel file.");
						errors++;
						//add it in failed list
						failedTexts.add(TempEntry);
						//
						continue;
					}
				}
			} catch(Exception ex) {
				log.error("Error while importing Row [" + (i + 1) + "]: " + ex);
				errors++;
				continue;
			}
		}
		
		if(!updateText.isEmpty()) {
			log.info("Performing UPDATE of Assist Texts");
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("updateText", updateText);
			dbh.handleDBRequest("mergeUpdateText", params, true);
		}
		
		if(!insertText.isEmpty()) {
			log.info("Performing INSERT of New Assist Texts");
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("insertText", insertText);
			dbh.handleDBRequest("mergeInsertText", params, true);
		}
		
		if(overwrite) {
			//Delete old Entries
			log.info("Performing DELETE of old Assist Texts");
			
			for(int i = 0; i < currentTextsList.size(); i++) {
				if(!updatedTextIndexes.contains(i)) {
					AssistTextEntry entry = currentTextsList.get(i);
					Map<String, String> params = new HashMap<String, String>();
					params.put("textID", entry.getTextID());
					//add it in deleteEntries list
					Map<String, Object> text= (Map<String, Object>) dbh.handleDBRequest("getAssistTextUsingTextId", params, true);
					AssistText aText=(AssistText) text.get("AssistTextUsingTextId");
					String classId=(String) text.get("classId");
					String attrId=(String) text.get("attrId");
					AssistTextEntry deleteEntry=new AssistTextEntry();
					deleteEntry.setClassID(classId);
					deleteEntry.setAttrID(attrId);
					deleteEntry.setWorkflowID(aText.getWorkflow_lifecycle());
					String statuses="";
					try {
						List<String> allStatuses=aText.getWorkflowStatuses();
						statuses=allStatuses.get(0);
						for(int k1=1; k1<allStatuses.size(); k1++)
							statuses=statuses+";"+allStatuses.get(k1);
						}
						catch(Exception e)
						{
							statuses="";
						}
					deleteEntry.setWorkflowStatusId(statuses);
					deleteEntry.setWorkflowStatuses(aText.getWorkflowStatuses());
					deleteEntry.setRolesList(aText.getRoles());
					deletedTexts.add(deleteEntry);
					//
					dbh.handleDBRequest("removeAssistText", params, true);
				}
			}
		}
		statusMap.put("success", success);
		statusMap.put("errors", errors);
		statusMap.put("insertedEntries", insertedTexts);
		statusMap.put("updatedEntries", updatedText);
		statusMap.put("deletedEntries", deletedTexts);
		statusMap.put("failedEntries", failedTexts);
		
		return statusMap;
	}
	
	@SuppressWarnings("unchecked")
	public void importClassExcelFile(String importType) throws Exception {
		boolean overwrite = "overwrite".equalsIgnoreCase(importType);
		
		//Get all Roles configured in the Admin panel
		Map<String, String> allRoles = new HashMap<String, String>();
		List<RoleEntry> allRoleEntries = (List<RoleEntry>) dbh.handleDBRequest("getRolePriority", null, false).get("rolePriority");
		for(RoleEntry role : allRoleEntries) {
			allRoles.put(role.getRole(), role.getRoleID().toString());
		}
		
		int lastRow = sheet.get(2).getLastRowNum();
		log.debug("Rows in the excel sheet: " + lastRow);
		
		if(overwrite) {
			log.info("Deleting Assist Class Notifications");
			dbh.handleDBRequest("deleteClassNotificationTables", null, false);
		}
		
		// Skip first Row (i=0) for the Header
		rowsLoop: for(int i = 1; i <= lastRow; i++) {
			log.debug("Processing excel row: " + (i + 1));
			XSSFRow row = sheet.get(2).getRow(i);
			
			try {
				if(row != null) {
					String className = getStringValue(row.getCell(0));
					String assistText = getStringValue(row.getCell(1));
					String enableNotification = getStringValue(row.getCell(2));
					String enableOverride = getStringValue(row.getCell(3));
					String fontColor = getStringValue(row.getCell(4));
					String backgroundColor = getStringValue(row.getCell(5));
					String roles = getStringValue(row.getCell(6));
			
					//Validating Required fields
					if(className == null || assistText == null || enableNotification == null || roles == null || 
							className.isEmpty() || assistText.isEmpty() || enableNotification.isEmpty() || roles.isEmpty()) {
						log.error("Row [" + (i + 1) + "] is missing required field values");
						continue;
					}
					
					if(enableOverride == null ||  enableOverride.isEmpty()) {
						enableOverride = "No";
					}
					if(fontColor == null ||  fontColor.isEmpty() || backgroundColor == null || backgroundColor.isEmpty()) {
						fontColor = "#3e3434";
						backgroundColor = "#EEECE1";
					}
					//Validate Roles
					List<String> roleIds = new ArrayList<String>();
					String[] rolesArray = roles.trim().split("\\s*;\\s*");
					if(rolesArray.length > 0) {
						for(String role : rolesArray) {
							String roleId = allRoles.get(role);
							if(roleId != null) {
								roleIds.add(roleId);
							} else {
								log.error("Role is invalid or not configured in the Assist+ admin panel: " + role);
								continue rowsLoop;
							}
						}
					} else {
						log.error("Row [" + (i + 1) + "] is missing required field values");
						continue;
					}
					
					AgileClassDO aclassDO = null;
					if(agileClassesCache.containsKey(className)) {
						aclassDO = agileClassesCache.get(className);
					} else {
						IAgileClass agileClass = admin.getAgileClass(className);
						if(agileClass != null) {
							aclassDO = new AgileClassDO();
							aclassDO.agileClass = agileClass;
							aclassDO.classId = (Integer) agileClass.getId();
							
							aclassDO.isItem = agileClass.isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS);						
							agileClassesCache.put(className, aclassDO);
						} else {
							log.error("Agile Class not found: " + className);
							continue;
						}
					}
					
					Integer classId = aclassDO.classId;
					assistText = assistText.trim();
					if(!assistText.startsWith("<") || !assistText.endsWith(">")) {
						assistText = "<p>" + StringEscapeUtils.escapeHtml4(assistText).replaceAll("\\n", "</p><p>").replaceAll("<p>\\s*<\\/p>", "<p>&nbsp;</p>") + "</p>";
					}
					
					try {
						final byte[] utf8Bytes = assistText.getBytes("UTF-8");
						if(utf8Bytes.length > 2000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of size " + utf8Bytes.length + "Bytes. Maximum size is 2000 Bytes");
							continue;
						}
					} catch(Exception ex) {
						log.error("Error while getting Byte size of Assist Text: " + ex);
						if(assistText.length() > 1000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of length exceeding the maximum length of 1000.");
							continue;
						}
					}
					
					Map<String, Object> params = new HashMap<>();
					params.put("classID", classId.toString());
					params.put("assistText", assistText);
					params.put("notifEnable", enableNotification);
					params.put("overrideEnable", enableOverride);
					params.put("fontColor", fontColor);
					params.put("backgroundColor", backgroundColor);
					params.put("roles", roleIds.toArray(new String[0]));
					dbh.handleDBRequest("toggleClassNotificationMsg", params, true);
				}
			} catch(Exception ex) {
				log.error("Error while importing Row [" + (i + 1) + "]: " + ex);
				continue;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void importNotificationExcelFile(String importType) throws Exception {		
		//Get all Roles configured in the Admin panel
		Map<String, String> allRoles = new HashMap<>();
		List<RoleEntry> allRoleEntries = (List<RoleEntry>) dbh.handleDBRequest("getRolePriority", null, false).get("rolePriority");
		for(RoleEntry role : allRoleEntries) {
			allRoles.put(role.getRole(), role.getRoleID().toString());
		}
		
		int lastRow = sheet.get(1).getLastRowNum();
		log.debug("Rows in the excel sheet: " + lastRow);
		
		// Skip first Row (i=0) for the Header
		rowsLoop: for(int i = 1; i <= lastRow; i++) {
			log.debug("Processing excel row: " + (i + 1));
			XSSFRow row = sheet.get(1).getRow(i);
			
			try {
				if(row != null) {
					String assistText = getStringValue(row.getCell(0));
					String enableNotification = getStringValue(row.getCell(1));
					String enableOptOut = getStringValue(row.getCell(2));
					String enableDuration = getStringValue(row.getCell(3));
					String durationLimit = getStringValue(row.getCell(4));
					String fontColor = getStringValue(row.getCell(5));
					String backgroundColor = getStringValue(row.getCell(6));
					String roles = getStringValue(row.getCell(7));
			
					//Validating Required fields
					if(enableOptOut == null || assistText == null || enableNotification == null || enableDuration == null
							|| roles == null || enableOptOut.isEmpty() || assistText.isEmpty() || enableNotification.isEmpty()
							|| enableDuration.isEmpty() || roles.isEmpty()) {
						
						log.error("Row [" + (i + 1) + "] is missing required field values");
						continue;
					}
					
					if(enableDuration.equals("Yes") && (durationLimit == null || durationLimit.isEmpty())) {
						log.error("Row [" + (i + 1) + "] is missing required field values");
						continue;
					}
					
					if(fontColor == null ||  fontColor.isEmpty() || backgroundColor == null || backgroundColor.isEmpty()) {
						fontColor = "#3e3434";
						backgroundColor = "#EEECE1";
					}
					
					//Validate Roles
					List<String> roleIds = new ArrayList<String>();
					String[] rolesArray = roles.trim().split("\\s*;\\s*");
					if(rolesArray.length > 0) {
						for(String role : rolesArray) {
							String roleId = allRoles.get(role);
							if(roleId != null) {
								roleIds.add(roleId);
							} else {
								log.error("Role is invalid or not configured in the Assist+ admin panel: " + role);
								continue rowsLoop;
							}
						}
					} else {
						log.error("Row [" + (i + 1) + "] is missing required field values");
						continue;
					}
					
					assistText = assistText.trim();
					if(!assistText.startsWith("<") || !assistText.endsWith(">")) {
						assistText = "<p>" + StringEscapeUtils.escapeHtml4(assistText).replaceAll("\\n", "</p><p>").replaceAll("<p>\\s*<\\/p>", "<p>&nbsp;</p>") + "</p>";
					}
					
					try {
						final byte[] utf8Bytes = assistText.getBytes("UTF-8");
						if(utf8Bytes.length > 2000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of size " + utf8Bytes.length + "Bytes. Maximum size is 2000 Bytes");
							continue;
						}
					} catch(Exception ex) {
						log.error("Error while getting Byte size of Assist Text: " + ex);
						if(assistText.length() > 1000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of length exceeding the maximum length of 1000.");
							continue;
						}
					}
					
					List<String> configs = new ArrayList<>();
					//List<String> roles = new ArrayList<>();
					configs.add("isNotifEnabled" + "=" + enableNotification);
					configs.add("isAckEnabled" + "=" + enableOptOut);
					configs.add("fontNotifColor"+"="+fontColor);
					configs.add("backgroundNotifColor"+"="+backgroundColor);
					configs.add("isDurationEnabled"+"="+enableDuration);
					configs.add("durationLimit"+"="+durationLimit);		
					
					Map msgMap = dbh.handleDBRequest("getNotificationMsgOnly", null, true);
					String assistMessage = msgMap.containsKey("assistMessage")?  (String) msgMap.get("assistMessage"): "";
					if(!assistMessage.equals(assistText)) {
						configs.add("notificationMsg"+ "="+assistText);
					}
					
					HashMap<String, Object> params = new HashMap<>();
					params.put("configs", configs.toArray(new String[0]));
					params.put("roles", roleIds.toArray(new String[0]));
					
					dbh.handleDBRequest("toggleNotificationMsg", params, true);
				}
			} catch(Exception ex) {
				log.error("Error while importing Row [" + (i + 1) + "]: " + ex);
				continue;
			}
		}
	}

	
	private String getStringValue(XSSFCell cell) {
		if(cell != null) {
			if(cell.getCellType() != XSSFCell.CELL_TYPE_STRING) {
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			}
			
			return cell.getStringCellValue();
		}
		return null;
	}
	
	private class AgileClassDO {
		private IAgileClass agileClass = null;
		private Integer classId = null;
		private boolean isRoutable = false;
		private boolean isItem = false;
		private Set<String> workflows = new HashSet<String>();
		private Map<String, Set<String>> statuses = new HashMap<String, Set<String>>();
		private Map<String, Integer> attributes = new HashMap<String, Integer>(); 
	}
}
