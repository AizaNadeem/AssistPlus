package com.xavor.plmxl.assist.DO;

import java.util.ArrayList;
import java.util.List;

public class AssistText {
	private List<String> roles;
	private List<String> workflowStatuses;
	private String workflow_lifecycle;
	private String text, DateCreated, lastUpdated;
	private String backgroundColor;
	private String fontColor;
	private String textID;
	private boolean	isDiffColor;
	
	
	public AssistText() {
		
	}
	
	
	
	public String getTextID() {
		return textID;
	}



	public void setTextID(String textID) {
		this.textID = textID;
	}



	public String getDateCreated() {
		return DateCreated;
	}



	public void setDateCreated(String dateCreated) {
		DateCreated = dateCreated;
	}



	public String getLastUpdated() {
		return lastUpdated;
	}



	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}



	public boolean getIsDifferentColor() {
		return isDiffColor;
	}

	public void setIsDiffColor(boolean isDiffColor) {
		this.isDiffColor = isDiffColor;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public List<String> getWorkflowStatuses() {
		return workflowStatuses;
	}
	public void setWorkflowStatuses(List<String> workflowStatuses) {
		this.workflowStatuses = workflowStatuses;
	}
	public String getWorkflow_lifecycle() {
		return workflow_lifecycle;
	}
	public void setWorkflow_lifecycle(String workflow_lifecycle) {
		this.workflow_lifecycle = workflow_lifecycle;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	
}
