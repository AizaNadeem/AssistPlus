$.template("classRow","<tr id=${idVal} data-level=${level} class=${classVal}>"+
		"<td id='className'  class=style${level}>${name}</td>"+
		"<td id='hasTextFlag'>{{if hasTextFlag=='yes'}}<img src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text'>{{/if}}</td></tr>");


$.template("attributeRow", "<tr id=${attrIdVal} data-attr=${classIdVal};${attrIdVal} data-attrcolors=${assistColorId};${assistColor} class='attrRow'" +
		"{{if hasTextFlag=='yes'}} " +
		"title='Has Assist Text, Click to view already added texts. Right click to add text.'" +
		"{{else}} " +
		"title='Right click to add text.'" +
		"{{/if}} >"+
		"<td class='attrName'><a {{if isVisible=='no'}} class='attrVisible' {{/if}}>${attrName}</a></td>"+
		"<td id=HasAssistText >" +
		"{{if hasTextFlag=='yes'}}" +
		"<img src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text, Click to view already added texts. Right click to add text.'>" +
		"{{/if}}" +
		"</td>"+
		"<td class='attColorTd' title='Click to select Label Color. Right click to add text.' style='text-align: center;' ></td>"+
		"<td>${attrDescription}</td></tr>");

$.template( "textRow", 		
		"<tr>" +
		"<td class='workflows'  rowspan='2'>" +
			"<div  class='wfcombo'>" +
				"<select id=workflowSelect${TextID} data-Textid='' data-selectedStatus='' class='wfcomboSelect'></select>" +
			"</div>" +
			"<div  class='statuscombo'>" +
			"<select multiple='multiple' size=3  id=statusSelect${TextID} class='statusComboSelect' ></select>" +
			"</div>" +
		"</td>"+
		"<td class='roles' rowspan='2'><select class='roleList' id=roleop${TextID} multiple='multiple'>{{each roles}}" +
			"{{html $value}}"+
			"{{/each}}</select>"+
		"</td>" +
		"<td rowspan='2' class='rowTextWidth'>" +
		"<div id=rowText title='Click to Edit Text'><div id='transparentDiv'></div>{{html AssistText}}</div>" + //Tranparent div added so that clicking on text box only
		"</td>" +
		"<td id='diffColor' class='pickertd' data-action=${isDiffColor} data-colors=${fontcolor};${background}></td>" +
		
		"<td id='save-delete' rowspan='2'>" +
			"<a  data-id=${TextID} class='save' data-action='save'>" +
				"<img alt='Save' title='Save Text' src='img/save3.png'>" +
			"</a>" +
				"<br><br>" +
			"<a data-id=${TextID} class='remove' data-action='remove' >" +
				"<img alt='Remove'  title='Remove Text'  src='img/delete3.png'>" +
			"</a>"+
		"</td>"+
		"</tr>"+
		"<tr>"+
		"<td colspan='1' class='updatedDiv' style='	padding:0px;border-top: 0px;'  >"+
		"<label class='lastupdated' >Updated (Server Time)</label><br>"+
		"<label class='updatedDate'>{{if lastUpdated=='' }}N/A{{/if}}{{if lastUpdated!='' }}{{html lastUpdated}}{{/if}}</label>" +
		"</td>"+
		"</tr>"
		
		);

$.template("licRow","<tr>"+
		"<td class='licRowValid' >${ValidUntil}</td>"+
		"<td class='licRowArg' >${arguments[1]}</td></tr>");
