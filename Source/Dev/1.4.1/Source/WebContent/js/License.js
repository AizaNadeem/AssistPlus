if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.License=(function(){
	
function loadSettings($contentboxheader,$contentboxcontent){
	
	
	PLMFlex.Assist.LicRequest.get("activate?",function(jsonResponse){
		
		//$('#main-content').show();
		var _html=PLMFlex.Assist.LicHTML.getHTML;
		var $html=$(_html);
		$contentboxcontent.html($html);
		$contentboxcontent.css('padding','20px');
		var $button= $html.find('#ImportLicLnk');
		PLMFlex.Assist.BindEvents.saveLic($button);
		var $terms= $html.find('#terms');
		$terms.bind('click', function(){PLMFlex.Assist.BindEvents.toggle('licframe');});
		if (jsonResponse.hasOwnProperty("object"))
		{
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
					}
				html += "<tr><td style='width:30%;font-weight:bold; border:1px solid white; text-align:right;'>"+prop+"</td><td style='width:60%; border:1px solid white;'>"+_data[prop]+"</td></tr>";
			}
			
//			if(LicStatus!='Valid' || !_status)
//			{
//				$('#main-content').show();
//				$table.append(html);
//			}
//			else
//			{
//				$("#main-nav li a.nav-top-item").eq(0).click();
//			}
//			_status = false;
//			//$.tmpl( "licRow", jsonResponse.object).appendTo( $table );
			
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