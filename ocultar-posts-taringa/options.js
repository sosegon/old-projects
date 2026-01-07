window.onload = function(){

	var bkg = chrome.extension.getBackgroundPage();
	var words = bkg.getItem("words");
	var users = bkg.getItem("users");
	
	if(words !== null)
		$("#words-to-avoid").val(words);
	if(users !== null)	
		$("#users-to-avoid").val(users);
	
	$("#save-config").click(function(){
		var obj = {};
		obj.words = stringToArray($("#words-to-avoid").val());
		obj.users = stringToArray($("#users-to-avoid").val());
		bkg.setItem('words', obj.words);
		bkg.setItem('users', obj.users);
		$("#message").css("display", "block");
	});

	function arrayToString(array){
		var string = "";
		array.forEach(function(element, index, array){
			var stringToAdd = index < array.length -1 ? element + "," : element;
			string += stringToAdd;
		});

		return string;
	}

	function stringToArray(string){
		var elements = string.split(",");
		elements.forEach(function(element, index, array){
			array[index] = element.trim().toLowerCase();	
		});	
		elements = elements.filter(function(element){
			return element.length > 0;	
		});
		
		return elements;
	}
}

