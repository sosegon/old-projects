var words, users, hiddenPosts = 0;

function createHideStyle(){
	$('head').append($('<style></style').attr({rel: "stylesheet", type: "text/css"}).text(".hidden-post{display:none !important;}"));
}

function hidePosts(){
	
	$("li[data-post-id]").each(function(){
		var container = $(this);

		// hide posts by words
		var titles = container.find("h3");
		titles.each(function(){
			var text = $(this).text().toLowerCase();
			var hide = words.some(function(word){
				return text.indexOf(word.toLowerCase()) > -1;	
			});
			if(hide){
				container.addClass("hidden-post");
				hiddenPosts++;
			}

			return !hide;
		});

		// hide posts by user
		var usersDom = container.find(".usuario");
		usersDom.each(function(){
			var userName = $(this).attr("title").toLowerCase();
			var hide = users.some(function(user){
				return user.toLowerCase() === userName;
			});
			if(hide){
				container.addClass("hidden-post");
				hiddenPosts++;
			}

			return !hide;	
		});
	});
	console.log("hidden posts: " + hiddenPosts);
}

createHideStyle();

// window.onload = function(){
	chrome.extension.sendMessage({method: 'getOptions'}, function(res){
		words = res.words;
		users = res.users;
		hidePosts();
	});
// }
