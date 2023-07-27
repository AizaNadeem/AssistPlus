if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.UsageReport = (function() {
	
	function loadReport($contentboxheader, $contentboxcontent) {
		PLMFlex.Assist.BindEvents.Notify("Generating Report. Please wait...", "attention");
		
		$contentboxheader.html("<h3>Usage Report:</h3>");
		$contentboxcontent.html("<div id='report-grid-wrapper'><div id='report-grid'></div></div>");
		
		PLMFlex.Assist.Request.post("UsageReport", {
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
							fileName : "Assist+ Usage Report.xlsx",
							filterable : true,
							allPages : true
						}, 
						schema : {
							model : {
								fields : {
									userId : {
										type : "string"
									},
									classId : {
										type : "string"
									},
									attrId : {
										type : "string"
									},
									count : {
										type : "number"
									},
									lastAccessDate : {
										type : "date"
									}
								}
							}
						},
						columns : [ {
							field : "userId",
							title : "User",
							width : 100,
							type : "string"
						}, {
							field : "classId",
							title : "Class Name",
							type : "string",
							width : 150
						},{
							field : "attrId",
							title : "Attribute Name",
							type : "string",
							width : 150
						}, {
							field : "count",
							title : "Usage Count",
							width : 100,
							type : "number"
						}, {
							field : "lastAccessDate",
							title : "Last Access Date",
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