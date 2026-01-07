# code template from http://learnpythonthehardway.org/book/ex46.html

try:
	from setuptools import setuptools
except ImportError:
	from distutils.core import setup


config = {
	'description': 'A website that displays a list of movies',
	'author': 'Sebastian Velasquez',
	'url': '',
	'download_url': 'https://github.com/sosegon/moviesList',
	'author_email': 'anse23@hotmail.com',
	'version': '0.1',
	'install_requires':['nose'],
	'packages': ['movies'],
	'scripts': [],
	'name': 'moviesList'
}

setup(**config)