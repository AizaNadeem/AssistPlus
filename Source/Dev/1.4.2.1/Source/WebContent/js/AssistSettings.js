if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.Settings=(function(){
	
	function loadSettings($contentboxcontent)
	{
		PLMFlex.Assist.Request.get("loadConfig?",function(jsonResponse)
		{
			var User=jsonResponse.AgileUser;
			var Password=jsonResponse.AgilePassword;
			var URL=jsonResponse.AgileURL;
			var accessType=jsonResponse.accessType;
			
			if(accessType && accessType.value=='usergroups')
			{
				accessTypeRole=false;
				updateLabels();
			}
			
			var html=PLMFlex.Assist.HTML.getConfigHTML;
			var $html=$(html);
			//console.info($html);
			$html.find('#'+User.id).val(User.value);
			$html.find('#'+Password.id).val(PLMFlex.Assist.Security.Base64ToText(Password.value));
			$html.find('#'+URL.id).val(URL.value);
			if(accessType)
			{
				var txtAccessType=$html.find('#'+accessType.id);
				txtAccessType.val(accessType.value);
				txtAccessType.prop("disabled","disabled");
			}
			else
			{
				var txtAccessType=$html.find('#accessType');
				
				txtAccessType.val("roles");
				txtAccessType.prop("disabled",false);
				PLMFlex.Assist.BindEvents.Notify("Please choose Assist Text access type, default is Roles.","attention");
			}
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
	function saveSettings($subcontent){
		var configs = [];
		$subcontent.find('input').each(function(i, el){
			var key=$(this).data('id');
			var value=$(this).val();
			if($(this).attr('type')=='password'){
				value=PLMFlex.Assist.Security.Base64(value);
			}
			
			configs[i] = key+"="+value;
		});
		
		var accessType=$("#accessType").val();
		
		configs[configs.length]="accessType="+accessType;
		
		
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
	
	function saveRoles()
	{
		if ($("#assist-roles li").length == 0) 
		{ 
			if(accessTypeRole)
			{
				PLMFlex.Assist.BindEvents.Notify("Please Add a Roles to save","attention");
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
	function saveUserGroups()
	{
		if ($("#assist-roles li").length == 0) 
		{ 
			PLMFlex.Assist.BindEvents.Notify("Please Add a Roles to save","attention");
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
			$( "#assist-usergroups, #agile-usergroups" ).sortable({
				connectWith: ".connectedSortable",
				stop: userGroupStopped
			}).disableSelection();
		});
		
		setHeights($userGroupsContainer,$contentboxcontent,$containerHeader);
		
		$(window).resize(function()
		{
			setHeights($userGroupsContainer,$contentboxcontent,$containerHeader);	
		});
		
	}
	function loadRoles($containerHeader,$contentboxcontent){
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
			$.each(jsonResponse.object,function(index, roleentry) { 
				// Iterate over the JSON array.
						if (roleentry.priority < 0) {
							$('<LI />').attr({id:("'"+roleentry.roleID+"'")}).addClass("ui-state-default").html(roleentry.Role).appendTo($agileRoles);
									
									
						/*			$('<li id='
									+ roleentry.roleID
									+ ' class="ui-state-default">')
									.text(roleentry.Role))
									.appendTo($agileRoles);*/
						} else {
							
							//working fix 1
							//$list=$('<li id='+ roleentry.roleID	+ ' class="ui-state-default"><div class="roleText"><span class="text">'+roleentry.Role+'</span></div></li>').appendTo($assistRoles);
							
							//working fix 2 
							var $list = $('<LI />').attr({id:("'"+roleentry.roleID+"'")}).addClass("ui-state-default")
													.html("<div class='roleText'><span class='text'>"+roleentry.Role+"</span></div>")
													.appendTo($assistRoles);
							
							//old AssistPlus Code not working							
							/*$list=$('<li id="'
									+ roleentry.roleID
									+ '" class="ui-state-default">')
									.html("<div class='roleText'> <span class='text'>"+roleentry.Role+"</span></div>").appendTo($assistRoles);
							*/		
							AssistColorPicker.createPicker($list.find('.roleText'),roleentry.fontColor,roleentry.backgroundColor);
						}
					});
			}
			$( "#assist-roles, #agile-roles" ).sortable({
				connectWith: ".connectedSortable",
				stop: stopped
			}).disableSelection();
		});
		
		setHeights($rolesContainer,$contentboxcontent,$containerHeader);
		
		$(window).resize(function(){
			setHeights($rolesContainer,$contentboxcontent,$containerHeader);	
		});
		
	}
	
	function setHeights($rolesContainer,$contentboxcontent,$containerHeader){
		$rolesContainer.eq(1).height($contentboxcontent.height()-$containerHeader.height());
		$rolesContainer.eq(2).height($contentboxcontent.height()-$containerHeader.height());
		$( "#assist-roles, #agile-roles" ).height($contentboxcontent.height()-$containerHeader.height());
	}
	
	function stopped(event,ui){
		if(ui.item.parents('ul#assist-roles').length > 0){
			if(ui.item.find('.colorSelector').length==0){
				var roleText= ui.item.text();
				ui.item.html("<div class='roleText'><span class='text'>"+roleText+"</span></div>");
				AssistColorPicker.createPicker(ui.item.find('.roleText'),'#ffffff','#333333')
			}
		}else{
				if(ui.item.length>0){
					if(ui.item.find('.colorSelector').length>0){
						var $textDiv=ui.item.find('.roleText span.text');
						var roleText=$textDiv.text();
						ui.item.html(roleText);
						ui.item.find(':last-child').remove();
					}
				}
		}
		
	}
	function userGroupStopped(event,ui)
	{
		console.info(ui);
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
	
	return{
		Config:Config,
		Roles:Roles,
		UserGroups:UserGroups,
		Password:Password
	};
	
})();

