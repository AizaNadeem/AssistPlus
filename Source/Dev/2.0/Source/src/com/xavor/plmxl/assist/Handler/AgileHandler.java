package com.xavor.plmxl.assist.Handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sun.misc.BASE64Decoder;

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
import com.xavor.plmxl.assist.Util.AssistLogger;
import com.xavor.plmxl.assist.Util.ConfigHelper;
import com.xavor.plmxl.assist.Util.Constants;

public class AgileHandler {
	
	static AssistLogger log=AssistLogger.getInstance();
	static IAgileSession session = null;
	
	private AgileHandler() {
		
		
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
			// TODO Auto-generated catch block

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
	public static IAgileClass getAgileClass(IAdmin adminInstance, String apiName) throws Exception {
		log.debug("Entering getAgileClass..");

		try {
			return adminInstance.getAgileClass(apiName);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			
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
			// TODO Auto-generated catch block
	
			log.error("APIException in getBaseAgileClasses : " , e);
			throw e;
		}
	}

	public static String getCurrentUserRoles(IAgileSession session,String userid) {
		log.debug("Entering getCurrentUserRoles..");
		String userRoles = "";
		try {
			HashMap params=new HashMap();
			params.put(UserConstants.ATT_GENERAL_INFO_USER_ID, userid);
			log.debug("Userid from agile client: "+userid);
			IUser user = (IUser)session.getObject(IUser.OBJECT_TYPE, params);
			log.debug("User: "+user.toString());
			if("roles".equalsIgnoreCase(Constants.Config.ACCESSTYPEROLE))
			{
				userRoles ="'"+ user.getValue(UserConstants.ATT_GENERAL_INFO_ROLES).toString().replace("'", "''").replace(";", "','")+"',";
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
						userRoles+="'"+row.getValue(UserConstants.ATT_USER_GROUP_GROUP_NAME).toString().replace("'", "''")+"',";
					}
				}
			}
			log.debug("UserRoles: "+userRoles);
		} catch (APIException e) {

			log.error("APIException in getCurrentUserRoles : ", e);

		}
		catch (Exception e) {

			log.error("Exception in getCurrentUserRoles : ", e);

		}
		log.debug("Exiting getCurrentUserRoles..");
		return userRoles;
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
			session.setTimeout(200);
			
			log.debug("Exiting getAgileSession..");
	
			return session;
		}
	}


	public static void refreshConnectionAfterConfigChange() throws Exception {
		IAgileSession session = null;
		try {
			session = getAgileSession();
		} catch (Exception e) {
			throw e;
		} 
	}
}
