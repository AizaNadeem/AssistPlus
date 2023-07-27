if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.AssistMessageBar=(function() {
	var originalNotificationMsg = "";
	function load($containerHeader, $contentboxcontent) {
		var $containerHTML=$(PLMFlex.Assist.HTML.AssistMessage);
		$contentboxcontent.html($containerHTML);
	

	    if (tinymce.get('AssistMessage')) {
  			tinymce.get('AssistMessage').remove();
		}		
		tinymce.init({
			selector: 'textarea#AssistMessage',
			height: 165,
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
		    toolbar1: 'newdocument | bold underline italic strikethrough | alignleft aligncenter alignright alignjustify | fontselect fontsizeselect',
		    toolbar2: 'cut copy pastetext | undo redo | link unlink anchor insertdatetime | removeformat | help code preview | searchreplace',
		    content_css: ["css/tinymce-content.css"]
		});
		
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
			var notificationMsg = jsonResponse.notificationMsg;
			var enableNotification = jsonResponse.isNotifEnabled;
			var enableAck = jsonResponse.isAckEnabled;
			if(notificationMsg!=null && enableNotification!=null){
				$containerHTML.find('#enableNotification input:radio[value=' + enableNotification + ']').prop('checked', true);
				$containerHTML.find('#enableAck input:radio[value=' + enableAck + ']').prop('checked', true);
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
		var configs = [];
		var selectVal = "";
		configs[0] = "isNotifEnabled" + "=" + $("input:radio[name='enableNotification']:checked").val();
		configs[1]= "isAckEnabled" + "=" + $("input:radio[name='enableAck']:checked").val();
		var assistMessageText = tinyMCE.get('AssistMessage').getContent();
		assistMessageText = assistMessageText.replace("<p>&nbsp;</p>", '');
		if(assistMessageText !== originalNotificationMsg) {
			configs[2]= "notificationMsg"+ "="+assistMessageText+"";
		}		
		PLMFlex.Assist.Request.post("NotificationMsgBar?", {'configs[]': configs,rnd: Math.random()}, function(jsonResponse) {
			if(jsonResponse && jsonResponse.status == 'success') {
				PLMFlex.Assist.BindEvents.Notify("Settings have been saved successfully", "success");
			}
			
		});
		originalNotificationMsg = assistMessageText;	
	}
	return{
		Load: load,
		Save: save
	};
	
})();

