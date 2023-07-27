package com.xavor.plmxl.assist.DO;

import java.util.List;

public class AssistAttributeEntry {

	private String attrID;
	private List<AssistText> texts;
	private String LabelColor;
	
	
	public String getAttrID() {
		return attrID;
	}
	public void setAttrID(String attrID) {
		this.attrID = attrID;
	}
	public List<AssistText> getTexts() {
		return texts;
	}
	public void setTexts(List<AssistText> texts) {
		this.texts = texts;
	}
	public String getLabelColor() {
		return LabelColor;
	}
	public void setLabelColor(String labelColor) {
		LabelColor = labelColor;
	}
	
}
