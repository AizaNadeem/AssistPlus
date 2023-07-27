if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.BindEvents=(function(){
	
	function saveSettings($button){
		$button.click(function(){
			PLMFlex.Assist.Settings.Config.Save($('.subcontent'));
		});
	}
	
	function saveLicense($button,$contentboxcontent){
		$button.click(function(){
			var accessType="";
			var $accessTypeRoles=$contentboxcontent.find('#AccessTypeRoles');
			var $accessTypeUserGroups=$contentboxcontent.find('#AccessTypeUserGroup');
			if($accessTypeRoles.is(':checked'))
				{
					accessType="roles";
				}
			else
				{
					accessType="usergroups";
				}
			if(accessType=="roles")
				accessTypeRole=true;
			else
				accessTypeRole=false;
			var isAccessType=false;
		
			PLMFlex.Assist.LicRequest.post("activate?accessType="+accessType,function(jsonResponse){
				var $table = $('#licinfo-table');
				$table.children().remove();
				var _data = jsonResponse.object;
				var html = "";
				for(var prop in _data)
				{
					if(prop=='isFirstTime')
					{
						isAccessType=true;
						continue;
					}
					if(prop=='accessType')
					{
						if(_data[prop]=='usergroups')
						{
							$("#roles").text("User Group Priority");
							$accessTypeUserGroups.prop('checked', true);	
							$accessTypeRoles.prop('checked', false);
						}
						else
						{
							$accessTypeRoles.prop('checked', true);
							$accessTypeUserGroups.prop('checked', false);
						}
						$accessTypeRoles.prop("disabled","disabled");
						$accessTypeUserGroups.prop("disabled","disabled");
						continue;
						
					}
					if(prop=='License Status : ')
						{
							LicStatus=_data[prop];
							if(LicStatus=='Valid')
								LicInfo=true;
							else
								LicInfo=false;
						}
					html += "<tr><td class='prop'>"+prop+"</td><td class='prop-data'>"+_data[prop]+"</td></tr>";
				}
				if(isAccessType)
				{
					var txtAccessType=$html.find('#AccessTypeRoles');
					var txtRoles=$html.find('#AccessTypeUserGroup');
					
					txtAccessType.prop('checked', true);
					txtAccessType.prop("disabled",false);
					txtRoles.prop("disabled",false);
					PLMFlex.Assist.BindEvents.Notify("Please choose Assist Text access type, default is Roles.","attention");	
				}
				
				$table.append(html);
				
			});
		});
	}
	
	
	function togglevisibility(iname)
	{
		
		if($('#'+iname).is(':hidden'))
			$('#'+iname).show();
		else
			$('#'+iname).hide();
	}
	
	function changePWD(){
		$('.pwdchange').click(function(){
			PLMFlex.Assist.Security.ChangePassword();
		});
	}
	
	function saveSelectedRoles($saveRoles){
		$saveRoles.click(function(){

			          PLMFlex.Assist.Settings.Roles.Save();
			       
		});
	}
	
	function ImportDB(){
		
		$('#link').click(function(){
			$('#exportXML').trigger('click');
			$('#download').hide();

		});
		$('.exportDB').click(function(){
			PLMFlex.Assist.Settings.Export.Save();

		});
		
		$('.importDB').click(function(){
			PLMFlex.Assist.Settings.Import.Save();
			$("#upload")[0].reset(); 
		});
		
		$('#chooseFile').click( function() { 
			$('#importPath').trigger('click'); 
		});
		$('#importPath').click( function() { 
			$('#importPath').val(""); 
		});
		document.getElementById('importPath').onchange = function () {
			var pathVal=this.value;
			var fileName=pathVal.split('\\').pop();
			$('#path').text(fileName);
			this.form.submit();
		}; 
	}
	
	function LoadAttributes($tr){
		$tr.click(function(){
			var $this=$(this);
			var classID=$this.attr('id');
			var classLevel=$this.data('level');
			PLMFlex.Assist.AssistText.loadAttributes(classID,classLevel,$this);
		});
	}
	function LoadWorkflows($tr){
		$tr.click(function(){
			var $this=$(this);
			var classID=$this.attr('id');
			var classLevel=$this.data('level');
			PLMFlex.Assist.AssistText.loadWorkflows(classID,classLevel,$this);
		});
	}
	
	function LoadAssistText($tr){
		$tr.click(function(){
			var $thisRow=$(this);
			var data=$thisRow.data('attr');
			var classId=$('#class-table tr.selected').attr('id');
			var attributeId=data;
			PLMFlex.Assist.AssistText.loadAssistText(classId,attributeId,$thisRow);
		});	
		$tr.bind("contextmenu", function(evt) {
			evt.preventDefault();
			$(this).trigger("click");
			setTimeout(function (){PLMFlex.Assist.AssistText.bindActions($('.addNewText'));},300);
			
		});	
		$tr.mouseenter(function(){
			var $this=$(this);
			var title=$this.attr('title');
			$this.removeAttr('title');
			setTimeout(function(){
				$this.attr('title',title);
				
			},1);
		});
		$tr.find(".attColorTd").mouseenter(function(){
			var $this=$(this);
			var title=$this.attr('title');
			$this.removeAttr('title');
			setTimeout(function(){
				$this.attr('title',title);
				
			},1);
		});
	}
	
	
	function Notification(text,type){
		
		if(typeof notifyTimeOut!=="undefined"){
			window.clearTimeout(notifyTimeOut);
		}
		if(type==undefined || type==''){
			type='success';
		}
		$notifyDiv=$('.notification');
		$notifyDiv.find('div').html(text);
		$notifyDiv.attr('class','notification');
		$notifyDiv.toggleClass(type);
		if(!$notifyDiv.is(":hidden")){
			$notifyDiv.hide();
		}
		$notifyDiv.fadeIn("slow");
		notifyTimeOut=setTimeout(function(){
			$notifyDiv.fadeOut("slow");
			
		},3000);
			
	} 
	
	function bindFilter($container,filterInputSelector,elementListSelector,findElement){
		var $filter=$container.find(filterInputSelector);
		
		$filter.val('Enter filter string');
		
		$filter.focusin(function(){
			
			if($filter.val()=="Enter filter string"){
				$filter.val('');
			}
		});
		
		$filter.focusout(function(){
			if($filter.val()==""){
				$filter.val("Enter filter string");
			}
		});
		
		$filter.keyup(function(){
			
			if($filter.val()=="Enter filter string"){
				return;
			}else{
			$(elementListSelector).each(function (i) 
			{
				var $this=$(this).find(findElement);
				if(findElement==""){
					$this=$(this);
				}
				if ($this.text().toLowerCase().indexOf($.trim($filter.val()).toLowerCase()) == -1) {
					if(this.tagName.toLowerCase()=="tr"){
					
						$(this).css('display','none');
					}else{
					
						$(this).slideUp();	
					}
				}
				else {
					if(this.tagName.toLowerCase()=="tr"){
				
						$(this).css('display','');
						
					}else{
						$(this).fadeIn();	
					}
					
				}
			});
			}
			
		});
	}
	
	return{
		saveConfig:saveSettings,
		saveLic:saveLicense,
		toggle:togglevisibility,
		saveRoles:saveSelectedRoles,
		loadAttributes:LoadAttributes,
		LoadWorkflows:LoadWorkflows,
		loadAssistText:LoadAssistText,
		ChangePassword:changePWD,
		Filter:bindFilter,
		Notify:Notification,
		ImportDB:ImportDB

	};
	
})();


$(document).ready(function(){

	$contentboxcontent=$('.content-box-content');
	$contentboxheader=$('.content-box-header');
	var $userName=$("#username");
	
	 var el = $("input:text").get(0); 
	
	 var elemLen = el.value.length;

	 el.selectionStart = elemLen;
	 el.selectionEnd = elemLen;
	 el.focus();
	 PLMFlex.Assist.Request.get("auth?",	function(responseJson) {
		$userName.val(responseJson.userName);
			
		}, "json");
	var licMsgs = {'error':'License File Error','invalid':'Please accept the License.'};
	
	$(window).bind('beforeunload',function(){
		$(":input").val('');
	});
	
	$('#config').click(function(){
		
		if(LicInfo)
			PLMFlex.Assist.Settings.Config.Load($contentboxcontent);
		else if(LicStatus != '')
			PLMFlex.Assist.BindEvents.Notify(licMsgs['invalid'],'info');
	});
	
	$('#roles').click(function()
	{
		if(LicInfo)
			PLMFlex.Assist.Settings.Roles.Load($contentboxheader, $contentboxcontent);
		else
			PLMFlex.Assist.BindEvents.Notify(licMsgs['invalid'],'info');
	});

	
	$('#changePwd').click(function(){
		if(LicInfo)
			PLMFlex.Assist.Settings.Password.Load($contentboxcontent);
		else
			PLMFlex.Assist.BindEvents.Notify(licMsgs['invalid'],'info');
	});
	
	$('#addText').click(function(){
		if(LicInfo)
			PLMFlex.Assist.AssistText.add($contentboxheader, $contentboxcontent);
		else
			PLMFlex.Assist.BindEvents.Notify(licMsgs['invalid'],'info');
	});
	
	$('.login').click(function()
	{
		if((this.className.indexOf("notification")) !== -1)
			return;
		$(":input").html('');
		PLMFlex.Assist.Security.auth();
			
	});

	$('#lic').click(function()
	{
		PLMFlex.Assist.License.Load($contentboxheader,$contentboxcontent);
		
	});
	$('.notification').click(function()
	{
				
				
	});
	$('#import').click(function(){
		if(LicInfo)
		{
			$("#ImportLicLnk").hide();
			PLMFlex.Assist.Settings.Import.Load($contentboxcontent);
		}
		else
			PLMFlex.Assist.BindEvents.Notify(licMsgs['invalid'],'info');
	});
	
	
		
	jQuery('#signout a').click(function(){
		if (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1)
		{
			LicStatus='';
			document.write("Signing Out...");
			window.location.reload(true);
		}
		else
		{
			LicStatus='';
			$('.content-box-content').html('');
			$(":input").html('');
			window.location.reload();
		}

	});
	
	
	$(document).keypress(function(e) { 
	    if(e.keyCode == 13) { 
	        if($('#login-wrapper').is(':visible')){
	        	$('.login').click();
	        } 
	    } 
	});
	
	
	
});





