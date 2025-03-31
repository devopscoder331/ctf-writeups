#!/bin/sh

while ! nc -z mongo 27017; do
  echo "Waiting for MongoDB to be ready..."
  sleep 1
done

sleep 1
echo "MongoDB is ready"

node server.js
