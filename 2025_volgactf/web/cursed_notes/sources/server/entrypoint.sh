#!/bin/sh

while ! nc -z $POSTGRES_HOST $POSTGRES_PORT; do
    echo "Waiting for PostgreSQL to be ready..."
    sleep 1
done

python3 manage.py migrate

DJANGO_SUPERUSER_PASSWORD=$ADMIN_PASSWORD \
DJANGO_SUPERUSER_USERNAME=definetlynotadmin \
DJANGO_SUPERUSER_EMAIL=admin@volgactf.ru \
python3 manage.py createsuperuser --noinput

gunicorn -w 10 -b 0.0.0.0:8000 task.wsgi:application