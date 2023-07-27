package com.xavor.plmxl.assist.DO;

import java.util.List;

public class AssistNotificationEntry {
	private String MsgID;
	private String AssistMessage;
	private String DurationEnable; 
	private String DurationLimit;
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
	public String getMsgID() {
		return MsgID;
	}
	public void setMsgID(String msgID) {
		MsgID = msgID;
	}
	public String getAssistMessage() {
		return AssistMessage;
	}
	public void setAssistMessage(String assistMessage) {
		AssistMessage = assistMessage;
	}
	public String getDurationLimit() {
		return DurationLimit;
	}
	public void setDurationLimit(String durationLimit) {
		DurationLimit = durationLimit;
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
	public String getDateCreated() {
		return DateCreated;
	}
	public void setDateCreated(String dateCreated) {
		DateCreated = dateCreated;
	}
	public String getDurationEnable() {
		return DurationEnable;
	}
	public void setDurationEnable(String durationEnabled) {
		DurationEnable = durationEnabled;
	}
}