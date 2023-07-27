package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FilenameUtils;
import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.ExcelHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class ImportExcel
 */
public class ImportExcel extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AssistLogger log = AssistLogger.getInstance();

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ImportExcel: doPost...");
		String jsonResponse = "";
		
		try {
			String type = null;
			InputStream fileContent = null;
			String fileName = null;
			
			if(ServletFileUpload.isMultipartContent(request)) {
				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
				for (FileItem item : items) {
					if (!item.isFormField()) {
						// Process form file field (input type="file")
						fileName = FilenameUtils.getName(item.getName());
						if(fileName.endsWith(".xlsx")) {
							log.info("Importing xlsx file: " + fileName);
							fileContent = item.getInputStream();
						} else {
							throw new Exception("Excel file is invalid [" + fileName + "]");
						}
					} else {
						String attr = item.getFieldName();
						InputStream stream = item.getInputStream();
						if(attr.equals("type")) {
							type = Streams.asString(stream);
						}
					}
				}
			}
			
			if(type != null && fileContent != null) {
				log.info("Import Type: " + type);
				ExcelHandler excelHandler = new ExcelHandler(fileContent);
				String result = excelHandler.importExcelFile(type);
				
				jsonResponse = new Gson().toJson(new ReturnStatus("success", result));
			} else {
				log.error("Invalid file or import type. Type: " + type + ", Content: " + fileContent);
				jsonResponse = new Gson().toJson(new ReturnStatus("error", "Invalid data. Please try again."));
			}
		} catch(Exception ex) {
			log.error("Error while importing xlsx file: ", ex);
			String errorMsg = (ex != null) ? ex.getMessage() : "Null";
			jsonResponse = new Gson().toJson(new ReturnStatus("error", "Error while importing xlsx file: " + errorMsg));
		}
		
		response.setContentType("application/json");
		response.getWriter().write(jsonResponse);
		log.debug(jsonResponse);
		log.debug("Exiting ImportExcel: doPost...");
	}
}
