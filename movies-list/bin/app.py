#!/usr/bin/python

# basic code from http://learnpythonthehardway.org/book/ex50.html
# code for using mod_wsgi http://www.thefourtheye.in/2013/03/deploying-webpy-application-in-apache.html
import web, os, sys

def add_to_sys_path(path):
    if path in sys.path:
        print "path already is sys.path"
    else:
        sys.path.append(path)
        print "path added to sys.path"

parent_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)))
bin_dir = parent_dir + "/bin/"
movies_dir = parent_dir + "/movies/"
templates_dir = parent_dir + "/templates/"
static_dir = parent_dir + "/static/"

sys.path.append(bin_dir)
sys.path.append(movies_dir)
sys.path.append(templates_dir)

urls = (
	'/.*', 'Index'
)

app = web.application(urls, globals())

render = web.template.render(templates_dir)

if __name__ == "__main__":
    from movies.entertainment_center import *
else:
    from entertainment_center import *

class Index:
	def GET(self):
		movies = read_movies_file(static_dir + "list.json")
		return render.index(movies)

if __name__ == "__main__":
    app.run()
else:
    application = app.wsgifunc()
