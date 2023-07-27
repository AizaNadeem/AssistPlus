package com.xavor.plmxl.assist.DO;

public class AttributeEntry 
{
	public String classIdVal="";
	public String attrIdVal="";
	public String attrName="";
	public String attrDescription="";
	public String hasTextFlag="";
	public String assistColor="";
	public String assistColorId="";
	public String isVisible="";
	
	public String getAttrName() 
	{
		return attrName;
	}

	public void setAttrName(String attrName) 
	{
		this.attrName = attrName;
	}

	public String getClassID() 
	{
		return classIdVal;
	}

	public void setClassID(String classID) 
	{
		this.classIdVal = classID;
	}

	public String getAttrID() 
	{
		return attrIdVal;
	}

	public void setAttrID(String attrID) 
	{
		this.attrIdVal = attrID;
	}

	public String getAttrDescription() 
	{
		return attrDescription;
	}

	public void setAttrDescription(String attrDescription) 
	{
		this.attrDescription = attrDescription;
	}
	
	public String gethasTextFlag() 
	{
		return hasTextFlag;
	}

	public void sethasTextFlag(String hasTextFlag) 
	{
		this.hasTextFlag = hasTextFlag;
	}
		public String getAssistColorId() {
		return assistColorId;
	}

	public void setAssistColorId(String assistColorId) {
		this.assistColorId = assistColorId;
	}

	public String getAssistColor() {
		return assistColor;
	}

	public void setAssistColor(String assistColor) {
		this.assistColor = assistColor;
	}

	public String getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}
	
}
