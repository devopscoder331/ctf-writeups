#!/bin/sh

sleep 3

if [ ! -d "migrations" ]; then
  flask db init
  flask db migrate -m "Initial migration"
fi

echo "Applying database migrations..."
flask db upgrade

echo "Starting application..."
python fill_db.py
exec flask run -h 0.0.0.0 -p 8080
# exec gunicorn --bind 0.0.0.0:8080 "app:app"
