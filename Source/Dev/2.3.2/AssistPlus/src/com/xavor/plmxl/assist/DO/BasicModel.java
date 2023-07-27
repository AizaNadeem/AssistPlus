package com.xavor.plmxl.assist.DO;

public class BasicModel 
{
	private String id="";
	private String value="";
	
	public BasicModel(String id,String value)
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
	
}
