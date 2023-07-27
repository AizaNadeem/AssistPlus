if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.LicHTML=(function(){
		
		var LicHTML="<div class='subcontent'><table id='licinfo-table' style='border:1px solid white;' class='listing' cellpadding='0'	cellspacing='0'>"+
			"</table>" +
			"<div style='background-color:#D0D0D0;height:29px; width:617px; border: 1px solid;border-color:white;'>"+
			"<span class='label' style='float:left;border-right:1px solid white;height:29px;font-family:arial; font-size:13px;color:#606060;padding-right:0;padding-top:7px;'>*Access Type :&nbsp;</span>"+
			"<label style='float:left;margin-top:7px;padding:1px;' class='input-check'>"+
			  "<input style='float:left;margin:0px;margin-left:10px;'  type='radio' value='roles' id='AccessTypeRoles' name='radio'/>&nbspRoles"+
			"</label>"+
			"<label style='float:left;margin-left:5px;margin-top:7px;padding:1px;' class='input-check'>"+
			  "<input style='float:left;margin:0px;margin-left:30px;' type='radio' value='usergroups' id='AccessTypeUserGroup' name='radio' />&nbspUser Groups"+
			"</label>"+
			"</div>"+
			"<br>"+
			"<label style='margin:10px;margin-left:0px;'> *Access Type could be changed only once.</label></br></br>" +
			"<a href='#' id='terms' style='font-size: 14px;'>TERMS OF SERVICE</a> <br />"+
			"<iframe id='licframe' src='LicenseTerms.txt' style='display:none' width='99%'>"+
				"<p>Your browser does not support frames.</p>"+
			"</iframe><span id='ImportLicLnk' style='float:right;margin-top:10px' class='button saveRoles'>Accept Terms of Service and Activate</span>"+
				" <br /></div>";

	return{
		getHTML:LicHTML
		
		
	};
	
})();

