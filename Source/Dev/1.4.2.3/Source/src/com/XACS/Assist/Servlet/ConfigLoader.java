package com.XACS.Assist.Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.XACS.Assist.DO.ConfigEntry;
import com.XACS.Assist.DO.ReturnStatus;
import com.XACS.Assist.Handler.AgileHandler;
import com.XACS.Assist.Handler.DBHandler;
import com.XACS.Assist.Handler.UIListHandler;
import com.XACS.Assist.Util.ConfigHelper;
import com.agile.api.APIException;
import com.google.gson.Gson;

/**
 * Servlet implementation class ConfigLoader
 */
public class ConfigLoader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ConfigLoader() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String masterServerUrl="";
		
		try 
		{
			masterServerUrl=request.getParameter("masterServerUrl");
			System.out.println("masterServerUrl=["+masterServerUrl+"]");
			if(masterServerUrl!=null && !masterServerUrl.equals(""))
			{
				json=getMasterConfig(masterServerUrl);
			}else
			{
				HashMap<String, ConfigEntry> lstConfig = UIListHandler.getConfigEntries();
				json = new Gson().toJson(lstConfig);
			}
		} catch (Exception e) {
			json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
		}
		response.getWriter().write(json);
	}
	private String getMasterConfig(String masterUrl)
	{
		String json="";
		
		try 
		{
			URL url = new URL(masterUrl+"/loadConfig");
			URLConnection conn = url.openConnection();
			((HttpURLConnection) conn).setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setDefaultUseCaches(false);
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			
			BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
			String responseStr = "";
			String line = null;
			while ((line = br.readLine()) != null) {
				responseStr = responseStr + line;
			}
			
			JSONObject jobj = (JSONObject) new JSONParser()
			.parse(responseStr);
			System.out.println(jobj);
			json=jobj.toString();
			
		} catch (MalformedURLException e) 
		{
			e.printStackTrace();
			json = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+masterUrl+", please verify the URL."));
		} catch (IOException e) {
			e.printStackTrace();
			json = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+masterUrl+", please verify the URL."));
		}
		catch (Exception e) {
			e.printStackTrace();
			json = new Gson().toJson(new ReturnStatus("error", "Could not connect to "+masterUrl+", please verify the URL."));
		}
		
		return json;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String json = "";
		String[] configs = new String[] {};
		configs = request.getParameterValues("configs[]");
		int chk = 0;
		String[] split;
		for (int i = 0; i < configs.length; i++) {
			chk = 0;
			split = configs[i].split("=");
			if (split.length > 1) {
				chk = 1;
			}
			if(split[0].equalsIgnoreCase("PrimaryInstanceURL"))
			{
				chk=1;
			}
		}
		DBHandler dbh =null;
		try {
			
			if (chk == 1) {
				dbh = new DBHandler();
				dbh.updateConfigurations(configs);
				ConfigHelper.loadConfigurations();
				AgileHandler.refreshConnection();
				json = new Gson().toJson(new ReturnStatus("success", "Configuration saved"));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", "Mandatory Fields are not filled"));
			}
		} catch (Exception e) {
			if (e instanceof APIException) {
				APIException apie = (APIException) e;
				json = new Gson().toJson(new ReturnStatus("error", "System is unable to connect to Agile due to " + apie.getMessage()
						+ ". Please check configuration."));
			} else {
				json = new Gson().toJson(new ReturnStatus("error", e.getMessage()));
			}
		}finally
		{
			if(dbh!=null)
			{
				dbh.closeDB();
			}
		}
		response.getWriter().write(json);
	}
	public static void main(String[] args) 
	{
		
	}
}
