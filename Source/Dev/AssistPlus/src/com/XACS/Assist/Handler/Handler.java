package com.XACS.Assist.Handler;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.XACS.Assist.Util.AssistLogger;

public class Handler implements ErrorHandler {
	private boolean hasErrors;
	AssistLogger log=AssistLogger.getInstance();
	  public Handler()
	  {
	   hasErrors = false;
	  }
	  
	  public boolean getHasErrors()
	  {
	   return hasErrors;
	  }
	  
	  public void setHasErrors(boolean b)
	  {
	   this.hasErrors = b;
	  }
	  
	public void error(SAXParseException exception) throws SAXException {
		log.error("Line: " + exception.getLineNumber() + ") " , exception);
		hasErrors = true;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void warning(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	

}
