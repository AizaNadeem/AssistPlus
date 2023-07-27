package com.xavor.plmxl.assist.Handler;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.xavor.plmxl.assist.Util.AssistLogger;

public class Handler implements ErrorHandler {
	private boolean hasErrors;
	private boolean isUserGroupPriority;
	private boolean isRolePriority;
	private boolean accessTypeOkay;
	
	AssistLogger log=AssistLogger.getInstance();
	  public Handler()
	  {
	   hasErrors = false;
	  }
	  
	  public boolean getHasErrors()
	  {
	   return hasErrors;
	  }
	  
	  public boolean isUserGroupPriority() {
		return isUserGroupPriority;
	}

	public void setUserGroupPriority(boolean isUserGroupPriority) {
		this.isUserGroupPriority = isUserGroupPriority;
	}

	public boolean isRolePriority() {
		return isRolePriority;
	}

	public void setRolePriority(boolean isRolePriority) {
		this.isRolePriority = isRolePriority;
	}

	public void setHasErrors(boolean b)
	  {
	   this.hasErrors = b;
	  }
	  
	public void error(SAXParseException exception) throws SAXException {
		log.error("Line: " + exception.getLineNumber() + ") " , exception);
		if(exception.toString().contains("Invalid content was found starting with element 'UserGroupPriority'"))
		{
			isRolePriority=true;
			isUserGroupPriority=false;
			accessTypeOkay=false;
		}
		else if(exception.toString().contains("Invalid content was found starting with element 'RolePriority'"))
		{
			isUserGroupPriority=true;
			isRolePriority=false;
			accessTypeOkay=false;
		}
		else
		{
			accessTypeOkay=true;
		}
		hasErrors = true;
	}

	public boolean isAccessTypeOkay() {
		return accessTypeOkay;
	}

	public void setAccessTypeOkay(boolean accessTypeOkay) {
		this.accessTypeOkay = accessTypeOkay;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void warning(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	

}
