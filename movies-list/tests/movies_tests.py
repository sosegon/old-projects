from nose.tools import *
from movies.media import Movie

def test_movie():
	the_cube = Movie(
		"The Cube",
		"""A movie about six people trying to scape from a cube""",
		"https://www.youtube.com/watch?v=MY5PkidV1cM",
		"https://en.wikipedia.org/wiki/File:Cube_The_Movie_Poster_Art.jpg"
	)

	assert_equal(the_cube.name, "The Cube")
	assert_equal(the_cube.description, "A movie about six people trying to scape from a cube")