 /* Agile Assist+
 * provides help when you hover over Agile field 
 * Copyright 2016, Xavor.com
 */
(function() {
	var homeDirectory="/AssistPlus";
	var head=document.getElementsByTagName("head")[0];
	
	
	if(typeof(jQuery)=="undefined"){
		var jScript=document.createElement("script");
		jScript.type="text/javascript";
		jScript.src=homeDirectory+"/js/jquery.min.js";
		head.appendChild(jScript);
	}
	
	jQuery(document).ready(function() {
		jQuery("body").append("<link type='text/css' rel='stylesheet' href="+homeDirectory+"/css/style.css>");
		jQuery("body").append("<script type='text/javascript' src="+homeDirectory+"/js/jquery.contextMenu.js><\/script>");	
	});
})();


//PLMXL is the global data container object
var PLMXL = {};

function initPLMXL() {
	PLMXL.TOUT;
	PLMXL.VIDEO_ATTID = -1;
	PLMXL.FADE_DURATION = 300;
	PLMXL.HELP_MARGIN_TOP_FROM_DIV = 4;
	PLMXL.HELP_MARGIN_LEFT = 3;
	PLMXL.HELP_MARGIN_TOP = 3;
	PLMXL.HELP_MARGIN_RIGHT = 3;
	PLMXL.HELP_MARGIN_BOTTOM = 3;
	PLMXL.HELP_HEIGHT = 200;
	PLMXL.HELP_MIN_HEIGHT = 75;
	PLMXL.HELP_WIDTH = 300;
	PLMXL.NEW_DURATION = 3;
	PLMXL.ORIG_LABEL_COLOR = "";
	PLMXL.CSS1 = document.compatMode == 'CSS1Compat';
	PLMXL.SCROLLBAR_MARGIN = scrollbarHeight();
	
	PLMXL.helpMap = [];
	PLMXL.pinStack = [];
	
	PLMXL.WORKFLOW_BASEID = 3742;
	PLMXL.STATUS_BASEID = 1030;
	PLMXL.IS_COVER_PAGE = false;
	
	PLMXL.LIFECYCLE_BASEID = 1084;
	PLMXL.ITEMS_BASECLASS_ID = 901;
	
	var dummyDIV = addEmptyHelpDiv("dummy");	
	dummyDIV.find('.description').css("height",PLMXL.HELP_HEIGHT);
	PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT = dummyDIV.outerHeight() - PLMXL.HELP_HEIGHT;
	PLMXL.HELP_WIDTH_VARIANCE = dummyDIV.outerWidth() - dummyDIV.width();
	PLMXL.HELP_HEIGHT_VARIANCE = dummyDIV.outerHeight() - dummyDIV.height();
	dummyDIV.remove();
	
	jQuery(window).bind("resize", resizeWindow);
}

function resizeWindow( e ) {
	if(PLMXL.pinStack.length > 0) {
		var $help = PLMXL.pinStack[0];
		if($help.css("position") == "fixed") {
			setHelpWidth($help,null,$help.outerWidth());		
			$description=$help.find('.description');	
			var desiredHeight = $help.outerHeight();		
			if ((desiredHeight < PLMXL.HELP_HEIGHT) && hasVerticalScrollbar($description)) {
				desiredHeight = PLMXL.HELP_HEIGHT;
			}
			setHelpHeight($help,null,$description,desiredHeight);
		}
	}
}

function cleanUpDivs() {
	jQuery('.help-toolTip').remove(); 
	jQuery('.helpDT').remove(); 
}

function strip(html) {
	var div = document.createElement("DIV");
	div.innerHTML=html;
	var iHTML = div.innerHTML;	
	return iHTML;
}

function calcDaysDiff( date1, date2 ) {
	// Get 1 day in milliseconds
	var one_day = 1000*60*60*24;
	
	// Convert both dates to milliseconds
	var date1_ms = date1.getTime();
	var date2_ms = date2.getTime();
	
	// Calculate the difference in milliseconds
	var difference_ms = date2_ms - date1_ms;
	    
	// Convert back to days and return
	return Math.round(difference_ms/one_day);
}

function bindHelpDt($thisDT,helpTextArray,attColorsArray,wfAttrId,status)
{
	//console.log("Into Bind Help: ");
	var attid="";
	try{
		if(wfAttrId!=null)
		{
			attid=wfAttrId;
			PLMXL.IS_COVER_PAGE=false
		}
		else
		{
			var $attMeta=$thisDT.next();
			
			var attMetaSplit=$attMeta.attr("id").split("_");
			
			attid=attMetaSplit[attMetaSplit.length-1];
			PLMXL.IS_COVER_PAGE=true;
		}
	}
	
	catch(e)
	{
		try{
		$attMeta=$thisDT;
	
		attMetaSplit=$attMeta.attr("id").split("_");
	
		attid=attMetaSplit[attMetaSplit.length-1];
		PLMXL.IS_COVER_PAGE=false;
		}
		catch(e)
		{
			
		}
	}

	var attHelp=helpTextArray[attid];
	var attColor=attColorsArray[attid];
	PLMXL.ORIG_LABEL_COLOR=$thisDT.css('color');
	$thisDT.css('color',attColor);
	if($thisDT.find('label').length>0) //Apply css to Attribute Labels in EDIT MODE
		$thisDT.find('label').css('color',attColor);
	
	if(attHelp!=undefined){
		var diff=0;
		var lastUpdated=attHelp[4];
		PLMXL.NEW_DURATION=attHelp[5];
		var timeStampnow = new Date();
		if(PLMXL.NEW_DURATION!=-1)
		{
			lastUpdated= new Date(lastUpdated);
			diff=calcDaysDiff(lastUpdated,timeStampnow);
		}
		if($thisDT.find('.hoverSpan').length==0)
		{$thisDT.wrapInner( "<span class='hoverSpan'></span>");}
		
//		$thisDT.attr('title', 'Left click to maximize/minimize. \nRight Click to pin/unpin.');

		var hText=attHelp[0];
		var fontColor=attHelp[1];
		var backgroundColor=attHelp[2];
		
		PLMXL.hoverBackGroundColor=attHelp[3]; //Setting globally
		var $hoverSpan=$thisDT.find(".hoverSpan");
		if(PLMXL.NEW_DURATION=="-1"||diff>PLMXL.NEW_DURATION)
		{
			//console.log("old");
			if($hoverSpan.find('.helpDT').length==0)
			{
				$hoverSpan.prepend("<img class='helpDT' src='data:image/png;base64,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'></img>");
			}
		}
		else
		{
			if($hoverSpan.find('.helpDT').length==0)
			{
				$hoverSpan.prepend("<img class='helpDT' src='data:image/png;base64,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'></img>");
			}
		}
		$hoverSpan.css('cursor','pointer');
		$hoverSpan.unbind('mouseenter').unbind('mouseleave').unbind('contextmenu').unbind('click');
		$hoverSpan.bind('mouseenter',function(){
			var $this=jQuery(this);
			mouseEnterDT($this,hText,attid,fontColor,backgroundColor);
		});

		$hoverSpan.bind('mouseleave',function(){ 
			mouseLeaveDT($hoverSpan,attid);
		});
		$hoverSpan.bind("contextmenu", function(evt) {evt.preventDefault();pinHandler(null,null,attid,hText,fontColor,backgroundColor,true,status);});
		//console.log("binding events");
		if(PLMXL.IS_COVER_PAGE==true)
		{
			$hoverSpan.bind('click',function(){maximize(null,null,attid,status);});
		}
		
		

	}

}

function mouseEnterDT($this,hText,attid,fontColor,backgroundColor)
{
	if(hText!=undefined){
		
		//console.log("attribute id:"+attid);

		var $help,$maxButton,$description,$pinButton;
		
		var dtSpanLeft=$this.find('.helpDT')[0].getBoundingClientRect().left;
		//console.log("mouse enter dt");	
		if(PLMXL.helpMap[attid]!=undefined)
		{
			if(PLMXL.VIDEO_ATTID!=undefined&&PLMXL.VIDEO_ATTID==attid)
				{clearTimeout(PLMXL.TOUT);
				PLMXL.VIDEO_ATTID=-1;}
			$help=PLMXL.helpMap[attid];
			
			$this.css('background-color',PLMXL.hoverBackGroundColor); 
			
			setToolTip($help.attr("isMaxVisible"),$help.attr("isPinned"),$this,PLMXL.IS_COVER_PAGE);
			
			if($help.css("position") == "fixed") return;
			
			showToolTop($help,attid);
			
			$description=$help.find('.description');
			
			setHelpWidth($help,dtSpanLeft,$help.outerWidth());
			setHelpHeight($help,$this,$description,$help.outerHeight());
			setMaxButton($help,$description);
		}
		else
		{
			$this.css('background-color',PLMXL.hoverBackGroundColor);
			//console.log("hover color set");
			$help = addEmptyHelpDiv(attid);

			PLMXL.helpMap[attid]=$help;
			
			$help.find(".heading span").text($this.text()); //Setting Help Heading to DT Text
			
			$description=$help.find('.description');
			$description.html(strip(hText));
			//console.log("After adding text");
			
			$help
				.css("background-color",backgroundColor)
				.css('word-wrap','break-word')
				.css("color",fontColor);
				
			$description
				.css("width","auto")
				.css("height","auto")
				.css("overflow","auto");
			
			$help.find('.heading')
				.css("color",fontColor)
				.css('border-color',fontColor);
			
			$maxButton=$help.find('.max');
			$maxButton
				.css("background-color",backgroundColor)
				.css('border-color',fontColor);

			$pinButton=$help.find('.pin');
			$pinButton
				.css("background-color",backgroundColor)
				.css('border-color',fontColor);
			
			$help.find('.footer')
				.css('color',fontColor)
				.css('border-color',fontColor);

			$maxButton.bind('click',function(){maximize($help,$description,attid,jQuery($this).text());});
			
			$pinButton.bind('click',function(){pinHandler(null,null,attid,hText,fontColor,backgroundColor,false,jQuery($this).text());});
			
			$maxButton.bind('mouseenter',function(){maxHover($maxButton,fontColor,backgroundColor);});
			
			$maxButton.bind('mouseleave',function(){maxLeave($maxButton,fontColor,backgroundColor);});
			
			$pinButton.bind('mouseenter',function(){pinHover($pinButton,fontColor,backgroundColor);});
			
			$pinButton.bind('mouseleave',function(){pinLeave($pinButton,fontColor,backgroundColor);});
			
			$help.bind('mouseenter',function(){showToolTop($help,attid);});
			
			$help.bind('mouseleave',function(){mouseleaveToolTip($help,attid);});
			
			showToolTop($help,attid);
			
			setHelpWidth($help,dtSpanLeft,PLMXL.HELP_WIDTH);
			
			setHelpHeight($help,$this,$description,PLMXL.HELP_HEIGHT);
			
			setMaxButton($help,$description); // Should be called after setting size
			
			$help.attr("isPinned",false);
			setToolTip($help.attr("isMaxVisible"),$help.attr("isPinned"),$this,PLMXL.IS_COVER_PAGE);
		}
	}
}

function addEmptyHelpDiv (attid)
{
	var newDIV = jQuery("<div class='help-toolTip' id='help-toolTip"+attid+"' style='display:none;'>" +
			"<div>" +
				"<div style:'float:right'>"+
					"<div class='pin' id='pin' title='Click to pin/unpin'>"+
						"<div class='pinOuter' id='pinOuter'>"+
							"<div class='pinInner' id='pinInner'></div>"+
						"</div>" +		
					"</div>" +
					"<div class='max' id='max' title='Click to maximize/minimize'>"+
						"<span class='maximize' id='maxUpper'></span>" +
						"<span class='maximize' id='maxLower'></span>"+
					"</div>" +
				"<div>"+
				"<div class='heading'><span>Heading</span></div>" +
				"<div class='description'id='helpDiscription'>Some detailed Text</div>" +
				"<div class='footer'id='footer'>Assist + &copy Xavor Corporation</div>" +
			"</div>" +
		"</div>");	
	return newDIV.appendTo("body");
}

function setHelpWidth(helpDIV,dtSpanLeft,desiredWidth)
{	
	var helpLeft;
	
	if (desiredWidth < PLMXL.HELP_WIDTH)  desiredWidth = PLMXL.HELP_WIDTH;

	if(helpDIV.css("position") == "fixed")
	{
		var windowWidth= window.innerWidth || (PLMXL.CSS1 ? document.documentElement.clientWidth : document.body.clientWidth);
		
		helpLeft = windowWidth - desiredWidth - PLMXL.HELP_MARGIN_RIGHT - PLMXL.SCROLLBAR_MARGIN;
		
		if (helpLeft < PLMXL.HELP_MARGIN_LEFT)
		{
			desiredWidth = desiredWidth - (PLMXL.HELP_MARGIN_LEFT - helpLeft);
			helpLeft = PLMXL.HELP_MARGIN_LEFT; 
		}
	}
	else
	{
		desiredWidth = calculateHelpWidth(dtSpanLeft,desiredWidth);
		helpLeft = dtSpanLeft - desiredWidth;
	}	

	helpDIV
		.css('width',desiredWidth - PLMXL.HELP_WIDTH_VARIANCE)
		.css("left",helpLeft + "px");
}

function calculateHelpWidth(dtSpanLeft,desiredWidth)
{	
	var availableWidth = dtSpanLeft - PLMXL.HELP_MARGIN_LEFT;
	
	if(desiredWidth > availableWidth)	
		return availableWidth;
	else
		return desiredWidth;
}

function setHelpHeight(helpDIV,dtDIV,descriptionDIV,desiredHeight)
{
	var windowHeight = window.innerHeight || (PLMXL.CSS1 ? document.documentElement.clientHeight : document.body.clientHeight);
	
	var topPosition;
	
	if (desiredHeight < PLMXL.HELP_HEIGHT) desiredHeight = PLMXL.HELP_HEIGHT;
	
	descriptionDIV.css('height','auto');
	
	if (descriptionDIV.height() < (desiredHeight - PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT))
		
		desiredHeight = descriptionDIV.height() + PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT;
	
	if (desiredHeight < PLMXL.HELP_MIN_HEIGHT)  desiredHeight = PLMXL.HELP_MIN_HEIGHT;
	
	if(helpDIV.css("position") == "fixed")
	{
		topPosition = windowHeight - desiredHeight - PLMXL.HELP_MARGIN_BOTTOM;
		
		if (topPosition < PLMXL.HELP_MARGIN_TOP)
		{
			desiredHeight = desiredHeight - (PLMXL.HELP_MARGIN_TOP - topPosition);
			topPosition = PLMXL.HELP_MARGIN_TOP; 
		}
	}
	else
	{
		topPosition=dtDIV[0].getBoundingClientRect().top - PLMXL.HELP_MARGIN_TOP_FROM_DIV;

		if((topPosition + desiredHeight) > (windowHeight - PLMXL.HELP_MARGIN_BOTTOM))
		{
			var availableHeight = windowHeight - PLMXL.HELP_MARGIN_TOP - PLMXL.HELP_MARGIN_BOTTOM;

			if (desiredHeight > availableHeight)
			{
				desiredHeight = availableHeight;
				topPosition = PLMXL.HELP_MARGIN_TOP;
			}		
			else
				topPosition = windowHeight - desiredHeight - PLMXL.HELP_MARGIN_BOTTOM;	
		}
	}
	
	helpDIV
		.css("height",desiredHeight - PLMXL.HELP_HEIGHT_VARIANCE)
		.css("top",topPosition+"px");
	
	descriptionDIV.css('height',helpDIV.outerHeight() - PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT);
	
}

function setMaxButton(helpDIV, desciptionDIV) {
	var maxButton = helpDIV.find('.max');
	var helpDivHeight = helpDIV.outerHeight();
	var helpDivWidth = helpDIV.outerWidth();
	
	if(hasHorizontalScrollbar(desciptionDIV) || hasVerticalScrollbar(desciptionDIV) || helpDivHeight > PLMXL.HELP_HEIGHT || helpDivWidth > PLMXL.HELP_WIDTH) {
		maxButton.show();
		helpDIV.attr('isMaxVisible', true);
	} else {
		maxButton.hide();
		helpDIV.attr('isMaxVisible', false);
	}
}

function maxHover($maxButton,fontColor,backgroundColor)
{
	$maxButton
	.css("background-color",fontColor)
	.css('border-color',backgroundColor)
	.css('cursor','hand');
	$maxButton.find('#maxUpper').css('border-color',backgroundColor);
	$maxButton.find('#maxLower').css('border-color',backgroundColor);
}
function maxLeave($maxButton,fontColor,backgroundColor)
{
	jQuery($maxButton)
	.css("background-color",backgroundColor)
	.css('border-color',fontColor)
	.css('cursor','pointer');
	$maxButton.find('#maxUpper').css('border-color',fontColor);
	$maxButton.find('#maxLower').css('border-color',fontColor);
}
function pinHover($pinButton,fontColor,backgroundColor)
{
	jQuery($pinButton)
	.css("background-color",fontColor)
	.css('border-color',backgroundColor)
	.css('cursor','hand');
	$pinButton.find('#pinOuter').css('border-color',backgroundColor);
	$pinButton.find('#pinInner').css('border-color',backgroundColor);
}
function pinLeave($pinButton,fontColor,backgroundColor)
{
	jQuery($pinButton)
	.css("background-color",backgroundColor)
	.css('border-color',fontColor)
	.css('cursor','pointer');
	$pinButton.find('#pinOuter').css('border-color',fontColor);
	$pinButton.find('#pinInner').css('border-color',fontColor);
}

function mouseLeaveDT($thisDT,attid)
{
	jQuery($thisDT).css('background-color',PLMXL.prevColor);
	if(PLMXL.helpMap[attid]!=undefined)
		{
		if(PLMXL.helpMap[attid].css("position") == "fixed") return;
		hideToolTip(PLMXL.helpMap[attid],attid);
		}
}

function showToolTop($help,attid)
{
	if(PLMXL.VIDEO_ATTID!=undefined&&attid==PLMXL.VIDEO_ATTID)
		{clearTimeout(PLMXL.TOUT);
		PLMXL.VIDEO_ATTID=-1;}
	var $thisDT=(jQuery("form[name=MainForm] #col_"+attid)).prev();
	if($thisDT.length==0)
	{
		$thisDT=(jQuery("form[name=MainForm] #i_"+attid));
	}
	$help.stop().show().css('opacity','1.0');
	
	$thisDT.find(".hoverSpan").css('background-color',PLMXL.hoverBackGroundColor);
}

function mouseleaveToolTip($help,attid)
{
	var $thisDT=(jQuery("form[name=MainForm] #col_"+attid)).prev();
	if($thisDT.length==0)
	{
		$thisDT=(jQuery("form[name=MainForm] #i_"+attid));
	}
	$thisDT.find(".hoverSpan").css('background-color',PLMXL.prevColor);
	if($help!=undefined)
		{
		if($help.css("position") == "fixed")
			{return;}
		hideToolTip($help,attid);
		}

}

function pinHandler($help,$pinButton,attid,hText,fontColor,backgroundColor,flag,text)
{
	if($help==null)
	{
		$help=jQuery("#help-toolTip"+attid);
		$pinButton=$help.find('.pin');
	}
	else
	{
		$help=jQuery($help);
	}
	if($help.css("position") == "fixed")
	{
		unPin($help,attid,hText,fontColor,backgroundColor,flag,text);
	}
	else
	{	
		if(PLMXL.pinStack.length>0)
		{
			unPin(PLMXL.pinStack[0],PLMXL.pinStack[1],PLMXL.pinStack[2],PLMXL.pinStack[3],PLMXL.pinStack[4],false,text);
		}
		PLMXL.pinStack[0]=$help;
		PLMXL.pinStack[1]=attid;
		PLMXL.pinStack[2]=hText;
		PLMXL.pinStack[3]=fontColor;
		PLMXL.pinStack[4]=backgroundColor;
		
		$help.css("position","fixed");
		$help.attr("isPinned",true);
		var windowHeight = window.innerHeight || (PLMXL.CSS1 ? document.documentElement.clientHeight : document.body.clientHeight);
		var windowWidth= window.innerWidth || (PLMXL.CSS1 ? document.documentElement.clientWidth : document.body.clientWidth);
		
		var pinLeft = windowWidth - $help.outerWidth() - PLMXL.HELP_MARGIN_RIGHT - PLMXL.SCROLLBAR_MARGIN;
		var pinTop = windowHeight - $help.outerHeight() - PLMXL.HELP_MARGIN_BOTTOM;
		
		jQuery($help).animate({"left":pinLeft,"top":pinTop}, 30,
        					 function(){
        					 jQuery($help).css('opacity','1.0');
        						});	
		
	}
}

function unPin($help,attid,hText,fontColor,backgroundColor,flag,text)
{
	$help.attr("isPinned",false);
	PLMXL.pinStack=[]; //clearing Stack

	$help.css("position","absolute");
	
	if(flag!=undefined&&flag==true)
	{	
		var $thisDT=(jQuery("form[name=MainForm] #col_"+attid)).prev();
		if($thisDT.length==0)
		{
			$thisDT=(jQuery("form[name=MainForm] #i_"+attid));
		}
		if($thisDT.length==0)
		{
			$thisDT=jQuery( "form[name=MainForm] #WorkflowDiagramTable td :contains("+text+")" );
		}
		if($thisDT.find(".hoverSpan").length==0)
		{
			mouseEnterDT($thisDT,hText,attid,fontColor,backgroundColor);
		}
		else
		{
			mouseEnterDT($thisDT.find(".hoverSpan"),hText,attid,fontColor,backgroundColor);
		}
	}
	else
	{
		hideToolTip($help,attid);
	}
}
function hideToolTip($help,attid)
{
	jQuery($help).fadeOut(PLMXL.FADE_DURATION,stopVideo(attid));
}
function stopVideo(attid)
{
	var iFrame=jQuery(jQuery(PLMXL.helpMap[attid]).find("iframe").get(0));
	var src=iFrame.attr("src");
	if(src!="")
	{
		PLMXL.VIDEO_ATTID=attid;
		PLMXL.TOUT=setTimeout(function()
		{
				iFrame.attr("src","");
				iFrame.attr("src",src);
			
		}, PLMXL.FADE_DURATION);
	}
	
}
function scrollbarHeight() {
    var $inner = jQuery('<div style="width:200px; height:100%;">test</div>');
    var $outer = jQuery('<div style="width:150px; height:150px; position: absolute; top: 0; left: 0; visibility: hidden; overflow:hidden;"></div>').append($inner);
        
    var inner = $inner[0];
    var outer = $outer[0];
     
    jQuery('body').append(outer);
    var heightInner = inner.offsetHeight;
    $outer.css('overflow', 'scroll');
    var heightOuter = outer.clientHeight;
    $outer.remove();
 
    return (heightInner - heightOuter);
}
	
function hasHorizontalScrollbar($description)
{
	return ($description.get(0).scrollWidth > $description.width());
}

function hasVerticalScrollbar($description)
{
	return ($description.get(0).scrollHeight > $description.height());
}

function maximize($help,$description,attid,text)
{
	if($help==null)
	{
		$help=jQuery("#help-toolTip"+attid);
		$description=$help.find('.description');
	}

	if ($help.find('.max').is(":hidden")) return;
	
	var $helpDT=(jQuery("form[name=MainForm] #col_"+attid)).prev();
	if($helpDT.length==0)
	{
		$helpDT=(jQuery("form[name=MainForm] #i_"+attid));
	}
	if($helpDT.length==0)
	{
		$helpDT=jQuery( "form[name=MainForm] #WorkflowDiagramTable td :contains("+text+")" );
	}
	var dtSpanLeft=$helpDT.find('.helpDT')[0].getBoundingClientRect().left;
	
	var windowHeight = window.innerHeight || (PLMXL.CSS1 ? document.documentElement.clientHeight : document.body.clientHeight);
	var windowWidth= window.innerWidth || (PLMXL.CSS1 ? document.documentElement.clientWidth : document.body.clientWidth);
	
	var originalHeight = $help.outerHeight();
	var originalWidth = $help.outerWidth();
	
	var needToMinimize = true;
	var helpWidth = originalWidth;
	
	if(hasHorizontalScrollbar($description) || hasVerticalScrollbar($description))
	{
		if (hasVerticalScrollbar($description))
		{
			$description.css('height','auto');
			
			setHelpHeight($help,$helpDT,$description,$description.height() + PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT);
		}
		
		if (hasHorizontalScrollbar($description))
		{
			var desiredDescriptionWidth = $description.get(0).scrollWidth;
			
			if (hasVerticalScrollbar($description)) desiredDescriptionWidth = $description.get(0).scrollWidth + PLMXL.SCROLLBAR_MARGIN;
			
			setHelpWidth($help,dtSpanLeft,desiredDescriptionWidth + PLMXL.HELP_WIDTH_VARIANCE);	
			
			//AGAIN Setting Height for Scroll Bar Adjustment
			$description.css('height','auto');
			
			setHelpHeight($help,$helpDT,$description,$description.height() + PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT);
		}
		
		//Checking if maximize has incread size. If not then we should minimize
		needToMinimize = (originalHeight == $help.outerHeight()) && (originalWidth == $help.outerWidth());
	}
	
	if (needToMinimize)
	{
		var topPosition,leftPosition;
		
		if($help.css("position") == "fixed")
		{
			topPosition = windowHeight - PLMXL.HELP_HEIGHT - PLMXL.HELP_MARGIN_BOTTOM;
			leftPosition = windowWidth - PLMXL.HELP_WIDTH - PLMXL.HELP_MARGIN_RIGHT - PLMXL.SCROLLBAR_MARGIN;
			helpWidth = PLMXL.HELP_WIDTH;
		}
		else
		{
			var dtSpanLeft=$helpDT.find('.helpDT')[0].getBoundingClientRect().left;
			
			helpWidth = calculateHelpWidth(dtSpanLeft,PLMXL.HELP_WIDTH);
			
			leftPosition = dtSpanLeft - helpWidth;
			
			topPosition=$helpDT[0].getBoundingClientRect().top - PLMXL.HELP_MARGIN_TOP_FROM_DIV;
			
			var availableHeight = windowHeight - PLMXL.HELP_MARGIN_BOTTOM;

			if ((topPosition + PLMXL.HELP_HEIGHT) > availableHeight)
			{
				topPosition = availableHeight - PLMXL.HELP_HEIGHT;	
			}
		}
		
		jQuery($help).animate({
			"top":topPosition,
			"left":leftPosition,
			"height":PLMXL.HELP_HEIGHT - PLMXL.HELP_HEIGHT_VARIANCE,
			"width":helpWidth - PLMXL.HELP_WIDTH_VARIANCE},PLMXL.FADE_DURATION,function(){
				$description.css({'height':jQuery($help).outerHeight()-PLMXL.HELP_HEADER_AND_FOOTER_HEIGHT});});
	}
}

function setToolTip(isMaxVisible,isPinned,$thisDT,isCoverPage)
{
	if(isMaxVisible=="true"&&isPinned=="true")
	{
		if(isCoverPage)
		{
			$thisDT.attr('title', '*Assist Text is already pinned. Right click to unpin.\nLeft click to maximize/minimize.');
		}
		else
		{
			$thisDT.attr('title', '*Assist Text is already pinned. Right click to unpin.');
		}
	}
	if(isMaxVisible=="false"&&isPinned=="false")
	{
		$thisDT.attr('title', 'Right click to unpin/unpin');
	}
	if(isMaxVisible=="true"&&isPinned=="false")
	{
		if(isCoverPage)
		{
			$thisDT.attr('title', 'Right Click to pin/unpin. \nLeft click to maximize/minimize.');
		}
		else
		{
			$thisDT.attr('title', 'Right Click to pin/unpin.');
		}
	}
	if(isMaxVisible=="false"&&isPinned=="true")
	{
		$thisDT.attr('title', '*Assist Text is already pinned. Right click to unpin.');
	}
}

function loadAssistPlus() {
	try {
		var classid = eval('document.MainForm.subclsid').value;
		if(classid != undefined && classid != "") {
			getAssistText(classid);
		}
	} catch(err) {}
}

function optOutToggle(userid,classid) {
	var mode = "";
	var $showAssistText = jQuery("#showAssistText");
	$showAssistText.unbind('click');
	$showAssistText.css("cursor","wait");
	if($showAssistText.attr("flag") == "true") {
		mode = "on";
	} else {
		mode = "off";
	}
	var arg = {};
	arg.url = "/AssistPlus/OptOut";
	jQuery.post(arg.url, {
		userid:userid,
		mode:mode,
		rnd:Math.random()},
		function(response) {
			loadAssistPlus();	
		}, "json");
}

function getAssistText(classid) {
	initPLMXL();
	cleanUpDivs();
	
	var $headingDiv = jQuery("#Actions").parent();
	var arg = {};
	arg.url = "/AssistPlus/GetAssistText";
	var title = document.title;
	var userid = title.substring(title.lastIndexOf("(")+1,title.lastIndexOf(")"));
	
	var wfId = "";
	try {
		var wf = jQuery("form[name=MainForm] #col_"+PLMXL.WORKFLOW_BASEID);
		if(wf[0].className == "multiselect_textbox") {
			// Edit Mode
			var wfcombo = wf[0];
			var wfcomboSelect = wfcombo.children;
			var value = wfcomboSelect[0];
			if(value != undefined) {
				wfId=jQuery(value).find('option:selected').text();
			} else {
				wfId=wf.text();
			}
		} else {
			// Normal Mode
			wfId=wf.text();
		}
	} catch(e) {}
	
	var lifecycleId = "";
	try {
		lifecycleId = jQuery("form[name=MainForm] #col_"+PLMXL.LIFECYCLE_BASEID).text();
	} catch(e) {}
	
	var statusId = "";
	try {
		statusId = jQuery("form[name=MainForm] #col_"+PLMXL.STATUS_BASEID).text();
	} catch(e) {}
	
	var objID = "";
	try	{
		objID = jQuery("form[name=MainForm] .column_one h2").text();
	} catch(e) {}
	
	var fulltabledisplay = eval('document.MainForm.fulltabledisplay').value;
	var fullgridtableid = eval('document.MainForm.fullgridtableid').value;
	var loadAssistForMainObject = "true";
	var loadAssistForAffectedItem = "false";
	var itemNumber = getItemNumberFromRedlinesTable();
	
	if(itemNumber && itemNumber.length > 0) {
		var colNums = getColNums();
		if(colNums.length > 0) {
			cacheAffectedItems(colNums);
		}
		
		var jObj = PLMXL.affectedItemsCache[itemNumber];
		if(jObj) {
			loadAssistForAffectedItem = "true";
			if("true" == fulltabledisplay && "ITEMTABLE_TITLEBLOCK_RED" == fullgridtableid) {
				loadAssistForMainObject = "false";
			}
			var oldLifecycle = jObj.oldLifecycle;
			var newLifecycle = jObj.newLifecycle;
			
			lifecycleId = "";
			if(newLifecycle === ""){
				if(!(oldLifecycle === "") ){
					lifecycleId = oldLifecycle;
				}
			} else{
				lifecycleId = newLifecycle;
			}
		}
	}
	
	jQuery.post(arg.url, {
		classid:classid,
		workflowID:wfId,
		workflowStatusID:statusId,
		lifecycleID:lifecycleId,
		userid:userid,
		objID:objID,
		affectedItem:itemNumber,
		loadAssistForAffectedItem:loadAssistForAffectedItem,
		loadAssistForMainObject:loadAssistForMainObject,
		rnd:Math.random()},
		function(response) {
			var isOptedOut = response.isOptedOut;
			
			jQuery('#showAssistText').remove();
			$headingDiv.append("<img id='showAssistText' class='showAssistText'>");
			var $showAssistText = jQuery("#showAssistText");
			
			//var browserVersion = j.browser.version.slice(0,j.browser.version.indexOf("."));
			var documentVersion = document.documentMode;
			if(documentVersion == "7") {
				$showAssistText.css("margin-top", "11px");
			}
				
			if(isOptedOut != undefined && isOptedOut == "true") {
				jQuery('form[name=MainForm] dt').each(function(index) {
					var $thisDT = jQuery(this);
					var $hoverSpan = $thisDT.find('.hoverSpan');
					$hoverSpan.unbind('mouseenter').unbind('mouseleave').unbind('contextmenu').unbind('click');
					if($hoverSpan.length > 0) {
						var text = $hoverSpan.text();
						jQuery($thisDT).remove('.hoverSpan');
						if($thisDT.text() == undefined || $thisDT.text() == "")	{
							$thisDT.text(text);
						}
					}
					$thisDT.css('color', PLMXL.ORIG_LABEL_COLOR);
					//$thisDT.remove('.hoverSpan');
					//$thisDT.text(text);
				});
				
				jQuery('form[name=MainForm] .GMHeadMid .GMHeaderRow td span').each(function(index) {
					var $thisDT = jQuery(this);
					var $hoverSpan = $thisDT.find('.hoverSpan');
					$hoverSpan.unbind('mouseenter').unbind('mouseleave').unbind('contextmenu').unbind('click');
					if($hoverSpan.length > 0) {
						var text = $hoverSpan.text();
						jQuery($thisDT).remove('.hoverSpan');	
						if($thisDT.text() == undefined || $thisDT.text() == "")	{
							$thisDT.text(text);
						}
					}
					$thisDT.css('color', PLMXL.ORIG_LABEL_COLOR);
					//$thisDT.remove('.hoverSpan');
					//$thisDT.text(text);
				});
				
				jQuery('form[name=MainForm] #WorkflowDiagramTable td').each(function(index) {
					var $thisDT = jQuery(this);
					var $hoverSpan = $thisDT.find('.hoverSpan');
					$hoverSpan.unbind('mouseenter').unbind('mouseleave').unbind('contextmenu').unbind('click');
					if($hoverSpan.length > 0) {
						var text = $hoverSpan.text();
						jQuery($thisDT).remove('.hoverSpan');	
						if($thisDT.text() == undefined || $thisDT.text() == "")	{
							$thisDT.text(text);
						}
					}
					$thisDT.css('color', PLMXL.ORIG_LABEL_COLOR);
					//$thisDT.remove('.hoverSpan');
					//$thisDT.text(text);
				});
				
				$showAssistText.attr('src','data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAACHDwAAjA8AAP1SAACBQAAAfXkAAOmLAAA85QAAGcxzPIV3AAADAFBMVEUAAACVlZWWlpaZmZmampqcnJydnZ2hoaGmpqarq6u8vLzHx8fKysoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABUB+l4AAABAHRSTlP///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8AU/cHJQAAAAlwSFlzAAAOwwAADsMBx2+oZAAAABp0RVh0U29mdHdhcmUAUGFpbnQuTkVUIHYzLjUuMTFH80I3AAAAX0lEQVQoU2WP2xZAEQhEi+MS/f/vdmjwwDxoZi9UZJccaI3MseoCWopbVDL9PE5NS5ZOHiQZdVnBJUp5XWCUTAFmgwDAkIPnyf4UQPrb9h1sjNwQpY/Tl2u+HDpd65v9KbWudRxqsCEAAAAASUVORK5CYII=')
					.attr("flag", false)
					.attr("class", "showAssistText disable")
					.attr("title", "Click to enable Assist+ Help.")
					.bind('click', function() {
						optOutToggle(userid, classid);
					});
				
				$showAssistText.css("cursor", "default");
				
				return;
			}
			
			$showAssistText.unbind('click');
			$showAssistText.attr('src','data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAABGdBTUEAALGOfPtRkwAAACBjSFJNAACHDwAAjA8AAP1SAACBQAAAfXkAAOmLAAA85QAAGcxzPIV3AAADAFBMVEUAAADapAPapATapQXbqA3cqA7dqhXdqxberh/eryHgsy3itzflv07qzHPt1Ifx3qP26L/58dkAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADrGmjzAAABAHRSTlP///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8AU/cHJQAAAAlwSFlzAAAOwwAADsMBx2+oZAAAABp0RVh0U29mdHdhcmUAUGFpbnQuTkVUIHYzLjUuMTFH80I3AAAAa0lEQVQoU2WP4Q6AIAiEKS21KOT9H9bgtLbqfsDdtzGA2kcAssV5jpsMICXDSi7qQBZEl1vS9clGViXeR4B2pmRzrglVEwWYG7TQwdQF8BvhE26Ak6m+19b/Ydb66SqZreG5A8/1TZ/3W7sAOIqngZxgthQAAAAASUVORK5CYII=')
				.attr("flag", true)
				.attr("class", "showAssistText enable")
				.attr("title", "Click to disable Assist+ Help.")
				.bind('click', function() {
					optOutToggle(userid, classid);
				});
			
			var mainGridDomID = "form[name=MainForm] .GMHeadMid .GMHeaderRow td span";
			var affectedItemAssist = response.affectedItemAssist;
			if(affectedItemAssist) {
				var rlHelpText = affectedItemAssist.helpText;
				var rlAttColorsData = affectedItemAssist.attColors;

				jQuery("#rightpanecontentsubTabs dt").each(function(index) {
					var $thisDT = jQuery(this);
					if (PLMXL.prevColor == undefined) {
						PLMXL.prevColor = $thisDT.css('background-color');	
					}
					bindHelpDt($thisDT, rlHelpText, rlAttColorsData, null, null);
				});
     				
				jQuery("#rightpanecontentsubTabs .GMHeadMid .GMHeaderRow td span").each(function(index) {
					var $thisDT = jQuery(this);
					if(($thisDT.parents().eq(8)).attr('id')==undefined||!(($thisDT.parents().eq(8)).attr('id').toLowerCase().indexOf("redline") >= 0)) {
						if (PLMXL.prevColor == undefined) {
							PLMXL.prevColor = $thisDT.css('background-color');
						}
						bindHelpDt($thisDT, rlHelpText, rlAttColorsData, null, null);
					}		
				});
				
				mainGridDomID = "#CHANGETABLE_AFFECTEDITEMS .GMHeadMid .GMHeaderRow td span";
			}
			
			var mainObjectAssist = response.mainObjectAssist;
			if(mainObjectAssist) {
				var helpText = mainObjectAssist.helpText;
				var attColorsData = mainObjectAssist.attColors;
	
				jQuery("form[name=MainForm] dt").each(function(index) {
					var $thisDT = jQuery(this);
					if (PLMXL.prevColor == undefined) {
						PLMXL.prevColor = $thisDT.css('background-color');	
					}
					bindHelpDt($thisDT, helpText, attColorsData, null, null);
				});
		        		
				jQuery("form[name=MainForm] #WorkflowDiagramTable td").each(function(index) {
					var $thisDT = jQuery(this);			
					var text = $thisDT.text();	
					if(text != undefined && text != "") {
						var $workflow = jQuery('#WorkflowDiagram_toggle');
						var attrid = jQuery.trim($workflow.text().substring($workflow.text().lastIndexOf("(")+1,$workflow.text().lastIndexOf(")")))+jQuery.trim(text);
						attrid = attrid.replace(/\s+/g, '');
						if (PLMXL.prevColor == undefined) {
							PLMXL.prevColor = $thisDT.css('background-color');	
						}
						bindHelpDt($thisDT, helpText, attColorsData, attrid, text);
					}
				});
	 				
				jQuery(mainGridDomID).each(function(index) {
					var $thisDT = jQuery(this);
					if(($thisDT.parents().eq(8)).attr('id')==undefined||!(($thisDT.parents().eq(8)).attr('id').toLowerCase().indexOf("redline") >= 0)) {
						if (PLMXL.prevColor == undefined) {
							PLMXL.prevColor = $thisDT.css('background-color');	
						}
						bindHelpDt($thisDT, helpText, attColorsData, null, null);
					}		
				});
			}
			
			jQuery("form[name=MainForm] .GMHeadMid .GMHeaderRow").unbind("click").bind("click", function (e) {
	        	if (e.which == 1) {
	        		setTimeout(function() {
		               loadAssistPlus();
        			}, PLMXL.FADE_DURATION);
        		}            
	        });
			
			jQuery(".table_views").unbind("change").bind("change", function (e) {
        		setTimeout(function() {
	               loadAssistPlus();
    			}, PLMXL.FADE_DURATION);
			});
			
		}, "json");
	
	jQuery("#showAssistText").css("cursor","default");
}

function getColNums() {
	var colNums = [];
	try {
		jQuery('#CHANGETABLE_AFFECTEDITEMS .GMHeadMid .GMHeaderRow .AutoWrapHeader').children('span').each(function(index) {
		    var baseID = this.id;
			if(baseID.indexOf("1054") > -1) {
				colNums[0] = index;
			} else if(baseID.indexOf("4500") > -1) {
				colNums[1] = index;
			} else if(baseID.indexOf("1057") > -1) {
				 colNums[2] = index;
			}
		});
	} catch(ex){}
	
	return colNums;
}

function getItemNumberFromRedlinesTable() {
	var itemNumber = '';
	try {
		var h4 = jQuery('#subTabContentDiv h4');
		var h4Trimmed = h4.text().trim().replace(/ /g, '');
		if(h4Trimmed.indexOf('Redlinesfor') == 0) {
			itemNumber = h4Trimmed.replace('Redlinesfor','');
		}
	} catch(ex) {
		itemNumber = '';
	}
	
	return itemNumber;
}

function getSelectedRowNoUsingItemNumber(itemNumber) {
	var rowNum = -1;
	try {
		if(itemNumber && itemNumber.length > 0) {
			jQuery('form[name=MainForm] .GMBodyMid .GMPageOne tr .GMText a').each(function(index) {
				var rowVal = $(this).text;
				if(rowVal === itemNumber ){
					rowNum = index;
					return false;
				}
			});
		}
	} catch(ex) {}
	
	return rowNum;
}

function getSelectedRowData(rowNum, colNums) {
	var rowValues = [];
	try {
		var temp = parseInt(rowNum) + 1;
		var selectedRowTr = jQuery('form[name=MainForm] .GMBodyMid .GMPageOne tr:eq(' + temp + ') .GMText');

		//getting item number
		var itemNumber = selectedRowTr.eq(colNums[0]).text().trim();
		//getting new lifecycle
		var oldLifecycle = selectedRowTr.eq(colNums[1]).text().trim();
		//getting old lifecycle
		var newLifecycle = selectedRowTr.eq(colNums[2]).text().trim();

		rowValues[0] = itemNumber;
		rowValues[1] = oldLifecycle;
		rowValues[2] = newLifecycle;
	} catch(ex) {}
	
	return rowValues;
}

function cacheAffectedItems(colNums) {
	PLMXL.affectedItemsCache = {};
	jQuery('#CHANGETABLE_AFFECTEDITEMS .GMBodyMid .GMDataRow').each(function(index) {
		var $affectedItemRows = jQuery(this).children('.GMText');
		var itemNumber = $affectedItemRows.eq(colNums[0]).text().trim();
		var jObj = {};
		jObj.oldLifecycle = $affectedItemRows.eq(colNums[1]).text().trim();
		jObj.newLifecycle = $affectedItemRows.eq(colNums[2]).text().trim();
		PLMXL.affectedItemsCache[itemNumber] = jObj;
	});
}