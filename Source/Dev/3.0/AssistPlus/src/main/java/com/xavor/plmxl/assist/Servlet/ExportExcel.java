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
import com.xavor.plmxl.assist.DO.AssistClassNotificationEntry;
import com.xavor.plmxl.assist.DO.AssistNotificationEntry;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.DBHandler;
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
		
		//exporting as excel file
		try {
			dbh = new DBHandler();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet1 = workbook.createSheet("Sheet1");
		getAssistTexts(dbh, sheet1);
		
		XSSFSheet sheet2 = workbook.createSheet("Sheet2");
		getAssistNotification(dbh, sheet2);
		
		XSSFSheet sheet3 = workbook.createSheet("Sheet3");
		getClassNotifications(dbh, sheet3);
		
		try {
		    String abspath = ConfigHelper.getAppHomePath();
		    log.info("Path is " + abspath);
		    FileOutputStream outputStream = new FileOutputStream(new File(abspath + Constants.Excel.ExcelFileName));
		    workbook.write(outputStream);
		    outputStream.close();
		
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String jsonResponse = new Gson().toJson(new ReturnStatus("success", "Database Exported Successfully"));
        response.getWriter().write(jsonResponse);
        log.debug("Exiting ExportExcel:doPost..");
	}
	
	private void getAssistTexts(DBHandler dbh, XSSFSheet sheet) {
		log.info("Entering getAssistTexts..");
	    List<AssistTextEntry> allTextsList = (List<AssistTextEntry>) dbh.handleDBRequest("getAllAssistTexts", null, false).get("existingEntries");
	    int rowNum = 0;
	    String[] headers = { Constants.Excel.ClassHeader, Constants.Excel.AttributeHeader, Constants.Excel.TextHeader,
	            Constants.Excel.WorkflowHeader, Constants.Excel.StatusHeader, Constants.Excel.RolesHeader };

	    // Create a Font for styling header cells
	    XSSFFont headerFont = sheet.getWorkbook().createFont();
	    headerFont.setBold(true);
	    headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
	    headerFont.setFontHeightInPoints((short) 13);
	    headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

	    // Create a CellStyle with the font
	    XSSFCellStyle headerCellStyle = sheet.getWorkbook().createCellStyle();
	    headerCellStyle.setFont(headerFont);

	    Row headerRow = sheet.createRow(rowNum++);
	    int colNum = 0;
	    for (String header : headers) {
	        Cell cell = headerRow.createCell(colNum++);
	        cell.setCellValue(header);
	        cell.setCellStyle(headerCellStyle);
	    }

	    for (AssistTextEntry textEntry : allTextsList) {
	        Row row = sheet.createRow(rowNum++);
	        colNum = 0;
	        row.createCell(colNum++).setCellValue(textEntry.getClassName());
	        row.createCell(colNum++).setCellValue(textEntry.getAtrrName());
	        row.createCell(colNum++).setCellValue(textEntry.getAssistText());
	        row.createCell(colNum++).setCellValue(textEntry.getWorkflowID());
	        row.createCell(colNum++).setCellValue(String.join("; ", textEntry.getWorkflowStatuses()));
	        List<String> roleLabels = new ArrayList<>();
	        for (String role : textEntry.getRolesList()) {
	            String roleLabel = getRoleLabel(dbh, role);
	            if (!roleLabel.isEmpty()) {
	                roleLabels.add(roleLabel);
	            }
	        }
	        row.createCell(colNum++).setCellValue(String.join("; ", roleLabels));
	    }
	    log.info("Exiting getAssistTexts..");
	}
	
	private void getAssistNotification(DBHandler dbh, XSSFSheet sheet) {
		log.info("Entering getAssistNotification..");
	    AssistNotificationEntry assistNotification = (AssistNotificationEntry) dbh.handleDBRequest("getAssistNotification", null, false).get("assistNotification");
		HashMap<String, String> assistConfiguration = (HashMap<String, String>) dbh.handleDBRequest("readConfigurations",null,false);
	    int rowNum = 0;
	    String[] headers = {Constants.Excel.MessageHeader, Constants.Excel.NotifEnableHeader, Constants.Excel.OptOutEnableHeader, 
	    		Constants.Excel.DurationEnableHeader, Constants.Excel.DurationLimitHeader, Constants.Excel.FontColorHeader, 
	    		Constants.Excel.BackgroundColorHeader, Constants.Excel.RolesHeader };

	    // Create a Font for styling header cells
	    XSSFFont headerFont = sheet.getWorkbook().createFont();
	    headerFont.setBold(true);
	    headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
	    headerFont.setFontHeightInPoints((short) 13);
	    headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

	    // Create a CellStyle with the font
	    XSSFCellStyle headerCellStyle = sheet.getWorkbook().createCellStyle();
	    headerCellStyle.setFont(headerFont);

	    Row headerRow = sheet.createRow(rowNum++);
	    int colNum = 0;
	    for (String header : headers) {
	        Cell cell = headerRow.createCell(colNum++);
	        cell.setCellValue(header);
	        cell.setCellStyle(headerCellStyle);
	    }

        Row row = sheet.createRow(rowNum++);
        colNum = 0;
        row.createCell(colNum++).setCellValue(assistNotification.getAssistMessage());
        row.createCell(colNum++).setCellValue(assistConfiguration.get("isNotifEnabled"));
        row.createCell(colNum++).setCellValue(assistConfiguration.get("isAckEnabled"));
        row.createCell(colNum++).setCellValue(assistNotification.getDurationEnable());
        row.createCell(colNum++).setCellValue(assistNotification.getDurationLimit());
        row.createCell(colNum++).setCellValue(assistNotification.getFontColor());
        row.createCell(colNum++).setCellValue(assistNotification.getBackgroundColor());

        List<String> roleLabels = new ArrayList<>();
        for (String role : assistNotification.getRoleList()) {
            String roleLabel = getRoleLabel(dbh, role);
            if (!roleLabel.isEmpty()) {
                roleLabels.add(roleLabel);
            }
        }
        row.createCell(colNum++).setCellValue(String.join("; ", roleLabels));
	    log.info("Exiting getAssistNotification..");
	}


	private void getClassNotifications(DBHandler dbh, XSSFSheet sheet) {
		log.info("Entering getClassNotifications..");
	    List<AssistClassNotificationEntry> allClassNotifList = (List<AssistClassNotificationEntry>) dbh.handleDBRequest("getClassNotification", null, false).get("classNotification");
	    int rowNum = 0;
	    String[] headers = { Constants.Excel.ClassHeader, Constants.Excel.ClassMessageHeader,
	    		Constants.Excel.NotifEnableHeader, Constants.Excel.OverrideEnableHeader, Constants.Excel.FontColorHeader,
	    		Constants.Excel.BackgroundColorHeader, Constants.Excel.RolesHeader };

	    // Create a Font for styling header cells
	    XSSFFont headerFont = sheet.getWorkbook().createFont();
	    headerFont.setBold(true);
	    headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
	    headerFont.setFontHeightInPoints((short) 13);
	    headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

	    // Create a CellStyle with the font
	    XSSFCellStyle headerCellStyle = sheet.getWorkbook().createCellStyle();
	    headerCellStyle.setFont(headerFont);

	    Row headerRow = sheet.createRow(rowNum++);
	    int colNum = 0;
	    for (String header : headers) {
	        Cell cell = headerRow.createCell(colNum++);
	        cell.setCellValue(header);
	        cell.setCellStyle(headerCellStyle);
	    }

	    for (AssistClassNotificationEntry notifEntry : allClassNotifList) {
	        Row row = sheet.createRow(rowNum++);
	        colNum = 0;
	        row.createCell(colNum++).setCellValue(notifEntry.getClassName());
	        row.createCell(colNum++).setCellValue(notifEntry.getAssistText());
	        row.createCell(colNum++).setCellValue(notifEntry.getNotifEnable());
	        row.createCell(colNum++).setCellValue(notifEntry.getOverrideEnable());
	        row.createCell(colNum++).setCellValue(notifEntry.getFontColor());
	        row.createCell(colNum++).setCellValue(notifEntry.getBackgroundColor());

	        List<String> roleLabels = new ArrayList<>();
	        for (String role : notifEntry.getRoleList()) {
	            String roleLabel = getRoleLabel(dbh, role);
	            if (!roleLabel.isEmpty()) {
	                roleLabels.add(roleLabel);
	            }
	        }
	        row.createCell(colNum++).setCellValue(String.join("; ", roleLabels));
	    }
	    log.info("Exiting getClassNotifications..");
	}
	
	private String getRoleLabel(DBHandler dbh, String role) {
	    log.info("Entering getRoleLabel..");
	    try {
	        dbh = new DBHandler();
	        Map<String, HashMap> rolesFromDB = (Map<String, HashMap>) dbh.handleDBRequest("getRoleLable", null, false);
	        HashMap<String, Object> roleMap = rolesFromDB.get("roleLabel");
	        if (roleMap.containsKey(role)) {
	            return (String) roleMap.get(role);
	        }
	    } catch (Exception e) {
	        log.debug("Role does not exist in AssistPlus: " + e.getMessage());
	    }
	    log.info("Exiting getRoleLabel..");
	    return "";
	}
}
