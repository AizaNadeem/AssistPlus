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

public class UploadFile extends HttpServlet {

	private static final long serialVersionUID = 1L;
	AssistLogger log = AssistLogger.getInstance();

	@SuppressWarnings("rawtypes")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		log.debug("Entering UploadFile...");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		try {
			log.debug("Entering UploadFile Try..");
			String path = ConfigHelper.getAppHomePath();
			MultipartRequest m = new MultipartRequest(request, path);
			Enumeration files = m.getFileNames();
			String filename = null;
			while(files.hasMoreElements()) {
				String name = (String) files.nextElement();
				filename = m.getFilesystemName(name);

			}

			if(!filename.endsWith(".xlsx") && !filename.endsWith(".xml")) {
				log.info("Import file is not a valid xlsx or xml file: " + filename);
				File file = new File(path + filename);
				file.delete();
			} else {
				log.debug("File uploaded: " + filename);
			}
		} catch(IOException ioe) {
			log.error("IOException: ", ioe);
		} catch(Exception e) {
			log.error("Exception: ", e);
		}

		log.debug("Exiting UploadFile..");
	}

}
