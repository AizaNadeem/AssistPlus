package com.XACS.Assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sun.misc.BASE64Decoder;

import com.XACS.Assist.Util.ConfigHelper;
import com.XACS.Assist.Util.Constants;
import com.agile.api.APIException;
import com.agile.api.AgileSessionFactory;
import com.agile.api.IAdmin;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.INode;
import com.agile.api.IQuery;
import com.agile.api.IUser;
import com.agile.api.NodeConstants;
import com.agile.api.UserConstants;

public class AgileHandler {
	private AgileHandler() {
	}

	public static IAgileSession getAgileSession() throws Exception {
		IAgileSession session = null;
		String ServerName = "", UserName = "", Password = "";
		ServerName = ConfigHelper.getProperty(Constants.Config.AgileServerURL);
		UserName = ConfigHelper.getProperty(Constants.Config.AgileUserName);// "Agile User Name");
		Password = ConfigHelper.getProperty(Constants.Config.AgilePassword);// "Agile Password");
		BASE64Decoder decoder = new BASE64Decoder();
		Password = new String(decoder.decodeBuffer(Password));
		AgileSessionFactory f = AgileSessionFactory.getInstance(ServerName);
		Map<Integer, String> params = new HashMap<Integer, String>();
		params.put(AgileSessionFactory.USERNAME, UserName);
		params.put(AgileSessionFactory.PASSWORD, Password);
		params.put(AgileSessionFactory.URL, ServerName);
		session = f.createSession(params);
		session.setTimeout(5000);
		return session;
	}

	public static void disconnect(IAgileSession session) {
		try {
			if (session != null) {
				session.close();
				session = null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			session = null;
		}
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

	private static IAdmin getAdminInstance(IAgileSession session) throws Exception {
		IAdmin adminInstance = null;
		try {
			adminInstance = session.getAdminInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return adminInstance;
	}

	public static IAgileClass getAgileClass(IAgileSession session, int classId) throws Exception {
		try {
			IAdmin adminInstance = getAdminInstance(session);
			return adminInstance.getAgileClass(classId);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static IAgileClass[] getConcreteAgileClasses(IAgileSession session) throws Exception {
		try {
			IAdmin adminInstance = getAdminInstance(session);
			return adminInstance.getAgileClasses(IAdmin.CONCRETE);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
			throw e;
		}
	}

	public static ArrayList<String> getUserRoles(String userId, IAgileSession session) {
		ArrayList<String> roleList = new ArrayList<String>();
		IQuery q;
		// getAdminInstance();
		try {
			q = (IQuery) session.createObject(IQuery.OBJECT_TYPE, UserConstants.CLASS_USER);// "select * from [Users]");
			q.setCriteria("[User ID] Equal To '" + userId + "'");
			Iterator itr = q.execute().getReferentIterator();
			IUser user = null;
			while (itr.hasNext()) {
				user = (IUser) itr.next();
			}
			String[] strarr = user.getValue(UserConstants.ATT_GENERAL_INFO_ROLES).toString().split(";");
			for (String string : strarr) {
				roleList.add(string);
			}
		} catch (APIException e) {
			e.printStackTrace();
		}
		return roleList;
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
			if (cls != null)
			{
			classList.add(cls.getId().toString());
			// TOP CLASS
			cls = cls.getSuperClass();
				if (cls!=null)
					classList.add(cls.getId().toString());
			}
		} catch (APIException e) {
			e.printStackTrace();
			throw e;
		}
		return classList;
	}

	public static Collection getAllRoles(IAgileSession session) throws Exception {
		// HashMap<String, String> roleMap = new HashMap<String, String>();
		try {
			IAdmin adminInstance = getAdminInstance(session);
			INode rolesNode = adminInstance.getNode(NodeConstants.NODE_ROLES);
			return rolesNode.getChildNodes();
			/*
			 * { IRole role = (IRole)roleObj; roleMap.put(role.getName(),
			 * role.getId().toString()); }
			 */
		} catch (APIException e) {
			e.printStackTrace();
			throw e;
		}
		// return null;
	}
}
