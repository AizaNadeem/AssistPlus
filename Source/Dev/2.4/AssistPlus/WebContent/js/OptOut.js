if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.OptOut=(function(){
	
function loadUsers($contentboxheader,$contentboxcontent){
	$('#saveRolesButton').remove();
	
	//var _html=PLMFlex.Assist.LicHTML.getHTML;
	
	PLMFlex.Assist.Request.get("OptOut?useCase=getOptOut",function(jsonResponse)
	{
		var header="<h3>Following are the users who have opted out Assist+ help:</h3>";
		header=$(header);
		$contentboxheader.html(header);
		var html="<div class='settingsFilter'>" +
					"<div class='filter'>" +
					"<h3>Filter by Username:</h3>" +
						"<input type='text' id='filter1' onkeyup=\"if( event.keyCode == 27 )this.value='' \" />" +
					"</div>" +
					"<div class='filter'>" +
					"<h3>Filter by Opt Out Date:</h3>" +
						"<input  type='text' id='filter2'  onkeyup=\"if( event.keyCode == 27 )this.value='' \" />" +				
					"</div>" +
				"</div>";
		html += "<div class='CSSTableGenerator' ><table id='dataTable' class='tablesorter'>";
		html += "<thead><tr><th>Username</th><th>Opt Out Date</th></tr></thead><tbody>";
		//var resp = $.parseJSON(jsonResponse);
		var data=jsonResponse["data"];
		if(data!=undefined&&data!=null)
		{
			PLMFlex.Assist.BindEvents.Notify("Opt Out Data Loaded Successfully","success");
			Object.keys = Object.keys || function(o) { 
			    var keysArray = []; 
			    for(var name in o) { 
			        if (o.hasOwnProperty(name)) 
			          keysArray.push(name); 
			    } 
			    return keysArray; 
			};
	        if(Object.keys(data).length>0)
	        {
	        	for (var key in data) 
	        	{
		            if (data.hasOwnProperty(key)) 
		            {
		            	 html += "<tr><td class='username'> " + key + "</td><td class='date'> "
				          + data[key] + "</td></tr>"; 
		            }
		        }
	        }
	        else
	        	{
	        	html += "<tr><td>" + "No data Available" + "</td><td>"
		          + "No data Available" + "</td></tr>"; 
	        	}
		}
		else
			{
			var error=jsonResponse["error"];
			PLMFlex.Assist.BindEvents.Notify(error,"error");
			}

		

		$.extend($.expr[":"], {
			"containsIN": function(elem, i, match, array) {
			return (elem.textContent || elem.innerText || "").toLowerCase().indexOf((match[3] || "").toLowerCase()) >= 0;
			}
			});
		   

		html += "</tbody></table></div>";
		var $html=$(html);
		$contentboxcontent.html($html);
		 $("#dataTable").tablesorter(); 
		 $('#filter1').bind('keyup change',function() { 
	         $("#dataTable td.username:containsIN('" + $(this).val() + "')").parent().show();
	         $("#dataTable td.username:not(:containsIN('" + $(this).val() + "'))").parent().hide();
	     });
		 $('#filter2').bind('keyup change', function() { 
	        $("#dataTable td.date:containsIN('" + $(this).val() + "')").parent().show();
	        $("#dataTable td.date:not(:containsIN('" + $(this).val() + "'))").parent().hide();
	    });
				
			});
		
	}
	



return{
	Load:loadUsers
};
	
})();