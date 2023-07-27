package com.xavor.plmxl.assist.DO;

public class RoleEntry {
	String	RoleID, Role;
	int		Priority;
	String	fontcolor;
	String	background;

	public String getRoleID() {
		return RoleID;
	}

	public void setRoleID(String roleID) {
		this.RoleID = roleID;
	}

	public String getRole() {
		return Role;
	}

	public void setRole(String role) {
		Role = role;
	}

	public int getPriority() {
		return Priority;
	}

	public void setPriority(int priority) {
		this.Priority = priority;
	}

	public void setFontColor(String color) {
		fontcolor = color;
	}

	public String getFontColor() {
		return fontcolor;
	}

	public void setBackgroundColor(String color) {
		background = color;
	}

	public String getBackgroundColor() {
		return background;
	}
}
