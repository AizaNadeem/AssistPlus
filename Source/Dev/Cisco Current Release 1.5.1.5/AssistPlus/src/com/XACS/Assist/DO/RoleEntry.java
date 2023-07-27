package com.XACS.Assist.DO;

public class RoleEntry {
	String	roleID, Role;
	int		priority;
	String	fontColor;
	String	backgroundColor;

	public String getRoleID() {
		return roleID;
	}

	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}

	public String getRole() {
		return Role;
	}

	public void setRole(String role) {
		Role = role;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setFontColor(String color) {
		fontColor = color;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setBackgroundColor(String color) {
		backgroundColor = color;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}
}
