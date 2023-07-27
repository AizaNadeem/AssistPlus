package com.XACS.Assist.Servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.Constants;

public class ExportXML extends HttpServlet {

	AssistLogger log=AssistLogger.getInstance();
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)	throws ServletException
	{
		log.debug("Entering ExportXML:doPost..");
		
		String abspath=System.getenv(Constants.Config.HOME_ENVVAR);
		log.debug(abspath+Constants.Config.ASSISTPLUSPATH+Constants.XML.XMLFileName);
		
		File file = new File(abspath+Constants.Config.ASSISTPLUSPATH+Constants.XML.XMLFileName);
		
		response.setContentType("text/xml");
		response.setHeader("Content-Disposition","attachment;filename=export.xml");
		
		FileInputStream fileIn;
		try {
			
			fileIn = new FileInputStream(file);

			ServletOutputStream os = response.getOutputStream();
			byte[] bufferData = new byte[1024];
			int read=0;

			while((read = fileIn.read(bufferData))!= -1)
			{
				os.write(bufferData, 0, read);

			}
			os.flush();
			os.close();
			fileIn.close();
			file.delete();

		} catch (FileNotFoundException e) 
		{
			log.error("FileNotFoundException: ", e);
			
		} catch (IOException e) {
			
			log.error("IOException", e);
		}
		
		log.debug("Exiting ExportXML:doPost..");
		
	}
}
