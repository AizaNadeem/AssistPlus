package com.xavor.plmxl.assist.Servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.xavor.plmxl.assist.Handler.ExcelHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

/**
 * Servlet implementation class DownloadXLSX
 */
public class DownloadXLSX extends HttpServlet {
	private static final long serialVersionUID = 1L;
	AssistLogger log=AssistLogger.getInstance();

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		log.debug("Entering DownloadXLSX:doPost..");
		SimpleDateFormat simpleDateFormatter= new SimpleDateFormat("d MMM yyyy hh:mm aaa");
		Date today = new Date();
	    
		simpleDateFormatter.setTimeZone(java.util.TimeZone.getDefault());		
		
		String timeStamp =simpleDateFormatter.format(today);
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition","attachment;filename=AssistPlus Export-"+timeStamp+".xlsx");
		
		FileInputStream fileIn;
		try {
			String abspath=ConfigHelper.getAppHomePath();
			log.debug(abspath+Constants.Excel.ExcelFileName);
			
			File file = new File(abspath+Constants.Excel.ExcelFileName);
			fileIn = new FileInputStream(file);
			boolean exists=file.exists();
			if(exists)
			{
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
			}
			else {
				ExcelHandler excelHandler=new ExcelHandler();
	    		XSSFWorkbook workbook = excelHandler.export();
	        	String abspath1=ConfigHelper.getAppHomePath();
	        	log.info("Path is" +abspath1 );
	            FileOutputStream outputStream = new FileOutputStream(new File(abspath1+Constants.Excel.ExcelFileName));
	            workbook.write(outputStream);
	            outputStream.close();
	            File file1 = new File(abspath+Constants.Excel.ExcelFileName);
				fileIn = new FileInputStream(file1);
	            
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
				file1.delete();
			}
			

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
		
		log.debug("Exiting DownloadXLSX:doPost..");
	}

}
