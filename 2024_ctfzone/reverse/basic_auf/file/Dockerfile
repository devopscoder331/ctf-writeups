FROM httpd:2.4

RUN apt update

RUN apt install -y python3 python3-pip

COPY ./httpd/httpd.conf /usr/local/apache2/conf/httpd.conf

COPY ./httpd/server-configs.conf /usr/local/apache2/conf/server-configs.conf

COPY ./httpd/index.cgi ./httpd/index.html /usr/local/apache2/htdocs/