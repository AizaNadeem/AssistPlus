if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.ImportStats = (function() {
	
	function loadStats($contentboxheader, $contentboxcontent) {
		window.open('','popUpWindow','height=500,width=400,left=100,top=100,resizable=yes,scrollbars=yes,toolbar=yes,menubar=no,location=no,directories=no, status=yes');
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are importing Database","attention");
		
		$contentboxheader.html("<h3>Import Report:</h3>");
		$contentboxcontent.html("<div id='report-grid-wrapper'><div id='report-grid'></div></div>");
		
		
		var type="";
		if ($("#TypeOverWrite").is(':checked'))
		{
			type='overWrite';
		}
		else if ($("#TypeMerge").is(':checked'))
		{
			type='merge';
		}
		PLMFlex.Assist.Request.post("DBUtility", {'type' : type,'mode' : "import", 'path' : $('#importPath').val(), rnd: Math.random()}, function(responseJson) {
			$("#upload").get(0).reset();	
			$('#path').text('');
			$("body").css("cursor", "default");
			$(".importDB").css("cursor", "pointer");
			if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)
			{
				PLMFlex.Assist.Settings.Import.Load($contentboxcontent);
			}
			
			});
	}
	
	return {
		loadStats: loadStats
	};		
})();