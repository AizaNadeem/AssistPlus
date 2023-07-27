if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.HTML=(function(){
	
		var configHTML="<div class='subcontent'><div><span class='label'>Agile User Name:</span><span class='field'><input type='text' value='' id='AgileUser' data-id='AgileUser' /></span></div>"+
			"<div><span class='label'>Agile Password:</span><span class='field'><input class='pwds' type='password' value='' id='AgilePassword' data-id='AgilePassword' /></span></div>"+
			"<div><span class='label'>Agile Server URL:</span><span class='field'><input  type='text' value='' id='AgileURL' data-id='AgileURL' /></span></div>"+
			"<div><span class='label'>Access Type:</span>" +
			"<span class='field'><div class='styled-select'>" +
				"<select id='accessType' name='accessType'>" +
					"<option value='roles' selected='true'>Roles</option>" +
					"<option value='usergroups'>User Groups</option>" +
				"</select></div>" +
			"</span></div></br>"+
			"<span class='button saveConfig'>Save Configuration</span></div>";
		
		rolesContainerHTML=function(roleText)
		{
			var html="<div class='content-box-header'><h3 class='rolecontainer'>&nbsp;&nbsp;&nbsp;"+roleText+" in Agile PLM<span style='float: right;'><input class='roleFilter agileRoleFilter' type='text'></span></h3>" +
			"<h3 class='rolecontainer' style='width:60%;'>&nbsp;&nbsp;&nbsp;Selected "+roleText+" for Assist<span class='theme'>Theme</span><span style='float: right;'><input class='roleFilter assistRoleFilter' type='text' /></span></h3></div>" +
			"<span class='rolesContainer'><ul id='agile-roles' class='connectedSortable'></ul></span>"+
			"<span class='rolesContainer' style='width:60%;overflow-x: hidden;'><ul id='assist-roles' class='connectedSortable'></ul></span>";
			
			return html;
		};
		
		
	
		var userGroupsContainerHTML="<div class='content-box-header'>" +
				"<h3 class='userGroupContainer'>&nbsp;&nbsp;&nbsp;User Group(s) in Agile PLM<span style='float: right;'>" +
					"<input class='userGroupFilter agileUserGroupFilter' type='text'>" +
				"</span></h3>" +
				"<h3 class='userGroupContainer' style='width:60%;'>&nbsp;&nbsp;&nbsp;Selected User Group(s) for Assist<span class='theme'>Theme</span><span style='float: right;'><input class='userGroupFilter assistUserGroupFilter' type='text' /></span></h3></div>" +
				"<span class='userGroupsContainer'><ul id='agile-usergroups' class='connectedSortable'></ul></span>"+
		"<span class='userGroupsContainer' style='width:60%;overflow-x: hidden;'><ul id='assist-usergroups' class='connectedSortable'></ul></span>";
		
		var changePasswordHTML="<div class='subcontent'><div><span class='label'>Current Password:</span><span class='field'><input type='password' value'' name='cpassword' id='cpassword' /></span></div>"+
			"<div><span class='label'>New Password:</span><span class='field' ><input id='npassword' class='pwds' type='password' value='' name='npassword' /></span></div>"+
			"<div><span class='label'>Confirm Password:</span><span class='field'><input id='cnpassword' class='pwds' type='password' value='' name='cnpassword' /></span></div>"+
			"<span class='button pwdchange'>Change Password</span></div>";
	
		function classesContainerHTML(rolesHtml)
		{
			var html="<div class='content-box-header'>" +
			"<h3 class='rolecontainer' style='width:29%;min-width:261px;'>&nbsp;&nbsp;&nbsp;Agile Classes<span style='float: right;'><input class='roleFilter classFilter' style='width:115px;' type='text'></span></h3>" +
			"<h3 class='rolecontainer' style='width:70%;min-width:629px;'>&nbsp;&nbsp;&nbsp;Attributes<span style='float: right;'><input class='roleFilter attFilter' style='width:140px;' type='text' /></span></h3>" +
			"</div><div class='left' ><table id='class-table' class='listing' cellpadding='0' cellspacing='0'></table></div><div class='rightMain'><div class='rightTop'>" +
			"<table id='attr-header' class='listing' cellpadding='0' cellspacing='0'>" +
			"<tr>" +
			"<th style='width:237px;'>Name</th>" +
			"<th style='width:16px;'><img src='img/notifi.png' alt='*'/></th>" +
			"<th style='width:80px;text-align:center'><img src='img/color-plette.png' alt='Set Attribute Font Color'/></th>" +
			"<th >Description</th>" +
			"<th style='text-align: right; width: 4%;'><a id='saveColorCodes'><img alt='Save' title='Save Color Codes' src='img/save.png'></a></th>"+
			"</tr>" +
			"</table>" +
			"<div id='attr-div' style='height: 121px; overflow: auto;'><table id='attr-attribute' class='listing' cellpadding='0' cellspacing='0'></table>" +
			"</div></div><div class='rightBottom'>" +
			"<table id='text-header' class='listing' cellpadding='0' cellspacing='0'>" +
			"<tr>" +
			"<th style='width:22%;padding:10px;' >"+rolesHtml+"</th>" +
			"<th style='width:5%;padding:10px;' ></th>" +
			"<th style='width:20%;min-width:144px;padding:0;' >Theme</th>" +
			"<th style='padding:0;width:45%'>Assist Text</th>" +
			"<th style='padding: 0 10px 0 0;text-align: right;'><a class='saveAllAssistText' style='display:none;'><img alt='Save All values' title='Save All values' src='img/save-all.png'></a>&nbsp;" +
			"<span class='addNewText ui-icon ui-icon-circle-plus pointer' data-action='new' ></span>" +
			"</th></tr></table>"+
			"<div id='text-div' style='height: 121px; overflow: auto;'><table id='text-table' class='listing' cellpadding='0' cellspacing='0'></table>" +
			"</div>"+
			"</div></div>";
			
			return html;
		}
		
	var RoleHTML=
	{
		rolesHTML:rolesContainerHTML
	};
	var ClassesHTML=
	{
		classesHTML:classesContainerHTML
	};
	return{
		getConfigHTML:configHTML,
		RoleHTML:RoleHTML,
		getUserGroupsContainerHTML:userGroupsContainerHTML,
		getChangePasswordHTML:changePasswordHTML,
		ClassesHTML:ClassesHTML
		
	};
	
})();





