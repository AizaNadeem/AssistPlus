package com.xavor.plmxl.assist.Servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class DownloadXML extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	AssistLogger log=AssistLogger.getInstance();
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)	throws ServletException
	{
		log.debug("Entering DownloadXML:doPost..");
				
		response.setContentType("text/xml");
		response.setHeader("Content-Disposition","attachment;filename=export.xml");
		
		FileInputStream fileIn;
		try {
			String abspath=ConfigHelper.getAppHomePath();
			log.debug(abspath+Constants.XML.XMLFileName);
			
			File file = new File(abspath+Constants.XML.XMLFileName);
			fileIn = new FileInputStream(file);
			
			ServletOutputStream os = response.getOutputStream();
			byte[] bufferData = new byte[ (int) fileIn.getChannel().size()];
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
			
		} catch (IOException e) 
		{
			log.error("IOException: ", e);
		} catch (Exception e) 
		{
			log.error("Exception: ", e);
		}
		
		log.debug("Exiting DownloadXML:doPost..");
		
	}
}
