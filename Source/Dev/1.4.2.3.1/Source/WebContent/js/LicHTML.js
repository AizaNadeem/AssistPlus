if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.LicHTML=(function(){
		
		var LicHTML="<div class='subcontent' id='licenseInfo' >"+
		"<table id='licinfo-table' class='listing' cellpadding='0'	cellspacing='0'>"+
			"</table>" +
			"<div id='licRow'>"+
			"<span  id='licRowLabel'>*Access Type :&nbsp;</span>"+
			"<label class='licRadioLeft'  >"+
			  "<input class='licRadioLeftLabel'   type='radio' value='roles' id='AccessTypeRoles' name='radio'/>Roles"+
			"</label>"+
			"<label class='licRadioRgiht'  >"+
			  "<input class='licRadioRgihtLabel' type='radio' value='usergroups' id='AccessTypeUserGroup' name='radio' />User Groups"+
			"</label>"+
			"</div>"+
			"<br>"+
			"<label class='licStarText'> *Access Type could be changed only once.</label></br></br>" +
			"<a href='#' id='terms'>TERMS OF SERVICE</a> <br />"+
			"<iframe id='licframe' src='LicenseTerms.txt' >"+
				"<p>Your browser does not support frames.</p>"+
			"</iframe><span id='ImportLicLnk' class='button saveRoles'>Accept Terms of Service and Activate</span>"+
				" <br /></div>";

	return{
		getHTML:LicHTML
		
		
	};
	
})();

