#!/usr/bin/python

import json
import re

class Movie():

	def __init__(self, name, description, trailer_url, poster_url):
		self.name = name
		self.description = description
		self.trailer_url = trailer_url
		self.poster_url = poster_url

	def toJSON(self):
		#return json.dumps(self, cls = MovieEncoder)
		return '{"name":"' + self.name + '", "description":"' + self.description + '", "trailer_url":"' + self.trailer_url + '", "poster_url":"' + self.poster_url + '"}'

	def extractYoutubeId(self):
		youtube_id_match = re.search(r'(?<=v=)[^&#]+', self.trailer_url)
		youtube_id_match = youtube_id_match or re.search(r'(?<=be/)[^&#]+', self.trailer_url)
		return youtube_id_match.group(0) if youtube_id_match else None

