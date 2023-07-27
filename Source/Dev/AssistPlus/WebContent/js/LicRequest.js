if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}

PLMFlex.Assist.LicRequest=(function(){
	
	function getData(url,callback){
		$.get(url+"&rnd=" + Math.random(),function(responseJson) {
			if($.isFunction(callback)){
				if(responseJson.hasOwnProperty("message")){
					var text=responseJson.message;
					var type=responseJson.status;
					PLMFlex.Assist.BindEvents.Notify(text,type);
				}
		
				callback(responseJson);
			}
		});
	}
	
	function postData(url,callback){
		$.post(url, function(responseJson) {
			if($.isFunction(callback)){
				if(responseJson.hasOwnProperty("message")){
					var text=responseJson.message;
					var type=responseJson.status;
					PLMFlex.Assist.BindEvents.Notify(text,type);
				}
				
				callback(responseJson);
			}
		});
	}
	
	function showLoad(){
		scrollTop=$('html').scrollTop();
		$('.loading').show();
		$('.loading').offset({top:scrollTop,left:0});
		$('body').css('overflow','hidden');
	}
	function hideLoad(){
		$('.loading').hide();
		$('body').css('overflow','auto');
	}
	
	return{
		get:getData,
		post:postData
	};
	
})();
