var AssistColorPicker=(function(){
	
	function createOptionList(colorClass,title)
	{
				var colors="#ffffff^#000000,"+
					"#000000^#99ccff,"+
					"#840303^#E6E6E6,"+
					"#433900^#EEECE1,"+
					"#4B0C2D^#D6E5F6,"+
					"#000000^#F6E1E0,"+
					"#013620^#EBF1DD,"+
					"#342352^#EDE9F4,"+
					"#650103^#DBEEF3,"+
					
					"#5F2E00^#FDEADA,"+
					"#584501^#FEF1D9,"+
					"#00183F^#D5E2E6,"+
					"#D3D3D3^#848383,"+
					"#62B4B7^#2C2C2C,"+
					"#000000^#cccccc,"+
					"#DFB301^#570303,"+
					"#5CBDC8^#17365D,"+
					"#D8FB93^#526330,"+
					"#AEEDF3^#3F3151,"+
					"#84FAE5^#205867,"+
					"#ffffff^#666699,"+
					"#00B2DF^#002060,"+
					"#61CD99^#014C28,"+
					"#D59696^#602B2B";

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
					"#710000^#ffffff,"+
					"#7d014d^#ffffff,"+
					"#35256a^#ffffff,"+
					"#354a70^#ffffff,"+
					"#004aa5^#ffffff,"+
					"#005f8b^#ffffff,"+
					"#026f73^#ffffff,"+
					"#a45901^#ffffff,"+
					"#01580f^#ffffff,"+
					"#2e8101^#ffffff,"+
					"#858383^#ffffff,"+
					"#2e2e2e^#ffffff,"+
					"#17365d^#ffffff";

		var colorList=colors.split(",");
		var html="<span class='colorSelector "+colorClass+"'><select>";
		var optionList="";
			for(var i=0;i<colorList.length;i++)
			{
				optionList+="<option id="+colorList[i].replace(/\#/gi,'')+" value="+colorList[i].replace(/\#/gi,'')+" >"+colorList[i]+"</option>";
			}
			return html=html+optionList+"</select>"+title+"</span>";
	}
	
	function createBackgroundList(colorClass,title)
	{
		var colors="#666^,"+
					"#333333^#e6e6e6,"+
					"#333333^#eeece1,"+
					"#333333^#d6e5e6,"+
					"#333333^#f6e1e0,"+
					"#333333^#ebf1dd,"+
					"#333333^#ede9f4,"+
					"#333333^#dbeeef3,"+
					"#333333^#fdeada,"+
					"#333333^#fef1d9,"+
					"#333333^#d5e2e6,"+
					"#333333^#848383,"+
					"#ffffff^#570303,"+
					"#ffffff^#17365d";
				//	+"#333333^#d8fb93";

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
	
	function createColorPicker($target,fontColor,backgroundColor,defaultColor,isRoles,showTitle)
	{
		if($target.find('.colorSelector').length==0){
			var fonttitle=" Font";
			var bgtitle=" Background";
			if(showTitle==undefined){
				fonttitle="";
				bgtitle="";
			}
			var $themePickerDiv=$(createOptionList("fontcolor",fonttitle));
			
			$themePickerDiv.find('select option'+fontColor).attr('selected','selected');
			
			$themePickerDiv.find('select').colourPicker({fontcolor:fontColor,bgcolor:backgroundColor,dfColor:defaultColor,rolesScreen:isRoles});
			
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
			var $themePickerDiv=$(createBackgroundList("fontcolor",fonttitle));
			
			$themePickerDiv.find('select option'+fontColor).attr('selected','selected');
			
			$themePickerDiv.find('select').colourPicker({fontcolor:fontColor,bgcolor:backgroundColor,isBG:true});
			
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
			
			$themePickerDiv.find('select option'+fontColor).attr('selected','selected');
			
			$themePickerDiv.find('select').colourPicker({fontcolor:fontColor,bgcolor:backgroundColor,isLabel:true});
			
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
	};

})();