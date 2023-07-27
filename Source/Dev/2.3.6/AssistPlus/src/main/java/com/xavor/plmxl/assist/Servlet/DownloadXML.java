package com.xavor.plmxl.assist.Servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		SimpleDateFormat simpleDateFormatter= new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();
	    
		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());		
		
		String timeStamp =simpleDateFormatter.format(today);
		response.setContentType("text/xml");
		response.setHeader("Content-Disposition","attachment;filename=AssistPlus Export-"+timeStamp+".xml");
		
		try (FileInputStream fileIn = new FileInputStream(new File(ConfigHelper.getAppHomePath()+Constants.XML.XMLFileName));
				ServletOutputStream os = response.getOutputStream()){
			log.debug(ConfigHelper.getAppHomePath()+Constants.XML.XMLFileName);
			
			byte[] bufferData = new byte[ (int) fileIn.getChannel().size()];
			int read=0;

			while((read = fileIn.read(bufferData))!= -1) {
				os.write(bufferData, 0, read);
			}

		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException: ", e);		
		} catch (IOException e) {
			log.error("IOException: ", e);
		} catch (Exception e) {
			log.error("Exception: ", e);
		}
		
		log.debug("Exiting DownloadXML:doPost..");
		
	}
}
