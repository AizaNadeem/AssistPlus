if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.Settings=(function() {
	var fontColor = '';
	var backgroundColor = '';
	
	function loadSettings($contentboxcontent) {
		if($("#ImportLicLnk")) {
			$("#ImportLicLnk").hide();
		}
		
		PLMFlex.Assist.Request.get("loadConfig?", function(jsonResponse) {
			var User = jsonResponse.AgileUser;
			var Password = jsonResponse.AgilePassword;
			var URL = jsonResponse.AgileURL;
			var primaryURL = jsonResponse.PrimaryInstanceURL;
			var accessType = jsonResponse.accessType;
			var hoverColor = jsonResponse.hoverColor;
			var textDuration = jsonResponse.textDuration;
			var indicateNewText = jsonResponse.indicateNewText;
			var enableOptOut = jsonResponse.enableOptOut;
			fontColor = jsonResponse.fontColor;
			backgroundColor = jsonResponse.backgroundColor;
			
			var html = PLMFlex.Assist.HTML.getConfigHTML;
			var $html = $(html);
			
			$html.find('#enableOptOut input:radio[value=' + enableOptOut.value.toString() + ']').prop('checked', true);
			
			if(indicateNewText.value.toString() == "Yes") {
				$html.find('#durationBox').show();
				var $isNew = $html.find('#indicateNewText input:radio[value='+indicateNewText.value.toString()+']');
				$isNew.prop('checked', true);
			} else {
				$html.find('#durationBox').hide();
				var $isNew = $html.find('#indicateNewText input:radio[value='+indicateNewText.value.toString()+']');
				$isNew.prop('checked',true);
			}
			
			if(accessType && accessType.value=='usergroups') {
				accessTypeRole = false;
				updateLabels();
			}
			
			var $checkedDuration=$html.find('input:radio[value='+textDuration.value.toString()+']');
			$checkedDuration.prop('checked',true);			
			
			var $NodeTypeMaster=$html.find('#NodeTypeMaster');
			var $NodeTypeSlave=$html.find('#NodeTypeSlave');
			$NodeTypeMaster.change(function() {
				if($NodeTypeMaster.is(':checked')) {
			        $("#PrimaryInstanceURL").attr("disabled", 'disabled');
			        $("#PrimaryInstanceURL").hide();
			        $("#primaryInstanceLabel").hide();
			    } else {
					$("#PrimaryInstanceURL").attr("disabled", null);
					$("#PrimaryInstanceURL").show();
					$("#primaryInstanceLabel").show();
					if($("#PrimaryInstanceURL").val() == '') {
						$("#PrimaryInstanceURL").val('http://<AgileServer>:<AgilePort>/AssistPlus');
					}
				}
			});
			$NodeTypeSlave.change(function() {
				if($NodeTypeSlave.is(':checked')) {
					$("#PrimaryInstanceURL").attr("disabled", null);
					$("#PrimaryInstanceURL").show();
					$("#primaryInstanceLabel").show();
					if($("#PrimaryInstanceURL").val() == '') {
						$("#PrimaryInstanceURL").val('http://<AgileServer>:<AgilePort>/AssistPlus');
					}
			    } else {
			        $("#PrimaryInstanceURL").attr("disabled", 'disabled');
			        $("#PrimaryInstanceURL").hide();
			        $("#primaryInstanceLabel").hide();
				}
			});
			
			var $showNew=$html.find('#showNew');
			var $disableNew=$html.find('#disableNew');
			$showNew.change(function() {
				if($showNew.is(':checked')) {
					$html.find('#durationBox').show();
			    } else {
					$html.find('#durationBox').hide();
				}
			});
			$disableNew.change(function() {
				if($disableNew.is(':checked')) {
					$html.find('#durationBox').hide();
			    } else {
					$html.find('#durationBox').show();
				}
			});
			
			$html.find('#'+User.id).val(User.value);
			$html.find('#'+Password.id).val(PLMFlex.Assist.Security.Base64ToText(Password.value));
			$html.find('#'+URL.id).val(URL.value);
			if(primaryURL) {
				$html.find('#'+primaryURL.id).val(primaryURL.value);
			}
			
			if(primaryURL.value && primaryURL.value != "") {
				$NodeTypeSlave.prop('checked', true);
				$html.find("#PrimaryInstanceURL").show();
				$html.find("#primaryInstanceLabel").show();
				$html.find("#PrimaryInstanceURL").attr("disabled", null);
				$("#roles").hide();
				$("#assistTextLi").hide();
				$("#data-import-export").hide();
			} else {
				$NodeTypeMaster.prop('checked', true);
				$html.find("#PrimaryInstanceURL").hide();
				$html.find("#primaryInstanceLabel").hide();
				$("#roles").show();
				$("#assistTextLi").show();
				$("#data-import-export").show();
			}
			
			if(hoverColor.value.toString()=="#570303" || hoverColor.value.toString()=="#17365d" )
				AssistColorPicker.createBackgroundColorPicker($html.find("#hoverColor"),'#ffffff',hoverColor.value.toString());
			else
				AssistColorPicker.createBackgroundColorPicker($html.find("#hoverColor"),'#333333',hoverColor.value.toString());
 			
			AssistColorPicker.createPicker($html.find("#colorPalette"),fontColor.value.toString(),backgroundColor.value.toString(),true,false);
			$html.find("#colorPalette").find('.picker').html("Default Color");	
			$("#colorPalette").find('.picker').each(function() {
				$(this).css('border',"0px")
					.css('padding',"1px")
					.css('margin-top',"1px")
					.css('width',"110px")
					.css('background-color',"")
					.text("Default Color")
					.html("Default Color");
			});
			$html.find('.colorSelector ').each(function() {
				$(this).css('padding',"0px")
					.css('margin',"0px");
			});

			$contentboxcontent.html($html);
			var el = $("input").get(0);
			var elemLen = el.value.length;
			el.selectionStart = elemLen;
			el.selectionEnd = elemLen;
			el.focus();

			$("input[type=text]").on('focus', function() {
				$(this).addClass("focused");
			}).on('blur', function() {
				$(this).removeClass("focused");			
			});
			
			$("input[type=password]").on('focus', function() {
				$(this).addClass("focused");
			}).on('blur', function() {
				$(this).removeClass("focused");
			});
			 
			$("input[type=radio]").on('focus', function() {	
				$(this).parents(".box").addClass("focused");	  
			}).on('blur', function() {
				$(this).parents(".box").removeClass("focused");		
			});
			 
			$("button").on('focus', function() {
				$(this).addClass("focused");
			}).on('blur', function() {
				$(this).removeClass("focused");
			});
			
			$contentboxcontent.attr("id", "");
			$contentboxcontent.outerHeight($(window).height()-110);
			var $button = $html.find('.button.saveConfig');
			PLMFlex.Assist.BindEvents.saveConfig($button);
			$('.saveRoles').remove();
		});
	}
	
	function updateLabels() {
		$("#roles").text("User Group Priority");
	}
	
	function endsWith(str, suffix) {
	    return str.indexOf(suffix, str.length - suffix.length) !== -1;
	}
	
	function saveSettings($subcontent) {
		var configs = [];
		var selectVal = "";
		if ($("#NodeTypeMaster").is(':checked')) {
			selectVal = 'master';
		}
		
		var primaryInstUrl = $("#PrimaryInstanceURL");
		var agileUrl = $("#AgileURL").val();

		if(agileUrl == "" ) {
			PLMFlex.Assist.BindEvents.Notify("Please provide Agile Server URL", "error");
			return;
		}
		
		var agileServerOnly = agileUrl.substring(0, agileUrl.indexOf('/Agile'));
		
		if(selectVal=='master') {
			primaryInstUrl.val("");
		} else {
			var primaryNodeUrl=primaryInstUrl.val();
			
			if(!primaryNodeUrl || primaryNodeUrl == 'http://<AgileServer>:<AgilePort>/AssistPlus'
				|| 	primaryNodeUrl.indexOf('AgileServer')!= -1 || primaryNodeUrl.indexOf('AgilePort') != -1) {
				PLMFlex.Assist.BindEvents.Notify("Please provide AssistPlus Master node URL", "error");
				return;
			}
			
			if(endsWith(primaryNodeUrl, "/")) {
				primaryNodeUrl = primaryNodeUrl.substring(0, primaryNodeUrl.length-1);
			}
			
			if(!endsWith(primaryNodeUrl, "/AssistPlus")) {
				primaryInstUrl.val(primaryNodeUrl+"/AssistPlus");
			} else {
				primaryInstUrl.val(primaryNodeUrl);
			}
			
			if(primaryInstUrl.val().indexOf(agileServerOnly)!= -1) {
				PLMFlex.Assist.BindEvents.Notify("A Master node cannot be its own Slave.", "error");
				return;
			}
		}
		
		var primaryURL = primaryInstUrl.val();
		if(primaryURL) {
			$("#roles").hide();
			$("#assistTextLi").hide();
			$("#data-import-export").hide();
		} else {
			$("#roles").show();
			$("#assistTextLi").show();
			$("#data-import-export").show();
		}
		
		configs[0] = "hoverColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#hoverColor .colorSelector button').css('background-color'));
		configs[1] = "fontColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#colorPalette .colorSelector button').css('color'));
		configs[2] = "backgroundColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#colorPalette .colorSelector button').css('background-color'));
		
		configs[3] = "textDuration" + "=" + $("input:radio[name='duration']:checked").val();
		configs[4] = "indicateNewText" + "=" + $("input:radio[name='shownew']:checked").val();
		configs[5] = "enableOptOut" + "=" + $("input:radio[name='enableOptOut']:checked").val();

		if(selectVal == 'master') {
			var dataIndex = 6;
			$subcontent.find('input').each(function(i, el) {
				if($(this).attr('type') != 'checkbox') {
					var key = $(this).data('id');
					var value = $(this).val();
					if($(this).attr('type') == 'password') {
						value = PLMFlex.Assist.Security.Base64(value);
					}
					if(key) {
						configs[dataIndex] = key + "=" + value;
						dataIndex++;
					}
				}
			});
			
			PLMFlex.Assist.Request.post("loadConfig?", {'configs[]': configs,rnd: Math.random()}, function(jsonResponse) {
				if(jsonResponse && jsonResponse.status == 'success') {
					if(accessType != undefined) {
						if(accessType == 'roles') {
							$("#roles").text("Role Priority");
							accessTypeRole = true;
						} else {
							$("#roles").text("User Group Priority");
							accessTypeRole = false;
						}
					}
				}
			});
		} else {
			PLMFlex.Assist.Request.get("loadConfig?masterServerUrl=" + primaryURL, function(jsonResponse) {
				var primaryURL = jsonResponse.PrimaryInstanceURL;
				if(primaryURL) {
					if(primaryURL.value != "") {
						PLMFlex.Assist.BindEvents.Notify("The provided Master node itself is a Slave, cannot proceed.", "error");
					} else {
						var dataIndex = 0;
						$subcontent.find('input').each(function(i, el) {
							if($(this).attr('type') != 'checkbox') {
								var key = $(this).data('id');
								var value = $(this).val();
								if($(this).attr('type') == 'password') {
									value=PLMFlex.Assist.Security.Base64(value);
								}
								if(key) {
									configs[dataIndex] = key + "=" + value;
									dataIndex++;
								}
							}
						});
						
						PLMFlex.Assist.Request.post("loadConfig?", {'configs[]': configs, rnd: Math.random()}, function(jsonResponse) {
							if(jsonResponse && jsonResponse.status == 'success') {
								if(accessType == 'roles') {
									$("#roles").text("Role Priority");
									accessTypeRole = true;
								} else {
									$("#roles").text("User Group Priority");
									accessTypeRole = false;
								}
							}
						});
					}
				}
			});
		}	
	}
	
	function saveRoles()
	{
		if ($("#assist-roles li").length == 0) 
		{ 
			if(accessTypeRole)
			{
				PLMFlex.Assist.BindEvents.Notify("Please Add a Role to save","attention");
			}
			else
			{
				PLMFlex.Assist.BindEvents.Notify("Please Add a User Group to save","attention");
			}
		}  
		else 
		{			
			var roles = [];
			var rolesToSave=[];
			
			$("#assist-roles li").each(function(i, el){
				var $el=$(el);
				var fontcolor=AssistColorPicker.getHexColor($el.find('.colorSelector button').css('color'));
				var background=AssistColorPicker.getHexColor($el.find('.colorSelector button').css('background-color'));
				
				if(fontcolor==fontColor.value.toString() && background==backgroundColor.value.toString())
				{
					fontcolor=' ';
					background=' ';
				}
				roles[i] = ($("#assist-roles li").length - $el.index()) + "<ri>" + $.trim($el.find('.text').text()) +"<ri>"+$el.attr('id')+"<ri>"+  fontcolor+"<ri>"+background;
				rolesToSave[i]=$.trim($el.find('.text').text());
			});
			
			var sorted_arr = rolesToSave.sort(); 			
			var duplicate=false;
			
			for (var i = 0; i < rolesToSave.length - 1 && !duplicate; i++) 
			{
				if (sorted_arr[i + 1] == sorted_arr[i]) 
				{
					duplicate=true;
					PLMFlex.Assist.BindEvents.Notify("Can Not Save Duplicate Values","attention");
					return;
				}
			}		
			var diff = $(initialRoleList).not(rolesToSave).get();
			if(diff.length==0)
			{
				 PLMFlex.Assist.Request.post("loadRoles",
							{'roles[]' : roles,
						rnd: Math.random()
						},function(jsonResponse){});
				 initialRoleList=rolesToSave;
			}
			else
			{
				var choice=accessTypeRole?"Role(s)":"Group(s)" ;
				var strConfirm="Removing " +choice+ " priority may result in assist text removal. Do you want to continue?";
				$( "#dialog-confirm-priority" ).html(strConfirm);

				$( "#dialog-confirm-priority" ).dialog({
					resizable: false,
					height:145,
					modal: true,
					buttons: {
						"Ok": function() {
							$( this ).dialog( "close" );
							PLMFlex.Assist.Request.post("loadRoles",
									{'roles[]' : roles,
									'flag1' : "remove",
								rnd: Math.random()
									},function(jsonResponse){});
							initialRoleList=rolesToSave;

						},
						Cancel: function() {
							$( this ).dialog( "close" );
						}
					}
				});
			}
			
		}
	}
	
	var initialRoleList;
	
	function loadRoles($containerHeader,$contentboxcontent){
		initialRoleList=new Array;
		var rolesContainer=PLMFlex.Assist.HTML.RoleHTML.rolesHTML((accessTypeRole?"Role(s)":"Group(s)"));
		
		var $rolesContainer=$(rolesContainer);
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".assistRoleFilter","#assist-roles li",".roleText .text");
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".agileRoleFilter","#agile-roles li","");
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".classFilter","#class-table tr.parent","td:first-child a");
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".attFilter","#attr-attribute tr","td:first-child a");
		$contentboxcontent.html($rolesContainer);
		$contentboxcontent.css('padding','0');
		$contentboxcontent.attr("id","loadRolesContent");
		$contentboxcontent.find(".settingsFilter").height('30px');
		
		$contentboxcontent.outerHeight($(window).height()-147);
		
		PLMFlex.Assist.Request.get("loadConfig?",function(jsonResponse)
				{
						fontColor=jsonResponse.fontColor;
						backgroundColor=jsonResponse.backgroundColor;
				});
		
		var $saveRoles=$("<button id='saveRolesButton'  class='button saveRoles buttonStyle'>"+(accessTypeRole?"Save Roles":"Save User Groups")+"</button>");
		$contentboxcontent.parents('#main-content').find('.saveRoles').remove();
		$saveRoles.insertAfter( $contentboxcontent.parents('.content-box'));
		PLMFlex.Assist.BindEvents.saveRoles($saveRoles);
		
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Agile/Assist"+(accessTypeRole?" Roles":"Group(s)"),"attention");
		var $agileRoles=$rolesContainer.find('#agile-roles');
		var $assistRoles=$rolesContainer.find('#assist-roles');
		
		var height=$('.content-box-content').height();
		var width=$('.content-box-content').width();
		width=width/2;
		width=width-69;
		height=height/4;
		
		
		$('.rolesDiv').append('<div class="loadingDiv" ><img src="img/loading1.gif" class="waiting" /></div> ');
		$(".waiting").css("margin-left",width+"px");
		$(".waiting").css("margin-top",height+"px");
		
		PLMFlex.Assist.Request.get("loadRoles?",function(jsonResponse){
			$('.rolesDiv').remove('.loadingDiv');
			if(jsonResponse.hasOwnProperty("object")){
			$.each(jsonResponse.object,function(index, roleentry) 
			{ 
				
				// Iterate over the JSON array.
						if (roleentry.Priority < 0) {
							var $list1= $('<LI />').attr({id:(roleentry.RoleID)}).addClass("ui-state-default")
							.html("<div class='roleText' >" +
							"<span class='text textExtend'></span>" +
							"</div>");
							$list1.find(".text").text(roleentry.Role);
							$list1.appendTo($agileRoles);
						} else {
							
							var $list = $('<LI />').attr({id:(roleentry.RoleID)}).addClass("ui-state-default")
													.html("<div class='roleText' >" +
													"<span class='text textExtend'></span>" +
													"</div>");
							$list.find(".text").text(roleentry.Role);
							$list.appendTo($assistRoles);
							
							if(roleentry.fontcolor==' ' && roleentry.background==' ')
							{
								AssistColorPicker.createPicker($list.find('.roleText'),fontColor.value.toString(),backgroundColor.value.toString(),false,false);
								$list.find('.picker').html("Default Color");
							}
							else
							{
								AssistColorPicker.createPicker($list.find('.roleText'),roleentry.fontcolor,roleentry.background,false,false);
								$list.find('.picker').html("Text Color");
							}
							initialRoleList.push(roleentry.Role);
							
								
						}
					});
			
			}
			/*$assistRoles.find('.colorSelector').each(function()
			{
				if($.browser.msie && $.browser.version == "7.0")
				{
					$(this).css("margin-right","19px");
				}
			});*/
			
					
			$rolesContainer=$('.rolesContainer');
			setHeights($rolesContainer,$contentboxcontent,$containerHeader);
			var lists=$( "#assist-roles, #agile-roles" ).sortable({
				connectWith: ".connectedSortable",
				placeholder: "sortablePlaceholder",
				forcePlaceholderSize: true,
				remove:function(event, ui)
	            {
	            	$("#assist-roles").sortable( "option", "scroll", true);
            		$("#agile-roles").sortable( "option", "scroll", true);
            		
            	},
	            deactivate:function(event, ui)
	            {
	            	$("#assist-roles").sortable( "option", "scroll", true);
            		$("#agile-roles").sortable( "option", "scroll", true);
            		
	            },
	            over:function(event, ui)
	            {
	            	var id=$(this).attr("id");
	            	
	            	if(id == "assist-roles")
	            	{
	            		
	            		$("#assist-roles").sortable( "option", "scroll", true);
	            		$("#agile-roles").sortable( "option", "scroll", false);
	            	}
	            	else
	            	{
	            		$("#assist-roles").sortable( "option", "scroll", false);
	            		$("#agile-roles").sortable( "option", "scroll", true);
	            	}
	            },
	            start:function()
	            {
	            	
	            },
				stop: stopped
			});
			lists.disableSelection();
			
			 
		});
		
		$("#selectAll").click(function()
		{
			$('#agile-roles').find('li').each(function()
			{
				var $row=$(this);
				var dislplay=$row.css('display');
				if(dislplay!="none")
				{
					var $list = $('<LI />').attr({id:($row.attr("id"))}).addClass("ui-state-default")
					.html("<div class='roleText' >" +
							"<span class='text textExtend'></span>" +
					"</div>");
					$list.find(".text").text($row.text());
					$list.appendTo('#assist-roles');
					AssistColorPicker.createPicker($list.find('.roleText'),fontColor.value.toString(),backgroundColor.value.toString(),false,false);
					$list.find('.picker').html("Default Color");
					$row.remove();
				}

			});	
		
			$rolesContainer=$('.rolesContainer');
			setHeights($rolesContainer,$('.content-box-content'),$('.content-box-header'));

		});
		
		$("#removeAll").click(function()
		{
			$('#assist-roles').find('li').each(function()
			{
				var $row=$(this);
				var dislplay=$row.css('display');
				if(dislplay!="none")
				{
					var $list = $('<LI />').attr({id:($row.attr("id"))}).addClass("ui-state-default").html("<div class='roleText' >" +
						"<span class='text textExtend'></span>" +
						"</div>");
					var text=$row.text().replace('Text Color','');
					text=text.replace('Default Color','');
					$list.find(".text").text(text);
					$list.appendTo('#agile-roles');
					$row.remove();
				}
			});
			
			$rolesContainer=$('.rolesContainer');
			setHeights($rolesContainer,$('.content-box-content'),$('.content-box-header'));

		});
		
		$(window).resize(function(){		
			setHeights($rolesContainer,$contentboxcontent,$containerHeader);	
		});
		
	}
	
	function setHeights($rolesContainer,$contentboxcontent,$containerHeader){
		$rolesContainer.eq(0).height($contentboxcontent.height()-30);
		$rolesContainer.eq(1).height($contentboxcontent.height()-30);				
		var spanHeight=$(".rolesContainer").height();
		$( "#agile-roles").css('min-height',spanHeight+"px");		
		$( "#assist-roles").css('min-height',spanHeight+"px");	

	}
	
	function stopped(event,ui){
		
		if(ui.item.parents('ul#assist-roles').length > 0){
			if(ui.item.find('.colorSelector').length==0){
				var roleText= ui.item.text();
				ui.item.html("<div class='roleText' >" +
						"<span id='spanRoleText' class='text textExtend'>" +
						"</span></div>");
				ui.item.find("#spanRoleText").text(roleText);
		
				AssistColorPicker.createPicker(ui.item.find('.roleText'),fontColor.value.toString(),backgroundColor.value.toString(),false,false);
				ui.item.find('.roleText').find('.picker').html("Default Color");
			   /* if($.browser.msie && $.browser.version == "7.0")
				{
					ui.item.find(".colorSelector").css("margin-right","17px");
				}*/
			}
		}else{
				if(ui.item.length>0)
				{
					if(ui.item.find('.colorSelector').length>0){
						var $textDiv=ui.item.find('.roleText span.text');
						var $roleText=$textDiv.text();
						
						ui.item.html("<div class='roleText' >" +
								"<span class='text textExtend'></span>" +
						"</div>");
						ui.item.find(".text").text($roleText);
					;
					}
				}
		}
		var $contentboxcontent=$('.content-box-content');
		var $contentboxheader=$('.content-box-header');
		var $rolesContainer=$('.rolesContainer');
		
		setHeights($rolesContainer,$contentboxcontent,$contentboxheader);
	}
	
	function loadXlsxImport($contentboxcontent) {
		if($("#ImportLicLnk")) {
			$("#ImportLicLnk").hide();
		}
		
		var html = PLMFlex.Assist.HTML.xlsxImportHTML;
		var $html = $(html);
		$contentboxcontent.html($html);
		
		var userAgent = navigator.userAgent.toUpperCase();
		$html.find('#exportExcel').hide();
		
		var $importAction=$html.find('#importAction');
		var $exportAction=$html.find('#exportAction');
		var $TypeOverWrite=$html.find('#TypeOverWrite');
		
		if (userAgent.indexOf('MSIE') >= 0) {
			$html.find("#chooseFile").hide();
			$html.find("#path").hide();
			$html.find("#importPath").show();
		} else {
			$html.find("#chooseFile").show();
			$html.find("#importPath").hide();
		}
		$html.find("#importType").show();
		$html.find("#fileName").show();
		$html.find("#b-import-xlsx").show();
		$html.find(".b-export-xlsx").hide();
		$html.find("#exportBox").hide();
		$html.find("#download").hide();
		 
		$importAction.prop('checked',true);
		$TypeOverWrite.prop('checked',true);
		
		$importAction.change(function(){
			
			var val=$(this).val();
			if($importAction.is(':checked'))
			{
				var userAgent = navigator.userAgent.toUpperCase();
				
				if (userAgent.indexOf('MSIE') >= 0) {
					
					$html.find("#chooseFile").hide();
					$html.find("#path").hide();
					$html.find("#importPath").show();
				}
				else
				{
					$html.find("#chooseFile").show();
					$html.find("#path").show();
					$html.find("#importPath").hide();
				}
				$html.find("#radio").show();
				$html.find("#upperBox").show();
				$html.find("#lowerBox").show();
				$html.find("#importType").show();
				$html.find("#fileName").show();
				$html.find("#b-import-xlsx").show();
				$html.find(".b-export-xlsx").hide();
				$html.find("#exportBox").hide();
				$html.find("#download").hide();
						
		    }
			else
			{
				$html.find(".b-export-xlsx").show();
				$html.find("#exportBox").show();
				$html.find("#download").hide();
				$html.find("#chooseFile").hide();
				$html.find("#path").hide();
				$html.find("#importPath").hide();
				$html.find("#importType").hide();
				$html.find("#radio").hide();
				$html.find("#upperBox").hide();
				$html.find("#lowerBox").hide();
				$html.find("#fileName").hide();
				$html.find("#b-import-xlsx").hide();
				
			}
		});
		
		$exportAction.change(function(){
			var val=$(this).val();
			if($exportAction.is(':checked'))
			{
				$html.find(".b-export-xlsx").show();
				$html.find("#exportBox").show();
				$html.find("#download").hide();
				$html.find("#importPath").hide();
				$html.find("#upperBox").hide();
				$html.find("#lowerBox").hide();
				$html.find("#radio").hide();
				$html.find("#chooseFile").hide();
				$html.find("#path").hide();
				$html.find("#importPath").hide();
				$html.find("#importType").hide();
				$html.find("#fileName").hide();
				$html.find("#b-import-xlsx").hide();
			
		    }
			else
			{
				$html.find("#exportBox").hide();
				var userAgent = navigator.userAgent.toUpperCase();
				
				if (userAgent.indexOf('MSIE') >= 0) {
				
					$html.find("#chooseFile").hide();
					$html.find("#path").hide();
					$html.find("#importPath").show();
				}
				else
				{
					$html.find("#chooseFile").show();
					$html.find("#path").show();
					$html.find("#importPath").hide();
				}
				$html.find("#radio").show();
				$html.find("#upperBox").show();
				$html.find("#lowerBox").show();
				$html.find("#importType").show();
				$html.find("#fileName").show();
				$html.find("#b-import-xlsx").show();
				$html.find(".b-export-xlsx").hide();
				$html.find("#download").hide();
			}
		});
		
		
		$('input:radio[name=importType]:nth(0)').attr('checked', true);
		 
		 $("input[type=radio]").on('focus', function() {
			   $(this).parents(".box").addClass("focused");		  
			  }).on('blur', function() {
				  $(this).parents(".box").removeClass("focused");			
			  });
		 
		 $("button").on('focus', function() {
			   $(this).addClass("focused");
			  }).on('blur', function() {
			       $(this).removeClass("focused");	
			  });
		 
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height() - 110);
		
		PLMFlex.Assist.BindEvents.importXlsx();
	}
	
	function loadImport($contentboxcontent)
	{
		if($("#ImportLicLnk"))
		{
			$("#ImportLicLnk").hide();
		}
		
		var html=PLMFlex.Assist.HTML.getImportDatabaseHTML;
		var $html=$(html);
				
		$html.find("#exportXML").hide();
		
		var $importAction=$html.find('#importAction');
		var $exportAction=$html.find('#exportAction');
		var $TypeOverWrite=$html.find('#TypeOverWrite');
		
		var userAgent = navigator.userAgent.toUpperCase();
		
		if (userAgent.indexOf('MSIE') >= 0) {
			$html.find("#chooseFile").hide();
			$html.find("#path").hide();
			$html.find("#importPath").show();
		}
		else
		{
			$html.find("#chooseFile").show();
			$html.find("#importPath").hide();
		}
		$html.find("#importType").show();
		$html.find("#fileName").show();
		$html.find(".importDB").show();
		$html.find(".exportDB").hide();
		$html.find("#exportBox").hide();
		$html.find("#download").hide();
		 
		$importAction.prop('checked',true);
		$TypeOverWrite.prop('checked',true);
		
		$importAction.change(function(){
			
			var val=$(this).val();
			if($importAction.is(':checked'))
			{
				var userAgent = navigator.userAgent.toUpperCase();
				
				if (userAgent.indexOf('MSIE') >= 0) {
					
					$html.find("#chooseFile").hide();
					$html.find("#path").hide();
					$html.find("#importPath").show();
				}
				else
				{
					$html.find("#chooseFile").show();
					$html.find("#path").show();
					$html.find("#importPath").hide();
				}
				$html.find("#radio").show();
				$html.find("#upperBox").show();
				$html.find("#lowerBox").show();
				$html.find("#importType").show();
				$html.find("#fileName").show();
				$html.find(".importDB").show();
				$html.find(".exportDB").hide();
				$html.find("#exportBox").hide();
				$html.find("#download").hide();
						
		    }
			else
			{
				$html.find(".exportDB").show();
				$html.find("#exportBox").show();
				$html.find("#download").hide();
				$html.find("#chooseFile").hide();
				$html.find("#path").hide();
				$html.find("#importPath").hide();
				$html.find("#importType").hide();
				$html.find("#radio").hide();
				$html.find("#upperBox").hide();
				$html.find("#lowerBox").hide();
				$html.find("#fileName").hide();
				$html.find(".importDB").hide();
				
			}
		});
		
		$exportAction.change(function(){
			var val=$(this).val();
			if($exportAction.is(':checked'))
			{
				$html.find(".exportDB").show();
				$html.find("#exportBox").show();
				$html.find("#download").hide();
				$html.find("#importPath").hide();
				$html.find("#upperBox").hide();
				$html.find("#lowerBox").hide();
				$html.find("#radio").hide();
				$html.find("#chooseFile").hide();
				$html.find("#path").hide();
				$html.find("#importPath").hide();
				$html.find("#importType").hide();
				$html.find("#fileName").hide();
				$html.find(".importDB").hide();
			
		    }
			else
			{
				$html.find("#exportBox").hide();
				var userAgent = navigator.userAgent.toUpperCase();
				
				if (userAgent.indexOf('MSIE') >= 0) {
				
					$html.find("#chooseFile").hide();
					$html.find("#path").hide();
					$html.find("#importPath").show();
				}
				else
				{
					$html.find("#chooseFile").show();
					$html.find("#path").show();
					$html.find("#importPath").hide();
				}
				$html.find("#radio").show();
				$html.find("#upperBox").show();
				$html.find("#lowerBox").show();
				$html.find("#importType").show();
				$html.find("#fileName").show();
				$html.find(".importDB").show();
				$html.find(".exportDB").hide();
				$html.find("#download").hide();
			}
		});
		
	
		$contentboxcontent.html($html);
		
		$("input[type=text]").on('focus', function(){	
			   $(this).addClass("focused");
			  }).on('blur', function(){
			       $(this).removeClass("focused");			
			  });
		 
		 $("input[type=radio]").on('focus', function(){				
			
			   $(this).parents(".box").addClass("focused");
			  
			  }).on('blur', function(){
				  $(this).parents(".box").removeClass("focused");			
			  });
		 
		 $("button").on('focus', function(){
			   $(this).addClass("focused");

			  }).on('blur', function(){
			       $(this).removeClass("focused");
			
			  });
		//$contentboxcontent.css('padding','20px');
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height()-110);
		$('.saveRoles').remove();
		
		PLMFlex.Assist.BindEvents.ImportDB();
		
	}
	
	function saveXLSX() {
		var filePath = $('#importPath').val();
		
		if(filePath == undefined || !filePath.endsWith(".xlsx")) {
			PLMFlex.Assist.BindEvents.Notify("Select a valid xlsx file for the Import", "error");
			return;
		}
		
		$("body").css("cursor", "progress");
		$("#b-import-xlsx").css("cursor", "progress");
		
		PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are importing database","attention", "enable");
		
		//PLMFlex.Assist.BindEvents.Notify("Please wait while the xlsx file is being imported", "attention");
		
		$('input:hidden[name=type]').val($('input:radio[name=importType]:checked').val());
		
		$("#upload").ajaxSubmit({
			success: function(ressponseJson) {
				$("#upload").get(0).reset();	
				$('#path').text('');
				$("body").css("cursor", "default");
				$("#b-import-xlsx").css("cursor", "pointer");
				//kendo grid
				var retStatus = ressponseJson.retStatus;
				//PLMFlex.Assist.BindEvents.TinyNotify(retStatus.message,retStatus.status);
				console.log(retStatus.message);
				console.log(retStatus.status);
				console.log(retStatus.status);
				var statistis=ressponseJson.stats;
				
				var htmldata="" +
				"<body><div style='width: 1000Px; height: 700px;'>" +
				"<div style='height: 20px;'>"+
				"<h3 style='font-size:13px; font-weight:normal;' id='notif' ></h3></div>"+
				"<h3 style='padding: 5px 0px 5px;'>Summary:</h3> " +
					"<p id='insertCount' style='line-height: 0.5em; padding: 6px 0;'></p><p id='updateCount'style='line-height: 0.5em; padding: 6px 0;'></p>"+
					"<p id='deleteCount' style='line-height: 0.5em; padding: 6px 0;'></p><p id='failedCount' style='line-height: 0.5em; padding: 6px 0;'></p>" +
				"<h3 style='padding: 5px 0px 5px;'>Details</h3>" +
				"<div style='width: 100%; height: 100%'><div style='width: 100%; height: 560px' id='report-grid'></div></div>"+
				"</div></body>";
					
				
				if(retStatus.status!=="error")
				{	
					PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are importing database","attention", "disable");
					
					TINY.box.show({html:htmldata,width:1000,height:700,customSource:ressponseJson,openjs:function(){
						
						var retStatus = ressponseJson.retStatus;
						//PLMFlex.Assist.BindEvents.TinyNotify(retStatus.message,retStatus.status);
						console.log(retStatus.message);
						console.log(retStatus.status);
						console.log(retStatus.status);
						var statistis=ressponseJson.stats
						var insertCount=ressponseJson.insertCount;
						var updateCount=ressponseJson.updateCount;
						var deleteCount=ressponseJson.deleteCount;
						var failedCount=ressponseJson.failedCount;
						$('#notif').text(retStatus.message);
						$('#insertCount').text("Number of Texts inserted= "+insertCount+".");
						$('#updateCount').text("Number of Texts updated: "+ updateCount+ ".");
						$('#deleteCount').text("Number of Texts deleted="+ deleteCount+ ".");
						$('#failedCount').text("Number of Texts failed="+ failedCount+ ".");
						setTimeout(function() {
							$("#report-grid").empty();
		
							var config = {
								sortable : true,
								filterable : true,
								columnMenu : false,
								resizable : true,
								reorderable : false,
								scrollable : true,
								dataSource : {
									data: statistis.stats
								},
								pageable : false,
								toolbar: ["excel"],
								excel : {
									fileName : "Assist+ Import Stats.xlsx",
									filterable : true,
									allPages : true
								}, 
								schema : {
									model : {
										fields : {
											classId : {
												type : "string"
											},
											attrId : {
												type : "string"
											},
											workflow_lifecycle : {
												type : "string"
											},
											statuses : {
												type : "string"
											},
											roles : {
												type : "string"
											},
											action : {
												type : "string"
											}
										}
									}
								},
								columns : [ {
									field : "classId",
									title : "Class Name",
									type : "string",
									width : 150
								},	{
									field : "attrId",
									title : "Attribute Name",
									type : "string",
									width : 150
								}, {
									field : "workflow_lifecycle",
									title : "Workflow",
									width : 100,
									type : "string"
								},
								{
									field : "statuses",
									title : "Statuses",
									width : 150,
									type : "string",
									width : 100
								},
								{
									field : "roles",
									title : "Roles",
									width : 150,
									type : "string",
									width : 100
								},
								{
									field : "action",
									title : "Action",
									width : 150,
									type : "string",
									width : 100
								}]
							};
		
							$("#report-grid").kendoGrid(config);
						}, 100);
						
	
						}});
				}
				else
					PLMFlex.Assist.BindEvents.Notify(ressponseJson.retStatus.message, ressponseJson.retStatus.status);
				
				if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)	{
					PLMFlex.Assist.Settings.Import.Load($contentboxcontent);
				}
				
			},
			error : function(response) {
				$("#upload").get(0).reset();	
				$('#path').text('');
				$("body").css("cursor", "default");
				$("#b-import-xlsx").css("cursor", "pointer");
				PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are importing database","attention", "disable");
				
				if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)	{
					PLMFlex.Assist.Settings.Import.Load($contentboxcontent);
				}
				
				PLMFlex.Assist.BindEvents.Notify("Error while XLSX import. " + response.message, "error");
			}
		});
	}
	
//	function saveImport()
//	{
//		if($('#importPath').val()=="" ) {
//			PLMFlex.Assist.BindEvents.Notify("File path field is required","error");
//			return;
//		}
//		
//		var split=$('#importPath').val().split('.');
//		var ext=split[1];
//		
//		if(ext!="xml" ) {
//			PLMFlex.Assist.BindEvents.Notify("XML file is required","error");
//			return;
//		}
//		$("body").css("cursor", "progress");
//		$(".importDB").css("cursor", "progress");
//		var x=document.getElementById("importPath").value;
//		
//		//var myWindow = window.open("popUp.html", "MsgWindow", "width=200,height=100");
//		
////		var htmldata="<div class='lookupContainer' data-bind='template:{name:\"lookupchainTempl\" }'>" +
////		"</div>";
////		TINY.box.show({html:htmldata,width:600,height:400,customSource:customSourceko,openjs:function(){
////		AEViewModels.LookupChainVM=new LookupChain(customSourceko,"condition");
////		ko.cleanNode($(".lookupContainer")[0]);
////		ko.setTemplateEngine(new ko.nativeTemplateEngine());
////		ko.applyBindings(AEViewModels.LookupChainVM, $(".lookupContainer")[0]);
////
////		}});
//		
//		
//
//	
//		
//		
//		
//		var type="";
//		if ($("#TypeOverWrite").is(':checked'))
//		{
//			type='overWrite';
//		}
//		else if ($("#TypeMerge").is(':checked'))
//		{
//			type='merge';
//		}
//		PLMFlex.Assist.BindEvents.Notify("Please wait while we are importing Database","attention");
//		PLMFlex.Assist.Request.post("DBUtility", {'type' : type,'mode' : "import", 'path' : $('#importPath').val(), rnd: Math.random()}, function(ressponseJson) {
//			$("#upload").get(0).reset();	
//			$('#path').text('');
//			$("body").css("cursor", "default");
//			$(".importDB").css("cursor", "pointer");
//			var retStatus = ressponseJson.retStatus;
//			PLMFlex.Assist.BindEvents.Notify(retStatus.message,retStatus.status);
//			console.log(retStatus.message);
//			console.log(retStatus.status);
//			console.log(retStatus.status);
//			var statistis=ressponseJson.stats
//			var insertCount=ressponseJson.insertCount;
//			var updateCount=ressponseJson.updateCount;
//			var deleteCount=ressponseJson.deleteCount;
//			
//			
//			var htmldata="" +
//			"<div style='width: 1000Px; height: 700px;'>" +
//				"<div class='modal'></div>" +
//					"<h3 style='padding: 5px 0px 5px;'>Summary:</h3> " +
//						"<p style='line-height: 0.5em; padding: 6px 0;'>Number of Texts inserted= "+insertCount+".</p><p style='line-height: 0.5em; padding: 6px 0;'>Number of Texts updated: "+ updateCount+ ". </p>"+
//						"<p style='line-height: 0.5em; padding: 6px 0;'>Number of Texts deleted=		"+ deleteCount+ ". </p>" +
//					"<h3 style='padding: 5px 0px 5px;'>Details</h3>" +
//					"<div style='width: 100%; height: 100%'><div style='width: 100%; height: 560px' id='report-grid'></div></div>"+
//			"</div>";
//				
//			
//			if(retStatus.status!="error")
//				{
//				TINY.box.show({html:htmldata,width:1000,height:700, customSource:statistis,openjs:function(){
//				
//					setTimeout(function() {
//						$("#report-grid").empty();
//	
//						var config = {
//							sortable : true,
//							filterable : true,
//							columnMenu : false,
//							resizable : true,
//							reorderable : false,
//							scrollable : true,
//							dataSource : {
//								data: statistis.stats
//							},
//							pageable : false,
//							toolbar: ["excel"],
//							excel : {
//								fileName : "Assist+ Import Stats.xlsx",
//								filterable : true,
//								allPages : true
//							}, 
//							schema : {
//								model : {
//									fields : {
//										classId : {
//											type : "string"
//										},
//										attrId : {
//											type : "string"
//										},
//										workflow_lifecycle : {
//											type : "string"
//										},
//										statuses : {
//											type : "string"
//										},
//										roles : {
//											type : "string"
//										},
//										action : {
//											type : "string"
//										}
//									}
//								}
//							},
//							columns : [ {
//								field : "classId",
//								title : "Class Name",
//								type : "string",
//								width : 150
//							},	{
//								field : "attrId",
//								title : "Attribute Name",
//								type : "string",
//								width : 150
//							}, {
//								field : "workflow_lifecycle",
//								title : "Workflow",
//								width : 100,
//								type : "string"
//							},
//							{
//								field : "statuses",
//								title : "Statuses",
//								width : 150,
//								type : "string",
//								width : 100
//							},
//							{
//								field : "roles",
//								title : "Roles",
//								width : 150,
//								type : "string",
//								width : 100
//							},
//							{
//								field : "action",
//								title : "Action",
//								width : 150,
//								type : "string",
//								width : 100
//							}]
//						};
//	
//						$("#report-grid").kendoGrid(config);
//					}, 100);
//					
//
//					}});
//				}
//			
//			
//			
//			if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)
//			{
//				PLMFlex.Assist.Settings.Import.Load($contentboxcontent);
//			}
//			
//			});
//	}
	
	function saveImport()
	{
		if($('#importPath').val()=="" ) {
			PLMFlex.Assist.BindEvents.Notify("File path field is required","error");
			return;
		}
		
		var split=$('#importPath').val().split('.');
		var ext=split[1];
		
		if(ext!="xml" ) {
			PLMFlex.Assist.BindEvents.Notify("XML file is required","error");
			return;
		}
		$("body").css("cursor", "progress");
		$(".importDB").css("cursor", "progress");
		var x=document.getElementById("importPath").value;
		
		//var myWindow = window.open("popUp.html", "MsgWindow", "width=200,height=100");
		
//		var htmldata="<div class='lookupContainer' data-bind='template:{name:\"lookupchainTempl\" }'>" +
//		"</div>";
//		TINY.box.show({html:htmldata,width:600,height:400,customSource:customSourceko,openjs:function(){
//		AEViewModels.LookupChainVM=new LookupChain(customSourceko,"condition");
//		ko.cleanNode($(".lookupContainer")[0]);
//		ko.setTemplateEngine(new ko.nativeTemplateEngine());
//		ko.applyBindings(AEViewModels.LookupChainVM, $(".lookupContainer")[0]);
//
//		}});
		
		
		var type="";
		if ($("#TypeOverWrite").is(':checked'))
		{
			type='overWrite';
		}
		else if ($("#TypeMerge").is(':checked'))
		{
			type='merge';
		}
		var importPathVal=$('#importPath').val();
		//var allinfo={type, importPathVal};
		//<p>loading</p> <img src="css/images/preload.gif" alt="Loading">
		//TINY.box.show({image: 'css/images/preload.gif',close:false,customSource:allinfo,openjs:function(){
		PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are importing database","attention", "enable");
			PLMFlex.Assist.Request.post("DBUtility", {'type' : type,'mode' : "import", 'path' : importPathVal, rnd: Math.random()}, function(ressponseJson) {
				$("#upload").get(0).reset();	
				$('#path').text('');
				$("body").css("cursor", "default");
				$(".importDB").css("cursor", "pointer");
				var retStatus = ressponseJson.retStatus;
				//PLMFlex.Assist.BindEvents.TinyNotify(retStatus.message,retStatus.status);
				console.log(retStatus.message);
				console.log(retStatus.status);
				console.log(retStatus.status);
				var statistis=ressponseJson.stats;
			
				
//				var htmldata="" +
//				"<body><div style='width: 1000Px; height: 700px;'>" +
//				"<div style='height: 40px;'>"+
//				"<div class='tiny-notification attention' id='tinNotify' style='display: none; width: 1000px; height: 20px;'>" +
//					"<span class='img'>&nbsp;</span>" +
//						"<div>Assist Text entries after successful import contain classes/attributes not defined on the destination server, some entries may not have not been imported</div>" +
//						"</div>" +
//				"</div>"+
//				"<h3 style='padding: 5px 0px 5px;'>Summary:</h3> " +
//					"<p id='insertCount' style='line-height: 0.5em; padding: 6px 0;'></p><p id='updateCount'style='line-height: 0.5em; padding: 6px 0;'></p>"+
//					"<p id='deleteCount' style='line-height: 0.5em; padding: 6px 0;'></p>" +
//				"<h3 style='padding: 5px 0px 5px;'>Details</h3>" +
//				"<div style='width: 100%; height: 100%'><div style='width: 100%; height: 560px' id='report-grid'></div></div>"+
//				"</div></body>";
				
				var htmldata="" +
				"<body><div style='width: 1000Px; height: 700px;'>" +
				"<div style='height: 20px;'>"+
				"<h3 style='font-size:13px; font-weight:normal;' id='notif' ></h3></div>"+
				"<h3 style='padding: 5px 0px 5px;'>Summary:</h3> " +
					"<p id='insertCount' style='line-height: 0.5em; padding: 6px 0;'></p><p id='updateCount'style='line-height: 0.5em; padding: 6px 0;'></p>"+
					"<p id='deleteCount' style='line-height: 0.5em; padding: 6px 0;'></p><p id='failedCount' style='line-height: 0.5em; padding: 6px 0;'></p>" +
				"<h3 style='padding: 5px 0px 5px;'>Details</h3>" +
				"<div style='width: 100%; height: 100%'><div style='width: 100%; height: 560px' id='report-grid'></div></div>"+
				"</div></body>";
					
				
				if(retStatus.status!=="error")
				{	
					PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are importing database","attention", "disable");
					
					TINY.box.show({html:htmldata,width:1000,height:700,customSource:ressponseJson,openjs:function(){
						
						var retStatus = ressponseJson.retStatus;
						//PLMFlex.Assist.BindEvents.TinyNotify(retStatus.message,retStatus.status);
						console.log(retStatus.message);
						console.log(retStatus.status);
						console.log(retStatus.status);
						var statistis=ressponseJson.stats
						var insertCount=ressponseJson.insertCount;
						var updateCount=ressponseJson.updateCount;
						var deleteCount=ressponseJson.deleteCount;
						var failedCount=ressponseJson.failedCount;
						$('#notif').text(retStatus.message);
						$('#insertCount').text("Number of Texts inserted= "+insertCount+".");
						$('#updateCount').text("Number of Texts updated: "+ updateCount+ ".");
						$('#deleteCount').text("Number of Texts deleted="+ deleteCount+ ".");
						$('#failedCount').text("Number of Texts failed="+ failedCount+ ".");
						setTimeout(function() {
							$("#report-grid").empty();
		
							var config = {
								sortable : true,
								filterable : true,
								columnMenu : false,
								resizable : true,
								reorderable : false,
								scrollable : true,
								dataSource : {
									data: statistis.stats
								},
								pageable : false,
								toolbar: ["excel"],
								excel : {
									fileName : "Assist+ Import Stats.xlsx",
									filterable : true,
									allPages : true
								}, 
								schema : {
									model : {
										fields : {
											classId : {
												type : "string"
											},
											attrId : {
												type : "string"
											},
											workflow_lifecycle : {
												type : "string"
											},
											statuses : {
												type : "string"
											},
											roles : {
												type : "string"
											},
											action : {
												type : "string"
											}
										}
									}
								},
								columns : [ {
									field : "classId",
									title : "Class Name",
									type : "string",
									width : 150
								},	{
									field : "attrId",
									title : "Attribute Name",
									type : "string",
									width : 150
								}, {
									field : "workflow_lifecycle",
									title : "Workflow",
									width : 100,
									type : "string"
								},
								{
									field : "statuses",
									title : "Statuses",
									width : 150,
									type : "string",
									width : 100
								},
								{
									field : "roles",
									title : "Roles",
									width : 150,
									type : "string",
									width : 100
								},
								{
									field : "action",
									title : "Action",
									width : 150,
									type : "string",
									width : 100
								}]
							};
		
							$("#report-grid").kendoGrid(config);
						}, 100);
						
	
						}});
				}
									
				else 
					{
					PLMFlex.Assist.BindEvents.Notify(retStatus.message,retStatus.status);
					}

				
				
				
				});
		//	}});
		if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)
		{
			PLMFlex.Assist.Settings.Import.Load($contentboxcontent);
		}
	
		
		
	}
	function saveExport()
	{
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are Exporting Database","attention");
			PLMFlex.Assist.Request.post("DBUtility", {
				'mode' : "export",
				rnd: Math.random()
			}, function(responseJson) {
				var type=responseJson.status;
				if(type=="success")
					$('#download').show();
			});
	}
	function saveXLSXFile()
	{
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are Exporting Database","attention");
			PLMFlex.Assist.Request.post("ExportExcel", {
				'mode' : "export",
				rnd: Math.random()
			}, function(responseJson) {
				var type=responseJson.status;
				console.log(type);
				if(type=="success")
					$('#download').show();
			});
	}
	
	function saveChangePassword(params){
		
	}
	
	function loadChangePassword($contentboxcontent){
		var html=PLMFlex.Assist.HTML.getChangePasswordHTML;
		$contentboxcontent.html(html);
		 $("input[type=password]").on('focus', function(){	
			   $(this).addClass("focused");
			  }).on('blur', function(){
			       $(this).removeClass("focused");			
			  });
	
		 $("button").on('focus', function(){
			   $(this).addClass("focused");

			  }).on('blur', function(){
			       $(this).removeClass("focused");
			
			  });
		
		//$contentboxcontent.css('padding','20px');
		$('.saveRoles').remove();
		PLMFlex.Assist.BindEvents.ChangePassword();
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height()-110);
		
	}
	
	var Config={
			Save:saveSettings,
			Load:loadSettings	
	};
	
	var Roles={
			Save:saveRoles,
			Load:loadRoles	
	};
	var Password={
			Save:saveChangePassword,
			Load:loadChangePassword
	};
	var Import={
			Save:saveImport,
			Load:loadImport,
			LoadXLSX: loadXlsxImport,
			saveXLSX: saveXLSX
	};
	var Export={
			Save:saveExport,
			saveXLSXFile:saveXLSXFile
	};
	return{
		Config:Config,
		Roles:Roles,
		Password:Password,
		Import:Import,
		Export:Export
	};
	
})();

