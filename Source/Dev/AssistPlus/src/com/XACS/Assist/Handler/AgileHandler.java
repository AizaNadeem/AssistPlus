package com.XACS.Assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import sun.misc.BASE64Decoder;

import com.XACS.Assist.Util.AssistLogger;
import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;
import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.INode;
import com.agile.api.IQuery;
import com.agile.api.IRow;
import com.agile.api.ITable;
import com.agile.api.ITwoWayIterator;
import com.agile.api.IUser;
import com.agile.api.NodeConstants;
import com.agile.api.UserConstants;
import com.xavor.ACS.AgileUtils;

public class AgileHandler {
	
	static AssistLogger log=AssistLogger.getInstance();
	
	private AgileHandler() {
		
		
	}

	public static IAgileSession getAgileSession() throws Exception {
	
		log.debug("Entering getAgileSession..");
		IAgileSession session = null;
		String ServerName = "", UserName = "", Password = "";
		ServerName = ConfigHelper.getProperty(Constants.Config.AgileServerURL);
		UserName = ConfigHelper.getProperty(Constants.Config.AgileUserName);
		Password = ConfigHelper.getProperty(Constants.Config.AgilePassword);
		BASE64Decoder decoder = new BASE64Decoder();
		Password = new String(decoder.decodeBuffer(Password));
		AgileSessionFactory f = AgileSessionFactory.getInstance(ServerName);
		Map<Integer, String> params = new HashMap<Integer, String>();
		params.put(AgileSessionFactory.USERNAME, UserName);
		params.put(AgileSessionFactory.PASSWORD, Password);
		params.put(AgileSessionFactory.URL, ServerName);
		log.info("Creating Session...");
		session = f.createSession(params);
		log.info("Session created.");
		session.setTimeout(5000);
		
		log.debug("Exiting getAgileSession..");

		return session;
	}

	public static IAgileSession getAgileSession(HttpServletRequest request, String serverURL) throws Exception {
		IAgileSession session = AgileUtils.getRequestSession(request, serverURL);
		if (session == null) {
			log.info("Getting Agile Session..");
			session = AgileUtils.getAgileCookieSession(request.getCookies(), serverURL);
		}
		return session;
	}

	public static IAgileSession getRequestSession(HttpServletRequest request, String AgileServerURL) {
		AgileSessionFactory factory;
		IAgileSession session = null;
		try {
			factory = AgileSessionFactory.getInstance(AgileServerURL);
			HashMap<Integer, HttpServletRequest> params = new HashMap<Integer, HttpServletRequest>();
			params.put(AgileSessionFactory.PX_REQUEST, request);
			
			session = factory.createSession(params);
		} catch (APIException e) {
		}
		return session;
	}

	public static IAgileSession getAgileCookieSession(Cookie[] cookies, String AgileServerURL) throws Exception {
		AgileSessionFactory factory = AgileSessionFactory.getInstance(AgileServerURL);
		HashMap<Integer, String> params = new HashMap<Integer, String>();
		String username = null;
		String pwd = null;
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals("j_username")) {
				username = cookies[i].getValue();
			} else if (cookies[i].getName().equals("j_password")) {
				pwd = cookies[i].getValue();
			}
		}
		params.put(AgileSessionFactory.PX_USERNAME, username);
		params.put(AgileSessionFactory.PX_PASSWORD, pwd);
		IAgileSession session = factory.createSession(params);
		return session;
	}

	public static void disconnect(IAgileSession session) {
		log.debug("Entering disconnect..");

		try {
			if (session != null) {
				log.info("Closing Session..");
				session.close();
				session = null;
				log.info("Session Closed..");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block

			log.error("Exception in disconnect : ", e);
			session = null;
		}
		log.debug("Exiting disconnect..");

	}

	public static void refreshConnection() throws Exception {
		IAgileSession session = null;
		try {
			session = getAgileSession();
		} catch (Exception e) {
			throw e;
		} finally {
			disconnect(session);
		}
	}

	public static IAdmin getAdminInstance(IAgileSession session) throws Exception {
		log.debug("Entering getAdminInstance..");

		IAdmin adminInstance = null;
		try {
			adminInstance = session.getAdminInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Exception in getAdminInstance : ", e);
			throw e;
		}
		log.debug("Exiting getAdminInstance..");

		return adminInstance;
	}

	public static IAgileClass getAgileClass(IAgileSession session, int classId) throws Exception {
		log.debug("Entering getAgileClass..");

		try {
			IAdmin adminInstance = getAdminInstance(session);
			return adminInstance.getAgileClass(classId);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			
			log.error("APIException in getAgileClass : ", e);

		}
		log.debug("Exiting getAgileClass..");

		return null;
	}
	public static IAgileClass getAgileClass(IAdmin adminInstance, int classId) throws Exception {
		log.debug("Entering getAgileClass..");

		try {
			return adminInstance.getAgileClass(classId);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			
			log.error("APIException in getAgileClass : ", e);

		}
		log.debug("Exiting getAgileClass..");

		return null;
	}

	public static IAgileClass[] getConcreteAgileClasses(IAgileSession session) throws Exception {
		try {
			IAdmin adminInstance = getAdminInstance(session);
			return adminInstance.getAgileClasses(IAdmin.CONCRETE);
		} catch (APIException e) {
			// TODO Auto-generated catch block
		
			log.error("APIException in getConcreteAgileClasses : ", e);
			throw e;
		}
		// return null;
	}

	public static IAgileClass[] getBaseAgileClasses(IAgileSession session) throws Exception {
		try {
			IAdmin adminInstance = getAdminInstance(session);
			return adminInstance.getAgileClasses(IAdmin.TOP);
		} catch (APIException e) {
			// TODO Auto-generated catch block
	
			log.error("APIException in getBaseAgileClasses : " , e);
			throw e;
		}
	}

	public static ArrayList<String> getUserRoles(String userId, IAgileSession session) {
		log.debug("Entering getUserRoles..");
		ArrayList<String> roleList = new ArrayList<String>();
		IQuery q;
		// getAdminInstance();
		try {
			q = (IQuery) session.createObject(IQuery.OBJECT_TYPE, UserConstants.CLASS_USER);// "select * from [Users]");
			q.setCriteria("[User ID] Equal To '" + userId + "'");
			ITwoWayIterator itr = q.execute().getReferentIterator();
			IUser user = null;
			while (itr.hasNext()) {
				user = (IUser) itr.next();
			}
			String[] strarr = user.getValue(UserConstants.ATT_GENERAL_INFO_ROLES).toString().split(";");
			for (String string : strarr) {
				roleList.add(string);
			}
			log.debug("Roles List: "+roleList);
		} catch (APIException e) {

			log.error("APIException in getUserRoles : " , e);

		}
		log.debug("Exiting getUserRoles..");
		return roleList;
	}

	public static String getCurrentUserRoles(IAgileSession session) {
		log.debug("Entering getCurrentUserRoles..");
		String userRoles = "";
		try {
			IUser user = session.getCurrentUser();
			log.debug("User: "+user.toString());
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
			{
				userRoles = user.getValue(UserConstants.ATT_GENERAL_INFO_ROLES).toString();
			}
			else
			{
				ITable tbl=user.getTable(UserConstants.TABLE_USERGROUP);
				ITwoWayIterator ugIt=tbl.getTableIterator();
				if(ugIt!=null)
				{
					IRow row=null;
					while(ugIt.hasNext())
					{
						row=(IRow)ugIt.next();
						userRoles+=row.getValue(UserConstants.ATT_USER_GROUP_GROUP_NAME)+";";
					}
				}
			}
			log.debug("UserRoles: "+userRoles);
		} catch (APIException e) {

			log.error("APIException in getCurrentUserRoles : ", e);

		}
		log.debug("Exiting getCurrentUserRoles..");
		return userRoles;
	}

	public static ArrayList<String> getAllClasses(IAgileSession session) {
		ArrayList<String> allClasses = new ArrayList<String>();
		try {
			IAgileClass[] baseClassArray = session.getAdminInstance().getAgileClasses(IAdmin.TOP);
			String classes="";
			for (IAgileClass basecls : baseClassArray)
			{
				
				IAgileClass[] subClassArray = basecls.getSubclasses();

				for (IAgileClass subcls : subClassArray)
				{
					classes = basecls.getId().toString();
					classes += ";" + subcls.getId().toString();
					IAgileClass[] conClassArray = subcls.getSubclasses();
					
					for (IAgileClass conccls : conClassArray) 
					{
						classes += ";" + conccls.getId().toString();
						System.out.println(conccls.getAPIName()+"::"+conccls.getId());
					}
					allClasses.add(classes);
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return allClasses;
	}

	public static ArrayList<String> getRelatedClasses(IAgileSession session, String classId) throws Exception {
		ArrayList<String> classList = new ArrayList<String>();
		try {
			// CONCRETE CLASS
			classList.add(classId);
			IAdmin adminInstance = getAdminInstance(session);
			IAgileClass cls = adminInstance.getAgileClass(new Integer(classId));
			// SUB CLASS
			cls = cls.getSuperClass();
			if (cls != null) {
				classList.add(cls.getId().toString());
				// TOP CLASS
				cls = cls.getSuperClass();
				if (cls != null) {
					classList.add(cls.getId().toString());
				}
			}
		} catch (APIException e) {
		
			log.error("APIException in getRelatedClasses : ", e);;
			throw e;
		}
		return classList;
	}

	public static Collection getAllRoles(IAgileSession session) throws Exception 
	{
		log.debug("Entering getAllRoles..");

		try {
			INode rolesNode = null;
			
			IAdmin adminInstance = getAdminInstance(session);
			
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
			{
				rolesNode = adminInstance.getNode(NodeConstants.NODE_ROLES);
			}
			else
			{
				rolesNode = adminInstance.getNode("User Groups");
			}
			
			return rolesNode.getChildNodes();
			
		} catch (APIException e) {
		
			log.error("APIException in getAllRoles : ", e);
			log.debug("Exiting getAllRoles..");
			throw e;
		}
		
	}
	public static ITable getAllUserGroups(IAgileSession session) throws APIException 
	{
		log.info("Getting All User Groups..");
		IQuery q =(IQuery)session.createObject(IQuery.OBJECT_TYPE, "select * from [User Groups]");
		ITable tbl=q.execute();
		
	
		return tbl;
	}
	
	public static IAgileSession getAgileSession(DBHandler db) throws Exception {
		log.debug("Entering getAgileSession..");
		IAgileSession session = null;
		String ServerName = "", UserName = "", Password = "";
		ServerName = ConfigHelper.getProperty(Constants.Config.AgileServerURL,db);
		UserName = ConfigHelper.getProperty(Constants.Config.AgileUserName,db);
		Password = ConfigHelper.getProperty(Constants.Config.AgilePassword,db);
		BASE64Decoder decoder = new BASE64Decoder();
		Password = new String(decoder.decodeBuffer(Password));
		AgileSessionFactory f = AgileSessionFactory.getInstance(ServerName);
		Map<Integer, String> params = new HashMap<Integer, String>();
		params.put(AgileSessionFactory.USERNAME, UserName);
		params.put(AgileSessionFactory.PASSWORD, Password);
		params.put(AgileSessionFactory.URL, ServerName);
		log.info("Creating Session...");
		session = f.createSession(params);
		log.info("Session created.");
		session.setTimeout(5000);
		
		log.debug("Exiting getAgileSession..");

		return session;
	}
}
