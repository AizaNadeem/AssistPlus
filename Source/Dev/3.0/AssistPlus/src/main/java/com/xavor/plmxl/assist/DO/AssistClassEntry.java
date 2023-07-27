package com.xavor.plmxl.assist.DO;

import java.util.List;
import java.util.Map;

public class AssistClassEntry {
	private Map<String, AssistAttributeEntry> attrMap;
	private List<AssistAttributeEntry> attributes;
	private String classID;

	public Map<String, AssistAttributeEntry> getAttrMap() {
		return attrMap;
	}
	public void setAttrMap(Map<String, AssistAttributeEntry> attrMap) {
		this.attrMap = attrMap;
	}
	public List<AssistAttributeEntry> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<AssistAttributeEntry> attributes) {
		this.attributes = attributes;
	}
	public String getClassID() {
		return classID;
	}
	public void setClassID(String classID) {
		this.classID = classID;
	}	
	
}
