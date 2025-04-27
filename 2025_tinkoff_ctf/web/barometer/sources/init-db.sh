#!/bin/sh
set -e

export PGPASSWORD=$DB_PASSWORD

echo "Waiting for PostgreSQL to be ready..."
while ! pg_isready -h db -p 5432 -U postgres; do
    sleep 1
done

echo "Creating database if it doesn't exist..."
psql -h db -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'weather_db'" | grep -q 1 || \
psql -h db -U postgres -c "CREATE DATABASE weather_db"

echo "Running migrations..."
psql -h db -U postgres -d weather_db -f /app/init.sql || true

echo "Database initialization complete!"
exec gunicorn --bind 0.0.0.0:5000 --workers 4 app:app 
