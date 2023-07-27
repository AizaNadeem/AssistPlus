package com.xavor.plmxl.assist.Servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.xavor.plmxl.assist.Handler.ActHandler;
import com.xavor.plmxl.assist.Handler.AgileHandler;
import com.xavor.plmxl.assist.Handler.DBHandler;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class AssistPlus extends HttpServlet 
{
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
	}
	protected String completeURL(String mURL)
	{
		String murl=mURL;
		if(murl.endsWith("/"))
		{
			murl=murl.substring(0,murl.length()-1);
		}
		if(!murl.endsWith("/"+Constants.Config.ProjectName))
		{
			murl+="/"+Constants.Config.ProjectName;
		}
		murl+="/"+Constants.Config.URL_GetAssistText;
		return murl;		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		log.debug("Entering AssistPlus: doPost..");
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		JSONObject jobj = new JSONObject();
		HashMap<String, ArrayList<String>> helpMap = null;
		
		try 
		{
			String classId =request.getParameter("classid").toString().trim();
			String userid =request.getParameter("userid").toString().trim();
			log.debug("ClassID::" + classId);
			if (ActHandler.isLicValid()) 
			{
				if (!classId.equals("undefined")) 
				{
					String murl = ConfigHelper.getProperty(Constants.Config.PrimaryInstURL);	
					/**
					 * If there is a Master URL configured, this is Slave instance so get
					 * assist text data from Master instance
					 * **/
					if (murl != null && !murl.equals(""))
					{
					String body = "";
					Enumeration lstParam = request.getParameterNames();
					log.debug(lstParam.toString());
					while (lstParam.hasMoreElements())
					{
						String param = lstParam.nextElement().toString();
						body += body.equals("")?"":"&";
						body += param+"="+URLEncoder.encode(request.getParameter(param),"UTF-8");
					}
					murl=completeURL(murl);	
					
					log.debug("Masetr Node Url=["+murl+"]");
					
					String responseStr=ConfigHelper.getDataFromServer(murl, body);
					jobj = (JSONObject) new JSONParser()
							.parse(responseStr);
					log.debug(responseStr);
					}
					else // This is Master Instance
					{		
					IAgileSession session=AgileHandler.getAgileSession();
					String rolesList = AgileHandler.getCurrentUserRoles(session,userid);
					IAgileClass actualClass=AgileHandler.getAgileClass(session,Integer.parseInt(classId));
					IAgileClass levelOneParent=null;
					IAgileClass levelTwoParent=null;
					String roles =rolesList, classes = "";
					if(actualClass!=null)
					{
						classes+=(classId)+" ,";
						levelOneParent=actualClass.getSuperClass();
					}
					if(levelOneParent!=null)
					{
						classes+=(levelOneParent.getId().toString())+" ,";
						levelTwoParent=levelOneParent.getSuperClass();
					}
					if(levelTwoParent!=null)
					{
						classes+=(levelTwoParent.getId().toString());

					}
					
					
					String workflowID=request.getParameter("workflowID").toString().trim();
					String workflowStatusID=request.getParameter("workflowStatusID").toString().trim();

					log.debug("Classes"+classes.toString()+"Roles List=["+rolesList+"],Workflow Id=["+workflowID+"], Status Id=["+workflowStatusID+"]");
					
					
					
					DBHandler dbh = new DBHandler();
					
					
					String allRoleKey=ConfigHelper.configureAccessType(dbh);
					log.debug("allRoleKey: "+allRoleKey );
					HashMap params=new HashMap<String,Object>();
					params.put("classes",classes);
					params.put("roles",roles);
					params.put("allRoleKey",allRoleKey);
					params.put("workflowID",workflowID);
					params.put("workflowStatusID",workflowStatusID);
					helpMap = (HashMap<String, ArrayList<String>>) dbh.handleDBRequest("getAssistInfoMap", params, false);
					
					
					jobj.put("helpText", helpMap);
					HashMap colorsParams=new HashMap<String,Object>();
					colorsParams.put("classId", classes.toString());
					Map attColors=dbh.handleDBRequest("getAssistColorsForClasses", colorsParams, false);
					
					
					log.debug("Attribute Colors=["+attColors+"]");
					
					jobj.put("attColors", attColors);

					//dbh.handleDBRequest("closeDB", null, false);
					}
				}
			}
			
			log.debug(jobj.toString());
			response.getWriter().write(jobj.toString());
		} catch (Exception e) 
		{
			response.getWriter().write(e.getMessage());
			log.error("Exception in AssistPlus: doPost :", e);
			
		}
		log.debug("Exiting AssistPlus: doPost..");
	}
}
