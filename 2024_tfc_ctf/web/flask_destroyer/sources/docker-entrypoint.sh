#!/bin/bash
set -e

# Start MySQL service
service mysql start

# Wait for MySQL to be ready
sleep 10

# Create database and user, and apply the initialization script
mysql -e "CREATE DATABASE IF NOT EXISTS destroyer;"
mysql -e "CREATE USER IF NOT EXISTS 'bob'@'localhost' IDENTIFIED BY 'password';"
mysql -e "GRANT ALL PRIVILEGES ON destroyer.* TO 'bob'@'localhost';"
mysql -e "UPDATE mysql.user SET File_priv = 'Y' WHERE user='bob' AND host='localhost';"
mysql -e "FLUSH PRIVILEGES;"


# Run the initialization script
mysql destroyer < /docker-entrypoint-initdb.d/init.sql

# Run Python application
gunicorn -w 1 -b 0.0.0.0:1337 run:gunicorn_app