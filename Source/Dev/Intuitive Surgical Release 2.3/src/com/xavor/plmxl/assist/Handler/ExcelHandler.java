package com.xavor.plmxl.assist.Handler;

import java.io.InputStream;
import java.util.ArrayList;
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
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.RoleEntry;
import com.xavor.plmxl.assist.Util.AssistLogger;

public class ExcelHandler {

	private AssistLogger log = AssistLogger.getInstance();
	private DBHandler dbh = null;
	
	private XSSFSheet sheet = null;
	private XSSFWorkbook book = null;
	
	private IAdmin admin = null;
	
	private Map<String, AgileClassDO> agileClassesCache = null;
	
	private final String ITEMS_WFVALUE = "Lifecycles";
	private final String ALL_STATUS = "All Statuses";
	private final String ALL_WORKFLOWS = "All Workflows";
	
	public ExcelHandler(InputStream inputStream) throws Exception {
		dbh = new DBHandler();
		book = new XSSFWorkbook(inputStream);
		sheet = book.getSheetAt(0);
		admin = AgileHandler.getAgileSession().getAdminInstance();
		agileClassesCache = new HashMap<String, AgileClassDO>();
	}
	
	@SuppressWarnings("unchecked")
	public String importExcelFile(String importType) throws Exception {
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
		
		int lastRow = sheet.getLastRowNum();
		log.debug("Rows in the excel sheet: " + lastRow);

		Map<String, AssistTextEntry> updateText = new HashMap<String, AssistTextEntry>();
		Map<String, AssistTextEntry> insertText = new HashMap<String, AssistTextEntry>();
		int insertKey = 1;
		List<Integer> updatedTextIndexes = new ArrayList<Integer>();
		List<AssistTextEntry> insertedTexts = new ArrayList<AssistTextEntry>();
		
		int success = 0;
		int errors = 0;
		
		// Skip first Row (i=0) for the Header
		rowsLoop: for(int i = 1; i <= lastRow; i++) {
			log.debug("Processing excel row: " + (i + 1));
			XSSFRow row = sheet.getRow(i);
			
			try {
				if(row != null) {
					String className = getStringValue(row.getCell(0));
					String attrName = getStringValue(row.getCell(1));
					String assistText = getStringValue(row.getCell(2));
					String workflow = getStringValue(row.getCell(3));
					String status = getStringValue(row.getCell(4));
					String roles = getStringValue(row.getCell(5));
					
					//Validating Required fields
					if(className == null || attrName == null || assistText == null || roles == null || 
							className.isEmpty() || attrName.isEmpty() || assistText.isEmpty() || roles.isEmpty()) {
						
						log.error("Row [" + (i + 1) + "] is missing required field values");
						errors++;
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
								continue rowsLoop;
							}
						}
					} else {
						log.error("Row [" + (i + 1) + "] is missing required field values");
						errors++;
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
					
					if(status == null || status.isEmpty() || workflow.equalsIgnoreCase(ALL_WORKFLOWS)) {
						status = ALL_STATUS;
					}
					
					//Validating Workflow and Status
					if(aclassDO.isRoutable) {
						if(!aclassDO.workflows.contains(workflow)) {
							log.error("Workflow not found: " + workflow);
							errors++;
							continue;
						} else {
							Set<String> wfstatuses = aclassDO.statuses.get(workflow);
							String[] split = status.trim().split("\\s*;\\s*");
							for(String st : split) {
								if(!wfstatuses.contains(st)) {
									log.error("Status not found: " + st);
									errors++;
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
							continue;
						}
					}
					
					AssistTextEntry entry = new AssistTextEntry();
					entry.setClassID(classId.toString());
					entry.setAttrID(attrId.toString());
					entry.setWorkflowID(workflow);
					entry.setWorkflowStatusID(status);
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
							continue;
						}
					} catch(Exception ex) {
						log.error("Error while getting Byte size of Assist Text: " + ex);
						if(assistText.length() > 2000) {
							log.error("Row [" + (i + 1) + "] has Assist Text of length exceeding the maximum length of 2000.");
							errors++;
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
							currentEntry.setWorkflowStatusID(status);
							//TODO roles
							updateText.put(currentEntry.getTextID(), currentEntry);
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
					dbh.handleDBRequest("removeAssistText", params, true);
				}
			}
		}
		
		return "Import completed. Successful Rows: " + success + ", Errors: " + errors;
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
