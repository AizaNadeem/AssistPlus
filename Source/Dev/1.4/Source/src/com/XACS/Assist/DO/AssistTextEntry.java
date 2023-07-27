package com.XACS.Assist.DO;

public class AssistTextEntry {
	String		textID, classID, attrID, assistText, fontcolor, background;
	boolean		isDiffColor;
	String[]	roles;

	public String getAttrID() {
		return attrID;
	}

	public void setAttrID(String attrID) {
		this.attrID = attrID;
	}

	public String getAssistText() {
		return assistText;
	}

	public void setAssistText(String assistText) {
		this.assistText = assistText;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String getClassID() {
		return classID;
	}

	public void setClassID(String classID) {
		this.classID = classID;
	}

	public String getTextID() {
		return textID;
	}

	public void setTextID(String textID) {
		this.textID = textID;
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

	public boolean getIsDifferentColor() {
		return isDiffColor;
	}

	public void setIsDifferentColor(boolean _isDifferentColor) {
		isDiffColor = _isDifferentColor;
	}
}
