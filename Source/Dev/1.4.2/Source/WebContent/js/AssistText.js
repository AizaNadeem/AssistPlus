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
	
	function loadAttributes(classId, classLevel,$this) 
	{
		$('#class-table tr').removeClass('selected');
		var $AttContainer=$("#attr-attribute");
		$('#text-table').children().remove();
		$AttContainer.children().remove();
		$this.addClass('selected');
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Class Attributes","attention");
		PLMFlex.Assist.Request.get("loadAttributes?classId=" + classId + "&level="+ classLevel ,function(jsonResponse){
			if (jsonResponse.hasOwnProperty("object"))
			{	
				$.tmpl( "attributeRow", jsonResponse.object ).appendTo( $AttContainer );
				bindRowHover($AttContainer);
				PLMFlex.Assist.BindEvents.loadAssistText($AttContainer.find('tr'));
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
			$.tmpl( "textRow", jsonResponse.object ).appendTo($textContainer);
			binRowdActions(); 
			
			
		});
		
	}
	
	
	function UpdateText(){
		console.log("Text Updated");
	}
	
	function binRowdActions(){
		$('#text-table tr .save, #text-table tr .remove').click(function(){
			bindActions($(this));
		});
		bindRowEvents();
		
		$('#text-table tr #rowText').click(function(){
			PLMFlex.Assist.AssistText.EditText($(this));
		});
		
	}

	function bindActions($this){
		var selectedRoles=new Array;
		var duplicateFound=false;
		$('#text-table td select :selected').each(function(i,sel){
			if($.inArray(sel.value, selectedRoles)==-1){
				selectedRoles.push(sel.value);
			}else{
			duplicateFound=true;	
			}
			
			
		});
		
		
		$('#text-table tr').css('border','none');
		if(!duplicateFound){
		$parentTR=$this.parents('tr');
		var classID="",attrID="";
		classID=$('#class-table tr.selected').attr('id');
		attrID=$('#attr-attribute tr.selected').attr('id');
		
		var $addBtn=$this;
		var txtId='',Text='',fontC='#ffffff',bgC='#333333',isDiffC=false,roles=[];
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
					PLMFlex.Assist.BindEvents.Notify("Please select at least one role","error");
					$parentTR.css('border','2px solid #DF8F8F');
					return;
				}
			}
		}
		
		
		
		if((typeof classID!="undefined") && classID!="" && (typeof attrID!="undefined") && attrID!=""){
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
		rnd: Math.random()
	},function(jsonResponse){
		var message=jsonResponse.message;
		if(message=="New Assist Text added"){
			var $newRow =$.tmpl( "textRow", jsonResponse.object );
			$newRow.appendTo($('#text-table'));
			
			$newRow.find('.save ,.remove').click(function(){
				bindActions($(this));
			});
			
			$newRow.find('#rowText').click(function(){
				PLMFlex.Assist.AssistText.EditText($(this));
			});
			bindRowEvents();
			$newRow.find('.#rowText').click();
		}
		
		if(message=="Assist Text removed"){
		$parentTR.remove();	
		}
		
	});}else{PLMFlex.Assist.BindEvents.Notify("Please select a class and attribute to add Assist Text","error");}
		}else{
			PLMFlex.Assist.BindEvents.Notify("Same "+(accessTypeRole?"Role":"User Group")+" in Multiple Rows","error");
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
	function editText($this){
		
		var textId=$this.data('id');
		var $roleList=$this.parents('tr').find('.ui-autolist');
		var $pickerTD=$this.parents('tr').find('.pickertd');
		var $textControl=$this.parents('tr').find('#rowText');
		var text=$this.parents('tr').find('#rowText').html();
		var $diffCheck=$this.parents('tr').find('td.isDiff');
			
		mce_text=text;
		var $dialogHTML=$("#editDialog");
		
		$("#editTextRoleLabel").html((accessTypeRole?"Role(s)":"User Group(s)"));
		
		$dialogHTML.attr('data-id',textId);
		$dialogHTML.find("#dialog-roles").html('');
		$dialogHTML.find("#dialog-roles").append($roleList);
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
				$textControl.html( tinyMCE.get('mce_text').getContent());
				$( this ).dialog( "destroy" );
				$this.parents('tr').find('td.roles').append($roleList);
				$pickerTD.insertAfter( $this.parents('tr').find('td.isDiff'));
				$pickerTD.append($dialogHTML.find("#dialog-colors").children());
				$diffCheck.append($dialogHTML.find("#diffCheckBox").children());
				$this.parents('tr').find('.save').click();
				if(tinyMCE.get('mce_text').getContent()==""){
					PLMFlex.Assist.BindEvents.Notify("Empty text is not allowed, please enter text to save.","error");
				}
			},
			buttons:{
				"Save":function(){
				if(tinyMCE.get('mce_text').getContent()!=""){
					$textControl.html( tinyMCE.get('mce_text').getContent());
					$( this ).dialog( "destroy" );
					$this.parents('tr').find('td.roles').append($roleList);
					$pickerTD.insertAfter( $this.parents('tr').find('td.isDiff'));
					$pickerTD.append($dialogHTML.find("#dialog-colors").children());
					$diffCheck.append($dialogHTML.find("#diffCheckBox").children());
					$this.parents('tr').find('.save').click();
				}else{
					PLMFlex.Assist.BindEvents.Notify("Empty text is not allowed, please enter text to save.","error");
				}
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
		EditText:editText
	};
	
})();

