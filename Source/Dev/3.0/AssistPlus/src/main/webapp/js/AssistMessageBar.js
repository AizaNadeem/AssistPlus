if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.AssistMessageBar=(function() {
	var originalNotificationMsg = "<p>Assist Message</p>";
	function load($containerHeader, $contentboxcontent) {
		PLMFlex.Assist.BindEvents.Notify("Please wait while we are loading Assist Notification Bar","attention");
		var $containerHTML=$(PLMFlex.Assist.HTML.AssistMessage);
		AssistColorPicker.createPicker($containerHTML.find("#notificationBackgroundColor"),'#3e3434','#EEECE1',true,false);
		$containerHTML.find("#notificationBackgroundColor").find('.picker').html("Default Color");	
		$("#notificationBackgroundColor").find('.picker').each(function() {
			$(this).css('border',"0px")
				.css('padding',"1px")
				.css('margin-top',"1px")
				.css('width',"110px")
				.css('background-color',"")
				.text("Default Color")
				.html("Default Color");
		});
		$containerHTML.find('.colorSelector ').each(function() {
			$(this).css('padding',"0px")
				.css('margin',"0px");
		});
		$contentboxcontent.html($containerHTML);
		$contentboxcontent.attr("id","loadAssistMessage");
		$contentboxcontent.outerHeight($(window).height()-100);
		$("#loadAssistMessage").css({
			'overflow': 'auto'
		});
		$('#assistMessage').hide();
		function initTinyMCE() {
		    if (tinymce.get('AssistMessage')) {
	  			tinymce.get('AssistMessage').remove();
			}		
			tinymce.init({
				selector: 'textarea#AssistMessage',
				height: 115,
				width: 605,
				theme: 'modern',
				menubar: false,
				branding: false,
			    resize: false,
			    setup: function (editor) {
				    editor.on('change', function () {
				      var content = editor.getContent();
				      if (content.length > 500) {
				        editor.undoManager.undo();
						PLMFlex.Assist.BindEvents.Notify("Assist notification should be less than 500 characters", "error");
				      }
				    });
				    editor.on('keypress', function(e) {
				      var content = editor.getContent();
				      if (content.length >= 500) {
				        e.preventDefault();
				        PLMFlex.Assist.BindEvents.Notify("Assist notification should be less than 500 characters", "error");
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
			    toolbar1: 'newdocument | bold underline italic strikethrough | alignleft aligncenter alignright alignjustify | fontselect fontsizeselect',
			    toolbar2: 'cut copy pastetext | undo redo | link unlink anchor insertdatetime | removeformat | help code preview | searchreplace',
			    content_css: ["css/tinymce-content.css"]
			});
		}
		
		function setEditorContent(notificationMsg) {
		    var editor = tinymce.get('AssistMessage');
		    if (editor && editor.initialized) {
		      if(notificationMsg !== 'null') {
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
		
		PLMFlex.Assist.Request.get("NotificationMsgBar?isAdmin=true", function(jsonResponse) {
			$('#assistMessage').show();
			var notificationMsg = jsonResponse.notificationMsg;
			var enableNotification = jsonResponse.isNotifEnabled;
			var enableAck = jsonResponse.isAckEnabled;
			var enableDuration = jsonResponse.isDurationEnabled;
			var duration = jsonResponse.durationLimit;
			var fontC = jsonResponse.fontColor;
			var backgroundC = jsonResponse.backgroundColor;
			if(fontC == 'null') {
				fontC = '#3e3434';
			}
			if(backgroundC == 'null') {
				backgroundC = '#EEECE1';
			}
			$("#notificationBackgroundColor").find('.colorSelector').remove();
			AssistColorPicker.createPicker($("#notificationBackgroundColor"),fontC,backgroundC,true,false);
			var $enableDuration=$containerHTML.find('#enableDuration');
			var $disableDuration=$containerHTML.find('#disableDuration');
			$enableDuration.change(function() {
				if($enableDuration.is(':checked')) {
					$containerHTML.find('#durationBox').show();
			    } else {
					$containerHTML.find('#durationBox').hide();
				}
			});
			$disableDuration.change(function() {
				if($disableDuration.is(':checked')) {
					$containerHTML.find('#durationBox').hide();
			    } else {
					$containerHTML.find('#durationBox').show();
				}
			});
			$containerHTML.find("#editTextRoleLabel").html((accessTypeRole?"Role(s):":"Group(s):"));
			$("#dialog-notif-roles").html('');

			var $dialogRoles=$("#dialog-notif-roles");
			$dialogRoles.append("<select class='roleList' id=roleoptions multiple='multiple' autocomplete='off'></select>");
		
			initTinyMCE();
			if(notificationMsg!=null && enableNotification!=null){
				$containerHTML.find('#enableNotification input:radio[value=' + enableNotification + ']').prop('checked', true);
				$containerHTML.find('#enableAck input:radio[value=' + enableAck + ']').prop('checked', true);
				if(enableDuration == "Yes") {
					$containerHTML.find('#durationBox').show();
					var $isNew = $containerHTML.find('#enableNotifDuration input:radio[value='+enableDuration+']').prop('checked', true);
				} else {
					$containerHTML.find('#durationBox').hide();
					var $isNew = $containerHTML.find('#enableNotifDuration input:radio[value='+enableDuration+']').prop('checked',true);
				}
				$containerHTML.find('input:radio[value='+duration+']').prop('checked',true);
				drawRolesSelectText(jsonResponse.roles.split(","),"#roleoptions");
				$dialogRoles.find(".roleList").autolist();
				$dialogRoles.find(".ui-autolist").find(".ui-autocomplete-input").attr('autocomplete', 'new-password');
				setTimeout(function() {
      				setEditorContent(notificationMsg);
   				}, 100);
			}
			
			var $button = $containerHTML.find('.button.saveNotification');
			originalNotificationMsg = notificationMsg;
			PLMFlex.Assist.BindEvents.saveAssistMsgSettings($button);
		});

	}
	function save($subcontent){
		var configs = [], roles=[];
		var selectVal = "";
		configs[0] = "isNotifEnabled" + "=" + $("input:radio[name='enableNotification']:checked").val();
		configs[1]= "isAckEnabled" + "=" + $("input:radio[name='enableAck']:checked").val();
		configs[2] = "fontNotifColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#notificationBackgroundColor .colorSelector button').css('color'));
		configs[3] = "backgroundNotifColor"+"="+AssistColorPicker.getHexColor($subcontent.find('#notificationBackgroundColor .colorSelector button').css('background-color'));
		configs[4] = "isDurationEnabled"+"="+$("input:radio[name='enableDuration']:checked").val();
		configs[5] = "durationLimit"+"="+$("input:radio[name='duration']:checked").val();

		$('#roleoptions option:selected').each(function(i, selected) {
				roles[i] =$(selected).val().trim();
		});
		if(roles.length==0){
			var choice=accessTypeRole?"Role":"Group";
			PLMFlex.Assist.BindEvents.Notify("Please select at least one "+choice,"error");
			return;
		}
		var assistMessageText = tinyMCE.get('AssistMessage').getContent();
		assistMessageText = assistMessageText.replace("<p>&nbsp;</p>", '');
		if(assistMessageText !== originalNotificationMsg) {
			configs[6]= "notificationMsg"+ "="+assistMessageText+"";
		}		
		PLMFlex.Assist.Request.post("NotificationMsgBar?", {'configs[]': configs, 'roles[]': roles,rnd: Math.random()}, function(jsonResponse) {
			if(jsonResponse && jsonResponse.status == 'success') {
				PLMFlex.Assist.BindEvents.Notify("Settings have been saved successfully", "success");
			}
		});
		originalNotificationMsg = assistMessageText;
	}
	
	function drawRolesSelectText(roles,roleOptions)
	{
		var roleElem=$(roleOptions);
		var roles=roles;
		var rolesHtml="";
		for( var i=0;i<roles.length;i=i+3)
		{
			if(roles[i+2]!=null && roles[i+2].trim()=="selected")
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
	
	return{
		Load: load,
		Save: save
	};
	
})();

