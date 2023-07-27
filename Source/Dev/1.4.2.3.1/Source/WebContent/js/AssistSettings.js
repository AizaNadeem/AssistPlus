if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.Settings=(function(){
	
	var fontColor='';
	var backgroundColor='';
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
			fontColor=jsonResponse.fontColor;
			backgroundColor=jsonResponse.backgroundColor;
			var textDuration=jsonResponse.textDuration;	
			var indicateNewText=jsonResponse.indicateNewText;
			
			var html=PLMFlex.Assist.HTML.getConfigHTML;
			var $html=$(html);
			
			if(indicateNewText.value.toString()=="Yes")
			{
				$html.find('#durationBox').show();
				var $isNew=$html.find('input:radio[value='+indicateNewText.value.toString()+']');
				$isNew.prop('checked',true);		
				
			}
			else
			{
				$html.find('#durationBox').hide();
				var $isNew=$html.find('input:radio[value='+indicateNewText.value.toString()+']');
				$isNew.prop('checked',true);	
			}
			
			if(accessType && accessType.value=='usergroups')
			{
				accessTypeRole=false;
				updateLabels();
			}
			

			
			var $checkedDuration=$html.find('input:radio[value='+textDuration.value.toString()+']');
			$checkedDuration.prop('checked',true);			
			
			var $NodeTypeMaster=$html.find('#NodeTypeMaster');
			var $NodeTypeSlave=$html.find('#NodeTypeSlave');
			var $showNew=$html.find('#showNew');
			var $disableNew=$html.find('#disableNew');
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
			$showNew.change(function(){
				var val=$(this).val();
				if($showNew.is(':checked'))
				{
					$html.find('#durationBox').show();
			    }
				else
				{
					$html.find('#durationBox').hide();
				}
			});
			$disableNew.change(function(){
				var val=$(this).val();
				if($disableNew.is(':checked'))
				{
					$html.find('#durationBox').hide();
			    }
				else
				{
					$html.find('#durationBox').show();
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
				$("#data-import-export").hide();
			}
			else
			{
				$NodeTypeMaster.prop('checked',true);
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
 			
			AssistColorPicker.createPicker($html.find("#colorPalette"),fontColor.value.toString(),backgroundColor.value.toString());
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
			 var el = $("input:text").get(0);
			 var elemLen = el.value.length;
			 el.selectionStart = elemLen;
			 el.selectionEnd = elemLen;
			 el.focus();

			$contentboxcontent.css('padding','20px');
			$contentboxcontent.attr("id","");
			$contentboxcontent.outerHeight($(window).height()-110);
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

		if($("#AgileURL").val()=="" ) {
			PLMFlex.Assist.BindEvents.Notify("Please provide Agile Server URL","error");
			return;
		}			
		
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
			$("#data-import-export").hide();
		}
		else
		{
			$("#roles").show();
			$("#assistTextLi").show();
			$("#data-import-export").show();
		}
		configs[0]="hoverColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#hoverColor .colorSelector span').css('background-color'));
		configs[1]="fontColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#colorPalette .colorSelector span').css('color'));
		configs[2]="backgroundColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#colorPalette .colorSelector span').css('background-color'));
		
		var selectedVal = "";
		var selected = $("input:radio[name='duration']:checked");
		selectedVal = selected.val();
		configs[3]="textDuration"+"="+selectedVal;
	
		var selectedVal2 = "";
		var selected2 = $("input:radio[name='shownew']:checked");
		selectedVal2 = selected2.val();
		configs[4]="indicateNewText"+"="+selectedVal2;

		if(selectVal=='master')
		{
			var dataIndex=5;
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
				
				if(fontcolor==fontColor.value.toString() && background==backgroundColor.value.toString())
				{


					fontcolor=' ';
					background=' ';
				}
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
		//$contentboxcontent.css('height','0');
		
		PLMFlex.Assist.Request.get("loadConfig?",function(jsonResponse)
				{
						fontColor=jsonResponse.fontColor;
						backgroundColor=jsonResponse.backgroundColor;
				});
		
		var $saveRoles=$("<span id='saveRolesButton'  class='button saveRoles'>"+(accessTypeRole?"Save Roles":"Save User Groups")+"</span>");
		$contentboxcontent.parents('#main-content').find('.saveRoles').remove();
		$saveRoles.insertAfter( $contentboxcontent.parents('.content-box'));
		PLMFlex.Assist.BindEvents.saveRoles($saveRoles);
		
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Agile/Assist"+(accessTypeRole?" Roles":"Group(s)"),"attention");
		var $agileRoles=$rolesContainer.find('#agile-roles');
		var $assistRoles=$rolesContainer.find('#assist-roles');
		
		PLMFlex.Assist.Request.get("loadRoles?",function(jsonResponse){
			if(jsonResponse.hasOwnProperty("object")){
			$.each(jsonResponse.object,function(index, roleentry) 
			{ 
				
				// Iterate over the JSON array.
						if (roleentry.Priority < 0) {
							$('<LI />').attr({id:("'"+roleentry.RoleID+"'")}).addClass("ui-state-default").text(roleentry.Role).appendTo($agileRoles);
						} else {
							
							var $list = $('<LI />').attr({id:("'"+roleentry.RoleID+"'")}).addClass("ui-state-default")
													.html("<div class='roleText' >" +
													"<span class='text textExtend'></span>" +
													"</div>");
							$list.find(".text").text(roleentry.Role);
							$list.appendTo($assistRoles);
							
							if(roleentry.fontcolor==' ' && roleentry.background==' ')
								AssistColorPicker.createPicker($list.find('.roleText'),fontColor.value.toString(),backgroundColor.value.toString());
							else
								AssistColorPicker.createPicker($list.find('.roleText'),roleentry.fontcolor,roleentry.background);
							initialRoleList.push(roleentry.Role);
							
								
						}
					});
			
			}
			$assistRoles.find('.colorSelector').each(function()
			{
				if($.browser.msie && $.browser.version == "7.0")
				{
					$(this).css("margin-right","19px");
				}
			});
			
					
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
				var $list = $('<LI />').attr({id:($row.attr("id"))}).addClass("ui-state-default")
				.html("<div class='roleText' >" +
						"<span class='text textExtend'></span>" +
				"</div>");
				$list.find(".text").text($row.text());
				$list.appendTo('#assist-roles');
				AssistColorPicker.createPicker($list.find('.roleText'),fontColor.value.toString(),backgroundColor.value.toString());

			});	
			$('#agile-roles').children().remove();
			$rolesContainer=$('.rolesContainer');
			setHeights($rolesContainer,$('.content-box-content'),$('.content-box-header'));

		});
		
		$("#removeAll").click(function()
		{

			$('#assist-roles').find('li').each(function()
			{
				var $row=$(this);
				var $list = $('<LI />').attr({id:($row.attr("id"))}).addClass("ui-state-default");
				$list.text($row.text());
				$list.appendTo('#agile-roles');

			});
			
			$('#assist-roles').children().remove();
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
		
				AssistColorPicker.createPicker(ui.item.find('.roleText'),fontColor.value.toString(),backgroundColor.value.toString());
			    if($.browser.msie && $.browser.version == "7.0")
				{
					ui.item.find(".colorSelector").css("margin-right","17px");
				}
			}
		}else{
				if(ui.item.length>0)
				{
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
		
	function loadImport($contentboxcontent)
	{
		if($("#ImportLicLnk"))
		{
			$("#ImportLicLnk").hide();
		}

		$.browser.safari = ($.browser.webkit && !(/chrome/.test(navigator.userAgent.toLowerCase())));
		var html=PLMFlex.Assist.HTML.getImportDatabaseHTML;
		var $html=$(html);
				
		$html.find("#exportXML").hide();
		
		var $importAction=$html.find('#importAction');
		var $exportAction=$html.find('#exportAction');
		var $TypeOverWrite=$html.find('#TypeOverWrite');
		
		if ($.browser.msie || $.browser.safari)
		{
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
				if ($.browser.msie || $.browser.safari)
				{
					
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
				if ($.browser.msie || $.browser.safari)
				{
				
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
		$contentboxcontent.css('padding','20px');
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height()-110);
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
			Load:loadImport
	};
	var Export={
			Save:saveExport
	};
	return{
		Config:Config,
		Roles:Roles,
		Password:Password,
		Import:Import,
		Export:Export
	};
	
})();

