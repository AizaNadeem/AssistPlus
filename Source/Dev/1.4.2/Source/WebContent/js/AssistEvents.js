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
	
	function saveLicense($button){
		$button.click(function(){
			//PLMFlex.Assist.Settings.Config.Save($('.subcontent'));
			PLMFlex.Assist.LicRequest.post("activate?",function(jsonResponse){
				var $table = $('#licinfo-table');
				$table.children().remove();
				var _data = jsonResponse.object;
				var html = "";
				for(var prop in _data)
				{
					if(prop=='License Status')
						{
							
							LicStatus=_data[prop];
							if(LicStatus=='Valid')
								LicInfo=true;
							else
								LicInfo=false;
							//console.info(LicStatus);
						}
					html += "<tr><td style='width:30%;font-weight:bold; border:1px solid white; text-align:right;'>"+prop+"</td><td style='width:60%; border:1px solid white;'>"+_data[prop]+"</td></tr>";
				}
				
				$table.append(html);
				
			});
		});
	}
	
	
	function togglevisibility(iname)
	{
		//alert('in toggle LIC');
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
	function saveSelectedUserGroups($saveUserGroup){
		$saveUserGroup.click(function(){
			PLMFlex.Assist.Settings.UserGroups.Save();
		});
	}
	
	
	
	function LoadAttributes($tr){
		$tr.click(function(){
			var $this=$(this);
			var classID=$this.attr('id');
			var classLevel=$this.data('level');
			PLMFlex.Assist.AssistText.loadAttributes(classID,classLevel,$this);
		});
	}
	
	function LoadAssistText($tr){
		$tr.click(function(){
			var $thisRow=$(this);
			var data=$thisRow.data('attr');
			var classId=data.split(';')[0];
			var attributeId=data.split(';')[1];
			PLMFlex.Assist.AssistText.loadAssistText(classId,attributeId,$thisRow);
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
			
		},5000);
			
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
				var $this=$(this).find(findElement)
				if(findElement==""){
					$this=$(this);
				}
				
				if ($this.text().toLowerCase().indexOf($filter.val()) == -1) {
					if(this.tagName.toLowerCase()=="tr"){
						$(this).hide();	
					}else{
						$(this).slideUp();	
					}
				}
				else {
					if(this.tagName.toLowerCase()=="tr"){
						$(this).show();
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
		saveUserGroups:saveSelectedUserGroups,
		loadAttributes:LoadAttributes,
		loadAssistText:LoadAssistText,
		ChangePassword:changePWD,
		Filter:bindFilter,
		Notify:Notification
	}
	
})();


$(document).ready(function(){

	$contentboxcontent=$('.content-box-content');
	$contentboxheader=$('.content-box-header');
	
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
	$('#userGroups').click(function()
	{
		if(LicInfo)
			PLMFlex.Assist.Settings.UserGroups.Load($contentboxheader, $contentboxcontent);
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
	
	$('.login').click(function(){
		//alert(LicStatus);
		$(":input").html('');
		PLMFlex.Assist.Security.auth();
			
	});

	$('#lic').click(function(){
		//$contentboxcontent.html('');
		PLMFlex.Assist.License.Load($contentboxheader,$contentboxcontent);
		
	});
	
	$('#signout a').click(function(){
		LicStatus='';
		//PLMFlex.Assist.BindEvents.Notify("Please provide your Flex Assist Username and Password to Login.","info");
		$('.content-box-content').html('');
		$(":input").html('');
		window.location.reload();
		/*
		$('.content-box-content').html('');
		
		PLMFlex.Assist.BindEvents.Notify("Please provide your Flex Assist Username and Password to Login.","info");
		$('#login-wrapper').show();
		$('#login-wrapper #password').val('');
		
		*/
	});
	
	$(document).keypress(function(e) { 
	    if(e.keyCode == 13) { 
	        if($('#login-wrapper').is(':visible')){
	        	$('.login').click();
	        } 
	    } 
	});
	
	
	
});





