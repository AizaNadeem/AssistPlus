if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.License=(function(){
	
function loadSettings($contentboxheader,$contentboxcontent){
	
	
	PLMFlex.Assist.LicRequest.get("activate?",function(jsonResponse){
		
		$('.saveRoles').hide();
		var _html=PLMFlex.Assist.LicHTML.getHTML;
		var $html=$(_html);
		$contentboxcontent.html($html);
		$contentboxcontent.css('padding','20px');
		var $button= $html.find('#ImportLicLnk');
		var $accessType=$html.find('#accessType');
		var accessType=$accessType.val();
		if(accessType=="roles")
			accessTypeRole=true;
		else
			accessTypeRole=false;
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
					}
					$accessType.val(_data[prop]);
					$accessType.prop("disabled","disabled");
					continue;
					
				}
				if(prop=='License Status')
					{
						LicStatus=_data[prop];
						if(LicStatus=='Valid')
							LicInfo=true;
						else
							LicInfo=false;
					}
				html += "<tr><td style='width:30%;font-weight:bold; border:1px solid white; text-align:right;'>"+prop+"</td><td style='width:60%; border:1px solid white;'>"+_data[prop]+"</td></tr>";
				
			}
			if(isAccessType)
			{
				var txtAccessType=$html.find('#accessType');
				
				txtAccessType.val("roles");
				txtAccessType.prop("disabled",false);
				PLMFlex.Assist.BindEvents.Notify("Please choose Assist Text access type, default is Roles.","attention");	
			}	
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