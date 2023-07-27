package com.XACS.Assist.DO;

import java.util.List;

public class AssistTextEntry {
	String		TextID, ClassID, AttrID, AssistText, fontcolor, background, workflowID, workflowStatusId, DateCreated, lastUpdated;
	boolean		isDiffColor;
	String[]	roles;
	List<String> RoleList;
	String 		ClassName;
	String		AttributeName;

	
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
	
}
