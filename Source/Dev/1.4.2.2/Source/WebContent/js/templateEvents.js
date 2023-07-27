$(document).ready(function(){

	var browser=$.browser;
	var bodyClass="";
	if(browser.msie ){
		if(browser.version == "7.0"){
			bodyClass="ie7";
		}else if(browser.version == "8.0"){
			bodyClass="ie8";	
		}else {bodyClass="ie"; }
	}else if(browser.mozilla){
		bodyClass="firefox";
	}else if(browser.webkit){
		bodyClass="webkit";
	}
	$('body').addClass(bodyClass);
	//Sidebar Accordion Menu:
		$("#main-nav li ul").hide();
		$("#main-nav li a.current").parent().find("ul").slideToggle("slow");
		$("#main-nav li a.nav-top-item").click( 
			function () {
				var $this=$(this);
				$this.parent().siblings().find("ul").slideUp("normal");
				$this.next().slideToggle("normal");
				$this.parent().siblings().find('.current').removeClass('current');
				if($this.hasClass('current')){
					$this.removeClass('current');
					$('#main-content').hide();
				}else{
					$('#main-content').show();
					$this.addClass('current');
					var $selectedNavElement= $this.parent().find("ul li");
					var $alreadySelected=$this.parent().find("ul li .current");
					if($alreadySelected.length==0){
						if($selectedNavElement.length > 0){
							setTimeout(function(){
								$($selectedNavElement[0]).find('a').click();	
							},300);
							
						}
					}
				}
				return false;
			}
		);
		var $selected =$("#main-nav li a.nav-top-item");
		if($selected.length>0){
			$selected.eq(0).click();
		}
		
		$("#main-nav ul li a").click(function(){
			$('.content-box-header h3').html($(this).html());
			$(this).parent().siblings().find('a').removeClass('current');
			$(this).addClass('current');
		});
		
		var $selectedFirstElement= $selected.parent().find('ul li');
		if($selectedFirstElement.length > 0){
			$($selectedFirstElement[0]).find('a').click();
		}
		
		$('.content-box-content').height($(window).height()-215);
		$('#sidebar').height($(window).height()-54);
		$(window).resize(function(){
			$('.content-box-content').height($(window).height()-215);
			$('#sidebar').height($(window).height()-54);
		});
		
		$("#main-nav li .nav-top-item").hover(
			function () {
				$(this).stop().animate({ paddingRight: "25px" }, 200);
			}, 
			function () {
				$(this).stop().animate({ paddingRight: "15px" });
			}
		);


		
		$(".content-box-header h3").css({ "cursor":"s-resize" }); // Give the h3 in Content Box Header a different cursor
		$(".closed-box .content-box-content").hide(); // Hide the content of the header if it has the class "closed"
		$(".closed-box .content-box-tabs").hide(); // Hide the tabs in the header if it has the class "closed"
		
		$(".content-box-header h3").click( // When the h3 is clicked...
			function () {
			  $(this).parent().next().toggle(); // Toggle the Content Box
			  $(this).parent().parent().toggleClass("closed-box"); // Toggle the class "closed-box" on the content box
			  $(this).parent().find(".content-box-tabs").toggle(); // Toggle the tabs
			}
		);

    // Content box tabs:
		
		$('.content-box .content-box-content div.tab-content').hide(); // Hide the content divs
		$('ul.content-box-tabs li a.default-tab').addClass('current'); // Add the class "current" to the default tab
		$('.content-box-content div.default-tab').show(); // Show the div with class "default-tab"
		
		$('.content-box ul.content-box-tabs li a').click( // When a tab is clicked...
			function() { 
				$(this).parent().siblings().find("a").removeClass('current'); // Remove "current" class from all tabs
				$(this).addClass('current'); // Add class "current" to clicked tab
				var currentTab = $(this).attr('href'); // Set variable "currentTab" to the value of href of clicked tab
				$(currentTab).siblings().hide(); // Hide all content divs
				$(currentTab).show(); // Show the content div with the id equal to the id of clicked tab
				return false; 
			}
		);

    //Close button:
		
		$(".close").click(
			function () {
				$(this).parent().fadeTo(400, 0, function () {
					$(this).slideUp(400);
				});
				return false;
			}
		);
		$('tbody tr:even').addClass("alt-row"); 
		$('.check-all').click(
			function(){
				$(this).parent().parent().parent().parent().find("input[type='checkbox']").attr('checked', $(this).is(':checked'));   
			}
		);

   

});
  
  
  