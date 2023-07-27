if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.HTML=(function(){
	
	var configHTML="<div class='subcontent' id='config'>" +
	
	"<div class='box-container'>" +
		"<span class='field'><input class='text' type='text' value='' id='AgileUser' data-id='AgileUser' /></span>" +
		"<span class='label'>Agile User Name:</span>" +
	"</div>"+
	
	"<div class='box-container'>" +
	    "<span class='field'><input class='pwds' type='password' value='' id='AgilePassword' data-id='AgilePassword' /></span>" +
		"<span class='label'>Agile Password:</span>" +
	"</div>"+
	
	"<div class='box-container'>" +
		"<span class='field'><input  type='text' class='text' value='' id='AgileURL' data-id='AgileURL' /></span>" +
		"<span class='label'>Agile Server URL:</span>" +
	"</div>"+
	
	"<div class='box-container'>"+
		"<div class='box'>" +
			"<div class='radio-box'>"+
				"<label class='radio-left-label'  >"+
				"<input class='radio-left'  type='radio' value='master' id='NodeTypeMaster' name='radio'/>Master"+
				"</label>"+
				"<label class='radio-right-label' >"+
				"<input class='radio-right' type='radio' value='slave' id='NodeTypeSlave' name='radio' />Slave"+
				"</label>"+
			"</div>" +
		"</div>"+
		"<span class='label'>*AssistPlus Node Type:</span>" +
	"</div>"+
	
	"<div class='box-container'>" +
		"<span class='field'><input  type='text' value=''class='text' id='PrimaryInstanceURL' data-id='PrimaryInstanceURL' disabled='disabled'/></span>" +
		"<span class='label' id='primaryInstanceLabel'>Master Node URL:</span>" +
	"</div>"+
	"<div class='box-container' id='hidden'>" +
		"<span class='field'><input  type='text'/></span>" +
		"<span class='label'></span>" +
	"</div>"+
	
	"<div class='box-container' id='enableOptOut'>"+
	"<div class='box'>" +
		"<div class='radio-box'><form>"+
			"<label class='radio-label'>"+
				"<input class='radio-field' type='radio' value='Yes' id='enableOptOut-Yes' name='enableOptOut'/>Yes"+
			"</label>"+
			"<label class='radio-label'>"+
				"<input class='radio-field' type='radio' value='No' id='enableOptOut-No' name='enableOptOut'/>No"+
			"</label>"+
		"</form></div>" +
	"</div>"+
	"<span class='label'>Enable User Opt-Out:</span>" +
	"</div>"+
	
	"<div class='box-container' id='indicateNewText'>"+
	"<div class='box'>" +
		"<div class='radio-box'><form>"+
			"<label class='radio-label'  >"+
				"<input class='radio-field'  type='radio' value='Yes' id='showNew'  name='shownew'/>Yes"+
			"</label>"+
			"<label class='radio-label' >"+
				"<input class='radio-field'  type='radio' value='No' id='disableNew' name='shownew' />No"+
			"</label>"+
		"</form></div>" +
	"</div>"+
	"<span class='label'>Indicate Newly Updated Text:</span>" +
	"</div>"+
	"<div class='box-container' id='durationBox'>"+
	"<div class='box'>" +
		"<div class='radio-box'><form>"+
			"<label class='radio-label'  >"+
				"<input class='radio-field'  type='radio' value='1'  name='duration'/>1"+
			"</label>"+
			"<label class='radio-label' >"+
				"<input class='radio-field'  type='radio' value='2'  name='duration' />2"+
			"</label>"+
			"<label class='radio-label'  >"+
				"<input class='radio-field'  type='radio' value='3'  name='duration'/>3"+
			"</label>"+
			"<label class='radio-label' >"+
				"<input class='radio-field'  type='radio' value='4'  name='duration' />4"+
			"</label>"+
			"<label class='radio-label'  >"+
				"<input class='radio-field'   type='radio' value='5' name='duration'/>5"+
			"</label>"+
			"<label class='radio-label' >"+
				"<input class='radio-field'  type='radio' value='6'  name='duration' />6"+
			"</label>"+
			"<label class='radio-label'  >"+
				"<input class='radio-field'  type='radio' value='7'  name='duration'/>7"+
			"</label>"+
			"<label class='radio-label' >"+
				"<input class='radio-field'  type='radio' value='8'  name='duration' />8"+
			"</label>"+
			"<label class='radio-label'  >"+
				"<input class='radio-field'  type='radio' value='9'  name='duration'/>9"+
			"</label>"+
			"<label class='radio-label' >"+
				"<input class='radio-field'  type='radio' value='10'  name='duration' />10"+
			"</label>"+
		"</form></div>" +
	"</div>"+
	"<span class='label'>Updated Text Duration (Days):</span>" +
"</div>"+
	
	"<div class='box-container'>" +
		"<div class='hoverbox' >"+
			"<span id='hoverColor'  ></span>" +
		"</div>"+
		"<span class='label'>Attribute Hover Color:</span>" +
	"</div>"+
	
	"<div class='box-container'>" +
	"<div class='hoverbox' >"+
		"<span id='colorPalette'  ></span>" +
	"</div>"+
	"<span class='label'>Default Text Theme:</span>" +
	"</div>"+
	
	"<label class='star-text'> *To designate a different Master node, select Slave for current Assist+ node.</label>" +
	"<button class='button saveConfig buttonStyle' id='saveConfig'>Save Configuration</button>" +
	
"</div>";

var changePasswordHTML=
"<div class='subcontent' id='changePassword'>"+
	"<div class='box-container'>"+
		"<span class='field'><input type='password' value'' class='text' name='cpassword' id='cpassword' /></span>"+
		"<span class='label'>Current Password:</span>"+
	"</div>"+
	
	"<div class='box-container'>"+
			"<span class='field' ><input id='npassword' class='pwds' type='password' value='' name='npassword' /></span>"+
		"<span class='label'>New Password:</span>"+
	"</div>"+
	
	"<div class='box-container'>"+
		"<span class='field'><input id='cnpassword' class='pwds' type='password' value='' name='cnpassword' /></span>"+
		"<span class='label'>Confirm Password:</span>"+
	"</div>"+
	
	"<button class='button pwdchange buttonStyle'>Change Password</button>"+
"</div>";
			
var importDatabaseHTML=
	
		"<div class='subcontent' id='import-export'>"+
			
			"<div class='box-container'>"+
				"<div class='box' >" +
					"<div class='radio-box'><form>"+
						"<label class='radio-left-label' >"+
							"<input class='radio-left'  type='radio' value='Import' id='importAction' name='radio'/>Import"+
						"</label>"+
						"<label class='radio-right-label' >"+
							"<input class='radio-right' type='radio' value='Export' id='exportAction' name='radio' />Export"+
						"</label></form>"+	
					"</div>" +
				"</div>"+
				"<span class='label' >Action:</span>" +
			"</div>"+
			
			"<div id='exportBox' class='box-container' >"+
				"<button class='button exportDB buttonStyle' >Export</button>"+
				"<form name='exportForm' action='DownloadXML' method='post'>"+					
					"<p id='download' >Database Exported. <a id='link'>Click here</a> to Download!</p>"+
					"<input type='submit' id='exportXML'>"+
				"</form>"+
			"</div>"+
			
			"<div class='box-container'>"+
				"<div class='box' id='upperBox'>" +
					"<form id='upload' target='Iframe' action='UploadFile' method='post' enctype='multipart/form-data'>"+	
						"<iframe  id='Iframe' name='Iframe'/>"+
						"<a id='path'/><input type='file' name='importPath' id='importPath'  /><button id='chooseFile' type='button'>...</button>"+
					"</form>"+
				"</div>"+
				"<span class='label' id='fileName'>File Name:</span>"+
			"</div>"+	
		
			"<div class='box-container'>"+
				"<div class='box' id='lowerBox' >" +			
					"<div id='radio' class='radio-box'>"+
						"<label class='radio-left-label' >"+
							"<input class='radio-left'  type='radio' value='overWrite' id='TypeOverWrite' name='radio'/>Overwrite"+
						"</label>"+
						"<label class='radio-right-import'  >"+
							"<input  type='radio' value='merge' id='TypeMerge' name='radio' />Merge"+
						"</label>"+	
					"</div>" +
				"</div>"+
				"<span class='label' id='importType' >Import Type:</span>" +
			"</div>"+
			
			"<button  class='button importDB buttonStyle'>Import</button>"+
	"</div>";

var xlsxImportHTML = "<div class='subcontent' id='import-export'>" +
"<div class='box-container'>" +
	"<div class='box' id='upperBox'>" +
	"<form id='upload' method='POST' enctype='multipart/form-data' action='ImportExcel'>" +
		"<a id='path'/>" +
		"<input type='file' name='importPath' id='importPath'/>" +
		"<button id='chooseFile' type='button'>...</button>" +
		"<input type=hidden name='type'/>" +
	"</form>" +
	"</div>" +
	"<span class='label'>File Name:</span>" +
"</div>" +

"<div class='box-container'>" +
	"<div class='box' id='lowerBox'>" +
		"<div id='radio' class='radio-box'>" +
			"<input type='radio' name='importType' value='overwrite' /> Overwrite" +
			"<input type='radio' name='importType' value='merge' /> Merge" +
		"</div>" +
	"</div>" +
	"<span class='label'>Import Type:</span>" +
"</div>" +

"<button class='button buttonStyle' id='b-import-xlsx'>Import</button>"+
"</div>";

rolesContainerHTML=function(roleText)
{
var html=
"<div class='settingsFilter'>" +
	"<div class='filter'>" +			
		"<h3>Agile PLM "+roleText+":</h3>" +
		"<input type='text' class='agileRoleFilter' onkeyup=\"if( event.keyCode == 27 )this.value='' \" />" +
		"<span class='pointer' id='selectAll' title='Select All'></span>"+
	"</div>" +
	"<div class='filter'>" +
		"<span class='pointer' id='removeAll' title='Unselect All'></span>"+
		"<input  type='text' class='assistRoleFilter'  onkeyup=\"if( event.keyCode == 27 )this.value='' \" />" +
		"<h3 id='selectedRole'>Selected "+roleText+":</h3>" +				
	"</div>" +
"</div>" +

"<div  class='rolesDiv'>" +
	"<span class='rolesContainer rolesList'  >" +
		"<ul id='agile-roles' class='connectedSortable' ></ul>" +
	"</span>"+
	"<span class='rolesContainer rolesList'  >" +
		"<ul id='assist-roles' class='connectedSortable' ></ul>" +
	"</span>" +
"</div>";

return html;
};
	
		function classesContainerHTML(rolesHtml)
		{
			var html=
			"<div class='settingsFilter'>" +
			 	"<div class='textFilter classWidth'>"+
					"<h3>Agile Classes</h3>" +
					"<input class='classFilter' type='text' onkeyup=\"if( event.keyCode == 27 )this.value='' \" />" +
				"</div>"+
				"<div class='textFilter attrWidth'>"+
					"<h3>Attributes</h3>" +
					"<input class='attFilter'  type='text' onkeyup=\"if( event.keyCode == 27 )this.value='' \" />" +
				"</div>" +
			"</div>" +
			
			"<div class='left' >" +
				"<table id='class-table' class='listing' cellpadding='0' cellspacing='0'></table>" +
			"</div>" +
			
			"<div class='rightMain'>" +
				"<div class='rightTop'>" +
					"<table id='attr-header' class='listing' cellpadding='0' cellspacing='0'>" +
						"<tr>" +
							"<th class='name' >Name</th>" +
							"<th class='hasText' >Text</th>" +
							"<th class='labelColor' title='Set Attribute Font Color'>" +
								"Color"+
							"</th>" +
							"<th >Description</th>" +
							"<th class='saveColors'>" +
								"<a id='saveColorCodes'>" +
									"<img alt='Save' title='Save Label Colors' src='img/save3.png'>" +
								"</a>" +
							"</th>"+
						"</tr>" +
					"</table>" +
					"<div id='attr-div' >" +
						"<table id='attr-attribute' class='listing' cellpadding='0' cellspacing='0'></table>" +
					"</div>" +
				"</div>" +
				"<div class='rightBottom'>" +
				"<div style='min-width:760px;'>"+
				"<div class='header-wrapper'>"+
				"<div style='min-width:760px;'>"+
				"<table id='text-header'  style='min-width:760pxpx;' class='listing' cellpadding='0' cellspacing='0'>" +
				"<thead>"+
				"<tr>" +
					"<th class='tableWorkflow'>Workflows / Lifecycles</th>"+
					"<th class='tableRoles' >"+rolesHtml+"</th>" +
					"<th class='tableAssistText' > Assist Text</th>" +
					"<th class='setTheme'></th>" +
					"<th class='saveAllHeader'>" +
						"<a class='saveAllAssistText' >" +
							"<img alt='Save All values' title='Save All values' src='img/save-all.png'>" +
						"</a>" +
							"<span class='addNewText addbtn pointer' title='Add Text' data-action='new'  ></span>" +
					"</th>" +
				"</tr>" +
				"</thead>"+
				"</table>"+
				"</div>"+
				"</div>"+
					"<div id='text-div'>"+
						"<table id='text-table' class='listing' cellpadding='0' cellspacing='0'>" +
						"<thead style='height:0px'>"+
							"<tr style='height:0px'>" +
								"<th class='tableWorkflow' style='border-bottom-width: 0px;padding:0px;'></th>"+
								"<th class='tableRoles' style='border-bottom-width: 0px;padding:0px;' ></th>" +
								"<th class='tableAssistText' style='border-bottom-width: 0px;padding:0px;'> </th>" +
								"<th class='setTheme' style='border-bottom-width: 0px;padding:0px;'></th>" +
								"<th class='saveAllHeader' style='border-bottom-width: 0px;padding:0px;'>" +
									"<a class='saveAllAssistText' >" +
										"<img alt='Save All values' title='Save All values' src='img/save-all.png'>" +
									"</a>" +
								"</th>" +
							"</tr>" +
						"</thead>" +
						"</table>" +
					"</div>"+
				"</div>"+
				"</div>" +
			"</div>";
			
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
		getChangePasswordHTML:changePasswordHTML,
		getImportDatabaseHTML:importDatabaseHTML,
		xlsxImportHTML: xlsxImportHTML,
		ClassesHTML:ClassesHTML
		
	};
	
})();





