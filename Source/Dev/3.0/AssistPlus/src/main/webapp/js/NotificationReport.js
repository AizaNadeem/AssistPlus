if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.NotifUsageReport = (function() {
	
	function loadReport($contentboxheader, $contentboxcontent) {
		PLMFlex.Assist.BindEvents.Notify("Generating Report. Please wait...", "attention");
		
		$contentboxheader.html("<h3>Notification Usage Report:</h3>");
		$contentboxcontent.html("<div id='report-grid-wrapper'><div id='report-grid'></div></div>");
		
		PLMFlex.Assist.Request.post("NotifUsageReport", {
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
							fileName : "Assist+ Notification Usage Report.xlsx",
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
									usageCount : {
										type : "string"
									},
									ackCount : {
										type : "string"
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
							field : "usageCount",
							title : "No of Views",
							width : 100,
							type : "string"
						}, {
							field : "ackCount",
							title : "No of Acknowledgments",
							width : 100,
							type : "string"
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