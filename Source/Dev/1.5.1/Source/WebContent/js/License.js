if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.License=(function(){
	
function loadSettings($contentboxheader,$contentboxcontent){
	
	var primaryURL;
	var _html=PLMFlex.Assist.LicHTML.getHTML;
	var $html=$(_html);
	PLMFlex.Assist.Request.get("loadConfig?",function(jsonResponse)
			{				
				
				primaryURL=jsonResponse.PrimaryInstanceURL;
				var $licRow=$html.find('#licRow');
				if(primaryURL.value && primaryURL.value!="")
				{						
					$licRow.hide();
					$licRow.find(".licStarText").hide();
				}
				else
				{
					$licRow.show();
					$licRow.find(".licStarText").show();
				}					
				
			});
	PLMFlex.Assist.LicRequest.get("activate?",function(jsonResponse){
		
		$('.saveRoles').hide();		
		
		
		$contentboxcontent.html($html);	
		$contentboxcontent.css('padding','20px');
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height()-110);
		
		var $button= $html.find('#ImportLicLnk');
		var $accessTypeRoles=$contentboxcontent.find('#AccessTypeRoles');
		var $accessTypeUserGroups=$contentboxcontent.find('#AccessTypeUserGroup');
		
		PLMFlex.Assist.BindEvents.saveLic($button,$contentboxcontent);
		var $terms= $html.find('#terms');
		$terms.bind('click', function(){PLMFlex.Assist.BindEvents.toggle('licframe');});
	
		if (jsonResponse.hasOwnProperty("object"))
		{
			var $table = $('#licinfo-table');
			$table.children().remove();
			var _data = jsonResponse.object;
			var html = "";
			var isAccessType=false;
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
						$accessTypeRoles.prop("checked", "checked");
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
			var accessType="";
			if($accessTypeRoles.is(':checked'))
				{
					accessType="roles";
				}
			if(accessType=="roles")
				accessTypeRole=true;
			else
				accessTypeRole=false;
			
			$table.append(html);
		}
		
		else{
			LicStatus='error';
		}
		
		});
	
		
	}
	



return{
	Load:loadSettings
};
	
})();