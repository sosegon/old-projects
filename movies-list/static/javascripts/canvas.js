"use strict";

function work(){
	var container, renderer, camera, scene, movies = [], planes = [];
	
	init();
	animate();

	function readMovies(){
		var moviesDom = document.getElementsByTagName('movies')[0].children;
		for(var i = 0; i < moviesDom.length; i++){
			var movie = JSON.parse(moviesDom[i].getAttribute("data"));
			movie.id = i;
			movie.youtubeId = moviesDom[i].getAttribute("youtubeId");
			movies.push(movie);
		}
	}

	function getYoutubeId(youtubeUrl){
		var id = ""
		return id;
	}

	function animate() {

		requestAnimationFrame( animate );
		TWEEN.update();
		renderer.render( scene, camera );
	}

	function init(){
		container = document.getElementById("canvas");

		renderer = new THREE.WebGLRenderer();
		renderer.setPixelRatio( window.devicePixelRatio );
		renderer.setSize( window.innerWidth, window.innerHeight);
		container.appendChild( renderer.domElement );	
		camera = new THREE.PerspectiveCamera( 70, window.innerWidth / window.innerHeight, 1, 1000 );
		camera.position.z = 330;

		scene = new THREE.Scene();
		//scene.add( new THREE.AmbientLight( 0xeef0ff ) );
		var light1 = new THREE.DirectionalLight( 0xffffff, 1 );
		light1.position.set( 0, 0, 100 );
		scene.add( light1 );

		window.addEventListener( 'resize', onWindowResize, false );
		renderer.domElement.addEventListener('mousewheel', movePlanes, false);
		renderer.domElement.addEventListener('mousedown', preselectPlane, false);
		renderer.domElement.addEventListener('mouseup', confirmPlane, false)

		readMovies();
		createPlanes();
	}

	function onWindowResize() {

		camera.aspect = window.innerWidth / window.innerHeight;
		camera.updateProjectionMatrix();

		renderer.setSize( window.innerWidth, window.innerHeight );
	}

	var mouseCoord = {x: Infinity, y:Infinity},
		position3D = new THREE.Vector3(),
		clonePosition3D = position3D.clone(),
		ray = new THREE.Raycaster();

	function preselectPlane(event){
		getOffset(event, mouseCoord);
	}

	function confirmPlane(event){
		var newCoord = getOffset(event);
		if(newCoord.x === mouseCoord.x && newCoord.y === mouseCoord.y){

			// set position3D
			position3D.set(
				(mouseCoord.x / renderer.domElement.offsetWidth) * 2 - 1,
				(mouseCoord.y / renderer.domElement.offsetHeight) * 2 - 1,
				0.5
			);

			// intersect planes
			clonePosition3D.copy(position3D);
			clonePosition3D.unproject(camera);
			ray.set(camera.position, clonePosition3D.sub(camera.position).normalize());

			var intersect = ray.intersectObjects(planes, false);
			
			if(intersect.length > 0)
				displayMovie(intersect[0].object);
			else
				displayMovie(undefined);
		}
	}

	var positionFront = new THREE.Vector3(0, 0, 170),
		planeOrigPosition = new THREE.Vector3(),
		selectedPlane = undefined,
		duration = 400;
	function displayMovie(plane){
		TWEEN.removeAll();
		if(plane === undefined){
			if(selectedPlane !== undefined){
				moveWithAnimation(selectedPlane, planeOrigPosition, duration);
				// selectedPlane.position.copy(planeOrigPosition);
				setMovieInfo();
				selectedPlane = undefined;
				wheelEnabled = true;
			}
			return;
		}
		for(var i = 0; i < movies.length; i++){
			if(movies[i].id === plane.userData.movieId){
				if(selectedPlane !== undefined && selectedPlane === plane){
					playTrailer(movies[i]);
				}
				else if(selectedPlane !== undefined && selectedPlane !== plane){
					moveWithAnimation(selectedPlane, planeOrigPosition, duration);
					// selectedPlane.position.copy(planeOrigPosition);
					planeOrigPosition.copy(plane.position);
					moveWithAnimation(plane, positionFront, duration);
					// plane.position.copy(positionFront);
					setMovieInfo(movies[i]);
					selectedPlane = plane
					wheelEnabled = false;
				}
				else if(selectedPlane === undefined){
					planeOrigPosition.copy(plane.position);
					moveWithAnimation(plane, positionFront, duration);
					//plane.position.copy(positionFront);
					setMovieInfo(movies[i]);
					selectedPlane = plane;
					wheelEnabled = false;
				}
				return;
			}
		}
	}

	function setMovieInfo(movie){
		if(movie === undefined){
			$("#movie-name").text("");
			$("#movie-description").text("");	
			$("#movie-info").css({display: "none"});
		}
		else
		{
			$("#movie-name").text(movie.name);
			$("#movie-description").text(movie.description);
			$("#movie-info").css({display: "block"});
		}
	}

	function moveWithAnimation(object, target, duration){
		new TWEEN.Tween(object.position)
				.to({x: target.x, y: target.y, z: target.z},  duration)
				.easing(TWEEN.Easing.Quartic.Out)
				.start();
	}

	var sourceUrl = "";

	function playTrailer(movie){
		sourceUrl = 'http://www.youtube.com/embed/' + movie.youtubeId + '?autoplay=1&html5=1';
		$("#trailer-video-container").empty().append($('<iframe></iframe>',{
			'id': 'trailer-video',
			'type': 'text-html',
			'src': sourceUrl,
			'frameborder': 0
		}));
		$("#trailer").css({'display': 'block'});
	}

	$(document).on('click', '.hanging-close, .modal-backdrop, .modal', function (event) {
        // Remove the src so the player itself gets removed, as this is the only
        // reliable way to ensure the video stops playing in IE
        $("#trailer-video-container").empty();
        $("#trailer").css({'display': 'none'});
    });

	function getOffset(mouseEvent, target){
		var target = target !== undefined ? target : {x: Infinity, y: Infinity};
	    var el = mouseEvent.target,
	        x = 0,
	        y = 0;

	    while (el && !isNaN(el.offsetLeft) && !isNaN(el.offsetTop)) {
	        x += el.offsetLeft - el.scrollLeft;
	        y += el.offsetTop - el.scrollTop;
	        el = el.offsetParent;
	    }

	    target.x = mouseEvent.clientX - x;
	    target.y = mouseEvent.clientY - y;

	    return target;
	}

	var wheelEnabled = true;
	var transition = 200;
	function movePlanes(event){
		if(!wheelEnabled) return;
		var prevPosition = new THREE.Vector3();
		var currentPosition = new THREE.Vector3();
		if(event.wheelDelta > 0){
			planes.forEach(function(plane, index, array){
				currentPosition.copy(plane.position);
				if(index === 0)
					moveWithAnimation(plane, array[array.length - 1].position, transition);
					// plane.position.copy(array[array.length - 1].position);
				else
					moveWithAnimation(plane, prevPosition, transition);
					// plane.position.copy(prevPosition);

				prevPosition.copy(currentPosition);
			});
		}
		else{
			for(var i = planes.length - 1; i >= 0; i--){
				currentPosition.copy(planes[i].position);
				if(i === planes.length - 1)
					moveWithAnimation(planes[i], planes[0].position, transition);
					// planes[i].position.copy(planes[0].position);
				else
					moveWithAnimation(planes[i], prevPosition, transition);
					// planes[i].position.copy(prevPosition);

				prevPosition.copy(currentPosition);
			}
		}
	}

	function createPlanes(){

		var planePositions = [];
		var startPosition = new THREE.Vector3(200, 0, 0);
		var row = -1;
		movies.forEach(function(movie, index){
			row = index % 3 === 0 ? row + 1 : row;			
			
			var pos = startPosition.clone();
			pos.x -= index % 3 * 200;
			pos.z -= row * 30;

			planePositions.push(pos)
		});

		var geometry = new THREE.PlaneGeometry(120, 200, 1, 1);
		THREE.ImageUtils.crossOrigin = '';
		movies.forEach(function(movie, index){
			THREE.ImageUtils.loadTexture(movie.poster_url, undefined,
				function(texture){
					texture.minFilter = THREE.NearestFilter;
					var material = new THREE.MeshPhongMaterial({side: THREE.DoubleSide, map: texture});
					var plane = new THREE.Mesh(geometry, material);
					plane.position.copy(planePositions[index]);
					plane.userData.movieId = movie.id;
					planes.push(plane);
					movie.planeCreated = true;
					addPlanes();
				},
				function(){
					console.log("error: " + movie.poster_url);
				}
			);
		});
	}

	function addPlanes(){
		var readyToOrder = movies.every(function(movie){
			return movie.hasOwnProperty("planeCreated")
		});

		if(!readyToOrder) return;

		planes.sort(function(a, b){
			return a.userData.movieId - b.userData.movieId;
		});

		planes.forEach(function(plane){
			scene.add(plane);
		});

		animate();
	}
}

window.onload = work;