/*!
 * Agile Assist
 * provides help when you hover over Agile field 
 * Copyright 2013, Xavor.com
 * Date: Mon July 23 2013 
 */
(function(){
	

	var d="/AssistPlus";
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
		if(f>0){
			jQuery=$;
		}
	}
		var c=document.getElementsByTagName("head")[0];
		jQuery(document).ready(function(){
			jQuery("body").append("<link type='text/css' rel='stylesheet' href="+d+"/css/style.css>");
			jQuery("body").append("<script type='text/javascript' src="+d+"/js/jquery.contextMenu.js><\/script>");
			while(typeof(PLMFLEX)=="undefined")
			{
				if(f>1200)
					break;
				setTimeout(loadData,f);
				f+=300;
			}
			
		});
	

})();
PLMFlex={};
var helpText;
var fadeDuration=600;
var f=300;
var map=new Object();
var pinStack=[];
function loadData()
{
	jQuery.getJSON("/AssistPlus/settings?rnd="+Math.random(),function(response){

		PLMFlex.Roles=response.Roles;
		PLMFlex.Classes=response.classes;
	});
}
function strip(b){
	var a=document.createElement("DIV");
	a.innerHTML=b;
	return a.innerHTML;
}
var hoverBackGroundColor;
function bindHelpDt($thisDT,helpText,attColorsData)
{
	var $attMeta=$thisDT.next();
	var attMetaSplit=$attMeta.attr("id").split("_");
	var attid=attMetaSplit[attMetaSplit.length-1];
	var attributeProps=eval("helpText["+attid+"];");
	var attColors=eval("attColorsData["+attid+"];");
	
	$thisDT.css('color',attColors);
	
	var hText,fontColor,backgroundColor;
	if(attributeProps!=undefined){
		if($thisDT.find('.helpDT').length==0){
		$thisDT.prepend("<img class='helpDT' src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAIGNIUk0AAHolAACAgwAA+f8AAIDp AAB1MAAA6mAAADqYAAAXb5JfxUYAAAAJcEhZcwAACxIAAAsSAdLdfvwAAAFUSURBVChTbZCxSwJh GMa/gkCvxMHuCIsaNBwazTjPpjJuuGw4HCKzBiHl7qyhLuW78sSChoaioKkhnNrbGlr7NxqjMIQw g6Cn9yy3Xvjxwvt7lvdh7HeKRJt4I1qMDRLe7tEhzrzQkCiKT5IkQRQlhEIS0nIQUxMiQqPeTfRo M0VRAslkspVKKZCTCuKJeVw5k1hWE5idS4EcZFnuMsMwRvL5/Mv6RgHbJR23pzN4vBFxdxlFfXcJ q7kCyHeYbduBSqXyyrkDh9s4dndwfx3HxckmDp09VKsc5DvMdV2Bc/7eaDRQq7ngB0donmfRqO+D Oy7danAc57v3smmazXK53C2VSp9Ed6tofhimBcsyCeuL/MNfO2wgm81GdV2fzmQyUQ9NW9nRNA2q qj6n0+lgP/jvjsViuUgkAqpusR8YppEEQRjz+/1hYpxE2OvY5/OtkVv4AXQciT8kg47KAAAAAElF TkSuQmCC'></img>");
		}
		$thisDT.css('cursor','pointer');
		
		hText=attributeProps[0];
		fontColor=attributeProps[1];
		backgroundColor=attributeProps[2];
		hoverBackGroundColor=attributeProps[3];
		$thisDT.bind('mouseenter hover',function(){
			var $this=jQuery(this);
			mouseEnterHoverDT($this,hText,attid,fontColor,backgroundColor);
		});
		
		$thisDT.bind('mouseleave',function(){ 
			mouseLeaveDT($thisDT,attid,prevColor);
		});
		
		
	}else{
		$thisDT.bind('mouseover',function(){
			mouseOverDT(attid);
		});
	}

}
var prevColor="";
function mouseEnterHoverDT($this,hText,attid,fontColor,backgroundColor)
{
	if(hText!=undefined){
		var topPoistion=($this.position().top)-28;
		var windowHeight = window.innerHeight || (m ? document.documentElement.clientHeight : document.body.clientHeight);
		var windowWidth= window.innerWidth || (m ? document.documentElement.clientWidth : document.body.clientWidth);
		var m = document.compatMode == 'CSS1Compat';
		var $help,$maxButton,$maximize,$description,$pinButton,helpLeft;
		var dtLeft=$this.position().left;
		var dtSpanLeft=$this.find('.helpDT').position().left;
		
		if(map[attid]!=undefined)
		{
			$help=map[attid];
			if($help.css("position") == "fixed")
				return;
			mouseEnterToolTip($help,$this);
			$description=$help.find('.description');
		}
		else
		{
			$this.css('background-color',hoverBackGroundColor);
			jQuery("body").append("<div class='help-toolTip' id='help-toolTip"+attid+"' style='display:none'>" +
					"<div>" +
						"<div style:'float:right'>"+
							"<div class='pin' id='pin'>"+
								"<span class='pinUpper' id='pinUpper'></span>" +
								"<span class='pinLower' id='pinLower'></span>"+
							"</div>" +
							"<div class='max' id='max'>"+
								"<span class='maximize' id='maximize'></span>" +
								"<span class='maximize' id='maximize2'></span>"+
							"</div>" +
						"<div>"+
						"<div class='heading'><span>Heading</span></div>" +
						"<div class='description'id='helpDiscription'>Some detailed Text</div>" +
						"<div class='footer'id='footer'>Assist Plus &copy Xavor Corporation</div>" +
					"</div>" +
				"</div>");	
			$help=jQuery("#help-toolTip"+attid);
			map[attid]=$help;
			$help.find(".heading span").text($this.text());
			$description=$help.find('.description');
			$description.html(strip(hText));
			$maxButton=$help.find('.max');
			$pinButton=$help.find('.pin');
			
			helpLeft=dtSpanLeft-22-jQuery($help).width();
			$help
				.css("background-color",backgroundColor)
				.css('word-wrap','break-word')
				.css("color",fontColor)
				.css("max-height",259)
				.css("left",(helpLeft)+"px")
			 	.resizable({handles: "w, sw, s,n,nw"})
				.resizable({
					resize: function(event, ui) {
						$description=$help.find('.description');
					    $description.css({'max-height':$help.innerHeight()-68});	
					    var maxHeight=($description.get(0).scrollHeight+68 > windowHeight-22&&($description.get(0).scrollHeight)>jQuery($help).outerHeight()+10)? windowHeight-22 : ($description.get(0).scrollHeight)+68;
					    $help.css("max-height",maxHeight>259?maxHeight:259).css('opacity','1.0').css("max-width",dtSpanLeft-22);
				        $help.unbind('mouseleave');   
				   },
				   stop:function(event, ui) {
						jQuery($help).bind('mouseleave',function(){
							mouseleaveToolTip($help,$this);
						});
				   }
				});
			$description.css('max-height',$help.innerHeight()-68);	
			$help.find('.heading')
				.css("color",fontColor)
				.css('border-color',fontColor);
			
			$maxButton
				.css("background-color",backgroundColor)
				.css('border-color',fontColor);
			
			$pinButton
				.css("background-color",backgroundColor)
				.css('border-color',fontColor);
			$pinButton.find('#pinUpper').css("background-color",fontColor);
			$help.find('.footer').css('color',fontColor);
			mouseEnterToolTip($help,$this);
			$description=$help.find('.description');
			if($description.get(0).scrollHeight > $help.height())
			{
				$help.find('.max').show();
				$help.resizable( "enable" );
			}
			else
			{
					$help.find('.max').hide();
					$help.resizable( "disable" );
			}

			jQuery($maxButton).bind('click',function(){
				maximize($help,windowHeight,dtSpanLeft,helpLeft,actualTop,topPoistion,$description);
			});
			
			jQuery($pinButton).bind('click',function(){
				pinHandler($help,windowWidth,windowHeight,helpLeft,dtSpanLeft,$pinButton,$this);
			});
			$description.bind('click',function(){
				pinHandler($help,windowWidth,windowHeight,helpLeft,dtSpanLeft,$pinButton,$this);

			});
			$help.find('.heading').bind('click',function(){
				pinHandler($help,windowWidth,windowHeight,helpLeft,dtSpanLeft,$pinButton,$this);

			});
			$help.find('.footer').bind('click',function(){
				pinHandler($help,windowWidth,windowHeight,helpLeft,dtSpanLeft,$pinButton,$this);

			});
			jQuery($maxButton).bind('mouseenter hover',function(){
				maxHover($maxButton,fontColor,backgroundColor);
			});
			jQuery($maxButton).bind('mouseleave',function(){ 
				maxLeave($maxButton,fontColor,backgroundColor);
			});
			jQuery($pinButton).bind('mouseenter hover',function(){
				pinHover($pinButton,fontColor,backgroundColor);
			});
			jQuery($pinButton).bind('mouseleave',function(){
				pinLeave($pinButton,fontColor,backgroundColor);
			});
			$help.bind('mouseenter',function(){
				mouseEnterToolTip($help,$this);

			});
			jQuery($help).bind('mouseleave',function(){
				mouseleaveToolTip($help,$this);
			});
			
		}
		$help=jQuery($help);
		$description=$help.find('.description');
		if($description.get(0).scrollHeight > $help.height())
		{
			$help.find('.max').show();
			$help.resizable( "enable" );
		}
		else
		{
			if($help.position().left!=10&&($help.css('max-height')<260||$help.css('width')<249))
			{
				$help.find('.max').hide();
				//$help.resizable( "disable" );
			}
		}
		helpLeft=dtSpanLeft-22-jQuery($help).width();
		var actualTop=topPoistion;
		if((topPoistion+$help.outerHeight())>windowHeight)
		{
			topPoistion=windowHeight-($help.height()+30);
		}
		$help
		.css('left',helpLeft)
		.css("top",topPoistion+"px");
		
	}

}
function maxHover($maxButton,fontColor,backgroundColor)
{
	$maxButton
	.css("background-color",fontColor)
	.css('border-color',backgroundColor)
	.css('cursor','hand');
	$maxButton.find('#maximize').css('border-color',backgroundColor);
	$maxButton.find('#maximize2').css('border-color',backgroundColor);
}
function maxLeave($maxButton,fontColor,backgroundColor)
{
	jQuery($maxButton)
	.css("background-color",backgroundColor)
	.css('border-color',fontColor)
	.css('cursor','pointer');
	$maxButton.find('#maximize').css('border-color',fontColor);
	$maxButton.find('#maximize2').css('border-color',fontColor);
}
function pinHover($pinButton,fontColor,backgroundColor)
{
	jQuery($pinButton)
	.css("background-color",fontColor)
	.css('border-color',backgroundColor)
	.css('cursor','hand');
	$pinButton.find('#pinUpper').css('border-color',backgroundColor)
		.css('background-color',backgroundColor);;
	$pinButton.find('#pinLower').css('border-color',backgroundColor);
}
function pinLeave($pinButton,fontColor,backgroundColor)
{
	jQuery($pinButton)
	.css("background-color",backgroundColor)
	.css('border-color',fontColor)
	.css('cursor','pointer');
	$pinButton.find('#pinUpper')
	.css('border-color',fontColor)
	.css('background-color',fontColor);
	$pinButton.find('#pinLower').css('border-color',fontColor);
}
function mouseLeaveDT($thisDT,attid,prevColor)
{
	$thisDT.css('background-color',prevColor);
	if(map[attid]!=undefined)
		{
		if(map[attid].css("position") == "fixed")
			{return;}
		jQuery(map[attid]).fadeOut(fadeDuration);
		}

	
}
function mouseOverDT(attid)
{
	
	if(map[attid]!=undefined)
		{if(map[attid].css("position") == "fixed")
			{return;}
		jQuery(map[attid]).fadeOut(fadeDuration);
		}
	
}
function mouseEnterToolTip($help,$thisDT)
{
	$help.stop().fadeIn(fadeDuration,function(){$help.css('opacity','1.0');});
	$thisDT.css('background-color',hoverBackGroundColor);
}
function mouseleaveToolTip($help,$thisDT)
{
	$thisDT.css('background-color',prevColor);
	if($help!=undefined)
		{
		if($help.css("position") == "fixed")
			{return;}
		jQuery($help).fadeOut(fadeDuration);
		}

}
function pinHandler($help,windowWidth,windowHeight,helpLeft,dtSpanLeft,$pinButton,$thisDT)
{
	if($help.css("position") == "fixed")
	{
		unPin($help,dtSpanLeft,helpLeft,$thisDT,$pinButton);
	}
	else
	{
		if(pinStack.length>0)
		{
			unPin(jQuery(pinStack[0]),dtSpanLeft,pinStack[1],pinStack[2],$pinButton);
		}
		pinStack[0]=$help;
		pinStack[1]=jQuery($help).position().left;
		pinStack[2]=$thisDT;
		jQuery($help).unbind('mouseleave');
		$help.find('.max').hide();
		$help.resizable( "disable" );
		jQuery($help).css('left', function(){ return $help.position().left; })
        			 .animate({"left":windowWidth-$help.outerWidth(),"top":windowHeight-$help.outerHeight()}, 50,
        					 function(){
        					 jQuery($help).css("position","fixed");
        						$pinButton.find('#pinLower').css('height','3px');});	
		
	}
}
function unPin($help,dtSpanLeft,helpLeft,$thisDT,$pinButton)
{
	pinStack=[];
	setTimeout(function(){
		$thisDT.css('background-color',prevColor);
		jQuery($help).fadeOut(fadeDuration,function()
		{
		if($help.width()==(dtSpanLeft-32))
		{jQuery($help)
			.css("top",3)
			.css("left",10);}
		else
		{
				$help.css("left",10+((dtSpanLeft-22)-$help.width()));
		}
		$help.css("position","absolute");
		$help.resizable( "enable" );
		$pinButton.find('#pinLower').css('height','7px');
		jQuery($help).bind('mouseleave',function(){
			mouseleaveToolTip($help,$thisDT);
		});
		$help.find('.max').show();});	},200);
}
function maximize($help,windowHeight,dtSpanLeft,helpLeft,actualTop,topPoistion,$description)
{
	$help=jQuery($help);
	if(($help.find('.description')).get(0).scrollHeight > $help.height())
	{
		if($help.position().left!=10)
		{
			var $leftpane=jQuery('#leftpane');
			jQuery($help)
				.css("left",10)
				.css("width",dtSpanLeft-32)
				.css("height",($help.find('.description').get(0).scrollHeight)+68)
				.css("max-height",windowHeight-30);
			$description.css({'max-height':$help.innerHeight()-68});
			if((topPoistion+$help.outerHeight())>windowHeight)
			{
				topPoistion=windowHeight-($help.height()+30);
			}
			jQuery($help).css("top",topPoistion);
			$help.resizable( "disable" );
			return;
		}
	}

	jQuery($help).animate({
	"top":topPoistion,
	"left":helpLeft,
	"max-height":259,
	"width":'248px'},200,function(){
	$description.css({'max-height':jQuery($help).innerHeight()-68});
	$help.resizable( "enable" );});

}
function getClassParents(classid)
{
	var classesToSend=[];
	for(var i=0;i<PLMFlex.Classes.length;i++)
	{
	    var found=false;
	    var classLevel=PLMFlex.Classes[i].split(";");
	    if(jQuery.inArray(classid, classLevel)!=-1)
	    {
	    	found=true;
	    }
	    if(found){
			if(classLevel.length>2)
			{
				classesToSend.push(classid,classLevel[0],classLevel[1]);
			}
	        break;
	    }
	}
	return classesToSend;
}

function load(){
	setTimeout(function(){
		var classid=jQuery("form[name=MainForm] input:hidden#subclsid").val();
		if(classid!="")
		{
			var classes="";
			var title=document.title;
			var userid=title.substring(title.lastIndexOf("(")+1,title.lastIndexOf(")"));
			var classesToSend=getClassParents(classid);
			var arg=[];
			arg.url="/AssistPlus/GetAssistText";
			var wfId="";
			var statusId="";
			try{wfId=jQuery("form[name=MainForm] #col_"+WORKFLOW_BASEID).text();}catch(e){}
			try{statusId=jQuery("form[name=MainForm] #col_"+STATUS_BASEID).text();}catch(e){}
			jQuery.post(arg.url,
					{
					classid:classid,
					workflowID:wfId,
					workflowStatusID:statusId,
					userid:userid,
					classes:classesToSend,
					roles:PLMFlex.Roles,
					rnd:Math.random()
				},function(response){
					helpText=response.helpText;
					var attColorsData=response.attColors;
				jQuery('dt').each(function(index) {
					
					var $thisDT=jQuery(this);
					prevColor=$thisDT.css('background-color');
					bindHelpDt($thisDT,helpText,attColorsData);
				});

			},"json");
			}
	},200);
};