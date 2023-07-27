if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.Search=(function() {
	function load($containerHeader, $contentboxcontent) {
		var $containerHTML=$(PLMFlex.Assist.HTML.SearchText);
		$contentboxcontent.html($containerHTML);
		document.getElementById("replaceText").disabled = true;
		document.getElementById("replaceAssistText").disabled = true;
		$('#dialog-form').hide();
		//var searchText=$('#searchText').val();
		$("#searchAssistText").unbind('click');
		$('#searchAssistText').click(function()
		{
			 var searchText=$containerHTML.find("#searchText").val();
			 $("#report-grid").css('display','none');
			// var searchText =document.getElementById("searchText").value;
			//var searchText = $("#searchText").val();
//			$( "searchText" ).keyup(function() {
//			    searchText = $( this ).val();
//			  });
			 console.log("Search text is: "+searchText);
			if(searchText=='' ||searchText=='undefined') {
				PLMFlex.Assist.BindEvents.Notify("Search field is empty","error");
				document.getElementById("replaceText").disabled = true;
				document.getElementById("replaceAssistText").disabled = true;
				return;
			}
			
			PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are searching.","attention","enable");
			PLMFlex.Assist.Request.post("SearchAssistText",
			{'text': searchText},function(ressponseJson)
				{
				$("replaceText").val('');
				var retStatus = ressponseJson.retStatus;
				var searchCount=ressponseJson.searchCount;
				
					PLMFlex.Assist.BindEvents.LongNotify("Please wait while we are searching.","attention","disable");
					
					if(retStatus.status=="error")
						{
						PLMFlex.Assist.BindEvents.Notify(retStatus.message,retStatus.status);
						document.getElementById("replaceText").disabled = true;
						document.getElementById("replaceAssistText").disabled = true;
						return;
						}
					if(searchCount=='0')
						{
						PLMFlex.Assist.BindEvents.Notify("No entries found", "attention");
						document.getElementById("replaceText").disabled = true;
						document.getElementById("replaceAssistText").disabled = true;
						return;
						}
					document.getElementById("replaceText").disabled = false;
					document.getElementById("replaceAssistText").disabled = false;
						var statistis=ressponseJson.existingEntries;
						var count=0;
						PLMFlex.Assist.BindEvents.Notify("Total number of entries returned= "+searchCount+".", "attention");
						setTimeout(function() {
							$("#report-grid").css('display','block');
							$("#report-grid").empty();
		
							var config = {
								sortable : true,
								filterable : true,
								columnMenu : false,
								resizable : true,
								reorderable : false,
								scrollable : true,
								dataSource : {
									data: statistis
								},
								pageable : false,
								toolbar: ["excel"],
								excel : {
									fileName : "Assist+ Search Results.xlsx",
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
											assistText : {
												
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
									width : 100
								},	{
									field : "attrId",
									title : "Attribute Name",
									type : "string",
									width : 100
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
									field : "assistText",
									title : "Assist Text",
									width : 200,
									type : "string",
									width : 200
								},
								{	command: 
									{
									text: "Replace", 
									click: showDetails }, 
									title: " ", 
									width: "180px" 
								}
							]
							};
		
							$("#report-grid").kendoGrid(config);
						}, 100);
						var textid=[];
						var assistText=[];
						
						for (i = 0; i < ressponseJson.existingEntries.length; i++) {
							var obj = ressponseJson.existingEntries[i].textid;
							textid.push(obj);
							assistText.push(ressponseJson.existingEntries[i].assistText);
						}
						
						
	
						 function showDetails(e)
						 {
							$('#dialog-form').show();
							dialog = $( "#dialog-form" ).dialog(
								 {
								      autoOpen: false,
								      height: 200,
								      width: 350,
								      modal: true,
								      buttons: 
								      {
								        "Replace with": replace,
								        Cancel: function() 
								        {
								          dialog.dialog( "close" );
								        }
								      },
							      close: function() {
							    	$(this).find("#replaceTextDilog").val('');
							        allFields.removeClass( "ui-state-error" );
							      }
							    });
							var dataItem = this.dataItem($(e.currentTarget).closest("tr"));
							var textid2=[dataItem.textid];
			                var assistText2=[dataItem.assistText];
			                text = $( "#replaceTextDilog" ),
						    allFields = $( [] ).add( text );
			                console.log("Text ID" +textid);
			                console.log("into replace button function");
			                dialog.dialog( "open" );
			                
							function  replace(){
			                    	var replaceText=$(this).find("#replaceTextDilog").val();
			                    	if(replaceText=='' ||replaceText=='undefined') 
			                    	{
			                    		dialog.dialog( "close" );
										PLMFlex.Assist.BindEvents.Notify("Replace Text field is empty","error");
										return;	
									}
			                    	console.log("Replacement text is: "+replaceText);
			                    	PLMFlex.Assist.Request.post("ReplaceAll",
											{'text': replaceText,'oldText[]': assistText2, 'searchText': searchText, 'textid[]': textid2 },function(ressponseJson)
												{
												retStat=ressponseJson.retStatus;
												PLMFlex.Assist.BindEvents.Notify(retStat.message,retStat.status);
												$containerHTML.find("#replaceText").val('');
												$containerHTML.find("#searchText").val('');
												
//												PLMFlex.Assist.BindEvents.Notify("Successfully replaced","success");
												 $("#report-grid").css('display','none');
												});
			                    	
			                    	dialog.dialog( "close" );
			                    	
			                    }
			               }
						 
						 $("#replaceAssistText").unbind('click');
						 $('#replaceAssistText').click(function()
								{
									var textid1=[];
									var assistText1=[];
									for (i = 0; i < ressponseJson.existingEntries.length; i++) {
										var obj = ressponseJson.existingEntries[i].textid;
										textid1.push(obj);
										assistText1.push(ressponseJson.existingEntries[i].assistText);
										
									}
									var searchText=$containerHTML.find("#searchText").val();
									if(searchCount=='0')
									{
									PLMFlex.Assist.BindEvents.Notify("No entries found to be replaced", "attention");
									$containerHTML.find("#replaceText").val('');
									$containerHTML.find("#searchText").val('');
									document.getElementById("replaceText").disabled = true;
									document.getElementById("replaceAssistText").disabled = true;
									return;
									}
									var replaceText=$containerHTML.find("#replaceText").val();
									if(replaceText=='' ||replaceText=='undefined') {
										PLMFlex.Assist.BindEvents.Notify("Replace Text field is empty","error");
										
										return;
									}
									console.log("Replacement text is: "+replaceText);
									PLMFlex.Assist.Request.post("ReplaceAll",
											{'text': replaceText,'oldText[]':assistText1, 'searchText':searchText, 'textid[]': textid1 },function(ressponseJson)
												{
												$containerHTML.find("#replaceText").val('');
												$containerHTML.find("#searchText").val('');
												document.getElementById("replaceText").disabled = true;
												document.getElementById("replaceAssistText").disabled = true;
												$("#report-grid").css('display','none');
												retStat=ressponseJson.retStatus;
												PLMFlex.Assist.BindEvents.Notify(retStat.message,retStat.status);
												});
									
									
								});
							
						
				
					
				});
		});

	}
	return{
		Load: load
	};
	
})();

