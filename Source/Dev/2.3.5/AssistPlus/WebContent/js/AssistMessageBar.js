if(typeof PLMFlex =='undefined') {
	var PLMFlex = {};
	if(typeof PLMFlex.Assist =='undefined') {
		PLMFlex.Assist = {};
	}
}

PLMFlex.Assist.AssistMessageBar=(function() {
	
	function load($containerHeader, $contentboxcontent) {
		var $containerHTML=$(PLMFlex.Assist.HTML.AssistMessage);
		$contentboxcontent.html($containerHTML);
	

		PLMFlex.Assist.Request.get("NotificationMsgBar?", function(jsonResponse) {
			var notificationMsg = jsonResponse.notificationMsg;
			var enableNotification = jsonResponse.isNotifEnabled;
			if(notificationMsg!=null && enableNotification!=null){
				$containerHTML.find('#enableNotification input:radio[value=' + enableNotification + ']').prop('checked', true);
				$("#AssistMessage").val(notificationMsg+"");
			}
			
			var $button = $containerHTML.find('.button.saveNotification');

			PLMFlex.Assist.BindEvents.saveAssistMsgSettings($button);
		});

	}
	function save($subcontent){
		var configs = [];
		var selectVal = "";
		configs[0] = "isNotifEnabled" + "=" + $("input:radio[name='enableNotification']:checked").val();
		
		var assistMessageText = $("#AssistMessage").val();
		configs[1]="notificationMsg"+ "="+assistMessageText+"";
		if(assistMessageText.lenght>255 ) {
			PLMFlex.Assist.BindEvents.Notify("Assist message should be less than 255 characters", "error");
			return;
		}
		
		PLMFlex.Assist.Request.post("NotificationMsgBar?", {'configs[]': configs,rnd: Math.random()}, function(jsonResponse) {
			if(jsonResponse && jsonResponse.status == 'success') {
				PLMFlex.Assist.BindEvents.Notify("Settings have been saved successfully", "success");
			}
			
		});
		
	}
	return{
		Load: load,
		Save: save
	};
	
})();

