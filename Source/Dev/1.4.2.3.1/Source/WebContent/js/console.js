var browser=$.browser;
	var bodyClass="";
	if(browser.msie ){
		if(browser.version == "7.0"){
			bodyClass="ie7";
		}else {bodyClass="ie"; }
	}else if(browser.mozilla){
		bodyClass="firefox";
	}
	
$(document).ready(function() {
		initialize();
	});

var rolelist=new Array; ///this one way of declaring array in javascript
var classid=0;
var attid=0;

function initialize()
{
	$('body').addClass(bodyClass);
	
	$('#confTabLnk').click(function(){showConfTab();});
	$('#assistTextTabLnk').click(function(){showAssistTextTab();});
	$('#logoutTabLnk').click(function(){logout();});
	
	
	$('#LoginLnk').click(function(){authenticate();});
	$('.pwdChangeSection').click(function(){ showSection('ChangePwdSect');});
	$('#ChangePwdLnk').click(function(){changePwd();});
	$('.roleSection').click(function(){showSection('RolesSect');});
	$('.refreshRoles').click(function(){refreshRolesList();});
	$('.saveRoles').click(function(){saveRolePriority();});
	$('.configSection').click(function(){ showSection('ConfSect');});
	$('.loadConfig').click(function(){loadConfigs();});
	$('.saveConfig').click(function(){saveConfigurations();});

	$('.refreshClasses').click(function(){refreshClassTable();});
	$('.refreshAtt').click(function(){refreshAttributeTable();});
	$('.refreshAssistText').click(function(){refreshAssistTextTable();});
	$('.saveAllAssistText').click(function(){saveAllAssistText();});
	
	initFields();
	
	$(".loading").hide();
	$(".notifimg").hide();
	showLoginBox();
	
}

function showLoginBox() 
{	
	/*		$('#confTabLnk').attr('class',"");
		$('#assistTextTabLnk').attr('class',"");
		$('#userRolesTabLnk').attr('class',"");
	 */	
	$('.tab').hide();
	//$('#AssistTextTab').hide();
	//$('#UserRolesTab').hide();
	$('#top-navigation').hide();
	$('#LoginForm').show();
	$('#username').focus();
}

function showConfTab(){
	$('#confTabLnk').attr('class',"active");
	$('#assistTextTabLnk').attr('class',"");
	//$('#userRolesTabLnk').attr('class',"");

	$('#ConfTab').show();
	$('#AssistTextTab').hide();
	//$('#UserRolesTab').hide();	

	loadConfigs();
	loadRoles();
	$( "#assist-roles, #agile-roles" ).sortable({
		connectWith: ".connectedSortable",
		stop: stopped
	}).disableSelection();
}






function stopped(event,ui){
	if(ui.item.parents('ul#assist-roles').length > 0){
		var roleText= ui.item.text();
		ui.item.html("<div class='roleText'><span class='text'>"+roleText+"<span></div>");
		AssistColorPicker.createPicker(ui.item.find('.roleText'),'#ffffff','#333333');
	}else{
			if(ui.item.length>0){
				var $textDiv=ui.item.find('.roleText span');
				var roleText=$textDiv.text();
				ui.item.html(roleText);
				ui.item.find(':last-child').remove();
			}
	}
	
}

function showAssistTextTab(){
	$('#assistTextTabLnk').attr('class',"active");
	$('#confTabLnk').attr('class',"");
	//$('#userRolesTabLnk').attr('class',"");

	$('#ConfTab').hide();
	$('#AssistTextTab').show();
	//$('#UserRolesTab').hide();

	loadClasses();
	//$('#header').tabs();

}
/*function showUserRolesTab(){
		$('#userRolesTabLnk').attr('class',"active");
		$('#assistTextTabLnk').attr('class',"");
		$('#confTabLnk').attr('class',"");

		$('#ConfTab').hide();
		$('#AssistTextTab').hide();
		$('#UserRolesTab').show();

		loadRoles();
		$( "#assist-roles, #agile-roles" ).sortable({
			connectWith: ".connectedSortable"
		}).disableSelection();
	}*/
function logout(){

	$('#h_userId').val('');
	showLoginBox();
	notify("info", "Logged out");
}

function reSizeDivs() {
	
}

function initFields() {
	$('.tblFilterBox').blur(function() {
		if(this.value=='') 
		{
			this.value='filter...';	
		}
	});

	$('.tblFilterBox').focus(function()
			{
		if(this.value=='filter...') 
		{
			this.value='';
		}
			});

	$(".pwds").keyup(function(event)
			{   
		if(event.keyCode == 13){
			$(this).val(cne46b5dm($(this).val()));
			$("#ChangePwdLnk").click();   
		} 
			});
	$(".pwds").blur(function(event)
			{   
		$(this).val(cne46b5dm($(this).val()));
			});

	$("#password").keyup(function(event)
			{   
		if(event.keyCode == 13){
			$(this).val(cne46b5dm($(this).val()));
			$("#LoginLnk").click();   
		} 
			});
	$("#password").blur(function(event)
			{   
		$(this).val(cne46b5dm($(this).val()));
			});
	showSection('ConfSect');
}

function loadClasses() {

	notify("busy","Loading classes from Agile PLM...");
	$("#classloading").show();
	truncateTable("class-table");
	$('#h_classId').val('');
	$('#h_attrId').val('');
	truncateTable("text-table");
	truncateTable("attr-table");
	$.get(
			"/AssistPlus/loadClasses?rnd=" + Math.random(),
			function(responseJson) {
				$("#classloading").hide();
				if (responseJson.hasOwnProperty("object"))
				{
					var $table = $('#class-table');//.appendTo($('#left-column')); // Create HTML <table> element and append it to HTML DOM element with ID "somediv".
					var count=0;
					$.each(
							responseJson.object,
							function(index, classentry) { // Iterate over the JSON array.
								
								$('<tr id='+classentry.idVal+' class='+classentry.classVal+'>')
								.appendTo($table)
								// Create HTML <tr> element, set its text content with currently iterated item and append it to the <ul>.
								.append(
										$(
												'<td style="width:85%" class=style'
												+ classentry.level
												+ ' onClick=loadAttributes("'
												+ classentry.idVal
												+ '","'
												+ classentry.level
												+ '"); >').append($('<a>').text(
														classentry.name)))
								.append(
										$('<td style="width:16px;text-align:right;" id="HasAssistTextC'+classentry.idVal+'">').text(" "));
								//.append($('<td style="width: 6px;">'));
								if(responseJson.object[count].hasTextFlag =='yes')
								{
									$('#HasAssistTextC'+classentry.idVal+'').append($('<img src="img/dot.gif" height="11px" alt="Has Assist Text" title="Has Assist Text">'));
								}
								count++;
							});
					$table.treeTable();
				}
				notify(responseJson.status, responseJson.message);
				//$('a:hover').css('cursor','auto');
			});
}

function loadAttributes(classIdParam, levelParam) {

	notify("busy","Loading attributes for "+$('#'+classIdParam).text()+"...");
	var prevClass = $('#h_classId').val();
	$('#h_classId').val(classIdParam);
	if (prevClass != "")
		$("#"+prevClass+" td").css("background","#FFFFFF");
	$("#"+classIdParam+" td").css("background","#F2D271");
	//$('#h_classId').val(classIdParam);

	truncateTable("text-table");
	truncateTable("attr-table");

	//$('#attrloading img').css("margin-top", ((this.parent().height())/2)+"px");

	$('#attrloading').show();
	
	$.get("/AssistPlus/loadAttributes?classId=" + classIdParam + "&level="
			+ levelParam + "&rnd=" + Math.random(), function(responseJson) {
		$("#attrloading").hide();
		if (responseJson.hasOwnProperty("object"))
		{

			var $table = $('#attr-table');
			var count=0;
			$.each(responseJson.object, function(index, attrentry) { 

				$('<tr id='+attrentry.attrIdVal+' style="width=100%;">').appendTo($table) 
				.append(
						$(
								'<td style="width:250px;" onClick=loadAssistTexts("'
								+ attrentry.classIdVal + '","'
								+ attrentry.attrIdVal + '"); > ').append($('<a>').text(
										attrentry.attrName)))
				.append(
						$(
								'<td style="width:16px;border-right:1px solid #9097A9;border-left:1px solid #9097A9;" id=HasAssistText'+attrentry.attrIdVal+' >').text(" "))
				.append(
						$(
								'<td onClick=loadAssistTexts("'
								+ attrentry.classIdVal + '","'
								+ attrentry.attrIdVal + '"); >').text(
										attrentry.attrDescription));
				
				if(responseJson.object[count].hasTextFlag =='yes')
				{
							$('#HasAssistText'+attrentry.attrIdVal+'')
									.append($("<img src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text'>"));

				}
				count++;
				//******

				/*

				$('<tr id='+attrentry.attrIdVal+' style="width=100%;">').appendTo($table) 
				.append(
						$(
								'<td style="width:30%;" onClick=loadAssistTexts("'
										+ attrentry.classIdVal + '","'
										+ attrentry.attrIdVal + '"); >').append($('<a>').text(
								attrentry.attrName))) 
				.append(
						$(
								'<td style="width:70%;" onClick=loadAssistTexts("'
										+ attrentry.classIdVal + '","'
										+ attrentry.attrIdVal + '"); >').text(
								attrentry.attrDescription));*/
			});


			c = document.getElementById("attr-table").rows.length;
			if (c == 0) {
				$('<tr>').appendTo($table) 
				.append($('<td>').text('No Attribute(s) to display.')); 
			}
		}
		notify(responseJson.status, responseJson.message);
	});
}

function loadAssistTexts(classIdParam, attrIdParam) {
	notify("busy","Loading assist texts for "+$('#'+attrIdParam).text()+"...");
	var prevAttr = $('#h_attrId').val();
	if (prevAttr != "")
		$("#"+prevAttr+" td").css("background","#FFFFFF");
	//$("#"+($('#h_attrId').val())+" td").css("background","#FFFFFF");
	$("#"+attrIdParam+" td").css("background","#F2D271");

	$('#h_attrId').val(attrIdParam);
	truncateTable("text-table");
	$("#textloading").show();
	classid=classIdParam;
	attid=attrIdParam;
	//alert(classid);
	//alert(attid);
	$.get("/AssistPlus/loadAssistTexts?classId=" + classIdParam + "&attrId="
			+ attrIdParam + "&rnd=" + Math.random(),
			function(responseJson) {
		$("#textloading").hide();
		if (responseJson.hasOwnProperty("object"))
		{

			var $table = $('#text-table');//.appendTo($('#attr-div')); // Create HTML <table> element and append it to HTML DOM element with ID "somediv".

			$.each(responseJson.object, function(index, textentry) { // Iterate over the JSON array.
				addAssistTextRow(textentry);
			});

			c=document.getElementById("text-table").rows.length;
			if (c==0)
			{
				$('<tr id="rowNoTexts">').appendTo($table)                     // Create HTML <tr> element, set its text content with currently iterated item and append it to the <ul>.
				.append($('<td>').text('No Assist Text(s) to display.'));      // Create HTML <td> element, set its text content with name of currently iterated product and append it to the <tr>.
			}
		}
		notify(responseJson.status, responseJson.message);
	});
}


function markActiveLink(el) {   
    alert($(el).attr("id"));
}

function loadConfigs() {
	notify("busy","Loading Configurations...");
	truncateTable("conf-table");
	$("#confloading").show();

	$.get("/AssistPlus/loadConfig?&rnd=" + Math.random(),
			function(responseJson) {

		$("#confloading").hide();
		if (responseJson.hasOwnProperty("object"))
		{

			var $table = $('#conf-table');//.appendTo($('#attr-div')); // Create HTML <table> element and append it to HTML DOM element with ID "somediv".

			$.each(responseJson.object, function(index, configentry) { // Iterate over the JSON array.

				$('<tr>')
				.appendTo($table)
				.append($('<td style="text-align:right;width:20%">').text(configentry.key))
				.append($('<td style="width:80%">')
						.append($('<input id="cfg'+configentry.id+'" type="'+configentry.type+'" class="'+configentry.type+'">').val(configentry.value)));
				//text(configentry.value));
				//.append($('<input type="text" class="text" style="width=100%;">').text(configentry.value)));
				if(configentry.type == "password")
				{
					$('#cfg'+configentry.id).val(ced46b($('#cfg'+configentry.id).val()));
					//alert($('#cfg'+configentry.id).val());
				}
			});

			var c=document.getElementById("conf-table").rows.length;
			if (c==0)
			{
				$('<tr id="rowNoTexts">').appendTo($table)                     // Create HTML <tr> element, set its text content with currently iterated item and append it to the <ul>.
				.append($('<td>').text('No Configurations defined.'));      // Create HTML <td> element, set its text content with name of currently iterated product and append it to the <tr>.
			}
		}
		notify(responseJson.status,responseJson.message);

	});
}

function loadRoles() {
	notify("busy","Loading Role Preferences...");

	$("#assist-roles li").each(function(i, el){
		$(el).remove();        
	});
	$("#agile-roles li").each(function(i, el){
		$(el).remove();        
	});
	$("#rolesloading").show();
	//truncateTable("class-table");
	$.get(
			"/AssistPlus/loadRoles?rnd=" + Math.random(),
			function(responseJson) {
				$("#rolesloading").hide();
				if (responseJson.hasOwnProperty("object"))
				{
					var $assistRoles = $('#assist-roles');//.appendTo($('#left-column')); // Create HTML <table> element and append it to HTML DOM element with ID "somediv".
					var $agileRoles = $('#agile-roles');
					$.each(
							responseJson.object,
							function(index, roleentry) { // Iterate over the JSON array.
								if (roleentry.priority < 0) {
									($('<li id='
											+ roleentry.roleID
											+ ' class="ui-state-default">')
											.text(roleentry.Role))
											.appendTo($agileRoles);
								} else {
									
									$list=$('<li id='
											+ roleentry.roleID
											+ ' class="ui-state-default">')
											.html("<div class='roleText'> <span class='text'>"+roleentry.Role+"</span></div>").appendTo($assistRoles);
									AssistColorPicker.createPicker($list.find('.roleText'),roleentry.fontColor,roleentry.backgroundColor);
									
											
								}
							});
				}
				notify(responseJson.status, responseJson.message);
			});
}

function addNewAssistText() {
	if($('#h_classId').val() == '' || $('#h_attrId').val() == '')
	{
		notify("error","Please select a Class and Attribute to proceed");
	}
	else
	{
		notify("busy","Adding new Assist Text...");
		$.post("/AssistPlus/loadAssistTexts", {
			mode : "new",
			classId : $('#h_classId').val(),
			attrId : $('#h_attrId').val(),
			rnd: Math.random()
		}, function(responseJson) {
			if (responseJson.hasOwnProperty("object"))
			{
				$('#rowNoTexts').remove();
				addAssistTextRow(responseJson.object);
			}
			notify(responseJson.status,responseJson.message);
		}, "json");
	}
}

function removeAssistText(textIdParam) {
	notify("busy","Removing Assist Text entry...");
	$.post("/AssistPlus/loadAssistTexts", {
		mode : "remove",
		textId : textIdParam,
		rnd: Math.random()
	}, function(responseJson) {
		if(responseJson.status == "success")
		{
			$('#row' + textIdParam).remove();
		}
		notify(responseJson.status,responseJson.message);
	},"json");
}


function saveAllAssistText() {
	notify("busy","Saving All Assist Texts ...");
	var text=[];
	var coun=0;
	var j=0;
	var saveData=[];
	var rowId=[];
	var temp;
	var inv=0;
	$('#text-table tr').each(function(){
		coun=0;
		$(this).find('td').each(function(){

			var rowId=$(this).parent('tr').attr('id');
			$('#'+rowId).css("outline","#FFFFFF solid 1px");
		});
	});
	
	$('#text-table tr').each(function(){
		coun=0;
		$(this).find('td').each(function(){

			//do your stuff, you can use $(this) to get current cell
			if (typeof $(this).find('textarea').val() != "undefined") 
			{
				rowId[0]=$(this).parent('tr').attr('id');
				//alert($(this).find('textarea').val());
				text[0]=$(this).find('textarea').val();
				coun=1;
			}
			if(coun==1)
			{
				var trowId=rowId[0];
				trowId=trowId.replace("row","");
				var croles=[];
				$('#roleop' + trowId + ' :selected').each(function(i, selected) {
					croles[i] = $(selected).val();
				});
				
				
				if(text[0]=='' || croles.length<1)
				{
					//alert(rowId[0]);
						inv=1;
						var rowe=$(this).parent('tr').attr('id');
						$('#'+rowe).css("outline","red solid 1px");
						//removeAssistText(trowId);
						notify("error","Invalid Entry");
					
				}
				if(inv==1)
					notify("error","Invalid Entry");
				
					var roles = [];
					var rowid=$(this).parent('tr').attr('id');
					//alert('before'+rowid);
					rowid= rowid.replace("row","roleop");
					//alert('rowId');
								
					$('#'+rowid + ' :selected').each(function(i, selected) {
					//alert(rolelist[j]);
					roles[i] = $(selected).val();
						
					});
					/*$(rolelist[j] + ' :selected').each(function(i, selected) {
						roles[i] = $(selected).val();
						
						//alert('Row 1 is : '+roles[i]+'  '+text);
					});*/
					coun=0;	
					j++;
					
					var fontcolor=	AssistColorPicker.getHexColor($('#'+rowId).find('.pickerTD div.fontcolor div').css('background-color'));
					var backgroundcolor= AssistColorPicker.getHexColor(	$('#'+rowId).find('.pickerTD div.background div').css('background-color'));
					var isDiffColor=$('#'+rowId+ ' .chk').is(':checked');
					
					temp='{['+roles+'],['+text+'],['+rowId+'],['+isDiffColor+'],['+fontcolor+'],['+backgroundcolor+']}';
					if(roles.length>0 && text!='')
					saveData.push(temp);
				
				
				//	alert(temp);

			}

		});
	});
	
//	alert(saveData);
	if(inv==0)
	{
			
		$.post("/AssistPlus/loadAssistTexts", {
			mode : "saveAll",
			//textId : textIdParam,
			//Text : $('#txt' + textIdParam).val(),
			'saveAllText[]' : saveData,
			classid:classid,
			attid:attid,
			rnd: Math.random()
		}, function(responseJson) {
			//addAssistTextRow(textentry);
			notify(responseJson.status,responseJson.message);
			if(responseJson.status == "success")
			{
				
				$('#text-table tr').each(function(){	
					$(this).find('td').each(function(){
							var rowId=$(this).parent('tr').attr('id');
					
							$('#'+rowId+' td').css("background","#FFFFFF");
							});
				});
				
			}
				
			if(responseJson.hasOwnProperty("object"))
			{
				
				for(var j in responseJson.object){
					//alert(responseJson.object[j]);
					$('#'+responseJson.object[j]).css("outline","#FFCC66 solid 1px");
						    	
				}
				
			}
		}, "json");
		
	}
			


}

function saveAssistText(textIdParam) {
	notify("busy","Saving Assist Text ...");
	
	
	var text;
	var coun=0;
	var j=0;
	var saveData=[];
	var rowId=0;
	var temp;
	var croles=[];
	var empty=-1;
	//******** rows background to white
	$('#text-table tr').each(function(){
		coun=0;
		$(this).find('td').each(function(){

			var rowId=$(this).parent('tr').attr('id');
			$('#'+rowId).css("outline","#FFFFFF solid 1px");
		});
	});
	var bool=0;
	var remove=0;
	var roles=[];
	//getting all roles
	$('#text-table tr').each(function(){
		coun=0;
		var allroles = [];
		$(this).find('td').each(function(){

			//do your stuff, you can use $(this) to get current cell
			if (typeof $(this).find('textarea').val() != "undefined") 
			{
				rowId=$(this).parent('tr').attr('id');
				//alert($(this).find('textarea').val());
				text=$(this).find('textarea').val();
				coun=1;
			}
			
			if(coun==1)
			{
				//checking empty rows
				var trowId=rowId;
				trowId=trowId.replace("row","");
				var croles=[];
				$('#roleop' + trowId + ' :selected').each(function(i, selected) {
					croles[i] = $(selected).val();
				});
				var inv=0;
				
				if(text=='' || croles.length<1)
				{
					//removeAssistText(trowId);
					/*
					var rowId=$(this).parent('tr').attr('id');
					$('#'+rowId+' td').css("background","#FFCC66");
					*/
					empty=rowId;
					
					inv=1;
					notify("error","Invalid Entry");
					
					remove=1;
					$('#text-table tr').each(function(){
					
						$(this).find('td').each(function(){

							//do your stuff, you can use $(this) to get current cell
							
								var rowId=$(this).parent('tr').attr('id');
								$('#'+rowId+' td').css("background","#FFFFFF");
								});
					});
					
				}
				if(inv==1){
					notify("error","Invalid Entry");
					
				}
					
				//all roles contains the roles of that row
				rowId=rowId.replace("row","");
				$('#roleop' + rowId + ' :selected').each(function(i, selected) {
					allroles[i] = $(selected).val();
					
					//alert('Row 1 is : '+roles[i]+'  '+text);
				});
				coun=0;	
				j++;
			}
			
		});
		
		
		//getting all rows for the selected row
		roles = [];
		$('#roleop' + textIdParam + ' :selected').each(function(i, selected) {
			roles[i] = $(selected).val();
		});
		
		
		if(textIdParam!= rowId)
		{
			for(var out in roles)
			{
				for(var inr in allroles)
				{	
					
					if(roles[out]==allroles[inr])
					{
						$('#row'+textIdParam).css("outline","#FFCC66 solid 2px");
						$('#row'+rowId).css("outline","#FFCC66 solid 2px");
						notify("error","Same Role in Multiple Rows");
						bool=1;
						
					}
					
						
				}
			}
		}
		
	});
	
	if(empty!= -1)
	{
		
		$('#'+empty).css("outline","red solid 1px");
	}
	
	/* working 
	var roles = [];
	$('#roleop' + textIdParam + ' :selected').each(function(i, selected) {
		roles[i] = $(selected).val();
	});
*/
	if(bool ==0 && remove==0 && empty==-1)
	{
		//alert('in bool');
		$.post("/AssistPlus/loadAssistTexts", {
			mode : "save",
			textId : textIdParam,
			fontcolor:	AssistColorPicker.getHexColor($('#row'+textIdParam).find('.pickerTD div.fontcolor div').css('background-color')),
			backgroundcolor: AssistColorPicker.getHexColor(	$('#row'+textIdParam).find('.pickerTD div.background div').css('background-color')),
			isDiffColor:$('#row'+textIdParam+ ' .chk').is(':checked'),
			assistText : $('#txt' + textIdParam).val(),
			'roles[]' : roles,
			rnd: Math.random()
		}, function(responseJson) {
			//addAssistTextRow(textentry);
			notify(responseJson.status,responseJson.message);
			if(responseJson.status == "success")
				$('#row'+textIdParam+' td').css("background","white");
		}, "json");
	}
	
}


function refreshClassTable() {
	$('#h_classId').val('');
	$('#h_attrId').val('');
	loadClasses();
	truncateTable("attr-table");
	truncateTable("text-table");
}

function refreshAttributeTable() {
	if($('#h_attrId').val()!='')
	{
		$('#h_attrId').val('');
		//loadAttributes($('#h_classId').val());

		$('#'+$('#h_classId').val()+ ' td').click();
		truncateTable("text-table");

	}
			
	
}

function refreshAssistTextTable() {
	if($('#h_classId').val()!='')
		{
	loadAssistTexts($('#h_classId').val(), $('#h_attrId').val());
		}
}

function refreshRolesList() {
	loadRoles();
}

function truncateTable(tblName) {
	var c = document.getElementById(tblName).rows.length;
	for ( var i = c; i > 0; i--)
		document.getElementById(tblName).deleteRow(i - 1);
}




function addAssistTextRow(textentry) {
	
	var $table = $('#text-table');
	var $select = $('<select id="roleop' + textentry.textID
			+ '" multiple="multiple" style="width=100%;">');
	rolelist.push('#roleop'+textentry.textID);//push function will insert values in the list array

	$.each(textentry.roles, function(index1, stroption) {
		//alert($(stroption).val());
		$(stroption).appendTo($select);
	});
	
	/*
	$('<tr id="row'+textentry.textID+'">  .ui-helper-reset.ui-widget.ui-autolist-input.ui-autocomplete-input').change(function(){
		alert('test');
		});
	*/
	//.append($('<td style="width:40%">').append($select))
	
	var $pickerTD=$("<td class='pickerTD' style='width:3%;text-align:right;'> ");
	var $rowCheckBox=$("<td style='width:2%;text-align:right;'> <input class='chk' type='checkbox' /><span style='display: inline; position: absolute; margin-top: 2px;'>Use Different Colors</span></td>");
	
	
	$('<tr id="row'+textentry.textID+'">')
	.appendTo($table)
	.append($('<td style="width:40%">').append($select))
	.append($rowCheckBox)					
	.append($pickerTD)
	.append("<td style='width:8%;text-align:left;'><div style='margin-bottom: 10px;'>Font color</div><div>background color</div> </td>")
	.append(
			$('<td style="width:43%">').append(
					'<textarea id="txt' + textentry.textID
					+ '" class="assisttext" style="width=100%;" >'
					+ textentry.assistText + '</textarea>'))
	.append(
			$('<td style="width:5%;text-align:right;">')
				.append(
						'<a  onClick=saveAssistText('
						+ textentry.textID
						+ ');><img alt="Save" title="Save" src="img/save.png"></a><br><a onClick=removeAssistText('
						+ textentry.textID
						+ ');><img id="rem'+textentry.textID+'" alt="Remove" title="Remove" src="img/remove.png"></a>'));

	$('#roleop'+textentry.textID).autolist();   //Apply autolist format to select
	if(textentry.isDiffColor){
	AssistColorPicker.createPicker($pickerTD,textentry.fontcolor,textentry.background);
	}else{
		AssistColorPicker.createPicker($pickerTD,'#ffffff','#333333');
	}
	$pickerTD.find('.colorSelector')
		.css('float', 'none')
		.css('margin-top','1px')
		.css('width', '25px');
	
	
	
	if(textentry.isDiffColor){
		$rowCheckBox.find('.chk').prop("checked", true);
		$rowCheckBox.find('.chk').next().hide();
	}else{
		$pickerTD.children().hide();
		$pickerTD.next().children().hide();
	}
	
	$rowCheckBox.find('input').change(function() {
		var $this=$(this);
		 if(this.checked) { 
	    	 $this.next().hide();
	    	 $this.parent().next().children().show();
	    	 $this.parent().next().next().children().show();
	     } else{
	    	 $this.next().css('display','inline');
	    	 $this.parent().next().children().hide();
	    	 $this.parent().next().next().children().hide();
	     }
	 }); 

	//alert(textentry.textID);
	/*
	$("tr#row"+textentry.textID).select(function() {
		$("tr#row"+textentry.textID).select();
		});
	*/
	
/*	$(selected).click(function() {
		$("tr#row"+textentry.textID).focusout();
	});
	
	*/
	
	
	//$("tr#row"+textentry.textID).focusout();
	
	//***VALIDATION -- CODE***
/*	var newRole=0;
	$("tr#row"+textentry.textID).focusout(function(){
		//alert($(this).attr("id"));
		//alert('Yesss');
		$('#row'+textentry.textID+' td').css("background","#ffe4e1");
	*/
	
	
	
	$('#roleop'+textentry.textID).change(function(){   
		$('#row'+textentry.textID+' td').css("background","#ffe4e1"); 
		
	});
	$('#txt'+textentry.textID).change(function(){   
		$('#row'+textentry.textID+' td').css("background","#ffe4e1"); 
	});
	
}

function saveRolePriority()
{
	if ($("#assist-roles li").length == 0) 
	{ 
		refreshRolesList();
		setTimeout('notify("error","Atleast one role must be selected for Assist. Last saved preferences loaded.")', 1000);
	}  
	else 
	{			
		notify("busy","Saving Role Preferences...");
		var roles = [];
		$("#assist-roles li").each(function(i, el){
			var $el=$(el);
			var fontcolor=AssistColorPicker.getHexColor($el.find('.fontcolor div').css('background-color'));
			var background=AssistColorPicker.getHexColor($el.find('.background div').css('background-color'));
			roles[i] = ($("#assist-roles li").length - $el.index()) + ":" + $el.text() +":"+$el.attr('id')+":"+  fontcolor+":"+background;        
		});
		$.post("/AssistPlus/loadRoles", {
			'roles[]' : roles,
			rnd: Math.random()
		}, function(responseJson) {
			notify(responseJson.status,responseJson.message);
		}, "json");
	}
}

function saveConfigurations()
{
	notify("busy","Saving Configurations...");
	var configs = [];
	$("#conf-table tr").each(function(i, el){
		if($(el).find(".text").val()==undefined)
			configs[i] = ($(el).eq(0).text()+"="+cne46b($(el).find(".password").val()));
		else
			configs[i] = ($(el).eq(0).text()+"="+$(el).find(".text").val());      
	});
	$.post("/AssistPlus/loadConfig", {
		'configs[]' : configs,
		rnd: Math.random()
	}, function(responseJson) {
		if(responseJson.status=='success')
			notify("success","Configurations Saved Sucessfully");
		else
			notify("error","Invalid configurations");
		//notify(responseJson.status,responseJson.message);
	}, "json");
}

function tblfilter (phrase, tbid, inputid){
	//$('#'+inputid).css("background-image","url(../img/close.png)");
	//$('#'+inputid).css("background","#ffffff url(../img/close.png) no-repeat scroll right top");
	var words = phrase.value.toLowerCase().split(" ");
	var table = document.getElementById(tbid);
	var ele;
	for (var r = 0; r < table.rows.length; r++){
		ele = table.rows[r].innerHTML.replace(/<[^>]+>/g,"");
		var displayStyle = 'none';
		for (var i = 0; i < words.length; i++) {
			if (ele.toLowerCase().indexOf(words[i])>=0)
				displayStyle = '';
			else {
				displayStyle = 'none';
				break;
			}
		}
		table.rows[r].style.display = displayStyle;
	}
}

function lstfilter (phrase, lstid){
	var words = phrase.value.toLowerCase().split(" ");
	var lst = document.getElementById(lstid);
	var lstitems= lst.getElementsByTagName("li");

	var ele;
	for (var r=0; r<lstitems.length; r++)
	{
		ele = lstitems[r].innerHTML.replace(/<[^>]+>/g,"");

		var displayStyle = 'none';
		for (var i = 0; i < words.length; i++) {
			if (ele.toLowerCase().indexOf(words[i])>=0)
				displayStyle = '';
			else {
				displayStyle = 'none';
				break;
			}
		}
		lstitems[r].style.display = displayStyle;
	}
}

function notify(mode, txtMessage)
{
	$(".notifimg").hide();
	$("#notifimg"+mode).show();	
	$('#notif').text(txtMessage);
}

function authenticate()
{
	notify("busy","Authenticating...");

	$.post("/AssistPlus/auth", {
		mode : 'login',
		une : $('#username').val(),
		pwd : $('#password').val(),
		rnd: Math.random()
	}, function(responseJson) {
		if(responseJson.status == "success")
		{				
			$('#h_userId').val(responseJson.object);
			$('#LoginForm').hide();
			$('#top-navigation').show();
			showConfTab();
		}
		$('#password').val('');
		notify(responseJson.status,responseJson.message);
	}, "json");
}

function changePwd()
{
	notify("busy","Changing Password...");

	if( $('#npassword').val() == $('#cnpassword').val() )
	{
		if(($('#npassword').val()==null) || ($('#npassword').val().length==0))
		{
			notify("error","Password cannot be empty");
		}
		else
		{
			$.post("/AssistPlus/auth", {
				mode : 'changepwd',
				uid : $('#h_userId').val(),
				cpwd : $('#cpassword').val(),
				npwd : $('#npassword').val(),
				rnd: Math.random()
			}, function(responseJson) {
				$('#cpassword').val('');
				$('#npassword').val('');
				$('#cnpassword').val('');
				notify(responseJson.status,responseJson.message);

			}, "json");
		}
	}
	else
	{
		notify("error","Passwords do not match");

		$('#npassword').val('');
		$('#cnpassword').val('');
	}
}

function togglevisibility(iname)
{
	if($('#'+iname).is(':hidden'))
		$('#'+iname).show();
	else
		$('#'+iname).hide();
}

function showSection(sname)
{
	$('.TabSection').hide();
	$('#'+sname).show();
}




/***ENC***/
/*
 * A JavaScript implementation of the RSA Data Security, Inc. MD5 Message
 * Digest Algorithm, as defined in RFC 1321.
 * Version 2.2 Copyright (C) Paul Johnston 1999 - 2009
 * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
 * Distributed under the BSD License
 * See http://pajhome.org.uk/crypt/md5 for more info.
 */

/*
 * Configurable variables. You may need to tweak these to be compatible with
 * the server-side, but the defaults work in most cases.
 */
var hexcase = 0;   /* hex output format. 0 - lowercase; 1 - uppercase        */
var b64pad  = "=";  /* base-64 pad character. "=" for strict RFC compliance   */

/*
 * These are the functions you'll usually want to call
 * They take string arguments and return either hex or base-64 encoded strings
 */
function cne46b5dm(s)    { /*B64MD5Enc*/ return rstr2b64(rstr_md5(str2rstr_utf8(s))); }
function cne46b(s){ /*B64Enc*/ if (s=='') return ''; else return rstr2b64(str2rstr_utf8(s)); }

/*
 * Perform a simple self-test to see if the VM is working
 */
/*function md5_vm_test()
	{
	  return hex_md5("abc").toLowerCase() == "900150983cd24fb0d6963f7d28e17f72";
	}*/

/*
 * Calculate the MD5 of a raw string
 */
function rstr_md5(s)
{
	return binl2rstr(binl_md5(rstr2binl(s), s.length * 8));
}

/*
 * Convert a raw string to a base-64 string
 */
function rstr2b64(input)
{
	try { b64pad; } catch(e) { b64pad='='; }
	var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	var output = "";
	var len = input.length;
	for(var i = 0; i < len; i += 3)
	{
		var triplet = (input.charCodeAt(i) << 16)
		| (i + 1 < len ? input.charCodeAt(i+1) << 8 : 0)
		| (i + 2 < len ? input.charCodeAt(i+2)      : 0);
		for(var j = 0; j < 4; j++)
		{
			if(i * 8 + j * 6 > input.length * 8) output += b64pad;
			else output += tab.charAt((triplet >>> 6*(3-j)) & 0x3F);
		}
	}
	return output;
}

/*
 * Encode a string as utf-8.
 * For efficiency, this assumes the input is valid utf-16.
 */
function str2rstr_utf8(input)
{
	var output = "";
	var i = -1;
	var x, y;

	while(++i < input.length)
	{
		/* Decode utf-16 surrogate pairs */
		x = input.charCodeAt(i);
		y = i + 1 < input.length ? input.charCodeAt(i + 1) : 0;
		if(0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF)
		{
			x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
			i++;
		}

		/* Encode output as utf-8 */
		if(x <= 0x7F)
			output += String.fromCharCode(x);
		else if(x <= 0x7FF)
			output += String.fromCharCode(0xC0 | ((x >>> 6 ) & 0x1F),
					0x80 | ( x         & 0x3F));
		else if(x <= 0xFFFF)
			output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F),
					0x80 | ((x >>> 6 ) & 0x3F),
					0x80 | ( x         & 0x3F));
		else if(x <= 0x1FFFFF)
			output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07),
					0x80 | ((x >>> 12) & 0x3F),
					0x80 | ((x >>> 6 ) & 0x3F),
					0x80 | ( x         & 0x3F));
	}
	return output;
}

/*
 * Convert a raw string to an array of little-endian words
 * Characters >255 have their high-byte silently ignored.
 */
function rstr2binl(input)
{
	var output = Array(input.length >> 2);
	for(var i = 0; i < output.length; i++)
		output[i] = 0;
	for(var i = 0; i < input.length * 8; i += 8)
		output[i>>5] |= (input.charCodeAt(i / 8) & 0xFF) << (i%32);
	return output;
}

/*
 * Convert an array of little-endian words to a string
 */
function binl2rstr(input)
{
	var output = "";
	for(var i = 0; i < input.length * 32; i += 8)
		output += String.fromCharCode((input[i>>5] >>> (i % 32)) & 0xFF);
	return output;
}

/*
 * Calculate the MD5 of an array of little-endian words, and a bit length.
 */
function binl_md5(x, len)
{
	/* append padding */
	x[len >> 5] |= 0x80 << ((len) % 32);
	x[(((len + 64) >>> 9) << 4) + 14] = len;

	var a =  1732584193;
	var b = -271733879;
	var c = -1732584194;
	var d =  271733878;

	for(var i = 0; i < x.length; i += 16)
	{
		var olda = a;
		var oldb = b;
		var oldc = c;
		var oldd = d;

		a = md5_ff(a, b, c, d, x[i+ 0], 7 , -680876936);
		d = md5_ff(d, a, b, c, x[i+ 1], 12, -389564586);
		c = md5_ff(c, d, a, b, x[i+ 2], 17,  606105819);
		b = md5_ff(b, c, d, a, x[i+ 3], 22, -1044525330);
		a = md5_ff(a, b, c, d, x[i+ 4], 7 , -176418897);
		d = md5_ff(d, a, b, c, x[i+ 5], 12,  1200080426);
		c = md5_ff(c, d, a, b, x[i+ 6], 17, -1473231341);
		b = md5_ff(b, c, d, a, x[i+ 7], 22, -45705983);
		a = md5_ff(a, b, c, d, x[i+ 8], 7 ,  1770035416);
		d = md5_ff(d, a, b, c, x[i+ 9], 12, -1958414417);
		c = md5_ff(c, d, a, b, x[i+10], 17, -42063);
		b = md5_ff(b, c, d, a, x[i+11], 22, -1990404162);
		a = md5_ff(a, b, c, d, x[i+12], 7 ,  1804603682);
		d = md5_ff(d, a, b, c, x[i+13], 12, -40341101);
		c = md5_ff(c, d, a, b, x[i+14], 17, -1502002290);
		b = md5_ff(b, c, d, a, x[i+15], 22,  1236535329);

		a = md5_gg(a, b, c, d, x[i+ 1], 5 , -165796510);
		d = md5_gg(d, a, b, c, x[i+ 6], 9 , -1069501632);
		c = md5_gg(c, d, a, b, x[i+11], 14,  643717713);
		b = md5_gg(b, c, d, a, x[i+ 0], 20, -373897302);
		a = md5_gg(a, b, c, d, x[i+ 5], 5 , -701558691);
		d = md5_gg(d, a, b, c, x[i+10], 9 ,  38016083);
		c = md5_gg(c, d, a, b, x[i+15], 14, -660478335);
		b = md5_gg(b, c, d, a, x[i+ 4], 20, -405537848);
		a = md5_gg(a, b, c, d, x[i+ 9], 5 ,  568446438);
		d = md5_gg(d, a, b, c, x[i+14], 9 , -1019803690);
		c = md5_gg(c, d, a, b, x[i+ 3], 14, -187363961);
		b = md5_gg(b, c, d, a, x[i+ 8], 20,  1163531501);
		a = md5_gg(a, b, c, d, x[i+13], 5 , -1444681467);
		d = md5_gg(d, a, b, c, x[i+ 2], 9 , -51403784);
		c = md5_gg(c, d, a, b, x[i+ 7], 14,  1735328473);
		b = md5_gg(b, c, d, a, x[i+12], 20, -1926607734);

		a = md5_hh(a, b, c, d, x[i+ 5], 4 , -378558);
		d = md5_hh(d, a, b, c, x[i+ 8], 11, -2022574463);
		c = md5_hh(c, d, a, b, x[i+11], 16,  1839030562);
		b = md5_hh(b, c, d, a, x[i+14], 23, -35309556);
		a = md5_hh(a, b, c, d, x[i+ 1], 4 , -1530992060);
		d = md5_hh(d, a, b, c, x[i+ 4], 11,  1272893353);
		c = md5_hh(c, d, a, b, x[i+ 7], 16, -155497632);
		b = md5_hh(b, c, d, a, x[i+10], 23, -1094730640);
		a = md5_hh(a, b, c, d, x[i+13], 4 ,  681279174);
		d = md5_hh(d, a, b, c, x[i+ 0], 11, -358537222);
		c = md5_hh(c, d, a, b, x[i+ 3], 16, -722521979);
		b = md5_hh(b, c, d, a, x[i+ 6], 23,  76029189);
		a = md5_hh(a, b, c, d, x[i+ 9], 4 , -640364487);
		d = md5_hh(d, a, b, c, x[i+12], 11, -421815835);
		c = md5_hh(c, d, a, b, x[i+15], 16,  530742520);
		b = md5_hh(b, c, d, a, x[i+ 2], 23, -995338651);

		a = md5_ii(a, b, c, d, x[i+ 0], 6 , -198630844);
		d = md5_ii(d, a, b, c, x[i+ 7], 10,  1126891415);
		c = md5_ii(c, d, a, b, x[i+14], 15, -1416354905);
		b = md5_ii(b, c, d, a, x[i+ 5], 21, -57434055);
		a = md5_ii(a, b, c, d, x[i+12], 6 ,  1700485571);
		d = md5_ii(d, a, b, c, x[i+ 3], 10, -1894986606);
		c = md5_ii(c, d, a, b, x[i+10], 15, -1051523);
		b = md5_ii(b, c, d, a, x[i+ 1], 21, -2054922799);
		a = md5_ii(a, b, c, d, x[i+ 8], 6 ,  1873313359);
		d = md5_ii(d, a, b, c, x[i+15], 10, -30611744);
		c = md5_ii(c, d, a, b, x[i+ 6], 15, -1560198380);
		b = md5_ii(b, c, d, a, x[i+13], 21,  1309151649);
		a = md5_ii(a, b, c, d, x[i+ 4], 6 , -145523070);
		d = md5_ii(d, a, b, c, x[i+11], 10, -1120210379);
		c = md5_ii(c, d, a, b, x[i+ 2], 15,  718787259);
		b = md5_ii(b, c, d, a, x[i+ 9], 21, -343485551);

		a = safe_add(a, olda);
		b = safe_add(b, oldb);
		c = safe_add(c, oldc);
		d = safe_add(d, oldd);
	}
	return Array(a, b, c, d);
}

/*
 * These functions implement the four basic operations the algorithm uses.
 */
function md5_cmn(q, a, b, x, s, t)
{
	return safe_add(bit_rol(safe_add(safe_add(a, q), safe_add(x, t)), s),b);
}
function md5_ff(a, b, c, d, x, s, t)
{
	return md5_cmn((b & c) | ((~b) & d), a, b, x, s, t);
}
function md5_gg(a, b, c, d, x, s, t)
{
	return md5_cmn((b & d) | (c & (~d)), a, b, x, s, t);
}
function md5_hh(a, b, c, d, x, s, t)
{
	return md5_cmn(b ^ c ^ d, a, b, x, s, t);
}
function md5_ii(a, b, c, d, x, s, t)
{
	return md5_cmn(c ^ (b | (~d)), a, b, x, s, t);
}

/*
 * Add integers, wrapping at 2^32. This uses 16-bit operations internally
 * to work around bugs in some JS interpreters.
 */
function safe_add(x, y)
{
	var lsw = (x & 0xFFFF) + (y & 0xFFFF);
	var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
	return (msw << 16) | (lsw & 0xFFFF);
}

/*
 * Bitwise rotate a 32-bit number to the left.
 */
function bit_rol(num, cnt)
{
	return (num << cnt) | (num >>> (32 - cnt));
}

var keyStr = "ABCDEFGHIJKLMNOP" +
"QRSTUVWXYZabcdef" +
"ghijklmnopqrstuv" +
"wxyz0123456789+/" +
"=";
/*
	  function cne46b(input) {
	     input = escape(input);
	     var output = "";
	     var chr1, chr2, chr3 = "";
	     var enc1, enc2, enc3, enc4 = "";
	     var i = 0;

	     do {
	        chr1 = input.charCodeAt(i++);
	        chr2 = input.charCodeAt(i++);
	        chr3 = input.charCodeAt(i++);

	        enc1 = chr1 >> 2;
	        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
	        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
	        enc4 = chr3 & 63;

	        if (isNaN(chr2)) {
	           enc3 = enc4 = 64;
	        } else if (isNaN(chr3)) {
	           enc4 = 64;
	        }

	        output = output +
	           keyStr.charAt(enc1) +
	           keyStr.charAt(enc2) +
	           keyStr.charAt(enc3) +
	           keyStr.charAt(enc4);
	        chr1 = chr2 = chr3 = "";
	        enc1 = enc2 = enc3 = enc4 = "";
	     } while (i < input.length);

	     return output;
	  }
 */
function ced46b(input) {
	var output = "";
	var chr1, chr2, chr3 = "";
	var enc1, enc2, enc3, enc4 = "";
	var i = 0;

	// remove all characters that are not A-Z, a-z, 0-9, +, /, or =
	//var base64test = /[^A-Za-z0-9\+\/\=]/g;
	/*if (base64test.exec(input)) {
	        alert("There were invalid base64 characters in the input text.\n" +
	              "Valid base64 characters are A-Z, a-z, 0-9, '+', '/',and '='\n" +
	              "Expect errors in decoding.");
	     }*/
	input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

	do {
		enc1 = keyStr.indexOf(input.charAt(i++));
		enc2 = keyStr.indexOf(input.charAt(i++));
		enc3 = keyStr.indexOf(input.charAt(i++));
		enc4 = keyStr.indexOf(input.charAt(i++));

		chr1 = (enc1 << 2) | (enc2 >> 4);
		chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
		chr3 = ((enc3 & 3) << 6) | enc4;

		output = output + String.fromCharCode(chr1);

		if (enc3 != 64) {
			output = output + String.fromCharCode(chr2);
		}
		if (enc4 != 64) {
			output = output + String.fromCharCode(chr3);
		}

		chr1 = chr2 = chr3 = "";
		enc1 = enc2 = enc3 = enc4 = "";

	} while (i < input.length);

	return unescape(output);
}

var AssistColorPicker=(function(){
	
	function createOptionList(colorClass){
		var colorList="#ffffff,#333333,#330000,#666666,#cccccc,#333366,#666699,#99ccff,#330099,#3333cc,#6633cc,#000066,#006633,#33ff66,#ffff00,#ffcc00,#ff9900,#cc3300,#cc0000,#990033".split(',');
		var html="<span class='colorSelector "+colorClass+"'><select>";
		var optionList="";
			for(var i=0;i<colorList.length;i++)
			{
				optionList+="<option id="+colorList[i].replace('#','')+" value="+colorList[i].replace('#','')+" >"+colorList[i]+"</option>";
			}
			return html=html+optionList+"</select></span>";
	}
	
	function rgb2hex(rgb){
		if(rgb.indexOf('rgb')==0){
			rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);  
			return "#" +   ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) +   ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) +   ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
		}else{
			return rgb;
		}
	}
	
	function createColorPicker($target,fontColor,backgroundColor){
		if($target.find('.colorSelector').length==0){
			var $fontPickerDiv=$(createOptionList("fontcolor"));
			var $backPickerDiv=$(createOptionList("background"));
			$fontPickerDiv.find('select option#'+fontColor).attr('selected','selected');
			$backPickerDiv.find('select option#'+backgroundColor).attr('selected','selected');
			$fontPickerDiv.find('select').colourPicker({defaultColor:fontColor});
			$backPickerDiv.find('select').colourPicker({defaultColor:backgroundColor});
			$target.append($fontPickerDiv);
			$target.append($backPickerDiv);
		}
	}
	
	return{
		getHexColor:rgb2hex,
		createPicker:createColorPicker
	};

})();