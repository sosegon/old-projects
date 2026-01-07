import media
import json

def read_movies_file(file):
	movie_file = open(file)

	# reference to read json file http://stackoverflow.com/a/2835672
	with movie_file as data_file:
		data = json.load(data_file)

	movies_list = data["movies"]
	movies = []
	for movie in movies_list:
		movies.append(media.Movie(movie["name"], movie["description"], movie["trailer_url"], movie["poster_url"]))

	return movies
