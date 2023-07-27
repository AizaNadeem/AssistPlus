package com.xavor.plmxl.assist.Handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

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
import com.agile.api.IUserGroup;
import com.agile.api.NodeConstants;
import com.agile.api.UserConstants;
import com.agile.api.UserGroupConstants;
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class AgileHandler {
	
	private static AssistLogger log = AssistLogger.getInstance();
	private static IAgileSession session = null;
	HttpServletRequest request;
	private static long sessionsAlive=0;

	private static boolean usePXParamsForSession = true; //True for 9.3.3 and above
	
	private AgileHandler() {
		
	}
	public AgileHandler(HttpServletRequest request) {
		this.request = request;
	}
	public String getUserIdFromSession() throws Exception {
		String userid = "";
		try {
			if (request != null) {
				userid = request.getSession().getAttribute("userId")+"";
				log.debug("user id:"+userid+" obtained from: "+request.getSession().getId());
				if(userid.equalsIgnoreCase("null") || userid.isEmpty()) {
					log.debug("Creating session from request");
					IAgileSession reqSession = getRequestSession();
					userid=reqSession.getCurrentUser().getValue(UserConstants.ATT_GENERAL_INFO_USER_ID).toString();
					reqSession.close(true);
					if(!reqSession.isOpen()) {
						log.debug("Closed session from request");
						sessionsAlive--;
					}
					request.getSession().setAttribute("userId", userid);
					log.debug("user id:"+userid+" cached in: "+request.getSession().getId());
					log.debug("Sessions Alive: "+sessionsAlive);
				}
			}else {
				throw new Exception("Unable to create session due to incorrect invocation");
			}
			
		} catch (Exception e) {
			log.error("Unable to get userid because:" + e.getMessage(), e);
		}
		return userid;
	}
	private IAgileSession getRequestSession() throws APIException, Exception {
		AgileSessionFactory factor = AgileSessionFactory.getInstance(ConfigHelper.getProperty(Constants.Config.AgileServerURL));
		Map<Object, Object> params = new HashMap<>();
		params.put(AgileSessionFactory.PX_REQUEST, request);
		IAgileSession reqSession = factor.createSession(params);
		if(sessionsAlive<=Long.MAX_VALUE)
			sessionsAlive++;
		else
			sessionsAlive=0;
		log.debug("Session created. Active session count is: "+sessionsAlive);
		reqSession.setTimeout(10);
		return reqSession;
	}
	public static IAgileSession getAgileSession() throws Exception {
	
		log.debug("Entering getAgileSession..");
		if(session!=null&&session.isOpen())
		{
			return session;
		}
		else
		{
			String ServerName = "", UserName = "", Password = "";
			ServerName = ConfigHelper.getProperty(Constants.Config.AgileServerURL);
			UserName = ConfigHelper.getProperty(Constants.Config.AgileUserName);
			Password = ConfigHelper.getProperty(Constants.Config.AgilePassword);
			Password = new String(DatatypeConverter.parseBase64Binary(Password));
			AgileSessionFactory f = AgileSessionFactory.getInstance(ServerName);
			Map<Integer, String> params = new HashMap<Integer, String>();
			
			if(usePXParamsForSession) {
				params.put(AgileSessionFactory.PX_USERNAME, UserName);
				params.put(AgileSessionFactory.PX_PASSWORD, Password);
			} else {
				params.put(AgileSessionFactory.USERNAME, UserName);
				params.put(AgileSessionFactory.PASSWORD, Password);
			}
			
			params.put(AgileSessionFactory.URL, ServerName);
			log.info("Creating Session...");
			session = f.createSession(params);
			log.info("Session created.");
			session.setTimeout(200);
			
			log.debug("Exiting getAgileSession..");
	
			return session;
		}
	}

	public static void disconnect() {
		log.debug("Entering disconnect..");

		try {
			if (session != null) {
				log.info("Closing Session..");
				session.close();
				session = null;
				log.info("Session Closed..");
			}
		} catch (Exception e) {
			log.error("Exception in disconnect : ", e);
			session = null;
		}
		log.debug("Exiting disconnect..");

	}

	public static IAdmin getAdminInstance(IAgileSession session) throws Exception {
		log.debug("Entering getAdminInstance..");

		IAdmin adminInstance = null;
		try {
			adminInstance = session.getAdminInstance();
		} catch (Exception e) {
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
			log.error("APIException in getAgileClass : ", e);
		}
		log.debug("Exiting getAgileClass..");

		return null;
	}
	public static IAgileClass getAgileClass(IAdmin adminInstance, String apiName) {
		log.debug("Entering getAgileClass..");
		try {
			return adminInstance.getAgileClass(apiName);
		} catch (APIException e) {
			log.error("APIException in getAgileClass : ", e);
		}
		
		log.debug("Exiting getAgileClass..");
		return null;
	}

	public static IAgileClass[] getBaseAgileClasses(IAgileSession session) throws Exception {
		try {
			IAdmin adminInstance = getAdminInstance(session);
			return adminInstance.getAgileClasses(IAdmin.TOP);
		} catch (APIException e) {
			log.error("APIException in getBaseAgileClasses : " , e);
			throw e;
		}
	}

	public static Set<String> getCurrentUserRoles(IAgileSession session,String userid) {
		log.debug("Entering getCurrentUserRoles..");
		Set<String> userRoles = new HashSet<>();
		
		try {
			Map<Integer, Object> params = new HashMap<>();
			params.put(UserConstants.ATT_GENERAL_INFO_USER_ID, userid);
			IUser user = (IUser)session.getObject(IUser.OBJECT_TYPE, params);
			log.debug("User: " + user);
			
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE)) {
				addUserRoles(userRoles, user);
				ITable tbl = user.getTable(UserConstants.TABLE_USERGROUP);
				ITwoWayIterator ugIt = tbl.getReferentIterator();
				while(ugIt.hasNext()) {
					IUserGroup group = (IUserGroup) ugIt.next();
					addUserGroupRoles(userRoles, group);
				}
			} else {
				ITable tbl = user.getTable(UserConstants.TABLE_USERGROUP);
				ITwoWayIterator ugIt = tbl.getTableIterator();
				if(ugIt != null) {
					IRow row = null;
					while(ugIt.hasNext()) {
						row = (IRow) ugIt.next();
						userRoles.add(row.getValue(UserConstants.ATT_USER_GROUP_GROUP_NAME).toString());
					}
				}
			}
		} catch (APIException e) {
			log.error("APIException in getCurrentUserRoles: ", e);
		} catch (Exception e) {
			log.error("Exception in getCurrentUserRoles: ", e);
		}
		
		log.debug("Exiting getCurrentUserRoles...");
		return userRoles;
	}
	
	public static void addUserRoles(Set<String> userRoles, IUser user) throws Exception {
		try {
			String[] r1 = user.getValue(UserConstants.ATT_GENERAL_INFO_ROLES).toString().trim().split("\\s*;\\s*");
			userRoles.addAll(Arrays.asList(r1));
		} catch(Exception ex) {
			log.error("Exception in addUserRoles", ex);
		}
	}


	public static void addUserGroupRoles(Set<String> userRoles, IUserGroup userGroup) throws Exception {
		try {
			String[] r2 = userGroup.getValue(UserGroupConstants.ATT_GENERAL_INFO_ROLES).toString().trim().split("\\s*;\\s*");
			userRoles.addAll(Arrays.asList(r2));
		} catch(Exception ex) {
			log.error("Exception in addUserGroupRoles", ex);
		}
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
		if(session!=null&&session.isOpen())
		{
			return session;
		}
		else
		{
			String ServerName = "", UserName = "", Password = "";
			ServerName = ConfigHelper.getProperty(Constants.Config.AgileServerURL,db);
			UserName = ConfigHelper.getProperty(Constants.Config.AgileUserName,db);
			Password = ConfigHelper.getProperty(Constants.Config.AgilePassword,db);
			Password = new String(DatatypeConverter.parseBase64Binary(Password));
			AgileSessionFactory f = AgileSessionFactory.getInstance(ServerName);
			Map<Integer, String> params = new HashMap<Integer, String>();
			if(usePXParamsForSession) {
				params.put(AgileSessionFactory.PX_USERNAME, UserName);
				params.put(AgileSessionFactory.PX_PASSWORD, Password);
			} else {
				params.put(AgileSessionFactory.USERNAME, UserName);
				params.put(AgileSessionFactory.PASSWORD, Password);
			}
			params.put(AgileSessionFactory.URL, ServerName);
			log.info("Creating Session...");
			session = f.createSession(params);
			log.info("Session created.");
			session.setTimeout(200);
			
			log.debug("Exiting getAgileSession..");
	
			return session;
		}
	}
	

	public static void refreshConnectionAfterConfigChange() throws Exception {
		getAgileSession();
	}
}
