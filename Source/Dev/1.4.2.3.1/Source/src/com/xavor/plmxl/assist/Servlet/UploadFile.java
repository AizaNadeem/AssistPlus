package com.xavor.plmxl.assist.Servlet;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.MultipartRequest;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;



public class UploadFile  extends HttpServlet{
	
	AssistLogger log=AssistLogger.getInstance();
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException
			{
		log.debug("Entering UploadFile..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		try {
			log.debug("Entering UploadFile Try..");
			String path=ConfigHelper.getAppHomePath();
			MultipartRequest m=new MultipartRequest(request,path);
			Enumeration files = m.getFileNames();
			String filename=null;
			while (files.hasMoreElements())
			{
				String name = (String)files.nextElement();
				filename = m.getFilesystemName(name);

			}
			
			String[] split=filename.split(".");

			if(split.length==2 && !split[1].equals("xml"))
			{
				log.debug("Not an XML File..");
				File file = new File(path+filename);
				file.delete();
			}
			else
			{
				log.debug("File Uploaded..");
			}


		}  catch (IOException ioe) {
			log.error("IOException: ", ioe);
		} catch (Exception e) {
			log.error("Exception: ", e);
		}


		log.debug("Exiting UploadFile.."); 
	}

}
