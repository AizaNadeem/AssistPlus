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
		List<AssistTextEntry> allTextsList = (List<AssistTextEntry>) dbh.handleDBRequest("getAllAssistTexts", null, false).get("existingEntries");	
		XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Sheet1");

        int rowNum = 0;
        log.info("Creating excel file..");
        String[] headers= {Constants.Excel.ClassHeader, Constants.Excel.AttributeHeader,
        		Constants.Excel.TextHeader,Constants.Excel.WorkflowHeader,Constants.Excel.StatusHeader,
        		Constants.Excel.RolesHeader};

        //writing excel file headers
     // Create a Font for styling header cells
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setBoldweight((short) 12);
        headerFont.setFontHeightInPoints((short) 13);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());
         
        // Create a CellStyle with the font
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row row = sheet.createRow(rowNum++);
        int colNum = 0;
        for (int i=0; i<headers.length; i++) {
            Cell cell = row.createCell(colNum++);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }
        
        //creating data
        List<ArrayList<String>> allRows=new ArrayList<ArrayList<String>>();
        for(int i=0; i<allTextsList.size(); i++)
        {
        	ArrayList<String> excelRow=new ArrayList<String>();
        	AssistTextEntry textEntry=allTextsList.get(i);
        	String className=textEntry.getClassName();
        	String attrName=textEntry.getAtrrName();
        	String text=textEntry.getAssistText();
        	String wf_lc=textEntry.getWorkflowID();
        	
        	//convert to ; separated
        	String statuses="";
			try {
				List<String> workflowList=textEntry.getWorkflowStatuses();
				statuses=workflowList.get(0);
				for(int k1=1; k1<workflowList.size(); k1++)
					statuses=statuses+"; "+workflowList.get(k1);
				log.info(statuses);
				}
			//will not happen
				catch(Exception e)
				{
					log.debug("Has no Workflow status "+ e.getMessage());
					statuses="";
				}
        	//
        	String roleLabel="";
        	try {
	        	List<String> rolesList=textEntry.getRolesList();
	        	DBHandler dbh = new DBHandler();
				Map<String, HashMap> rolesFromDB=(Map<String, HashMap>) dbh.handleDBRequest("getRoleLable", null, false);
				HashMap<String, Object> roleMap=rolesFromDB.get("roleLabel");
				List<String> roleLables=new ArrayList<String>();
				for(int j=0; j<rolesList.size(); j++)
				{
					if(roleMap.containsKey(rolesList.get(j)))
					{
						roleLables.add((String) roleMap.get(rolesList.get(j)));
					}
				}
				
				roleLabel=roleLables.get(0);
				for(int k1=1; k1<roleLables.size(); k1++)
					roleLabel=roleLabel+"; "+roleLables.get(k1);
				log.info(roleLabel);
				
			}
        	//will not happen
			catch(Exception e)
			{
				log.debug("Role does not exist in AssistPlus: "+ e.getMessage());
				roleLabel="";
			}
        	
        	excelRow.add(className);
        	excelRow.add(attrName);
        	excelRow.add(text);
        	try {
        		
				if(wf_lc.equals("Lifecycles"))
				{
					wf_lc="";
					
					if(statuses.equals("All Statuses"))
					{
						statuses="All Lifecycles";
					}
				}
				
			}
			catch (Exception e)
			{
				log.debug("Workflow/lifecycle is empty");
				if(statuses.equalsIgnoreCase("All Statuses"))
					{
						wf_lc="";
						statuses="";
					}
			}
        	log.info("Workflow/lifecycle added as " +wf_lc);
        	log.info("Statuses added as " +statuses);
        	log.info("Role Label added as " +roleLabel);
        	excelRow.add(wf_lc);
        	excelRow.add(statuses);
        	excelRow.add(roleLabel);
        	allRows.add(excelRow);
        }
        
        //writing into excel cells
        for(int i=0; i<allRows.size(); i++)
        {
        	ArrayList<String> dataRow=allRows.get(i);
        	 row = sheet.createRow(rowNum++);
             colNum = 0;
             for (int j=0; j<dataRow.size(); j++) {
                 Cell cell = row.createCell(colNum++);
                 cell.setCellValue(dataRow.get(j));
             }
        }

        try {
        	String abspath=ConfigHelper.getAppHomePath();
        	log.info("Path is" +abspath );
            FileOutputStream outputStream = new FileOutputStream(new File(abspath+Constants.Excel.ExcelFileName));
            workbook.write(outputStream);
            outputStream.close();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String jsonResponse = new Gson().toJson(new ReturnStatus("success", "Database Exported Successfully"));
        response.getWriter().write(jsonResponse);
        log.debug("Exiting ExportExcel:doPost..");
	}

}
