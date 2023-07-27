var AssistColorPicker=(function(){
	
	function createOptionList(colorClass,title)
	{
		var colors="#000000^#ffffff,"+
					"#ffffff^#333333,"+
					"#ffffff^#330000,"+
					"#ffffff^#666666,"+
					"#000000^#cccccc,"+
					"#ffffff^#333366,"+
					"#ffffff^#666699,"+
					"#000000^#99ccff,"+
					"#ffffff^#330099,"+
					"#ffffff^#3333cc,"+
					"#ffffff^#6633cc,"+
					"#ffffff^#000066,"+
					"#ffffff^#006633,"+
					"#000000^#33ff66,"+
					"#000000^#ffff00,"+
					"#000000^#ffcc00,"+
					"#000000^#ff9900,"+
					"#ffffff^#cc3300,"+
					"#ffffff^#cc0000,"+
					"#ffffff^#990033";

		var colorList=colors.split(",");
		var html="<span class='colorSelector "+colorClass+"'><select>";
		var optionList="";
			for(var i=0;i<colorList.length;i++)
			{
				optionList+="<option id="+colorList[i].replace(/\#/gi,'')+" value="+colorList[i].replace(/\#/gi,'')+" >"+colorList[i]+"</option>";
			}
			return html=html+optionList+"</select>"+title+"</span>";
	}
	function createFontList(colorClass,title)
	{
		var colors="#666^#ffffff,"+
					"#B0171F^#ffffff,"+
					"#FF1493^#ffffff,"+
					"#CD00CD^#ffffff,"+
					"#0000FF^#ffffff,"+
					"#00BFFF^#ffffff,"+
					"#2E8B57^#ffffff,"+
					"#32CD32^#ffffff,"+
					"#FFB90F^#ffffff,"+
					"#8B7E66^#ffffff,"+
					"#FF8000^#ffffff,"+
					"#FA8072^#ffffff,"+
					"#FF0000^#ffffff,"+
					"#FF00FF^#ffffff";

		var colorList=colors.split(",");
		var html="<span class='colorSelector "+colorClass+"'><select>";
		var optionList="";
			for(var i=0;i<colorList.length;i++)
			{
				optionList+="<option id="+colorList[i].replace(/\#/gi,'')+" value="+colorList[i].replace(/\#/gi,'')+" >"+colorList[i]+"</option>";
			}
			return html=html+optionList+"</select>"+title+"</span>";
	}
	function rgb2hex(rgb){
		if(rgb.indexOf('rgb')==0){
			rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);  
			return "#" +   ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) +   ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) +   ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
		}else{
			return rgb;
		}
	}
	
	function createColorPicker($target,fontColor,backgroundColor,showTitle)
	{
		if($target.find('.colorSelector').length==0){
			var fonttitle=" Font";
			var bgtitle=" Background";
			if(showTitle==undefined){
				fonttitle="";
				bgtitle="";
			}
			var $themePickerDiv=$(createOptionList("fontcolor",fonttitle));
			
			$themePickerDiv.find('select option#'+fontColor).attr('selected','selected');
			
			$themePickerDiv.find('select').colourPicker({fontcolor:fontColor,bgcolor:backgroundColor});
			
			$target.append($themePickerDiv);
			
			if(showTitle!=undefined){
				$themePickerDiv.css('margin-top','3px');
				$themePickerDiv.css('width','100%');
			
			}
		}
	}
	function createFontColorPicker($target,fontColor,backgroundColor,showTitle)
	{
		if($target.find('.colorSelector').length==0){
			var fonttitle=" Font";
			var bgtitle=" Background";
			if(showTitle==undefined){
				fonttitle="";
				bgtitle="";
			}
			var $themePickerDiv=$(createFontList("fontcolor",fonttitle));
			
			$themePickerDiv.find('select option#'+fontColor).attr('selected','selected');
			
			$themePickerDiv.find('select').colourPicker({fontcolor:fontColor,bgcolor:backgroundColor});
			
			$target.append($themePickerDiv);
			
			if(showTitle!=undefined){
				$themePickerDiv.css('margin-top','3px');
				$themePickerDiv.css('width','100%');
			
			}
		}
	}
	function createBackgroundColorPicker($target,fontColor,backgroundColor,showTitle)
	{
		if($target.find('.colorSelector').length==0){
			var fonttitle=" Font";
			var bgtitle=" Background";
			if(showTitle==undefined){
				fonttitle="";
				bgtitle="";
			}
			var $themePickerDiv=$(createFontList("fontcolor",fonttitle));
			
			$themePickerDiv.find('select option#'+fontColor).attr('selected','selected');
			
			$themePickerDiv.find('select').colourPicker({fontcolor:fontColor,bgcolor:backgroundColor,isBG:true});
			
			$target.append($themePickerDiv);
			
			if(showTitle!=undefined){
				$themePickerDiv.css('margin-top','3px');
				$themePickerDiv.css('width','100%');
			
			}
		}
	}
	
	return{
		getHexColor:rgb2hex,
		createPicker:createColorPicker,
		createFontColorPicker:createFontColorPicker,
		createBackgroundColorPicker:createBackgroundColorPicker
	}

})();