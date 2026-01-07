var ls = $.localStorage;

function getItem(item){
	return ls.get(item);
}

function setItem(key, value){
	ls.set(key, value);
}

chrome.extension.onMessage.addListener(function(req, sender, sendResponse){
	if(req.method === "getOptions"){
		var words = ls.get('words');
		var users = ls.get('users'); 
		sendResponse({users: users, words: words});
	}
	else{
		sendResponse({});
	}
});