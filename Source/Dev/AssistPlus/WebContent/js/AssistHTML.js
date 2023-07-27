if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.HTML=(function(){
	
	var configHTML="<div class='subcontent'>" +
	"<div>" +
		"<span class='label'>Agile User Name:</span>" +
		"<span class='field'><input type='text' value='' id='AgileUser' data-id='AgileUser' /></span>" +
	"</div>"+
	"<div>" +
		"<span class='label'>Agile Password:</span>" +
		"<span class='field'><input class='pwds' type='password' value='' id='AgilePassword' data-id='AgilePassword' /></span>" +
	"</div>"+
	"<div>" +
		"<span class='label'>Agile Server URL:</span>" +
		"<span class='field'><input  type='text' value='' id='AgileURL' data-id='AgileURL' />" +
		"</span>" +
	"</div>"+
	"<div style='height:29px; width:620px; background-color:#F0F0F0;border:1px solid; border-color:grey; border-left-color:white;border-top-color:white;'>" +
	
	"<span class='label' style='height:21px;float:left;background-color:white;padding-top: 9px; border-color:white; border-right:1px solid; border-right-color:grey;'>*AssistPlus Node Type:</span>" +
	"<div style='border-top:1px solid grey'>"+
	"<label style='float:left;margin-top:7px;padding:2px;;' class='input-check'>"+
	  "<input style='float:left;margin:0px;margin-left:10px;'  type='radio' value='master' id='NodeTypeMaster' name='radio'/>&nbspMaster"+
	"</label>"+
	"<label style='float:left;margin-left:5px;margin-top:7px;padding:2px;vertical-align:middle;' class='input-check'>"+
	  "<input style='float:left;margin:0px; margin-left:30px;' type='radio' value='slave' id='NodeTypeSlave' name='radio' />&nbspSlave"+
	"</label>"+
	"<span style='width:5%;'>" +
	"<img id='helpIcon' src='img/icon_help.png' style='padding-top:8px; float: left; cursor:help;' title='Identifies the current AssistPlus node type.To provide a different Master node, make current AssistPlus node as Slave by selecting Slave option.'/>" +
	"</span>" +	
	"</div>" +
	"<br>"+
		
	"</div>"+
	"<div>" +
		"<span class='label' style='float:left;margin-top:13px;' id='primaryInstanceLabel'>AssistPlus Master Node URL:</span>" +
		"<span class='field'><input  style='margin-top:4px; type='text' value='' id='PrimaryInstanceURL' data-id='PrimaryInstanceURL' disabled='disabled'/></span>" +
	"</div>"+
	"<div>" +
	"<span class='label' style='float:left;margin-top:13px;'>Attribute Hover Color:</span>" +
	"<span id='hoverColor' style='margin-top:5px;float:left'></span>" +
"</div>"+
"</br></br>"+
	"<span class='button saveConfig'>Save Configuration</span>" +
"</div>";

rolesContainerHTML=function(roleText)
{
var html="<div class='settingsFilter'>" +
"<span>" +			
"<h3>"+roleText+" in Agile PLM:</h3>" +
"<input type='text' class='agileRoleFilter'>" +
"</span>" +
"<span>" +
"<h3 id='selectedRole'>Selected "+roleText+" for Assist:</h3>" +
"<input  type='text' class='assistRoleFilter' />" +
"</span>" +
"</div>" +
"<div style='width:100%;'>" +
"<span class='rolesContainer' style='width:50%;overflow-x: hidden;'>" +
"<ul id='agile-roles' class='connectedSortable' style='width:100%;'></ul>" +
"</span>"+
"<span class='rolesContainer' style='width:49.99%;overflow-x: hidden;'>" +
"<ul id='assist-roles' class='connectedSortable' ></ul>" +
"</span>" +
"</div>";

return html;
};



var userGroupsContainerHTML="<div class='content-box-header'>" +
"<h3 class='userGroupContainer'>&nbsp;&nbsp;&nbsp;User Group(s) in Agile PLM<span style='float: right;'>" +
"<input class='userGroupFilter agileUserGroupFilter' type='text'>" +
"</span></h3>" +
"<h3 class='userGroupContainer' style='width:60%;'>&nbsp;&nbsp;&nbsp;Selected User Group(s) for Assist<span class='theme' title='Set Theme Color'><img src='img/color-plette.png' alt='Theme'/></span><span style='float: right;'><input class='userGroupFilter assistUserGroupFilter' type='text' /></span></h3></div>" +
"<span class='userGroupsContainer'><ul id='agile-usergroups' class='connectedSortable'></ul></span>"+
"<span class='userGroupsContainer' style='width:60%;overflow-x: hidden;'><ul id='assist-usergroups' class='connectedSortable'></ul></span>";

var changePasswordHTML="<div class='subcontent'><div><span class='label'>Current Password:</span><span class='field'><input type='password' value'' name='cpassword' id='cpassword' /></span></div>"+
"<div><span class='label'>New Password:</span><span class='field' ><input id='npassword' class='pwds' type='password' value='' name='npassword' /></span></div>"+
"<div><span class='label'>Confirm Password:</span><span class='field'><input id='cnpassword' class='pwds' type='password' value='' name='cnpassword' /></span></div>"+
"<span class='button pwdchange'>Change Password</span></div>";
			
	var importDatabaseHTML=
	"<div class='subcontent'>"+
	
	"<div style='height:29px; width:620px; background-color:#F0F0F0;border:1px solid; border-color:grey; border-left-color:white;border-top-color:white;'>" +
	"<span class='label' style='height:21px;float:left;background-color:white;padding-top: 9px; border-color:white; border-right:1px solid; border-right-color:grey;'>Action:</span>" +
		"<div style='border-top:1px solid grey'>"+
			"<label style='float:left;margin-top:7px;padding:2px;;' class='input-check'>"+
				"<input style='float:left;margin:0px;margin-left:10px;'  type='radio' value='Import' id='importAction' name='radio'/>&nbspImport"+
			"</label>"+
			"<label style='float:left;margin-left:5px;margin-top:7px;padding:2px;vertical-align:middle;' class='input-check'>"+
				"<input style='float:left;margin:0px; margin-left:30px;' type='radio' value='Export' id='exportAction' name='radio' />&nbspExport"+
			"</label>"+	
		"</div>" +
		"<br>"+
	"</div>"+
	
	"<form name='exportForm' action='ExportXML' method='post'>"+
		"<span class='button exportDB'style='margin-top:10px;' >Export</span>"+
		"<p id='download' style='float:left;margin-top:20px;padding:2px;margin-left:210px'>Database Exported. <a style='color:#0000FF;text-decoration:underline;' >Click here</a> to Download!</p>"+
		"<input type='submit' style='display:none' id='exportXML'>"+
	"</form>"+

    "<form id='upload' target='Iframe' action='UploadFile' method='post' enctype='multipart/form-data'>"+
    "<div id='upperBox' style='height:29px; width:620px;margin-top:4px;  background-color:#F0F0F0;border:1px solid; border-color:grey; border-left-color:white;border-top-color:white;'>" +
		"<iframe style='display:none' id='Iframe' name='Iframe'>sdf</iframe>"+
			"<span class='label' id='fileName' style='height:21px;float:left;background-color:white;padding-top: 9px; border-color:white; border-right:1px solid; border-right-color:grey;'>File Name:</span>"+
			"<input type='file' style='display:none' name='importPath' id='importPath' val='...'  />"+ 
			"<div style='border-top:1px solid grey'><a style='float:left;margin-top:7px;padding:2px;;' id='path'/><button id='chooseFile' style='height:28px;margin-right:0px;' type='button'>...</button><div>"+
	"</div>"+
	"</form>"+
	"<div id='lowerBox' style='height:29px; width:620px;margin-top:5px; background-color:#F0F0F0;border:1px solid; border-color:grey; border-left-color:white;border-top-color:white;'>" +
		"<span class='label' id='importType' style='height:21px;float:left;background-color:white;padding-top: 9px; border-color:white; border-right:1px solid; border-right-color:grey;'>Import Type:</span>" +
			"<div id='radio' style='border-top:1px solid grey'>"+
				"<label style='float:left;margin-top:7px;padding:2px;;' class='input-check'>"+
					"<input style='float:left;margin:0px;margin-left:10px;'  type='radio' value='overWrite' id='TypeOverWrite' name='radio'/>&nbspOver Write"+
				"</label>"+
				"<label style='float:left;margin-left:5px;margin-top:7px;padding:2px;vertical-align:middle;' class='input-check'>"+
					"<input style='float:left;margin:0px; margin-left:30px;' type='radio' value='merge' id='TypeMerge' name='radio' />&nbspMerge"+
				"</label>"+	
			"</div>" +
			"<br>"+
	  "</div>"+
	  "</br>"+
	  "<span style='margin-top:10px;' class='button importDB'>Import</span>" +
	"</div>";

	
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
			"<th style='width:80px;'  title='Set Attribute Font Color'><img style='margin-left:20px' src='img/color-plette.png' alt='Set Attribute Font Color'/></th>" +
			"<th >Description</th>" +
			"<th style='text-align: right; width: 4%;'><a id='saveColorCodes'><img alt='Save' title='Save Color Codes' src='img/save.png'></a></th>"+
			"</tr>" +
			"</table>" +
			"<div id='attr-div' style='height: 121px; overflow: auto;'><table id='attr-attribute' class='listing' cellpadding='0' cellspacing='0'></table>" +
			"</div></div><div class='rightBottom'>" +
			"<div id='text-div' style='height: 121px; overflow: auto;'>"+
			"<table id='text-table' class='listing' cellpadding='0' cellspacing='0' style='margin:0px;padding:0px;width:835px;'>" +
			"<thead>"+
			"<tr>" +
				"<th style='font-size:13px;'>Workflows</th>"+
				"<th style='font-size:13px;'>"+rolesHtml+"</th>" +
				"<th></th>" +
				"<th title='Set Assist Text Color' ><img src='img/color-plette.png' alt='Theme'/></th>" +
				"<th style='font-size:13px;'> Assist Text</th>" +
				"<th style=''> <a class='saveAllAssistText' style='display:none;'>" +
				"<img alt='Save All values' title='Save All values' src='img/save-all.png'></a>" +
				"<span class='addNewText ui-icon ui-icon-circle-plus pointer' data-action='new' style='float:right;' ></span>" +
				"</th></tr></thead></table>" +
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
		getImportDatabaseHTML:importDatabaseHTML,
		ClassesHTML:ClassesHTML
		
	};
	
})();





