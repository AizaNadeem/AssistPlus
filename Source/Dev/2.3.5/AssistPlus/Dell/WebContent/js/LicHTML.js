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
			"<span  id='licRowLabel'>*Access Type :&nbsp;</span>" +
			"<div class='radio-box'>"+
			"<label class='radio-label'  >"+
			  "<input class='radio-field'   type='radio' value='roles' id='AccessTypeRoles' name='radio'/>Roles"+
			"</label>"+
			"<label class='radio-label'  >"+
			  "<input class='radio-field' type='radio' value='usergroups' id='AccessTypeUserGroup' name='radio' />User Groups"+
			"</label>" +
			"</div>"+
			"</div>"+
			"<br>"+
			"<label class='licStarText'> *Access Type could be changed only once.</label></br></br>" +
			"<a href='#' id='terms'>TERMS OF SERVICE</a> <br />"+
			"<iframe id='licframe' src='LicenseTerms.txt' >"+
				"<p>Your browser does not support frames.</p>"+
			"</iframe><button id='ImportLicLnk' class='button saveRoles buttonStyle'>Accept Terms of Service and Activate</button>"+
				" <br /></div>";

	return{
		getHTML:LicHTML
		
		
	};
	
})();

