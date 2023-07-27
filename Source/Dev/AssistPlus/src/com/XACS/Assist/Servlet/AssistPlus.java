package com.XACS.Assist.Servlet;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.XACS.Assist.Handler.ActHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;

public class AssistPlus extends HttpServlet 
{
	private static final long	serialVersionUID	= 1L;
	 AssistLogger log=AssistLogger.getInstance();
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doPost(request, response);
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
			String classId = request.getParameter("classid").toString().trim();
			
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
					if(murl.endsWith("/"))
					{
						murl=murl.substring(0,murl.length()-1);
					}
					if(!murl.endsWith("/AssistPlus"))
					{
						murl+="/AssistPlus";
					}
					murl+="/GetAssistText";
					
					log.debug("Masetr Node Url=["+murl+"]");
					URL url = new URL(murl);
					URLConnection urlConnection = url.openConnection();
					((HttpURLConnection) urlConnection).setRequestMethod("POST");
					urlConnection.setDoInput(true);
					urlConnection.setDoOutput(true);
					urlConnection.setUseCaches(false);
					urlConnection.setDefaultUseCaches(false);
					urlConnection.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					urlConnection.setRequestProperty("Content-Length",
							"" + body.length());
					DataOutputStream outStream = new DataOutputStream(urlConnection.getOutputStream());

					// Send request
					log.debug(body.toString());
					outStream.writeBytes(body);
					outStream.flush();

					DataInputStream inStream = new DataInputStream(urlConnection.getInputStream());
					BufferedReader rd = new BufferedReader(new InputStreamReader(
							inStream));
					String responseStr = "";
					String line = null;
					while ((line = rd.readLine()) != null) {
						responseStr = responseStr + line;
					}

					// Close I/O streams
					inStream.close();
					outStream.close();

					jobj = (JSONObject) new JSONParser()
							.parse(responseStr);
					log.debug(responseStr);
					}
					else // This is Master Instance
					{				
					String rolesList = request.getParameter("roles").toString().trim();
					String[] classesList = request.getParameterValues("classes");
					String workflowID=request.getParameter("workflowID").toString().trim();
					String workflowStatusID=request.getParameter("workflowStatusID").toString().trim();
					
					
					log.debug("Roles List=["+rolesList+"],Classes List=["+classesList+"],Workflow Id=["+workflowID+"], Status Id=["+workflowStatusID+"]");
					
					ArrayList<String> roles = new ArrayList<String>(), classes = new ArrayList<String>();
					
					classes = new ArrayList<String>(Arrays.asList(classesList));
					
					Collections.addAll(roles, rolesList.split(";"));
					
					DBHandler dbh = new DBHandler();
					
					if(Constants.Config.ACCESSTYPEROLE==null)
					{
						HashMap params=new HashMap<String,String>();
						params.put("key", "accessType");
						log.debug("Key: accessType");
						HashMap map=dbh.handleDBRequest("getConfigByKey", params, false);
						Constants.Config.ACCESSTYPEROLE=(String) map.get("value");
					}
					String allRoleKey="All Roles";
					if(!"roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
					{
						allRoleKey="All User Groups";
					}
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
					colorsParams.put("mClasses",true);
					Map attColors=dbh.handleDBRequest("getAssistColors", colorsParams, false);
					
					
					log.debug("Attribute Colors=["+attColors+"]");
					
					jobj.put("attColors", attColors);

					dbh.handleDBRequest("closeDB", null, false);
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
