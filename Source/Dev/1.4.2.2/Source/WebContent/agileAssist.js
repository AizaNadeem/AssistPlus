/*!
 * Agile Assist
 * provides help when you hover over Agile field 
 * Copyright 2013, Xavor.com
 * Date: Mon July 23 2013 
 */
(function(){
	
	var d="/AssistPlus";
	var f=0;
	var c=document.getElementsByTagName("head")[0];
	var ap=document.createElement("script");
	ap.type="text/javascript";
	ap.src=d+"/js/AssistProperties.js";
	c.appendChild(ap);
	
	if(typeof(jQuery)=="undefined"){
		
		var b=document.createElement("script");
		b.type="text/javascript";
		b.src=d+"/js/jquery.min.js";
		c.appendChild(b);
		
		
		f=300;
		}
	setTimeout(function(){
		if(f>0){
			jQuery=$;
		}
		var c=document.getElementsByTagName("head")[0];
		jQuery(document).ready(function(){
			jQuery("body").append("<ul id='appMenu' class='contextMenu'><li><a href='#Approv'>Complete</a></li></ul>");
			jQuery("body").append("<div id='help-toolTip' style='display:none'>" +
						"<div>" +
							"<div class='heading'><span>Heading</span></div>" +
							"<div class='description'id='helpDiscription'>Some detailed Text</div>" +
						"</div>" +
						"<span class='rightImg'>" +
						"</span>" +
					"</div>");
			jQuery("body").append("<link type='text/css' rel='stylesheet' href="+d+"/css/style.css>");
			jQuery("body").append("<script type='text/javascript' src="+d+"/js/jquery.contextMenu.js><\/script>");
			PLMFlex={};
			jQuery.getJSON("/AssistPlus/settings?rnd="+Math.random(),function(response){
				PLMFlex.Roles=response.Roles;
				PLMFlex.Classes=response.classes;
			});
		});
	},f);
})();
var helpText;
function hideAssistText(){
	jQuery("#help-toolTip").hide();
	jQuery(this).removeClass("helpDT");
}
function strip(b){
	var a=document.createElement("DIV");
	a.innerHTML=b;
	return a.innerHTML;
}


function parentNavigationClick(){
	jQuery('#MSG_Show_In_Navigator,#Navigation,#Navigationspan').click(function(){
		bindNavigationClick();
		setTimeout(function(){
			bindNavigationClick();
		},500);		
	});
}

function bindNavigationClick(){
	setTimeout(function(){
		jQuery('#ctxNavTree .ygtvitem .node_status_icon,#ctxNavTree .ygtvitem .node_status_icon + a').contextMenu(
			{        
				menu: 'appMenu'    
			},        
			function(action, el, pos) {        
				var infoUrl = jQuery(el).attr("infourl");
				var valueList=infoUrl.split(',');
				jQuery.getJSON("/AssistPlus/complete?prgName="+valueList[1].replace(/\'/g,'')+"&rnd="+Math.random(),function(response){
					if (response.hasOwnProperty("fail")){
						var html="<html><head><title>Assist Plus</title><style>td a{font-weight:bold;}th{width:400px;}table, td, th{border:1px solid #999;}th{background-color:#333;color:white;}</style></head><body><table><div>Due to some problem, following activities are not completed. Please check the activity or contact admin.</div><tr><th> <a>Activity</a></th><th>Description</th><th>Exception</th></tr>";
							jQuery.each(response.fail,function(){
								html+=this;
							});
						html+="</table></body></html>";
						
						var myWindow=window.open('','','width=600,height=600,scrollbars=1');
						myWindow.document.write(html);
						myWindow.focus();
					}
					refreshCtxNavTree();
					
				});
			});
	},300);
}

function load(){
	setTimeout(function(){
		var classid=jQuery("form[name=MainForm] input:hidden#subclsid").val();
		var classes="";
		var title=document.title;
		var userid=title.substring(title.lastIndexOf("(")+1,title.lastIndexOf(")"));
		for(var i=0;i<PLMFlex.Classes.length;i++){
		    var found=false;
		    var classLevel=PLMFlex.Classes[i].split(";");
		    for(var j=0;j<classLevel.length;j++){
		        if(classLevel[j]==classid){
		            found=true;
		            break;
		        }
		    }
		    if(found){
		        classes=PLMFlex.Classes[i];
		        break;
		    }
		}
		if(classid!=""){
			var arg=[];
			arg.url="/AssistPlus/GetAssistText";
			var $helpFrame=jQuery("#helpFrame");
			var wfId="";
			var statusId="";
			
			/*jQuery('dt').each(function(index) 
			{
				var thisDT=jQuery(this);
				var thisDtText=thisDT.text();
				
				if(thisDtText == 'Workflow:')
				{
					var nextDT=thisDT.next();
					if(nextDT)
					{
						wfId=nextDT.text();
					}
				}
				if(thisDtText == 'Status :')
				{
					var nextDT=thisDT.next();
					if(nextDT)
					{
						statusId=nextDT.text();
					}
				}
			});*/
			try{wfId=jQuery("#col_"+WORKFLOW_BASEID).text();}catch(e){}
			try{statusId=jQuery("#col_"+STATUS_BASEID).text();}catch(e){}
			jQuery.post(arg.url,
					{
					classid:classid,
					workflowID:wfId,
					workflowStatusID:statusId,
					userid:userid,
					classes:classes,
					roles:PLMFlex.Roles,
					rnd:Math.random()
				},function(response){
					helpText=response.helpText;
					var attColorsData=response.attColors;
				
				jQuery('dt').each(function(index) {
					
					var $thisDT=jQuery(this);
					var $attMeta=$thisDT.next();
					var attMetaSplit=$attMeta.attr("id").split("_");
					var attid=attMetaSplit[attMetaSplit.length-1];
					var attributeProps=eval("helpText["+attid+"];");
					var attColors=eval("attColorsData["+attid+"];");
					
					$thisDT.css('color',attColors);
					
					var hText,fontColor,backgroundColor;
					if(attributeProps!=undefined){
						if($thisDT.find('.helpDT').length==0){
						$thisDT.prepend("<span class='helpDT'>&nbsp;&nbsp;&nbsp;</span>");
						}
						$thisDT.css('cursor','pointer');
						
						hText=attributeProps[0];
						fontColor=attributeProps[1];
						backgroundColor=attributeProps[2];
						$thisDT.bind('mouseenter hover',function(){
							var $this=jQuery(this);
							if(hText!=undefined){
								var $help=jQuery("#help-toolTip");
								$help.find(".heading span").text($this.text());
								$help.find(".description").html(strip(hText));
								var dtLeft=$this.position().left;
								var dtSpanLeft=$this.find('.helpDT').position().left;
								var helpLeft;
								
								var topPoistion=$this.position().top;
								
								var m = document.compatMode == 'CSS1Compat';
								var windowHeight = window.innerHeight || (m ? document.documentElement.clientHeight : document.body.clientHeight);
								
								if((topPoistion+$help.height())>windowHeight)
								{
									topPoistion=windowHeight-($help.height()+30);
								}
								
								if(dtSpanLeft>500){
									helpLeft=dtLeft-120;
								}else{
									helpLeft=10;
								}
								$help
									.css("top",topPoistion+"px")
									.css("left",(helpLeft)+"px")
									.css("background-color",backgroundColor)
									.css("color",fontColor);
								$help.find('.heading')
									.css("color",fontColor)
									.css('border-color',fontColor);
								$help.stop();
								$help.show();
								$help.css('opacity','1.0');
								$help.css('word-wrap','break-word');
								
								
								/*$help.css('min-height','100px');
								$help.css('height','auto');
								$help.css('max-height','200px');*/
								
							}
						});
						
						$thisDT.bind('mouseleave',function(){ 
							jQuery("#help-toolTip").fadeOut("slow");
						});
						
						jQuery("#help-toolTip").bind('mouseenter',function(){
							jQuery("#help-toolTip").stop();
							jQuery("#help-toolTip").css('opacity','1.0');
							jQuery("#help-toolTip").show();

						});
						
						jQuery("#help-toolTip").bind('mouseleave',function(){
							jQuery("#help-toolTip").fadeOut("slow");
						});
						
						
					}else{
						$thisDT.bind('mouseover',function(){
							jQuery("#help-toolTip").hide();
						});
					}
				});
				
				
				
				/*jQuery("dt").bind("mouseover",function(){
					var $this=jQuery(this);
					$this.css("cursor","pointer");
					jQuery("#help-toolTip").hide();
					$this.addClass("helpDT");
					var attMeta=$this.next();
					var attMetaSplit=attMeta.attr("id").split("_");
					var attid=attMetaSplit[attMetaSplit.length-1];
					var attributeProps=eval("helpText["+attid+"];")
					var hText,fontColor,backgroundColor;
					if(attributeProps!=undefined){
						
					}
					
				});*/
			},"json");
		}
	},200);
};