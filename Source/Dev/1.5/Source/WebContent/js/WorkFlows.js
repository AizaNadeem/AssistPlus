if(typeof PLMFlex =='undefined'){
	var PLMFlex={};
	if(typeof PLMFlex.Assist =='undefined'){
		PLMFlex.Assist={};
	}
}
PLMFlex.Assist.WorkFlows=(function(){
	function loadWorkflows()
	{
	var classid=jQuery("form[name=MainForm] input:hidden#subclsid").val();
	var classes="";
	var title=document.title;
	var userid=title.substring(title.lastIndexOf("(")+1,title.lastIndexOf(")"));
	var action="loadworkflow";
	classid='2470431';
	if(classid!=""){
		var arg=[];
		arg.url="/AssistPlus/WorkflowServlet";
		var $helpFrame=jQuery("#helpFrame");
		jQuery.post(arg.url,
				{
				action:action,
				classid:classid,
				userid:userid,
				classes:classes,
				roles:PLMFlex.Roles,
				rnd:Math.random()
			},function(response){
				//console.info(response);
				helpText=response.helpText;
				var attColorsData=response.attColors;
			
			},"json");
	}
	}
	
	function loadWorkflowStatuses()
	{
	var classid=jQuery("form[name=MainForm] input:hidden#subclsid").val();
	var classes="";
	var title=document.title;
	var userid=title.substring(title.lastIndexOf("(")+1,title.lastIndexOf(")"));
	var action="loadworkflowstatus";
	workflowname='DefaultDeviations';
	if(workflowname!=""){
		var arg=[];
		arg.url="/AssistPlus/WorkflowServlet";
		var $helpFrame=jQuery("#helpFrame");
		jQuery.post(arg.url,
				{
				action:action,
				workflowname:workflowname,
				userid:userid,
				classes:classes,
				roles:PLMFlex.Roles,
				rnd:Math.random()
			},function(response){
				//console.info(response);
				helpText=response.helpText;
				var attColorsData=response.attColors;
			
			},"json");
	}
	}
	return{
		load:loadWorkflows,
		loadStatus:loadWorkflowStatuses

	}
})();
	