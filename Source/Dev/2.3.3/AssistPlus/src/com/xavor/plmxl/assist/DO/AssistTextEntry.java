package com.xavor.plmxl.assist.DO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssistTextEntry {
	private String		TextID, ClassID, AttrID, AssistText, fontcolor, background, workflowID, workflowStatusId, DateCreated, lastUpdated;
	private boolean		isDiffColor;
	private String[]	roles;
	private List<String> RoleList;
	private String 		ClassName;
	private String		AttributeName;
	private List<String> workflowStatuses;
	

	
	public List<String> getRolesList() {
		return RoleList;
	}

	public void setRolesList(List<String> rolesList) {
		this.RoleList = rolesList;
	}

	public boolean getIsDifferentColor() {
		return isDiffColor;
	}

	public String getWorkflowStatusId() {
		return workflowStatusId;
	}

	public void setWorkflowStatusId(String workflowStatusId) {
		this.workflowStatusId = workflowStatusId;
	}

	public String getWorkflowID() {
		return workflowID;
	}

	public String getClassName() {
		return ClassName;
	}

	public void setClassName(String className) {
		this.ClassName = className;
	}

	public String getAtrrName() {
		return AttributeName;
	}

	public void setAtrrName(String atrrName) {
		this.AttributeName = atrrName;
	}

	public String getAttrID() {
		return AttrID;
	}

	public void setAttrID(String attrID) {
		this.AttrID = attrID;
	}

	public String getAssistText() {
		return AssistText;
	}

	public void setAssistText(String assistText) {
		this.AssistText = assistText;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String getClassID() {
		return ClassID;
	}

	public void setClassID(String classID) {
		this.ClassID = classID;
	}

	public String getTextID() {
		return TextID;
	}

	public void setTextID(String textID) {
		this.TextID = textID;
	}

	public String getFontColor() {
		return fontcolor;
	}

	public void setFontColor(String _color) {
		fontcolor = _color;
	}

	public String getBackgroundColor() {
		return background;
	}

	public void setBackgroundColor(String _color) {
		background = _color;
	}

	
	public void setIsDifferentColor(boolean _isDifferentColor) {
		isDiffColor = _isDifferentColor;
	}

	public void setWorkflowID(String WorkflowID) {
		workflowID = WorkflowID;
	}

	public void setWorkflowStatusID(String WorkflowStatusID) {
		workflowStatusId = WorkflowStatusID;
	}

	public String getDateCreated() {
		return DateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.DateCreated = dateCreated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AssistTextEntry) {
			AssistTextEntry entry = (AssistTextEntry) obj;
			if(ClassID.equals(entry.ClassID) && AttrID.equals(entry.AttrID)) {
				
				if(workflowID == null || workflowID.isEmpty() || workflowID.equals("Lifecycles") || workflowID.equals("All Workflows") || 
						entry.workflowID == null || entry.workflowID.isEmpty() || entry.workflowID.equals("Lifecycles") || entry.workflowID.equals("All Workflows") || 
						workflowID.equalsIgnoreCase(entry.workflowID)) {
					
					List<String> status1 = new ArrayList<String>();
					List<String> status2 = new ArrayList<String>();
					
					if(workflowStatusId != null && !workflowStatusId.trim().isEmpty()) {
						String[] split1 = workflowStatusId.trim().split("\\s*;\\s*");
						for(int j=0; j<split1.length; j++) {
							status1.add(split1[j]);
						}
					}
					
					if(entry.workflowStatusId != null && !entry.workflowStatusId.trim().isEmpty()) {
						String[] split2 = entry.workflowStatusId.trim().split("\\s*;\\s*");
						for(int j=0; j<split2.length; j++) {
							status2.add(split2[j]);
						}
					}
					
					if(status1.isEmpty() || status1.contains("All Statuses") || status2.isEmpty() || status2.contains("All Statuses") || !Collections.disjoint(status1, status2)) {
						return RoleList.contains("0") || entry.RoleList.contains("0") || !Collections.disjoint(RoleList, entry.RoleList);
					}
				}
			}
		}
		
		return false;
	}
	public boolean strictlyEquals(Object obj) {
		if(obj instanceof AssistTextEntry) {
			AssistTextEntry entry = (AssistTextEntry) obj;
			if(ClassID.equals(entry.ClassID) && AttrID.equals(entry.AttrID)) {
				
				if(workflowID.equalsIgnoreCase(entry.workflowID)) {
					
					List<String> status1 = new ArrayList<String>();
					List<String> status2 = new ArrayList<String>();
					
					//for case when workflow/lifecycle= All Workflows/ All LifeCylces
					if(workflowStatusId.isEmpty()) {
						status1.add("A");
					}
					if(entry.workflowStatusId.isEmpty()) {
						status2.add("A");
					}
				
					
					
					
					
					if(workflowStatusId != null && !workflowStatusId.trim().isEmpty()) {
						String[] split1 = workflowStatusId.trim().split("\\s*;\\s*");
						for(int j=0; j<split1.length; j++) {
							status1.add(split1[j]);
						}
					}
//					else if(!(workflowStatusId != null) && workflowStatusId.trim().isEmpty())
//						status1.add("A");
					
					if(entry.workflowStatusId != null && !entry.workflowStatusId.trim().isEmpty()) {
						String[] split2 = entry.workflowStatusId.trim().split("\\s*;\\s*");
						for(int j=0; j<split2.length; j++) {
							status2.add(split2[j]);
						}
					}
//					else if(!(entry.workflowStatusId != null) && entry.workflowStatusId.trim().isEmpty())
//						status2.add("A");
					
					if(!Collections.disjoint(status1, status2)) {
						return !Collections.disjoint(RoleList, entry.RoleList);
					}
				}
			}
		}
		
		return false;
	}

	public List<String> getWorkflowStatuses() {
		return workflowStatuses;
	}

	public void setWorkflowStatuses(List<String> workflowStatuses) {
		this.workflowStatuses = workflowStatuses;
	}
}
