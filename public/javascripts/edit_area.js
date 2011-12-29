/*
editArea functions - utilized by save and edit the codes
Further informations: https://github.com/franzejr/qefweb
*/
editAreaLoader.init({
			id : "codeArea"		
			,syntax: "xml"			
			,start_highlight: true		
			,show_line_colors: true
			,allow_toggle: false
			,toolbar: "save,|, search, go_to_line, |, undo, redo, |, select_font, |, change_smooth_selection, highlight, reset_highlight, |, help"
			,save_callback: "my_save"

		});
		
		
		// callback functions
		function my_save(id, content){
			
			//Show the dialog to put the options
			$('#saveOptions').dialog();
			$('#saveOptions').dialog({ buttons: [
						                                    {
						                                        text: "Send",
						                                        click: function() { 
						                                        
						                                        	
																 		var code = $('#codeArea').html();
																 		var queryName = $('#queryName').val();
																 		var queryDescription = $('#queryDescription').val();
																 		
																 		$.post('/save/', {fileName:queryName,fileContents:code});
																 		
																 		$(this).dialog("close");
						                                        
						                                        
						                                        }
						                                    }
						                                ] });
			$('#saveOptions').dialog( "option", "closeOnEscape", true );
			$('#saveOptions').dialog( "option", "minWidth", 420 );
			$('#saveOptions').html('<b>Query Name:</b> <input type="text" id="queryName" /><br/>'+'<b>Query Description:</b> <textarea id="queryDescription" rows="4" cols="40"></textarea><br/>');
		
		}
		
