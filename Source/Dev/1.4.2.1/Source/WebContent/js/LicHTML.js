if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.LicHTML=(function(){
		
		var LicHTML="<div class='subcontent'><table id='licinfo-table' style='border:1px solid white;' class='listing' cellpadding='0'	cellspacing='0'>"+
			"</table><br /> <a href='#' id='terms' style='font-size: 14px;'>TERMS OF SERVICE</a> <br />"+
			"<iframe id='licframe' src='LicenseTerms.txt' style='display:none' width='99%'>"+
				"<p>Your browser does not support frames.</p>"+
			"</iframe><span id='ImportLicLnk' style='float:right;margin-top:10px' class='button saveRoles'>Accept Terms of Service and Activate</span>"+
				" <br /></div>";

	return{
		getHTML:LicHTML
		
		
	};
	
})();

