package com.XACS.Assist.PPM;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;

public class CompleteTask {
//	public static void main(String[] args) {
//		// G27386
//		JSONObject json = new JSONObject();
//		IAgileSession session = null;
//		try {
//			Properties props = null;
//			props = Utils.loadPropertyFile(Utils.getAgilePath("D:/Agile/BRCMPX.properties", "/opt/Agile/BRCMPX.properties"));
//			session = AgileUtils.getAgileSession(Utils.getValueByKey(props, "AgileInternalURL"), "usmanm", "Myold!23");
//			IProgram program = (IProgram) session.getObject(IProgram.OBJECT_TYPE, "PH0051663");
//			JSONArray errorResponse = new JSONArray();
//			CompleteTask comTask = new CompleteTask();
//			comTask.CompleteActivites(program, session, errorResponse, "http://plmdev.broadcom.com/Agile");
//			if (!program.getValue(new Integer(2000008049)).toString().equalsIgnoreCase("OTP Projects Template")) {
//				if (errorResponse.size() > 0) {
//					json.put("fail", errorResponse);
//				}
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//	}

	public JSONObject CompleteProgram(String ProgramNumber, HttpServletRequest request) {
		JSONObject json = new JSONObject();
//		try {
//			// Properties props =
//			// Utils.loadPropertyFile(Utils.getAgilePath("D:/Agile/BRCMPX.properties",
//			// "/opt/Agile/BRCMPX.properties"));
//			// Properties props =
//			// Utils.loadPropertyFile(Utils.getAgilePath("/opt/Agile/BRCMPX.properties",
//			// "/projects/agiledev1/Agile/BRCMPX.properties"));
//			Properties props = Utils.loadPropertyFile(Utils.getAgilePath("/opt/Agile/BRCMPX.properties", "/projects/agiledev1/Agile/BRCMPX.properties"));
//			String activityNumber = "";
//			try {
//				Connection con = DBUtils.getConnection(Utils.getValueByKey(props, "db.driver"), Utils.getValueByKey(props, "db.url"), Utils.getValueByKey(
//						props, "dbLogin"), Utils.getValueByKey(props, "dbPassword"));
//				ResultSet rs = DBUtils.getSQLResultSet(con, "SELECT activity_number from agile.activity where id=" + ProgramNumber);
//				while (rs.next()) {
//					activityNumber = rs.getString("activity_number");
//				}
//				con.close();
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if (!activityNumber.equals("")) {
//				String AgileURL = Utils.getValueByKey(props, "AgileURL");
//				IAgileSession session = AgileUtils.getBroadcomSession(request, props);
//				IProgram prg = (IProgram) session.getObject(IProgram.OBJECT_TYPE, activityNumber);
//				JSONArray errorResponse = new JSONArray();
//				CompleteActivites(prg, session, errorResponse, AgileURL);
//				if (!prg.getValue(new Integer(2000008049)).toString().equalsIgnoreCase("OTP Projects Template")) {
//					if (errorResponse.size() > 0) {
//						json.put("fail", errorResponse);
//					}
//				}
//				json.put("Success", "Activity Completed");
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			json.put("Error", e.getMessage());
//		}
		return json;
	}
//
//	private void CompleteActivites(IProgram program, IAgileSession session, ArrayList<String> errorResponse, String AgileURL) throws APIException {
//		try {
//			if (!program.getStatus().toString().equalsIgnoreCase("complete")) {
//				ITable scheduleTable = program.getTable(ProgramConstants.TABLE_SCHEDULE);
//				ITwoWayIterator scheduleTableIterator = scheduleTable.getReferentIterator();
//				if (scheduleTable.isEmpty()) {
//					try {
//						completeStatus(program, session);
//					} catch (Exception e) {
//						// TODO: handle exception
//						String URL = AgileUtils.getURL(AgileURL, program.getAgileClass().getId().toString(), program);
//						errorResponse.add("<tr><td id='" + program.getName() + "'><a target='_blank' href='" + URL + "'> "
//								+ program.getValue(ProgramConstants.ATT_GENERAL_INFO_NAME) + "</a> </td><td>"
//								+ program.getValue(ProgramConstants.ATT_GENERAL_INFO_DESCRIPTION) + "</td><td> " + e.getMessage() + "</td></tr>");
//					}
//				} else {
//					while (scheduleTableIterator.hasNext()) {
//						IProgram object = (IProgram) scheduleTableIterator.next();
//						CompleteActivites(object, session, errorResponse, AgileURL);
//					}
//				}
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
//
//	private void completeStatus(IProgram program, IAgileSession session) throws APIException {
//		IStatus releasedStatus = null;
//		IDataObject[] defaultApprovers;
//		IDataObject[] defaultObservers;
//		if (!program.getStatus().toString().equalsIgnoreCase("complete")) {
//			IWorkflow wf = program.getWorkflow();
//			IStatus[] releasedStatuses = wf.getStates(StatusConstants.TYPE_COMPLETE);
//			releasedStatus = releasedStatuses[0];
//			defaultApprovers = program.getApproversEx(releasedStatus);
//			defaultObservers = program.getObserversEx(releasedStatus);
//			program.changeStatus(releasedStatus, false, "", false, false, null, defaultApprovers, defaultObservers, false);
//		}
//	}
//
//	public static String getURL(String AgileURL, String ClassID, IDataObject obj) throws APIException {
//		String url = "";
//		String objectId = obj.getObjectId().toString();
//		if (obj.getAgileClass().isSubclassOf(ItemConstants.CLASS_ITEM_BASE_CLASS)) {
//			ClassID = ItemConstants.CLASS_ITEM_BASE_CLASS.toString();
//		} else if (obj.getAgileClass().isSubclassOf(ChangeConstants.CLASS_CHANGE_BASE_CLASS)) {
//			ClassID = ChangeConstants.CLASS_CHANGE_BASE_CLASS.toString();
//		} else if (obj.getAgileClass().isSubclassOf(ProgramConstants.CLASS_PROGRAM_BASE_CLASS)) {
//			ClassID = ProgramConstants.CLASS_PROGRAM_BASE_CLASS.toString();
//		}
//		url = AgileURL + "/PLMServlet?action=OpenEmailObject&classid=" + ClassID + "&objid=" + objectId;
//		return url;
//	}
}
