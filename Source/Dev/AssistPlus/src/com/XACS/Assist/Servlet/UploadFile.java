package com.XACS.Assist.Servlet;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.Constants;
import com.oreilly.servlet.MultipartRequest;



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
			String path=System.getenv(Constants.Config.HOME_ENVVAR);
			MultipartRequest m=new MultipartRequest(request,path+Constants.Config.ASSISTPLUSPATH);
			Enumeration files = m.getFileNames();
			String filename=null;
			while (files.hasMoreElements())
			{
				String name = (String)files.nextElement();
				filename = m.getFilesystemName(name);

			}
			String newfile=filename.replace(".","-");
			String[] split=newfile.split("-");

			if(!split[1].equals("xml"))
			{
				File file = new File(path+Constants.Config.ASSISTPLUSPATH+filename);
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
