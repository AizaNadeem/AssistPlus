package com.xavor.plmxl.assist.Servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Handler.ExcelHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

/**
 * Servlet implementation class ExportExcel
 */
public class ExportExcel extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DBHandler dbh = null;
	private AssistLogger log = AssistLogger.getInstance();
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ExportExcel:doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

        try {
        	ExcelHandler excelHandler=new ExcelHandler();
    		XSSFWorkbook workbook = excelHandler.export();
        	String abspath=ConfigHelper.getAppHomePath();
        	log.info("Path is" +abspath );
            FileOutputStream outputStream = new FileOutputStream(new File(abspath+Constants.Excel.ExcelFileName));
            workbook.write(outputStream);
            outputStream.close();
            
        } catch (FileNotFoundException e) {
        	log.error(e.getMessage(), e);
        } catch (IOException e) {
        	log.error(e.getMessage(), e);
        } catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        String jsonResponse = new Gson().toJson(new ReturnStatus("success", "Database Exported Successfully"));
        response.getWriter().write(jsonResponse);
        log.debug("Exiting ExportExcel:doPost..");
	}
	

}
