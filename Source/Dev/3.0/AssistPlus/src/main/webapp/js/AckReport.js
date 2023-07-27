if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.UserAckReport = (function() {
	
	function loadReport($contentboxheader, $contentboxcontent) {
		PLMFlex.Assist.BindEvents.Notify("Generating Report. Please wait...", "attention");
		
		$contentboxheader.html("<h3>User Acknowledgment Report:</h3>");
		$contentboxcontent.html("<div id='report-grid-wrapper'><div id='report-grid'></div></div>");
		
		PLMFlex.Assist.Request.post("UserAckReport", {
			rnd: Math.random()
		}, function(responseJson) {
			var type = responseJson.status;
			if(type != "error") {
				PLMFlex.Assist.BindEvents.Notify("Report generated successfully", "success");
				
				setTimeout(function() {

					$("#report-grid").empty();

					var config = {
						sortable : true,
						filterable : true,
						columnMenu : false,
						resizable : false,
						reorderable : false,
						scrollable : true,
						dataSource : {
							data: responseJson.report
						},
						pageable : false,
						toolbar: ["excel"],
						excel : {
							fileName : "Assist+ User Acknowledgment Report.xlsx",
							filterable : true,
							allPages : true
						}, 
						schema : {
							model : {
								fields : {
									msgId : {
										type : "string"
									},
									msgDesc : {
										type : "string"
									},
									userId : {
										type : "string"
									},
									userName : {
										type : "string"
									},
									ackDate : {
										type : "date"
									}
								}
							}
						},
						columns : [ {
							field : "msgId",
							title : "Message Id",
							width : 100,
							type : "string"
						}, {
							field : "msgDesc",
							title : "Assist Message",
							template: "#= data.msgDesc #",
							width : 150
						}, {
							field : "userId",
							title : "User Id",
							width : 100,
							type : "string"
						}, {
							field : "userName",
							title : "User Name",
							width : 100,
							type : "string"
						}, {
							field : "ackDate",
							title : "Acknowledgment Date",
							width : 150,
							type : "date",
							format: "{0:MM/dd/yyyy}"
						}]
					};

					$("#report-grid").kendoGrid(config);
				}, 100);
			}
		});
	}
	
	return {
		loadReport: loadReport
	};		
})();