package com.xavor.plmxl.assist.DO;

import java.util.ArrayList;
import java.util.List;

public class CListModel 
{
	private String id="";
	private String value="";
	private List<CListModel> childs=new ArrayList<CListModel>(0);
	
	public CListModel(String id,String value,List<CListModel> childs)
	{
		this.id=id;
		this.value=value;
		this.childs=childs;
	}
	public CListModel(String id,String value)
	{
		this.id=id;
		this.value=value;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<CListModel> getChilds() {
		return childs;
	}

	public void setChilds(List<CListModel> childs) {
		this.childs = childs;
	}
	
	
}
