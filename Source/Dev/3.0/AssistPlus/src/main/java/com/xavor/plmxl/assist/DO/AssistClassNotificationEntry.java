package com.xavor.plmxl.assist.DO;

import java.util.List;

public class AssistClassNotificationEntry {
	private String TextID;
	private String ClassID;
	private String ClassName;
	private String AssistText;
	private String NotifEnable;
	private String OverrideEnable;
	private String FontColor;
	private String BackgroundColor;
	private String DateCreated;
	private List<String> RoleList;
	
	public List<String> getRoleList() {
		return RoleList;
	}

	public void setRoleList(List<String> roleList) {
		RoleList = roleList;
	}
	
	public String getFontColor() {
		return FontColor;
	}

	public void setFontColor(String fontColor) {
		FontColor = fontColor;
	}

	public String getBackgroundColor() {
		return BackgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		BackgroundColor = backgroundColor;
	}

	public String getAssistText() {
		return AssistText;
	}

	public void setAssistText(String assistText) {
		this.AssistText = assistText;
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

	public String getNotifEnable() {
		return NotifEnable;
	}

	public void setNotifEnable(String notifEnable) {
		NotifEnable = notifEnable;
	}

	public String getOverrideEnable() {
		return OverrideEnable;
	}

	public void setOverrideEnable(String overrideEnable) {
		OverrideEnable = overrideEnable;
	}

	public void setTextID(String textID) {
		this.TextID = textID;
	}

	public String getDateCreated() {
		return DateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.DateCreated = dateCreated;
	}
	
	public String getClassName() {
		return ClassName;
	}

	public void setClassName(String className) {
		ClassName = className;
	}
}
