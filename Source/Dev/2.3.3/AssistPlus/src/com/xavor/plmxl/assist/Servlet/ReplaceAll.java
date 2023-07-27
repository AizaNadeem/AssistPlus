package com.xavor.plmxl.assist.Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONObject;

import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xavor.plmxl.assist.DO.AssistText;
import com.xavor.plmxl.assist.DO.AssistTextEntry;
import com.xavor.plmxl.assist.DO.ReturnStatus;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;

/**
 * Servlet implementation class ReplaceAll
 */
public class ReplaceAll extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AssistLogger log = AssistLogger.getInstance();
	DBHandler dbh =null;
       
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("Entering ReplaceAll: doPost...");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String jsonResponse = "";
		HashMap<String, Object> gsonObj=new HashMap<String, Object>();
		
		
		try {
			
			dbh=new DBHandler();
			String searchText=request.getParameter("searchText").toString();
			String replaceText=request.getParameter("text").toString();
			String[] oldText=request.getParameterValues("oldText[]");
			log.info("Old AssistText: "+ oldText);
			log.info("Keyword searched: "+ searchText);
			log.info("Replacement word: "+ replaceText);
			//assist texst length validations
//			String assistText=replaceText;
//			if(!assistText.startsWith("<") || !assistText.endsWith(">")) {
//				assistText = "<p>" + StringEscapeUtils.escapeHtml4(assistText).replaceAll("\\n", "</p><p>").replaceAll("<p>\\s*<\\/p>", "<p>&nbsp;</p>") + "</p>";
//			}
//			
//			try {
//				final byte[] utf8Bytes = assistText.getBytes("UTF-8");
//				if(utf8Bytes.length > 4000) {
//					ReturnStatus retStatus=new ReturnStatus("error","HTML source code for the Assist Text exceeds 4000 character limit. Please remove some text");
//					gsonObj.put("retStatus",retStatus);
//					return;
//				}
//			} catch(Exception ex) {
//				log.error("Error while getting Byte size of Assist Text: " + ex);
//				if(assistText.length() > 2000) {
//					ReturnStatus retStatus=new ReturnStatus("error","HTML source code for the Assist Text exceeds 4000 character limit. Please remove some text");
//					gsonObj.put("retStatus",retStatus);
//					return;
//				}
//			}
//			
			String[] textid = request.getParameterValues("textid[]");
			String[] newText=new String[textid.length];
			
			for(int i=0; i<textid.length; i++)
			{
				newText[i]=oldText[i].replaceAll("(?i)"+searchText, replaceText);
				log.info("Text ID: "+ textid[i] +" and new AssistText: "+newText[i]);
			}
			
			jsonResponse=new Gson().toJson(textid);
			HashMap<String, Object> params=new HashMap<String, Object>();
			params.put("textids", textid);
			params.put("replaceText", newText);
			
			dbh.handleDBRequest("replaceAll", params, true);
			
			ReturnStatus retStatus=new ReturnStatus("success","Successfully replaced ");
			gsonObj.put("retStatus",retStatus);
					
		} catch(Exception ex) {
			log.error("Unable to replace: ", ex);
			String errorMsg = (ex != null) ? ex.getMessage() : "Null";
			ReturnStatus retStatus=new ReturnStatus("error","Unable to replace: " + errorMsg);
			gsonObj.put("retStatus",retStatus);
		}
		
		
		jsonResponse=new Gson().toJson(gsonObj);
		response.getWriter().write(jsonResponse);
		
		
		log.debug("Exiting ReplaceAll: doPost...");
	
	}

}
