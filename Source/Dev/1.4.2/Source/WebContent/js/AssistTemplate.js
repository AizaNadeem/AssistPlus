$.template("classRow","<tr id=${idVal} data-level=${level} class=${classVal}>"+
		"<td style='width:85%' class=style${level}>${name}</td>"+
		"<td style='width:16px;text-align:right;'>{{if hasTextFlag=='yes'}}<img src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text'>{{/if}}</td></tr>");


$.template("attributeRow", "<tr id=${attrIdVal} data-attr=${classIdVal};${attrIdVal} data-attrcolors=${assistColorId};${assistColor} style='width=100%;'>"+
		"<td style='width:250px;'><a>${attrName}</a></td>"+
		"<td id=HasAssistText style='width:16px;border-right:1px solid #9097A9;border-left:1px solid #9097A9;'>{{if hasTextFlag=='yes'}}<img src='img/dot.gif' alt='Has Assist Text' title='Has Assist Text'>{{/if}}</td>"+
		"<td >${attDescription}</td></tr>");

$.template( "textRow", 	"<tr><td class='roles' style='width: 20%;'><select class='roleList' id=roleop${textID} multiple='multiple' style='width=100%;'>{{each roles}}" +
							"{{html $value}}"+
							"{{/each}}</select></td>" +
							"<td class='isDiff' style='text-align: right; width: 5%'><input class='chk' type='checkbox' {{if isDiffColor}}checked=checked{{/if}}  /></td>" +
							"<td style='width: 20%;min-width:132px;' class=pickertd data-colors=${fontcolor};${background}><span class='text' style='display:none;'>Use Different Theme</span></td>" +
							"<td><div id=rowText>{{html assistText}}</div></td>" +
							"<td style='text-align: right; width: 4%;'><a  data-id=${textID} class='save' data-action='save'><img alt='Save' title='Save' src='img/save.png'></a><br><br><a data-id=${textID} class='remove' data-action='remove'><img alt='Remove' title='Remove' src='img/remove.png'></a></td></tr>") ;

$.template("licRow","<tr>"+
		"<td style='width:15%'>${ValidUntil}</td>"+
		"<td style='width:85%;text-align:right;'>${arguments[1]}</td></tr>");
