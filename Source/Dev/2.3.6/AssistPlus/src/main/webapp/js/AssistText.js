if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist == 'undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.AssistText = (function() {
	var fontColor = '';
	var backgroundColor = '';
	
	function AddText($containerHeader, $contentboxcontent) {
		var $containerHTML=$(PLMFlex.Assist.HTML.ClassesHTML.classesHTML((accessTypeRole?"Role(s)":"User Group(s)")));
		$('.saveRoles').remove();
		PLMFlex.Assist.BindEvents.Filter($containerHTML,".classFilter","#class-table tr","td:first-child");
		PLMFlex.Assist.BindEvents.Filter($containerHTML,".attFilter","#attr-attribute tr","td:first-child a");
		
		$contentboxcontent.html($containerHTML);
		$contentboxcontent.css('padding','0');
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height()-110);

		$('#saveColorCodes').click(function()
				{
			PLMFlex.Assist.BindEvents.Notify("Please wait while we are Saving Assist Color(s).","attention");
			var $attTable=$("#attr-attribute");
			var rowIndex=0;

			var classId='';
			var attColorJsonArr=[];

			$attTable.find('tr').each(function()
					{
				var $thisRow=$(this);
				var attColorJson={'colorId':'','attId':'','assistColor':''};
				var attColorArr=$thisRow.data('attrcolors').split(';');
				var attMetaArr=$thisRow.data('attr');

				var fontC=AssistColorPicker.getHexColor($thisRow.find('.colorSelector button').css('color'));

			//	classId=attMetaArr[0];
				classId=$('#class-table tr.selected').attr('id');
				attColorJson['colorId']=attColorArr[0];
				attColorJson['attId']=attMetaArr;
				attColorJson['assistColor']=fontC;

				attColorJsonArr[rowIndex]=attColorJson;
				rowIndex++;

					});

			PLMFlex.Assist.Request.post("AssistColor",
					{
				classId:classId,
				attColors:JSON.stringify(attColorJsonArr),
				rnd: Math.random()
					},function(jsonResponse)
					{
						PLMFlex.Assist.BindEvents.Notify("Assist Color(s) saved successfully.","success");
					});


				});

		$('.addNewText').click(function()
				{
			bindActions($(this));
				});
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Agile Classes","attention");
		PLMFlex.Assist.Request.get("loadClasses?isNotifText=false",function(jsonResponse){

			if (jsonResponse.hasOwnProperty("object"))
			{
				var $table = $('#class-table');
				$table.children().remove();
				$.tmpl( "classRow", jsonResponse.object ).appendTo( $table );

				bindRowHover($table);
				PLMFlex.Assist.BindEvents.loadAttributes($table.find('tr'));
				$table.treeTable();	
			}
		});
		
		setHeights($containerHTML,$contentboxcontent,$containerHeader);

		$(window).resize(function(){
			
			$('#text-table tr').not('#text-table tr tr').each(function(index)
			{
					if(index%2==0)
						return true;
					var atRow = $(this);
					var width=atRow.find("#rowText").width();
					if(width<200)
					{
						$(".tableAssistText").width('200px');
						width=200;
					}
					else
					{
						$(".tableAssistText").width('');
					}
					var height=atRow.find("#rowText").get(0).scrollHeight;
					
					atRow.find("#transparentDiv").width(width+'px');
					atRow.find("#transparentDiv").height(height+'px');
							
			});
			
			setHeights($containerHTML,$contentboxcontent,$containerHeader);	
		});
	}
	
	function AddNotifText($containerHeader, $contentboxcontent) {
		var $containerHTML=$(PLMFlex.Assist.HTML.ClassesNotifHTML.classesNotifHTML((accessTypeRole?"Role(s)":"User Group(s)")));
		$('.saveRoles').remove();
		$contentboxcontent.html($containerHTML);
		$contentboxcontent.css('padding','0');
		$contentboxcontent.attr("id","");
		$contentboxcontent.outerHeight($(window).height()-110);

		PLMFlex.Assist.BindEvents.Filter($containerHTML,".classFilter","#class-table tr","td:first-child");		
		$('#assistClassMessage').hide();
		if (tinymce.get('AssistClassMessage')) {
  			tinymce.get('AssistClassMessage').remove();
		}		
		tinymce.init({
			selector: 'textarea#AssistClassMessage',
			height: 145,
			width: 505,
			theme: 'modern',
			menubar: false,
			branding: false,
		    resize: false,
		    setup: function (editor) {
			    editor.on('change', function () {
			      var content = editor.getContent();
			      if (content.length > 500) {
			        editor.undoManager.undo();
					PLMFlex.Assist.BindEvents.Notify("Assist message should be less than 500 characters", "error");
			      }
			    });
			},
			plugins: 'anchor autolink code contextmenu help insertdatetime link paste preview searchreplace table',
	        fontsize_formats: '8pt 10pt 11pt 12pt 14pt',
	        font_formats: 'Arial=arial,helvetica,sans-serif;Courier New=courier new,courier,monospace;Helvetica=helvetica;Times New Roman=times new roman,times',
			formats: {
				bold: {inline: 'b'},
	            italic: {inline: 'i'},
	            underline: {inline: 'u'}
		    },
		    extended_valid_elements: 'b,i,b/strong,i/em',
		    paste_retain_style_properties: 'border border-left border-right border-bottom border-top font-size text-decoration text-align',
			paste_remove_styles_if_webkit: false,
			paste_data_images: false,
		    toolbar1: 'newdocument | bold underline italic strikethrough | alignleft aligncenter alignright alignjustify',
		    toolbar2: 'cut copy pastetext | undo redo | link unlink anchor insertdatetime | removeformat',
		   	toolbar3: 'fontselect fontsizeselect | help code preview | searchreplace',
		    content_css: ["css/tinymce-content.css"]
		});
		var $button = $('#saveClassNotification');		
		$button.click(function() {
			bindClassActions($(this));
		});
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Agile Classes","attention");
		PLMFlex.Assist.Request.get("loadClasses?isNotifText=true",function(jsonResponse){

			if (jsonResponse.hasOwnProperty("object"))
			{
				var $table = $('#class-table');
				$table.children().remove();
				$.tmpl( "classRow", jsonResponse.object ).appendTo( $table );

				bindRowHover($table);
				$table.treeTable({
					onClick: true,
				});
				PLMFlex.Assist.BindEvents.loadAssistMessage($table.find('tr'));	
			}
		});
		
		setHeights($containerHTML,$contentboxcontent,$containerHeader);

		$(window).resize(function(){			
			setHeights($containerHTML,$contentboxcontent,$containerHeader);	
		});
	}

	function setHeights($containerHTML,$contentboxcontent,$containerHeader){
		
		$containerHTML.eq(1).height($contentboxcontent.height()-$containerHeader.height());
		$containerHTML.eq(2).height($contentboxcontent.height()-$containerHeader.height());
		$( "content-box .left" ).height($contentboxcontent.height()-$containerHeader.height());
		$('#assistClassMessage').height( $(".rightMain").height());
		$('#attr-div').height( $(".rightTop").height()-$('#attr-header').height());
		$('#text-div').height( $(".rightBottom").height()-42);
	}
	var workflows=[];
	var isRoutable = null;
	var $AttContainer;
	function loadAttributes(classId, classLevel,$this) 
	{
		$('#class-table tr').removeClass('selected');
		$AttContainer=$("#attr-attribute");
		$('#text-table tbody').remove();
		var height=$('#attr-div').height();
		var width=$('#attr-div').width();
		$AttContainer.children().remove();

	
		width=width/2;
		width=width-69;
		height=height/4;
		$('#attr-div').find("#waitingDiv").remove();
		$('#attr-div').append('<div id="waitingDiv" ><img src="img/loading1.gif" class="waiting" /></div> ');		
		$(".waiting").css("margin-left",width+"px");
		$(".waiting").css("margin-top",height+"px");
		$AttContainer.css("background-color", "white");
		$this.addClass('selected');
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Class Attributes","attention");
		PLMFlex.Assist.Request.get("loadAttributes?classId=" + classId + "&level="+ classLevel ,function(jsonResponse){
			$('#attr-div').remove('#waitingDiv');
			$("#waitingDiv").remove();
			$AttContainer.children().remove();
			if (jsonResponse.hasOwnProperty("object"))
			{
				var attributes=jsonResponse.object.attributes;
				workflows=jsonResponse.object.workflows;
				isRoutable = jsonResponse.object.isRoutable;
				if(!workflows)
				{
					workflows=[];
				}
		
				$.tmpl( "attributeRow", attributes).appendTo( $AttContainer );
				
				bindRowHover($AttContainer);
				$('#attr-div').height( $(".rightTop").height()-$('#attr-header').height());
				PLMFlex.Assist.BindEvents.loadAssistText($AttContainer.find('tr'));

				$AttContainer.find('tr').each(function()
				{
					var row=$(this);
					var assistColor=row.data('attrcolors').split(";")[1];
					$(this).find('.attColorTd').each(function()
					{
						if(assistColor!="")
						{
							AssistColorPicker.createFontColorPicker($(this),assistColor,'#ffffff');
						}
						else
						{
							AssistColorPicker.createFontColorPicker($(this),'#666','#ffffff');
							$(this).find('.picker').css('background-color',"");
						}
					});
					$(this).find('.picker').each(function()
					{
						$(this).css('border',"0px");
						$(this).css('padding',"1px");
						$(this).css('margin-top',"1px");
						$(this).html("Label");
					});
					$(this).find('.colorSelector ').each(function()
					{
						$(this).css('width',"70px");
						$(this).css('padding',"0px");
						$(this).css('margin',"0px");
					});
				});
			}
		});
	}
	
	function loadAssistMessage(classId, classLevel,$this) 
	{
		$('#assistClassMessage').hide();
		function setEditorContent(notificationMsg) {
		    var editor = tinymce.get('AssistClassMessage');
		    if (editor && editor.initialized) {
		      if(notificationMsg !== 'null' && notificationMsg !== null) {
  					editor.setContent(notificationMsg+"");
				}
				else {
					editor.setContent("");
				}
				editor.save();
		    } else {
		      setTimeout(function() {
		        setEditorContent(notificationMsg);
		      }, 100);
		    }
		}
		
		$('#class-table tr').removeClass('selected');
		$this.addClass('selected');
		PLMFlex.Assist.Request.get("ClassNotificationMsgBar?classId=" + classId + "&level="+ classLevel + "&isAdmin=true" ,function(jsonResponse){
			$('#assistClassMessage').show();
			var notificationMsg = jsonResponse.classNotificationMsg;
			var enableNotification = jsonResponse.isClassNotifEnabled;
			if(enableNotification!=null && enableNotification!="null"){
				$('#enableClassNotification input[type=radio][value=' + enableNotification + ']').prop('checked', true);
				setTimeout(function() {
	      			setEditorContent(notificationMsg);
	   			}, 100);
			} else {
				$('#enableClassNotification input[type=radio]').prop('checked', false);
				setTimeout(function() {
	      			setEditorContent(notificationMsg);
	   			}, 100);
			}
		});
	}
	
	function bindRowHover($table)
	{
		$table.find('tr').hover(
				function(){
					$(this).css("background-color","#E8E8E8");
				},function(){
					$(this).css("background-color","");
				});
	}
	function LoadAssistText(classId,attributeId,$this){
		PLMFlex.Assist.Request.get("loadConfig?",function(jsonResponse)
				{
						fontColor=jsonResponse.fontColor;
						backgroundColor=jsonResponse.backgroundColor;
						
				});
		$('#attr-attribute tr').removeClass('selected');
		var $textContainer=$('#text-table');
		$('#text-table tbody').remove();
		$this.addClass('selected');
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Assist Text","attention");

		PLMFlex.Assist.Request.get("loadAssistTexts?classId=" + classId + "&attrId="+ attributeId + "&isRoutable=" + isRoutable,function(jsonResponse){
			
			$('#text-table tbody').remove();
			$.tmpl( "textRow", jsonResponse.object ).appendTo($textContainer);
			var textArr=jsonResponse.object;
			var textObj={};
			var selectElem=null;			
			var wfSelectHtml;
			
			if(isRoutable == undefined){
				
			} else if(!isRoutable){
				wfSelectHtml="<option value='-1' selected='selected'>Select Lifecycle</option>";
			} else if(isRoutable){
				wfSelectHtml="<option value='-1' selected='selected'>Select Workflow</option>";
			}
			
		//	wfSelectHtml+="<option value='"+"AllWorkflows"+"'>"+"All Workflows"+"</option>";
			var wfObj={};
			for(var wfIndex=0;wfIndex<workflows.length;wfIndex++)
			{
				wfObj=workflows[wfIndex];

				wfSelectHtml+="<option value='"+wfObj.id+"'>"+wfObj.value+"</option>";
			}
			if(textArr.length==0)
			{
				var $thisrow=$AttContainer.find('tr.selected');
				$thisrow.attr('title','Right click to add text.');
				$thisrow.find("#textImage").remove();
				if($AttContainer.find('#HasAssistText #textImage').length == 0) {
					$('#class-table tr.selected #hasTextFlag').empty();
				}
			}
			for(var txtIndex=0;txtIndex<textArr.length;txtIndex++)
			{
				textObj=textArr[txtIndex];
				PLMFlex.Assist.AssistText.drawWorkflowSelect(textObj,"#workflowSelect","#statusSelect",$this);
				PLMFlex.Assist.AssistText.drawRolesSelectText(textObj,"#roleop");
			}
			
			binRowdActions(textArr); 
		});
		
		$headerDiv = $('.header-wrapper');
		$rowDiv = $('#text-div');
		$rowDiv.scroll(function(e) {
		    $headerDiv.css({
		        left: -$rowDiv[0].scrollLeft + 'px'
		    });
		});

	}
	
	function drawRolesSelect(textObj,roleOptions)
	{		
		var roleElem=$(roleOptions+textID);
		var roles=textObj.roles;
		var rolesHtml="";
		for( var i=0;i<roles.length;i=i+2)
		{
				rolesHtml+="<option value='"+roles[i]+"'>"+roles[i+1]+"</option>";
		}		
		roleElem.html(rolesHtml);		
	}
	
	function drawRolesSelectText(textObj,roleOptions)
	{
		if(roleOptions=="#roleop")
		{
			var roleElem=$(roleOptions+textID);
		}
		else
		{
			var roleElem=$(roleOptions);
		}
		var roles=textObj.roles;
		var rolesHtml="";
		for( var i=0;i<roles.length;i=i+3)
		{
			if(roles[i+2]!=null && roles[i+2]=="selected")
			{
				rolesHtml+="<option value='"+roles[i]+"' selected='selected'>"+roles[i+1]+"</option>";
			}
			else
			{
				rolesHtml+="<option value='"+roles[i]+"'>"+roles[i+1]+"</option>";
			}
		}		
		roleElem.html(rolesHtml);		
	}
	
	function drawWorkflowSelect(textObj,cmbWorkflow,cmbStatus,row)
	{
		textID=textObj.TextID;
		if(cmbWorkflow!="#workflowSelect")
		{textID="";}
		selectElem=$(cmbWorkflow+textID);

		if(row.data('isworkflow')==true||!workflows || workflows.length==0) //disabled
		{
			$(cmbStatus+textID).attr("disabled","disabled");
			var wfDisabled="<option value='' selected='selected'>No Workflow / Lifecycle</option>";
			selectElem.html(wfDisabled);
			selectElem.attr("disabled","disabled");
			return;
		}
		else
		{
			$(cmbStatus+textID).removeAttr('disabled');
			selectElem.removeAttr('disabled');
			if(isRoutable == undefined){
				
			}
			else if(!isRoutable){
				wfSelectHtml="<option value='-1' selected='selected'>Select Lifecycle</option>";
			} else if(isRoutable){
				wfSelectHtml="<option value='-1' selected='selected'>Select Workflow</option>";
			}
	//		wfSelectHtml+="<option value='"+"All Workflows"+"'>"+"All Workflows"+"</option>";
			var wfObj={};

			for(var wfIndex=0;wfIndex<workflows.length;wfIndex++)
			{
				wfObj=workflows[wfIndex];
				wfSelectHtml+="<option value='"+wfObj.id+"'>"+wfObj.value+"</option>";
			}
			selectElem.html(wfSelectHtml);

			selectElem.attr("data-selectedStatus",textObj.workflowStatusId);
		}

		


		selectElem.attr("data-Textid",textID);

		

		selectElem.change(function()
				{
		
			var wfSelect=$(this);
			var textId=wfSelect.attr("data-Textid");
			var wfstID=wfSelect.attr("data-selectedStatus");		
			
			var selectedOption=wfSelect[0].options[wfSelect[0].selectedIndex];			
			var selectedWf=wfSelect.val();			
			
			if(isNew!="new" && selectedWf=="-1" )
			{
				 for(var i=0; i <wfSelect[0].options.length; i++)
				  {
				    if(wfSelect[0].options[i].value == "All Workflows") 
				    {
				    	wfSelect[0].selectedIndex = i;
				    	break;
				    }
				  }
				
			}
			selectedOption=wfSelect[0].options[wfSelect[0].selectedIndex];
			selectedWf=wfSelect.val();	
			
			var statuses=[];
			if(selectedWf == "All Workflows"||selectedWf == ""||selectedWf=="-1")
			{
				$(cmbStatus+textId)[0].disabled=true;
				$(cmbStatus+textId).html("");
				return;
			}
			else
			{
				$(cmbStatus+textId)[0].disabled=false;
			}
			for(var wfIndex=0;wfIndex<workflows.length;wfIndex++)
			{
				var wfObj=workflows[wfIndex];

				if(wfObj.id==selectedWf)
				{
					statuses=wfObj.childs;
					break;
				}
			}
			var statusObj={};
			var statusOptions="";
			
			if(isRoutable != undefined &&  isRoutable){
				statusOptions+="<option id='"+"All Statuses"+"'>"+"All Statuses"+"</option>";
			}
			

			for(var sIndex=0;sIndex<statuses.length;sIndex++)
			{
				statusObj=statuses[sIndex];
				statusOptions+="<option id='"+statusObj.id+"'>"+statusObj.value+"</option>";
			}

			$(cmbStatus+textId).html(statusOptions);
			if(textObj.workflowStatusId)
			{var wfstID=textObj.workflowStatusId;

			for (var i = 0; i < $(cmbStatus+textId)[0].options.length; i++) 
			{
				if (wfstID.indexOf($(cmbStatus+textId)[0].options[i].text/*.replace(/\s/g, "")*/) !=-1) 
				{
					$($(cmbStatus+textId)[0].options[i]).attr("selected", "selected");
				}
			}
			}
				});
		
		if(textObj.workflowID)	
		{var wfID=textObj.workflowID;
		for (var i = 0; i < selectElem[0].options.length; i++) 
		{
			if (selectElem[0].options[i].text == wfID) {
				selectElem[0].selectedIndex = i;
				break;
			}
		}
		}
		selectElem.trigger('change');
	}
	function UpdateText(){

	}

	function binRowdActions(dataObj){
		$('#text-table tr .save, #text-table tr .remove').click(function(){
			bindActions($(this));
		});
		bindRowEvents();
		$('#text-table tr').not('#text-table tr tr').each(function(index)
			{
			if(index%2==0)
				return true;
			var atRow = $(this);
			var width=atRow.find("#rowText").width();
			var height=atRow.find("#rowText").get(0).scrollHeight;
		
			if(width<200)
			{
				$(".tableAssistText").width('200px');
				width=200;
			}
			else
			{
				$(".tableAssistText").width('');
			}
			atRow.find("#transparentDiv").width(width+'px');
			atRow.find("#transparentDiv").height(height+'px');
			atRow.find("#rowText").click(function() {
				action = '';
				var textRowIndex = $(this).parent().parent().index();
				if(textRowIndex == 0)
					PLMFlex.Assist.AssistText.EditText($(this),dataObj[textRowIndex]);
				else
					PLMFlex.Assist.AssistText.EditText($(this),dataObj[textRowIndex/2]);
			});
					
		});


	}
	var txtId='';
	var uniqueFlag=false;
	var action='';
	var isNew='';
	function bindActions($this)
	{
		var entryList=new Array;
		var duplicateEntryFound=false;
		
		var rowIndex = -1;
		try {
			rowIndex = $this.parent().parent().index();
			rowIndex++;
		} catch(ex) {
			rowIndex = -1;
		}

		var $textTableRows = $('#text-table tr').not('#text-table tr tr');
		if(rowIndex > 0) {
			atRow = $($textTableRows[rowIndex]);
			atRow.find(".roleList :selected").each(function(i,role) {
				var wfComboObj=$(atRow.find(".wfcombo :selected")).val();
				if(!isRoutable || wfComboObj=="All Workflows" || wfComboObj=="No Workflow" || wfComboObj=="") {
					entry="["+wfComboObj+"][]["+role.value+"]";
					if($.inArray(entry, entryList) == -1) {
						entryList.push(entry);
					} else {										
						duplicateEntryFound=true;
					}
				} else {
					atRow.find(".statuscombo :selected").each(function(i,status) {
						var statusSelObj=$(status);
						entry="["+wfComboObj+"]["+statusSelObj.val()+"]["+role.value+"]";
						if($.inArray(entry, entryList)==-1) {
							entryList.push(entry);
						} else {										
							duplicateEntryFound=true;
						}
					});
				}
			});
		}
		
		$textTableRows.each(function(index) {
			if(index%2==0 || index==rowIndex)
				return true;
			atRow = $(this);
			atRow.find(".roleList").find('option').each(function(i,role) {
				var outerhtml = $("<div />").append($(role).clone()).html();
				outerhtml = outerhtml.substring(0, outerhtml.indexOf('>'));
				if(outerhtml.indexOf('selected') < 0) {
					return true;
				}
				
				var wfComboObj=$(atRow.find(".wfcombo :selected")).val();

				if(!isRoutable || wfComboObj=="All Workflows" || wfComboObj=="No Workflow" || wfComboObj=="")
				{
					entry="["+wfComboObj+"][]["+role.value+"]";
					if($.inArray(entry, entryList)==-1)
					{
						entryList.push(entry);
					}
					else
					{										
						duplicateEntryFound=true;
					}
				}
				else
				{
					atRow.find(".statuscombo :selected").each(function(i,status) {
						var statusSelObj=$(status);
						entry="["+wfComboObj+"]["+statusSelObj.val()+"]["+role.value+"]";
						if($.inArray(entry, entryList)==-1)
						{
							entryList.push(entry);
						}
						else
						{										
							duplicateEntryFound=true;
						}
					});
				}
			});				
		});
		$textTableRows.css('border','none');
		var $addBtn=$this;
		action=$addBtn.data('action');
		if(!duplicateEntryFound||action=="remove") 
		{
			uniqueFlag=true;
			$parentTR=$this.parents('tr');
			var classID="",attrID="";
			classID=$('#class-table tr.selected').attr('id');
			attrID=$('#attr-attribute tr.selected').attr('id');

			var Text='',fontC='',bgC='',isDiffC=false,roles=[];
			if(fontColor!='')
				{fontC=fontColor.value.toString();}
			if(backgroundColor!='')
			{
				bgC=backgroundColor.value.toString();
			}
			isNew=action;
			var $row=$addBtn.parents('tr');

			if(action!='new')
			{
				txtId=$addBtn.data('id');
				$row.find('#rowText').find('#transparentDiv').remove();
				Text=$row.find('#rowText').html();

				fontC=AssistColorPicker.getHexColor($row.find('.colorSelector button').css('color'));
				bgC=AssistColorPicker.getHexColor($row.find('.colorSelector button').css('background-color'));

				$('#roleop'+txtId+' :selected').each(function(i, selected) {
					roles[i] =$(selected).val();
				});
				if(action!="remove"){
					if(Text==""){
						PLMFlex.Assist.BindEvents.Notify("You cannot save empty text","error");
						$parentTR.css('border','2px solid #DF8F8F');
						return;
					}

					if(roles.length==0){
						var choice=accessTypeRole?"Role":"Group";
						PLMFlex.Assist.BindEvents.Notify("Please select at least one "+choice,"error");
						$parentTR.css('border','2px solid #DF8F8F');
						return;
					}
				}
			}

			var workflowname=$("#workflowSelect"+txtId).val();
			var workflowstatusArr=$("#statusSelect"+txtId).val();
			var workflowstatus="";

			if(workflowstatusArr)
			{
				for(var wfIndex=0;wfIndex<workflowstatusArr.length;wfIndex++)
				{
					workflowstatus+=workflowstatusArr[wfIndex];

					if(wfIndex < workflowstatusArr.length-1)
					{
						workflowstatus+=";";
					}
				}
			}
			if(action == 'save' && ($("#workflowSelect"+txtId).attr("disabled")!='disabled' && workflowname=='-1'))
			{	
				if(isRoutable != undefined){
					if(!isRoutable){
						PLMFlex.Assist.BindEvents.Notify("Please select a lifecycle","error");
					}
					else if(isRoutable){
						PLMFlex.Assist.BindEvents.Notify("Please select a workflow","error");
					}	
				}
				
				return;
			}
			if(action == 'save' && ($("#statusSelect"+txtId).attr("disabled")!='disabled' && (workflowstatus=='-1' || workflowstatus=='')) && isRoutable != undefined && isRoutable )
			{
				PLMFlex.Assist.BindEvents.Notify("Please select a status","error");
				return;
			}

			
			if(fontC!=fontColor.value.toString() && bgC!=backgroundColor.value.toString())
			{
		
				isDiffC=true;
			}

			if((typeof classID!="undefined") && classID!="" && (typeof attrID!="undefined") && attrID!="")
			{
				PLMFlex.Assist.Request.post("loadAssistTexts",{
					mode : action,
					textId : txtId,
					fontcolor:	fontC,
					backgroundcolor:bgC ,
					isDiffColor:isDiffC,
					assistText : Text,
					'roles[]' : roles,
					classId:classID,
					attrId:attrID,
					workflowname:workflowname,
					workflowstatus:workflowstatus,
					'isRoutable':isRoutable,
					rnd: Math.random()
				},
				function(jsonResponse)
				{

					var message=jsonResponse.message;
					if(message=="New Assist Text added")
					{					
						var $newRow =$.tmpl( "textRow", jsonResponse.object );
						$newRow.appendTo($('#text-table'));

						$newRow.find('.save ,.remove').click(function()
								{
							bindActions($(this));
								});

						$newRow.find('#rowText').click(function()
								{
							PLMFlex.Assist.AssistText.EditText($(this),jsonResponse.object);
								});
						PLMFlex.Assist.AssistText.drawWorkflowSelect(jsonResponse.object,"#workflowSelect","#statusSelect",$AttContainer.find('tr.selected'));

						PLMFlex.Assist.AssistText.drawRolesSelect(jsonResponse.object,"#roleop");
						
						bindRowEvents();	
					
							
						$newRow.find('#rowText').click();

					}

					if(message=="Assist Text removed")
					{
						$parentTR.next().remove();
						$parentTR.remove();
						var $thisrow=$AttContainer.find('tr.selected');
						$thisrow.trigger('click');
						message="Assist Text removed";
					}
					if(message=="Assist Text saved")
					{
						var $thisrow=$AttContainer.find('tr.selected');
						$thisrow.trigger('click');
						message="Assist Text saved";
						return;
					}

				});
			}
			else{PLMFlex.Assist.BindEvents.Notify("Please select a class and attribute to add Assist Text","error");}
		}else{
			uniqueFlag=false;
			PLMFlex.Assist.BindEvents.Notify("Assist text already exists against the specified criteria","error");
		}
	}
	
	function bindClassActions($this)
	{
		var classID=$('#class-table tr.selected').attr('id');
		if((typeof classID!="undefined") && classID!="") {
			var notifEnabled = $("input:radio[name='enableClassNotification']:checked").val();
			var assistMessageText = tinyMCE.get('AssistClassMessage').getContent();
			assistMessageText = assistMessageText.replace("<p>&nbsp;</p>", '');
			
			if(notifEnabled!=null && notifEnabled!="null"){
				PLMFlex.Assist.Request.post("ClassNotificationMsgBar",{
						assistText : assistMessageText,
						classId:classID,
						notifEnable:notifEnabled,
						rnd: Math.random()
					},
					function(jsonResponse) {
						if(jsonResponse && jsonResponse.status == 'success') {
							PLMFlex.Assist.BindEvents.Notify("Settings have been saved successfully", "success");
							if(notifEnabled == 'Yes') {
								$('#class-table tr.selected').find("#hasTextFlag").not(':has(#textImage)').append("<img id='textImage' src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text' />");
							} else {
								$('#class-table tr.selected #hasTextFlag').empty();
							}
					}
				});
			} else {
				PLMFlex.Assist.BindEvents.Notify("The settings cannot be empty.", "error");
			}
		}
		else {
			PLMFlex.Assist.BindEvents.Notify("Please select a class to add Assist Text","error");
		}
	}
	
	function bindRowEvents() {
		$.each($('#text-table tr').not('#text-table tr tr'), function(index, row) {
			if(index%2 == 0)
				return true;
			
			var $row = $(row);
			if(!$row.hasClass('binded')) {
				$row.addClass('binded');
				$row.find('.roleList').autolist();

				var $pickerTD = $row.find('.pickertd');
				var colors = $pickerTD.data('colors').split(';');
				var isDiffColor = $pickerTD.data('action');
				if((colors[0] == "" && colors[1] == "") || !isDiffColor) {
					if(fontColor != '') {
						AssistColorPicker.createPicker($pickerTD, fontColor.value.toString(), backgroundColor.value.toString(), false, false);
						$pickerTD.find('.picker').html("Default Color");
					}
				} else {
					AssistColorPicker.createPicker($pickerTD, colors[0], colors[1], false, false);
					$pickerTD.find('.picker').html("Text Color");
				}
			}
		});
	}	
	
	function checkDuplicateEntry(textID)
	{
		var entryList=new Array;
		var duplicateEntryFound=false;
		var $textTableRows = $('#text-table tr').not('#text-table tr tr');
		var workflowOrLifecycle=$($textTableRows.find(".wfcomboSelect").children()[0]).html();
		$textTableRows.each(function(index)
		{
			if(index%2==0)
				return true;
			atRow = $(this);
		
			atRow.not(':has(#roleop'+textID+')').find(".roleList :selected").each(function(i,role)
			{
				var wfComboObj=$(atRow.find(".wfcombo :selected"));

				if(workflowOrLifecycle=="Select Lifecycle" || wfComboObj.val()=="All Workflows" || wfComboObj.val()=="No Workflow" || wfComboObj.val()=="")
				{
					entry="["+wfComboObj.val()+"][]["+role.value+"]";
					if($.inArray(entry, entryList)==-1)
					{
						entryList.push(entry);
					}
					else
					{										
						duplicateEntryFound=true;
					}
				}
				else
				{
					atRow.find(".statuscombo :selected").each(function(i,status)
					{
						var statusSelObj=$(status);
						entry="["+wfComboObj.val()+"]["+statusSelObj.val()+"]["+role.value+"]";
						if($.inArray(entry, entryList)==-1)
						{
							entryList.push(entry);
						}
						else
						{										
							duplicateEntryFound=true;	
						}
					});
				}
			});				
		});
		
		$("#editDialog").find(".roleList :selected").each(function(i,role)
		{
			var wfComboObj=$($("#editDialog").find("#dialog-workflowSelect :selected"));

			if(workflowOrLifecycle=="Select Lifecycle" || wfComboObj.val()=="All Workflows" || wfComboObj.val()=="No Workflow" || wfComboObj.val()=="")
			{
				entry="["+wfComboObj.val()+"][]["+role.value+"]";
				
	
				if($.inArray(entry, entryList)==-1)
				{
					entryList.push(entry);
				}
				else
				{										
					duplicateEntryFound=true;
				}
			}
			else
			{
				$("#editDialog").find("#dialog-statusSelect :selected").each(function(i,status)
				{
					var statusSelObj=$(status);
					entry="["+wfComboObj.val()+"]["+statusSelObj.val()+"]["+role.value+"]";
				
					if($.inArray(entry, entryList)==-1)
					{
						entryList.push(entry);
					}
					else
					{										
						duplicateEntryFound=true;	
					}
				});
			}
		});
	
		
		if(duplicateEntryFound!=true)
		{
			uniqueFlag=true;
		}
		else
		{
			uniqueFlag=false;
		}
	
	}

	mce_text="";
	function editText($this,dataObj)
	{
		var textId=$this.prop('id');
		var $roleList=$this.parents('tr').find('.ui-autolist');
		var $previousRoleList=$roleList.clone();
		var $pickerTD=$this.parents('tr').find('.pickertd');
		
		var $previousPickerTD=$pickerTD.children();
			
		var $textControl=$this.parents('tr').find('#rowText');
		$this.parents('tr').find('#rowText').find('#transparentDiv').remove();
		var text=$this.parents('tr').find('#rowText').html();

		var wfCombo=$this.parents('tr').find(".wfcombo").children();
		var statusCombo=$this.parents('tr').find(".statuscombo").children();

		var wfComp=wfCombo[0];
		var statuseComp=statusCombo[0];

		var jqTxt=$(text);
		if(jqTxt.prop('tagName') == 'A' || jqTxt.prop('tagName') == 'a')
		{
			mce_text=jqTxt.text("")[0].outerHTML;
		}
		else
		{
			mce_text=text;
		}

		mce_text=text;
		var $dialogHTML=$("#editDialog");

		$("#editTextRoleLabel").html((accessTypeRole?"Role(s):":"Group(s):"));

		$dialogHTML.attr('data-id',textId);
		$dialogHTML.find("#dialog-roles").html('');

		var $dialogRoles=$dialogHTML.find("#dialog-roles");


		PLMFlex.Assist.AssistText.drawWorkflowSelect(dataObj,"#dialog-workflowSelect","#dialog-statusSelect",$AttContainer.find('tr.selected'));
		$("#dialog-workflowSelect").val($(wfComp).val());
		$("#dialog-workflowSelect").trigger('change');
		$("#dialog-statusSelect").val($(statuseComp).val());
		
		$dialogRoles.append("<select class='roleList' id=roleoptions multiple='multiple'></select>");
		if(action=='new')
			PLMFlex.Assist.AssistText.drawRolesSelect(dataObj,"#roleoptions");
		else
			PLMFlex.Assist.AssistText.drawRolesSelectText(dataObj,"#roleoptions");
		$dialogRoles.find(".roleList").autolist();
	
		$dialogColors=$dialogHTML.find("#dialog-colors");
		$dialogHTML.find("#dialog-colors").html('');
		var colors= $pickerTD.data('colors').split(';');

		var isDiffColor=$pickerTD.data('action');
		if((colors[0]=="" && colors[1]=="") || !isDiffColor )
		{
			if(fontColor!='')
			{
			AssistColorPicker.createPicker($dialogColors,fontColor.value.toString(),backgroundColor.value.toString(),false,false);
			$("#dialog-colors").find('.picker').html("Default Color");
			}
		}
		else
		{
			AssistColorPicker.createPicker($dialogColors,colors[0],colors[1],false,false);
			$("#dialog-colors").find('.picker').html("Text Color");
		}
		
		
		var $dialogRolesList=$dialogRoles.find('.ui-autolist');

		$dialogHTML.dialog({
			width:720,
			height:550,
			modal: true,
			title:"Add/update Text",
			draggable: true,
			resizable:false,
			open: function(){
				enableRichText($(this));
			},
			close:function(){
				isNew="old";
				if(action=="new")
				{
					$this.parents('tr').next().remove();
					$this.parents('tr').remove();
				}
				$( this ).dialog( "destroy" );
			
			},
			buttons:{
				"Save":function(){
					checkDuplicateEntry(dataObj.TextID);
					
					if(uniqueFlag==false)
					{
						PLMFlex.Assist.BindEvents.Notify("Assist text already exists against the specified criteria","error");
					
					}
					else
					{
						isNew="old";
						
						if( ($("#dialog-workflowSelect").attr("disabled")!='disabled' && $("#dialog-workflowSelect").val()!='-1'))
						{
							$("#workflowSelect"+dataObj.TextID).val($("#dialog-workflowSelect").val());
							$("#workflowSelect"+dataObj.TextID).trigger('change');							
						}
						else
						{
							if( ($("#dialog-workflowSelect").attr("disabled")!='disabled'))
							{
								if(isRoutable != undefined){
									if(!isRoutable){
										PLMFlex.Assist.BindEvents.Notify("Please select a lifecycle","error");
									}
									else if(isRoutable){
										PLMFlex.Assist.BindEvents.Notify("Please select a workflow","error");
									}
								}

								return;
							}
						}
						if( ($("#dialog-statusSelect").attr("disabled")!='disabled' && $("#dialog-statusSelect").val()!=null))
						{
							
								$("#statusSelect"+dataObj.TextID).val($("#dialog-statusSelect").val());
						
						}						
						else
						{
							if( ($("#dialog-statusSelect").attr("disabled")!='disabled')  && isRoutable != undefined && isRoutable )
							{
								PLMFlex.Assist.BindEvents.Notify("Please select a status","error");
								return;
							}
						}

						var roleChildren=($dialogRolesList.children()[0]);

						if(roleChildren.getElementsByTagName("li").length == 0)
						{
							var choice=accessTypeRole?"Role":"Group";
							PLMFlex.Assist.BindEvents.Notify("Please select at least one "+choice,"error");
							return;
						}
						var tinyMiceText="";
						try
						{
							tinyMiceText=tinyMCE.get('mce_text').getContent();
						}catch(e)
						{
							
							tinyMiceText=$("#mce_text_ifr").contents().find('#tinymce').html();
						}
						
						if(tinyMiceText != "") {
							if(tinyMiceText.length > 4000) {
								PLMFlex.Assist.BindEvents.Notify("HTML source code for the Assist Text exceeds 4000 character limit. Please remove some text.","error");
								return;
							}
							
							$textControl.html(tinyMiceText);
						} else {
							PLMFlex.Assist.BindEvents.Notify("Empty text is not allowed, please enter text to save.","error");
							return;
						}
						
						$this.parents('tr').find('td.roles .ui-autolist').remove();
						$this.parents('tr').find('td.roles .roleList').html('');
						$this.parents('tr').find('td.roles .roleList').html($dialogRoles.find('.roleList').children());
						$this.parents('tr').find('td.roles').append($dialogRolesList);					
					
						$pickerTD.insertAfter( $this.parents('tr').find('td.diffColor'));
						$pickerTD.html('');
						$pickerTD.html($dialogHTML.find("#dialog-colors").children());				
						$this.parents('tr').find('.save').click();
					
						$('#attr-attribute tr.selected').find("#HasAssistText").not(':has(#textImage)').append("<img id='textImage' src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text, Click to view already added texts. Right click to add text.'>");
						$('#class-table tr.selected').find("#hasTextFlag").not(':has(#textImage)').append("<img id='textImage' src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text' />");
						
						$(this).dialog("destroy");
					}
				
				},
				"Cancel":function(){
					isNew="old";
					if(action=="new")
					{
						$this.parents('tr').next().remove();
						$this.parents('tr').remove();
					}
					else
					{
						$this.parents('tr').find('#rowText').prepend("<div id='transparentDiv'></div>");
						var width=$this.parents('tr').find('#rowText').width();
						var height=$this.parents('tr').find('#rowText').get(0).scrollHeight;					
						$this.parents('tr').find('#rowText').find("#transparentDiv").width(width+'px');
						$this.parents('tr').find('#rowText').find("#transparentDiv").height(height+'px');
					}
					$( this ).dialog( "destroy" );
					
				}

			}
		}); 
	}

	function enableRichText($this) {
		if(typeof tinyMCE != "undefined") {
			tinyMCE.remove('mce_text');
		}
		
		$('#mce_editor').html('<textarea id=mce_text></textarea>');
		
		tinymce.init({
			selector: 'textarea#mce_text',
			height: 195,
			width: 690,
			theme: 'modern',
			menubar: false,
			branding: false,
		    resize: false,
		    setup: function(ed) {
		    	ed.on("init",
	                function(ed) {
		    			// this == tinyMCE.get('mce_text')
		    			this.getBody().innerHTML = mce_text;
	                }
		    	);
		    	ed.on("keydown",
	                function(ed) {
			    		if (ed.keyCode == 9) { // tab pressed
					        if (ed.shiftKey) {
					        	this.execCommand('Outdent');
					        } else {
					        	this.execCommand('Indent');
					        }
					        
					        ed.preventDefault();
					        return false;
				        }
	                }
		    	);
			},
			plugins: 'anchor autolink code contextmenu help image insertdatetime link media paste preview searchreplace table',
	        fontsize_formats: '8pt 10pt 11pt 12pt 14pt 18pt 24pt',
	        font_formats: 'Arial=arial,helvetica,sans-serif;Courier New=courier new,courier,monospace;Helvetica=helvetica;Times New Roman=times new roman,times',
			formats: {
				bold: {inline: 'b'},
	            italic: {inline: 'i'},
	            underline: {inline: 'u'}
		    },
		    extended_valid_elements: 'b,i,b/strong,i/em',
		    paste_retain_style_properties: 'border border-left border-right border-bottom border-top font-size text-decoration text-align',
			paste_remove_styles_if_webkit: false,
			paste_data_images: false,
		    toolbar1: 'newdocument | bold underline italic strikethrough | alignleft aligncenter alignright alignjustify | fontselect fontsizeselect | searchreplace',
		    toolbar2: 'cut copy pastetext | undo redo | link unlink anchor media image table insertdatetime | removeformat | help code preview',
		    content_css: ["css/tinymce-content.css"]
		});
	}

	return {
		add: AddText,
		addNotif: AddNotifText,
		update: UpdateText,
		loadAttributes: loadAttributes,
		loadAssistMessage: loadAssistMessage,
		loadAssistText: LoadAssistText,
		EditText: editText,
		drawWorkflowSelect: drawWorkflowSelect,
		drawRolesSelect: drawRolesSelect,
		drawRolesSelectText: drawRolesSelectText,
		bindActions: bindActions
	};
})();