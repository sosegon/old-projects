# Movies list
A web site to display information of a list of movies

## Installation
### Linux

- Install apache

    `sudo apt-get install apache2`

- Install webpy
```
      sudo wget http://webpy.org/static/web.py-0.37.tar.gz
      sudo tar xf web.py-0.37.tar.gz 
      cd web.py-0.37
      sudo python setup.py  install
```

- Download the project [here](https://github.com/sosegon/moviesList/archive/master.zip)

- Unzip the project in the corresponding location, usually under `/var/www/html/`

- Change permissions of the folder

    `sudo chmod -R 755 /var/www/html/folder_name/`

- Add folder's name to apache configuration, usually `/etc/apache2/apache2.conf`
```
      <Directory /var/www/html/folder_name>
          AllowOverride None
          Require all granted
      </Directory>
```
- Install wsgi module

    `sudo apt-get install libapache2-mod-wsgi`

- Add configuration to apache, usually `etc/apache2/conf-enabled/serve-cgi-bin.conf`

```
      <IfModule mod_wsgi.c>
          WSGIScriptAlias /app/ /var/www/html/  folder_name/bin/app.py/
          <Directory /var/www/html/moviesList/bin>
              AllowOverride None
              Options None
              Require all granted
          </Directory>
      </IfModule>
```

- Restart apache

    `sudo service apache2 restart`

- Open a web browser and go to [http://localhost/app/](http://localhost/app/)

- If everything goes well, you will see something like this

<img src=http://i1041.photobucket.com/albums/b414/sosegon/Movies%20list.png width="500"></img>


## License

The [MIT](https://opensource.org/licenses/MIT) license.