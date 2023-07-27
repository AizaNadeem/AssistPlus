if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.Settings=(function(){
	
	function loadSettings($contentboxcontent)
	{
		if($("#ImportLicLnk"))
		{
			$("#ImportLicLnk").hide();
		}
		
		PLMFlex.Assist.Request.get("loadConfig?",function(jsonResponse)
		{
			var User=jsonResponse.AgileUser;
			var Password=jsonResponse.AgilePassword;
			var URL=jsonResponse.AgileURL;
			var primaryURL=jsonResponse.PrimaryInstanceURL;
			var accessType=jsonResponse.accessType;
			var hoverColor=jsonResponse.hoverColor;
			
			
			if(accessType && accessType.value=='usergroups')
			{
				accessTypeRole=false;
				updateLabels();
			}
			
			var html=PLMFlex.Assist.HTML.getConfigHTML;
			var $html=$(html);
			var $NodeTypeMaster=$html.find('#NodeTypeMaster');
			var $NodeTypeSlave=$html.find('#NodeTypeSlave');
			
			$NodeTypeMaster.change(function(){
				var val=$(this).val();
				if($NodeTypeMaster.is(':checked'))
				{
			        $("#PrimaryInstanceURL").attr("disabled",'disabled');
			        $("#PrimaryInstanceURL").hide();
			        $("#primaryInstanceLabel").hide();
			    }
				else
				{
					$("#PrimaryInstanceURL").attr("disabled",null);
					$("#PrimaryInstanceURL").show();
					$("#primaryInstanceLabel").show();
					if($("#PrimaryInstanceURL").val() == '')
					{
						$("#PrimaryInstanceURL").val('http://<AgileServer>:<AgilePort>/AssistPlus');
					}
				}
			});
			$NodeTypeSlave.change(function(){
				var val=$(this).val();
				if($NodeTypeSlave.is(':checked'))
				{
					$("#PrimaryInstanceURL").attr("disabled",null);
					$("#PrimaryInstanceURL").show();
					$("#primaryInstanceLabel").show();
					if($("#PrimaryInstanceURL").val() == '')
					{
						$("#PrimaryInstanceURL").val('http://<AgileServer>:<AgilePort>/AssistPlus');
					}
					
	
			    }
				else
				{
			        $("#PrimaryInstanceURL").attr("disabled",'disabled');
			        $("#PrimaryInstanceURL").hide();
			        $("#primaryInstanceLabel").hide();
				}
			});
			
			$html.find('#'+User.id).val(User.value);
			$html.find('#'+Password.id).val(PLMFlex.Assist.Security.Base64ToText(Password.value));
			$html.find('#'+URL.id).val(URL.value);
			if(primaryURL)
			{
				$html.find('#'+primaryURL.id).val(primaryURL.value);
			}
			
			
			var nodeTypeSelect=$html.find('#isCurrentInstancePrimary');
			if(primaryURL.value && primaryURL.value!="")
			{
				$NodeTypeSlave.prop('checked',true);
				$html.find("#PrimaryInstanceURL").show();
				$html.find("#primaryInstanceLabel").show();
				$html.find("#PrimaryInstanceURL").attr("disabled",null);
				$("#roles").hide();
				$("#assistTextLi").hide();
			}
			else
			{
				$NodeTypeMaster.prop('checked',true);
				$html.find("#PrimaryInstanceURL").hide();
				$html.find("#primaryInstanceLabel").hide();
				$("#roles").show();
				$("#assistTextLi").show();
			} 

				AssistColorPicker.createBackgroundColorPicker($html.find("#hoverColor"),hoverColor.value.toString(),'#ffffff');
				$(this).find('.picker').each(function()
						{
					$this
						.css('border',"0px")
						.css('padding',"1px")
						.css('margin-top',"1px")
						.css('width',"110px")
						.css('background-color',"")
						.html("Label");
						});
					$html.find('.colorSelector ').each(function()
						{
							$(this).css('padding',"0px")
									.css('margin',"0px");
						});

			$contentboxcontent.html($html);
			$contentboxcontent.css('padding','20px');
			var $button= $html.find('.button.saveConfig');
			PLMFlex.Assist.BindEvents.saveConfig($button);
			$('.saveRoles').remove();
		});
	}
	function updateLabels()
	{
		$("#roles").text("User Group Priority");
	}
	function endsWith(str, suffix) {
	    return str.indexOf(suffix, str.length - suffix.length) !== -1;
	}
	function saveSettings($subcontent)
	{
		var configs = [];
		var selectVal="";
		if ($("#NodeTypeMaster").is(':checked'))
		{
			selectVal='master';
		}
		
		var primaryInstUrl=$("#PrimaryInstanceURL");
		var agileUrl=$("#AgileURL").val();
		var agileServerOnly=agileUrl.substring(0,agileUrl.indexOf('/Agile'));
		
		if(selectVal=='master')
		{
			primaryInstUrl.val("");
		}
		else
		{
			if(!primaryInstUrl.val() || primaryInstUrl.val()=='http://<AgileServer>:<AgilePort>/AssistPlus'
			|| 	primaryInstUrl.val().indexOf('AgileServer')!=-1
			|| 	primaryInstUrl.val().indexOf('AgilePort')!=-1
			)
			{
				PLMFlex.Assist.BindEvents.Notify("Please provide AssistPlus Master node URL","error");
				return;
			}
			else
			{
				var primaryNodeUrl=primaryInstUrl.val();
				
				if(endsWith(primaryNodeUrl,"/"))
				{
					primaryNodeUrl=primaryNodeUrl.substring(0,primaryNodeUrl.length-1);
				}	
				if(!endsWith(primaryNodeUrl,"/AssistPlus"))
				{
					if(!endsWith(primaryNodeUrl,"/"))
					{
						primaryInstUrl.val(primaryNodeUrl+"/AssistPlus");
					}
					else
					{
						primaryInstUrl.val(primaryNodeUrl+"AssistPlus");
					}
				}
				else
				{
					primaryInstUrl.val(primaryNodeUrl);
				}
				if(agileUrl)
				{
					
					if(primaryInstUrl.val().indexOf(agileServerOnly)!=-1)
					{
						PLMFlex.Assist.BindEvents.Notify("A Master node cannot be its own Slave.","error");
						return;
					}
				}
			}
		}
		var primaryURL=primaryInstUrl.val();
		if(primaryURL)
		{
			$("#roles").hide();
			$("#assistTextLi").hide();
		}
		else
		{
			$("#roles").show();
			$("#assistTextLi").show();
		}
		configs[0]="hoverColor"+"="+AssistColorPicker.getHexColor($subcontent.find('.colorSelector span').css('color'));
		if(selectVal=='master')
		{

			var dataIndex=1;
			$subcontent.find('input').each(function(i, el){
				
				if($(this).attr('type')!='checkbox')
				{
					var key=$(this).data('id');
					var value=$(this).val();
					if($(this).attr('type')=='password'){
						value=PLMFlex.Assist.Security.Base64(value);
					}
					if(key)
					{
						configs[dataIndex] = key+"="+value;
						dataIndex++;
					}
				}
			});
			PLMFlex.Assist.Request.post("loadConfig?",
					{'configs[]' : configs,
					rnd: Math.random()
				},function(jsonResponse)
				{
					if(jsonResponse && jsonResponse.status=='success')
					{
						if(accessType=='roles')
						{
							$("#roles").text("Role Priority");
							accessTypeRole=true;
						}
						else
						{
							$("#roles").text("User Group Priority");
							accessTypeRole=false;
						}
					}
				});
		
		}
		else
		{
			PLMFlex.Assist.Request.get("loadConfig?masterServerUrl="+primaryURL,function(jsonResponse)
					{
							var primaryURL=jsonResponse.PrimaryInstanceURL;
							if(primaryURL)
							{
								if(primaryURL.value!="")
								{
									PLMFlex.Assist.BindEvents.Notify("The provided Master node itself is a Slave, cannot proceed.","error");
								}
								else
								{
									var dataIndex=0;
									$subcontent.find('input').each(function(i, el){
										
										if($(this).attr('type')!='checkbox')
										{
											var key=$(this).data('id');
											var value=$(this).val();
											if($(this).attr('type')=='password'){
												value=PLMFlex.Assist.Security.Base64(value);
											}
											if(key)
											{
												configs[dataIndex] = key+"="+value;
												dataIndex++;
											}
										}
									});
									PLMFlex.Assist.Request.post("loadConfig?",
											{'configs[]' : configs,
											rnd: Math.random()
										},function(jsonResponse)
										{
											if(jsonResponse && jsonResponse.status=='success')
											{
												if(accessType=='roles')
												{
													$("#roles").text("Role Priority");
													accessTypeRole=true;
												}
												else
												{
													$("#roles").text("User Group Priority");
													accessTypeRole=false;
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
				var fontcolor=AssistColorPicker.getHexColor($el.find('.colorSelector span').css('color'));
				var background=AssistColorPicker.getHexColor($el.find('.colorSelector span').css('background-color'));
				roles[i] = ($("#assist-roles li").length - $el.index()) + "<ri>" + $.trim($el.find('.text').text()) +"<ri>"+$el.attr('id')+"<ri>"+  fontcolor+"<ri>"+background;
				rolesToSave[i]=$.trim($el.find('.text').text());
			});
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
			var choice=accessTypeRole?"Role(s)":"User Group(s)" ;
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
	function saveUserGroups()
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
			$("#assist-roles li").each(function(i, el){
				var $el=$(el);
				var fontcolor=AssistColorPicker.getHexColor($el.find('.colorSelector span').css('color'));
				var background=AssistColorPicker.getHexColor($el.find('.colorSelector span').css('background-color'));
				roles[i] = ($("#assist-roles li").length - $el.index()) + ":" + $.trim($el.find('.text').text()) +":"+$el.attr('id')+":"+  fontcolor+":"+background;        
			});
			
			
			PLMFlex.Assist.Request.post("loadRoles",
					{'roles[]' : roles,
				rnd: Math.random()
				},function(jsonResponse){});
		}
	}
	
	function loadUserGroups($containerHeader,$contentboxcontent)
	{
		var userGroupsContainer=PLMFlex.Assist.HTML.getUserGroupsContainerHTML;
		var $userGroupsContainer=$(userGroupsContainer);
		PLMFlex.Assist.BindEvents.Filter($userGroupsContainer,".assistUserGroupFilter","#assist-usergroups li",".roleText .text");
		PLMFlex.Assist.BindEvents.Filter($userGroupsContainer,".agileUserGroupFilter","#agile-usergroups li","");
		PLMFlex.Assist.BindEvents.Filter($userGroupsContainer,".classFilter","#class-table tr.parent","td:first-child a");
		PLMFlex.Assist.BindEvents.Filter($userGroupsContainer,".attFilter","#attr-attribute tr","td:first-child a");
		$contentboxcontent.html($userGroupsContainer);
		$contentboxcontent.css('padding','0');
		
		var $saveUserGroups=$("<span style='float:right;margin-top:10px' class='button saveUserGroups'>Save User Groups;</span>");
		$contentboxcontent.parents('#main-content').find('.saveUserGroups').remove();
		$saveUserGroups.insertAfter( $contentboxcontent.parents('.content-box'));
		PLMFlex.Assist.BindEvents.saveUserGroups($saveUserGroups);
		
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Agile/Assist User Group(s)","attention");
		var $agileUserGroups=$userGroupsContainer.find('#agile-usergroups');
		var $assistUserGroups=$userGroupsContainer.find('#assist-usergroups');
		
		PLMFlex.Assist.Request.get("userGroups?",function(jsonResponse)
		{
			if(jsonResponse.hasOwnProperty("object"))
			{
				$.each(jsonResponse.object,function(index, usergroupentry)
				{ 
					if (usergroupentry.priority < 0) 
					{
						$('<LI />').attr({id:("'"+usergroupentry.roleID+"'")}).addClass("ui-state-default").html(usergroupentry.Role).appendTo($agileUserGroups);
					} else
					{
						var $list = $('<LI />').attr({id:("'"+usergroupentry.roleID+"'")}).addClass("ui-state-default")
								    .html("<div class='roleText'><span class='text'>"+usergroupentry.Role+"</span></div>")
									.appendTo($assistUserGroups);
						AssistColorPicker.createPicker($list.find('.roleText'),usergroupentry.fontColor,usergroupentry.backgroundColor);
					}
				});
			}
			setHeights($userGroupsContainer,$contentboxcontent,$containerHeader);
			$( "#assist-usergroups, #agile-usergroups" ).sortable({
				connectWith: ".connectedSortable",
				stop: userGroupStopped
			}).disableSelection();
		});
		
		$(window).resize(function()
		{
			setHeights($userGroupsContainer,$contentboxcontent,$containerHeader);	
		});
		
	}
	var initialRoleList;
	function loadRoles($containerHeader,$contentboxcontent){
		initialRoleList=new Array;
		var rolesContainer=PLMFlex.Assist.HTML.RoleHTML.rolesHTML((accessTypeRole?"Role(s)":"User Group(s)"));
		
		var $rolesContainer=$(rolesContainer);
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".assistRoleFilter","#assist-roles li",".roleText .text");
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".agileRoleFilter","#agile-roles li","");
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".classFilter","#class-table tr.parent","td:first-child a");
		PLMFlex.Assist.BindEvents.Filter($rolesContainer,".attFilter","#attr-attribute tr","td:first-child a");
		$contentboxcontent.html($rolesContainer);
		$contentboxcontent.css('padding','0');
		
		var $saveRoles=$("<span style='float:right;margin-top:10px' class='button saveRoles'>"+(accessTypeRole?"Save Roles":"Save User Groups")+"</span>");
		$contentboxcontent.parents('#main-content').find('.saveRoles').remove();
		$saveRoles.insertAfter( $contentboxcontent.parents('.content-box'));
		PLMFlex.Assist.BindEvents.saveRoles($saveRoles);
		
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Agile/Assist"+(accessTypeRole?" Roles":" User Group(s)"),"attention");
		var $agileRoles=$rolesContainer.find('#agile-roles');
		var $assistRoles=$rolesContainer.find('#assist-roles');
		
		PLMFlex.Assist.Request.get("loadRoles?",function(jsonResponse){
			if(jsonResponse.hasOwnProperty("object")){
			$.each(jsonResponse.object,function(index, roleentry) 
			{ 
				
				// Iterate over the JSON array.
						if (roleentry.priority < 0) {
							$('<LI />').attr({id:("'"+roleentry.roleID+"'")}).addClass("ui-state-default").text(roleentry.Role).appendTo($agileRoles);
						} else {
							
							var $list = $('<LI />').attr({id:("'"+roleentry.roleID+"'")}).addClass("ui-state-default")
													.html("<div class='roleText' style='display:inline-block;width:100%;'>" +
													"<span class='text textExtend'></span>" +
													"</div>");
							$list.find(".text").text(roleentry.Role);
							$list.appendTo($assistRoles);
							AssistColorPicker.createPicker($list.find('.roleText'),roleentry.fontColor,roleentry.backgroundColor);
							initialRoleList.push(roleentry.Role);
								
						}
					});
			
			}
			$assistRoles.find('.colorSelector').each(function()
			{
				$(this).css('float',"right");
				$(this).css('width',"80px");
				$(this).css("margin","0px 5px 0px 0px");
				if($.browser.msie && $.browser.version == "7.0")
				{
					$(this).css("margin-right","19px");
				}
			});
			
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
		
		
		
		$(window).resize(function(){
			setHeights($rolesContainer,$contentboxcontent,$containerHeader);	
		});
		
	}
	
	function setHeights($rolesContainer,$contentboxcontent,$containerHeader){
		$rolesContainer.eq(1).height($contentboxcontent.height()-$containerHeader.height());
		$rolesContainer.eq(2).height($contentboxcontent.height()-$containerHeader.height());
		
		var spanHeight=$(".rolesContainer").height();
		var assistRoleHeight=$( "#assist-roles").height();
		var agileRoleHeight=$( "#agile-roles").height();
		
		if(assistRoleHeight < spanHeight)
		{
			$( "#assist-roles").height(spanHeight);
		}
		if(agileRoleHeight < spanHeight)
		{
			$( "#agile-roles").height(spanHeight);
		}
	}
	
	function stopped(event,ui){
		
		if(ui.item.parents('ul#assist-roles').length > 0){
			if(ui.item.find('.colorSelector').length==0){
				var roleText= ui.item.text();
				ui.item.html("<div class='roleText' style='height:auto;display:inline-block;width:100%;'>" +
						"<span id='spanRoleText' class='text textExtend'>" +
						"</span></div>");
				ui.item.find("#spanRoleText").text(roleText);
				AssistColorPicker.createPicker(ui.item.find('.roleText'),'#ffffff','#333333');
				ui.item.find(".colorSelector").css("margin","0px 2px 0px 0px");
				if($.browser.msie && $.browser.version == "7.0")
				{
					ui.item.find(".colorSelector").css("margin-right","17px");
				}
			}
		}else{
				if(ui.item.length>0){
					if(ui.item.find('.colorSelector').length>0){
						var $textDiv=ui.item.find('.roleText span.text');
						var roleText=$textDiv.text();
						ui.item.text(roleText);
						ui.item.find(':last-child').remove();
					}
				}
		}
		var $contentboxcontent=$('.content-box-content');
		var $contentboxheader=$('.content-box-header');
		var $rolesContainer=$('.rolesContainer');
		
		setHeights($rolesContainer,$contentboxcontent,$contentboxheader);
	}
	function userGroupStopped(event,ui)
	{
		if(ui.item.parents('ul#assist-usergroups').length > 0)
		{
			if(ui.item.find('.colorSelector').length==0)
			{
				var roleText= ui.item.text();
				ui.item.html("<div class='roleText'><span class='text'>"+roleText+"</span></div>");
				AssistColorPicker.createPicker(ui.item.find('.roleText'),'#ffffff','#333333');
			}
		}else
		{
			if(ui.item.length>0)
			{
				if(ui.item.find('.colorSelector').length>0)
				{
					var $textDiv=ui.item.find('.roleText span.text');
					var roleText=$textDiv.text();
					ui.item.html(roleText);
					ui.item.find(':last-child').remove();
				}
			}
		}
		
	}
	
	function loadImport($contentboxcontent)
	{
		if($("#ImportLicLnk"))
		{
			$("#ImportLicLnk").hide();
		}
		
		var html=PLMFlex.Assist.HTML.getImportDatabaseHTML;
	
		var $html=$(html);
		
		
		var $importAction=$html.find('#importAction');
		
		
		var $exportAction=$html.find('#exportAction');
		
		var $TypeOverWrite=$html.find('#TypeOverWrite');
		
		$html.find("#chooseFile").show();
		$html.find("#importType").show();
		$html.find("#fileName").show();
		$html.find(".importDB").show();
		$html.find(".exportDB").hide();
		$html.find("#download").hide();
		 
		$importAction.prop('checked',true);
		$TypeOverWrite.prop('checked',true);
		
		$importAction.change(function(){
			
			var val=$(this).val();
			if($importAction.is(':checked'))
			{
				$html.find("#chooseFile").show();
				$html.find("#radio").show();
				$html.find("#upperBox").show();
				$html.find("#lowerBox").show();
				$html.find("#importType").show();
				$html.find("#fileName").show();
				$html.find(".importDB").show();
				$html.find(".exportDB").hide();
				$html.find("#download").hide();
						
		    }
			else
			{
				$html.find(".exportDB").show();
				$html.find("#download").hide();
				$html.find("#chooseFile").hide();
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
				$html.find("#download").hide();
				$html.find("#importPath").hide();
				$html.find("#upperBox").hide();
				$html.find("#lowerBox").hide();
				$html.find("#radio").hide();
				$html.find("#chooseFile").hide();
				$html.find("#importType").hide();
				$html.find("#fileName").hide();
				$html.find(".importDB").hide();
			
		    }
			else
			{
				
				$html.find("#chooseFile").show();
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
		$contentboxcontent.css('padding','20px');
		$('.saveRoles').remove();
		
		PLMFlex.Assist.BindEvents.ImportDB();
		
	}
	
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
		
		var x=document.getElementById("importPath").value;
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are importing Database","attention");
		
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
				$('#path').text('');
			});
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
	
	function saveChangePassword(params){
		
	}
	
	function loadChangePassword($contentboxcontent){
		var html=PLMFlex.Assist.HTML.getChangePasswordHTML;
		$contentboxcontent.html(html);
		$contentboxcontent.css('padding','20px');
		$('.saveRoles').remove();
		PLMFlex.Assist.BindEvents.ChangePassword();
		
	}
	var Config={
			Save:saveSettings,
			Load:loadSettings	
	}
	
	var Roles={
			Save:saveRoles,
			Load:loadRoles	
	}
	var UserGroups={
			Save:saveUserGroups,
			Load:loadUserGroups	
	}
	var Password={
			Save:saveChangePassword,
			Load:loadChangePassword
	}
	var Import={
			Save:saveImport,
			Load:loadImport
	}
	var Export={
			Save:saveExport
	}
	return{
		Config:Config,
		Roles:Roles,
		UserGroups:UserGroups,
		Password:Password,
		Import:Import,
		Export:Export
	};
	
})();

