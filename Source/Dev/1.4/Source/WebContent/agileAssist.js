/*!
 * Agile Assist
 * provides help when you hover over Agile filed 
 * Copyright 2011, Xavor.com
 * Date: Mon July 11 2011 
 */
(function(){
	
	var d="/AssistPlus";
	var f=0;
	if(typeof(jQuery)=="undefined"){
		var c=document.getElementsByTagName("head")[0];
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
		jQuery(document).ready(function(){
			if(typeof(JSON)=="undefined"){
				jQuery("body").append("<script type='text/javascript' src="+d+"/js/json2.js><\/script>");
			}
			jQuery("body").append("<div id='help-toolTip' style='display:none'><div><div class='heading'><span>Heading</span></div><div class='description'>Some detailed Text</div></div><span class='rightImg'></span></div>");
			jQuery("body").append("<link type='text/css' rel='stylesheet' href="+d+"/css/style.css>");
			
			jQuery('#dms .text').change(function(){
				alert($(this).text());
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
function load(){
	setTimeout(function(){
		var classid=jQuery("form[name=MainForm] input:hidden#subclsid").val();
		var title=document.title;
		var userid=title.substring(title.lastIndexOf("(")+1,title.lastIndexOf(")"));
		if(classid!=""){
			var arg=[];
			arg.url="/AssistPlus/GetAssistText?classid="+classid+"&userid="+userid+"&rnd="+Math.random();
			var $helpFrame=jQuery("#helpFrame");
			jQuery.getJSON(arg.url,function(response){
				helpText=response;
				jQuery('dt').each(function(index) {
					
					var $thisDT=jQuery(this);
					var $attMeta=$thisDT.next();
					var attMetaSplit=$attMeta.attr("id").split("_");
					var attid=attMetaSplit[attMetaSplit.length-1];
					var attributeProps=eval("helpText["+attid+"];");
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
								if(dtSpanLeft>500){
									helpLeft=dtLeft-120;
								}else{
									helpLeft=10;
								}
								$help
									.css("top",($this.position().top-20)+"px")
									.css("left",(helpLeft)+"px")
									.css("background-color",backgroundColor)
									.css("color",fontColor);
								$help.find('.heading')
									.css("color",fontColor)
									.css('border-color',fontColor);
								$help.stop();
								$help.show();
								$help.css('opacity','1.0');
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
			});
		}
	},200);
};