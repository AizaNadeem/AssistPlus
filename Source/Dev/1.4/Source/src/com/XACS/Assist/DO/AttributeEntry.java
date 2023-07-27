package com.XACS.Assist.DO;

public class AttributeEntry {
	String classIdVal, attrIdVal, attrName, attrDescription ,hasTextFlag;

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public String getClassID() {
		return classIdVal;
	}

	public void setClassID(String classID) {
		this.classIdVal = classID;
	}

	public String getAttrID() {
		return attrIdVal;
	}

	public void setAttrID(String attrID) {
		this.attrIdVal = attrID;
	}

	public String getAttrDescription() {
		return attrDescription;
	}

	public void setAttrDescription(String attrDescription) {
		this.attrDescription = attrDescription;
	}
	
	public String gethasTextFlag() {
		return hasTextFlag;
	}

	public void sethasTextFlag(String hasTextFlag) {
		this.hasTextFlag = hasTextFlag;
	}
	
}
