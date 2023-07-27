package com.xavor.plmxl.assist.DO;

public class ReturnStatus {
	String status, message;
	Object object;

	public ReturnStatus(String s, String m)
	{
		setStatus(s);
		setMessage(m);
	}

	public ReturnStatus(String s, String m, Object o)
	{
		setStatus(s);
		setMessage(m);
		setObject(o);
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
