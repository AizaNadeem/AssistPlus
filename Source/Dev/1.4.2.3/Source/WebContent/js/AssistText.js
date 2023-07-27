if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}


PLMFlex.Assist.AssistText=(function(){
	
	function AddText($containerHeader,$contentboxcontent){
		var $containerHTML=$(PLMFlex.Assist.HTML.ClassesHTML.classesHTML((accessTypeRole?"Role(s)":"User Group(s)")));
		$('.saveRoles').remove();
		PLMFlex.Assist.BindEvents.Filter($containerHTML,".classFilter","#class-table tr.parent","td:first-child");
		PLMFlex.Assist.BindEvents.Filter($containerHTML,".attFilter","#attr-attribute tr","td:first-child a");
		
		$('.classFilter').bind('foucs',function()
		{
		});
		
		$contentboxcontent.html($containerHTML);
		$contentboxcontent.css('padding','0');
		
		/*var $pickerTh= $('#attr-header').find('.pickerth');
		
		AssistColorPicker.createFontColorPicker($pickerTh,'#000000','#ffffff');*/
		
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
				var attMetaArr=$thisRow.data('attr').split(';');
				
				
				var fontC=AssistColorPicker.getHexColor($thisRow.find('.colorSelector span').css('color'));
				
				classId=attMetaArr[0];
				
				attColorJson['colorId']=attColorArr[0];
				attColorJson['attId']=attMetaArr[1];
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
		PLMFlex.Assist.Request.get("loadClasses?",function(jsonResponse){
			
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
			setHeights($containerHTML,$contentboxcontent,$containerHeader);	
		});
	}
	
	
	
	function setHeights($containerHTML,$contentboxcontent,$containerHeader){
		$containerHTML.eq(1).height($contentboxcontent.height()-$containerHeader.height());
		$containerHTML.eq(2).height($contentboxcontent.height()-$containerHeader.height());
		$( "content-box .left" ).height($contentboxcontent.height()-$containerHeader.height());
		$('#attr-div').height( $(".rightTop").height()-$('#attr-header').height());
		$('#text-div').height( $(".rightBottom").height()-$('#text-header').height());
	}
	var workflows=[];
	var $AttContainer;
	function loadAttributes(classId, classLevel,$this) 
	{
		$('#class-table tr').removeClass('selected');
		$AttContainer=$("#attr-attribute");
		$('#text-table').children().remove();
		$AttContainer.children().remove();
		$this.addClass('selected');
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Class Attributes","attention");
		PLMFlex.Assist.Request.get("loadAttributes?classId=" + classId + "&level="+ classLevel ,function(jsonResponse){
			if (jsonResponse.hasOwnProperty("object"))
			{
				var attributes=jsonResponse.object.attributes;
				workflows=jsonResponse.object.workflows;
				if(!workflows)
				{
					workflows=[];
				}
				attributes.sort(function(a, b) {
					   var aID = b.isVisible;
					   var bID = a.isVisible;
					   return (aID == bID) ? 0 : (aID > bID) ? 1 : -1;
					});
				
				$.tmpl( "attributeRow", attributes).appendTo( $AttContainer );
				
				
				bindRowHover($AttContainer);
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
						}
					});
					$(this).find('.picker').each(function()
					{
						$(this).css('border',"0px");
						$(this).css('padding',"1px");
						$(this).css('margin-top',"1px");
						$(this).css('background-color',"");
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
		$('#attr-attribute tr').removeClass('selected');
		var $textContainer=$('#text-table');
		$textContainer.children().remove();
		$this.addClass('selected');
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Assist Text","attention");
			
		PLMFlex.Assist.Request.get("loadAssistTexts?classId=" + classId + "&attrId="+ attributeId ,function(jsonResponse){
			//console.info(jsonResponse);
			$.tmpl( "textRow", jsonResponse.object ).appendTo($textContainer);
			
			
			
			var textArr=jsonResponse.object;
			var textObj={};
			var selectElem=null;
			
			
			
//			for(var txtIndex=0;txtIndex<textArr.length;txtIndex++)
//			{
//				textObj=textArr[txtIndex];
				binRowdActions(textArr); 
//			}
			var wfSelectHtml="<option value='-1' selected='selected'>Select Workflow</option>";
			wfSelectHtml+="<option value='"+"AllWorkflows"+"'>"+"All Workflows"+"</option>";
			var wfObj={};
			for(var wfIndex=0;wfIndex<workflows.length;wfIndex++)
			{
				wfObj=workflows[wfIndex];
				
				wfSelectHtml+="<option value='"+wfObj.id+"'>"+wfObj.value+"</option>";
			}
			
			
			for(var txtIndex=0;txtIndex<textArr.length;txtIndex++)
			{
				textObj=textArr[txtIndex];
				PLMFlex.Assist.AssistText.drawWorkflowSelect(textObj,"#workflowSelect","#statusSelect");
			}
		});
		
	}
	function drawWorkflowSelect(textObj,cmbWorkflow,cmbStatus)
	{
		textID=textObj.textID;
		if(cmbWorkflow!="#workflowSelect")
			{textID="";}
		selectElem=$(cmbWorkflow+textID);
		
		if(!workflows || workflows.length==0)
		{
			$(cmbStatus+textID).attr("disabled","disabled");
			var wfDisabled="<option value='' selected='selected'>No Workflow</option>";
			selectElem.html(wfDisabled);
			selectElem.attr("disabled","disabled");

			return;
		}
		else
		{
			$(cmbStatus+textID).removeAttr('disabled');
			selectElem.removeAttr('disabled');
		}
		
		var wfSelectHtml="<option value='-1' selected='selected'>Select Workflow</option>";
		wfSelectHtml+="<option value='"+"All Workflows"+"'>"+"All Workflows"+"</option>";
		var wfObj={};
		
		for(var wfIndex=0;wfIndex<workflows.length;wfIndex++)
		{
			wfObj=workflows[wfIndex];
			
			wfSelectHtml+="<option value='"+wfObj.id+"'>"+wfObj.value+"</option>";
		}
		
		
		selectElem.attr("data-textid",textID);
		
		selectElem.html(wfSelectHtml);
		
		selectElem.attr("data-selectedStatus",textObj.workflowStatusId);
		
		selectElem.change(function()
		{
			var wfSelect=$(this);
			var textId=wfSelect.attr("data-textid");
			var wfstID=wfSelect.attr("data-selectedStatus");
			
			var selectedOption=wfSelect[0].options[wfSelect[0].selectedIndex];
			var selectedWf=wfSelect.val();
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
			statusOptions+="<option id='"+"All Statuses"+"'>"+"All Statuses"+"</option>";
			
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
		$('#text-table tr').each(function()
		{
			var atRow = $(this);
			atRow.find("#rowText").click(function()
			{
				PLMFlex.Assist.AssistText.EditText($(this),dataObj[$(this).parent().parent().index()]);
			});
		});
		
		/*
		$('#text-table tr #rowText').click(function()
		{
			alert($(this).parent().parent().index());
			PLMFlex.Assist.AssistText.EditText($(this),dataObj[$(this).parent().parent().index()]);
			for(var txtIndex=0;txtIndex<dataObj.length;txtIndex++)
			{
				var textObj=dataObj[txtIndex]; 
				PLMFlex.Assist.AssistText.EditText($(this),textObj);
			}
		
		});*/
		
	}
	var txtId='';
	var uniqueFlag=false;
	function bindActions($this)
	{
		var entryList=new Array;
		var duplicateEntryFound=false;		
		
		$('#text-table tr').each(function()
		{
			atRow = $(this);
			atRow.find(".roleList :selected").each(function(i,role)
			{
				var wfComboObj=$(atRow.find(".wfcombo :selected"));
				
				if(wfComboObj.val()=="All Workflows" || wfComboObj.val()=="No Workflow" || wfComboObj.val()=="")
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
		$('#text-table tr').css('border','none');
		
		if(!duplicateEntryFound) {
			uniqueFlag=true;
		$parentTR=$this.parents('tr');
		var classID="",attrID="";
		classID=$('#class-table tr.selected').attr('id');
		attrID=$('#attr-attribute tr.selected').attr('id');
		
		var $addBtn=$this;
		var Text='',fontC='#ffffff',bgC='#333333',isDiffC=false,roles=[];
		var action=$addBtn.data('action');
		var $row=$addBtn.parents('tr');
		if(action!='new'){
			txtId=$addBtn.data('id');
			Text=$row.find('#rowText').html();
			isDiffC=$row.find('.chk').is(':checked');
			if(isDiffC){
				
			fontC=AssistColorPicker.getHexColor($row.find('.colorSelector span').css('color'));
			bgC=AssistColorPicker.getHexColor($row.find('.colorSelector span').css('background-color'));
			}
			
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
					var choice=accessTypeRole?"Role":"User Group";
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
			PLMFlex.Assist.BindEvents.Notify("Please select a workflow","error");
			return;
		}
		if(action == 'save' && ($("#statusSelect"+txtId).attr("disabled")!='disabled' && (workflowstatus=='-1' || workflowstatus=='')))
		{
			PLMFlex.Assist.BindEvents.Notify("Please select a status","error");
			return;
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
					PLMFlex.Assist.AssistText.drawWorkflowSelect(jsonResponse.object,"#workflowSelect","#statusSelect");
					
					bindRowEvents();
					$newRow.find('.#rowText').click();
				}
		
				if(message=="Assist Text removed")
				{
					$parentTR.remove();	
				}
				if(message=="Assist Text saved")
				{
					if(txtId=="-1")
					{
						var $thisrow=$AttContainer.find('tr.selected');
						$thisrow.trigger('click');
						message="";
						return;
					}
				}
		
	});
			}
		else{PLMFlex.Assist.BindEvents.Notify("Please select a class and attribute to add Assist Text","error");}
		}else{
			uniqueFlag=false;
			PLMFlex.Assist.BindEvents.Notify("Assist text already exists against the specified criteria","error");
		}
	}
	
	function bindRowEvents(){
		
		$.each($('#text-table tr'),function(index, row)
		{
			var $row=$(row);
			if(!$row.hasClass('binded'))
			{
				$row.addClass('binded');
				$row.find('.roleList').autolist();
				
				var $pickerTD= $row.find('.pickertd');
				var colors= $pickerTD.data('colors').split(';');
				var $checkBox=$row.find('.chk');
			
				if($checkBox.is(":checked"))
				{
					AssistColorPicker.createPicker($pickerTD,colors[0],colors[1]);
					$pickerTD.css('padding-top','0');
					$pickerTD.css('padding-bottom','0');
				}else{
					AssistColorPicker.createPicker($pickerTD,'#ffffff','#333333');
					$pickerTD.find('.text').show();
					$pickerTD.find('.colorSelector').hide();
				}
			$checkBox.change(function()
			{
				
				 if(this.checked) 
				 { 
					 $pickerTD.find('.text').hide();
					 $pickerTD.find('.colorSelector').show();
					 $pickerTD.find('.text').hide();
					 $pickerTD.find('.colorSelector').show();
					 $(this).parents("#editDialog").find('#dialog-colors .text').hide();
					 $(this).parents("#editDialog").find('#dialog-colors .colorSelector').show();
					 
				 }else
				 {
					 $pickerTD.find('.text').show();
					 $pickerTD.find('.colorSelector').hide();
					 $(this).parents("#editDialog").find('#dialog-colors .text').show();
					 $(this).parents("#editDialog").find('#dialog-colors .colorSelector').hide();
				 }
			});
		}
		});
	}
	
	mce_text="";
	function editText($this,dataObj)
	{
		var textId=$this.data('id');
		var $roleList=$this.parents('tr').find('.ui-autolist');
		var $pickerTD=$this.parents('tr').find('.pickertd');
		var $textControl=$this.parents('tr').find('#rowText');
		var text=$this.parents('tr').find('#rowText').html();
		var $diffCheck=$this.parents('tr').find('td.isDiff');
		
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
		
		$("#editTextRoleLabel").html((accessTypeRole?"Role(s)":"User Group(s)"));
		
		$dialogHTML.attr('data-id',textId);
		$dialogHTML.find("#dialog-roles").html('');
		
		var dialogRoles=$dialogHTML.find("#dialog-roles");
		
		dialogRoles.append($roleList);
		dialogRoles.css("max-height","20px");
		
		PLMFlex.Assist.AssistText.drawWorkflowSelect(dataObj,"#dialog-workflowSelect","#dialog-statusSelect");
		$("#dialog-workflowSelect").val($(wfComp).val());
		$("#dialog-workflowSelect").trigger('change');
		$("#dialog-statusSelect").val($(statuseComp).val());
		
		$dialogHTML.find("#dialog-colors").html('');
		$dialogHTML.find("#dialog-colors").append($pickerTD.children());
		$dialogHTML.find("#diffCheckBox").html('');
		$dialogHTML.find("#diffCheckBox").append($diffCheck.children());
		
		//$pickerTD.find('.picker').css('float','left');
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
				$( this ).dialog( "destroy" );
				var $thisrow=$AttContainer.find('tr.selected');
				$thisrow.trigger('click');
			},
			buttons:{
				"Save":function(){
				if( ($("#dialog-workflowSelect").attr("disabled")!='disabled' && $("#dialog-workflowSelect").val()!='-1'))
				{
					$("#workflowSelect"+dataObj.textID).val($("#dialog-workflowSelect").val());
					$("#workflowSelect"+dataObj.textID).trigger('change');
				}
				else
				{
					if( ($("#dialog-workflowSelect").attr("disabled")!='disabled'))
					{
						PLMFlex.Assist.BindEvents.Notify("Please select a workflow","error");
						return;
					}
				}
				if( ($("#dialog-statusSelect").attr("disabled")!='disabled' && $("#dialog-statusSelect").val()!=null))
				{
					$("#statusSelect"+dataObj.textID).val($("#dialog-statusSelect").val());
				}
				else
				{
					if( ($("#dialog-statusSelect").attr("disabled")!='disabled'))
					{
						PLMFlex.Assist.BindEvents.Notify("Please select a status","error");
						return;
					}
				}
				
				var roleChildren=($roleList.children()[0]);
				
				if(roleChildren.childElementCount==0)
				{
					var choice=accessTypeRole?"Role":"User Group";
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
				if(tinyMiceText!="")
				{
					var aParentObj=$(tinyMiceText);
					var anchorObj=aParentObj.children();
					
					if(anchorObj.prop('tagName') == 'A' || anchorObj.prop('tagName') == 'a')
					{
						var title=anchorObj.attr('title');
						anchorObj.text(title);
						$textControl.html(anchorObj);
					}
					else
					{
						$textControl.html(tinyMiceText);
					}
				}
				else
				{
					PLMFlex.Assist.BindEvents.Notify("Empty text is not allowed, please enter text to save.","error");
					return;
				}
				$this.parents('tr').find('td.roles').append($roleList);
				$pickerTD.insertAfter( $this.parents('tr').find('td.isDiff'));
				$pickerTD.append($dialogHTML.find("#dialog-colors").children());
				$diffCheck.append($dialogHTML.find("#diffCheckBox").children());					
				$this.parents('tr').find('.save').click();
				
				if(uniqueFlag==true)
				{
					$( this ).dialog( "destroy" );
				}
				else
				{
					$dialogHTML.find("#dialog-roles").append($roleList);
					$dialogHTML.find("#dialog-colors").html('');
					$dialogHTML.find("#dialog-colors").append($pickerTD.children());
					$dialogHTML.find("#diffCheckBox").html('');
					$dialogHTML.find("#diffCheckBox").append($diffCheck.children());
				}
				},
			"Cancel":function(){
				$( this ).dialog( "destroy" );
				var $thisrow=$AttContainer.find('tr.selected');
				$thisrow.trigger('click');
				}
			
			}
		}); 
	}
	
	function setText(){
		tinyMCE.get('mce_text').setContent(mce_text);
	}
	
	function enableRichText($this){
		if(typeof tinyMCE!="undefined"){
			tinyMCE.remove('mce_text');
		}
		$('#mce_editor').html('<textarea id=mce_text></textarea>');
		
		$this.find('textarea').tinymce({
			// Location of TinyMCE script
			script_url : 'js/tiny_mce/tiny_mce.js',
			setup:function(ed){ed.onInit.add(setText)},
			// General options
			theme : "advanced",
			plugins : "autolink,lists,pagebreak,style,layer,table,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media,searchreplace,print,contextmenu,paste,directionality",
			height:	"300",
			// Theme options
			theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,styleselect,formatselect,fontselect,fontsizeselect",
			theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
			theme_advanced_toolbar_location : "top",
			theme_advanced_toolbar_align : "left",
			theme_advanced_statusbar_location : "bottom",
			theme_advanced_resizing : true,

			// Example content CSS (should be your site CSS)
			content_css : "css/reset.css",

			// Drop lists for link/image/media/template dialogs
			template_external_list_url : "lists/template_list.js",
			external_link_list_url : "lists/link_list.js",
			external_image_list_url : "lists/image_list.js",
			media_external_list_url : "lists/media_list.js"
		});
		
	}
	
	return{
		add:AddText,
		update:UpdateText,
		loadAttributes:loadAttributes,
		loadAssistText:LoadAssistText,
		EditText:editText,
		drawWorkflowSelect:drawWorkflowSelect
	};
	
})();

