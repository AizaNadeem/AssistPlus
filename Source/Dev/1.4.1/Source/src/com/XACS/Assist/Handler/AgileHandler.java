package com.XACS.Assist.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
import com.xavor.ACS.AgileUtils;

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

	public static IAgileSession getAgileSession(HttpServletRequest request, String serverURL) throws Exception {
		IAgileSession session = AgileUtils.getRequestSession(request, serverURL);
		if (session == null) {
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

	public static String getCurrentUserRoles(IAgileSession session) {
		String userRoles = "";
		try {
			IUser user = session.getCurrentUser();
			userRoles = user.getValue(UserConstants.ATT_GENERAL_INFO_ROLES).toString();
		} catch (APIException e) {
			e.printStackTrace();
		}
		return userRoles;
	}

	public static ArrayList<String> getAllClasses(IAgileSession session) {
		ArrayList<String> allClasses = new ArrayList<String>();
		try {
			IAgileClass[] baseclsarr = session.getAdminInstance().getAgileClasses(IAdmin.TOP);
			for (IAgileClass basecls : baseclsarr) {
				String classes = basecls.getId().toString();
				IAgileClass[] subclsarr = basecls.getSubclasses();
				for (IAgileClass subcls : subclsarr) {
					classes += ";" + subcls.getId().toString();
					IAgileClass[] concclsarr = subcls.getSubclasses();
					for (IAgileClass conccls : concclsarr) {
						classes += ";" + conccls.getId().toString();
					}
				}
				allClasses.add(classes);
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
